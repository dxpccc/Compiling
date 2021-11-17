package util.AST;

public class StmtAST implements BaseAST {
    private final UnaryExpAST ast;

    public StmtAST(UnaryExpAST ast) {
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
