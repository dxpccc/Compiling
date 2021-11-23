package util.AST;

import java.util.ArrayList;

public class FuncCallAST {
    public final String ident;
    public final ArrayList<AddExpAST> params;

    public FuncCallAST(String ident, ArrayList<AddExpAST> params) {
        this.ident = ident;
        this.params = params;
    }
}
