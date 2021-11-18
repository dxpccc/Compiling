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

    /*
    * Add  -> Mul RAdd
    * RAdd -> ('+'|'-') RAdd
    *      -> eps
    * */
    public AddExpAST parseAddExp() {
        MulExpAST mul;
        RAddExpAST r_add;
        if ((mul = parseMulExp()) == null)
            return null;
        else if ((r_add = parseRAddExp()) == null)
            return null;
        else
            return new AddExpAST(mul, r_add);
    }

    public RAddExpAST parseRAddExp() {
        int mark = index;
        Token token = getNextToken();
        if (token != null && (token.getType() == TokenType.ADD || token.getType() == TokenType.MIN)) {
            UnaryExpAST unary;
            RAddExpAST r_add;
            if ((unary = parseUnaryExp()) == null) {
                index = mark;
                return null;
            } else if ((r_add = parseRAddExp()) == null) {
                return new RAddExpAST(token.getValue(), unary, null);
            } else
                return new RAddExpAST(token.getValue(), unary, r_add);
        } else {
            index = mark;
            return null;
        }
    }

    /*
    * Mul  -> Unary RMul
    * RMul -> ('*'|'/'|'%') Unary RMul
    *      -> eps
    * */
    public MulExpAST parseMulExp() {
        UnaryExpAST unary;
        RMulExpAST r_mul;
        if ((unary = parseUnaryExp()) == null)
            return null;
        else if ((r_mul = parseRMulExp()) == null)
            return null;
        else
            return new MulExpAST(unary, r_mul);
    }

    public RMulExpAST parseRMulExp() {
        int mark = index;
        Token token = getNextToken();
        if (token != null && (token.getType() == TokenType.MUL || token.getType() == TokenType.DIV || token.getType() == TokenType.MOD)) {
            UnaryExpAST unary;
            RMulExpAST r_mul;
            if ((unary = parseUnaryExp()) == null) {
                index = mark;
                return null;
            } else if ((r_mul = parseRMulExp()) == null) {
                return new RMulExpAST(token.getValue(), unary, null);
            } else
                return new RMulExpAST(token.getValue(), unary, r_mul);
        } else {
            index = mark;
            return null;
        }
    }

    public UnaryExpAST parseUnaryExp() {
        Token token = getNextToken();

        if (token == null)
            return null;
        else if (token.getType() == TokenType.NUMBER) {
            NumberAST numberAST = new NumberAST(token.getValue());
            return new UnaryExpAST(null, numberAST);
        } else if (token.getType() == TokenType.ADD || token.getType() == TokenType.MIN) {
            return new UnaryExpAST(token.getValue(), parseUnaryExp());
        } else if (token.getType() == TokenType.PAREN_L) {
            AddExpAST ast;
            Token next_token;
            if ((ast = parseAddExp()) == null)
                return null;
            else if ((next_token = getNextToken()).getType() != TokenType.PAREN_R)
                return null;
            else
                return new UnaryExpAST(next_token.getValue(), ast);
        } else
            return null;
    }

    public StmtAST parseStmt() {
        Token token = getNextToken();
        if (token != null && token.getType() == TokenType.RETURN) {
            AddExpAST addExpAST = parseAddExp();
            if (addExpAST == null)
                return null;
            else if ((token = getNextToken()) != null && token.getType() == TokenType.SEMICOLON) {
                return new StmtAST(addExpAST);
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
