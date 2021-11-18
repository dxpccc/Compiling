package util.AST;

public class AddExpAST implements BaseAST {
    private MulExpAST mul_ast;
    private RAddExpAST r_add_ast;

    public AddExpAST(MulExpAST mul_ast, RAddExpAST r_add_ast) {
        this.mul_ast = mul_ast;
        this.r_add_ast = r_add_ast;
    }

    public AddExpAST() {
        this(null, null);
    }

    @Override
    public String generateIR() {
        return null;
    }
}
