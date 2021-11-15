package util;

import java.util.HashMap;

public class OpMap {
    private static HashMap<String, TokenType> map = new HashMap<>();

    private OpMap() {
        map.put("(", TokenType.PAREN_L);
        map.put(")", TokenType.PAREN_R);
        map.put("{", TokenType.BRACE_L);
        map.put("}", TokenType.BRACE_R);
        map.put(";", TokenType.SEMICOLON);
        map.put("+", TokenType.PLUS);
        map.put("-", TokenType.MINUS);
    }

    public static TokenType getOpType(String op) {
        return map.get(op);
    }

    public static boolean isOp(String op) {
        return map.containsKey(op);
    }
}
