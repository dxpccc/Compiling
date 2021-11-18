package util.AST;

public class FuncDefAST{
    private final String func_type;
    private final String ident;
    private final BlockAST ast;

    public FuncDefAST(String func_type, String ident, BlockAST ast) {
        this.func_type = func_type;
        this.ident = ident;
        this.ast = ast;
    }

    public FuncDefAST() {
        this(null, null, null);
    }

    public String getFuncType() {
        return func_type;
    }

    public String getIdent() {
        return ident;
    }

    public BlockAST getBlock() {
        return ast;
    }
}
