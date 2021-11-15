package front;

import util.OpMap;
import util.Token;
import util.TokenType;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/*
* 词法分析程序
* */
public class Lexer {
    private String path;
    private Reader reader;
    private BufferedReader bfdReader;
    private ArrayList<Token> tokens = new ArrayList<>();

    private int curChar = ' ';
    private boolean stop = false;
    private boolean isAnnotation = false;
    //private StringBuilder token = new StringBuilder();

    public Lexer(String path) {
        this.path = path;
    }

    public ArrayList<Token> analyse() {
        File input = new File(path);
        try {
            reader = new FileReader(input);
            bfdReader = new BufferedReader(reader);
            Token token;
            while ((token = getNextToken()).getType() != TokenType.ERR && token.getType() != TokenType.EOF)
                tokens.add(token);
            bfdReader.close();
            reader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return tokens;
    }

    private Token getNextToken() throws IOException {
        Token token = null;
        while (isBlank(curChar))        // 读掉空白
            curChar = bfdReader.read();

        if (curChar == '/') {
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
            }
        } else
            token = getToken();
        return token;
    }

    private Token getToken() throws IOException {
        Token token = new Token();

        if (isLetter(curChar)) {
            lexIdentifier(token);
        } else if (isDigit(curChar)) {
            lexNumber(token);
        } else if (isOperator(curChar)) {
            lexOperator(token);
        }
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
        return OpMap.isOp(Character.toString((char) ch));
    }

    private void lexIdentifier(Token token) throws IOException {
        StringBuilder value = new StringBuilder();
        value.append(curChar);
        while (isLetter(curChar = bfdReader.read()))
            value.append(curChar);

        switch (value.toString()) {
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
            default:
                token.setType(TokenType.ERR);
                token.setValue(value.toString());
                break;
        }
    }

    private void lexNumber(Token token) throws IOException {
        StringBuilder value = new StringBuilder();
        value.append(curChar);
        bfdReader.mark(1);
        if (curChar == '0') {
            curChar = bfdReader.read();
            if (curChar == 'x' || curChar == 'X') {
                curChar = bfdReader.read();
                if (isDigit(curChar)) {
                    bfdReader.reset();
                    curChar = bfdReader.read();
                    value.append(curChar);
                    while (isHexDigit((curChar = bfdReader.read())))
                        value.append(curChar);
                } else {
                    bfdReader.reset();
                    curChar = bfdReader.read();
                }
            } else {
                while (isOctalDigit((curChar = bfdReader.read())))
                    value.append(curChar);
            }
        } else {
            while (isDigit(curChar)) {
                value.append(curChar);
            }
        }
        token.setType(TokenType.NUMBER);
        token.setValue(value.toString());
    }

    private void lexOperator(Token token) throws IOException {
        token.setType(OpMap.getOpType(Character.toString((char) curChar)));
        token.setValue(String.valueOf((char) curChar));
    }
}
