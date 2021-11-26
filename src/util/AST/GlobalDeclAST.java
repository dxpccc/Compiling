package util.AST;

public class GlobalDeclAST {
    public enum Type {
        CONST,
        VAR
    }
    public final Type type;
    public final ConstDeclAST const_decl;
    public final VarDeclAST var_decl;

    public GlobalDeclAST(Type type, ConstDeclAST const_decl, VarDeclAST var_decl) {
        this.type = type;
        if (type == Type.CONST) {
            this.const_decl = const_decl;
            this.var_decl = null;
        } else {
            this.const_decl = null;
            this.var_decl = var_decl;
        }
    }
}
