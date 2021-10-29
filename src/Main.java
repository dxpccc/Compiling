import java.io.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        File input = new File(args[0]);
        File output = new File(args[1]);
        //File file = new File("test.txt");
        ArrayList<String> strings = readFile(input);
        // System.out.println(strings);
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
        String res_maker = maker.print(res_lexer);

        if (res_maker != null) {
            writeFile(res_maker, output);
        }
    }

    private static ArrayList<String> readFile(File file) {
        ArrayList<String> strings = new ArrayList<>();
        try {
            String str;
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            while ((str = bufferedReader.readLine()) != null)
                strings.add(str);
            bufferedReader.close();
            reader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return strings;
    }

    private static void writeFile(String string, File file) {
        try {
            FileWriter writer = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(string);
            bufferedWriter.close();
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
