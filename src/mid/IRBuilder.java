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

class Func {
    public enum Type {
        INT,
        VOID
    }
    public final Type type;
    public final String ident;

    public Func(Type type, String ident) {
        this.type = type;
        this.ident = ident;
    }
}

public class IRBuilder {
    private final String path;
    private int reg_code = 0;
    private final StringBuilder ir = new StringBuilder();
    private Stack<HashMap<String, Ident>> ident_table_list = new Stack<>();
    private HashMap<String, Func> function_table = new HashMap<>();

    public IRBuilder(String path) {
        this.path = path;
        ir.append("declare i32 @getint()\n");
        ir.append("declare i32 @getch()\n");
        ir.append("declare void @putint(i32)\n");
        ir.append("declare void @putch(i32)\n");
        function_table.put("getint", new Func(Func.Type.INT, "getint"));
        function_table.put("getch", new Func(Func.Type.INT, "getch"));
        function_table.put("putint", new Func(Func.Type.VOID, "putint"));
        function_table.put("putch", new Func(Func.Type.VOID, "putch"));
    }

    public IRBuilder() {
        this(null);
    }

    /* *********** 常量类型检查 ************* */
    private boolean checkConstInitVal(AddExpAST ast) {
        if (ast.RHS == null) {
            return checkConstMulExp(ast.LHS);
        } else {
            return checkConstMulExp(ast.LHS) && checkConstInitVal(ast.RHS);
        }
    }

    private boolean checkConstMulExp(MulExpAST ast) {
        if (ast.RHS == null) {
            return checkConstUnaryExp(ast.LHS);
        } else {
            return checkConstUnaryExp(ast.LHS) && checkConstMulExp(ast.RHS);
        }
    }

    private boolean checkConstUnaryExp(UnaryExpAST ast) {
        return checkConstPrimaryExp(ast.primary);
    }

    private boolean checkConstPrimaryExp(PrimaryExpAST ast) {
        boolean res = true;
        switch (ast.type) {
            case EXP:
                res = checkConstInitVal(ast.exp);
                break;
            case LVAL:
                Ident ident = searchIdent(ast.l_val);
                res = (ident != null && ident.type == Ident.Type.CONSTVAR);
                break;
            case FUNC_CALL:
                res = false;
                break;
            case NUMBER:
            default:
                break;
        }
        return res;
    }
    /* ************ 常量类型检查 ************* */

    /* *********** 变量初始化检查 *********** */
    private boolean checkInitExp(AddExpAST ast) {
        if (ast.RHS == null) {
            return checkInitMulExp(ast.LHS);
        } else {
            return checkInitMulExp(ast.LHS) && checkInitExp(ast.RHS);
        }
    }

    private boolean checkInitMulExp(MulExpAST ast) {
        if (ast.RHS == null) {
            return checkInitUnaryExp(ast.LHS);
        } else {
            return checkInitUnaryExp(ast.LHS) && checkInitMulExp(ast.RHS);
        }
    }

    private boolean checkInitUnaryExp(UnaryExpAST ast) {
        return checkInitPrimaryExp(ast.primary);
    }

    private boolean checkInitPrimaryExp(PrimaryExpAST ast) {
        boolean res = true;
        switch (ast.type) {
            case EXP:
                res = checkInitExp(ast.exp);
                break;
            case LVAL:
                Ident ident = searchIdent(ast.l_val);
                res = (ident != null && (ident.type == Ident.Type.VAR_INIT || ident.type == Ident.Type.CONSTVAR));
                break;
            case FUNC_CALL:
                // 返回值为void的函数不允许出现在exp中，Stmt中特判
                Func func = searchFunc(ast.func_call.ident);
                res = (func != null && func.type == Func.Type.INT);
                break;
            case NUMBER:
            default:
                break;
        }
        return res;
    }
    /* *********** 变量初始化检查 *********** */

    /* ************ 变量命名检查 ************ */
    private boolean checkExistedIdent(String ident) {
        return searchIdent(ident) != null;
    }

