package util.AST;

import java.util.ArrayList;

public class ConstArrayAST {
    public final String ident;
    public final int dim;
    public final ArrayList<AddExpAST> lengths;
    public final InitValAST values;

    public ConstArrayAST(String ident, int dim, ArrayList<AddExpAST> lengths, InitValAST values) {
        this.ident = ident;
        this.dim = dim;
        this.lengths = lengths;
        this.values = values;
    }
}
