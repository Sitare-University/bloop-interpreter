import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    private final String source;
    private int pos;
    private int line;

    public Tokenizer(String source) {
        this.source = source;
        this.pos = 0;
        this.line = 1;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < source.length()) {
            char c = source.charAt(pos);

            // ── Whitespace ──
            if (c == ' ' || c == '\t') {
                pos++;
                continue;
            }

            // ── Newline ──
            if (c == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\\n", line));
                line++;
                pos++;
                continue;
            }

            // ── Carriage return (Windows \r\n) — skip 
            if (c == '\r') {
                pos++;
                continue;
            }

            // ── Colon — skip  (from "then:") ──
            if (c == ':') {
                pos++;
                continue;
            }

            // ── String literal ───
            if (c == '"') {
                tokens.add(readString());
                continue;
            }

            // ── Number ──────────
            if (Character.isDigit(c)) {
                tokens.add(readNumber());
                continue;
            }

            // ── Identifier or keyword ─────
            if (Character.isLetter(c) || c == '_') {
                tokens.add(readWord());
                continue;
            }

            // ── Arithmetic operators ───────────────────────────────────────
            if (c == '+') { tokens.add(new Token(TokenType.PLUS,  "+", line)); pos++; continue; }
            if (c == '-') { tokens.add(new Token(TokenType.MINUS, "-", line)); pos++; continue; }
            if (c == '*') { tokens.add(new Token(TokenType.STAR,  "*", line)); pos++; continue; }
            if (c == '/') { tokens.add(new Token(TokenType.SLASH, "/", line)); pos++; continue; }

            // ── Comparison operators ───────────────────────────────────────
            if (c == '>') { tokens.add(new Token(TokenType.GREATER, ">", line)); pos++; continue; }
            if (c == '<') { tokens.add(new Token(TokenType.LESS,    "<", line)); pos++; continue; }

            // ── Equality: == ───────────────────────────────────────────────
            if (c == '=') {
                if (pos + 1 < source.length() && source.charAt(pos + 1) == '=') {
                    tokens.add(new Token(TokenType.EQUAL_EQUAL, "==", line));
                    pos += 2;
                } else {
                    pos++; // bare = not used in BLOOP, skip
                }
                continue;
            }

            // ── Anything else — skip  ──────
            pos++;
        }

        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    // ── Helper: reads a keyword or identifier ─────
    private Token readWord() {
        int startLine = line;
        StringBuilder sb = new StringBuilder();

        while (pos < source.length() &&
              (Character.isLetterOrDigit(source.charAt(pos))
               || source.charAt(pos) == '_')) {
            sb.append(source.charAt(pos));
            pos++;
        }

        String word = sb.toString();

        switch (word) {
            case "put":    return new Token(TokenType.PUT,    word, startLine);
            case "into":   return new Token(TokenType.INTO,   word, startLine);
            case "print":  return new Token(TokenType.PRINT,  word, startLine);
            case "if":     return new Token(TokenType.IF,     word, startLine);
            case "then":   return new Token(TokenType.THEN,   word, startLine);
            case "repeat": return new Token(TokenType.REPEAT, word, startLine);
            case "times":  return new Token(TokenType.TIMES,  word, startLine);
            default:       return new Token(TokenType.IDENTIFIER, word, startLine);
        }
    }

    // ── Helper: reads an integer or decimal number ────
    private Token readNumber() {
        int startLine = line;
        StringBuilder sb = new StringBuilder();

        while (pos < source.length() &&
              (Character.isDigit(source.charAt(pos))
               || source.charAt(pos) == '.')) {
            sb.append(source.charAt(pos));
            pos++;
        }

        return new Token(TokenType.NUMBER, sb.toString(), startLine);
    }

    // ── Helper: reads a double-quoted string literal ───
    private Token readString() {
        int startLine = line;
        pos++; // skip opening "

        StringBuilder sb = new StringBuilder();

        while (pos < source.length() && source.charAt(pos) != '"') {
            sb.append(source.charAt(pos));
            pos++;
        }

        pos++; // skip closing "
        return new Token(TokenType.STRING, sb.toString(), startLine);
    }
}