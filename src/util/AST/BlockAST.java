package util.AST;

import java.util.ArrayList;

public class BlockAST {
    public final ArrayList<BlockItemAST> asts;

    public BlockAST(ArrayList<BlockItemAST> asts) {
        this.asts = asts;
    }
}
