package util.AST;

public class CompUnitElement {
    public enum Type {
        GLOBAL,
        FUNC
    }
    public final Type type;
    public final GlobalDeclAST global;
    public final FuncDefAST func;

    public CompUnitElement(Type type, GlobalDeclAST global, FuncDefAST func) {
        this.type = type;
        if (type == Type.GLOBAL) {
            this.global = global;
            this.func = null;
        } else {
            this.global = null;
            this.func = func;
        }
    }
}
