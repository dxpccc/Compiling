package util.AST;

public class UnaryExpAST implements BaseAST {
    private String op;
    private BaseAST ast;

    public UnaryExpAST(String op, BaseAST ast) {
        this.op = op;
        this.ast = ast;
    }

    public UnaryExpAST() {
        this(null, null);
    }

    @Override
    public String generateIR() {
        String last = ast.generateIR();
        char last_op = last.charAt(0);
        String res = last;
        if (op != null && op.equals("-")) {
            if (last_op == '-')
                res = last.substring(1);
            else
                res = "-" + last;
        }
        return res;
    }
}
