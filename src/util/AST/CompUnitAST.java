package util.AST;

public class CompUnitAST implements BaseAST {
    private FuncDefAST ast;

    public CompUnitAST(FuncDefAST ast) {
        this.ast = ast;
    }

    public CompUnitAST() {
        this(null);
    }

    @Override
    public String generateIR() {
        return ast.generateIR();
    }
}
