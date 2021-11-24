package util.AST;

import java.util.ArrayList;

public class LAndExpAST {
    public final ArrayList<EqExpAST> eqs;

    public LAndExpAST(ArrayList<EqExpAST> eqs) {
        this.eqs = eqs;
    }
}
