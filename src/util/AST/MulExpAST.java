package util.AST;

public class MulExpAST implements BaseAST {
    private UnaryExpAST unary;
    private RMulExpAST r_mul;

    public MulExpAST(UnaryExpAST unary, RMulExpAST r_mul) {
        this.unary = unary;
        this.r_mul = r_mul;
    }

    public MulExpAST() {
        this(null, null);
    }

    @Override
    public String generateIR() {
        return null;
    }
}
