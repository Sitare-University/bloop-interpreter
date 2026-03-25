import java.util.HashMap;
import java.util.Map;

public class Environment {
    // Maps variable names to their current values (Double or String)
    private final Map<String, Object> store = new HashMap<>();

    /**
     * Store or update a variable's value.
     */
    public void set(String name, Object value) {
        store.put(name, value);
    }

    /**
     * Retrieve a variable's current value.
     * Throws RuntimeException if the variable hasn't been defined yet.
     */
    public Object get(String name) {
        if (!store.containsKey(name)) {
            throw new RuntimeException("Variable not defined: " + name);
        }
        return store.get(name);
    }
}
