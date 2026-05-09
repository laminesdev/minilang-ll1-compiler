import java.util.*;

public class Parser {
    private List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token current() {
        return pos < tokens.size() ? tokens.get(pos) : tokens.get(tokens.size() - 1);
    }

    private Token peek(int offset) {
        int idx = pos + offset;
        return (idx < tokens.size()) ? tokens.get(idx) : tokens.get(tokens.size() - 1);
    }

    private boolean check(TokenType type) {
        return current().type == type;
    }

    private Token consume(TokenType expected) {
        Token t = current();
        if (t.type != expected) {
            throw new RuntimeException(
                "[SYNTAX ERROR] Line " + t.line +
                ": expected '" + expected + "' but found '" + t.lexeme + "' (" + t.type + ")"
            );
        }
        pos++;
        return t;
    }

    private boolean match(TokenType type) {
        if (check(type)) { pos++; return true; }
        return false;
    }

    // program → declaration_list statement_list
    public ASTNode parseProgram() {
        List<ASTNode> decls = parseDeclarationList();
        List<ASTNode> stmts = parseStatementList();
        return new ProgramNode(decls, stmts);
    }

    // declaration_list → declaration declaration_list | ε
    private List<ASTNode> parseDeclarationList() {
        List<ASTNode> list = new ArrayList<>();
        while (check(TokenType.INT) || check(TokenType.FLOAT)) {
            list.add(parseDeclaration());
        }
        return list;
    }

    // declaration → type identifier ;
    private ASTNode parseDeclaration() {
        String type = current().lexeme;
        pos++;
        String name = consume(TokenType.IDENTIFIER).lexeme;
        consume(TokenType.SEMICOLON);
        return new DeclarationNode(type, name);
    }

    // statement_list → statement statement_list | ε
    private List<ASTNode> parseStatementList() {
        List<ASTNode> list = new ArrayList<>();
        while (!check(TokenType.EOF) && !check(TokenType.RBRACE)) {
            list.add(parseStatement());
        }
        return list;
    }

    // statement → assignment | if | while | print | block
    private ASTNode parseStatement() {
        switch (current().type) {
            case IF:     return parseIf();
            case WHILE:  return parseWhile();
            case PRINT: return parsePrint();
            case LBRACE: return parseBlock();
            case IDENTIFIER: return parseAssignment();
            default:
                throw new RuntimeException(
                    "[SYNTAX ERROR] Line " + current().line +
                    ": unexpected token '" + current().lexeme + "'"
                );
        }
    }

    // assignment → identifier = expression ;
    private ASTNode parseAssignment() {
        String name = consume(TokenType.IDENTIFIER).lexeme;
        consume(TokenType.ASSIGN);
        ASTNode expr = parseExpression();
        consume(TokenType.SEMICOLON);
        return new AssignNode(name, expr);
    }

    // if → if ( expression ) statement [ else statement ]
    private ASTNode parseIf() {
        consume(TokenType.IF);
        consume(TokenType.LPAREN);
        ASTNode condition = parseExpression();
        consume(TokenType.RPAREN);
        ASTNode thenBranch = parseStatement();

        ASTNode elseBranch = null;
        if (check(TokenType.ELSE)) {
            pos++;
            elseBranch = parseStatement();
        }
        return new IfNode(condition, thenBranch, elseBranch);
    }

    // while → while ( expression ) statement
    private ASTNode parseWhile() {
        consume(TokenType.WHILE);
        consume(TokenType.LPAREN);
        ASTNode condition = parseExpression();
        consume(TokenType.RPAREN);
        ASTNode body = parseStatement();
        return new WhileNode(condition, body);
    }

    // print → print ( expression ) ;
    private ASTNode parsePrint() {
        consume(TokenType.PRINT);
        consume(TokenType.LPAREN);
        ASTNode expr = parseExpression();
        consume(TokenType.RPAREN);
        consume(TokenType.SEMICOLON);
        return new PrintNode(expr);
    }

