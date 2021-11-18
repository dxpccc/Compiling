package util.AST;

public class BlockAST {
    private StmtAST ast;

    public BlockAST(StmtAST ast) {
        this.ast = ast;
    }

    public StmtAST getStmt() {
        return ast;
    }
}
