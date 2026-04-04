public class Test {
    public static void main(String[] args) {
         System.out.println("=== Test 1: Keywords & Identifiers ===");
        Tokenizer t1 = new Tokenizer("put 10 into x");
        for (Token tok : t1.tokenize())
            System.out.println(tok);

        // Test 2 — number recognition
        System.out.println("\n=== Test 2: Numbers ===");
        Tokenizer t2 = new Tokenizer("put 3.14 into y");
        for (Token tok : t2.tokenize())
            System.out.println(tok);

        // Test 3 — string recognition
        System.out.println("\n=== Test 3: Strings ===");
        Tokenizer t3 = new Tokenizer("print \"Hello from BLOOP\"");
        for (Token tok : t3.tokenize())
            System.out.println(tok);
    }
    
}
