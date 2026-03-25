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

// ─── VariableNode ───────────────────────────────────────────────────────────

class VariableNode implements Expression {
    private final String name;

    public VariableNode(String name) {
        this.name = name;
    }

    @Override
    public Object evaluate(Environment env) {
        // Looks up the variable's current value in the Environment.
        return env.get(name);
    }
}


// ─── BinaryOpNode ───────────────────────────────────────────────────────────

class BinaryOpNode implements Expression {
    private final Expression left;
    private final String operator;  // "+", "-", "*", "/", ">", "<", "=="
    private final Expression right;

    public BinaryOpNode(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public Object evaluate(Environment env) {
        Object leftVal  = left.evaluate(env);
        Object rightVal = right.evaluate(env);

        // Arithmetic operators — both sides must be numbers
        if (operator.equals("+") || operator.equals("-")
                || operator.equals("*") || operator.equals("/")) {

            double l = toDouble(leftVal);
            double r = toDouble(rightVal);

            switch (operator) {
                case "+": return l + r;
                case "-": return l - r;
                case "*": return l * r;
                case "/":
                    if (r == 0) throw new RuntimeException("Division by zero.");
                    return l / r;
            }
        }

        // Comparison operators — return Boolean
        if (operator.equals(">") || operator.equals("<") || operator.equals("==")) {
            double l = toDouble(leftVal);
            double r = toDouble(rightVal);
            switch (operator) {
                case ">":  return l > r;
                case "<":  return l < r;
                case "==": return l == r;
            }
        }

        throw new RuntimeException("Unknown operator: " + operator);
    }

    private double toDouble(Object val) {
        if (val instanceof Double) return (Double) val;
        throw new RuntimeException("Expected a number but got: " + val);
    }
}