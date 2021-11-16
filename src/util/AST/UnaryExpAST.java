package util.AST;

public class UnaryExpAST implements BaseAST {
    private String op;
    private BaseAST ast;

    public UnaryExpAST(String op, BaseAST ast) {
        this.op = op;
        this.ast = ast;
    }

    public UnaryExpAST() {
        this(null, null);
    }

    @Override
    public String generateIR() {
        return null;
    }
}
