/**
 * FirstAndFollow.java
 * 
 * Computes First and Follow sets for all grammar symbols.
 * These sets are essential for building the LL(1) parsing table.
 * 
 * FIRST(α): Set of terminals that can appear as the first symbol
 *           in a derivation starting from α.
 *           Also includes ε if α can derive to empty.
 * 
 * FOLLOW(A): Set of terminals that can appear immediately after
 *             non-terminal A in a valid derivation.
 *             Used for handling ε-productions and end-of-input.
 */
import java.util.*;

public class FirstAndFollow {
    /** First sets for each symbol (terminal or non-terminal) */
    public Map<String, Set<String>> first = new HashMap<>();
    /** Follow sets for each non-terminal */
    public Map<String, Set<String>> follow = new HashMap<>();
    private static final String EPSILON = "ε";  // Epsilon represents empty string
    private static final String EOF = "$";      // End-of-input marker
    private Grammar grammar;

    /**
     * Constructor - computes First and Follow sets for the grammar
     * @param grammar The grammar to analyze
     */
    public FirstAndFollow(Grammar grammar) {
        this.grammar = grammar;
        computeFirst();
        computeFollow();
    }

    /**
     * Computes First sets using iterative fixed-point algorithm:
     * 1. For each terminal T, First(T) = {T}
     * 2. For each production A → α:
     *    - Add First(α) to First(A), excluding ε
     *    - If ε ∈ First(α), continue to next symbol
     */
    public void computeFirst() {
        for (String terminal : grammar.terminals) {
            if (!terminal.equals("$") && !terminal.equals("EOF")) {
                first.put(terminal, new HashSet<>(Collections.singleton(terminal)));
            }
        }

        first.put(EPSILON, new HashSet<>(Collections.singleton(EPSILON)));

        for (String nonTerminal : grammar.nonTerminals) {
            first.put(nonTerminal, new HashSet<>());
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Grammar.Production prod : grammar.productions) {
                String lhs = prod.lhs;
                List<String> rhs = prod.rhs;

                Set<String> oldFirst = new HashSet<>(first.get(lhs));

                if (rhs.get(0).equals(EPSILON)) {
                    oldFirst.add(EPSILON);
                } else {
                    for (String symbol : rhs) {
                        Set<String> symbolFirst = first.get(symbol);
                        if (symbolFirst != null) {
                            for (String f : symbolFirst) {
                                if (!f.equals(EPSILON)) {
                                    oldFirst.add(f);
                                }
                            }
                        }
                        if (symbolFirst == null || !symbolFirst.contains(EPSILON)) {
                            break;
                        }
                    }
                }

                Set<String> currentFirst = new HashSet<>(first.get(lhs));
                first.get(lhs).addAll(oldFirst);

                if (!first.get(lhs).equals(currentFirst)) {
                    changed = true;
                }
            }
        }
    }

    public void computeFollow() {
        for (String nonTerminal : grammar.nonTerminals) {
            follow.put(nonTerminal, new HashSet<>());
        }

        follow.get(grammar.startSymbol).add(EOF);

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Grammar.Production prod : grammar.productions) {
                String lhs = prod.lhs;
                List<String> rhs = prod.rhs;

                for (int i = 0; i < rhs.size(); i++) {
                    String symbol = rhs.get(i);

                    if (!grammar.isNonTerminal(symbol)) {
                        continue;
                    }

                    Set<String> oldFollow = new HashSet<>(follow.get(symbol));

                    boolean hasEpsilon = true;
                    for (int j = i + 1; j < rhs.size(); j++) {
                        String nextSymbol = rhs.get(j);
                        Set<String> nextFirst = first.get(nextSymbol);
                        if (nextFirst != null) {
                            for (String f : nextFirst) {
                                if (!f.equals(EPSILON)) {
                                    oldFollow.add(f);
                                }
                            }
                        }
                        if (nextFirst == null || !nextFirst.contains(EPSILON)) {
                            hasEpsilon = false;
                            break;
                        }
                    }

                    if (hasEpsilon || i == rhs.size() - 1) {
                        Set<String> lhsFollow = follow.get(lhs);
                        if (lhsFollow != null) {
                            oldFollow.addAll(lhsFollow);
                        }
                    }

                    follow.get(symbol).addAll(oldFollow);

                    if (!follow.get(symbol).equals(oldFollow)) {
                        changed = true;
                    }
                }
            }
        }
    }

    public Set<String> getFirst(String symbol) {
        if (first.containsKey(symbol)) {
            return first.get(symbol);
        }
        return Collections.emptySet();
    }

    public Set<String> getFollow(String symbol) {
        if (follow.containsKey(symbol)) {
            return follow.get(symbol);
        }
        return Collections.emptySet();
    }

    public boolean hasEpsilon(String symbol) {
        Set<String> firstSet = getFirst(symbol);
        return firstSet.contains(EPSILON);
    }

    public void printFirst() {
        System.out.println("=== FIRST SETS ===");
        for (String symbol : grammar.nonTerminals) {
            System.out.println("First(" + symbol + ") = " + first.get(symbol));
        }
    }

    public void printFollow() {
        System.out.println("=== FOLLOW SETS ===");
        for (String symbol : grammar.nonTerminals) {
            System.out.println("Follow(" + symbol + ") = " + follow.get(symbol));
        }
    }

    public Set<String> getFirstOfSequence(List<String> sequence) {
        Set<String> result = new HashSet<>();

        for (String symbol : sequence) {
            Set<String> symbolFirst = getFirst(symbol);
            if (symbolFirst != null) {
                for (String f : symbolFirst) {
                    if (!f.equals(EPSILON)) {
                        result.add(f);
                    }
                }
            }
            if (symbolFirst == null || !symbolFirst.contains(EPSILON)) {
                return result;
            }
        }

        result.add(EPSILON);
        return result;
    }
}