package util.AST;

public class IdentityAST implements BaseAST {
    private String identity;

    public IdentityAST(String identity) {
        this.identity = identity;
    }

    public IdentityAST() {
        this(null);
    }

    @Override
    public String generateIR() {
        return identity;
    }
}
