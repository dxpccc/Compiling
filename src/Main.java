import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.Key;
import java.security.SecurityPermission;
import java.util.StringTokenizer;

public class Main {
    public static boolean isStop = false;

    public static void main(String[] args) {
        File file = new File(args[0]);
        String str;
        try {
            FileReader reader = new FileReader(file);
            BufferedReader bfReader = new BufferedReader(reader);
            while ((str = bfReader.readLine()) != null) {
                print(str);
                if (isStop)
                    break;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void print(String string) {
        StringTokenizer st = new StringTokenizer(string, " =;(){}+*/<>", true);
        while (st.hasMoreTokens()) {
            String a = st.nextToken();
            if (a.equals("=") && st.hasMoreTokens()) {
                String b = st.nextToken();
                if (b.equals("=")) {
                    a = a + b;
                    match(a);
                } else {
                    match(a);
                    match(b);
                }
            }
            else
                match(a);
            if (isStop)
                break;
        }
    }

    public static void match(String string) {
        if (!string.matches("\\s+")) {
            if (string.matches("[a-z_A-Z][a-z_A-Z\\d]*")) {
                if (Keyword.isKeyword(string))
                    Keyword.print(string);
                else
                    System.out.println("Ident(" + string + ")");
            } else if (string.matches("\\d+")) {
                System.out.println("Number(" + string + ")");
            } else if (Separator.isSeparator(string)) {
                Separator.print(string);
            } else {
                System.out.println("Err");
                isStop = true;
            }
        }
    }
}
