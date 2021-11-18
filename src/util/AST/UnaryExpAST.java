package util.AST;

public class UnaryExpAST {
    public final String op;
    public final PrimaryExpAST ast;

    public UnaryExpAST(String op, PrimaryExpAST ast) {
        this.op = op;
        this.ast = ast;
    }
}
