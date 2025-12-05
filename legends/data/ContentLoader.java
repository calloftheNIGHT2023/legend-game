package legends.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import legends.factory.MonsterRegistry;

// Very small content loader that reads simple semicolon-separated key=value entries
// Example line: type=Dragon;name=Young Red Dragon;level=1;baseDamage=20;defense=5;dodge=0.05
public class ContentLoader {

    public static List<Map<String, String>> loadEntries(File f) {
        List<Map<String, String>> out = new ArrayList<>();
        if (!f.exists()) return out;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Map<String, String> map = new HashMap<>();
                String[] parts = line.split(";");
                for (String p : parts) {
                    String[] kv = p.split("=", 2);
                    if (kv.length == 2) {
                        map.put(kv[0].trim(), kv[1].trim());
                    }
                }
                if (!map.isEmpty()) out.add(map);
            }
        } catch (Exception e) {
            // ignore and return what we have
        }
        return out;
    }

}
