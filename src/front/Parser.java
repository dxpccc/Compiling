package front;

import util.AST.*;
import util.Token;
import util.TokenType;

import java.util.ArrayList;

public class Parser {
    private ArrayList<Token> tokens;
    private int index;
    private int mark;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        index = -1;
        mark = -1;
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

    /************ token流标记和还原 ************/
    private void mark() {
        mark = index;
    }

    private void reset() {
        index = mark;
    }
    /************ token流标记和还原 ************/

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
    * CompUnit -> [CompUnit] (Decl | FuncDef)
    * CompUnit -> ( Decl | FuncDef ) { ( Decl | FuncDef ) }
    * */
    private CompUnitAST parseCompUnit() {
        GlobalDeclAST decl;
        FuncDefAST func;
        ArrayList<CompUnitElement> elems = new ArrayList<>();
        while (nextToken() != null) {
            mark();
            decl = parseDecl();
            if (decl == null) {
                reset();
                func = parseFuncDef();
                if (func == null)
                    return null;
                else
                    elems.add(new CompUnitElement(CompUnitElement.Type.FUNC, null, func));
            } else {
                elems.add(new CompUnitElement(CompUnitElement.Type.GLOBAL, decl, null));
            }
        }
        if (elems.size() == 0)
            return null;
        else
            return new CompUnitAST(elems);
    }

    /*
    * Decl -> ConstDecl | VarDecl
    * */
    private GlobalDeclAST parseDecl() {
        Token token;
        ConstDeclAST const_decl;
        VarDeclAST var_decl;
        if ((token = nextToken()) == null) {
            return null;
        } else if (token.getType() == TokenType.CONST) {
            const_decl = parseConstDecl();
            if (const_decl == null) {
                return null;
            } else {
                return new GlobalDeclAST(GlobalDeclAST.Type.CONST, const_decl, null);
            }
        } else if (token.getType() == TokenType.INT) {
            var_decl = parseVarDecl();
            if (var_decl == null) {
                return null;
            } else {
                return new GlobalDeclAST(GlobalDeclAST.Type.VAR, null, var_decl);
            }
        } else {
            return null;
        }
    }

    /*
    * FuncDef -> FuncType Ident '(' FuncFParams ')' Block
    * */
    private FuncDefAST parseFuncDef() {
        Token token = getNextToken();
        String func_type;
        String ident;
        FuncParams params;
        BlockAST blockAST;

        if (token == null) {
            return null;
        } else if (token.getType() != TokenType.INT && token.getType() != TokenType.VOID) {
            return null;
        } else {
            func_type = token.getValue();
            if ((token = getNextToken()) == null) {
                return null;
            } else if (token.getType() != TokenType.IDENT) {
                return null;
            } else {
                ident = token.getValue();
                if ((token = getNextToken()) == null || token.getType() != TokenType.PAREN_L) {
                    return null;
                } else {
                    params = parseFuncParams();
                    if (params == null) {
                        return null;
                    } else if ((token = getNextToken()) == null || token.getType() != TokenType.PAREN_R) {
                        return null;
                    } else if ((blockAST = parseBlock()) == null) {
                        return null;
                    } else {
                        return new FuncDefAST(func_type, ident, params, blockAST);
                    }
                }
            }
        }
    }

    /*
    * FuncFParams  -> FuncFParam { ',' FuncFParam }
    * */
    private FuncParams parseFuncParams() {
        Token token;
        ArrayList<FuncParam> params = new ArrayList<>();
        FuncParam param;
        if ((token = nextToken()) != null && token.getType() == TokenType.PAREN_R) {
            return new FuncParams(params);
        } else {
            param = parseFuncParam();
            if (param == null) {
                return null;
            } else {
                params.add(param);
                while ((token = getNextToken()) != null && token.getType() == TokenType.COMMA) {
                    param = parseFuncParam();
                    if (param != null)
                        params.add(param);
                    else
                        return null;
                }
                rollBack();
                return new FuncParams(params);
            }
        }
    }

