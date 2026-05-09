import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║  MINILANG COMPILER - PRESENTATION     ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println();
        
        if (args.length == 0) {
            showMenu();
            return;
        }
        
        String file = args[0];
        
        // Lexical Analysis
        System.out.println("═══════════════════════════════════════");
        System.out.println("PHASE 1: LEXICAL ANALYSIS");
        System.out.println("═══════════════════════════════════════");
        Lexer lexer = new Lexer(new File(file));
        List<Token> tokens = new ArrayList<>();
        Token token;
        do {
            token = lexer.nextToken();
            tokens.add(token);
            System.out.println("  " + token.type + " → " + token.lexeme);
        } while (token.type != TokenType.EOF);
        
        if (lexer.hasError()) {
            System.out.println("\n❌ LEXICAL ERROR");
            return;
        }
        System.out.println("✓ Lexical Analysis: OK\n");
        
        // Syntax Analysis
        System.out.println("═══════════════════════════════════════");
        System.out.println("PHASE 2: SYNTAX ANALYSIS (LL(1) Table-Driven)");
        System.out.println("═══════════════════════════════════════");
        SemanticAnalyzer semantic = new SemanticAnalyzer();
        LL1Parser ll1Parser = new LL1Parser(tokens);
        LL1Parser.ASTNode ast = null;
        try {
            ast = ll1Parser.parse();
        } catch (Exception e) {
            System.out.println("\n❌ SYNTAX ERROR: " + e.getMessage());
            return;
        }
        System.out.println("✓ Syntax Analysis: OK\n");
        
        // Semantic Analysis
        System.out.println("═══════════════════════════════════════");
        System.out.println("PHASE 3: SEMANTIC ANALYSIS");
        System.out.println("═══════════════════════════════════════");
        
        // Process AST for semantic checks
        if (ast instanceof LL1Parser.ProgramNode) {
            LL1Parser.ProgramNode prog = (LL1Parser.ProgramNode) ast;
            for (LL1Parser.DeclarationNode decl : prog.declarations) {
                semantic.checkDeclaration(decl.name, decl.type);
            }
            // Check for undeclared variables in statements
            checkStatementsSemantic(prog.statements, semantic);
        }
        
        if (semantic.hasError()) {
            System.out.println("❌ SEMANTIC ERROR");
            return;
        }
        System.out.println("Symbol Table: " + semantic.getSymbolTable());
        System.out.println("✓ Semantic Analysis: OK\n");
        
        // Code Generation
        System.out.println("═══════════════════════════════════════");
        System.out.println("PHASE 4: CODE GENERATION");
        System.out.println("═══════════════════════════════════════");
        CodeGenerator cg = new CodeGenerator(tokens);
        String code = cg.generate();
        
        String cFile = file.replace(".mlang", ".c");
        new PrintWriter(cFile).close();
        PrintWriter pw = new PrintWriter(new FileWriter(cFile));
        pw.print(code);
        pw.close();
        
        System.out.println(code);
        System.out.println("✓ Generated: " + cFile);
    }
    
    private static void showMenu() {
        System.out.println("Usage: java Main <file.mlang>");
        System.out.println();
        System.out.println("Example programs:");
        System.out.println("  examples/test1.mlang  - Variables & While");
        System.out.println("  examples/test2.mlang  - Arithmetic");
        System.out.println("  examples/test3.mlang  - If-Else");
        System.out.println("  examples/error1.mlang - Error (undeclared)");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  java Compiler test.mlang      - Execute");
        System.out.println("  java Compiler test.mlang -c   - Generate C code");
    }
    
    private static void checkStatementsSemantic(List<LL1Parser.ASTNode> stmts, SemanticAnalyzer semantic) {
        for (LL1Parser.ASTNode node : stmts) {
            if (node instanceof LL1Parser.AssignNode) {
                LL1Parser.AssignNode a = (LL1Parser.AssignNode) node;
                semantic.checkVariable(a.name, 0);
            } else if (node instanceof LL1Parser.IfNode) {
                LL1Parser.IfNode i = (LL1Parser.IfNode) node;
                checkStatementsSemantic(List.of(i.thenBranch), semantic);
                if (i.elseBranch != null) {
                    checkStatementsSemantic(List.of(i.elseBranch), semantic);
                }
            } else if (node instanceof LL1Parser.WhileNode) {
                LL1Parser.WhileNode w = (LL1Parser.WhileNode) node;
                checkStatementsSemantic(List.of(w.body), semantic);
            } else if (node instanceof LL1Parser.BlockNode) {
                LL1Parser.BlockNode b = (LL1Parser.BlockNode) node;
                checkStatementsSemantic(b.statements, semantic);
            }
        }
    }
}