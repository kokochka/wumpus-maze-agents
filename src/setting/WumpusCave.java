package setting;
import constant.Orientation;
import constant.TextConstants;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class WumpusCave {

    private final int caveXDim;
    private final int caveYDim;
    private Room wumpusRoom;
    private Room gold;
    private AgentPosition start = new AgentPosition(1, 1, Orientation.FACING_NORTH);
    private final Set<Room> pits = new LinkedHashSet<>();
    private final Set<Room> allowedRooms;

    public WumpusCave() {
        this(4, 4);
    }

    public WumpusCave setAllowed(Set<Room> allowedRooms) {
        this.allowedRooms.clear();
        this.allowedRooms.addAll(allowedRooms);

        return this;
    }

    public void setWumpusRoom(Room room) {
        wumpusRoom = room;
    }

    public void setGold(Room room) {
        gold = room;
    }

    public void setPit(Room room, boolean b) {
        if (!b)
            pits.remove(room);
        else if (!room.equals(start.getRoom()) && !room.equals(gold))
            pits.add(room);
    }

    public int getCaveXDim() {
        return caveXDim;
    }

    public int getCaveYDim() {
        return caveYDim;
    }

    public AgentPosition getStart() {
        return start;
    }

    public Room getWumpusRoom() {
        return wumpusRoom;
    }

    public Room getGold() {
        return gold;
    }

    public boolean isPit(Room room) {
        return pits.contains(room);
    }

    public WumpusCave(int caveXDim, int caveYDim) {
        if (caveXDim < TextConstants.DIMENSION_THRESHOLD || caveYDim < TextConstants.DIMENSION_THRESHOLD)
            throw new IllegalArgumentException("Cave must have dimensions greater than 1");

        this.caveXDim = caveXDim;
        this.caveYDim = caveYDim;

        allowedRooms = getAllRooms();
    }

    public Set<Room> getAllRooms() {
        Set<Room> allowedRooms = new HashSet<>();

        for (int x = 1; x <= caveXDim; x++)
            for (int y = 1; y <= caveYDim; y++)
                allowedRooms.add(new Room(x, y));

        return allowedRooms;
    }

    public WumpusCave(int caveXDim, int caveYDim, String config) {
        this(caveXDim, caveYDim);

        for (int i = 0; i < config.length(); i++) {
            char c = config.charAt(i);
            Room r = new Room(i / 2 % caveXDim + 1, caveYDim - i / 2 / caveXDim);

            switch (c) {
                case 'S' -> start = new AgentPosition(r.getX(), r.getY(), Orientation.FACING_NORTH);
                case 'W' -> wumpusRoom = r;
                case 'G' -> gold = r;
                case 'P' -> pits.add(r);
            }
        }
    }

    // Actions
    public AgentPosition turnLeft(AgentPosition position) {
        Orientation orientation = switch (position.getOrientation()) {
            case FACING_NORTH -> Orientation.FACING_WEST;
            case FACING_SOUTH -> Orientation.FACING_EAST;
            case FACING_EAST -> Orientation.FACING_NORTH;
            case FACING_WEST -> Orientation.FACING_SOUTH;
        };
        start = new AgentPosition(position.getX(), position.getY(), orientation);

        return start;
    }

    public AgentPosition turnRight(AgentPosition position) {
        Orientation orientation = switch (position.getOrientation()) {
            case FACING_NORTH -> Orientation.FACING_EAST;
            case FACING_SOUTH -> Orientation.FACING_WEST;
            case FACING_EAST -> Orientation.FACING_SOUTH;
            case FACING_WEST -> Orientation.FACING_NORTH;
        };
        start = new AgentPosition(position.getX(), position.getY(), orientation);

        return start;
    }

    public AgentPosition moveForward(AgentPosition position) {
        int x = position.getX();
        int y = position.getY();

        switch (position.getOrientation()) {
            case FACING_NORTH -> y++;
            case FACING_SOUTH -> y--;
            case FACING_EAST -> x++;
            case FACING_WEST -> x--;
        }

        Room room = new Room(x, y);
        start = allowedRooms.contains(room) ? new AgentPosition(x, y, position.getOrientation()) : position;

        return start;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int y = caveYDim; y >= 1; y--) {
            for (int x = 1; x <= caveXDim; x++) {
                Room r = new Room(x, y);
                String text = "";

                if (r.equals(start.getRoom()))
                    text += "S";

                if (r.equals(gold))
                    text += "G";

                if (r.equals(wumpusRoom))
                    text += "W";

                if (isPit(r))
                    text += "P";

                if (text.isEmpty())
                    text = ". ";
                else if (text.length() == 1)
                    text += " ";
                else if (text.length() > 2)
                    text = text.substring(0, 2);

                builder.append(text);
            }
            builder.append("\n");
        }

        return builder.toString();
    }
}