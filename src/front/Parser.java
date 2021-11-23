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

    private void rollBack() {
        --index;
    }

    private Token nextToken() {
        if (index < tokens.size() - 1) {
            return tokens.get(index + 1);
        } else
            return null;
    }

    public CompUnitAST analyse() {
        return parseCompUnit();
    }

    /*
    * CompUnit -> FuncDef
    * */
    public CompUnitAST parseCompUnit() {
        FuncDefAST funcDefAST = parseFuncDef();
        if (funcDefAST == null)
            return null;
        else
            return new CompUnitAST(funcDefAST);
    }

    /*
    * FuncDef -> 'int' 'main' '(' ')' Block
    * */
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

    /*
    * Block -> '{' {ConstDecl | VarDecl | Stmt} '}'
    * */
    public BlockAST parseBlock() {
        Token token = getNextToken();
        if (token != null && token.getType() == TokenType.BRACE_L) {
            ArrayList<BlockItemAST> asts = new ArrayList<>();
            while ((token = nextToken()) != null && token.getType() != TokenType.BRACE_R) {
                BlockItemAST ast = parseBlockItem();
                if (ast != null)
                    asts.add(ast);
                else
                    return null;
            }
            if (token != null)
                return new BlockAST(asts);
            else
                return null;
        } else
            return null;
    }

    /*
    * BlockItem -> ConstDecl | VarDecl | Stmt
    * */
    public BlockItemAST parseBlockItem() {
        Token token = nextToken();
        ConstDeclAST const_decl;
        VarDeclAST var_decl;
        StmtAST stmt;
        if (token == null) {
            return null;
        } else if (token.getType() == TokenType.CONST) {
            const_decl = parseConstDecl();
            if (const_decl == null)
                return null;
            else
                return new BlockItemAST(BlockItemAST.Type.CONSTDECL, const_decl, null, null);
        } else if (token.getType() == TokenType.INT) {
            var_decl = parseVarDecl();
            if (var_decl == null)
                return null;
            else
                return new BlockItemAST(BlockItemAST.Type.VARDECL, null, var_decl, null);
        } else {
            stmt = parseStmt();
            if (stmt == null)
                return null;
            else
                return new BlockItemAST(BlockItemAST.Type.STMT, null, null, stmt);
        }
    }

    /*
    * ConstDecl -> 'const' 'int' ConstDef { ',' ConstDef } ';'
    * */
    public ConstDeclAST parseConstDecl() {
        Token token = getNextToken();
        if (token == null) {
            return null;
        } else if (token.getType() != TokenType.CONST) {
            return null;
        } else if ((token = getNextToken()) == null) {
            return null;
        } else if (token.getType() != TokenType.INT) {
            return null;
        } else {
            ArrayList<ConstDefAST> asts = new ArrayList<>();
            ConstDefAST ast = parseConstDef();
            if (ast == null) {
                return null;
            } else {
                asts.add(ast);
                while ((token = getNextToken()).getType() == TokenType.COMMA) {
                    ast = parseConstDef();
                    if (ast != null)
                        asts.add(ast);
                    else
                        return null;
                }
                if (token.getType() != TokenType.SEMICOLON) {
                    return null;
                } else {
                    return new ConstDeclAST(asts);
                }
            }
        }
    }

    /*
    * ConstDef -> Ident '=' ConstInitVal
    * */
    public ConstDefAST parseConstDef() {
        Token token;
        String ident;
        AddExpAST add;
        token = getNextToken();
        if (token == null) {
            return null;
        } else if (token.getType() != TokenType.IDENT) {
            return null;
        } else {
            ident = token.getValue();
            token = getNextToken();
            if (token.getType() != TokenType.ASSIGN) {
                return null;
            } else if ((add = parseAddExp()) == null) {
                return null;
            } else {
                return new ConstDefAST(ident, add);
            }
        }
    }

    /*
    * VarDecl -> 'int' VarDef { ',' VarDef } ';'
    * */
    public VarDeclAST parseVarDecl() {
        Token token = getNextToken();
        if (token == null) {
            return null;
        } else if (token.getType() != TokenType.INT) {
            return null;
        } else {
            ArrayList<VarDefAST> asts = new ArrayList<>();
            VarDefAST ast = parseVarDef();
            if (ast == null) {
                return null;
            } else {
                asts.add(ast);
                while ((token = getNextToken()).getType() == TokenType.COMMA) {
                    ast = parseVarDef();
                    if (ast != null)
                        asts.add(ast);
                    else
                        return null;
                }
                if (token.getType() != TokenType.SEMICOLON) {
                    return null;
                } else {
                    return new VarDeclAST(asts);
                }
            }
        }
    }

    /*
    * VarDef  -> Ident | Ident '=' InitVal
    * InitVal -> Exp
    * */
    public VarDefAST parseVarDef() {
        Token token;
        String ident;
        AddExpAST add;
        token = getNextToken();
        if (token == null) {
            return null;
        } else if (token.getType() != TokenType.IDENT) {
            return null;
        } else {
            ident = token.getValue();
            token = getNextToken();
            if (token.getType() != TokenType.ASSIGN) {
                rollBack();
                return new VarDefAST(VarDefAST.Type.UNINIT, ident, null);
            } else if ((add = parseAddExp()) == null) {
                return null;
            } else {
                return new VarDefAST(VarDefAST.Type.INIT, ident, add);
            }
        }
    }

    /*
    * Stmt -> LVal '=' Add ';'
    *      -> [Add] ';'
    *      -> 'return' Add ';'
    * LVal -> Ident
    * */
    public StmtAST parseStmt() {
        String ident;
        AddExpAST addExpAST;
        Token token = getNextToken();
        if (token == null) {
            return null;
        } else if (token.getType() == TokenType.IDENT) {
            ident = token.getValue();
            token = getNextToken();
            if (token.getType() != TokenType.ASSIGN) {
                return null;
            } else {
                addExpAST = parseAddExp();
                if (addExpAST == null) {
                    return null;
                } else if ((token = getNextToken()).getType() != TokenType.SEMICOLON) {
                    return null;
                } else
                    return new StmtAST(StmtAST.Type.ASSIGN, new AssignAST(ident, addExpAST), null);
            }
        } else if (token.getType() == TokenType.RETURN) {
            addExpAST = parseAddExp();
            if (addExpAST == null) {
                return null;
            } else if ((token = getNextToken()) != null && token.getType() == TokenType.SEMICOLON) {
                return new StmtAST(StmtAST.Type.RETURN, null, new ReturnAST(addExpAST));
            } else
                return null;
        } else {
            rollBack();
            addExpAST = parseAddExp();
            if (addExpAST == null) {
                return null;
            } else if ((token = getNextToken()) == null) {
                return null;
            } else if (token.getType() != TokenType.SEMICOLON) {
                return null;
            } else {
                return new StmtAST(StmtAST.Type.EXP, null, null);
            }
        }
    }

    /*
    * Add  -> Mul RAdd
    * RAdd -> ('+'|'-') Mul RAdd
    *      -> eps
    *
    * Add  -> Mul { ('+'|'-') Mul }
    * */
    public AddExpAST parseAddExp() {
        MulExpAST LHS;
        AddExpAST RHS;
        Token token;
        if ((LHS = parseMulExp()) == null) {
            return null;
        } else if ((token = getNextToken()) == null) {
            return new AddExpAST(null, LHS, null);
        } else if (token.getType() != TokenType.ADD && token.getType() != TokenType.MIN) {
            rollBack();
            return new AddExpAST(null, LHS, null);
        } else if ((RHS = parseAddExp()) == null) {
            return null;
        } else {
            return new AddExpAST(token.getValue(), LHS, RHS);
        }
    }

    /*
    * Mul  -> Unary RMul
    * RMul -> ('*'|'/'|'%') Unary RMul
    *      -> eps
    *
    * Mul  -> Unary { ('*'|'/'|'%') Unary }
    * */
    public MulExpAST parseMulExp() {
        UnaryExpAST LHS;
        MulExpAST RHS;
        Token token;
        if ((LHS = parseUnaryExp()) == null) {
            return null;
        } else if ((token = getNextToken()) == null) {
            return new MulExpAST(null, LHS, null);
        } else if (token.getType() != TokenType.MUL && token.getType() != TokenType.DIV && token.getType() != TokenType.MOD) {
            rollBack();
            return new MulExpAST(null, LHS, null);
        } else if ((RHS = parseMulExp()) == null) {
            return null;
        } else {
            return new MulExpAST(token.getValue(), LHS, RHS);
        }
    }

    /*
    * Unary -> { UnaryOp } Primary
    * */
    public UnaryExpAST parseUnaryExp() {
        Token token;
        String op;
        PrimaryExpAST primary;
        token = getNextToken();
        op = "+";
        while (token != null && (token.getType() == TokenType.ADD || token.getType() == TokenType.MIN)) {
            String opp = token.getValue();
            if (opp.equals("-")) {
                if (op.equals("-"))
                    op = "+";
                else
                    op = "-";
            }
            token = getNextToken();
        }
        if (token == null) {
            return null;
        } else {
            rollBack();
            primary = parsePrimaryExp();
            if (primary == null) {
                return null;
            } else {
                return new UnaryExpAST(op, primary);
            }
        }
    }

    /*
    * Primary -> '(' Exp ')' | LVal | Number
    * LVal    -> Ident
    * */
    public PrimaryExpAST parsePrimaryExp() {
        Token token = getNextToken();
        if (token == null) {
            return null;
        } else if (token.getType() == TokenType.PAREN_L) {
            AddExpAST add = parseAddExp();
            if (add == null) {
                return null;
            } else if ((token = getNextToken()) == null) {
                return null;
            } else if (token.getType() != TokenType.PAREN_R) {
                return null;
            } else {
                return new PrimaryExpAST(PrimaryExpAST.Type.EXP, add, null, null);
            }
        } else if (token.getType() == TokenType.IDENT) {
            return new PrimaryExpAST(PrimaryExpAST.Type.LVAL, null, token.getValue(), null);
        } else if (token.getType() == TokenType.NUMBER){
            return new PrimaryExpAST(PrimaryExpAST.Type.NUMBER, null, null, token.getValue());
        } else {
            return null;
        }
    }

    public NumberAST parseNumber(String number) {
        return new NumberAST(number);
    }

    public IdentityAST parseIdent(String ident) {
        return new IdentityAST(ident);
    }
}
