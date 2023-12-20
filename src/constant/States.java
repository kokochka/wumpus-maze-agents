package constant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class States {
    public final static Map<Integer, String> STATES = new LinkedHashMap<>() {{
        put(1, "Stench");
        put(2, "Breeze");
        put(3, "Glitter");
        put(4, "Scream");
    }};
}