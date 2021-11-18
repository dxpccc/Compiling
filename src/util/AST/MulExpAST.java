package util.AST;

public class MulExpAST {
    public final String op;
    public final UnaryExpAST LHS;
    public final MulExpAST RHS;

    public MulExpAST(String op, UnaryExpAST LHS, MulExpAST RHS) {
        this.op = op;
        this.LHS = LHS;
        this.RHS = RHS;
    }
}
