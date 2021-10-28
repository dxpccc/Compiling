import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Syntax {
    private HashMap<Integer, String> vocabulary = new HashMap<>();
    private ArrayList<String> productions = new ArrayList<>();
    private HashMap<String, Integer> terminal = new HashMap<>();
    private HashMap<String, Integer> non_terminal = new HashMap<>();
    private int[][] LL1_table = new int[6][5];

    private void init() {
        initVocabulary();
        initProductions();
        initLL1Table();
    }

    private void initVocabulary() {
        vocabulary.put(2, "Number");
        vocabulary.put(5, "int");
        vocabulary.put(6, "main");
        vocabulary.put(7, "return");
        vocabulary.put(8, "(");
        vocabulary.put(9, ")");
        vocabulary.put(10, "{");
        vocabulary.put(11, "}");
        vocabulary.put(12, ";");
    }

    private void initProductions() {
        productions.add("err");
        productions.add("CompUnit FuncDef");
        productions.add("FuncDef FuncType Ident ( ) Block");
        productions.add("FuncType int");
        productions.add("Ident main");
        productions.add("Block { Stmt }");
        productions.add("Stmt return Number ;");
    }

    private void initLL1Table() {
        initTerminal();
        initNonTerminal();

        LL1_table[0][0] = 1;
        LL1_table[1][0] = 2;
        LL1_table[2][0] = 3;
        LL1_table[3][1] = 4;
        LL1_table[4][2] = 5;
        LL1_table[5][3] = 6;
    }

    private void initTerminal() {
        terminal.put("int", 0);
        terminal.put("main", 1);
        terminal.put("{", 2);
        terminal.put("return", 3);
        terminal.put("#", 4);
    }

    private void initNonTerminal() {
        non_terminal.put("CompUnit", 0);
        non_terminal.put("FuncDef", 1);
        non_terminal.put("FuncType", 2);
        non_terminal.put("Ident", 3);
        non_terminal.put("Block", 4);
        non_terminal.put("Stmt", 5);
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
                if (isNonTerminal(left)) {
                    // 匹配到非终结符，查LL1分析表，表达式倒序入栈
                    String production = productions.get(LL1_table[getIndexOfNT(left)][getIndexOfT(right)]); // 查询LL1分析表
                    if (production.equals("err")) {
                        // err跳出
                        stop = true;
                        break;
                    } else {
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
