package util.AST;

public class VarDefAST {
    public enum Type {
        INIT,
        UNINIT
    }
    public final Type type;
    public final String ident;
    public final AddExpAST init_var;

    public VarDefAST(Type type, String ident, AddExpAST init_var) {
        this.type = type;
        this.ident = ident;
        this.init_var = type == Type.INIT ? init_var : null;
    }
}
