package util.AST;

import java.util.ArrayList;

public class VarArrayAST {
    public enum Type {
        INIT,
        UNINIT
    }
    public final Type type;
    public final String ident;
    public final int dim;
    public final ArrayList<AddExpAST> lengths;
    public final InitValAST values;
    public VarArrayAST(Type type, String ident, int dim, ArrayList<AddExpAST> lengths, InitValAST values) {
        this.type = type;
        this.ident = ident;
        this.dim = dim;
        this.lengths = lengths;
        this.values = type == Type.INIT ? values : null;
    }
}
