package util.AST;

public class PrimaryExpAST {
    public enum Type {
        EXP,
        LVAL,
        NUMBER,
        FUNC_CALL,
        ARR_ELEM
    }
    public final Type type;
    public final AddExpAST exp;
    public final String l_val;
    public final String number;
    public final FuncCallAST func_call;
    public final ArrayElement array_elem;

    public PrimaryExpAST(Type type, AddExpAST exp, String l_val, String number, FuncCallAST func_call, ArrayElement array_elem) {
        this.type = type;
        switch (type) {
            case EXP:
                this.exp = exp;
                this.l_val = null;
                this.number = null;
                this.func_call = null;
                this.array_elem = null;
                break;
            case LVAL:
                this.exp = null;
                this.l_val = l_val;
                this.number = null;
                this.func_call = null;
                this.array_elem = null;
                break;
            case NUMBER:
                this.exp = null;
                this.l_val = null;
                this.number = number;
                this.func_call = null;
                this.array_elem = null;
                break;
            case FUNC_CALL:
                this.exp = null;
                this.l_val = null;
                this.number = null;
                this.func_call = func_call;
                this.array_elem = null;
                break;
            case ARR_ELEM:
                this.exp = null;
                this.l_val = null;
                this.number = null;
                this.func_call = null;
                this.array_elem = array_elem;
                break;
            default:
                this.exp = null;
                this.l_val = null;
                this.number = null;
                this.func_call = null;
                this.array_elem = null;
                break;
        }
    }
}