    private boolean checkExistedLocalIdent(String ident) {
        return searchLocalIdent(ident) != null;
    }

    private boolean checkExistedExternalIdent(String ident) {
        return searchExternIdent(ident) != null;
    }
    /* ************ 变量命名检查 ************ */

    /*
    * 判断Exp是否为单独的 void Func() 调用
    * */
    private boolean isVoidFunc(AddExpAST ast) {
        MulExpAST mul = ast.LHS;
        if (ast.RHS == null && mul.RHS == null) {
            UnaryExpAST unary = mul.LHS;
            if (unary.op.equals("") && unary.primary.type == PrimaryExpAST.Type.FUNC_CALL) {
                Func func = searchFunc(unary.primary.func_call.ident);
                return func.type == Func.Type.VOID;
            } else
                return false;
        } else
            return false;
    }

    /* **************** 搜索变量名 **************** */
    private Ident searchIdent(String ident) {
        Ident _ident = searchLocalIdent(ident);
        return _ident == null? searchExternIdent(ident) : _ident;
    }

    private Ident searchLocalIdent(String ident) {
        if (ident.isEmpty())
            return null;
        else
            return ident_table_list.peek().get(ident);
    }

    private Ident searchExternIdent(String ident) {
        int len = ident_table_list.size();
        Ident _ident = null;
        if (len < 1)
            return null;
        for (int i = len - 2; i >= 0; --i) {
            _ident = ident_table_list.elementAt(i).get(ident);
            if (_ident != null) {
                break;
            }
        }
        return _ident;
    }
    /* **************** 搜索变量名 **************** */

    /* **************** 搜索函数 **************** */
    private Func searchFunc(String ident) {
        return function_table.get(ident);
    }
    /* **************** 搜索函数 **************** */

    /* *************** 检查函数是否声明 ***************** */
    private boolean checkFunc(String ident) {
        return searchFunc(ident) != null;
    }
    /* *************** 检查函数是否声明 ***************** */

    /*
     * 获取虚拟寄存器
     * */
    private String getReg() {
        return "%" + (++reg_code);
    }

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
        // 变量已存在
        if (checkExistedIdent(ident))
            System.exit(-3);
        String reg_l = getReg();
        ir.append("\t").append(reg_l).append(" = ").append("alloca i32\n");
        // 用非常量赋值
        if (!checkConstInitVal(ast.init_val))
            System.exit(-3);
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
        // 变量已存在
        if (checkExistedIdent(ident))
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
                // void Func 特判
                if (isVoidFunc(ast.exp)) {
                    String func_call = visitFuncCall(ast.exp.LHS.LHS.primary.func_call);
                    ir.append("\t").append(func_call).append("\n");
                }
                break;
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
        // 变量未定义
        if (!checkInitExp(ast))
            System.exit(-3);
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
                Ident ident;
                ident = searchIdent(ast.l_val);
                if (ident == null) {
                    System.exit(-3);
                }
                reg_r = ident.reg;
                reg = getReg();
                ir.append("\t").append(reg).append(" = load i32, i32* ").append(reg_r).append("\n");
                break;
            case FUNC_CALL:
                reg = getReg();
                ir.append("\t").append(reg).append(" = ").append(visitFuncCall(ast.func_call)).append("\n");
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

    private String visitFuncCall(FuncCallAST ast) {
        StringBuilder res = new StringBuilder();
        String ident = ast.ident;
        Func func = searchFunc(ident);
        if (func == null) {
            System.exit(-3);
        }
        String type = "i32";
        switch (func.type) {
            case INT:
                type = "i32";
                break;
            case VOID:
                type = "void";
                break;
            default:
                break;
        }
        res.append("call ").append(type).append("@").append(ident).append("(");
        for (AddExpAST add : ast.params) {
            res.append("i32 ").append(visitAddExp(add));
        }
        res.append(")");
        return res.toString();
    }
}
