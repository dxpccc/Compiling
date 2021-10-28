import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        // File file = new File(args[0]);
        File file = new File("test.txt");
        ArrayList<String> strings = readFile(file);
        ArrayList<String> res_lexer = Lexer.analyse(strings);
        if (res_lexer == null)
            System.exit(-1);
        // System.out.println(res_lexer);
        Syntax syntax = new Syntax();
        // System.out.println(syntax.analyse(res_lexer));
        boolean res_syntax = syntax.analyse(res_lexer);
        if (!res_syntax)
            System.exit(-2);
        LLVMIRMaker maker = new LLVMIRMaker();
        maker.print(res_lexer);
    }

    public static ArrayList<String> readFile(File file) {
        ArrayList<String> strings = new ArrayList<>();
        try {
            String str;
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            while ((str = bufferedReader.readLine()) != null)
                strings.add(str);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return strings;
    }
}
