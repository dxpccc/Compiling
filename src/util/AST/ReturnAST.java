package util.AST;

public class ReturnAST {
    public enum Type {
        INT,
        VOID
    }
    public final Type type;
    public final AddExpAST exp;

    public ReturnAST(Type type, AddExpAST exp) {
        this.type = type;
        this.exp = type == Type.INT ? exp : null;
    }
}
