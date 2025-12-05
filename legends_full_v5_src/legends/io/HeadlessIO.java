package legends.io;

import java.util.ArrayList;
import java.util.List;

// Simple headless IO for automated tests: collects outputs and provides scripted inputs
public class HeadlessIO implements IO {
    private final List<String> outputs = new ArrayList<>();
    private final List<String> inputs = new ArrayList<>();
    private int inputPos = 0;

    public HeadlessIO() {}

    public void addInput(String s) {
        inputs.add(s);
    }

    public List<String> getOutputs() {
        return outputs;
    }

    @Override
    public void println(String s) {
        outputs.add(s + "\n");
    }

    @Override
    public void print(String s) {
        outputs.add(s);
    }

    @Override
    public String readLine() {
        if (inputPos >= inputs.size()) return "";
        return inputs.get(inputPos++);
    }

    @Override
    public void clear() {
        outputs.add("<CLEAR>\n");
    }
}
