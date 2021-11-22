package util.AST;

import java.util.ArrayList;

public class VarDeclAST {
    public final ArrayList<VarDefAST> asts;

    public VarDeclAST(ArrayList<VarDefAST> asts) {
        this.asts = asts;
    }
}
