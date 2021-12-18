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
        GLOBAL_CONST,
        GLOBAL_VAR_INIT,
        GLOBAL_VAR_UNINIT,
        CONSTARR,
        ARR,
        GLOBAL_ARR_CONST,
        GLOBAL_ARR
    }
    public final String ident;
    public Type type;
    public String reg;
    public final String value;
    public final Array array;

    public Ident(String ident, Type type, String reg, String value, Array array) {
        this.ident = ident;
        this.type = type;
        this.reg = reg;
        switch (type) {
            case GLOBAL_CONST:
            case GLOBAL_VAR_INIT:
                this.value = value;
                this.array = null;
                break;
            case GLOBAL_VAR_UNINIT:
                this.value = "0";
                this.array = null;
                break;
            case CONSTARR:
            case ARR:
            case GLOBAL_ARR_CONST:
            case GLOBAL_ARR:
                this.value = null;
                this.array = array;
                break;
            case CONSTVAR:
            case VAR_INIT:
            case VAR_UNINIT:
            default:
                this.value = null;
                this.array = null;
                break;
        }
    }
}

class Array {
    public final int dim;
    public final int[] lengths;

    public Array(int dim, int[] lengths) {
        this.dim = dim;
        this.lengths = lengths;
    }
}

class Func {
    public enum Type {
        INT,
        VOID
    }
    public final Type type;
    public final String ident;
    public final FuncParams params;
    public final String[] params_type;

    public Func(Type type, String ident, FuncParams params, String[] params_type) {
        this.type = type;
        this.ident = ident;
        this.params = params;
        this.params_type = params_type;
    }
}

public class IRBuilder {
    private final String path;
    private int reg_code = 0;
    private int label_code = 0;
    private final StringBuilder ir = new StringBuilder();
    private Stack<HashMap<String, Ident>> ident_table_list = new Stack<>();
    private HashMap<String, Func> function_table = new HashMap<>();
    private HashMap<String, Ident> globals = new HashMap<>();
    private HashMap<String, Ident> formal_params = new HashMap<>();

    // 检查return语句
    private boolean has_return = false;
    private Func.Type func_type;

    public IRBuilder(String path) {
        this.path = path;

        ir.append("declare i32 @getint()\n");
        ir.append("declare i32 @getch()\n");
        ir.append("declare void @putint(i32)\n");
        ir.append("declare void @putch(i32)\n");
        ir.append("declare void @memset(i32*, i32, i32)\n");
        ir.append("declare i32 @getarray(i32*)\n");
        ir.append("declare void @putarray(i32, i32*)\n");

        ArrayList<FuncParam> param_list;
        FuncParams params;
        String[] params_type;

        params = new FuncParams(new ArrayList<>());
        function_table.put("getint", new Func(Func.Type.INT, "getint", params, null));

        params = new FuncParams(new ArrayList<>());
        function_table.put("getch", new Func(Func.Type.INT, "getch", params, null));

        param_list = new ArrayList<>();
        param_list.add(new FuncParam("a", 0, null));
        params = new FuncParams(param_list);
        params_type = new String[1];
        params_type[0] = "i32";
        function_table.put("putint", new Func(Func.Type.VOID, "putint", params, params_type));

        param_list = new ArrayList<>();
        param_list.add(new FuncParam("a", 0, null));
        params = new FuncParams(param_list);
        params_type = new String[1];
        params_type[0] = "i32";
        function_table.put("putch", new Func(Func.Type.VOID, "putch", params, params_type));

        param_list = new ArrayList<>();
        param_list.add(new FuncParam("a", 1, new ArrayList<>()));
        param_list.add(new FuncParam("b", 0, null));
        param_list.add(new FuncParam("c", 0, null));
        params = new FuncParams(param_list);
        params_type = new String[3];
        params_type[0] = "i32*";
        params_type[1] = "i32";
        params_type[2] = "i32";
        function_table.put("memset", new Func(Func.Type.VOID, "memset", params, params_type));

        param_list = new ArrayList<>();
        param_list.add(new FuncParam("a", 1, new ArrayList<>()));
        params = new FuncParams(param_list);
        params_type = new String[1];
        params_type[0] = "i32*";
        function_table.put("getarray", new Func(Func.Type.INT, "getarray", params, params_type));

        param_list = new ArrayList<>();
        param_list.add(new FuncParam("a", 0, null));
        param_list.add(new FuncParam("b", 1, new ArrayList<>()));
        params = new FuncParams(param_list);
        params_type = new String[2];
        params_type[0] = "i32";
        params_type[1] = "i32*";
        function_table.put("putarray", new Func(Func.Type.VOID, "putarray", params, params_type));
    }

    public IRBuilder() {
        this(null);
    }

    /* *********** 表达式检查 *********** */
    /*
    * isConst 表示是否是常量表达式检查
    * 常量表达式的项必须为常数或者已定义常量，不允许出现数组和函数调用
    * 表达式中函数调用返回值不可为void，变量必须已定义且已初始化，数组默认初始化
    * */
    private boolean checkExp(AddExpAST ast, boolean isConst) {
        if (ast.RHS == null) {
            return checkMulExp(ast.LHS, isConst);
        } else {
            return checkMulExp(ast.LHS, isConst) && checkExp(ast.RHS, isConst);
        }
    }

    private boolean checkMulExp(MulExpAST ast, boolean isConst) {
        if (ast.RHS == null) {
            return checkUnaryExp(ast.LHS, isConst);
        } else {
            return checkUnaryExp(ast.LHS, isConst) && checkMulExp(ast.RHS, isConst);
        }
    }

    private boolean checkUnaryExp(UnaryExpAST ast, boolean isConst) {
        return checkPrimaryExp(ast.primary, isConst);
    }

