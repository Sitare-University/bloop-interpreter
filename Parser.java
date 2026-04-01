import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
    }

    //  Entry point
    public List<Instruction> parse() {
        List<Instruction> instructions = new ArrayList<>();
        skipNewlines();

        while (!isAtEnd()) {
            Instruction instr = parseInstruction();
            if (instr != null) instructions.add(instr);
            skipNewlines();
        }
        return instructions;
    }

    //  Decide which instruction to parse
    private Instruction parseInstruction() {
        Token token = peek();

        switch (token.getType()) {
            case PUT:   return parseAssign();
            case PRINT: return parsePrint();
            default:
                advancePastNewline();
                return null;
        }
    }

    //  put <expr> into <var>
    private Instruction parseAssign() {
        consume(TokenType.PUT);
        Expression expr = parsePrimary(); // simple for now
        consume(TokenType.INTO);
        Token name = consume(TokenType.IDENTIFIER);
        consumeNewlineOrEOF();

        return new AssignInstruction(name.getValue(), expr);
    }

    //  print <expr>
    private Instruction parsePrint() {
        consume(TokenType.PRINT);
        Expression expr = parsePrimary(); // simple for now
        consumeNewlineOrEOF();

        return new PrintInstruction(expr);
    }

    //  Simple expression (only single value for now)
    private Expression parsePrimary() {
        if (match(TokenType.NUMBER)) {
            return new NumberNode(Double.parseDouble(previous().getValue()));
        }
        if (match(TokenType.STRING)) {
            return new StringNode(previous().getValue());
        }
        if (match(TokenType.IDENTIFIER)) {
            return new VariableNode(previous().getValue());
        }

        throw new RuntimeException("Unexpected token: " + peek());
    }

    // ── Utilities ─────────────────────────────

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean check(TokenType type) {
        return !isAtEnd() && peek().getType() == type;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type) {
        if (check(type)) return advance();
        throw new RuntimeException("Expected " + type + " but got " + peek());
    }

    private void consumeNewlineOrEOF() {
        if (check(TokenType.NEWLINE)) advance();
    }

    private void skipNewlines() {
        while (check(TokenType.NEWLINE)) advance();
    }

    private void advancePastNewline() {
        while (!isAtEnd() && !check(TokenType.NEWLINE)) advance();
        if (check(TokenType.NEWLINE)) advance();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }
}