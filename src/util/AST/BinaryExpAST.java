package util.AST;

public class BinaryExpAST implements BaseAST {
    private String op;
    private BaseAST LHS;
    private BaseAST RHS;

    @Override
    public String generateIR() {
        return null;
    }
}
