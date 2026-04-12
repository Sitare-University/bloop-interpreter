import java.util.List;

public class Interpreter {

    public void run(String sourceCode) {
        // Step 1 — Tokenize
        Tokenizer tokenizer = new Tokenizer(sourceCode);
        List<Token> tokens = tokenizer.tokenize();

        // Step 2 — Parse
        Parser parser = new Parser(tokens);
        List<Instruction> instructions = parser.parse();

        // Step 3 — Execute
        Environment env = new Environment();
        for (Instruction instruction : instructions) {
            instruction.execute(env);
        }
    }
}
