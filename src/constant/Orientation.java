package constant;

public enum Orientation {
    FACING_NORTH("FacingNorth"),
    FACING_SOUTH("FacingSouth"),
    FACING_EAST("FacingEast"),
    FACING_WEST("FacingWest");

    Orientation(String sym) {
        symbol = sym;
    }

    private final String symbol;

    public String getSymbol() {
        return symbol;
    }
}