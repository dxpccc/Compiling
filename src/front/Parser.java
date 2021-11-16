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

    public NumberAST parseNumber(String number) {
        return new NumberAST(number);
    }

    public IdentityAST parseIdent(String ident) {
        return new IdentityAST(ident);
    }

    public BaseAST parseUnaryExp() {
        Token token = getNextToken();

        if (token == null)
            return null;
        else if (token.getType() == TokenType.PLUS || token.getType() == TokenType.MINUS) {
            return new UnaryExpAST(token.getValue(), parseUnaryExp());
        } else if (token.getType() == TokenType.PAREN_L) {
            BaseAST ast = parseUnaryExp();
            if (getNextToken().getType() != TokenType.PAREN_R)
                return null;
            else
                return ast;
        } else if (token.getType() == TokenType.NUMBER) {
            return parseNumber(token.getValue());
        } else
            return null;
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
            StmtAST stmtAST = parseStmt();
            if (stmtAST != null && getNextToken().getType() == TokenType.BRACE_R)
                return new BlockAST(stmtAST);
            else
                return null;
        } else
            return null;
    }

    public FuncDefAST parseFuncDef() {
        Token token = getNextToken();
        if (token != null && token.getType() == TokenType.INT) {
            if (getNextToken().getType() == TokenType.MAIN && getNextToken().getType() == TokenType.PAREN_L && getNextToken().getType() == TokenType.PAREN_R) {
                BlockAST blockAST = parseBlock();
                if (blockAST != null)
                    return new FuncDefAST("int", "main", blockAST);
                else
                    return null;
            }
            else
                return null;
        } else
            return null;
    }

    public CompUnitAST parseCompUnit() {
        FuncDefAST funcDefAST = parseFuncDef();
        if (funcDefAST == null)
            return null;
        else
            return new CompUnitAST(funcDefAST);
    }
}
