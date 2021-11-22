package util.AST;

import java.util.ArrayList;

public class ConstDeclAST {
    public final ArrayList<ConstDefAST> asts;

    public ConstDeclAST(ArrayList<ConstDefAST> asts) {
        this.asts = asts;
    }
}
