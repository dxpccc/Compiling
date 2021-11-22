package util.AST;

public class PrimaryExpAST {
    public enum Type {
        EXP,
        LVAL,
        NUMBER
    }

    public final Type type;
    public final AddExpAST exp;
    public final String l_val;
    public final String number;

    public PrimaryExpAST(Type type, AddExpAST exp, String l_val, String number) {
        this.type = type;
        switch (type) {
            case EXP:
                this.exp = exp;
                this.l_val = null;
                this.number = null;
                break;
            case LVAL:
                this.exp = null;
                this.l_val = l_val;
                this.number = null;
                break;
            case NUMBER:
                this.exp = null;
                this.l_val = null;
                this.number = number;
                break;
            default:
                this.exp = null;
                this.l_val = null;
                this.number = null;
                break;
        }
    }
}
