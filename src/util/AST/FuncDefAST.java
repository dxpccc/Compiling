package util.AST;

public class FuncDefAST{
    public final String func_type;
    public final String ident;
    public final BlockAST block;

    public FuncDefAST(String func_type, String ident, BlockAST block) {
        this.func_type = func_type;
        this.ident = ident;
        this.block = block;
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
}
