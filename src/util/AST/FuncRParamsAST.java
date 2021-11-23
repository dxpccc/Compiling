package util.AST;

import java.util.ArrayList;

public class FuncRParamsAST {
    public final ArrayList<AddExpAST> params;

    public FuncRParamsAST(ArrayList<AddExpAST> params) {
        this.params = params;
    }
}
