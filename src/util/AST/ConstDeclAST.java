package util.AST;

import java.util.ArrayList;

public class ConstDeclAST {
    public final ArrayList<ConstDeclElement> asts;

    public ConstDeclAST(ArrayList<ConstDeclElement> asts) {
        this.asts = asts;
    }
}
