// ─── Expression Interface ───────────────────────────────────────────────────

public interface Expression {
    /**
     * Evaluate this expression using the current variable store.
     * Returns either a Double (for numbers) or a String (for text).
     */
    Object evaluate(Environment env);
}


