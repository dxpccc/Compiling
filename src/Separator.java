public class Separator {
    public static boolean isSeparator(String string) {
        boolean res = false;
        if (string != null) {
            switch (string) {
                case "=":
                case ";":
                case "(":
                case ")":
                case "{":
                case "}":
                case "+":
                case "*":
                case "/":
                case "<":
                case ">":
                case "==":
                    res = true;
                    break;
                default:
                    break;
            }
        }
        return res;
    }

    public static void print(String string) {
        if (string != null) {
            switch (string) {
                case "=":
                    System.out.println("Assign");
                    break;
                case ";":
                    System.out.println("Semicolon");
                    break;
                case "(":
                    System.out.println("LPar");
                    break;
                case ")":
                    System.out.println("RPar");
                    break;
                case "{":
                    System.out.println("LBrace");
                    break;
                case "}":
                    System.out.println("RBrace");
                    break;
                case "+":
                    System.out.println("Plus");
                    break;
                case "*":
                    System.out.println("Mult");
                    break;
                case "/":
                    System.out.println("Div");
                    break;
                case "<":
                    System.out.println("Lt");
                    break;
                case ">":
                    System.out.println("Gt");
                    break;
                case "==":
                    System.out.println("Eq");
                    break;
                default:
                    break;
            }
        }
    }
}
