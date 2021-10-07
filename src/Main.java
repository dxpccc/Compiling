import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.Key;
import java.util.Calendar;
import java.util.StringTokenizer;

public class Main {
    public static boolean isStop = false;
    public static StringBuilder token = new StringBuilder();
    public static char curChar;

    public static void main(String[] args) {
        File file = new File(args[0]);
        String str;
        int index;
        try {
            FileReader reader = new FileReader(file);
            BufferedReader bfReader = new BufferedReader(reader);
            while ((str = bfReader.readLine()) != null) {
                if (!isStop)
                    print(str);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void print(String string) {
        token.delete(0, token.length());
        int len = string.length();
        for (int index = 0; index < len; ++index) {
            curChar = string.charAt(index);
            if (isNonDigit(curChar)) {
                while (isNonDigit(curChar) || isDigit(curChar)) {
                    token.append(curChar);
                    ++index;
                    if (index < len)
                        curChar = string.charAt(index);
                    else
                        break;
                }
                --index;
                String str = token.toString();
                if (Keyword.isKeyword(str))
                    Keyword.print(str);
                else
                    System.out.println("Ident(" + str + ")");
                token.delete(0, token.length());
            } else if (isDigit(curChar)) {
                while (isDigit(curChar)) {
                    token.append(curChar);
                    ++index;
                    if (index < len)
                        curChar = string.charAt(index);
                    else
                        break;
                }
                --index;
                String str = token.toString();
                System.out.println("Number(" + str + ")");
                token.delete(0, token.length());
            } else if (Separator.isSeparator(curChar)) {
                Separator.print(curChar);
            } else if (Separator.isEqual(curChar)) {
                if (index < len -1 && string.charAt(index + 1) == '=') {
                    index++;
                    System.out.println("Eq");
                } else
                    Separator.print(curChar);
            } else if (isBlank(curChar)) {
                while (isBlank(curChar)) {
                    ++index;
                    if (index < len)
                        curChar = string.charAt(index);
                    else
                        break;
                }
                --index;
            } else {
                System.out.println("Err");
                isStop = true;
                break;
            }
        }
    }

    public static boolean isNonDigit(char ch) {
        return Character.isLetter(ch) || ch == '_';
    }

    public static boolean isDigit(char ch) {
        return Character.isDigit(ch);
    }

    public static boolean isBlank(char ch) {
        return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t';
    }

    /*public static void print(String string) {
        StringTokenizer st = new StringTokenizer(string, " \n\r\t=;(){}/+*<>", true);
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
        if (!string.matches("\\s*")) {
            // 匹配标识符
            if (string.matches("[a-z_A-Z][a-z_A-Z\\d]*")) {
                // 判断是否为保留字
                if (Keyword.isKeyword(string))
                    Keyword.print(string);
                else
                    System.out.println("Ident(" + string + ")");
            } else if (string.matches("\\d+")) {
                // 匹配数字
                System.out.println("Number(" + string + ")");
            } else if (Separator.isSeparator(string)) {
                // 匹配间隔符
                Separator.print(string);
            } else {
                System.out.println("Err");
                //isStop = true;
            }
        }
    }*/
}
