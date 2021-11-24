package util.AST;

public class EqExpAST {
    public final String op;
    public final RelExpAST rel;
    public final EqExpAST eq;

    public EqExpAST(String op, RelExpAST rel, EqExpAST eq) {
        this.op = op;
        this.rel = rel;
        this.eq = eq;
    }
}
