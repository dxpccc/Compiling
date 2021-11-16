package util.AST;

public class NumberAST implements BaseAST {
    private String number;

    public NumberAST(String number) {
        this.number = number;
    }

    public NumberAST() {
        this(null);
    }

    @Override
    public String generateIR() {
        return number;
    }
}