    // block → { statement_list }
    private ASTNode parseBlock() {
        consume(TokenType.LBRACE);
        List<ASTNode> stmts = parseStatementList();
        consume(TokenType.RBRACE);
        return new BlockNode(stmts);
    }

    // expression → simple_expression [ relop simple_expression ]
    private ASTNode parseExpression() {
        ASTNode left = parseSimpleExpression();
        if (isRelop()) {
            String op = current().lexeme;
            pos++;
            ASTNode right = parseSimpleExpression();
            return new BinOpNode(op, left, right);
        }
        return left;
    }

    private boolean isRelop() {
        switch (current().type) {
            case EQUAL: case NOT_EQUAL: case GREATER: case LESS: case GREATER_EQUAL: case LESS_EQUAL:
                return true;
            default:
                return false;
        }
    }

    // simple_expression → term { (+|-) term }
    private ASTNode parseSimpleExpression() {
        ASTNode node = parseTerm();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            String op = current().lexeme;
            pos++;
            node = new BinOpNode(op, node, parseTerm());
        }
        return node;
    }

    // term → factor { (*|/) factor }
    private ASTNode parseTerm() {
        ASTNode node = parseFactor();
        while (check(TokenType.MULTIPLY) || check(TokenType.DIVIDE)) {
            String op = current().lexeme;
            pos++;
            node = new BinOpNode(op, node, parseFactor());
        }
        return node;
    }

    // factor → ( expression ) | identifier | number
    private ASTNode parseFactor() {
        if (check(TokenType.LPAREN)) {
            pos++;
            ASTNode expr = parseExpression();
            consume(TokenType.RPAREN);
            return expr;
        }
        if (check(TokenType.IDENTIFIER)) {
            return new IdentifierNode(consume(TokenType.IDENTIFIER).lexeme);
        }
        if (check(TokenType.INTEGER)) {
            return new NumberNode(consume(TokenType.INTEGER).lexeme);
        }
        throw new RuntimeException(
            "[SYNTAX ERROR] Line " + current().line +
            ": unexpected token in expression: '" + current().lexeme + "'"
        );
    }

    // Simple wrapper for ProgramNode
    public static class ASTNode {}
    public static class ProgramNode extends ASTNode {
        public List<DeclarationNode> declarations;
        public List<ASTNode> statements;
        public ProgramNode(List<ASTNode> decls, List<ASTNode> stmts) {
            this.declarations = new ArrayList<>();
            this.statements = stmts;
            for (ASTNode n : decls) {
                if (n instanceof DeclarationNode) {
                    declarations.add((DeclarationNode) n);
                }
            }
        }
    }
    public static class DeclarationNode extends ASTNode {
        public String type, name;
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
        public ASTNode condition, thenBranch, elseBranch;
        public IfNode(ASTNode c, ASTNode t, ASTNode e) {
            condition = c; thenBranch = t; elseBranch = e;
        }
    }
    public static class WhileNode extends ASTNode {
        public ASTNode condition, body;
        public WhileNode(ASTNode c, ASTNode b) {
            condition = c; body = b;
        }
    }
    public static class PrintNode extends ASTNode {
        public ASTNode expr;
        public PrintNode(ASTNode e) { expr = e; }
    }
    public static class BlockNode extends ASTNode {
        public List<ASTNode> statements;
        public BlockNode(List<ASTNode> stmts) { statements = stmts; }
    }
    public static class BinOpNode extends ASTNode {
        public String operator;
        public ASTNode left, right;
        public BinOpNode(String op, ASTNode l, ASTNode r) {
            operator = op; left = l; right = r;
        }
    }
    public static class IdentifierNode extends ASTNode {
        public String name;
        public IdentifierNode(String name) { this.name = name; }
    }
    public static class NumberNode extends ASTNode {
        public String value;
        public NumberNode(String value) { this.value = value; }
    }
}