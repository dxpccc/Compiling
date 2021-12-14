package util.AST;

import java.util.ArrayList;

public class VarDeclAST {
    public final ArrayList<VarDeclElement> asts;

    public VarDeclAST(ArrayList<VarDeclElement> asts) {
        this.asts = asts;
    }
}
