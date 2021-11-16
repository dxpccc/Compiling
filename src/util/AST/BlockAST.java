package util.AST;

public class BlockAST implements BaseAST {
    private StmtAST ast;

    public BlockAST(StmtAST ast) {
        this.ast = ast;
    }

    @Override
    public String generateIR() {
        return "{\n" + ast.generateIR() + "}\n";
    }
}
