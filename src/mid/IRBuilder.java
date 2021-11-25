package mid;

import util.AST.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

class Ident {
    public enum Type {
        CONSTVAR,
        VAR_INIT,
        VAR_UNINIT,
    }
    public final String ident;
    public Type type;
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
    private int label_code = 0;
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
            if (unary.op_arithmetic.equals("") && unary.op_logic.equals("") && unary.primary.type == PrimaryExpAST.Type.FUNC_CALL) {
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

    /*
    *  获取label
    * */
    private String getLabel() {
        return "l" + (++label_code);
    }

    public void generateIR(CompUnitAST ast) {
        File output = new File(path);
        Writer writer;
        BufferedWriter bfdWriter;
        try {
            writer = new FileWriter(output);
            bfdWriter = new BufferedWriter(writer);
            ir.append(visitCompUnit(ast));
            bfdWriter.write(ir.toString());
            bfdWriter.close();
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @return CompUnit的IR
     * */
    private String visitCompUnit(CompUnitAST ast) {
        return visitFuncDef(ast.getFuncDef());
    }

    /**
     * @return FuncDef的IR
     * */
    private String visitFuncDef(FuncDefAST ast) {
        StringBuilder res = new StringBuilder();
        res.append("define " + "i32 " + "@").append(ast.getIdent()).append("(){\n");
        res.append(visitBlock(ast.block));
        res.append("}\n");
        return res.toString();
    }

    /**
     * @return Block的IR
     * */
    private String visitBlock(BlockAST ast) {
        StringBuilder res = new StringBuilder();
        ident_table_list.push(new HashMap<>());
        for (BlockItemAST item:ast.asts) {
            res.append(visitBlockItem(item));
        }
        ident_table_list.pop();
        return res.toString();
    }

    /**
     * @return BlockItem的IR
     * */
    private String visitBlockItem(BlockItemAST ast) {
        String res;
        switch (ast.type) {
            case CONSTDECL:
                res = visitConstDecl(ast.const_decl);
                break;
            case VARDECL:
                res = visitVarDecl(ast.var_decl);
                break;
            case STMT:
                res = visitStmt(ast.stmt);
                break;
            default:
                res = null;
                break;
        }
        return res;
    }

    /**
     * @return ConstDecl的IR
     * */
    private String visitConstDecl(ConstDeclAST ast) {
        StringBuilder res = new StringBuilder();
        for (ConstDefAST def : ast.asts) {
            res.append(visitConstDef(def));
        }
        return res.toString();
    }

    /**
     * @return ConstDef的IR
     * */
    private String visitConstDef(ConstDefAST ast) {
        StringBuilder res = new StringBuilder();
        String ident = ast.ident;
        // 变量已存在
        if (checkExistedIdent(ident))
            System.exit(-3);
        String reg_l = getReg();

        // 添加IR
        res.append("\t").append(reg_l).append(" = ").append("alloca i32\n");

        // 用非常量赋值
        if (!checkConstInitVal(ast.init_val))
            System.exit(-3);

        // 添加IR
        StringBuilder add_code = new StringBuilder();
        String reg_r = visitAddExp(ast.init_val, add_code);
        res.append(add_code);
        // 添加IR

        // 添加IR
        ir.append("\tstore i32 ").append(reg_r).append(", i32* ").append(reg_l).append("\n");

        ident_table_list.peek().put(ident, new Ident(ident, Ident.Type.CONSTVAR, reg_l));
        return res.toString();
    }

    /**
     * @return VarDecl的IR
     * */
    private String visitVarDecl(VarDeclAST ast) {
        StringBuilder res = new StringBuilder();
        for (VarDefAST def : ast.asts) {
            res.append(visitVarDef(def));
        }
        return res.toString();
    }

    /**
     * @return VarDef的IR
     * */
    private String visitVarDef(VarDefAST ast) {
        StringBuilder res = new StringBuilder();
        String ident = ast.ident;
        // 变量已存在
        if (checkExistedIdent(ident))
            System.exit(-3);
        String reg_l = getReg();
        String reg_r;

        // 添加IR
        res.append("\t").append(reg_l).append(" = ").append("alloca i32\n");

        if (ast.type == VarDefAST.Type.INIT) {

            // 添加IR
            StringBuilder add_code = new StringBuilder();
            reg_r = visitAddExp(ast.init_var, add_code);
            res.append(add_code);
            // 添加IR

            // 添加IR
            res.append("\tstore i32 ").append(reg_r).append(", i32* ").append(reg_l).append("\n");

            ident_table_list.peek().put(ident, new Ident(ident, Ident.Type.VAR_INIT, reg_l));
        } else {
            ident_table_list.peek().put(ident, new Ident(ident, Ident.Type.VAR_UNINIT, reg_l));
        }
        return res.toString();
    }

    /**
     * @return Stmt的IR
     * */
    private String visitStmt(StmtAST ast) {
        StringBuilder res = new StringBuilder();
        switch (ast.type) {
            case ASSIGN:
                res.append(visitAssign(ast.assign_ast));
                break;
            case RETURN:
                res.append(visitReturn(ast.return_ast));
                break;
            case EXP:
                // void Func 特判
                if (isVoidFunc(ast.exp)) {
                    String func_call = visitFuncCall(ast.exp.LHS.LHS.primary.func_call, null);
                    res.append(func_call);
                }
                break;
            case BLOCK:
                res.append(visitBlock(ast.block));
                break;
            case IF:
                String next_label = getLabel();
                res.append(visitIf(ast.if_ast, next_label));
                break;
            default:
                break;
        }
        return res.toString();
    }

    /**
     * @return 赋值语句的IR
     * */
    private String visitAssign(AssignAST ast) {
        StringBuilder res = new StringBuilder();
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
                // 添加IR
                StringBuilder add_code = new StringBuilder();
                String reg_r = visitAddExp(add, add_code);
                res.append(add_code);
                // 添加IR

                // 添加IR
                res.append("\tstore i32 ").append(reg_r).append(", i32* ").append(reg_l).append("\n");

                ident.type = Ident.Type.VAR_INIT;
            }
        }
        return res.toString();
    }

    /**
     * @return 返回语句的IR
     * */
    private String visitReturn(ReturnAST ast) {
        StringBuilder res = new StringBuilder();

        // 添加IR
        StringBuilder add_code = new StringBuilder();
        String reg = visitAddExp(ast.exp, add_code);
        res.append(add_code);
        // 添加IR

        // 添加IR
        res.append("\tret i32 ").append(reg).append("\n");

        return res.toString();
    }

    /**
     * @param ast AST节点
     * @param sb 记录IR代码
     * @return 表达式最后结果的reg
     * */
    private String visitAddExp(AddExpAST ast, StringBuilder sb) {
        // 变量未定义
        if (!checkInitExp(ast))
            System.exit(-3);
        String reg, reg_l, reg_r, op;
        AddExpAST cur_ast = ast;

        // 添加IR
        StringBuilder mul_code = new StringBuilder();
        reg = visitMulExp(ast.LHS, mul_code);
        sb.append(mul_code);
        // 添加IR

        while (cur_ast.RHS != null) {
            reg_l = reg;

            // 添加IR
            mul_code = new StringBuilder();
            reg_r = visitMulExp(cur_ast.RHS.LHS, mul_code);
            sb.append(mul_code);
            // 添加IR

            reg = getReg();
            op = cur_ast.op.equals("+") ? "add" : "sub";

            // 添加IR
            sb.append("\t").append(reg).append(" = ").append(op).append(" i32 ").append(reg_l).append(", ").append(reg_r).append("\n");

            cur_ast = cur_ast.RHS;
        }
        return reg;
    }

    /**
     * @param ast AST节点
     * @param sb 记录IR代码
     * @return 表达式最后结果的reg
     * */
    private String visitMulExp(MulExpAST ast, StringBuilder sb) {
        String reg, reg_l, reg_r, op;
        MulExpAST cur_ast = ast;

        // 添加IR
        StringBuilder unary_code = new StringBuilder();
        reg = visitUnaryExp(ast.LHS, unary_code);
        sb.append(unary_code);
        // 添加IR

        while (cur_ast.RHS != null) {
            reg_l = reg;

            // 添加IR
            unary_code = new StringBuilder();
            reg_r = visitUnaryExp(cur_ast.RHS.LHS, unary_code);
            sb.append(unary_code);
            // 添加IR

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

            // 添加IR
            sb.append("\t").append(reg).append(" = ").append(op).append(" i32 ").append(reg_l).append(", ").append(reg_r).append("\n");

            cur_ast = cur_ast.RHS;
        }
        return reg;
    }

    /**
     * @param ast AST节点
     * @param sb 记录IR代码
     * @return 表达式最后结果的reg
     * */
    private String visitUnaryExp(UnaryExpAST ast, StringBuilder sb) {
        String reg, reg_r;

        // 添加IR
        StringBuilder primary_code = new StringBuilder();
        reg = reg_r = visitPrimaryExp(ast.primary, primary_code);
        sb.append(primary_code);
        // 添加IR

        if (ast.op_arithmetic.equals("-")) {
            reg = getReg();

            // 添加IR
            sb.append("\t").append(reg).append(" = sub i32 0, ").append(reg_r).append("\n");
        }
        if (ast.op_logic.equals("!")) {
            reg = getReg();

            // 添加IR
            sb.append("\t").append(reg).append(" = icmp eq i32 ").append(reg_r).append(", 0\n");

            reg_r = reg;
            reg = getReg();

            // 添加IR
            sb.append("\t").append(reg).append(" = zext i1 ").append(reg_r).append(" to i32\n");
        }
        return reg;
    }

    /**
     * @param ast AST节点
     * @param sb 记录IR代码
     * @return 表达式最后结果的reg
     * */
    private String visitPrimaryExp(PrimaryExpAST ast, StringBuilder sb) {
        String reg, reg_r;
        switch (ast.type) {
            case EXP:

                // 添加IR
                StringBuilder add_code = new StringBuilder();
                reg = visitAddExp(ast.exp, add_code);
                sb.append(add_code);
                // 添加IR

                break;
            case LVAL:
                Ident ident;
                ident = searchIdent(ast.l_val);
                if (ident == null) {
                    System.exit(-3);
                }
                reg_r = ident.reg;
                reg = getReg();

                // 添加IR
                sb.append("\t").append(reg).append(" = load i32, i32* ").append(reg_r).append("\n");

                break;
            case FUNC_CALL:
                reg = getReg();

                // 添加IR
                sb.append(visitFuncCall(ast.func_call, reg));

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

    /**
    *  @return 函数调用的IR
    * */
    private String visitFuncCall(FuncCallAST ast, String caller) {
        StringBuilder res = new StringBuilder();
        ArrayList<String> regs = new ArrayList<>();
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

        StringBuilder add_code;
        String reg;
        for (AddExpAST add : ast.params) {
            // 添加IR
            add_code = new StringBuilder();
            reg = visitAddExp(add, add_code);
            res.append(add_code);
            // 添加IR

            // 记录参数的reg
            regs.add(reg);
        }
        res.append("\t");
        if (caller != null)
            res.append(caller).append(" = ");
        res.append("call ").append(type).append(" @").append(ident).append("(");
        if (!regs.isEmpty()) {
            res.append("i32 ").append(regs.get(0));
            for (int i = 1; i < regs.size(); ++i) {
                res.append(", i32 ").append(regs.get(i));
            }
        }
        res.append(")\n");
        return res.toString();
    }

    /**
     * @param ast AST节点
     * @param next_label if后面的代码块的label
     * @return If语句的IR
     * */
    private String visitIf(IfAST ast, String next_label) {
        StringBuilder res = new StringBuilder();

        LOrExpAST or = ast.cond;
        String if_label, else_label = null;
        if_label = getLabel();
        if (ast.stmt_else != null)
            else_label = getLabel();
        int ands_len = ast.cond.ands.size();
        for (int i = 0; i < ands_len - 1; ++i) {
            LAndExpAST and = ast.cond.ands.get(i);
            ArrayList<String> code_list = generateCodeList(and, if_label);
            String next_and = getLabel();

            // 添加IR
            res.append(dealCodeList(code_list, next_and, false));
        }
        // 最后一个And单独处理
        LAndExpAST and = ast.cond.ands.get(ands_len - 1);
        ArrayList<String> code_list = generateCodeList(and, if_label);
        // String next_and = getLabel(); // 最后一个And，不需要再申请label了

        // 添加IR
        if (ast.stmt_else != null)
            res.append(dealCodeList(code_list, else_label, true));
        else
            res.append(dealCodeList(code_list, next_label, true));
        res.append("  ").append(if_label).append(":\n");
        res.append(visitStmt(ast.stmt_if));
        res.append("\tbr label %").append(next_label).append("\n");     // 执行完跳转到if外面
        if (ast.stmt_else != null) {
            res.append("  ").append(else_label).append(":\n");
            res.append(visitStmt(ast.stmt_else));
            res.append("\tbr label %").append(next_label).append("\n");     // 执行完跳转到if外面
        }
        res.append("  ").append(next_label).append(":\n");      // 添加If语句后块的label
        // 添加IR

        return res.toString();
    }

    /**
    * @param if_label If语句条件为真时需要执行代码的label
    * @return Cond中每个AndExp按照短路求值生成的IR的列表，只包含条件为真时的跳转label
    * */
    private ArrayList<String> generateCodeList(LAndExpAST and, String if_label) {
        int eqs_len = and.eqs.size();
        ArrayList<String> code_list = new ArrayList<>();

        // str格式:
        // instructions
        // br i1 %cmp_res, label %true_label
        for (int j = 0; j < eqs_len - 1; ++j) {
            EqExpAST eq = and.eqs.get(j);
            String str = visitEqExp(eq);
            str += " ,label %" + getLabel();
            code_list.add(str);
        }
        // 最后一个Eq单独处理
        EqExpAST eq = and.eqs.get(eqs_len - 1);
        String str = visitEqExp(eq);
        str += ", label %" + if_label;  // 一个And为真跳转执行stmt_if
        code_list.add(str);
        return code_list;
    }

    /**
     * 添加条件为假时的跳转label
    * @return If语句Cond中AndExp按照短路求值生成的IR
    * */
    private String dealCodeList(ArrayList<String> code_list, String next, boolean is_last) {
        StringBuilder res = new StringBuilder();
        // 处理code_list
        // 处理后格式:
        //   instructions
        //   br i1 %cmp_res, label %true_label, label %false_label
        // true_label:
        for (int k = 0, len = code_list.size(); k < len - 1; ++k) {
            String s = code_list.get(k);
            String next_eq = s.substring(s.length() - 2);
            res.append(s).append(", label %").append(next).append("\n");  // And中有0，直接跳转下一个And
            res.append("  ").append(next_eq).append(":\n");  // 下一个Eq的开头
        }
        // 最后一个后面接的是下一个And的开头，特殊处理一下
        String s = code_list.get(code_list.size() - 1);
        res.append(s).append(", label %").append(next).append("\n");
        if (!is_last)
            res.append("  ").append(next).append(":\n"); // 最后一个And不需要

        return res.toString();
    }

    /**
     * @return EqExp的IR，结果寄存器刚好在最后面，所以不用返回寄存器，可以直接返回IR
     * */
    private String visitEqExp(EqExpAST ast) {
        StringBuilder res = new StringBuilder();
        String reg, reg_l, reg_r, op, new_reg;
        EqExpAST cur_ast = ast;

        // 添加IR
        StringBuilder rel_code = new StringBuilder();
        reg = visitRelExp(ast.rel, rel_code);
        res.append(rel_code);
        // 添加IR

        while (cur_ast.eq != null) {
            reg_l = reg;

            // 添加IR
            rel_code = new StringBuilder();
            reg_r = visitRelExp(cur_ast.eq.rel, rel_code);
            res.append(rel_code);
            // 添加IR

            reg = getReg();
            op = cur_ast.op.equals("==") ? "eq" : "ne";
            res.append("\t").append(reg).append(" = icmp ").append(op).append(" i32 ").append(reg_l).append(", ").append(reg_r).append("\n");
            new_reg = getReg();
            res.append("\t").append(new_reg).append(" = zext i1 ").append(reg).append(" to i32\n");
            reg = new_reg;
            cur_ast = cur_ast.eq;
        }
        // str格式:
        // instructions
        // br i1 %cmp_res
        reg_l = reg;
        reg = getReg();
        res.append("\t").append(reg).append(" = icmp ne i32 ").append(reg_l).append(", 0\n");
        res.append("\tbr i1 ").append(reg);
        return res.toString();
    }

    /**
     * @param ast AST节点
     * @param sb 记录IR代码
     * @return 表达式最后结果的reg
     * */
    private String visitRelExp(RelExpAST ast, StringBuilder sb) {
        String reg, reg_l, reg_r, op, new_reg;
        RelExpAST cur_ast = ast;

        // 添加IR
        StringBuilder add_code = new StringBuilder();
        reg = visitAddExp(ast.add, add_code);
        sb.append(add_code);
        // 添加IR

        while (cur_ast.rel != null) {
            reg_l = reg;

            // 添加IR
            add_code = new StringBuilder();
            reg_r = visitAddExp(cur_ast.rel.add, add_code);
            sb.append(add_code);
            // 添加IR

            reg = getReg();
            op = cur_ast.op.equals("==") ? "eq" : "ne";
            switch (cur_ast.op) {
                case ">":
                    op = "sgt";
                    break;
                case "<":
                    op = "slt";
                    break;
                case ">=":
                    op = "sge";
                    break;
                case "<=":
                    op = "sle";
                    break;
                default:
                    break;
            }

            // 添加IR
            sb.append("\t").append(reg).append(" = icmp ").append(op).append(" i32 ").append(reg_l).append(", ").append(reg_r).append("\n");

            new_reg = getReg();

            // 添加IR
            sb.append("\t").append(new_reg).append(" = zext i1 ").append(reg).append(" to i32\n");

            reg = new_reg;
            cur_ast = cur_ast.rel;
        }
        return reg;
    }
}
