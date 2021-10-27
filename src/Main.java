import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        //File file = new File(args[0]);
        File file = new File("test.txt");
        ArrayList<String> strings = readFile(file);
        System.out.println(Lexer.analyse(strings));
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
