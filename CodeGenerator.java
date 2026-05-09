import java.io.*;
import java.util.*;

public class CodeGenerator {
    private List<Token> tokens;
    private int pos = 0;
    private Token current;
    private boolean hasError = false;
    private boolean hasReturn = false;
    private StringBuilder code = new StringBuilder();
    private Map<String, String> varTypes = new HashMap<>();
    private int labelCount = 0;

    public CodeGenerator(List<Token> tokens) {
        this.tokens = tokens;
    }

    public String generate() {
        code.append("#include <stdio.h>\n\n");
        code.append("int main() {\n");
        
        current = next();
        while (current.type != TokenType.EOF && !hasError) {
            statement();
        }
        
        if (!hasReturn) {
            code.append("    return 0;\n");
        }
        code.append("}\n");
        return code.toString();
    }

    private Token next() {
        return (pos < tokens.size()) ? tokens.get(pos++) : new Token(TokenType.EOF, "", 0, 0);
    }

    private void statement() {
        if (current.type == TokenType.INT) declaration("int");
        else if (current.type == TokenType.FLOAT) declaration("float");
        else if (current.type == TokenType.IDENTIFIER) assignment();
        else if (current.type == TokenType.IF) ifStmt();
        else if (current.type == TokenType.WHILE) whileStmt();
        else if (current.type == TokenType.PRINT) printStmt();
        else if (current.type == TokenType.RETURN) returnStmt();
        else if (current.type == TokenType.LBRACE) block();
        else if (current.type != TokenType.EOF) error("Unexpected: " + current.lexeme);
    }

    private void declaration(String type) {
        consume(type.equals("int") ? TokenType.INT : TokenType.FLOAT);
        String name = current.lexeme;
        if (varTypes.containsKey(name)) {
            error("Variable already declared: " + name);
            return;
        }
        varTypes.put(name, type);
        String cType = type.equals("int") ? "int" : "float";
        consume(TokenType.IDENTIFIER);
        
        if (current.type == TokenType.ASSIGN) {
            consume(TokenType.ASSIGN);
            code.append("    ").append(cType).append(" ").append(name).append(" = ");
            code.append(expr()).append(";\n");
        } else {
            code.append("    ").append(cType).append(" ").append(name).append(" = 0;\n");
        }
        consume(TokenType.SEMICOLON);
    }

    private void assignment() {
        String name = current.lexeme;
        if (!varTypes.containsKey(name)) {
            error("Undeclared variable: " + name);
            return;
        }
        consume(TokenType.IDENTIFIER);
        consume(TokenType.ASSIGN);
        
        code.append("    ").append(name).append(" = ").append(expr()).append(";\n");
        consume(TokenType.SEMICOLON);
    }

    private void ifStmt() {
        String elseLabel = "L" + labelCount++;
        String endLabel = "L" + labelCount++;
        
        consume(TokenType.IF);
        consume(TokenType.LPAREN);
        String cond = expr();
        consume(TokenType.RPAREN);
        
        code.append("    if (!(").append(cond).append(")) goto ").append(elseLabel).append(";\n");
        statement();
        
        if (current.type == TokenType.ELSE) {
            consume(TokenType.ELSE);
            code.append("    goto ").append(endLabel).append(";\n");
            code.append(elseLabel).append(":\n");
            statement();
            code.append(endLabel).append(":\n");
        } else {
            code.append(elseLabel).append(":\n");
        }
    }

    private void whileStmt() {
        String startLabel = "L" + labelCount++;
        String endLabel = "L" + labelCount++;
        
        consume(TokenType.WHILE);
        consume(TokenType.LPAREN);
        
        code.append(startLabel).append(":\n");
        String cond = expr();
        code.append("    if (!(").append(cond).append(")) goto ").append(endLabel).append(";\n");
        consume(TokenType.RPAREN);
        
        statement();
        
        code.append("    goto ").append(startLabel).append(";\n");
        code.append(endLabel).append(":\n");
    }

    private void printStmt() {
        consume(TokenType.PRINT);
        consume(TokenType.LPAREN);
        
        code.append("    printf(\"");
        String format = "%d";
        if (current.type == TokenType.FLOAT || (current.type == TokenType.IDENTIFIER && "float".equals(varTypes.get(current.lexeme)))) {
            format = "%f";
        }
        code.append(format).append("\\n\", ");
        code.append(expr());
        code.append(");\n");
        
        consume(TokenType.RPAREN);
        consume(TokenType.SEMICOLON);
    }

    private void returnStmt() {
        consume(TokenType.RETURN);
        hasReturn = true;
        String returnValue = expr();
        code.append("    printf(\"%d\\n\", ");
        code.append(returnValue);
        code.append(");\n");
        code.append("    return ");
        code.append(returnValue);
        code.append(";\n");
        consume(TokenType.SEMICOLON);
    }

    private void block() {
        consume(TokenType.LBRACE);
        while (current.type != TokenType.RBRACE && current.type != TokenType.EOF) {
            statement();
        }
        consume(TokenType.RBRACE);
    }

    private String expr() {
        String left = term();
        while (current.type == TokenType.PLUS || current.type == TokenType.MINUS) {
            TokenType op = current.type;
            current = next();
            String right = term();
            left = "(" + left + " " + opToStr(op) + " " + right + ")";
        }
        if (current.type == TokenType.EQUAL || current.type == TokenType.NOT_EQUAL ||
            current.type == TokenType.LESS || current.type == TokenType.GREATER ||
            current.type == TokenType.LESS_EQUAL || current.type == TokenType.GREATER_EQUAL) {
            TokenType op = current.type;
            current = next();
            String right = term();
            left = "(" + left + " " + relOpToStr(op) + " " + right + ")";
        }
        return left;
    }

    private String term() {
        String left = factor();
        while (current.type == TokenType.MULTIPLY || current.type == TokenType.DIVIDE) {
            TokenType op = current.type;
            current = next();
            String right = factor();
            left = "(" + left + " " + opToStr(op) + " " + right + ")";
        }
        return left;
    }

    private String factor() {
        if (current.type == TokenType.INTEGER || current.type == TokenType.FLOAT) { 
            String v = current.lexeme; 
            current = next(); 
            return v; 
        }
        if (current.type == TokenType.IDENTIFIER) { 
            String n = current.lexeme; 
            current = next(); 
            return n; 
        }
        if (current.type == TokenType.LPAREN) { 
            current = next(); 
            String v = expr(); 
            consume(TokenType.RPAREN); 
            return v; 
        }
        error("Expected factor");
        return "0";
    }

    private void consume(TokenType t) {
        if (current.type == t) current = next();
        else error("Expected " + t + " but got " + current.type);
    }

    private void consume(String type) {
        TokenType tt = type.equals("int") ? TokenType.INT : TokenType.FLOAT;
        if (current.type == tt) current = next();
        else error("Expected " + tt + " but got " + current.type);
    }

    private String opToStr(TokenType op) {
        switch (op) {
            case PLUS: return "+";
            case MINUS: return "-";
            case MULTIPLY: return "*";
            case DIVIDE: return "/";
            default: return "?";
        }
    }

    private String relOpToStr(TokenType op) {
        switch (op) {
            case EQUAL: return "==";
            case NOT_EQUAL: return "!=";
            case LESS: return "<";
            case GREATER: return ">";
            case LESS_EQUAL: return "<=";
            case GREATER_EQUAL: return ">=";
            default: return "?";
        }
    }

    private void error(String msg) {
        hasError = true;
        System.err.println("CODE GEN ERROR line " + current.line + ": " + msg);
    }

    public boolean hasError() { return hasError; }
}