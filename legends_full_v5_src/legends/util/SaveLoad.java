package legends.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import legends.party.Party;
import legends.world.WorldMap;
import legends.model.Hero;
import legends.items.Weapon;
import legends.items.Armor;

import legends.skills.SimpleJson;

public class SaveLoad {
    private static final Path SAVE_PATH = Paths.get("save", "savegame.json");

    public static void saveGame(Party party, WorldMap map) throws IOException {
        Files.createDirectories(SAVE_PATH.getParent());
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"row\": ").append(party.getRow()).append(",\n");
        sb.append("  \"col\": ").append(party.getCol()).append(",\n");
        sb.append("  \"heroes\": [\n");
        List<Hero> heroes = party.getHeroes();
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            sb.append("    {");
            sb.append("\"class\": \"").append(h.getClass().getSimpleName()).append("\"");
            sb.append(", \"name\": \"").append(h.getName()).append("\"");
            sb.append(", \"level\": ").append(h.getLevel());
            sb.append(", \"hp\": ").append(h.getHp());
            sb.append(", \"mp\": ").append(h.getMp());
            sb.append(", \"strength\": ").append(h.getStrength());
            sb.append(", \"dexterity\": ").append(h.getDexterity());
            sb.append(", \"agility\": ").append(h.getAgility());
            sb.append(", \"gold\": ").append(h.getGold());
            sb.append(", \"healthPotions\": ").append(h.getHealthPotions());
            sb.append(", \"manaPotions\": ").append(h.getManaPotions());
            Weapon eqWeapon = h.getEquippedWeapon();
            if (eqWeapon != null) {
                sb.append(", \"equippedWeaponName\": \"").append(escape(eqWeapon.getName())).append("\"");
                sb.append(", \"equippedWeaponLevel\": ").append(eqWeapon.getLevel());
                sb.append(", \"equippedWeaponPrice\": ").append(eqWeapon.getPrice());
                sb.append(", \"equippedWeaponDamage\": ").append(eqWeapon.getDamage());
                sb.append(", \"equippedWeaponHands\": ").append(eqWeapon.getHands());
            }
            Armor eqArmor = h.getEquippedArmor();
            if (eqArmor != null) {
                sb.append(", \"equippedArmorName\": \"").append(escape(eqArmor.getName())).append("\"");
                sb.append(", \"equippedArmorLevel\": ").append(eqArmor.getLevel());
                sb.append(", \"equippedArmorPrice\": ").append(eqArmor.getPrice());
                sb.append(", \"equippedArmorReduction\": ").append(eqArmor.getReduction());
            }
            // consumables: write simple array of objects
            sb.append(", \"consumables\": [");
            java.util.List<legends.items.ConsumableStack> stacks = h.getConsumables();
            for (int j = 0; j < stacks.size(); j++) {
                legends.items.ConsumableStack st = stacks.get(j);
                legends.items.Consumable c = st.getConsumable();
                sb.append("{\"name\": \"").append(c.getName()).append("\", \"type\": \"")
                  .append(c.getType().name()).append("\", \"level\": ").append(c.getLevel())
                  .append(", \"count\": ").append(st.getCount()).append("}");
                if (j < stacks.size() - 1) sb.append(", ");
            }
            sb.append("]}");
            if (i < heroes.size() - 1) sb.append(",\n"); else sb.append("\n");
                }
                sb.append("  ]");
                List<Weapon> weaponBackpack = party.getBackpackWeapons();
                sb.append(",\n  \"weaponBackpack\": [");
                for (int i = 0; i < weaponBackpack.size(); i++) {
                        Weapon w = weaponBackpack.get(i);
                        sb.append("\n    {\"name\": \"").append(escape(w.getName())).append("\", \"level\": ").append(w.getLevel())
                            .append(", \"price\": ").append(w.getPrice())
                            .append(", \"damage\": ").append(w.getDamage())
                            .append(", \"hands\": ").append(w.getHands())
                            .append("}");
                        if (i < weaponBackpack.size() - 1) sb.append(",");
                }
                if (!weaponBackpack.isEmpty()) sb.append("\n  ");
                sb.append("]");

                List<Armor> armorBackpack = party.getBackpackArmors();
                sb.append(",\n  \"armorBackpack\": [");
                for (int i = 0; i < armorBackpack.size(); i++) {
                        Armor a = armorBackpack.get(i);
                        sb.append("\n    {\"name\": \"").append(escape(a.getName())).append("\", \"level\": ").append(a.getLevel())
                            .append(", \"price\": ").append(a.getPrice())
                            .append(", \"reduction\": ").append(a.getReduction())
                            .append("}");
                        if (i < armorBackpack.size() - 1) sb.append(",");
                }
                if (!armorBackpack.isEmpty()) sb.append("\n  ");
                sb.append("]\n");
                // catalog unlocked entries
        List<String> unlocked = legends.CatalogState.getUnlockedList();
        sb.append(",\n  \"catalogUnlocked\": [");
        for (int i = 0; i < unlocked.size(); i++) {
            sb.append("\"").append(escape(unlocked.get(i))).append("\"");
            if (i < unlocked.size() - 1) sb.append(", ");
        }
        sb.append("]\n}");
        Files.write(SAVE_PATH, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static Party loadGame(WorldMap map) throws IOException {
        if (!Files.exists(SAVE_PATH)) return null;
        String content = new String(Files.readAllBytes(SAVE_PATH), StandardCharsets.UTF_8);
        // extract row/col
        int row = extractIntField(content, "row", 0);
        int col = extractIntField(content, "col", 0);
        // extract heroes array substring
        String heroesArray = extractArray(content, "heroes");
        if (heroesArray == null) return null;
        List<Map<String,String>> parsed = SimpleJson.parseArrayFromString(heroesArray);
        List<Hero> heroes = new ArrayList<>();
        for (Map<String,String> obj : parsed) {
            String cls = obj.get("class");
            String name = obj.get("name");
            if (name == null) name = "Hero";
            Hero h = null;
            if ("Warrior".equalsIgnoreCase(cls)) h = Hero.createWarrior(name);
            else if ("Sorcerer".equalsIgnoreCase(cls)) h = Hero.createSorcerer(name);
            else if ("Paladin".equalsIgnoreCase(cls)) h = Hero.createPaladin(name);
            else h = Hero.createWarrior(name);

            // set fields via reflection to restore saved state
            try {
                setFieldInt(h, "level", parseInt(obj.get("level"), h.getLevel()));
                setFieldInt(h, "hp", parseInt(obj.get("hp"), h.getHp()));
                setFieldInt(h, "mp", parseInt(obj.get("mp"), h.getMp()));
                setFieldInt(h, "maxMp", parseInt(obj.get("mp"), h.getMaxMp()));
                setFieldInt(h, "strength", parseInt(obj.get("strength"), h.getStrength()));
                setFieldInt(h, "dexterity", parseInt(obj.get("dexterity"), h.getDexterity()));
                setFieldInt(h, "agility", parseInt(obj.get("agility"), h.getAgility()));
                setFieldInt(h, "gold", parseInt(obj.get("gold"), h.getGold()));
                setFieldInt(h, "healthPotions", parseInt(obj.get("healthPotions"), h.getHealthPotions()));
                setFieldInt(h, "manaPotions", parseInt(obj.get("manaPotions"), h.getManaPotions()));
                // consumables
                String cons = obj.get("consumables");
                if (cons != null && cons.startsWith("[")) {
                    List<Map<String,String>> consList = SimpleJson.parseArrayFromString(cons);
                    // clear existing consumables by reflection on consumables list
                        java.lang.reflect.Field f = Hero.class.getDeclaredField("consumables");
                        f.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        java.util.List<legends.items.ConsumableStack> existing =
                            (java.util.List<legends.items.ConsumableStack>) f.get(h);
                    existing.clear();
                    for (Map<String,String> cobj : consList) {
                        String cname = cobj.get("name");
                        String type = cobj.get("type");
                        int levelc = parseInt(cobj.get("level"), 1);
                        int count = parseInt(cobj.get("count"), 1);
                        legends.items.Consumable c = new legends.items.Consumable(cname, levelc, 0, legends.items.Consumable.ConsumeType.valueOf(type), 0, false);
                        for (int k = 0; k < count; k++) h.addConsumable(c);
                    }
                }
                String weaponName = obj.get("equippedWeaponName");
                if (weaponName != null && !weaponName.isEmpty()) {
                    Weapon restoredWeapon = new Weapon(weaponName,
                            parseInt(obj.get("equippedWeaponLevel"), 1),
                            parseInt(obj.get("equippedWeaponPrice"), 0),
                            parseInt(obj.get("equippedWeaponDamage"), 0),
                            parseInt(obj.get("equippedWeaponHands"), 1));
                    h.equipWeapon(restoredWeapon);
                }
                String armorName = obj.get("equippedArmorName");
                if (armorName != null && !armorName.isEmpty()) {
                    Armor restoredArmor = new Armor(armorName,
                            parseInt(obj.get("equippedArmorLevel"), 1),
                            parseInt(obj.get("equippedArmorPrice"), 0),
                            parseInt(obj.get("equippedArmorReduction"), 0));
                    h.equipArmor(restoredArmor);
                }
            } catch (Exception e) {
                // ignore reflection failures; best-effort restore
            }
            heroes.add(h);
        }
        List<Weapon> storedWeapons = new ArrayList<>();
        String weaponArray = extractArray(content, "weaponBackpack");
        if (weaponArray != null) {
            try {
                List<Map<String,String>> weaponObjs = SimpleJson.parseArrayFromString(weaponArray);
                for (Map<String,String> wobj : weaponObjs) {
                    String name = wobj.get("name");
                    if (name == null) continue;
                    Weapon w = new Weapon(name,
                            parseInt(wobj.get("level"), 1),
                            parseInt(wobj.get("price"), 0),
                            parseInt(wobj.get("damage"), 0),
                            parseInt(wobj.get("hands"), 1));
                    storedWeapons.add(w);
                }
            } catch (Exception e) {
                // ignore malformed weapon data
            }
        }

        List<Armor> storedArmors = new ArrayList<>();
        String armorArray = extractArray(content, "armorBackpack");
        if (armorArray != null) {
            try {
                List<Map<String,String>> armorObjs = SimpleJson.parseArrayFromString(armorArray);
                for (Map<String,String> aobj : armorObjs) {
                    String name = aobj.get("name");
                    if (name == null) continue;
                    Armor a = new Armor(name,
                            parseInt(aobj.get("level"), 1),
                            parseInt(aobj.get("price"), 0),
                            parseInt(aobj.get("reduction"), 0));
                    storedArmors.add(a);
                }
            } catch (Exception e) {
                // ignore malformed armor data
            }
        }

        Party p = new Party(heroes);
        p.loadInventory(storedWeapons, storedArmors);
        p.setPosition(row, col);
        // attempt to restore catalog unlocked entries if present
        String catalogArray = extractArray(content, "catalogUnlocked");
        if (catalogArray != null) {
            try {
                java.util.List<String> names = parseStringArray(catalogArray);
                legends.CatalogState.setUnlockedList(names);
            } catch (Exception ex) {
                // ignore catalog parsing issues
            }
        }
        return p;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static int extractIntField(String s, String key, int def) {
        int idx = s.indexOf('"' + key + '"');
        if (idx < 0) return def;
        int colon = s.indexOf(':', idx);
        if (colon < 0) return def;
        int j = colon + 1;
        while (j < s.length() && Character.isWhitespace(s.charAt(j))) j++;
        StringBuilder sb = new StringBuilder();
        while (j < s.length() && (Character.isDigit(s.charAt(j)) || s.charAt(j) == '-')) { sb.append(s.charAt(j)); j++; }
        try { return Integer.parseInt(sb.toString()); } catch (Exception e) { return def; }
    }

    private static int parseInt(String v, int def) { if (v == null) return def; try { return Integer.parseInt(v.trim()); } catch (Exception e) { return def; } }

    private static void setFieldInt(Object o, String name, int val) throws Exception {
        java.lang.reflect.Field f = o.getClass().getSuperclass().getDeclaredField(name);
        f.setAccessible(true);
        f.setInt(o, val);
    }

    private static java.util.List<String> parseStringArray(String arr) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (arr == null) return out;
        boolean inStr = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length(); i++) {
            char c = arr.charAt(i);
            if (!inStr) {
                if (c == '"') { inStr = true; sb.setLength(0); }
            } else {
                if (c == '\\') {
                    if (i + 1 < arr.length()) {
                        char n = arr.charAt(i + 1);
                        if (n == '"' || n == '\\') { sb.append(n); i++; } else { sb.append(c); }
                    } else sb.append(c);
                } else if (c == '"') {
                    inStr = false;
                    out.add(sb.toString());
                } else {
                    sb.append(c);
                }
            }
        }
        return out;
    }

    private static String extractArray(String content, String key) {
        int idx = content.indexOf('"' + key + '"');
        if (idx < 0) return null;
        int start = content.indexOf('[', idx);
        if (start < 0) return null;
        int depth = 0;
        for (int i = start; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) {
                    return content.substring(start, i + 1);
                }
            }
        }
        return null;
    }
}
