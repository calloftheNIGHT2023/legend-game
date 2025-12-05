package legends.io;

import java.util.List;

public interface IO {
    void println(String s);
    void print(String s);
    String readLine();
    void clear();
    // Convenience for printing lists
    default void println(List<String> lines) {
        for (String l : lines) println(l);
    }
}
