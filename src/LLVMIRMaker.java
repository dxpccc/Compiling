import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class LLVMIRMaker {
    private HashMap<Integer, String> vocabulary = new HashMap<>();

    private void initVocabulary() {
        vocabulary.put(2, "Number");
        vocabulary.put(3, "Boolean");
        vocabulary.put(4, "String");
        vocabulary.put(5, "int");
        vocabulary.put(6, "main");
        vocabulary.put(7, "return");
        vocabulary.put(8, "(");
        vocabulary.put(9, ")");
        vocabulary.put(10, "{");
        vocabulary.put(11, "}");
        vocabulary.put(12, ";");
    }

    public String print(ArrayList<String> input) {
        if (input != null) {
            StringBuilder res = new StringBuilder();
            res.append("define dso_local i32 @main(){ret i32 ");
            for (String s : input) {
                String[] strings = s.split("\\s+");
                if (strings[0].equals("2"))
                    res.append(numFormat(strings[1]));
            }
            res.append("}");
            return res.toString();
        } else
            return null;
    }

    private String numFormat(String string) {
        String res = null;
        if (string.matches("0[0-7]+")) {
            res = String.valueOf(Integer.parseInt(string, 8));
        } else if (string.matches("(0x|0X)[0-9a-fA-F]+")) {
            res = String.valueOf(Integer.parseInt(string.substring(2), 16));
        } else
            res = string;
        return res;
    }
}