    private boolean checkPrimaryExp(PrimaryExpAST ast, boolean isConst) {
        boolean res = true;
        switch (ast.type) {
            case EXP:
                res = checkExp(ast.exp, isConst);
                break;
            case LVAL:
                Ident ident = searchIdent(ast.l_val);
                if (isConst)
                    res = (ident != null && (ident.type == Ident.Type.CONSTVAR || ident.type == Ident.Type.GLOBAL_CONST));
                else
                    res = !(ident == null || ident.type == Ident.Type.VAR_UNINIT);
                break;
            case FUNC_CALL:
                if (isConst)
                    res = false;
                else {
                    // 返回值为void的函数不允许出现在exp中，Stmt中特判
                    Func func = searchFunc(ast.func_call.ident);
                    res = (func != null && func.type == Func.Type.INT);
                }
                break;
            case NUMBER:
            default:
                break;
        }
        return res;
    }
    /* *********** 表达式检查 *********** */

    /* ************ 变量命名检查 ************ */
    private boolean checkIdent(String ident) {
        return checkExistedLocalIdent(ident) || checkFormalParamIdent(ident);
    }

    private boolean checkExistedLocalIdent(String ident) {
        return searchLocalIdent(ident) != null;
    }

    private boolean checkFormalParamIdent(String ident) {
        return searchFormalParamIdent(ident) != null;
    }
    /* ************ 变量命名检查 ************ */

    /* **************** 搜索变量 **************** */
    private Ident searchIdent(String ident) {
        Ident _ident;
        if ((_ident = searchFormalParamIdent(ident)) != null) {
            return _ident;
        } else if ((_ident = searchLocalIdent(ident)) != null) {
            return _ident;
        } else if ((_ident = searchExternIdent(ident)) != null) {
            return _ident;
        } else if ((_ident =searchGlobalIdent(ident)) != null) {
            return _ident;
        } else {
            return null;
        }
    }

    private Ident searchLocalIdent(String ident) {
        if (ident_table_list.isEmpty())
            return null;
        else
            return ident_table_list.peek().get(ident);
    }

    private Ident searchFormalParamIdent(String ident) {
        if (formal_params.isEmpty())
            return null;
        else
            return formal_params.get(ident);
    }

