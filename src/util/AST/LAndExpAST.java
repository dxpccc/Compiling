package util.AST;

public class LAndExpAST {
    public final EqExpAST eq;
    public final LAndExpAST and;

    public LAndExpAST(EqExpAST eq, LAndExpAST and) {
        this.eq = eq;
        this.and = and;
    }
}
