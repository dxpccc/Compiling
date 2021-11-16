package util.AST;

public class NumberAST implements BaseAST {
    private int number;

    public NumberAST(int number) {
        this.number = number;
    }

    @Override
    public String generateIR() {
        return String.valueOf(number);
    }
}
