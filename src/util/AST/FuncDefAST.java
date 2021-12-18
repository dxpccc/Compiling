package util.AST;

public class FuncDefAST{
    public final String func_type;
    public final String ident;
    public final FuncParams params;
    public final BlockAST block;

    public FuncDefAST(String func_type, String ident, FuncParams params, BlockAST block) {
        this.func_type = func_type;
        this.ident = ident;
        this.params = params;
        this.block = block;
    }
}
