import front.Lexer;
import front.Parser;
import mid.IRBuilder;
import util.AST.BaseAST;
import util.AST.CompUnitAST;
import util.Token;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Lexer lexer = new Lexer(args[0]);
        ArrayList<Token> res_lexer = lexer.analyse();   // 词法分析结果
        if (res_lexer == null)                                  // 词法分析出错返回-1
            System.exit(-1);

        Parser parser = new Parser(res_lexer);
        CompUnitAST res_parser = parser.analyse();
        if (res_parser == null)
            System.exit(-2);

        IRBuilder ir = new IRBuilder(args[1]);
        ir.generateIR(res_parser);
    }
}
