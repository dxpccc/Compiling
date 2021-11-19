package mid;

import util.AST.*;

import java.io.*;

public class IRBuilder {
    private String path;
    private static int reg_code = 0;
    private StringBuilder ir = new StringBuilder();

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
        visitBlock(ast.getBlock());
    }

    private void visitBlock(BlockAST ast) {
        ir.append("{\n");
        visitStmt(ast.getStmt());
        ir.append("}\n");
    }

    private void visitStmt(StmtAST ast) {
        if (ast.type == StmtAST.Type.RETURN)
            visitReturn(ast.getReturn());
    }

    private void visitReturn(ReturnAST ast) {
        String reg = visitAddExp(ast.getAddExp());
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
        ir.append("\t").append(reg).append(" = ").append(op).append(" i32 0, ").append(reg_r).append("\n");
        return reg;
    }

    private String visitPrimaryExp(PrimaryExpAST ast) {
        String reg, reg_r;
        if (ast.type == PrimaryExpAST.Type.NUMBER) {
            reg = getReg();
            ir.append("\t").append(reg).append(" = add i32 0, ").append(ast.number).append("\n");
        } else {
            assert ast.ast != null;
            reg_r = visitAddExp(ast.ast);
            reg = getReg();
            ir.append("\t").append(reg).append(" = add i32 0, ").append(reg_r).append("\n");
        }
        return reg;
    }
}
