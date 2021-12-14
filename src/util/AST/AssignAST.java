package util.AST;

public class AssignAST {
    public enum Type {
        VAR,
        ARR_ELEM
    }
    public final Type type;
    public final String ident;
    public final ArrayElement array_elem;
    public final AddExpAST exp;

    public AssignAST(Type type, String ident, ArrayElement array_elem, AddExpAST exp) {
        this.type = type;
        this.exp = exp;
        switch (type) {
            case VAR:
                this.ident = ident;
                this.array_elem = null;
                break;
            case ARR_ELEM:
                this.ident = null;
                this.array_elem = array_elem;
                break;
            default:
                this.ident = null;
                this.array_elem = null;
                break;
        }
    }
}
