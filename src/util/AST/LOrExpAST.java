package util.AST;

import java.util.ArrayList;

public class LOrExpAST {
    public final ArrayList<LAndExpAST> ands;

    public LOrExpAST(ArrayList<LAndExpAST> ands) {
        this.ands = ands;
    }
}
