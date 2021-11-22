package util.AST;

public class UnaryExpAST {
    public final String op;
    public final PrimaryExpAST primary;

    public UnaryExpAST(String op, PrimaryExpAST ast) {
        this.op = op;
        this.primary = ast;
    }
}
