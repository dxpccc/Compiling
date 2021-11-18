package util.AST;

public class AddExpAST {
    public final String op;
    public final MulExpAST LHS;
    public final AddExpAST RHS;

    public AddExpAST(String op, MulExpAST LHS, AddExpAST RHS) {
        this.op = op;
        this.LHS = LHS;
        this.RHS = RHS;
    }
}
