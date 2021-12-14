package util.AST;

public class ConstDeclElement {
    public final boolean isArray;
    public final ConstDefAST var;
    public final ConstArrayAST array;

    public ConstDeclElement(ConstDefAST var, ConstArrayAST array, boolean isArray) {
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
