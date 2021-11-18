package util.AST;

public class PrimaryExpAST {
    public enum Type {
        NUMBER,
        EXP
    }

    public final Type type;
    public final String number;
    public final AddExpAST ast;

    public PrimaryExpAST(Type type, String number, AddExpAST ast) {
        this.type = type;
        if (type == Type.NUMBER) {
            this.number = number;
            this.ast = null;
        } else {
            this.number = null;
            this.ast = ast;
        }
    }
}
