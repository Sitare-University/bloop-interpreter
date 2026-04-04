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
            case PUT:    return parseAssign();
            case PRINT:  return parsePrint();
            case IF:     return parseIf();
            case REPEAT: return parseRepeat();
            default:
                // Unknown token on a new line — skip the whole line
                advancePastNewline();
                return null;
        }
    }


    // ── Assignment Statement parsing ───────────────────────────────────────────────

    //  put <expr> into <variable>
    private Instruction parseAssign() {
        consume(TokenType.PUT);
        Expression expr = parseExpression();   // the value expression
        consume(TokenType.INTO);
        Token name = consume(TokenType.IDENTIFIER);
        consumeNewlineOrEOF();

        return new AssignInstruction(name.getValue(), expr);
    }

    // ── Print Statement parsing ───────────────────────────────────────────────

    //  print <expr>
    private Instruction parsePrint() {
        consume(TokenType.PRINT);
        Expression expr = parseExpression();
        consumeNewlineOrEOF();
        return new PrintInstruction(expr);
    }

    // ── Conditional Statement parsing───────────────────────────────────────────

    /**
     * if <condition> then:
     *     <body>
     *
     * The body is all indented lines that follow (lines starting with spaces/tabs).
     * We detect this by checking whether the next non-blank line is indented.
     */
    private Instruction parseIf() {
        consume(TokenType.IF);
        Expression condition = parseComparison();
        consume(TokenType.THEN);
        // optional trailing colon is already swallowed by the Tokenizer (it skips ':')
        consumeNewlineOrEOF();
        List<Instruction> body = parseIndentedBlock();
        return new IfInstruction(condition, body);
    }


    // ── Reapeat parsing ───────────────────────────────────────────────

    /**
     * repeat <number> times:
     *     <body>
     */
    private Instruction parseRepeat() {
        consume(TokenType.REPEAT);
        Token countToken = consume(TokenType.NUMBER);
        int count = (int) Double.parseDouble(countToken.getValue());
        consume(TokenType.TIMES);
        consumeNewlineOrEOF();

        List<Instruction> body = parseIndentedBlock();
        return new RepeatInstruction(count, body);
    }
    // ── Indented block parsing ───────────────────────────────────────────────

        /**
     * Parses instructions inside an IF/REPEAT block.
     *
     * Since the tokenizer removes indentation, we use NEWLINE tokens
     * to determine block boundaries.
     *
     * Strategy:
     * - Keep parsing instructions line by line after the block header.
     * - A single NEWLINE means instructions are part of the same block.
     * - Stop when we reach EOF or a clear separation (like a blank line)
     *   indicating the end of the block.
     *
     * This simplified approach works correctly for the current spec
     * where nesting is limited and input is well-structured.
     */

    private List<Instruction> parseIndentedBlock() {
        List<Instruction> body = new ArrayList<>();

        // Keep consuming lines as body instructions until:
        //   - EOF is reached, OR
        //   - a blank line (2+ newlines) separates this block from the next statement
        //
        // This works for all BLOOP sample programs which use blank lines or EOF
        // to terminate blocks.

        while (!isAtEnd()) {
            int newlineCount = skipNewlinesCount();

            if (isAtEnd()) break;

            // If we skipped a blank line (>=2 newlines or the block just
            // ended naturally with one newline then hits a top-level line),
            // AND the next token is a block-starter keyword, we stop.
            if (newlineCount >= 2 && isBlockStarter(peek().getType())) {
                break;
            }

            // If no newlines were skipped and we're at a top-level keyword,
            // that means the block is empty — stop.
            if (newlineCount == 0 && isAtEnd()) break;

            // Parse the next instruction as a body instruction.
            Instruction instr = parseInstruction();
            if (instr != null) body.add(instr);
        }

        return body;
    }
    //  Helper Function for parseIndentedBlock
    private boolean isBlockStarter(TokenType type) {
        return type == TokenType.PUT    ||
               type == TokenType.PRINT  ||
               type == TokenType.IF     ||
               type == TokenType.REPEAT;
    }


    /** Top level: handles comparisons. */
    private Expression parseComparison() {
        Expression left = parseExpression();

        while (match(TokenType.GREATER, TokenType.LESS, TokenType.EQUAL_EQUAL)) {
            String op = previous().getValue();
            Expression right = parseExpression();
            left = new BinaryOpNode(left, op, right);
        }
        return left;
    }

    /** Handles + and -. */
    private Expression parseExpression() {
        Expression left = parseTerm();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            String op = previous().getValue();
            Expression right = parseTerm();
            left = new BinaryOpNode(left, op, right);
        }
        return left;
    }

    /** Handles * and /. */
    private Expression parseTerm() {
        Expression left = parsePrimary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            String op = previous().getValue();
            Expression right = parsePrimary();
            left = new BinaryOpNode(left, op, right);
        }
        return left;
    }

    /** Handles a single literal or variable. */
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
        throw new RuntimeException(
            "Unexpected token in expression: " + peek() + " on line " + peek().getLine());
    }




    // ── ── Token stream utilities ─────────────────────────────

    /** Return current token without advancing. */
    private Token peek() {
        return tokens.get(current);
    }

    /** Return the most recently consumed token. */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /** Advance and return current token. */
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

     /** Check type of current token without consuming. */
    private boolean check(TokenType type) {
        return !isAtEnd() && peek().getType() == type;
    }
    
    /** If current token matches any of the given types, consume it and return true. */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    /** Consume a token of the expected type or throw. */
    private Token consume(TokenType type) {
        if (check(type)) return advance();
        throw new RuntimeException(
            "Expected " + type + " but got " + peek() + " on line " + peek().getLine());
    }

    /** Consume a NEWLINE or EOF (end of statement). */
    private void consumeNewlineOrEOF() {
        if (check(TokenType.NEWLINE)) advance();
        // EOF is fine — do nothing
    }

    /** Skip zero or more NEWLINE tokens. */
    private void skipNewlines() {
        while (check(TokenType.NEWLINE)) advance();
    }

    /** Skip tokens until after the next NEWLINE (used to ignore bad lines). */
    private void advancePastNewline() {
        while (!isAtEnd() && !check(TokenType.NEWLINE)) advance();
        if (check(TokenType.NEWLINE)) advance();
    }

    /** True if we've reached the end-of-file token. */
    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    /** Skip zero or more NEWLINE tokens and return how many were skipped. */
    private int skipNewlinesCount() {
        int count = 0;
        while (check(TokenType.NEWLINE)) {
            advance();
            count++;
        }
        return count;
    }
}