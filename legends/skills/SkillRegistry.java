package legends.skills;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import legends.effects.StatusEffect;

public class SkillRegistry {
    private static final Map<String, Skill> registry = new ConcurrentHashMap<>();

    static {
        // try to load declarative skills from data/skills.json first
        java.io.File f = new java.io.File("data/skills.json");
        if (f.exists()) {
            try {
                java.nio.file.Path p = f.toPath();
                java.util.List<java.util.Map<String,String>> list = SimpleJson.parseArray(p);
                for (java.util.Map<String,String> obj : list) {
                    String className = obj.get("class");
                    String name = obj.get("name");
                    int mpCost = parseInt(obj.get("mpCost"), 0);
                    double strMul = parseDouble(obj.get("strengthMultiplier"), 0.0);
                    double dexMul = parseDouble(obj.get("dexterityMultiplier"), 0.0);
                    String targetS = obj.get("target");
                    double chance = parseDouble(obj.get("chance"), 1.0);
                    String statusTypeS = obj.get("statusType");
                    int statusDuration = parseInt(obj.get("statusDuration"), 0);
                    int statusPotency = parseInt(obj.get("statusPotency"), 0);

                    StatusEffect.Type statusType = null;
                    if (statusTypeS != null && !statusTypeS.isEmpty()) {
                        try { statusType = StatusEffect.Type.valueOf(statusTypeS); } catch (IllegalArgumentException ex) { statusType = null; }
                    }

                    DeclarativeSkill.Target target = DeclarativeSkill.Target.SINGLE;
                    if ("AOE".equalsIgnoreCase(targetS)) target = DeclarativeSkill.Target.AOE;

                    DeclarativeSkill s = new DeclarativeSkill(name, mpCost, strMul, dexMul, target, chance, statusType, statusDuration, statusPotency);
                    if (className != null && !className.isEmpty()) registry.put(className, s);
                }
            } catch (IOException e) {
                // fallback to hardcoded if parsing fails
                registry.put("Warrior", new DeclarativeSkill("Power Strike", 20, 1.2, 0.0, DeclarativeSkill.Target.SINGLE, 1.0,
                        StatusEffect.Type.ARMOR_PENETRATE, 2, 2));
                registry.put("Sorcerer", new DeclarativeSkill("Arcane Blast", 30, 0.5, 1.5, DeclarativeSkill.Target.SINGLE, 1.0,
                        StatusEffect.Type.BURN, 3, 3));
                registry.put("Paladin", new DeclarativeSkill("Holy Smite", 25, 1.0, 0.5, DeclarativeSkill.Target.SINGLE, 1.0,
                        StatusEffect.Type.SLOW, 1, 1));
            }
        } else {
            // no data file; use the hardcoded defaults
            registry.put("Warrior", new DeclarativeSkill("Power Strike", 20, 1.2, 0.0, DeclarativeSkill.Target.SINGLE, 1.0,
                    StatusEffect.Type.ARMOR_PENETRATE, 2, 2));
            registry.put("Sorcerer", new DeclarativeSkill("Arcane Blast", 30, 0.5, 1.5, DeclarativeSkill.Target.SINGLE, 1.0,
                    StatusEffect.Type.BURN, 3, 3));
            registry.put("Paladin", new DeclarativeSkill("Holy Smite", 25, 1.0, 0.5, DeclarativeSkill.Target.SINGLE, 1.0,
                    StatusEffect.Type.SLOW, 1, 1));
        }
    }

    private static String extractString(String obj, String key) {
        try {
            String needle = '"' + key + '"';
            int i = obj.indexOf(needle);
            if (i >= 0) {
                int colon = obj.indexOf(':', i + needle.length());
                if (colon >= 0) {
                    int firstQuote = obj.indexOf('"', colon + 1);
                    if (firstQuote >= 0) {
                        int secondQuote = obj.indexOf('"', firstQuote + 1);
                        if (secondQuote > firstQuote) return obj.substring(firstQuote + 1, secondQuote);
                    }
                }
            }
        } catch (Exception e) {}
        return null;
    }

    private static int extractInt(String obj, String key, int def) {
        try {
            String needle = '"' + key + '"';
            int i = obj.indexOf(needle);
            if (i >= 0) {
                int colon = obj.indexOf(':', i + needle.length());
                if (colon >= 0) {
                    int j = colon + 1;
                    while (j < obj.length() && Character.isWhitespace(obj.charAt(j))) j++;
                    StringBuilder sb = new StringBuilder();
                    while (j < obj.length() && Character.isDigit(obj.charAt(j))) { sb.append(obj.charAt(j)); j++; }
                    if (sb.length() > 0) return Integer.parseInt(sb.toString());
                }
            }
        } catch (Exception e) {}
        return def;
    }

    private static double extractDouble(String obj, String key, double def) {
        try {
            String needle = '"' + key + '"';
            int i = obj.indexOf(needle);
            if (i >= 0) {
                int colon = obj.indexOf(':', i + needle.length());
                if (colon >= 0) {
                    int j = colon + 1;
                    while (j < obj.length() && Character.isWhitespace(obj.charAt(j))) j++;
                    StringBuilder sb = new StringBuilder();
                    boolean seenDot = false;
                    while (j < obj.length()) {
                        char c = obj.charAt(j);
                        if (Character.isDigit(c)) { sb.append(c); }
                        else if (c == '.' && !seenDot) { sb.append(c); seenDot = true; }
                        else break;
                        j++;
                    }
                    if (sb.length() > 0) return Double.parseDouble(sb.toString());
                }
            }
        } catch (Exception e) {}
        return def;
    }

    public static Skill getSkillForHeroClass(String className) {
        return registry.get(className);
    }

    private static int parseInt(String v, int def) {
        if (v == null) return def;
        try { return Integer.parseInt(v.trim()); } catch (Exception e) { return def; }
    }

    private static double parseDouble(String v, double def) {
        if (v == null) return def;
        try { return Double.parseDouble(v.trim()); } catch (Exception e) { return def; }
    }
}
