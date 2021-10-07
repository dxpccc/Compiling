public class Keyword {
    public static boolean isKeyword(String string) {
        boolean res = false;
        if (string != null) {
            switch (string) {
                case "if":
                case "else":
                case "while":
                case "break":
                case "continue":
                case "return":
                    res = true;
                    break;
                default: break;
            }
        }
        return res;
    }

    public static void print(String string) {
        if (string != null) {
            switch (string) {
                case "if":
                    System.out.println("If");
                    break;
                case "else":
                    System.out.println("Else");
                    break;
                case "while":
                    System.out.println("While");
                    break;
                case "break":
                    System.out.println("Break");
                    break;
                case "continue":
                    System.out.println("Continue");
                    break;
                case "return":
                    System.out.println("Return");
                    break;
                default: break;
            }
        }
    }
}
