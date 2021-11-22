package util.AST;

public class ConstDefAST {
    public final String ident;
    public final AddExpAST init_val;

    public ConstDefAST(String ident, AddExpAST init_val) {
        this.ident = ident;
        this.init_val = init_val;
    }
}
