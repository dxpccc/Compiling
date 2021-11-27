package util.AST;

public class StmtAST {
    public enum Type {
        ASSIGN,
        EXP,
        RETURN,
        IF,
        BLOCK,
        WHILE,
        BREAK,
        CONTINUE
    }

    public final Type type;
    public final ReturnAST return_ast;
    public final AssignAST assign_ast;
    public final AddExpAST exp;
    public final IfAST if_ast;
    public final BlockAST block;
    public final WhileAST while_ast;

    public StmtAST(Type type, AssignAST assign_ast, ReturnAST return_ast, AddExpAST exp, IfAST if_ast, BlockAST block, WhileAST while_ast) {
        this.type = type;
        switch (type) {
            case ASSIGN:
                this.assign_ast = assign_ast;
                this.return_ast = null;
                this.exp = null;
                this.if_ast = null;
                this.block = null;
                this.while_ast = null;
                break;
            case RETURN:
                this.assign_ast = null;
                this.return_ast = return_ast;
                this.exp = null;
                this.if_ast = null;
                this.block = null;
                this.while_ast = null;
                break;
            case EXP:
                this.assign_ast = null;
                this.return_ast = null;
                this.exp = exp;
                this.if_ast = null;
                this.block = null;
                this.while_ast = null;
                break;
            case IF:
                this.assign_ast = null;
                this.return_ast = null;
                this.exp = null;
                this.if_ast = if_ast;
                this.block = null;
                this.while_ast = null;
                break;
            case BLOCK:
                this.assign_ast = null;
                this.return_ast = null;
                this.exp = null;
                this.if_ast = null;
                this.block = block;
                this.while_ast = null;
                break;
            case WHILE:
                this.assign_ast = null;
                this.return_ast = null;
                this.exp = null;
                this.if_ast = null;
                this.block = null;
                this.while_ast = while_ast;
                break;
            case BREAK:
            case CONTINUE:
            default:
                this.assign_ast = null;
                this.return_ast = null;
                this.exp = null;
                this.if_ast = null;
                this.block = null;
                this.while_ast = null;
                break;
        }
    }
}
