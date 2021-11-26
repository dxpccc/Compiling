package util.AST;

import java.util.ArrayList;

public class CompUnitAST {
    public final ArrayList<GlobalDeclAST> globals;
    public final FuncDefAST func_def;

    public CompUnitAST(ArrayList<GlobalDeclAST> globals, FuncDefAST func_def) {
        this.globals = globals;
        this.func_def = func_def;
    }
}
