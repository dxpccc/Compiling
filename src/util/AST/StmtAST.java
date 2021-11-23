package util.AST;

public class StmtAST {
    public enum Type {
        ASSIGN,
        EXP,
        RETURN
    }

    public final Type type;
    public final ReturnAST return_ast;
    public final AssignAST assign_ast;
    public final AddExpAST exp;

    public StmtAST(Type type, AssignAST assign_ast, ReturnAST return_ast, AddExpAST exp) {
        this.type = type;
        switch (type) {
            case ASSIGN:
                this.assign_ast = assign_ast;
                this.return_ast = null;
                this.exp = null;
                break;
            case RETURN:
                this.assign_ast = null;
                this.return_ast = return_ast;
                this.exp = null;
                break;
            case EXP:
                this.assign_ast = null;
                this.return_ast = null;
                this.exp = exp;
                break;
            default:
                this.assign_ast = null;
                this.return_ast = null;
                this.exp = null;
                break;
        }
    }
}
