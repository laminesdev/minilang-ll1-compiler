/**
 * Grammar.java
 * 
 * Defines the formal grammar for MiniLang in LL(1) format.
 * Contains terminals, non-terminals, and production rules.
 * 
 * Grammar Format:
 *   Program          → DeclarationList StatementList
 *   DeclarationList  → Declaration DeclarationList | ε
 *   Declaration      → Type IDENTIFIER ;
 *   StatementList    → Statement StatementList | ε
 *   Statement        → Assignment | IfStatement | WhileStatement | PrintStatement | Block
 *   Expression       → SimpleExpression [RELOP SimpleExpression]
 *   SimpleExpression → Term {ADDOP Term}
 *   Term             → Factor {MULOP Factor}
 *   Factor           → ( Expression ) | IDENTIFIER | NUMBER
 */
import java.util.*;

public class Grammar {
    /** Start symbol of the grammar */
    public String startSymbol;
    /** Set of terminal symbols */
    public Set<String> terminals = new HashSet<>();
    /** Set of non-terminal symbols */
    public Set<String> nonTerminals = new HashSet<>();
    /** List of production rules */
    public List<Production> productions = new ArrayList<>();

    /**
     * Represents a single production rule: LHS → RHS
     */
    public static class Production {
        public String lhs;         // Left-hand side (non-terminal)
        public List<String> rhs;   // Right-hand side (sequence of symbols)

        public Production(String lhs, List<String> rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public String toString() {
            return lhs + " -> " + String.join(" ", rhs);
        }
    }

    public Grammar() {
        defineGrammar();
    }

    private void defineGrammar() {
        nonTerminals.add("Program");
        nonTerminals.add("DeclarationList");
        nonTerminals.add("Declaration");
        nonTerminals.add("StatementList");
        nonTerminals.add("Statement");
        nonTerminals.add("Assignment");
        nonTerminals.add("IfStatement");
        nonTerminals.add("WhileStatement");
        nonTerminals.add("PrintStatement");
        nonTerminals.add("Block");
        nonTerminals.add("Expression");
        nonTerminals.add("SimpleExpression");
        nonTerminals.add("Term");
        nonTerminals.add("Factor");
        nonTerminals.add("Type");

        terminals.add("INT");
        terminals.add("FLOAT");
        terminals.add("IF");
        terminals.add("ELSE");
        terminals.add("WHILE");
        terminals.add("PRINT");
        terminals.add("IDENTIFIER");
        terminals.add("INTEGER");
        terminals.add("FLOAT");
        terminals.add("PLUS");
        terminals.add("MINUS");
        terminals.add("MULTIPLY");
        terminals.add("DIVIDE");
        terminals.add("ASSIGN");
        terminals.add("EQUAL");
        terminals.add("NOT_EQUAL");
        terminals.add("LESS");
        terminals.add("GREATER");
        terminals.add("LESS_EQUAL");
        terminals.add("GREATER_EQUAL");
        terminals.add("SEMICOLON");
        terminals.add("LPAREN");
        terminals.add("RPAREN");
        terminals.add("LBRACE");
        terminals.add("RBRACE");
        terminals.add("EOF");
        terminals.add("$");

        startSymbol = "Program";

        productions.add(new Production("Program", Arrays.asList("DeclarationList", "StatementList")));

        productions.add(new Production("DeclarationList", Arrays.asList("Declaration", "DeclarationList")));
        productions.add(new Production("DeclarationList", Arrays.asList("ε")));

        productions.add(new Production("Declaration", Arrays.asList("Type", "IDENTIFIER", "SEMICOLON")));

        productions.add(new Production("Type", Arrays.asList("INT")));
        productions.add(new Production("Type", Arrays.asList("FLOAT")));

        productions.add(new Production("StatementList", Arrays.asList("Statement", "StatementList")));
        productions.add(new Production("StatementList", Arrays.asList("ε")));

        productions.add(new Production("Statement", Arrays.asList("Assignment")));
        productions.add(new Production("Statement", Arrays.asList("IfStatement")));
        productions.add(new Production("Statement", Arrays.asList("WhileStatement")));
        productions.add(new Production("Statement", Arrays.asList("PrintStatement")));
        productions.add(new Production("Statement", Arrays.asList("Block")));

        productions.add(new Production("Assignment", Arrays.asList("IDENTIFIER", "ASSIGN", "Expression", "SEMICOLON")));

        productions.add(new Production("IfStatement", Arrays.asList("IF", "LPAREN", "Expression", "RPAREN", "Statement", "ELSE", "Statement")));
        productions.add(new Production("IfStatement", Arrays.asList("IF", "LPAREN", "Expression", "RPAREN", "Statement")));

        productions.add(new Production("WhileStatement", Arrays.asList("WHILE", "LPAREN", "Expression", "RPAREN", "Statement")));

        productions.add(new Production("PrintStatement", Arrays.asList("PRINT", "LPAREN", "Expression", "RPAREN", "SEMICOLON")));

        productions.add(new Production("Block", Arrays.asList("LBRACE", "StatementList", "RBRACE")));

        productions.add(new Production("Expression", Arrays.asList("SimpleExpression")));
        productions.add(new Production("Expression", Arrays.asList("SimpleExpression", "RELOP", "SimpleExpression")));

        productions.add(new Production("SimpleExpression", Arrays.asList("Term")));
        productions.add(new Production("SimpleExpression", Arrays.asList("Term", "ADDOP", "SimpleExpression")));

        productions.add(new Production("Term", Arrays.asList("Factor")));
        productions.add(new Production("Term", Arrays.asList("Factor", "MULOP", "Term")));

        productions.add(new Production("Factor", Arrays.asList("LPAREN", "Expression", "RPAREN")));
        productions.add(new Production("Factor", Arrays.asList("IDENTIFIER")));
        productions.add(new Production("Factor", Arrays.asList("NUMBER")));

        terminals.add("RELOP");
        terminals.add("ADDOP");
        terminals.add("MULOP");
        terminals.add("NUMBER");
    }

    public List<Production> getProductionsFor(String nonTerminal) {
        List<Production> result = new ArrayList<>();
        for (Production p : productions) {
            if (p.lhs.equals(nonTerminal)) {
                result.add(p);
            }
        }
        return result;
    }

    public boolean isTerminal(String symbol) {
        return terminals.contains(symbol);
    }

    public boolean isNonTerminal(String symbol) {
        return nonTerminals.contains(symbol);
    }

    public void print() {
        System.out.println("=== GRAMMAR ===");
        System.out.println("Terminals: " + terminals);
        System.out.println("Non-Terminals: " + nonTerminals);
        System.out.println("Start Symbol: " + startSymbol);
        System.out.println("\nProductions:");
        for (Production p : productions) {
            System.out.println("  " + p);
        }
    }
}