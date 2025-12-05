package legends.io;

import java.util.Scanner;

public class ConsoleIO implements IO {
    private final Scanner scanner;

    public ConsoleIO() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void println(String s) {
        System.out.println(s);
    }

    @Override
    public void print(String s) {
        System.out.print(s);
    }

    @Override
    public String readLine() {
        String line = "";
        try {
            if (scanner.hasNextLine()) {
                line = scanner.nextLine();
            }
        } catch (Exception e) {
            // ignore
        }
        return (line == null) ? "" : line.trim();
    }

    @Override
    public void clear() {
        try {
            String os = System.getProperty("os.name");
            if (os != null && os.toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\u001b[2J\u001b[H");
                System.out.flush();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public Scanner getScanner() {
        return scanner;
    }
}
