package mid;

import util.AST.*;

import java.io.*;

public class IRBuilder {
    private String path;
    private static int reg_code = 0;
    private String ir = "";

    public IRBuilder(String path) {
        this.path = path;
    }

    public IRBuilder() {
        this(null);
    }

    public String getReg() {
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
            bfdWriter.write(ir);
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
        ir += "define " + "i32 " + "@" + ast.getIdent() + "()";
        visitBlock(ast.getBlock());
    }

    private void visitBlock(BlockAST ast) {
        ir += "{\n";
        visitStmt(ast.getStmt());
        ir += "}\n";
    }

    private void visitStmt(StmtAST ast) {
        if (ast.type == StmtAST.Type.RETURN)
            visitReturn(ast.getReturn());
    }

    private void visitReturn(ReturnAST ast) {
        String reg = visitAddExp(ast.getAddExp());
        ir += "\tret i32 " + reg + "\n";
    }

    private String visitAddExp(AddExpAST ast) {
        String reg, reg_l, reg_r, op;
        if (ast.op == null) {
            return visitMulExp(ast.LHS);
        } else {
            reg_l = visitMulExp(ast.LHS);
            reg_r = visitAddExp(ast.RHS);
            reg = getReg();
            op = ast.op.equals("+") ? "add" : "sub";
            ir += "\t" + reg + " = " + op + " i32 " + reg_l + ", " + reg_r + "\n";
            return reg;
        }
    }

    private String visitMulExp(MulExpAST ast) {
        String reg, reg_l, reg_r, op;
        if (ast.op == null) {
            return visitUnaryExp(ast.LHS);
        } else {
            reg_l = visitUnaryExp(ast.LHS);
            reg_r = visitMulExp(ast.RHS);
            reg = getReg();
            switch (ast.op) {
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
            ir += "\t" + reg + " = " + op + " i32 " + reg_l + ", " + reg_r + "\n";
            return reg;
        }
    }

    private String visitUnaryExp(UnaryExpAST ast) {
        String op, reg, reg_r;
        switch (ast.op) {
            case "+":
                op = "add";
                break;
            case "-":
                op = "sub";
                break;
            default:
                op = "";
                break;
        }
        reg_r = visitPrimaryExp(ast.ast);
        reg = getReg();
        ir += "\t" + reg + " = " + op + " i32 0, " + reg_r + "\n";
        return reg;
    }

    private String visitPrimaryExp(PrimaryExpAST ast) {
        String reg, reg_r;
        if (ast.type == PrimaryExpAST.Type.NUMBER) {
            reg = getReg();
            ir += "\t" + reg + " = add i32 0, " + ast.number + "\n";
            return reg;
        } else {
            assert ast.ast != null;
            reg_r = visitAddExp(ast.ast);
            reg = getReg();
            ir += "\t" + reg + " = add i32 0, " + reg_r + "\n";
            return reg;
        }
    }
}
