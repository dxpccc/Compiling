package util.AST;

public class ReturnAST {
    private AddExpAST ast;

    public ReturnAST(AddExpAST ast) {
        this.ast = ast;
    }

    public AddExpAST getAddExp() {
        return ast;
    }
}
