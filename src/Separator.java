public class Separator {
    public static boolean isSeparator(char ch) {
        boolean res = false;
            switch (ch) {
                case ';':
                case '(':
                case ')':
                case '{':
                case '}':
                case '+':
                case '*':
                case '/':
                case '<':
                case '>':
                    res = true;
                    break;
                default:
                    break;
            }
        return res;
    }

    public static boolean isEqual(char ch) {
        return ch == '=';
    }

    public static void print(char ch) {
        switch (ch) {
            case '=':
                System.out.println("Assign");
                break;
            case ';':
                System.out.println("Semicolon");
                break;
            case '(':
                System.out.println("LPar");
                break;
            case ')':
                System.out.println("RPar");
                break;
            case '{':
                System.out.println("LBrace");
                break;
            case '}':
                System.out.println("RBrace");
                break;
            case '+':
                System.out.println("Plus");
                break;
            case '*':
                System.out.println("Mult");
                break;
            case '/':
                System.out.println("Div");
                break;
            case '<':
                System.out.println("Lt");
                break;
            case '>':
                System.out.println("Gt");
                break;
            default:
                break;
        }
    }
}
