package util.AST;

import java.util.ArrayList;

public class FuncParam {
    public final String ident;
    public final int dim;
    public final ArrayList<AddExpAST> lengths;

    public FuncParam(String ident, int dim, ArrayList<AddExpAST> lengths) {
        this.ident = ident;
        this.dim = dim;
        this.lengths = lengths;
    }
}
