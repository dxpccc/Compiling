import front.Lexer;
import front.Parser;
import mid.IRBuilder;
import util.AST.CompUnitAST;
import util.Token;

import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {
        Lexer lexer = new Lexer("./test/test.txt");
        ArrayList<Token> res_lexer = lexer.analyse();   // 词法分析结果
        if (res_lexer == null)                                  // 词法分析出错返回-1
            System.exit(-1);

        Parser parser = new Parser(res_lexer);
        CompUnitAST res_parser = parser.analyse();
        if (res_parser == null)
            System.exit(-2);

        IRBuilder builder = new IRBuilder("./test/output.txt");
        builder.generateIR(res_parser);
    }

    /*public static void main(String[] args) {
        try {
            Reader reader = new FileReader(args[0]);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String s;
            while ((s = bufferedReader.readLine()) != null)
                System.out.println(s);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }*/
}
