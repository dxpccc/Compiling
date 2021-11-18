package util.AST;

public class CompUnitAST {
    private FuncDefAST ast;

    public CompUnitAST(FuncDefAST ast) {
        this.ast = ast;
    }

    public CompUnitAST() {
        this(null);
    }

    public FuncDefAST getFuncDef() {
        return ast;
    }
}
