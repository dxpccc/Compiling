package util.AST;

public class UnaryExpAST implements BaseAST {
    private String op;
    private BaseAST ast;

    public UnaryExpAST(String op, BaseAST ast) {
        this.op = op;
        this.ast = ast;
    }

    public UnaryExpAST() {
        this("+", null);
    }

    @Override
    public String generateIR() {
        String last = ast.generateIR();
        char last_op = last.charAt(0);
        String res = last;
        switch (op) {
            case "+":
                res = last;
                break;
            case "-":
                if (last_op == '-')
                    res = "+" + last.substring(1);
                else if (last_op == '+')
                    res = "-" + last.substring(1);
                else if (Character.isDigit(last_op))
                    res = "-" + last;
            default:
                break;
        }
        return res;
    }
}
