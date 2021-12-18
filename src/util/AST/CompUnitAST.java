package util.AST;

import java.util.ArrayList;

public class CompUnitAST {
    public final ArrayList<CompUnitElement> elems;

    public CompUnitAST(ArrayList<CompUnitElement> elems) {
        this.elems = elems;
    }
}