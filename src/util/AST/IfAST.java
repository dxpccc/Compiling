package util.AST;

public class IfAST {
    public final LOrExpAST cond;
    public final StmtAST stmt_if;
    public final StmtAST stmt_else;

    public IfAST(LOrExpAST cond, StmtAST stmt_if, StmtAST stmt_else) {
        this.cond = cond;
        this.stmt_if = stmt_if;
        this.stmt_else = stmt_else;
    }
}
