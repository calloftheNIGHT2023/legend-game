package legends.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import legends.model.Monster;

public class MonsterRegistry {
    private static final Map<String, MonsterFactory> REG = new ConcurrentHashMap<>();

    public static void register(String key, MonsterFactory factory) {
        REG.put(key, factory);
    }

    public static MonsterFactory get(String key) {
        return REG.get(key);
    }

    public static boolean has(String key) {
        return REG.containsKey(key);
    }
}
