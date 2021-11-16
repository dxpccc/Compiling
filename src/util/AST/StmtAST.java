package util.AST;

public class StmtAST implements BaseAST {
    private final String number;

    public StmtAST(String number) {
        this.number = number;
    }

    public StmtAST() {
        this(null);
    }

    @Override
    public String generateIR() {
        return "\tret " + "i32 " + number + ";\n";
    }
}
