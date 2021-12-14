package util.AST;

public class VarDeclElement {
    public final boolean isArray;
    public final VarDefAST var;
    public final VarArrayAST array;

    public VarDeclElement(VarDefAST var, VarArrayAST array, boolean isArray) {
        this.isArray = isArray;
        if (!isArray) {
            this.var = var;
            this.array = null;
        } else {
            this.var = null;
            this.array = array;
        }
    }
}
