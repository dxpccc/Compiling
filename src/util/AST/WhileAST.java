package util.AST;

public class WhileAST {
    public final LOrExpAST cond;
    public final StmtAST body;

    public WhileAST(LOrExpAST cond, StmtAST body) {
        this.cond = cond;
        this.body = body;
    }
}
