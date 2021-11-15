package front;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Parser {
    private ArrayList<String> productions = new ArrayList<>();
    private HashMap<String, Integer> terminal = new HashMap<>();
    private HashMap<String, Integer> non_terminal = new HashMap<>();

    private void init() {

    }

    private int getIndexOfT(String string) {
        return terminal.get(string);
    }

    private int getIndexOfNT(String string) {
        return non_terminal.get(string);
    }

    private String getWord(int code) {
        return vocabulary.get(code);
    }

    public boolean analyse(ArrayList<String> input) {
        boolean stop = false;
        init();
        ArrayList<String> queue = read(input);        // 待分析输入串
        if (queue != null) {
            int index = 0;
            int len = queue.size();
            Stack<String> stack = new Stack<>();        // 符号栈
            stack.push("#");
            stack.push("CompUnit");

            while (!stack.empty() && index < len) {
                String left = stack.peek();
                String right = queue.get(index);
                // System.out.println(stack + "\t" + queue);
                if (isNonTerminal(left)) {
                    // 匹配到非终结符，查LL1分析表，表达式倒序入栈
                    String production = productions.get(LL1_table[getIndexOfNT(left)][getIndexOfT(right)]); // 查询LL1分析表
                    if (production.equals("err")) {
                        // err跳出
                        stop = true;
                        break;
                    } else {
                        stack.pop();
                        String[] strings = production.split("\\s+");
                        for (int i = strings.length - 1; i > 0; --i)
                            // 倒序入栈
                            stack.push(strings[i]);
                    }
                } else if (isTerminal(right) && match(left, right)) {
                    stack.pop();
                    ++index;
                } else {
                    stop = true;
                    break;
                }
            }
        }
        return !stop;
    }

    /*
    * 输入词法分析结果，输出对应单词表中对应类别
    * */
    private ArrayList<String> read(ArrayList<String> input) {
        ArrayList<String> output = null;
        if (input != null) {
            output = new ArrayList<>();
            for (String s : input) {
                String[] strs = s.split("\\s+");
                output.add(getWord(Integer.parseInt(strs[0])));
            }
            output.add("#");
        }
        return output;
    }

    private boolean match(String left, String right) {
        return left.equals(right);
    }

    private boolean isTerminal(String string) {
        return terminal.get(string) != null;
    }

    private boolean isNonTerminal(String string) {
        return non_terminal.get(string) != null;
    }
}
