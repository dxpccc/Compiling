package front;

import util.OpMap;
import util.Token;
import util.TokenType;

import java.io.*;
import java.util.ArrayList;

/*
* 词法分析程序
* */
public class Lexer {
    private final String path;
    private Reader reader;
    private BufferedReader bfdReader;
    private Token token;
    private ArrayList<Token> tokens = new ArrayList<>();

    private int curChar = ' ';

    public Lexer(String path) {
        this.path = path;
        token = new Token(TokenType.ERR, null);
    }

    public ArrayList<Token> analyse() {
        File input = new File(path);
        try {
            reader = new FileReader(input);
            bfdReader = new BufferedReader(reader);
            while ((token = getNextToken()).getType() != TokenType.ERR && token.getType() != TokenType.EOF)
                tokens.add(token);
            bfdReader.close();
            reader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        if (token.getType() == TokenType.ERR)
            return null;
        else
            return tokens;
    }

    private Token getNextToken() throws IOException {
        Token token;
        while (isBlank(curChar))        // 读掉空白
            curChar = bfdReader.read();

        if (curChar == '/') {
            // 清除注释
            bfdReader.mark(1);
            int mark = curChar;
            curChar = bfdReader.read();
            if (curChar == '/') {
                while (curChar != '\n' && curChar != -1)
                    curChar = bfdReader.read();    // 吃掉注释
                if (curChar == '\n')
                    token = getNextToken();
                else
                    token = new Token(TokenType.EOF, null);
            } else if (curChar == '*') {
                int lastChar;
                lastChar = bfdReader.read();
                if (lastChar != -1) {
                    curChar = bfdReader.read();
                    while (curChar != -1 && !(lastChar == '*' && curChar == '/')) {
                        // 吃掉注释
                        lastChar = curChar;
                        curChar = bfdReader.read();
                    }
                    if (curChar == -1) {
                        // 注释符号不匹配，返回错误
                        token = new Token(TokenType.ERR, null);
                    } else {
                        curChar = bfdReader.read();
                        token = getNextToken();
                    }
                } else {
                    // 注释符号不匹配，返回错误
                    token = new Token(TokenType.ERR, null);
                }
            } else {
                // 不是注释，重置
                bfdReader.reset();
                curChar = mark;
                token = getToken();
            }
        } else
            token = getToken();
        return token;
    }

    private Token getToken() throws IOException {
        Token token = new Token();

        if (isNondigit(curChar)) {
            lexIdentifier(token);
        } else if (isDigit(curChar)) {
            lexNumber(token);
        } else if (isOperator(curChar)) {
            lexOperator(token);
        } else if (curChar == -1)
            token.setType(TokenType.EOF);
        return token;
    }

    private boolean isLetter(int ch) {
        return Character.isLetter(ch);
    }

    private boolean isDigit(int ch) {
        return Character.isDigit(ch);
    }

    private boolean isOctalDigit(int ch) {
        return Character.isDigit(ch) && ch < '8';
    }

    private boolean isHexDigit(int ch) {
        return Character.isDigit(ch) || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
    }

    private boolean isBlank(int ch) {
        return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t';
    }

    private boolean isOperator(int ch) {
        char c = (char) ch;
        return OpMap.getInstance().isOp(String.valueOf(c)) || c == '|' || c == '&';
    }

    private boolean isNondigit(int ch) {
        return isLetter(ch) || (char) ch == '_';
    }

    private void lexIdentifier(Token token) throws IOException {
        StringBuilder value = new StringBuilder();
        value.append((char) curChar);
        while (isNondigit(curChar = bfdReader.read()) || isDigit(curChar))
            value.append((char) curChar);

        switch (value.toString()) {
            case "const":
                token.setType(TokenType.CONST);
                token.setValue(value.toString());
                break;
            case "int":
                token.setType(TokenType.INT);
                token.setValue(value.toString());
                break;
            case "main":
                token.setType(TokenType.MAIN);
                token.setValue(value.toString());
                break;
            case "return":
                token.setType(TokenType.RETURN);
                token.setValue(value.toString());
                break;
            case "if":
                token.setType(TokenType.IF);
                token.setValue(value.toString());
                break;
            case "else":
                token.setType(TokenType.ELSE);
                token.setValue(value.toString());
                break;
            default:
                token.setType(TokenType.IDENT);
                token.setValue(value.toString());
                break;
        }
    }

    private void lexNumber(Token token) throws IOException {
        StringBuilder value = new StringBuilder();
        value.append((char) curChar);
        bfdReader.mark(1);
        if (curChar == '0') {
            curChar = bfdReader.read();
            if (curChar == 'x' || curChar == 'X') {
                // 16进制
                curChar = bfdReader.read();
                if (isHexDigit(curChar)) {
                    bfdReader.reset();
                    curChar = bfdReader.read();
                    value.append((char) curChar);
                    while (isHexDigit((curChar = bfdReader.read())))
                        value.append((char) curChar);
                    // 转10进制
                    int t = Integer.parseInt(value.substring(2), 16);
                    value.delete(0, value.length());
                    value.append(t);
                } else {
                    bfdReader.reset();
                    curChar = bfdReader.read();
                }
            } else if (isOctalDigit(curChar)) {
                value.append((char) curChar);
                // 8进制
                while (isOctalDigit((curChar = bfdReader.read())))
                    value.append((char) curChar);
                // 转8进制
                int t = Integer.parseInt(value.substring(1), 8);
                value.delete(0, value.length());
                value.append(t);
            }
        } else {
            while (isDigit(curChar = bfdReader.read())) {
                value.append((char) curChar);
            }
        }
        token.setType(TokenType.NUMBER);
        token.setValue(value.toString());
    }

    private void lexOperator(Token token) throws IOException {
        String op_single = Character.toString((char) curChar);
        curChar = bfdReader.read();
        String op_double = op_single + (char) curChar;
        switch (op_double) {
            case "==":
            case "!=":
            case ">=":
            case "<=":
            case "&&":
            case "||":
                token.setType(OpMap.getInstance().getOpType(op_double));
                token.setValue(op_double);
                curChar = bfdReader.read();
                break;
            default:
                token.setType(OpMap.getInstance().getOpType(op_single));
                token.setValue(op_single);
                break;
        }
    }
}
