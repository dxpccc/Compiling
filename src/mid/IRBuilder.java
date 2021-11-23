package mid;

import util.AST.*;

import java.io.*;
import java.util.HashMap;
import java.util.Stack;

class Ident {
    public enum Type {
        CONSTVAR,
        VAR_INIT,
        VAR_UNINIT,
        FUNCTION
    }
    public final String ident;
    public final Type type;
    public final String reg;

    public Ident(String ident, Type type, String reg) {
        this.ident = ident;
        this.type = type;
        this.reg = reg;
    }
}

public class IRBuilder {
    private final String path;
    private int reg_code = 0;
    private final StringBuilder ir = new StringBuilder();
    private Stack<HashMap<String, Ident>> ident_table_list = new Stack<>();

    public IRBuilder(String path) {
        this.path = path;
    }

    public IRBuilder() {
        this(null);
    }

    private String getReg() {
        return "%" + (++reg_code);
    }

    private Ident searchIdent(String ident) {
        int len = ident_table_list.size();
        Ident _ident = null;
        for (int i = len - 1; i >= 0; --i) {
            _ident = ident_table_list.elementAt(i).get(ident);
            if (_ident != null) {
                break;
            }
        }
        return _ident;
    }

/*    private Ident searchLocalIdent(String ident) {
        return ident_table_list.peek().get(ident);
    }*/

    public void generateIR(CompUnitAST ast) {
        File output = new File(path);
        Writer writer;
        BufferedWriter bfdWriter;
        try {
            writer = new FileWriter(output);
            bfdWriter = new BufferedWriter(writer);
            visitCompUnit(ast);
            bfdWriter.write(ir.toString());
            bfdWriter.close();
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void visitCompUnit(CompUnitAST ast) {
        visitFuncDef(ast.getFuncDef());
    }

    private void visitFuncDef(FuncDefAST ast) {
        ir.append("define " + "i32 " + "@").append(ast.getIdent()).append("()");
        visitBlock(ast.block);
    }

    private void visitBlock(BlockAST ast) {
        ir.append("{\n");
        ident_table_list.push(new HashMap<>());
        for (BlockItemAST item:ast.asts) {
            visitBlockItem(item);
        }
        ident_table_list.pop();
        ir.append("}\n");
    }

    private void visitBlockItem(BlockItemAST ast) {
        switch (ast.type) {
            case CONSTDECL:
                visitConstDecl(ast.const_decl);
                break;
            case VARDECL:
                visitVarDecl(ast.var_decl);
                break;
            case STMT:
                visitStmt(ast.stmt);
                break;
            default:
                break;
        }
    }

    private void visitConstDecl(ConstDeclAST ast) {
        for (ConstDefAST def : ast.asts) {
            visitConstDef(def);
        }
    }

    private void visitConstDef(ConstDefAST ast) {
        String ident = ast.ident;
        if (searchIdent(ident) != null)
            System.exit(-3);
        String reg_l = getReg();
        ir.append("\t").append(reg_l).append(" = ").append("alloca i32\n");
        String reg_r = visitAddExp(ast.init_val);
        ir.append("\tstore i32 ").append(reg_r).append(", i32* ").append(reg_l).append("\n");
        ident_table_list.peek().put(ident, new Ident(ident, Ident.Type.CONSTVAR, reg_l));
    }

    private void visitVarDecl(VarDeclAST ast) {
        for (VarDefAST def : ast.asts) {
            visitVarDef(def);
        }
    }

    private void visitVarDef(VarDefAST ast) {
        String ident = ast.ident;
        if (searchIdent(ident) != null)
            System.exit(-3);
        String reg_l = getReg();
        String reg_r;
        ir.append("\t").append(reg_l).append(" = ").append("alloca i32\n");
        if (ast.type == VarDefAST.Type.INIT) {
            reg_r = visitAddExp(ast.init_var);
            ir.append("\tstore i32 ").append(reg_r).append(", i32* ").append(reg_l).append("\n");
            ident_table_list.peek().put(ident, new Ident(ident, Ident.Type.VAR_INIT, reg_l));
        } else {
            ident_table_list.peek().put(ident, new Ident(ident, Ident.Type.VAR_UNINIT, reg_l));
        }
    }

    private void visitStmt(StmtAST ast) {
        switch (ast.type) {
            case ASSIGN:
                visitAssign(ast.assign_ast);
                break;
            case RETURN:
                visitReturn(ast.return_ast);
                break;
            case EXP:
            default:
                break;
        }
    }

    private void visitAssign(AssignAST ast) {
        String lhs = ast.ident;
        AddExpAST add = ast.exp;
        String reg_l;
        Ident ident = searchIdent(lhs);
        if (ident == null) {
            System.exit(-3);
        } else if (ident.type == Ident.Type.CONSTVAR) {
            System.exit(-3);
        } else {
            reg_l = ident.reg;
            if (reg_l == null) {
                System.exit(-3);
            } else {
                String reg_r = visitAddExp(add);
                ir.append("\tstore i32 ").append(reg_r).append(", i32* ").append(reg_l).append("\n");
            }
        }
    }

    private void visitReturn(ReturnAST ast) {
        String reg = visitAddExp(ast.exp);
        ir.append("\tret i32 ").append(reg).append("\n");
    }

    private String visitAddExp(AddExpAST ast) {
        String reg, reg_l, reg_r, op;
        AddExpAST cur_ast = ast;
        reg = visitMulExp(ast.LHS);
        while (cur_ast.RHS != null) {
            reg_l = reg;
            reg_r = visitMulExp(cur_ast.RHS.LHS);
            reg = getReg();
            op = cur_ast.op.equals("+") ? "add" : "sub";
            ir.append("\t").append(reg).append(" = ").append(op).append(" i32 ").append(reg_l).append(", ").append(reg_r).append("\n");
            cur_ast = cur_ast.RHS;
        }
        return reg;
    }

    private String visitMulExp(MulExpAST ast) {
        String reg, reg_l, reg_r, op;
        MulExpAST cur_ast = ast;
        reg = visitUnaryExp(ast.LHS);
        while (cur_ast.RHS != null) {
            reg_l = reg;
            reg_r = visitUnaryExp(ast.RHS.LHS);
            reg = getReg();
            switch (cur_ast.op) {
                case "*":
                    op = "mul";
                    break;
                case "/":
                    op = "sdiv";
                    break;
                case "%":
                    op = "srem";
                    break;
                default:
                    op = "";
                    break;
            }
            ir.append("\t").append(reg).append(" = ").append(op).append(" i32 ").append(reg_l).append(", ").append(reg_r).append("\n");
            cur_ast = cur_ast.RHS;
        }
        return reg;
    }

    private String visitUnaryExp(UnaryExpAST ast) {
        String reg, reg_r;
        reg_r = visitPrimaryExp(ast.primary);
        if (ast.op.equals("-")) {
            reg = getReg();
            ir.append("\t").append(reg).append(" = sub i32 0, ").append(reg_r).append("\n");
        } else {
            reg = reg_r;
        }
        return reg;
    }

    private String visitPrimaryExp(PrimaryExpAST ast) {
        String reg, reg_r;
        switch (ast.type) {
            case EXP:
                reg = visitAddExp(ast.exp);
                break;
            case LVAL:
                reg_r = searchIdent(ast.l_val).reg;
                if (reg_r == null) {
                    System.exit(-3);
                }
                reg = getReg();
                ir.append("\t").append(reg).append(" = load i32, i32* ").append(reg_r).append("\n");
                break;
            case NUMBER:
                reg = ast.number;
                break;
            default:
                reg = "";
                break;
        }
        return reg;
    }
}
