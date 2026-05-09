/**
 * ParseTable.java
 * 
 * Builds the LL(1) parsing table.
 * 
 * The parsing table M[A, a] contains production rules to use
 * when:
 *   - A is the current non-terminal on top of the stack
 *   - a is the current input token
 * 
 * Table Construction Rules:
 * 1. For each production A → α:
 *    - For each terminal a ∈ First(α): M[A, a] = A → α
 *    - If ε ∈ First(α), for each b ∈ Follow(A): M[A, b] = A → α
 * 
 * If M[A, a] is empty, it's a parsing error.
 */
import java.util.*;

public class ParseTable {
    /** Parsing table: table[A][a] = production for non-terminal A and terminal a */
    public Map<String, Map<String, Grammar.Production>> table = new HashMap<>();
    private Grammar grammar;
    private FirstAndFollow firstAndFollow;
    private static final String EPSILON = "ε";
    private static final String EOF = "$";

    /**
     * Constructor - builds the LL(1) parsing table
     * @param grammar The grammar
     * @param firstAndFollow Precomputed First and Follow sets
     */
    public ParseTable(Grammar grammar, FirstAndFollow firstAndFollow) {
        this.grammar = grammar;
        this.firstAndFollow = firstAndFollow;
        buildTable();
    }

    /**
     * Builds the parsing table using First and Follow sets.
     * For each production, fills appropriate table entries.
     */
    private void buildTable() {
        for (String nonTerminal : grammar.nonTerminals) {
            table.put(nonTerminal, new HashMap<>());
        }

        for (Grammar.Production prod : grammar.productions) {
            String lhs = prod.lhs;
            List<String> rhs = prod.rhs;

            if (rhs.get(0).equals(EPSILON)) {
                Set<String> followSet = firstAndFollow.getFollow(lhs);
                for (String terminal : followSet) {
                    if (!terminal.equals(EPSILON)) {
                        String terminalKey = mapTerminal(terminal);
                        table.get(lhs).put(terminalKey, prod);
                    }
                }
            } else {
                Set<String> firstSet = firstAndFollow.getFirstOfSequence(rhs);

                for (String symbol : firstSet) {
                    if (!symbol.equals(EPSILON)) {
                        String terminalKey = mapTerminal(symbol);
                        table.get(lhs).put(terminalKey, prod);
                    }
                }

                if (firstSet.contains(EPSILON)) {
                    Set<String> followSet = firstAndFollow.getFollow(lhs);
                    for (String terminal : followSet) {
                        if (!terminal.equals(EPSILON)) {
                            String terminalKey = mapTerminal(terminal);
                            if (!table.get(lhs).containsKey(terminalKey)) {
                                table.get(lhs).put(terminalKey, prod);
                            }
                        }
                    }
                }
            }
        }
    }

    private String mapTerminal(String terminal) {
        switch (terminal) {
            case "int": return "INT";
            case "float": return "FLOAT";
            case "if": return "IF";
            case "else": return "ELSE";
            case "while": return "WHILE";
            case "print": return "PRINT";
            case "$": return "$";
            default:
                if (terminal.length() == 1 && Character.isLowerCase(terminal.charAt(0))) {
                    return terminal.toUpperCase();
                }
                return terminal;
        }
    }

    public Grammar.Production get(String nonTerminal, String terminal) {
        Map<String, Grammar.Production> row = table.get(nonTerminal);
        if (row == null) return null;
        return row.get(terminal);
    }

    public void printTable() {
        System.out.println("=== LL(1) PARSING TABLE ===");
        List<String> terminals = getTerminals();

        System.out.print(String.format("%-20s", "Non-Terminal"));
        for (String term : terminals) {
            System.out.print(String.format("%-25s", term));
        }
        System.out.println();

        for (String nonTerminal : grammar.nonTerminals) {
            System.out.print(String.format("%-20s", nonTerminal));
            for (String term : terminals) {
                Grammar.Production prod = get(nonTerminal, term);
                if (prod != null) {
                    System.out.print(String.format("%-25s", prodToString(prod)));
                } else {
                    System.out.print(String.format("%-25s", ""));
                }
            }
            System.out.println();
        }
    }

    private List<String> getTerminals() {
        Set<String> terminals = new LinkedHashSet<>();
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
        terminals.add("$");

        return new ArrayList<>(terminals);
    }

    private String prodToString(Grammar.Production prod) {
        if (prod.rhs.get(0).equals(EPSILON)) {
            return prod.lhs + " -> ε";
        }
        return prod.lhs + " -> " + String.join(" ", prod.rhs);
    }
}