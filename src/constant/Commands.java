package constant;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Commands {
    public static final String TURN_LEFT = "left";
    public static final String TURN_RIGHT = "right";
    public static final String MOVE_FORWARD = "forward";
    public static final String GRAB = "grab";
    public static final String SHOOT = "shoot";
    public static final String CLIMB = "climb";

    public static final Map<Integer, String> WORDS = new LinkedHashMap<Integer, String>() {{
        put(1, "left");
        put(2, "right");
        put(3, "forward");
        put(4, "grab");
        put(5, "shoot");
        put(6, "climb");
    }};
}