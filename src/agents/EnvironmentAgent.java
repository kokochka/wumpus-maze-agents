package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import constant.TextConstants;
import setting.AgentPosition;
import setting.Room;
import setting.WumpusCave;
import setting.WumpusPercept;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class EnvironmentAgent extends Agent {
    private WumpusCave cave;
    private boolean isWumpusAlive = true;
    private boolean isGoldGrabbed;
    private AgentPosition agentPosition;
    private boolean hasArrow = true;
    private final String agentName;
    private static final String INITIAL_WUMPUS_CAVE = ". . . P W G . . . . . . S . P . ";

    public EnvironmentAgent(WumpusCave cave) {
        this.cave = cave;
        this.agentName = "Wumpus world";
    }

    public EnvironmentAgent() {
        this(new WumpusCave(4, 4, INITIAL_WUMPUS_CAVE));
    }

    public WumpusCave getCave() {
        return cave;
    }

    public boolean isWumpusAlive() {
        return isWumpusAlive;
    }

    public boolean isGoalGrabbed() {
        return isGoldGrabbed;
    }

    public AgentPosition getAgentPosition() {
        return agentPosition;
    }

    private void resetWorld() {
        cave = new WumpusCave(4, 4, INITIAL_WUMPUS_CAVE);

        agentPosition = cave.getStart();

        isWumpusAlive = true;
        isGoldGrabbed = false;
        hasArrow = true;
    }

    @Override
    protected void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(TextConstants.WUMPUS_WORLD_TYPE);
        sd.setName(TextConstants.WUMPUS_SERVICE_DESCRIPTION);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println(agentName + ": Agent " + getAID().getName() + " is ready.");
        System.out.println(agentName + ": Current world state:");
        System.out.println(cave);

        addBehaviour(new WumpusWorldGameProcessing());
        addBehaviour(new WumpusWorldGameInformation());
        addBehaviour(new SpeleologistProcessing());
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

    private void turnLeft() {
        agentPosition = cave.turnLeft(agentPosition);
    }

    private void turnRight() {
        agentPosition = cave.turnRight(agentPosition);
    }

    private boolean moveForward() {
        agentPosition = cave.moveForward(agentPosition);

        return (isWumpusAlive && cave.getWumpusRoom().equals(agentPosition.getRoom())) || cave.isPit(agentPosition.getRoom());
    }

    private void grabItem() {
        if (cave.getGold().equals(agentPosition.getRoom()))
            isGoldGrabbed = true;
    }

    private void shoot() {
        if (hasArrow && isFacingWumpus(agentPosition))
            isWumpusAlive = false;
    }

    private boolean climb() {
        return agentPosition.getRoom().equals(new Room(1, 1)) && isGoldGrabbed;
    }

    private boolean isFacingWumpus(AgentPosition pos) {
        var wumpus = cave.getWumpusRoom();

        return switch (pos.getOrientation()) {
            case FACING_NORTH -> pos.getX() == wumpus.getX() && pos.getY() < wumpus.getY();
            case FACING_SOUTH -> pos.getX() == wumpus.getX() && pos.getY() > wumpus.getY();
            case FACING_EAST -> pos.getY() == wumpus.getY() && pos.getX() < wumpus.getX();
            case FACING_WEST -> pos.getY() == wumpus.getY() && pos.getX() > wumpus.getX();
        };
    }

    private WumpusPercept getPerceptSeenBy() {
        WumpusPercept result = new WumpusPercept();
        AgentPosition pos = agentPosition;

        List<Room> adjacentRooms = Arrays.asList(
                new Room(pos.getX() - 1, pos.getY()), new Room(pos.getX() + 1, pos.getY()),
                new Room(pos.getX(), pos.getY() - 1), new Room(pos.getX(), pos.getY() + 1)
        );
        List<Room> adjacentRoomsFull = new LinkedList<>();

        for (Room r : adjacentRooms) {
            adjacentRoomsFull.addAll(Arrays.asList(new Room(r.getX() - 1, r.getY()), new Room(r.getX() + 1, r.getY()),
                    new Room(r.getX(), r.getY() - 1), new Room(r.getX(), r.getY() + 1)));
        }
        adjacentRoomsFull.addAll(adjacentRooms);

        for (Room r : adjacentRoomsFull) {
            if (r.equals(cave.getWumpusRoom()))
                result.setStench();
            if (cave.isPit(r))
                result.setBreeze();
        }

        if (pos.getRoom().equals(cave.getGold()))
            result.setGlitter();
        if (!isWumpusAlive)
            result.setScream();

        return result;
    }

    // Behaviours
    private class WumpusWorldGameProcessing extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                String message = msg.getContent();
                System.out.println(agentName + ": Current state:");
                System.out.println(cave);

                boolean sendTerminateMessage = false;
                boolean sendWinMessage = false;

                switch (message) {
                    case TextConstants.SPELEOLOGIST_TURN_LEFT:
                        turnLeft();
                        break;
                    case TextConstants.SPELEOLOGIST_TURN_RIGHT:
                        turnRight();
                        break;
                    case TextConstants.SPELEOLOGIST_MOVE_FORWARD:
                        sendTerminateMessage = moveForward();
                        break;
                    case TextConstants.SPELEOLOGIST_GRAB:
                        grabItem();
                        break;
                    case TextConstants.SPELEOLOGIST_SHOOT:
                        shoot();
                        break;
                    case TextConstants.SPELEOLOGIST_CLIMB:
                        if (climb())
                            sendWinMessage = true;
                        else
                            sendTerminateMessage = true;
                        break;
                    default:
                        System.out.println(agentName + ": There is no action!");
                        break;
                }

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                if (!sendTerminateMessage) {
                    reply.setContent(TextConstants.OK_MESSAGE);
                } else if (sendWinMessage) {
                    reset();
                    reply.setContent(TextConstants.WIN_MESSAGE);
                } else {
                    reset();
                    reply.setContent(TextConstants.FAIL_MESSAGE);
                }

                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    private class WumpusWorldGameInformation extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String message = msg.getContent();
                if (message.equals(TextConstants.GAME_INFORMATION)) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    WumpusPercept current = getPerceptSeenBy();
                    String g = current.toString();
                    reply.setContent(g);
                    myAgent.send(reply);
                }
            } else {
                block();
            }
        }
    }

    private class SpeleologistProcessing extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String message = msg.getContent();
                if (Objects.equals(message, TextConstants.GO_INSIDE)) {
                    agentPosition = cave.getStart();
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.CONFIRM);
                    reply.setContent(TextConstants.OK_MESSAGE);
                    myAgent.send(reply);
                }

            } else {
                block();
            }
        }
    }
}