    private Ident searchGlobalIdent(String ident) {
        if (globals.isEmpty())
            return null;
        else
            return globals.get(ident);
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
    /* **************** 搜索变量 **************** */

    /* ************** 计算常量表达式的值 *************** */
    private int calculateAddExp(AddExpAST ast) {
        int res, res_l, res_r;
        String op;
        AddExpAST cur_ast = ast;
        res = calculateMulExp(ast.LHS);
        while (cur_ast.RHS != null) {
            res_l = res;
            res_r = calculateMulExp(cur_ast.RHS.LHS);
            op = cur_ast.op;
            if (op.equals("+")) {
                res = res_l + res_r;
            } else {
                res = res_l - res_r;
            }
            cur_ast = cur_ast.RHS;
        }
        return res;
    }

    private int calculateMulExp(MulExpAST ast) {
        int res, res_l, res_r;
        String op;
        MulExpAST cur_ast = ast;
        res = calculateUnaryExp(cur_ast.LHS);
        while (cur_ast.RHS != null) {
            res_l = res;
            res_r = calculateUnaryExp(cur_ast.RHS.LHS);
            op = cur_ast.op;
            switch (op) {
                case "*":
                    res = res_l * res_r;
                    break;
                case "/":
                    res = res_l / res_r;
                    break;
                case "%":
                    res = res_l % res_r;
                default:
                    break;
            }
            cur_ast = cur_ast.RHS;
        }
        return res;
    }

    private int calculateUnaryExp(UnaryExpAST ast) {
        int res = calculatePrimaryExp(ast.primary);
        if (ast.op_arithmetic.equals("-")) {
            res = -res;
        }
        return res;
    }

    private int calculatePrimaryExp(PrimaryExpAST ast) {
        int res;
        switch (ast.type) {
            case EXP:
                res = calculateAddExp(ast.exp);
                break;
            case NUMBER:
                res = Integer.parseInt(ast.number);
                break;
            case LVAL:
                Ident ident = searchIdent(ast.l_val);
                res = Integer.parseInt(ident.value);
                break;
            case FUNC_CALL:
            default:
                res = 0;
                break;
        }
        return res;
    }
    /* ************** 计算常量表达式的值 *************** */

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
     * 分配虚拟寄存器
     * */
    private String getReg() {
        return "%r" + (++reg_code);
    }

    /*
    *  分配label
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
        StringBuilder res = new StringBuilder();
        for (CompUnitElement element : ast.elems) {
            if (element.type == CompUnitElement.Type.GLOBAL)
                res.append(visitGlobalDecl(element.global));
            else
                res.append(visitFuncDef(element.func));
        }
        if (function_table.get("main") == null) {
            System.out.println("[IRBuilder] 语义错误: 没有main函数");
            System.exit(-3);
        }
        return res.toString();
    }

    private String visitGlobalDecl(GlobalDeclAST ast) {
        if (ast.type == GlobalDeclAST.Type.CONST) {
            return visitGlobalConstDecl(ast.const_decl);
        } else if (ast.type == GlobalDeclAST.Type.VAR) {
            return visitGlobalVarDecl(ast.var_decl);
        } else {
            return null;
        }
    }

    private String visitGlobalConstDecl(ConstDeclAST ast) {
        StringBuilder res = new StringBuilder();
        for (ConstDeclElement def : ast.asts) {
            if (def.isArray)
                res.append(visitGlobalConstArray(def.array));
            else
                res.append(visitGlobalConstDef(def.var));
        }
        return res.toString();
    }

    private String visitGlobalVarDecl(VarDeclAST ast) {
        StringBuilder res = new StringBuilder();
        for (VarDeclElement def : ast.asts) {
            if (def.isArray)
                res.append(visitGlobalVarArray(def.array));
            else
                res.append(visitGlobalVarDef(def.var));
        }
        return res.toString();
    }

    private String visitGlobalConstDef(ConstDefAST ast) {
        StringBuilder res = new StringBuilder();
        String ident = ast.ident;

        // 全局变量已存在
        if (checkIdent(ident)) {
            System.out.println("[IRBuilder] 语义错误: 全局常量 " + ident + " 已定义");
            System.exit(-3);
        }

        // 非常值赋值
        if (!checkExp(ast.init_val, true)) {
            System.out.println("[IRBuilder] 语义错误: 全局常量 " + ident + " 不能用变量赋值");
            System.exit(-3);
        }

        // @a = dso_local global i32 5
        String reg = "@" + ident;

        int value = calculateAddExp(ast.init_val);

        // 添加IR
        res.append(reg).append(" = dso_local global i32 ").append(value).append("\n");

        globals.put(ident, new Ident(ident, Ident.Type.GLOBAL_CONST, reg, String.valueOf(value), null));
        return res.toString();
    }

    private String visitGlobalConstArray(ConstArrayAST ast) {
        StringBuilder res = new StringBuilder();
        String ident = ast.ident;

        // 全局变量已存在
        if (checkIdent(ident)) {
            System.out.println("[IRBuilder] 语义错误: 全局常量数组 " + ident + " 已定义");
            System.exit(-3);
        }

        // 声明数组
        String reg = "@" + ident;
        int[] lengths = calculateLengths(ast.lengths);
        if (lengths == null) {
            System.out.println("[IRBuilder] 语义错误: 数组 " + ident + " 长度不能用变量赋值");
            System.exit(-3);
        }

        // @arr = dso_local global [2 x [2 x i32]] [[2 x i32] [i32 1, i32 2], [2 x i32] [i32 3, i32 0]]
        String values = visitGlobalInitVal(ast.values, ast.dim, lengths);
        res.append(reg).append(" = dso_local global ").append(values).append("\n");

        globals.put(ident, new Ident(ident, Ident.Type.GLOBAL_ARR_CONST, reg, null, new Array(ast.dim, lengths)));
        return res.toString();
    }

    private String visitGlobalInitVal(InitValAST ast, int dim, int[] lengths) {
        StringBuilder res = new StringBuilder();
        if (ast.type == InitValAST.Type.EXP) {
            if (dim != 0) {
                System.out.println("[IRBuilder] 语义错误: 数组初始化错误，维数不对应");
                System.exit(-3);
            }
            if (!checkExp(ast.exp, true)) {
                System.out.println("[IRBuilder] 语义错误: 全局数组初始化表达式错误");
                System.exit(-3);
            }
            // @arr = dso_local global [2 x [2 x i32]] [[2 x i32] [i32 1, i32 2], [2 x i32] [i32 3, i32 0]]
            int value = calculateAddExp(ast.exp);
            res.append("i32 ").append(value);
        } else if (ast.type == InitValAST.Type.INITVAL) {
            int len = ast.init_vals.size();
            res.append(generateArrayType(lengths)).append(" [");
            InitValAST child = ast.init_vals.get(0);
            // 下一维数组的长度
            int[] new_lengths = new int[dim - 1];
            if (dim - 1 >= 0)
                System.arraycopy(lengths, 1, new_lengths, 0, dim - 1);
            String str = visitGlobalInitVal(child, dim - 1, new_lengths);
            res.append(str);
            for (int i = 1; i < len; ++i) {
                child = ast.init_vals.get(i);
                // 下一维数组的长度
                new_lengths = new int[dim - 1];
                if (dim - 1 >= 0)
                    System.arraycopy(lengths, 1, new_lengths, 0, dim - 1);
                str = visitGlobalInitVal(child, dim - 1, new_lengths);
                res.append(", ").append(str);
            }
            // 未初始化部分默认初始化为0
            for (int i = len; i < lengths[0]; ++i) {
                child = new InitValAST(InitValAST.Type.EMPTY_INIT, null, null);
                new_lengths = new int[dim - 1];
                if (dim - 1 >= 0)
                    System.arraycopy(lengths, 1, new_lengths, 0, dim - 1);
                str = visitGlobalInitVal(child, dim - 1, new_lengths);
                res.append(", ").append(str);
            }
            res.append("]");
        } else {
            if (dim == 0)
                res.append("i32 0");
            else
                res.append(generateArrayType(lengths)).append(" zeroinitializer");
        }
        return res.toString();
    }

    private String visitGlobalVarDef(VarDefAST ast) {
        StringBuilder res = new StringBuilder();
        String ident = ast.ident;

        // 全局变量已存在
        if (checkIdent(ident)) {
            System.out.println("[IRBuilder] 语义错误: 全局变量 " + ident + " 已定义");
            System.exit(-3);
        }

        String reg = "@" + ident;

        if (ast.type == VarDefAST.Type.INIT) {
            // 非常值赋值
            if (!checkExp(ast.init_var, true)) {
                System.out.println("[IRBuilder] 语义错误: 全局变量 " + ident + " 不能用变量赋值");
                System.exit(-3);
            }

            int value = calculateAddExp(ast.init_var);

            // 添加IR
            res.append(reg).append(" = dso_local global i32 ").append(value).append("\n");

            globals.put(ident, new Ident(ident, Ident.Type.GLOBAL_VAR_INIT, reg, String.valueOf(value), null));
        } else {
            // 添加IR
            res.append(reg).append(" = dso_local global i32 0\n");

            globals.put(ident,  new Ident(ident, Ident.Type.GLOBAL_VAR_UNINIT, reg, "0", null));
        }

        return res.toString();
    }

    private String visitGlobalVarArray(VarArrayAST ast) {
        StringBuilder res = new StringBuilder();
        String ident = ast.ident;

        // 全局变量已存在
        if (checkIdent(ident)) {
            System.out.println("[IRBuilder] 语义错误: 全局变量 " + ident + " 已定义");
            System.exit(-3);
        }

        // 声明数组
        String reg = "@" + ident;
        int[] lengths = calculateLengths(ast.lengths);
        if (lengths == null) {
            System.out.println("[IRBuilder] 语义错误: 数组 " + ident + " 长度不能用变量赋值");
            System.exit(-3);
        }

        if (ast.type == VarArrayAST.Type.INIT) {
            // @arr = dso_local global [2 x [2 x i32]] [[2 x i32] [i32 1, i32 2], [2 x i32] [i32 3, i32 0]]
            String values = visitGlobalInitVal(ast.values, ast.dim, lengths);
            res.append(reg).append(" = dso_local global ").append(values).append("\n");

            globals.put(ident, new Ident(ident, Ident.Type.GLOBAL_ARR, reg, null, new Array(ast.dim, lengths)));
        } else {
            // 添加IR
            res.append(reg).append(" = dso_local global ").append(generateArrayType(lengths)).append(" zeroinitializer\n");

            globals.put(ident,  new Ident(ident, Ident.Type.GLOBAL_ARR, reg, null, new Array(ast.dim, lengths)));
        }

        return res.toString();
    }

    /**
     * @return FuncDef的IR
     * */
    private String visitFuncDef(FuncDefAST ast) {
        StringBuilder res = new StringBuilder();
        formal_params.clear();
        has_return = false;

        res.append("define dso_local ");
        if (ast.func_type.equals("int")) {
            func_type = Func.Type.INT;
            res.append("i32 @");
        }
        else {
            func_type = Func.Type.VOID;
            res.append("void @");
        }
        res.append(ast.ident);

        String[] params_type = new String[ast.params.params.size()];
        res.append(visitFuncParams(ast.params, params_type));

        // 声明完就要加入，否则无法递归调用
        function_table.put(ast.ident, new Func(func_type, ast.ident, ast.params, params_type));

        res.append("{\n");
        for (Ident ident : formal_params.values()) {
            if (ident.type == Ident.Type.VAR_INIT) {
                // 复制int类型形参
                String reg = getReg();
                res.append("\t").append(reg).append("= alloca i32\n");
                res.append("\t").append("store i32 ").append(ident.reg).append(", i32* ").append(reg).append("\n");
                ident.reg = reg;
            }
        }
        res.append(visitBlock(ast.block));
        if (func_type == Func.Type.VOID) {
            res.append("\tret void\n");
        }
        res.append("}\n");

        if (func_type == Func.Type.INT && !has_return) {
            System.out.println("[IRBuilder] 语义错误: 函数" + ast.ident + "没有返回值");
            System.exit(-3);
        }
        return res.toString();
    }

    /**
     * @param ast 函数形参列表
     * @return IR
     */
    private String visitFuncParams(FuncParams ast, String[] params_type) {
        StringBuilder res = new StringBuilder();
        res.append("(");

        for (int i = 0, len = ast.params.size(); i < len; ++i) {
            StringBuilder sb = new StringBuilder();
            res.append(", ").append(visitFuncParam(ast.params.get(i), sb));
            params_type[i] = sb.toString();
        }

        res.append(") ");
        if (ast.params.size() != 0)
            res.delete(1, 3);
        return res.toString();
    }

    /**
     * @param ast 函数形参
     * @return IR
     */
    private String visitFuncParam(FuncParam ast, StringBuilder sb) {
        StringBuilder res = new StringBuilder();
        String reg = getReg();
        Ident ident;
        if (ast.dim == 0) {
            ident = new Ident(ast.ident, Ident.Type.VAR_INIT, reg, null, null);
            formal_params.put(ast.ident, ident);
            res.append("i32 ").append(reg);
            sb.append("i32");
        } else if (ast.dim == 1) {
            int[] lengths = new int[1];
            lengths[0] = Integer.MAX_VALUE;
            ident = new Ident(ast.ident, Ident.Type.ARR, reg, null, new Array(1, lengths));
            formal_params.put(ast.ident, ident);
            res.append("i32* ").append(reg);
            sb.append("i32*");
        } else {
            int[] lengths = calculateLengths(ast.lengths);
            String type = generateArrayType(lengths);
            ident = new Ident(ast.ident, Ident.Type.ARR, reg, null, new Array(ast.dim, lengths));
            formal_params.put(ast.ident, ident);
            res.append(type).append("* ").append(reg);
            sb.append(type).append("*");
        }
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
        for (ConstDeclElement def : ast.asts) {
            if (def.isArray)
                res.append(visitConstArray(def.array));
            else
                res.append(visitConstDef(def.var));
        }
        return res.toString();
    }

    /**
     * @return ConstDef的IR
     * */
    private String visitConstDef(ConstDefAST ast) {
        StringBuilder res = new StringBuilder();
        String ident = ast.ident;
        // 块内局部变量已存在
        if (checkIdent(ident)) {
            System.out.println("[IRBuilder] 语义错误: " + ident + " 已定义");
            System.exit(-3);
        }

        // 用非常量赋值
        if (!checkExp(ast.init_val, true)) {
            System.out.println("[IRBuilder] 语义错误: " + ident + " 不能用变量赋值");
            System.exit(-3);
        }

        String reg_l = getReg();

        // 添加IR
        res.append("\t").append(reg_l).append(" = ").append("alloca i32\n");

        // 添加IR
        StringBuilder add_code = new StringBuilder();
        String reg_r = visitAddExp(ast.init_val, add_code, 0);
        res.append(add_code);
        // 添加IR

        // 添加IR
        res.append("\tstore i32 ").append(reg_r).append(", i32* ").append(reg_l).append("\n");

        ident_table_list.peek().put(ident, new Ident(ident, Ident.Type.CONSTVAR, reg_l, null, null));
        return res.toString();
    }

    private String visitConstArray(ConstArrayAST ast) {
        StringBuilder res = new StringBuilder();
        String ident = ast.ident;
        // 块内局部变量已存在
        if (checkIdent(ident)) {
            System.out.println("[IRBuilder] 语义错误: 常量数组" + ident + " 已定义");
            System.exit(-3);
        }

        // 声明数组
        String reg = getReg();
        int[] lengths = calculateLengths(ast.lengths);
        if (lengths == null) {
            System.out.println("[IRBuilder] 语义错误: 数组 " + ident + " 长度不能用变量赋值");
            System.exit(-3);
        }
        // 申请空间
        String arrray_type = generateArrayType(lengths);
        res.append("\t").append(reg).append(" = ").append("alloca ").append(arrray_type).append("\n");
        // 默认初始化
        String reg_init = getReg();
        res.append("\t").append(reg_init).append(" = getelementptr ").append(arrray_type).append(", ")
                .append(arrray_type).append("* ").append(reg);
        for (int i = 0; i <= ast.dim; ++i) {
            res.append(", i32 0");
        }
        res.append("\n");
        res.append(initArray(reg_init, lengths));
        // 初始化
        res.append(visitInitVal(ast.values, ast.dim, reg, lengths, true));

        // 加入符号表
        ident_table_list.peek().put(ident, new Ident(ident, Ident.Type.CONSTARR, reg, null, new Array(ast.dim, lengths)));
        return res.toString();
    }

    /**
     * @param array_lengths 数组长度表达式列表
     * @return  数组长度列表
     */
    private int[] calculateLengths(ArrayList<AddExpAST> array_lengths) {
        int[] res = new int[array_lengths.size()];
        int i = 0;
        for (AddExpAST add : array_lengths) {
            if (!checkExp(add, true)) {
                res = null;
                break;
            } else
                res[i++] = calculateAddExp(add);
        }
        return res;
    }

    /**
     * @param lengths 每一维的长度
     * @return 形如 [3 × [4 × [5 × i32]]] 的字符串
     */
    private String generateArrayType(int[] lengths) {
        StringBuilder res_l = new StringBuilder();
        StringBuilder res_r = new StringBuilder();
        if (lengths[0] != Integer.MAX_VALUE) {
            for (int length : lengths) {
                res_l.append("[").append(length).append(" x ");
                res_r.append("]");
            }
        } else {
            for (int i = 1, len = lengths.length; i < len; ++i) {
                res_l.append("[").append(lengths[i]).append(" x ");
                res_r.append("]");
            }
        }
        return res_l.toString() + "i32" + res_r.toString();
    }

    /**
     * @param start_addr 数组起始地址
     * @param lengths 数组长度
     * @return IR
     */
    private String initArray(String start_addr, int[] lengths) {
        int size = 1;
        for (int x : lengths) {
            size *= x;
        }
        return memsetZero(start_addr, size * 4);
    }

    /**
     * @param start_addr 数组起始地址
     * @param size 数组大小
     * @return IR
     */
    private String memsetZero(String start_addr, int size) {
        // call void @memset(i32* start_addr, i32 0, i32 size)
        return "\tcall void @memset(i32* " + start_addr + ", i32 0, i32 " + size + ")\n";
    }

    /**
     * @param ast 初始化值
     * @param dim 数组维数
     * @param addr 数组首地址
     * @param lengths 数组每一维长度
     * @return IR
     */
    private String visitInitVal(InitValAST ast, int dim, String addr, int[] lengths, boolean isConst) {
        StringBuilder res = new StringBuilder();
        if (ast.type == InitValAST.Type.EXP) {
            if (dim != 0) {
                System.out.println("[IRBuilder] 语义错误: 数组初始化错误，维数不对应");
                System.exit(-3);
            }
            if (isConst) {
                if (!checkExp(ast.exp, true)) {
                    System.out.println("[IRBuilder] 语义错误: 常量数组初始化表达式错误");
                    System.exit(-3);
                }
                //store i32 1, i32* %4
                int value = calculateAddExp(ast.exp);
                res.append("\t").append("store i32 ").append(value).append(", i32* ").append(addr).append("\n");
            } else {
                if (!checkExp(ast.exp, false)) {
                    System.out.println("[IRBuilder] 语义错误: 数组初始化表达式错误");
                    System.exit(-3);
                }
                String value = visitAddExp(ast.exp, res, 0);
                res.append("\t").append("store i32 ").append(value).append(", i32* ").append(addr).append("\n");
            }
        } else if (ast.type == InitValAST.Type.INITVAL) {
            int len = ast.init_vals.size();
            for (int i = 0; i < len; ++i) {
                InitValAST child = ast.init_vals.get(i);
                // 下一维数组的长度
                int[] new_lengths = new int[dim - 1];
                if (dim - 1 >= 0)
                    System.arraycopy(lengths, 1, new_lengths, 0, dim - 1);
                // 下一维数组的索引
                String[] location = new String[1];
                location[0] = "" + i;
                // 下一维数组的地址
                String new_addr = getArrayElement(addr, lengths, location, res);
                // 递归
                String str = visitInitVal(child, dim - 1, new_addr, new_lengths, isConst);
                res.append(str);
            }
        }
        return res.toString();
    }

    /**
     * @param start_addr 数组首地址
     * @param array_lengths 数组每一维长度
     * @param location 所求元素索引，可能为寄存器
     * @param sb 返回的IR
     * @return 元素的寄存器
     */
    private String getArrayElement(String start_addr, int[] array_lengths, String[] location, StringBuilder sb) {
        // %1 = getelementptr [5 x [4 x i32]], [5 x [4 x i32]]* @a, i32 0, i32 2, i32 3
        String reg = getReg();
        String array_type = generateArrayType(array_lengths);
        sb.append("\t").append(reg).append(" = getelementptr ").append(array_type)
                .append(", ").append(array_type).append("* ").append(start_addr);
        // 不是指针
        if (array_lengths[0] != Integer.MAX_VALUE) {
            sb.append(", i32 0");
        }
        for (String i : location) {
            sb.append(", i32 ").append(i);
        }
        sb.append("\n");
        return reg;
    }

    /**
     * @return VarDecl的IR
     * */
      private String visitVarDecl(VarDeclAST ast) {
        StringBuilder res = new StringBuilder();
        for (VarDeclElement def : ast.asts) {
            if (def.isArray)
                res.append(visitVarArray(def.array));
            else
                res.append(visitVarDef(def.var));
        }
        return res.toString();
    }

    /**
     * @return VarDef的IR
     * */
    private String visitVarDef(VarDefAST ast) {
        StringBuilder res = new StringBuilder();
        String ident = ast.ident;
        // 块内局部变量已存在
        if (checkIdent(ident)) {
            System.out.println("[IRBuilder] 语义错误: " + ident + " 已定义");
            System.exit(-3);
        }

        String reg_l = getReg();
        String reg_r;

        // 添加IR
        res.append("\t").append(reg_l).append(" = ").append("alloca i32\n");

        if (ast.type == VarDefAST.Type.INIT) {

            // 添加IR
            StringBuilder add_code = new StringBuilder();
            reg_r = visitAddExp(ast.init_var, add_code, 0);
            res.append(add_code);
            // 添加IR

            // 添加IR
            res.append("\tstore i32 ").append(reg_r).append(", i32* ").append(reg_l).append("\n");

            ident_table_list.peek().put(ident, new Ident(ident, Ident.Type.VAR_INIT, reg_l, null, null));
        } else {
            ident_table_list.peek().put(ident, new Ident(ident, Ident.Type.VAR_UNINIT, reg_l, null, null));
        }
        return res.toString();
    }

    private String visitVarArray(VarArrayAST ast) {
        StringBuilder res = new StringBuilder();
        String ident = ast.ident;

        // 块内局部变量已存在
        if (checkIdent(ident)) {
            System.out.println("[IRBuilder] 语义错误: " + ident + " 已定义");
            System.exit(-3);
        }

        // 声明数组
        String reg = getReg();
        int[] lengths = calculateLengths(ast.lengths);
        if (lengths == null) {
            System.out.println("[IRBuilder] 语义错误: 数组 " + ident + " 长度不能用变量赋值");
            System.exit(-3);
        }
        // 申请空间
        String arrray_type = generateArrayType(lengths);
        res.append("\t").append(reg).append(" = ").append("alloca ").append(arrray_type).append("\n");
        // 默认初始化
        String reg_init = getReg();
        res.append("\t").append(reg_init).append(" = getelementptr ").append(arrray_type).append(", ")
                .append(arrray_type).append("* ").append(reg);
        for (int i = 0; i <= ast.dim; ++i) {
            res.append(", i32 0");
        }
        res.append("\n");
        res.append(initArray(reg_init, lengths));
        if (ast.type == VarArrayAST.Type.INIT)
            res.append(visitInitVal(ast.values, ast.dim, reg, lengths, false));

        // 加入符号表
        ident_table_list.peek().put(ident, new Ident(ident, Ident.Type.ARR, reg, null, new Array(ast.dim, lengths)));
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
                res.append(visitIf(ast.if_ast, getLabel()));
                break;
            case WHILE:
                res.append(visitWhile(ast.while_ast, getLabel()));
                break;
            case BREAK:
                res.append(visitBreak());
                break;
            case CONTINUE:
                res.append(visitContinue());
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
        String lhs;
        AddExpAST add = ast.exp;
        String reg_l;
        Ident ident;
        lhs = ast.type == AssignAST.Type.VAR ? ast.ident : ast.array_elem.ident;
        ident = searchIdent(lhs);
        if (ident == null) {
            System.out.println("[IRBuilder] 语义错误: 变量 " + lhs + " 未定义");
            System.exit(-3);
        } else if (ident.type == Ident.Type.CONSTVAR
                    || ident.type == Ident.Type.GLOBAL_CONST
                    || ident.type == Ident.Type.CONSTARR
                    || ident.type == Ident.Type.GLOBAL_ARR_CONST) {
            System.out.println("[IRBuilder] 语义错误: 常量 " + lhs + " 不能被赋值");
            System.exit(-3);
        } else if (ast.type == AssignAST.Type.ARR_ELEM && ast.array_elem.dim != ident.array.dim) {
            System.out.println("[IRBuilder] 语义错误: 无法为数组赋值");
            System.exit(-3);
        } else {
            if (ast.type == AssignAST.Type.VAR)
                reg_l = ident.reg;
            else {
                String[] locations = addExpArray2StringArray(ast.array_elem.locations, res);
                reg_l = getArrayElement(ident.reg, ident.array.lengths, locations, res);
            }
            // 添加IR
            StringBuilder add_code = new StringBuilder();
            String reg_r = visitAddExp(add, add_code, 0);
            res.append(add_code);
            // 添加IR

            // 添加IR
            res.append("\tstore i32 ").append(reg_r).append(", i32* ").append(reg_l).append("\n");
            if (ast.type == AssignAST.Type.VAR)
                ident.type = Ident.Type.VAR_INIT;
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
        if (ast.type == ReturnAST.Type.INT && func_type == Func.Type.INT) {
            String reg = visitAddExp(ast.exp, add_code, 0);
            res.append(add_code);
            res.append("\tret i32 ").append(reg).append("\n");
        } else if (ast.type == ReturnAST.Type.VOID && func_type == Func.Type.VOID) {
            res.append("\tret void\n");
        } else if (ast.type == ReturnAST.Type.INT && func_type == Func.Type.VOID) {
            System.out.println("[IRBuilder] 语义错误: 应当返回void");
            System.exit(-3);
        } else {
            System.out.println("[IRBuilder] 语义错误: return语句缺少返回值");
            System.exit(-3);
        }
        has_return = true;
        return res.toString();
    }

    /**
     * @param ast AST节点
     * @param sb 记录IR代码
     * @return 表达式最后结果的reg
     * */
    private String visitAddExp(AddExpAST ast, StringBuilder sb, int dim) {
        // 变量未定义
        if (!checkExp(ast, false)) {
            System.out.println("[IRBuilder] 语义错误: 表达式中有变量未声明");
            System.exit(-3);
        }

        String reg, reg_l, reg_r, op;
        AddExpAST cur_ast = ast;

        // 添加IR
        StringBuilder mul_code = new StringBuilder();
        reg = visitMulExp(ast.LHS, mul_code, dim);
        sb.append(mul_code);
        // 添加IR

        while (cur_ast.RHS != null) {
            reg_l = reg;

            // 添加IR
            mul_code = new StringBuilder();
            reg_r = visitMulExp(cur_ast.RHS.LHS, mul_code, dim);
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
    private String visitMulExp(MulExpAST ast, StringBuilder sb, int dim) {
        String reg, reg_l, reg_r, op;
        MulExpAST cur_ast = ast;

        // 添加IR
        StringBuilder unary_code = new StringBuilder();
        reg = visitUnaryExp(ast.LHS, unary_code, dim);
        sb.append(unary_code);
        // 添加IR

        while (cur_ast.RHS != null) {
            reg_l = reg;

            // 添加IR
            unary_code = new StringBuilder();
            reg_r = visitUnaryExp(cur_ast.RHS.LHS, unary_code, dim);
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
    private String visitUnaryExp(UnaryExpAST ast, StringBuilder sb, int dim) {
        String reg, reg_r;

        // 添加IR
        StringBuilder primary_code = new StringBuilder();
        reg = reg_r = visitPrimaryExp(ast.primary, primary_code, dim);
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
    private String visitPrimaryExp(PrimaryExpAST ast, StringBuilder sb, int dim) {
        String reg, reg_r;
        Ident ident;
        switch (ast.type) {
            case EXP:

                // 添加IR
                StringBuilder add_code = new StringBuilder();
                reg = visitAddExp(ast.exp, add_code, dim);
                sb.append(add_code);
                // 添加IR

                break;
            case LVAL:
                ident = searchIdent(ast.l_val);
                if (ident == null) {
                    System.out.println("[IRBuilder] 语义错误: 变量 " + ast.l_val + " 未定义");
                    System.exit(-3);
                }
                if (ident.array != null) {
                    // 可能为数组名
                    if (ident.array.dim != dim) {
                        System.out.println("[IRBuilder] 语义错误: 数组元素 " + ast.array_elem.ident + " 维数错误");
                        System.exit(-3);
                    }
                    String[] locations = new String[1];
                    locations[0] = "0";
                    reg = getArrayElement(ident.reg, ident.array.lengths, locations, sb);
                } else {
                    reg_r = ident.reg;
                    reg = getReg();
                    sb.append("\t").append(reg).append(" = load i32, i32* ").append(reg_r).append("\n");
                }
                break;
            case FUNC_CALL:
                reg = getReg();

                // 添加IR
                sb.append(visitFuncCall(ast.func_call, reg));

                break;
            case NUMBER:
                if (dim != 0) {
                    System.out.println("[IRBuilder] 语义错误: 维数错误");
                    System.exit(-3);
                }
                reg = ast.number;
                break;
            case ARR_ELEM:
                ident = searchIdent(ast.array_elem.ident);
                if (ident == null || ident.array == null) {
                    System.out.println("[IRBuilder] 语义错误: 数组元素 " + ast.array_elem.ident + " 未定义");
                    System.exit(-3);
                }
                if (ident.array.dim != ast.array_elem.dim + dim) {
                    System.out.println("[IRBuilder] 语义错误: 数组元素 " + ast.array_elem.ident + " 维数错误");
                    System.exit(-3);
                }
                String[] locations = addExpArray2StringArray(ast.array_elem.locations, sb);
                reg_r = getArrayElement(ident.reg, ident.array.lengths, locations, sb);
                if (dim == 0) {
                    reg = getReg();
                    // 添加IR
                    sb.append("\t").append(reg).append(" = load i32, i32* ").append(reg_r).append("\n");
                } else
                    reg = reg_r;
                break;
            default:
                reg = "";
                break;
        }
        return reg;
    }

    private String[] addExpArray2StringArray(ArrayList<AddExpAST> asts, StringBuilder sb) {
        int len = asts.size();
        String[] res = new String[len];
        for (int i = 0; i < len; ++i) {
            AddExpAST add = asts.get(i);
            if (!checkExp(add, false)) {
                System.out.println("[IRBuilder] 语义错误: 数组元素索引错误");
                System.exit(-3);
            }
            res[i] = visitAddExp(add, sb, 0);
        }
        return res;
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
            System.out.println("[IRBuilder] 语义错误: 函数 " + ident + " 未声明");
            System.exit(-3);
        }
        if (func.params.params.size() != ast.params.size()) {
            System.out.println("[IRBuilder] 语义错误: 函数 " + ident + " 参数数目错误");
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
        int len = ast.params.size();
        for (int i = 0; i < len; ++i) {
            // 添加IR
            add_code = new StringBuilder();
            // 计算参数
            reg = visitAddExp(ast.params.get(i), add_code, func.params.params.get(i).dim);
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
            res.append(func.params_type[0]).append(" ").append(regs.get(0));
            for (int i = 1; i < regs.size(); ++i) {
                res.append(",").append(func.params_type[i]).append(" ").append(regs.get(i));
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
        ident_table_list.push(new HashMap<>());         // 当前block的符号表入栈
        StringBuilder res = new StringBuilder();

        LOrExpAST or = ast.cond;
        String if_label, else_label = null;
        if_label = getLabel();
        if (ast.stmt_else != null)
            else_label = getLabel();
        int ands_len = ast.cond.ands.size();
        for (int i = 0; i < ands_len - 1; ++i) {
            LAndExpAST and = ast.cond.ands.get(i);
            ArrayList<String> labels = new ArrayList<>();
            ArrayList<String> code_list = generateCodeList(and, if_label, labels);
            String next_and = getLabel();

            // 添加IR
            res.append(dealCodeList(code_list, labels, next_and, false));
        }
        // 最后一个And单独处理
        LAndExpAST and = ast.cond.ands.get(ands_len - 1);
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> code_list = generateCodeList(and, if_label, labels);
        // String next_and = getLabel(); // 最后一个And，不需要再申请label了

        // 添加IR
        if (ast.stmt_else != null)
            res.append(dealCodeList(code_list, labels, else_label, true));
        else
            res.append(dealCodeList(code_list, labels, next_label, true));
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

        ident_table_list.pop();         // 当前block的符号表出栈
        return res.toString();
    }

    /**
     * @param if_label If语句条件为真时需要执行代码的label
     * @param labels 每个块跳转的true label列表
     * @return Cond中每个AndExp按照短路求值生成的IR的列表，只包含条件为真时的跳转label
     * */
    private ArrayList<String> generateCodeList(LAndExpAST and, String if_label, ArrayList<String> labels) {
        int eqs_len = and.eqs.size();
        ArrayList<String> code_list = new ArrayList<>();

        // str格式:
        // instructions
        // br i1 %cmp_res, label %true_label
        for (int j = 0; j < eqs_len - 1; ++j) {
            EqExpAST eq = and.eqs.get(j);
            String str = visitEqExp(eq);
            String label = getLabel();
            str += " ,label %" + label;
            labels.add(label);          // 因为label长度不一样所以需要保存
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
    private String dealCodeList(ArrayList<String> code_list, ArrayList<String> labels, String next, boolean is_last) {
        StringBuilder res = new StringBuilder();
        // 处理code_list
        // 处理后格式:
        //   instructions
        //   br i1 %cmp_res, label %true_label, label %false_label
        // true_label:
        for (int k = 0, len = code_list.size(); k < len - 1; ++k) {
            String s = code_list.get(k);
            String next_eq = labels.get(k);
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
        reg = visitAddExp(ast.add, add_code, 0);
        sb.append(add_code);
        // 添加IR

        while (cur_ast.rel != null) {
            reg_l = reg;

            // 添加IR
            add_code = new StringBuilder();
            reg_r = visitAddExp(cur_ast.rel.add, add_code, 0);
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


    private Stack<String> next_labels = new Stack<>();
    private Stack<String> cond_labels = new Stack<>();
    /**
     * @return while语句的IR
     * */
    private String visitWhile(WhileAST ast, String next_label) {
        ident_table_list.push(new HashMap<>());

        StringBuilder res = new StringBuilder();

        LOrExpAST cond = ast.cond;
        StmtAST body = ast.body;
        String cond_label, body_label;
        cond_label = getLabel();
        body_label = getLabel();

        next_labels.push(next_label);
        cond_labels.push(cond_label);

        res.append("\tbr label %").append(cond_label).append("\n");
        res.append("  ").append(cond_label).append(":\n");

        int cond_len = cond.ands.size();
        for (int i = 0; i < cond_len - 1; ++i) {
            LAndExpAST and = cond.ands.get(i);
            ArrayList<String> labels = new ArrayList<>();
            ArrayList<String> code_list = generateCodeList(and, body_label, labels);
            String next_and = getLabel();

            // 添加IR
            res.append(dealCodeList(code_list, labels, next_and, false));
        }
        // 最后一个And单独处理
        LAndExpAST and = ast.cond.ands.get(cond_len - 1);
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> code_list = generateCodeList(and, body_label, labels);

        // 添加IR
        res.append(dealCodeList(code_list, labels, next_label, true));
        res.append("  ").append(body_label).append(":\n");
        if (body != null)                   // 循环体可能为空
            res.append(visitStmt(body));
        res.append("\tbr label %").append(cond_label).append("\n");     // 执行完跳转到While外面
        res.append("  ").append(next_label).append(":\n");      // 添加While语句后块的label
        // 添加IR

        cond_labels.pop();
        next_labels.pop();
        ident_table_list.pop();         // 当前block的符号表出栈
        return res.toString();
    }

    /**
     * @return break语句的IR
     * */
    private String visitBreak() {
        String next_label = next_labels.peek();
        return "\tbr label %" + next_label + "\n";
    }

    /**
     * @return continue语句的IR
     * */
    private String visitContinue() {
        String next_label = cond_labels.peek();
        return "\tbr label %" + next_label + "\n";
    }
}
