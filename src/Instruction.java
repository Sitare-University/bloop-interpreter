// ─── Expression Interface ───────────────────────────────────────────────────

public interface Expression {
    /**
     * Evaluate this expression using the current variable store.
     * Returns either a Double (for numbers) or a String (for text).
     */
    Object evaluate(Environment env);
}


// ─── AssignInstruction ───────────────────────────────────────────────────────
// Handles:  put <expr> into <variable>
// Example:  put x + y * 2 into result

class AssignInstruction implements Instruction {
    private final String variableName;
    private final Expression expression;

    public AssignInstruction(String variableName, Expression expression) {
        this.variableName = variableName;
        this.expression   = expression;
    }

    @Override
    public void execute(Environment env) {
        // Evaluate the expression, then store the result under the variable name.
        Object value = expression.evaluate(env);
        env.set(variableName, value);
    }
}



// ─── PrintInstruction ────────────────────────────────────────────────────────
// Handles:  print <expr>
// Example:  print result   or   print "Hello from BLOOP"

class PrintInstruction implements Instruction {
    private final Expression expression;

    public PrintInstruction(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {
        Object value = expression.evaluate(env);

        // Print numbers without a trailing ".0" when the value is a whole number.
        if (value instanceof Double) {
            double d = (Double) value;
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                System.out.println((long) d);
            } else {
                System.out.println(d);
            }
        } else {
            System.out.println(value);
        }
    }
}





// ─── IfInstruction ───────────────────────────────────────────────────────────
// Handles:  if <condition> then:
//               <body instructions>
// Example:  if score > 50 then:
//               print "Pass"

class IfInstruction implements Instruction {
    private final Expression condition;
    private final List<Instruction> body;

    public IfInstruction(Expression condition, List<Instruction> body) {
        this.condition = condition;
        this.body      = body;
    }

    @Override
    public void execute(Environment env) {
        Object result = condition.evaluate(env);

        // The condition must evaluate to a Boolean (produced by BinaryOpNode comparisons).
        if (result instanceof Boolean && (Boolean) result) {
            for (Instruction instruction : body) {
                instruction.execute(env);
            }
        }
    }
}



// ─── RepeatInstruction ───────────────────────────────────────────────────────
// Handles:  repeat <count> times:
//               <body instructions>
// Example:  repeat 4 times:
//               print i

class RepeatInstruction implements Instruction {
    private final int count;
    private final List<Instruction> body;

    public RepeatInstruction(int count, List<Instruction> body) {
        this.count = count;
        this.body  = body;
    }

    @Override
    public void execute(Environment env) {
        for (int i = 0; i < count; i++) {
            for (Instruction instruction : body) {
                instruction.execute(env);
            }
        }
    }
}
