import front.Lexer;
import front.Parser;
import mid.IRBuilder;
import util.Token;

import java.io.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Lexer lexer = new Lexer(args[0]);
        ArrayList<Token> res_lexer = lexer.analyse();   // 词法分析结果
        if (res_lexer == null)                                  // 词法分析出错返回-1
            System.exit(-1);
        Parser parser = new Parser();
        boolean res_syntax = parser.analyse();         // 语法分析结果
        if (!res_syntax)
            System.exit(-2);                             // 语法分析出错返回-2
        IRBuilder maker = new IRBuilder();
        String res_maker = maker.print(res_lexer);              // 生成LLVM IR文件
        if (res_maker != null) {
            writeFile(res_maker, output);
        }
    }
}