    /*
    * FuncFParam -> BType Ident ['[' ']' { '[' Exp ']' }]
    * */
    private FuncParam parseFuncParam() {
        Token token;
        String ident;
        int dim = 0;
        ArrayList<AddExpAST> lengths = new ArrayList<>();
        AddExpAST add;
        if ((token = getNextToken()) == null || token.getType() != TokenType.INT) {
            return null;
        } else if ((token = getNextToken()) == null || token.getType() != TokenType.IDENT) {
            return null;
        } else {
            ident = token.getValue();
            if ((token = nextToken()) != null && token.getType() == TokenType.SQUARE_L) {
                getNextToken();
                if ((token = getNextToken()) == null || token.getType() != TokenType.SQUARE_R) {
                    return null;
                } else {
                    dim++;
                    while (((token = nextToken()) != null && token.getType() == TokenType.SQUARE_L)) {
                        add = parseAddExp();
                        if (add != null && (token = getNextToken()) != null && token.getType() == TokenType.SQUARE_R) {
                            lengths.add(add);
                            dim++;
                        } else
                            return null;
                    }
                    return new FuncParam(ident, dim, lengths);
                }
            } else {
                // int类型参数
                return new FuncParam(ident, dim, null);
            }
        }
    }

    /*
    * Block -> '{' {ConstDecl | VarDecl | Stmt} '}'
    * */
    private BlockAST parseBlock() {
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
            if (token != null) {
                token = getNextToken();
                return new BlockAST(asts);
            } else
                return null;
        } else
            return null;
    }

    /*
    * BlockItem -> ConstDecl | VarDecl | Stmt
    * */
    private BlockItemAST parseBlockItem() {
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
    * ConstDecl -> 'const' 'int' ConstEle { ',' ConstEle } ';'
    * */
    private ConstDeclAST parseConstDecl() {
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
            ArrayList<ConstDeclElement> asts = new ArrayList<>();
            ConstDeclElement ast = parseConstDeclEle();
            if (ast == null) {
                return null;
            } else {
                asts.add(ast);
                while ((token = getNextToken()).getType() == TokenType.COMMA) {
                    ast = parseConstDeclEle();
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
    * ConstEle  -> ConstDef | ConstArray
    * */
    private ConstDeclElement parseConstDeclEle() {
        Token token;
        if ((token = getNextToken()) == null) {
            return null;
        } else if (token.getType() != TokenType.IDENT) {
            return null;
        } else if ((token = getNextToken()) == null) {
            return null;
        } else if (token.getType() == TokenType.ASSIGN) {
            rollBack();
            rollBack();
            return new ConstDeclElement(parseConstDef(), null, false);
        } else if (token.getType() == TokenType.SQUARE_L) {
            rollBack();
            rollBack();
            return new ConstDeclElement(null, parseConstArray(), true);
        } else {
            return null;
        }
    }

    /*
    * ConstDef -> Ident '=' ConstExp
    * */
    private ConstDefAST parseConstDef() {
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
    * ConstArray -> Ident '[' ConstExp ']' { '[' ConstExp ']' } '=' InitVal
    * */
    private ConstArrayAST parseConstArray() {
        Token token;
        String ident;
        int dim = 0;
        ArrayList<AddExpAST> lengths;
        InitValAST values;
        if ((token = getNextToken()) == null) {
            return null;
        } else if (token.getType() != TokenType.IDENT) {
            return null;
        } else {
            ident = token.getValue();
            token = nextToken();
            if (token == null) {
                return null;
            } else if (token.getType() != TokenType.SQUARE_L) {
                return null;
            } else {
                AddExpAST add;
                lengths = new ArrayList<>();
                do {
                    if ((token = getNextToken()) == null) {
                        return null;
                    } else if (token.getType() != TokenType.SQUARE_L) {
                        return null;
                    } else if ((add = parseAddExp()) == null) {
                        return null;
                    } else if ((token = getNextToken()) == null) {
                        return null;
                    } else if (token.getType() != TokenType.SQUARE_R) {
                        return null;
                    } else {
                        dim++;
                        lengths.add(add);
                    }
                } while ((token = nextToken()) != null && token.getType() == TokenType.SQUARE_L);
                if ((token = getNextToken()) == null) {
                    return null;
                } else if (token.getType() != TokenType.ASSIGN) {
                    return null;
                } else if ((values = parseInitVal()) == null) {
                    return null;
                } else {
                    return new ConstArrayAST(ident, dim, lengths, values);
                }
            }
        }
    }

    /*
    * InitVal = Exp | '{' [ InitVal { ',' InitVal } ] '}'
    * */
    private InitValAST parseInitVal() {
        Token token;
        InitValAST init_val;
        AddExpAST add;
        if ((token = nextToken()) == null) {
            return null;
        } else if (token.getType() == TokenType.BRACE_L) {
            getNextToken();
            if ((token = nextToken()) == null) {
                return null;
            } else if (token.getType() == TokenType.BRACE_R) {
                getNextToken();
                return new InitValAST(InitValAST.Type.EMPTY_INIT, null, null);
            } else {
                ArrayList<InitValAST> asts = new ArrayList<>();
                init_val = parseInitVal();
                if (init_val == null) {
                    return null;
                } else {
                    asts.add(init_val);
                    while ((token = getNextToken()).getType() == TokenType.COMMA) {
                        init_val = parseInitVal();
                        if (init_val != null)
                            asts.add(init_val);
                        else
                            return null;
                    }
                    if (token.getType() != TokenType.BRACE_R) {
                        return null;
                    } else {
                        return new InitValAST(InitValAST.Type.INITVAL, null, asts);
                    }
                }
            }
        } else if ((add = parseAddExp()) == null) {
            return null;
        } else {
            return new InitValAST(InitValAST.Type.EXP, add, null);
        }
    }

    /*
    * VarDecl -> 'int' VarDef { ',' VarDef } ';'
    * */
    private VarDeclAST parseVarDecl() {
        Token token = getNextToken();
        if (token == null) {
            return null;
        } else if (token.getType() != TokenType.INT) {
            return null;
        } else {
            ArrayList<VarDeclElement> asts = new ArrayList<>();
            VarDeclElement ast = parseVarDeclEle();
            if (ast == null) {
                return null;
            } else {
                asts.add(ast);
                while ((token = getNextToken()).getType() == TokenType.COMMA) {
                    ast = parseVarDeclEle();
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
    * VarEle -> VarDef | VarArray
    * */
    private VarDeclElement parseVarDeclEle() {
        Token token;
        String ident;
        if ((token = getNextToken()) == null) {
            return null;
        } else if (token.getType() != TokenType.IDENT) {
            return null;
        } else if ((token = nextToken()) != null && token.getType() == TokenType.SQUARE_L) {
            rollBack();
            return new VarDeclElement(null, parseVarArray(), true);
        } else {
            rollBack();
            return new VarDeclElement(parseVarDef(), null, false);
        }
    }

    /*
    * VarDef  -> Ident | Ident '=' InitVal
    * InitVal -> Exp
    * */
    private VarDefAST parseVarDef() {
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
     * VarArray -> Ident '[' ConstExp ']' { '[' ConstExp ']' } | Ident '[' ConstExp ']' { '[' ConstExp ']' } '=' InitVal
     * */
    private VarArrayAST parseVarArray() {
        Token token;
        String ident;
        int dim = 0;
        ArrayList<AddExpAST> lengths;
        InitValAST values;
        if ((token = getNextToken()) == null) {
            return null;
        } else if (token.getType() != TokenType.IDENT) {
            return null;
        } else {
            ident = token.getValue();
            token = nextToken();
            if (token == null) {
                return null;
            } else if (token.getType() != TokenType.SQUARE_L) {
                return null;
            } else {
                AddExpAST add;
                lengths = new ArrayList<>();
                do {
                    if ((token = getNextToken()) == null) {
                        return null;
                    } else if (token.getType() != TokenType.SQUARE_L) {
                        return null;
                    } else if ((add = parseAddExp()) == null) {
                        return null;
                    } else if ((token = getNextToken()) == null) {
                        return null;
                    } else if (token.getType() != TokenType.SQUARE_R) {
                        return null;
                    } else {
                        dim++;
                        lengths.add(add);
                    }
                } while ((token = nextToken()) != null && token.getType() == TokenType.SQUARE_L);
                if ((token = getNextToken()) == null) {
                    return new VarArrayAST(VarArrayAST.Type.UNINIT, ident, dim, lengths, null);
                } else if (token.getType() != TokenType.ASSIGN) {
                    rollBack();
                    return new VarArrayAST(VarArrayAST.Type.UNINIT, ident, dim, lengths, null);
                } else if ((values = parseInitVal()) == null) {
                    return null;
                } else {
                    return new VarArrayAST(VarArrayAST.Type.INIT, ident, dim, lengths, values);
                }
            }
        }
    }

    /*
    * Stmt -> LVal '=' Add ';'
    *      -> [Add] ';'
    *      -> Block
    *      -> 'return' Add ';'
    *      -> 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    *      -> 'while' '(' Cond ')' Stmt
    *      -> 'break' ';'
    *      -> 'continue' ';'
    * LVal -> Ident
    * Cond -> LOrExp
    * */
    private StmtAST parseStmt() {
        String ident;
        AddExpAST addExpAST;
        Token token = getNextToken();
        if (token == null) {
            return null;
        } else if (token.getType() == TokenType.IDENT) {
            ident = token.getValue();
            token = nextToken();
            if (token != null && token.getType() == TokenType.ASSIGN) {
                getNextToken();
                addExpAST = parseAddExp();
                if (addExpAST == null) {
                    return null;
                } else if ((token = getNextToken()).getType() != TokenType.SEMICOLON) {
                    return null;
                } else
                    return new StmtAST(StmtAST.Type.ASSIGN, new AssignAST(AssignAST.Type.VAR, ident, null, addExpAST), null, null, null, null, null);
            } else if (token != null && token.getType() == TokenType.SQUARE_L) {
                rollBack();
                ArrayElement elem = parseArrayElem();
                if (elem == null) {
                    return null;
                } else if ((token = getNextToken()) == null) {
                    return null;
                } else if (token.getType() != TokenType.ASSIGN) {
                    return null;
                } else if ((addExpAST = parseAddExp()) == null) {
                    return null;
                } else if ((token = getNextToken()).getType() != TokenType.SEMICOLON) {
                    return null;
                } else {
                    return new StmtAST(StmtAST.Type.ASSIGN, new AssignAST(AssignAST.Type.ARR_ELEM, null, elem, addExpAST), null, null, null, null, null);
                }
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
                    return new StmtAST(StmtAST.Type.EXP, null, null, addExpAST, null, null, null);
                }
            }
        } else if (token.getType() == TokenType.RETURN) {
            if ((token = nextToken()) == null) {
                return null;
            } else if (token.getType() == TokenType.SEMICOLON) {
                getNextToken();
                return new StmtAST(StmtAST.Type.RETURN, null, new ReturnAST(ReturnAST.Type.VOID, null), null, null, null, null);
            } else {
                addExpAST = parseAddExp();
                if (addExpAST == null) {
                    return null;
                } else if ((token = getNextToken()) != null && token.getType() == TokenType.SEMICOLON) {
                    return new StmtAST(StmtAST.Type.RETURN, null, new ReturnAST(ReturnAST.Type.INT, addExpAST), null, null, null, null);
                } else
                    return null;
            }
        } else if (token.getType() == TokenType.IF) {
            rollBack();
            IfAST if_ast = parseIf();
            if (if_ast == null) {
                return null;
            } else {
                return new StmtAST(StmtAST.Type.IF, null, null, null, if_ast, null, null);
            }
        } else if (token.getType() == TokenType.BRACE_L) {
            rollBack();
            BlockAST block = parseBlock();
            if (block == null) {
                return null;
            } else {
                return new StmtAST(StmtAST.Type.BLOCK, null, null, null, null, block, null);
            }
        } else if (token.getType() == TokenType.WHILE) {
            rollBack();
            WhileAST while_ast = parseWhile();
            if (while_ast == null) {
                return null;
            } else {
                return new StmtAST(StmtAST.Type.WHILE, null, null, null, null, null, while_ast);
            }
        } else if (token.getType() == TokenType.BREAK) {
            token = getNextToken();
            if (token == null) {
                return null;
            } else if (token.getType() != TokenType.SEMICOLON) {
                return null;
            } else {
                return new StmtAST(StmtAST.Type.BREAK, null, null, null, null, null, null);
            }
        } else if (token.getType() == TokenType.CONTINUE) {
            token = getNextToken();
            if (token == null) {
                return null;
            } else if (token.getType() != TokenType.SEMICOLON) {
                return null;
            } else {
                return new StmtAST(StmtAST.Type.CONTINUE, null, null, null, null, null, null);
            }
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
                return new StmtAST(StmtAST.Type.EXP, null, null, addExpAST, null, null, null);
            }
        }
    }

    /*
    * 'while' '(' Cond ')' Stmt
    * */
    private WhileAST parseWhile() {
        Token token = getNextToken();
        LOrExpAST cond;
        StmtAST body;
        if (token == null) {
            return null;
        } else if (token.getType() != TokenType.WHILE) {
            return null;
        } else if ((token = getNextToken()) == null) {
            return null;
        } else if (token.getType() != TokenType.PAREN_L) {
            return null;
        } else if ((cond = parseLOrExp()) == null) {
            return null;
        } else if ((token = getNextToken()) == null) {
            return null;
        } else if (token.getType() != TokenType.PAREN_R) {
            return null;
        } else if ((token = nextToken()) == null) {
            return null;
        } else if (token.getType() == TokenType.SEMICOLON) {
            token = getNextToken();
            return new WhileAST(cond, null);
        } else if ((body = parseStmt()) == null) {
            return null;
        } else {
            return new WhileAST(cond, body);
        }
    }

    /*
    * 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    * */
    private IfAST parseIf() {
        Token token = getNextToken();
        LOrExpAST or;
        StmtAST stmt_if;
        StmtAST stmt_else;
        if (token == null) {
            return null;
        } else if (token.getType() != TokenType.IF) {
            return null;
        } else if ((token = getNextToken()) == null) {
            return null;
        } else if (token.getType() != TokenType.PAREN_L) {
            return null;
        } else if ((or = parseLOrExp()) == null) {
            return null;
        } else if ((token = getNextToken()) == null) {
            return null;
        } else if (token.getType() != TokenType.PAREN_R) {
            return null;
        } else if ((stmt_if = parseStmt()) == null) {
            return null;
        } else if ((token = getNextToken()) == null) {
            return new IfAST(or, stmt_if, null);
        } else if (token.getType() != TokenType.ELSE) {
            rollBack();
            return new IfAST(or, stmt_if, null);
        } else if ((stmt_else = parseStmt()) == null) {
            return null;
        } else {
            return new IfAST(or, stmt_if, stmt_else);
        }
    }

    /*
    * Add  -> Mul RAdd
    * RAdd -> ('+'|'-') Mul RAdd
    *      -> eps
    *
    * Add  -> Mul { ('+'|'-') Mul }
    * */
    private AddExpAST parseAddExp() {
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
    private MulExpAST parseMulExp() {
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
    private UnaryExpAST parseUnaryExp() {
        Token token;
        String op_arithmetic;
        String op_logic;
        PrimaryExpAST primary;
        token = getNextToken();
        op_arithmetic = op_logic = "";
        while (token != null && (token.getType() == TokenType.ADD || token.getType() == TokenType.MIN || token.getType() == TokenType.NOT)) {
            String opp = token.getValue();
            switch (opp) {
                case "-":
                    if (op_arithmetic.equals("-"))
                        op_arithmetic = "";
                    else
                        op_arithmetic = "-";
                    break;
                case "!":
                    if (op_logic.equals("!"))
                        op_logic = "";
                    else
                        op_logic = "!";
                    break;
                default:
                    break;
            }
            token = getNextToken();
        }
        if (token == null) {
            return null;
        } else  {
            rollBack();
            primary = parsePrimaryExp();
            if (primary == null) {
                return null;
            } else {
                return new UnaryExpAST(op_arithmetic, op_logic, primary);
            }
        }
    }

    /*
    * Primary -> '(' Exp ')' | LVal | Number | FuncCall
    * LVal    -> Ident
    * */
    private PrimaryExpAST parsePrimaryExp() {
        Token token = getNextToken();
        AddExpAST add;
        String l_val;
        FuncCallAST func_call;
        ArrayElement array_elem;
        if (token == null) {
            return null;
        } else if (token.getType() == TokenType.PAREN_L) {
            add = parseAddExp();
            if (add == null) {
                return null;
            } else if ((token = getNextToken()) == null) {
                return null;
            } else if (token.getType() != TokenType.PAREN_R) {
                return null;
            } else {
                return new PrimaryExpAST(PrimaryExpAST.Type.EXP, add, null, null, null, null);
            }
        } else if (token.getType() == TokenType.IDENT) {
            l_val = token.getValue();
            token = nextToken();
            if (token != null && token.getType() == TokenType.PAREN_L) {
                rollBack();
                func_call = parseFuncCall();
                if (func_call == null) {
                    return null;
                } else {
                    return new PrimaryExpAST(PrimaryExpAST.Type.FUNC_CALL, null, null, null, func_call, null);
                }
            } else if (token != null && token.getType() == TokenType.SQUARE_L) {
                rollBack();
                array_elem = parseArrayElem();
                if (array_elem == null) {
                    return null;
                } else {
                    return new PrimaryExpAST(PrimaryExpAST.Type.ARR_ELEM, null, null, null, null, array_elem);
                }
            } else
                return new PrimaryExpAST(PrimaryExpAST.Type.LVAL, null, l_val, null, null, null);
        } else if (token.getType() == TokenType.NUMBER){
            return new PrimaryExpAST(PrimaryExpAST.Type.NUMBER, null, null, token.getValue(), null, null);
        } else {
            return null;
        }
    }

    /*
    * ArrayElem -> Ident '[' ConstExp ']' { '[' ConstExp ']' }
    * */
    private ArrayElement parseArrayElem() {
        Token token;
        String ident;
        int dim = 0;
        AddExpAST add;
        ArrayList<AddExpAST> locations = new ArrayList<>();
        ident = getNextToken().getValue();
        do {
            if ((token = getNextToken()) == null) {
                return null;
            } else if (token.getType() != TokenType.SQUARE_L) {
                return null;
            } else if ((add = parseAddExp()) == null) {
                return null;
            } else if ((token = getNextToken()) == null) {
                return null;
            } else if (token.getType() != TokenType.SQUARE_R) {
                return null;
            } else {
                dim++;
                locations.add(add);
            }
        } while ((token = nextToken()) != null && token.getType() == TokenType.SQUARE_L);
        return new ArrayElement(ident, dim, locations);
    }

    /*
    * FuncCall   -> Ident '(' [FuncRParams] ')'
    * FuncParams -> Exp { ',' Exp }
    * */
    private FuncCallAST parseFuncCall() {
        Token token = getNextToken();
        String ident;
        ArrayList<AddExpAST> params = new ArrayList<>();
        if (token == null) {
            return null;
        } else {
            ident = token.getValue();
            token = getNextToken();
            if (token.getType() != TokenType.PAREN_L) {
                return null;
            } else if ((token = nextToken()) == null) {
                return null;
            } else if (token.getType() == TokenType.PAREN_R) {
                token = getNextToken();
                return new FuncCallAST(ident, params);
            } else {
                AddExpAST param;
                do {
                    param = parseAddExp();
                    if (param == null) {
                        return null;
                    } else {
                        params.add(param);
                    }
                } while ((token = getNextToken()).getType() == TokenType.COMMA);
                if (token.getType() != TokenType.PAREN_R) {
                    return null;
                } else {
                    return new FuncCallAST(ident, params);
                }
            }
        }
    }

    /*
    * LOr -> LAnd | LOr '||' LAnd
    *
    * LOr -> LAnd { '||' LAnd }
    * */
    private LOrExpAST parseLOrExp() {
        LAndExpAST and;
        ArrayList<LAndExpAST> ands = new ArrayList<>();
        Token token;
        if ((and = parseLAndExp()) == null) {
            return null;
        } else {
            ands.add(and);
            while ((token = getNextToken()) != null && token.getType() == TokenType.OR) {
                and = parseLAndExp();
                if (and != null) {
                    ands.add(and);
                } else {
                    return null;
                }
            }
            rollBack();
            return new LOrExpAST(ands);
        }
    }

    /*
    * LAnd -> Eq | LAnd '&&' Eq
    * LAnd -> Eq { '&&' Eq }
    * */
    private LAndExpAST parseLAndExp() {
        EqExpAST eq;
        ArrayList<EqExpAST> eqs = new ArrayList<>();
        Token token;
        if ((eq = parseEqExp()) == null) {
            return null;
        } else {
            eqs.add(eq);
            while ((token = getNextToken()) != null && token.getType() == TokenType.AND) {
                eq = parseEqExp();
                if (eq != null) {
                    eqs.add(eq);
                } else {
                    return null;
                }
            }
            rollBack();
            return new LAndExpAST(eqs);
        }
    }

    /*
    * Eq -> Rel | Eq ('==' | '!=') Rel
    * Eq -> Rel { ('==' | '!=') Rel }
    * */
    private EqExpAST parseEqExp() {
        RelExpAST rel;
        EqExpAST eq;
        Token token;
        if ((rel = parseRelExp()) == null) {
            return null;
        } else if ((token = getNextToken()) == null) {
            return new EqExpAST(null, rel, null);
        } else if (token.getType() != TokenType.EQ && token.getType() != TokenType.NOT_EQ) {
            rollBack();
            return new EqExpAST(null, rel, null);
        } else if ((eq = parseEqExp()) == null) {
            return null;
        } else {
            return new EqExpAST(token.getValue(), rel, eq);
        }
    }

    /*
    * Rel -> Add | Rel ('<' | '>' | '<=' | '>=') Add
    * Rel -> Add { ('<' | '>' | '<=' | '>=') Add }
    * */
    private RelExpAST parseRelExp() {
        AddExpAST add;
        RelExpAST rel;
        Token token;
        if ((add = parseAddExp()) == null) {
            return null;
        } else if ((token = getNextToken()) == null) {
            return new RelExpAST(null, add, null);
        } else if (token.getType() != TokenType.LESS && token.getType() != TokenType.GREATER && token.getType() != TokenType.LESS_EQ && token.getType() != TokenType.GREATER_EQ) {
            rollBack();
            return new RelExpAST(null, add, null);
        } else if ((rel = parseRelExp()) == null) {
            return null;
        } else {
            return new RelExpAST(token.getValue(), add, rel);
        }
    }
}
