package util.AST;

import java.util.ArrayList;

public class InitValAST {
    public enum Type {
        INITVAL,
        EXP,
        EMPTY_INIT
    }
    public final Type type;
    public final AddExpAST exp;
    public final ArrayList<InitValAST> init_vals;

    public InitValAST(Type type, AddExpAST exp, ArrayList<InitValAST> init_vals) {
        this.type = type;
        switch (type) {
            case EXP:
                this.exp = exp;
                this.init_vals = null;
                break;
            case INITVAL:
                this.exp = null;
                this.init_vals = init_vals;
                break;
            case EMPTY_INIT:
            default:
                this.exp = null;
                this.init_vals = null;
                break;
        }
    }
}
