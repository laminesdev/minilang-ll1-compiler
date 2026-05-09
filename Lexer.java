import java.io.*;
import java.util.*;

public class Lexer {
    private BufferedReader reader;
    private int currentChar;
    private int line = 1, column = 1;
    private boolean hasError = false;
    private int charPos = 0;

    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        keywords.put("int", TokenType.INT);
        keywords.put("float", TokenType.FLOAT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("print", TokenType.PRINT);
    }

    public Lexer(File file) throws IOException {
        reader = new BufferedReader(new FileReader(file));
        advance();
    }

    private void advance() throws IOException {
        if (currentChar == '\n') { line++; column = 0; }
        column++;
        currentChar = reader.read();
        charPos++;
        if (currentChar == -1) currentChar = '\0';
    }

    private void skipWhitespace() throws IOException {
        while (Character.isWhitespace(currentChar)) advance();
        if (currentChar == '/') {
            int peek = reader.read();
            if (peek == '/') {
                while (currentChar != '\n' && currentChar != '\0') advance();
                skipWhitespace();
            } else {
                reader.reset();
            }
        }
    }

    public Token nextToken() throws IOException {
        skipWhitespace();
        if (currentChar == '\0') return new Token(TokenType.EOF, "EOF", line, column);

        if (Character.isLetter(currentChar)) return scanIdentifier();
        if (Character.isDigit(currentChar)) return scanNumber();

        return scanOperator();
    }

    private Token scanIdentifier() throws IOException {
        StringBuilder sb = new StringBuilder();
        int startLine = line, startCol = column;
        while (Character.isLetterOrDigit(currentChar)) {
            sb.append((char)currentChar);
            advance();
        }
        String text = sb.toString();
        TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
        return new Token(type, text, startLine, startCol);
    }

    private Token scanNumber() throws IOException {
        StringBuilder sb = new StringBuilder();
        int startLine = line, startCol = column;
        boolean isFloat = false;
        while (Character.isDigit(currentChar)) {
            sb.append((char)currentChar);
            advance();
        }
        if (currentChar == '.') {
            isFloat = true;
            sb.append('.');
            advance();
            while (Character.isDigit(currentChar)) {
                sb.append((char)currentChar);
                advance();
            }
        }
        TokenType type = isFloat ? TokenType.FLOAT : TokenType.INTEGER;
        return new Token(type, sb.toString(), startLine, startCol);
    }

    private Token scanOperator() throws IOException {
        int startLine = line, startCol = column;
        char c = (char)currentChar;
        advance();

        switch (c) {
            case '=':
                if (currentChar == '=') { advance(); return new Token(TokenType.EQUAL, "==", startLine, startCol); }
                return new Token(TokenType.ASSIGN, "=", startLine, startCol);
            case '!':
                if (currentChar == '=') { advance(); return new Token(TokenType.NOT_EQUAL, "!=", startLine, startCol); }
                return error("!");
            case '<':
                if (currentChar == '=') { advance(); return new Token(TokenType.LESS_EQUAL, "<=", startLine, startCol); }
                return new Token(TokenType.LESS, "<", startLine, startCol);
            case '>':
                if (currentChar == '=') { advance(); return new Token(TokenType.GREATER_EQUAL, ">=", startLine, startCol); }
                return new Token(TokenType.GREATER, ">", startLine, startCol);
            case '+': return new Token(TokenType.PLUS, "+", startLine, startCol);
            case '-': return new Token(TokenType.MINUS, "-", startLine, startCol);
            case '*': return new Token(TokenType.MULTIPLY, "*", startLine, startCol);
            case '/': return new Token(TokenType.DIVIDE, "/", startLine, startCol);
            case ';': return new Token(TokenType.SEMICOLON, ";", startLine, startCol);
            case '(': return new Token(TokenType.LPAREN, "(", startLine, startCol);
            case ')': return new Token(TokenType.RPAREN, ")", startLine, startCol);
            case '{': return new Token(TokenType.LBRACE, "{", startLine, startCol);
            case '}': return new Token(TokenType.RBRACE, "}", startLine, startCol);
            default: return error(Character.toString(c));
        }
    }

    private Token error(String msg) {
        hasError = true;
        System.err.println("LEXICAL ERROR line " + line + ": " + msg);
        return new Token(TokenType.ERROR, msg, line, column);
    }

    public boolean hasError() { return hasError; }
    
    public int getPosition() { return charPos; }
    
    public void setPosition(int pos) {
        try {
            reader.reset();
            currentChar = ' ';
            charPos = 0;
            line = 1; column = 1;
            while (charPos < pos && currentChar != '\0') {
                advance();
            }
        } catch (IOException e) {}
    }
}
