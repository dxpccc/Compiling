package mid;

import util.AST.CompUnitAST;

import java.io.*;

public class IRBuilder {
    private String path;

    public IRBuilder(String path) {
        this.path = path;
    }

    public IRBuilder() {
        this(null);
    }

    public void generateIR(CompUnitAST ast) {
        File output = new File(path);
        Writer writer;
        BufferedWriter bfdWriter;
        try {
            writer = new FileWriter(output);
            bfdWriter = new BufferedWriter(writer);
            bfdWriter.write(ast.generateIR());
            bfdWriter.close();
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
