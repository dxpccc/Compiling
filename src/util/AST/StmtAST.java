package util.AST;

public class StmtAST implements BaseAST {
    private final AddExpAST ast;

    public StmtAST(AddExpAST ast) {
        this.ast = ast;
    }

    public StmtAST() {
        this(null);
    }

    @Override
    public String generateIR() {
        return "\tret " + "i32 " + ast.generateIR() + ";\n";
    }
}
