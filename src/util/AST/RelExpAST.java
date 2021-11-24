package util.AST;

public class RelExpAST {
    public final String op;
    public final AddExpAST add;
    public final RelExpAST rel;

    public RelExpAST(String op, AddExpAST add, RelExpAST rel) {
        this.op = op;
        this.add = add;
        this.rel = rel;
    }
}
