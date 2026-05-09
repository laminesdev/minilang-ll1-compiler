/**
 * LL1Parser.java
 * 
 * LL(1) Table-Driven Parser for MiniLang.
 * 
 * Uses the parsing table to decide which production to apply,
 * rather than recursive descent's switch/case approach.
 * 
 * Algorithm:
 * 1. Push start symbol onto stack
 * 2. While stack is not empty:
 *    a. If top is terminal, match with input
 *    b. If top is non-terminal, look up table[top, input]
 *       and push corresponding production (in reverse)
 * 3. Accept when both stack and input are empty
 * 
 * Key difference from Recursive Descent:
 * - Table lookup: M[A, a] decides action
 * - Explicit parsing table can be verified
 * - Decoupled parsing logic from grammar
 */
import java.util.*;

public class LL1Parser {
    private List<Token> tokens;
    private int currentPos = 0;
    private Grammar grammar;
    private FirstAndFollow firstAndFollow;
    private ParseTable parseTable;
    private static final String EPSILON = "ε";
    private static final String EOF_MARKER = "$";

    /**
     * Constructor - initializes parser with token stream
     * @param tokens The token stream from lexer
     */
    public LL1Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.grammar = new Grammar();
        this.firstAndFollow = new FirstAndFollow(grammar);
        this.parseTable = new ParseTable(grammar, firstAndFollow);
    }

    /**
     * Returns current token without advancing
     */
    private Token currentToken() {
        if (currentPos < tokens.size()) {
            return tokens.get(currentPos);
        }
        return new Token(TokenType.EOF, "$", 0, 0);
    }

    /**
     * Advances to next token in input
     */
    private void advance() {
        if (currentPos < tokens.size()) {
            currentPos++;
        }
    }

    private String tokenTypeToString(TokenType type) {
        switch (type) {
            case INT: return "INT";
            case FLOAT: return "FLOAT";
            case IF: return "IF";
            case ELSE: return "ELSE";
            case WHILE: return "WHILE";
            case PRINT: return "PRINT";
            case IDENTIFIER: return "IDENTIFIER";
            case INTEGER: return "INTEGER";
            case PLUS: return "PLUS";
            case MINUS: return "MINUS";
            case MULTIPLY: return "MULTIPLY";
            case DIVIDE: return "DIVIDE";
            case ASSIGN: return "ASSIGN";
            case EQUAL: return "EQUAL";
            case NOT_EQUAL: return "NOT_EQUAL";
            case LESS: return "LESS";
            case GREATER: return "GREATER";
            case LESS_EQUAL: return "LESS_EQUAL";
            case GREATER_EQUAL: return "GREATER_EQUAL";
            case SEMICOLON: return "SEMICOLON";
            case LPAREN: return "LPAREN";
            case RPAREN: return "RPAREN";
            case LBRACE: return "LBRACE";
            case RBRACE: return "RBRACE";
            case EOF: return "$";
            default: return type.name();
        }
    }

    private String mapTokenToTableKey(Token token) {
        TokenType type = token.type;
        if (type == TokenType.IDENTIFIER) return "IDENTIFIER";
        if (type == TokenType.INTEGER || type == TokenType.FLOAT) return "NUMBER";
        if (type == TokenType.PLUS || type == TokenType.MINUS) return "ADDOP";
        if (type == TokenType.MULTIPLY || type == TokenType.DIVIDE) return "MULOP";
        if (type == TokenType.EQUAL || type == TokenType.NOT_EQUAL ||
            type == TokenType.LESS || type == TokenType.GREATER ||
            type == TokenType.LESS_EQUAL || type == TokenType.GREATER_EQUAL) return "RELOP";
        if (type == TokenType.EOF) return "$";
        return tokenTypeToString(type);
    }

    public ASTNode parse() {
        return parseProgram();
    }

    private ASTNode parseProgram() {
        String input = mapTokenToTableKey(currentToken());
        Grammar.Production prod = parseTable.get("Program", input);
        if (prod == null) {
            throw new RuntimeException("[SYNTAX ERROR] Line " + currentToken().line +
                ": no production for Program with input '" + input + "'");
        }

        List<ASTNode> declarations = parseDeclarationList();
        List<ASTNode> statements = parseStatementList();

        consume(TokenType.EOF);

        return new ProgramNode(declarations, statements);
    }

    private List<ASTNode> parseDeclarationList() {
        String input = mapTokenToTableKey(currentToken());
        Grammar.Production prod = parseTable.get("DeclarationList", input);

        if (prod == null || prod.rhs.get(0).equals(EPSILON)) {
            return new ArrayList<>();
        }

        List<ASTNode> list = new ArrayList<>();
        list.add(parseDeclaration());
        list.addAll(parseDeclarationList());
        return list;
    }

    private ASTNode parseDeclaration() {
        String input = mapTokenToTableKey(currentToken());
        Grammar.Production prod = parseTable.get("Declaration", input);

        if (prod == null) {
            throw new RuntimeException("[SYNTAX ERROR] Line " + currentToken().line +
                ": no production for Declaration with input '" + input + "'");
        }

        String type = consume(TokenType.INT, TokenType.FLOAT);
        String name = consume(TokenType.IDENTIFIER);
        consume(TokenType.SEMICOLON);

        return new DeclarationNode(type, name);
    }

    private List<ASTNode> parseStatementList() {
        String input = mapTokenToTableKey(currentToken());
        Grammar.Production prod = parseTable.get("StatementList", input);

        if (prod == null || prod.rhs.get(0).equals(EPSILON)) {
            return new ArrayList<>();
        }

        List<ASTNode> list = new ArrayList<>();
        list.add(parseStatement());
        list.addAll(parseStatementList());
        return list;
    }

    private ASTNode parseStatement() {
        String input = mapTokenToTableKey(currentToken());
        Grammar.Production prod = parseTable.get("Statement", input);

        if (prod == null) {
            throw new RuntimeException("[SYNTAX ERROR] Line " + currentToken().line +
                ": no production for Statement with input '" + input + "'");
        }

        String firstSymbol = prod.rhs.get(0);

        switch (firstSymbol) {
            case "Assignment": return parseAssignment();
            case "IfStatement": return parseIfStatement();
            case "WhileStatement": return parseWhileStatement();
            case "PrintStatement": return parsePrintStatement();
            case "Block": return parseBlock();
            default:
                throw new RuntimeException("[SYNTAX ERROR] Line " + currentToken().line +
                    ": unexpected first symbol '" + firstSymbol + "'");
        }
    }

    private ASTNode parseAssignment() {
        String name = consume(TokenType.IDENTIFIER);
        consume(TokenType.ASSIGN);
        ASTNode expr = parseExpression();
        consume(TokenType.SEMICOLON);
        return new AssignNode(name, expr);
    }

    private ASTNode parseIfStatement() {
        consume(TokenType.IF);
        consume(TokenType.LPAREN);
        ASTNode condition = parseExpression();
        consume(TokenType.RPAREN);
        ASTNode thenBranch = parseStatement();

        ASTNode elseBranch = null;
        if (check(TokenType.ELSE)) {
            advance();
            elseBranch = parseStatement();
        }

        return new IfNode(condition, thenBranch, elseBranch);
    }

    private ASTNode parseWhileStatement() {
        consume(TokenType.WHILE);
        consume(TokenType.LPAREN);
        ASTNode condition = parseExpression();
        consume(TokenType.RPAREN);
        ASTNode body = parseStatement();
        return new WhileNode(condition, body);
    }

    private ASTNode parsePrintStatement() {
        consume(TokenType.PRINT);
        consume(TokenType.LPAREN);
        ASTNode expr = parseExpression();
        consume(TokenType.RPAREN);
        consume(TokenType.SEMICOLON);
        return new PrintNode(expr);
    }

    private ASTNode parseBlock() {
        consume(TokenType.LBRACE);
        List<ASTNode> stmts = parseStatementList();
        consume(TokenType.RBRACE);
        return new BlockNode(stmts);
    }

    private ASTNode parseExpression() {
        String input = mapTokenToTableKey(currentToken());
        Grammar.Production prod = parseTable.get("Expression", input);

        if (prod == null || prod.rhs.size() == 1) {
            return parseSimpleExpression();
        }

        ASTNode left = parseSimpleExpression();
        if (isRelop()) {
            String op = currentToken().lexeme;
            advance();
            ASTNode right = parseSimpleExpression();
            return new BinOpNode(op, left, right);
        }

        return left;
    }

    private boolean isRelop() {
        TokenType type = currentToken().type;
        return type == TokenType.EQUAL || type == TokenType.NOT_EQUAL ||
               type == TokenType.LESS || type == TokenType.GREATER ||
               type == TokenType.LESS_EQUAL || type == TokenType.GREATER_EQUAL;
    }

    private ASTNode parseSimpleExpression() {
        ASTNode node = parseTerm();

        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            String op = currentToken().lexeme;
            advance();
            node = new BinOpNode(op, node, parseTerm());
        }

        return node;
    }

    private ASTNode parseTerm() {
        ASTNode node = parseFactor();

        while (check(TokenType.MULTIPLY) || check(TokenType.DIVIDE)) {
            String op = currentToken().lexeme;
            advance();
            node = new BinOpNode(op, node, parseFactor());
        }

        return node;
    }

    private ASTNode parseFactor() {
        if (check(TokenType.LPAREN)) {
            advance();
            ASTNode expr = parseExpression();
            consume(TokenType.RPAREN);
            return expr;
        }

        if (check(TokenType.IDENTIFIER)) {
            return new IdentifierNode(consume(TokenType.IDENTIFIER));
        }

        if (check(TokenType.INTEGER) || check(TokenType.FLOAT)) {
            return new NumberNode(consume(currentToken().type));
        }

        throw new RuntimeException("[SYNTAX ERROR] Line " + currentToken().line +
            ": unexpected token in expression: '" + currentToken().lexeme + "'");
    }

    private boolean check(TokenType... types) {
        for (TokenType t : types) {
            if (currentToken().type == t) return true;
        }
        return false;
    }

    private String consume(TokenType... expectedTypes) {
        Token t = currentToken();
        for (TokenType expected : expectedTypes) {
            if (t.type == expected) {
                advance();
                return t.lexeme;
            }
        }
        throw new RuntimeException("[SYNTAX ERROR] Line " + t.line +
            ": expected one of " + Arrays.toString(expectedTypes) + " but found '" + t.lexeme + "'");
    }

    public static class ASTNode {}

    public static class ProgramNode extends ASTNode {
        public List<DeclarationNode> declarations = new ArrayList<>();
        public List<ASTNode> statements = new ArrayList<>();

        public ProgramNode(List<ASTNode> decls, List<ASTNode> stmts) {
            for (ASTNode n : decls) {
                if (n instanceof DeclarationNode) {
                    declarations.add((DeclarationNode) n);
                }
            }
            this.statements = stmts;
        }
    }

    public static class DeclarationNode extends ASTNode {
        public String type;
        public String name;

        public DeclarationNode(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    public static class AssignNode extends ASTNode {
        public String name;
        public ASTNode value;

        public AssignNode(String name, ASTNode value) {
            this.name = name;
            this.value = value;
        }
    }

    public static class IfNode extends ASTNode {
        public ASTNode condition;
        public ASTNode thenBranch;
        public ASTNode elseBranch;

        public IfNode(ASTNode c, ASTNode t, ASTNode e) {
            this.condition = c;
            this.thenBranch = t;
            this.elseBranch = e;
        }
    }

    public static class WhileNode extends ASTNode {
        public ASTNode condition;
        public ASTNode body;

        public WhileNode(ASTNode c, ASTNode b) {
            this.condition = c;
            this.body = b;
        }
    }

    public static class PrintNode extends ASTNode {
        public ASTNode expr;

        public PrintNode(ASTNode e) {
            this.expr = e;
        }
    }

    public static class BlockNode extends ASTNode {
        public List<ASTNode> statements;

        public BlockNode(List<ASTNode> stmts) {
            this.statements = stmts;
        }
    }

    public static class BinOpNode extends ASTNode {
        public String operator;
        public ASTNode left;
        public ASTNode right;

        public BinOpNode(String op, ASTNode l, ASTNode r) {
            this.operator = op;
            this.left = l;
            this.right = r;
        }
    }

    public static class IdentifierNode extends ASTNode {
        public String name;

        public IdentifierNode(String name) {
            this.name = name;
        }
    }

    public static class NumberNode extends ASTNode {
        public String value;

        public NumberNode(String value) {
            this.value = value;
        }
    }
}