package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import constant.Commands;
import constant.TextConstants;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeroAgent extends Agent {

    private AID wumpusWorld;
    private AID navigationAgent;

    private final String agentName;

    public HeroAgent() {
        this.agentName = "Speleologist";
    }

    @Override
    protected void setup() {
        System.out.println(agentName + ": Agent " + getAID().getName() + " is ready.");

        addBehaviour(new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {
                myAgent.addBehaviour(new WumpusWorldFinder());
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println(agentName + ": Agent " + getAID().getName() + " shutting down.");
    }

    // Behaviours
    private class NavigatorAgentFinder extends Behaviour {

        private boolean isDone = false;

        @Override
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(TextConstants.NAVIGATOR_AGENT_TYPE);
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result != null && result.length > 0) {
                    navigationAgent = result[0].getName();
                    myAgent.addBehaviour(new NavigatorAgentProcessing());
                    isDone = true;
                } else {
                    block();
                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean done() {
            return isDone;
        }
    }

    private class NavigatorAgentProcessing extends Behaviour {
        private int step = 0;
        private MessageTemplate mt;
        private ACLMessage reply;

        @Override
        public void action() {
            switch (step) {
                case 0:
                    ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                    inform.addReceiver(navigationAgent);
                    inform.setContent(TextConstants.ADVICE_PROPOSAL);
                    inform.setConversationId(TextConstants.NAVIGATOR_DIGGER_CONVERSATION_ID);
                    inform.setReplyWith("inform" + System.currentTimeMillis());

                    System.out.println(agentName + ": " + TextConstants.ADVICE_PROPOSAL);
                    myAgent.send(inform);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId(TextConstants.NAVIGATOR_DIGGER_CONVERSATION_ID),
                            MessageTemplate.MatchInReplyTo(inform.getReplyWith()));
                    step++;

                    break;
                case 1:
                    reply = myAgent.receive(mt);

                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.REQUEST) {
                            if (parseNavigatorMessageRequest(reply.getContent())) {
                                ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                                req.addReceiver(wumpusWorld);
                                req.setContent(TextConstants.GAME_INFORMATION);
                                req.setConversationId(TextConstants.WUMPUS_WORLD_DIGGER_CONVERSATION_ID);
                                req.setReplyWith("req" + System.currentTimeMillis());

                                myAgent.send(req);
                                mt = MessageTemplate.and(MessageTemplate.MatchConversationId(TextConstants.WUMPUS_WORLD_DIGGER_CONVERSATION_ID),
                                        MessageTemplate.MatchInReplyTo(req.getReplyWith()));
                                step++;
                            } else
                                System.out.println(agentName + ": There is no command.");
                        }
                    } else {

                        block();
                    }
                    break;
                case 2:
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            String rep = reply.getContent();
                            ACLMessage mes = new ACLMessage(ACLMessage.INFORM);
                            mes.addReceiver(navigationAgent);
                            mes.setContent(TextConstants.INFORMATION_PROPOSAL_SPELEOLOGIST + rep);
                            mes.setConversationId(TextConstants.NAVIGATOR_DIGGER_CONVERSATION_ID);
                            mes.setReplyWith("mes" + System.currentTimeMillis());

                            System.out.println(agentName + ": " + TextConstants.INFORMATION_PROPOSAL_SPELEOLOGIST + rep);

                            myAgent.send(mes);
                            mt = MessageTemplate.and(MessageTemplate.MatchConversationId(TextConstants.NAVIGATOR_DIGGER_CONVERSATION_ID),
                                    MessageTemplate.MatchInReplyTo(mes.getReplyWith()));
                            step++;
                        }
                    } else
                        block();
                    break;
                case 3:
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            String message = reply.getContent();
                            String action = parseNavigatorMessageProposal(message);
                            String content = "";

                            switch (action) {
                                case Commands.TURN_LEFT -> content = TextConstants.SPELEOLOGIST_TURN_LEFT;
                                case Commands.TURN_RIGHT -> content = TextConstants.SPELEOLOGIST_TURN_RIGHT;
                                case Commands.MOVE_FORWARD -> content = TextConstants.SPELEOLOGIST_MOVE_FORWARD;
                                case Commands.GRAB -> content = TextConstants.SPELEOLOGIST_GRAB;
                                case Commands.SHOOT -> content = TextConstants.SPELEOLOGIST_SHOOT;
                                case Commands.CLIMB -> content = TextConstants.SPELEOLOGIST_CLIMB;
                                default -> System.out.println(agentName + ": There is no action.");
                            }

                            ACLMessage prop = new ACLMessage(ACLMessage.PROPOSE);
                            prop.addReceiver(wumpusWorld);
                            prop.setContent(content);
                            prop.setConversationId(TextConstants.WUMPUS_WORLD_DIGGER_CONVERSATION_ID);
                            prop.setReplyWith("prop" + System.currentTimeMillis());

                            myAgent.send(prop);
                            mt = MessageTemplate.and(MessageTemplate.MatchConversationId(TextConstants.WUMPUS_WORLD_DIGGER_CONVERSATION_ID),
                                    MessageTemplate.MatchInReplyTo(prop.getReplyWith()));
                            step++;
                        }
                    } else {
                        block();
                    }
                    break;
                case 4:
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        String answer = reply.getContent();

                        switch (answer) {
                            case TextConstants.FAIL_MESSAGE -> {
                                System.out.println(agentName + ": You failed!");
                                step++;
                                doDelete();
                            }
                            case TextConstants.OK_MESSAGE -> {
                                System.out.println(agentName + ": Wumpus world returns OK.");
                                step = 0;
                            }
                            case TextConstants.WIN_MESSAGE -> {
                                System.out.println(agentName + ": The speleologist won!");
                                step++;
                                doDelete();
                            }
                        }
                    } else {
                        block();
                    }
                    break;
            }
        }

        @Override
        public boolean done() {
            return step == 5;
        }

        private String parseNavigatorMessageProposal(String instruction) {
            for (Map.Entry<Integer, String> entry : Commands.WORDS.entrySet()) {
                String value = entry.getValue();
                Pattern pattern = Pattern.compile("\\b" + value + "\\b", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(instruction);

                if (matcher.find()) {
                    String res = matcher.group();
                    return res.length() > 0 ? res : "";
                }
            }
            return "";
        }

        private boolean parseNavigatorMessageRequest(String instruction) {
            String regex = "\\binformation\\b";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(instruction);

            if (matcher.find()) {
                String res = matcher.group();
                return res.length() > 0;
            }

            return false;
        }

    }

    private class WumpusWorldFinder extends Behaviour {

        private boolean isDone = false;

        @Override
        public void action() {
            System.out.println(agentName + ": Start finding a Wumpus world");

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(TextConstants.WUMPUS_WORLD_TYPE);
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result != null && result.length > 0) {
                    wumpusWorld = result[0].getName();
                    myAgent.addBehaviour(new WumpusWorldProcessing());
                } else {
                    System.out.println(agentName + ": no Wumpus world available.");
                    block();
                }
                isDone = true;
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean done() {
            return isDone;
        }
    }

    private class WumpusWorldProcessing extends Behaviour {

        private MessageTemplate mt;
        private int step = 0;

        @Override
        public void action() {
            switch (step) {
                case 0 -> {
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    cfp.addReceiver(wumpusWorld);
                    cfp.setContent(TextConstants.GO_INSIDE);
                    cfp.setConversationId(TextConstants.WUMPUS_WORLD_DIGGER_CONVERSATION_ID);
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());

                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId(TextConstants.WUMPUS_WORLD_DIGGER_CONVERSATION_ID),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                }
                case 1 -> {
                    ACLMessage reply = myAgent.receive(mt);

                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.CONFIRM) {
                            System.out.println(agentName + ": Found a Wumpus World.");
                            super.myAgent.addBehaviour(new TickerBehaviour(super.myAgent, 5000) {
                                @Override
                                protected void onTick() {
                                    myAgent.addBehaviour(new NavigatorAgentFinder());
                                }
                            });
                            step++;
                        }
                    } else {
                        block();
                    }
                }
            }
        }

        @Override
        public boolean done() {
            return step == 2;
        }
    }

}

