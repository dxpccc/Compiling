package util.AST;

public class StmtAST {
    public enum Type {
        RETURN
    }

    public final Type type;
    private final ReturnAST ast;

    public StmtAST(Type type, ReturnAST ast) {
        this.type = type;
        this.ast = ast;
    }

    public ReturnAST getReturn() {
        return ast;
    }
}
