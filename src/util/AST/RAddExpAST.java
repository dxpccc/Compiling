package util.AST;

public class RAddExpAST implements BaseAST {
    private String op;
    private UnaryExpAST unary;
    private RAddExpAST r_add;

    public RAddExpAST(String op, UnaryExpAST unary, RAddExpAST r_add) {
        this.op = op;
        this.unary = unary;
        this.r_add = r_add;
    }

    public RAddExpAST() {
        this(null, null, null);
    }

    @Override
    public String generateIR() {
        return null;
    }
}
