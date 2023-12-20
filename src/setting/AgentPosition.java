package setting;
import constant.Orientation;

public class AgentPosition {

    private final Room room;
    private final Orientation orientation;

    public Room getRoom() {
        return room;
    }

    public int getX() {
        return room.getX();
    }

    public int getY() {
        return room.getY();
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public AgentPosition(Room room, Orientation orientation) {
        this.room = room;
        this.orientation = orientation;
    }

    public AgentPosition(int x, int y, Orientation orientation) {
        this(new Room(x, y), orientation);
    }

    @Override
    public String toString() {
        return room.toString() + "->" + orientation.getSymbol();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && getClass() == obj.getClass()) {
            AgentPosition other = (AgentPosition) obj;
            return (getX() == other.getX()) && (getY() == other.getY())
                    && (orientation == other.getOrientation());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + room.hashCode();
        result = 43 * result + orientation.hashCode();
        return result;
    }
}
