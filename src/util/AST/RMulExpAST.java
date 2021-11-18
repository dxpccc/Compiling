package util.AST;

public class RMulExpAST implements BaseAST {
    private String op;
    private UnaryExpAST unary;
    private RMulExpAST r_mul;

    public RMulExpAST(String op, UnaryExpAST unary, RMulExpAST r_mul) {
        this.op = op;
        this.unary = unary;
        this.r_mul = r_mul;
    }

    public RMulExpAST() {
        this(null, null, null);
    }

    @Override
    public String generateIR() {
        return null;
    }
}
