package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import constant.States;
import constant.TextConstants;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NavigatorAgent extends Agent {

    private final String agentName;

    public NavigatorAgent() {
        this.agentName = "Navigator";
    }

    @Override
    protected void setup() {
        System.out.println(agentName + ": Agent " + getAID().getName() + " is ready.");

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(TextConstants.NAVIGATOR_AGENT_TYPE);
        sd.setName(TextConstants.NAVIGATOR_SERVICE_DESCRIPTION);
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new LocationRequestsServer());
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        System.out.println(agentName + ": Agent " + getAID().getName() + " shutting down.");
    }

    // Behaviours
    private class LocationRequestsServer extends CyclicBehaviour {

        int time = 0;

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                if (parseSpeleologistMessageRequest(msg.getContent())) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REQUEST);
                    reply.setContent(TextConstants.INFORMATION_PROPOSAL_NAVIGATOR);
                    System.out.println(agentName + ": " + TextConstants.INFORMATION_PROPOSAL_NAVIGATOR);
                    myAgent.send(reply);
                } else if (parseSpeleologistMessageProposal(msg.getContent())) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.PROPOSE);
                    String advice = getAdvice(msg.getContent());
                    reply.setContent(advice);
                    System.out.println(agentName + ": " + advice);
                    myAgent.send(reply);

                } else
                    System.out.println(agentName + ": Wrong message.");
            } else {
                block();
            }
        }

        private boolean parseSpeleologistMessageRequest(String instruction) {
            String regex = "\\bAdvice\\b";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(instruction);

            if (matcher.find()) {
                String res = matcher.group();
                return res.length() > 0;
            }

            return false;
        }

        private boolean parseSpeleologistMessageProposal(String instruction) {
            String regex = "\\bGiving\\b";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(instruction);

            if (matcher.find()) {
                String res = matcher.group();
                return res.length() > 0;
            }

            return false;
        }

        private String getAdvice(String content) {
            boolean stench = false;
            boolean breeze = false;
            boolean glitter = false;
            boolean scream = false;
            String advicedAction = "";

            for (Map.Entry<Integer, String> entry : States.STATES.entrySet()) {
                String value = entry.getValue();
                Pattern pattern = Pattern.compile("\\b" + value + "\\b", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(content);

                if (matcher.find()) {
                    switch (value) {
                        case "Stench":
                            stench = true;
                        case "Breeze":
                            breeze = true;
                        case "Glitter":
                            glitter = true;
                        case "Scream":
                            scream = true;
                    }
                }
            }

            switch (time) {
                case 0, 2, 4, 6 -> {
                    advicedAction = TextConstants.MESSAGE_FORWARD;
                    time++;
                }
                case 1 -> {
                    advicedAction = TextConstants.MESSAGE_RIGHT;
                    time++;
                }
                case 3, 5 -> {
                    advicedAction = TextConstants.MESSAGE_LEFT;
                    time++;
                }
            }

            int rand = 1 + (int) (Math.random() * 3);

            return switch (rand) {
                case 1 -> TextConstants.ACTION_PROPOSAL1 + advicedAction;
                case 2 -> TextConstants.ACTION_PROPOSAL2 + advicedAction;
                case 3 -> TextConstants.ACTION_PROPOSAL3 + advicedAction;
                default -> "";
            };
        }
    }
}
