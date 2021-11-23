import java.io.*;

public class Test1 {
    public static void main(String[] args) {
        try {
            Reader reader = new FileReader(args[0]);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String s;
            while ((s = bufferedReader.readLine()) != null)
                System.out.println(s);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
