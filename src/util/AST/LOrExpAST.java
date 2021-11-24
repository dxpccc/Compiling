package util.AST;

public class LOrExpAST {
    public final LAndExpAST and;
    public final LOrExpAST or;

    public LOrExpAST(LAndExpAST and, LOrExpAST or) {
        this.and = and;
        this.or = or;
    }
}
