package util;

import java.util.HashMap;

public class OpMap {
    private static HashMap<String, TokenType> map = new HashMap<>();
    private static OpMap instance = null;

    private OpMap() {
        map.put("(", TokenType.PAREN_L);
        map.put(")", TokenType.PAREN_R);
        map.put("{", TokenType.BRACE_L);
        map.put("}", TokenType.BRACE_R);
        map.put(",", TokenType.COMMA);
        map.put(";", TokenType.SEMICOLON);
        map.put("+", TokenType.ADD);
        map.put("-", TokenType.MIN);
        map.put("*", TokenType.MUL);
        map.put("/", TokenType.DIV);
        map.put("%", TokenType.MOD);
        map.put("=", TokenType.EQ);
    }

    public static OpMap getInstance() {
        if (instance == null)
            instance = new OpMap();
        return instance;
    }

    public TokenType getOpType(String op) {
        return map.get(op);
    }

    public boolean isOp(String op) {
        return map.containsKey(op);
    }
}
