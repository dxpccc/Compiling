package util.AST;

import java.util.ArrayList;

public class ArrayElement {
    public final String ident;
    public final int dim;
    public final ArrayList<AddExpAST> locations;

    public ArrayElement(String ident, int dim, ArrayList<AddExpAST> locations) {
        this.ident = ident;
        this.dim = dim;
        this.locations = locations;
    }
}
