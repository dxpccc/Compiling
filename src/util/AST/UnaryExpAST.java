package util.AST;

public class UnaryExpAST {
    public final String op_arithmetic;
    public final String op_logic;
    public final PrimaryExpAST primary;

    public UnaryExpAST(String op_arithmetic, String op_logic, PrimaryExpAST primary) {
        this.op_arithmetic = op_arithmetic;
        this.op_logic = op_logic;
        this.primary = primary;
    }
}
