import java.util.ArrayList;

/*
* 词法分析程序
* */
public class Lexer {
    private static ArrayList<String> output = new ArrayList<>();

    private static boolean isStop = false;
    private static StringBuilder token = new StringBuilder();

    public static ArrayList<String> analyse(ArrayList<String> input) {
        int len = input.size();
        for (int i = 0; i < len && !isStop; ++i)
            getWord(input.get(i));
        if(isStop)
            output = null;
        return output;
    }

    private static void getWord(String string) {
        token.delete(0, token.length());
        int len = string.length();
        for (int index = 0; index < len; ++index) {
            char curChar = string.charAt(index);
            if (isDigit(curChar)) {
                if (curChar == '0') {
                    if (index < len - 2
                            && (string.charAt(index + 1) == 'x' || string.charAt(index + 1) == 'X')
                            && isHexDigit(string.charAt(index + 2))) {
                        token.append(curChar);
                        curChar = string.charAt(++index);
                        token.append(curChar);
                        curChar = string.charAt(++index);
                        while (isHexDigit(curChar)) {
                            token.append(curChar);
                            ++index;
                            if (index < len)
                                curChar = string.charAt(index);
                            else
                                break;
                        }
                    } else {
                        while (isOctalDigit(curChar)) {
                            token.append(curChar);
                            ++index;
                            if (index < len)
                                curChar = string.charAt(index);
                            else
                                break;
                        }
                    }
                } else {
                    while (isDigit(curChar)) {
                        token.append(curChar);
                        ++index;
                        if (index < len)
                            curChar = string.charAt(index);
                        else
                            break;
                    }
                }
                --index;
                output.add("2 " + token.toString());
                token.delete(0, token.length());
            } else if (isLetter(curChar)) {
                while (isLetter(curChar)) {
                    token.append(curChar);
                    ++index;
                    if (index < len)
                        curChar = string.charAt(index);
                    else
                        break;
                }
                --index;
                String str = token.toString();
                switch (str) {
                    case "int":
                        output.add("5 int");
                        break;
                    case "main":
                        output.add("6 main");
                        break;
                    case "return":
                        output.add("7 return");
                        break;
                    default:
                        isStop = true;
                        break;
                }
                token.delete(0, token.length());
            } else if (isDelimiter(curChar)) {
                switch (curChar) {
                    case '(':
                        output.add("8 (");
                        break;
                    case ')':
                        output.add("9 )");
                        break;
                    case '{':
                        output.add("10 {");
                        break;
                    case '}':
                        output.add("11 }");
                        break;
                    case ';':
                        output.add("12 ;");
                        break;
                    default:
                        break;
                }
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
                isStop = true;
                break;
            }
        }
    }

    private static boolean isLetter(char ch) { return Character.isLetter(ch); }

    private static boolean isDigit(char ch) { return Character.isDigit(ch); }

    private static boolean isOctalDigit(char ch) {
        return Character.isDigit(ch) && ch < '8';
    }

    private static boolean isHexDigit(char ch) {
        return Character.isDigit(ch) || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
    }

    private static boolean isBlank(char ch) {
        return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t';
    }

    private static boolean isDelimiter(char ch) {
        return ch == '(' || ch == ')' || ch == '{' || ch == '}' || ch == ';';
    }
}
