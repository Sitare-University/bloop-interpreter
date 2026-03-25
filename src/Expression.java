public interface Expression {
    /**
     * Evaluate this expression using the current variable store.
     * Returns either a Double (for numbers) or a String (for text).
     */
    Object evaluate(Environment env);
}


// ─── NumberNode ─────────────────────────────────────────────────────────────

class NumberNode implements Expression {
    private final double value;

    public NumberNode(double value) {
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        return value;
    }
}

// ─── StringNode ─────────────────────────────────────────────────────────────

class StringNode implements Expression {
    private final String value;

    public StringNode(String value) {
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        return value;
    }
}