package front;

import util.AST.*;
import util.Token;
import util.TokenType;

import java.util.ArrayList;

public class Parser {
    private ArrayList<Token> tokens;
    private int index;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        index = -1;
    }

    public Parser() {
        this(null);
    }

    private Token getNextToken() {
        if (index < tokens.size() - 1) {
            index++;
            return tokens.get(index);
        } else
            return null;
    }

    public CompUnitAST analyse() {
        return parseCompUnit();
    }

    public NumberAST parseNumber(int number) {
        return new NumberAST(number);
    }

    public IdentityAST parseIdent(String ident) {
        return new IdentityAST(ident);
    }

    public StmtAST parseStmt() {
        Token token = getNextToken();
        if (token != null && token.getType() == TokenType.RETURN) {
            token = getNextToken();
            if (token.getType() == TokenType.NUMBER) {
                StmtAST stmtAST = new StmtAST(token.getValue());
                token = getNextToken();
                if (token.getType() == TokenType.SEMICOLON)
                    return stmtAST;
                else
                    return null;
            } else
                return null;
        } else
            return null;
    }

    public BlockAST parseBlock() {
        Token token = getNextToken();
        if (token != null && token.getType() == TokenType.BRACE_L) {
            BlockAST blockAST = new BlockAST(parseStmt());
            token = getNextToken();
            if (token.getType() == TokenType.BRACE_R)
                return blockAST;
            else
                return null;
        } else
            return null;
    }

    public FuncDefAST parseFuncDef() {
        Token token = getNextToken();
        if (token != null && token.getType() == TokenType.INT) {
            if (getNextToken().getType() == TokenType.MAIN && getNextToken().getType() == TokenType.PAREN_L && getNextToken().getType() == TokenType.PAREN_R)
                return new FuncDefAST("int", "main", parseBlock());
            else
                return null;
        } else
            return null;
    }

    public CompUnitAST parseCompUnit() {
        return new CompUnitAST(parseFuncDef());
    }
}
