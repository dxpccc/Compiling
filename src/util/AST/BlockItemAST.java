package util.AST;

public class BlockItemAST {
    public enum Type {
        CONSTDECL,
        VARDECL,
        STMT
    }
    public final Type type;
    public final ConstDeclAST const_decl;
    public final VarDeclAST var_decl;
    public final StmtAST stmt;

    public BlockItemAST(Type type, ConstDeclAST const_decl, VarDeclAST var_decl, StmtAST stmt) {
        this.type = type;
        switch (type) {
            case CONSTDECL:
                this.const_decl = const_decl;
                this.var_decl = null;
                this.stmt = null;
                break;
            case VARDECL:
                this.const_decl = null;
                this.var_decl = var_decl;
                this.stmt = null;
                break;
            case STMT:
                this.const_decl = null;
                this.var_decl = null;
                this.stmt = stmt;
                break;
            default:
                this.const_decl = null;
                this.var_decl = null;
                this.stmt = null;
                break;
        }
    }
}
