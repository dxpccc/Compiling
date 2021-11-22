package util.AST;

public class AssignAST {
    public final String ident;
    public final AddExpAST exp;

    public AssignAST(String ident, AddExpAST exp) {
        this.ident = ident;
        this.exp = exp;
    }
}
