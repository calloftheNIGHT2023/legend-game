package legends.party;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import legends.io.IO;
import legends.items.Armor;
import legends.items.Item;
import legends.items.Weapon;
import legends.model.Hero;

public class Party {
    private final List<Hero> heroes;
    private final List<Weapon> backpackWeapons = new ArrayList<>();
    private final List<Armor> backpackArmors = new ArrayList<>();
    private int row;
    private int col;

    public Party(List<Hero> heroes) {
        this.heroes = heroes;
    }

    public List<Hero> getHeroes() {
        return heroes;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setPosition(int r, int c) {
        this.row = r;
        this.col = c;
    }

    public void printInfo() {
        printInfo(legends.LegendsGame.getGlobalIO());
    }

    public void printInfo(IO io) {
        for (Hero h : heroes) {
            String[] icon = legends.util.VisualAssets.heroIcon(h);
            String weapon = h.getEquippedWeapon() != null ? h.getEquippedWeapon().getName() : "None";
            String armor = h.getEquippedArmor() != null ? h.getEquippedArmor().getName() : "None";
            io.println(icon[0] + "  " + legends.util.VisualAssets.coloredHeroName(h));
            io.println(icon[1] + "  Lv " + h.getLevel() + "  HP " + h.getHp() + "/" + h.getMaxHp()
                    + "  MP " + h.getMp() + "/" + h.getMaxMp());
            io.println(icon[2] + "  Weapon: " + weapon + "  Armor: " + armor);
            io.println("     Skill: " + h.getSkillName() + " (MP " + h.getSkillMpCost() + ")");
            io.println("     Potions HP:" + h.getHealthPotions() + " MP:" + h.getManaPotions());
            io.println("");
        }
    }

    public void openBackpack(IO io) {
        if (heroes.isEmpty()) {
            io.println("No heroes in the party.");
            return;
        }

        while (true) {
            io.clear();
            io.println("--- Party Equipment ---");
            printInfo(io);
            io.println("Backpack - Weapons:");
            if (backpackWeapons.isEmpty()) {
                io.println("  (empty)");
            } else {
                for (int i = 0; i < backpackWeapons.size(); i++) {
                    Weapon w = backpackWeapons.get(i);
                    io.println(String.format("  %d) %s  Lv:%d  DMG:%d  Hands:%d", i + 1,
                            w.getName(), w.getLevel(), w.getDamage(), w.getHands()));
                }
            }
            io.println("Backpack - Armor:");
            if (backpackArmors.isEmpty()) {
                io.println("  (empty)");
            } else {
                for (int i = 0; i < backpackArmors.size(); i++) {
                    Armor a = backpackArmors.get(i);
                    io.println(String.format("  %d) %s  Lv:%d  DEF:%d", i + 1,
                            a.getName(), a.getLevel(), a.getReduction()));
                }
            }
            io.println("");
            io.println("Options:");
            io.println("  1) Equip weapon");
            io.println("  2) Equip armor");
            io.println("  3) Close backpack");
            io.print("Choice: ");
            String choice = io.readLine();
            if (choice == null) {
                return;
            }
            choice = choice.trim();
            if (choice.isEmpty() || choice.equals("3")) {
                return;
            }
            switch (choice) {
                case "1":
                    handleEquipWeapon(io);
                    pause(io);
                    break;
                case "2":
                    handleEquipArmor(io);
                    pause(io);
                    break;
                default:
                    io.println("Invalid choice.");
                    pause(io);
                    break;
            }
        }
    }

    public void addWeaponToBackpack(Weapon weapon) {
        if (weapon != null) {
            backpackWeapons.add(weapon);
        }
    }

    public void addArmorToBackpack(Armor armor) {
        if (armor != null) {
            backpackArmors.add(armor);
        }
    }

    public List<Weapon> getBackpackWeapons() {
        return Collections.unmodifiableList(backpackWeapons);
    }

    public List<Armor> getBackpackArmors() {
        return Collections.unmodifiableList(backpackArmors);
    }

    public void loadInventory(List<Weapon> weapons, List<Armor> armors) {
        backpackWeapons.clear();
        backpackArmors.clear();
        if (weapons != null) {
            backpackWeapons.addAll(weapons);
        }
        if (armors != null) {
            backpackArmors.addAll(armors);
        }
    }

    public void stashLoot(List<Item> loot, IO io) {
        if (loot == null || loot.isEmpty()) {
            return;
        }
        for (Item it : loot) {
            if (it instanceof Weapon) {
                addWeaponToBackpack((Weapon) it);
            } else if (it instanceof Armor) {
                addArmorToBackpack((Armor) it);
            }
        }
        if (io != null) {
            io.println("Loot stored in the backpack. Open with I to equip.");
        }
    }

    private void handleEquipWeapon(IO io) {
        if (backpackWeapons.isEmpty()) {
            io.println("Backpack has no weapons.");
            return;
        }
        Hero hero = selectHero(io);
        if (hero == null) {
            return;
        }
        io.println("Choose weapon to equip (0 cancel):");
        for (int i = 0; i < backpackWeapons.size(); i++) {
            Weapon w = backpackWeapons.get(i);
            io.println(String.format("  %d) %s  Lv:%d  DMG:%d  Hands:%d", i + 1,
                    w.getName(), w.getLevel(), w.getDamage(), w.getHands()));
        }
        io.print("Weapon #: ");
        int choice = readIndex(io);
        if (choice < 0 || choice >= backpackWeapons.size()) {
            return;
        }
        Weapon selected = backpackWeapons.get(choice);
        if (hero.getLevel() < selected.getLevel()) {
            io.println("Hero level too low for this weapon.");
            return;
        }
        Weapon previous = hero.getEquippedWeapon();
        backpackWeapons.remove(choice);
        hero.equipWeapon(selected);
        io.println(hero.getName() + " equipped " + selected.getName() + ".");
        if (previous != null) {
            backpackWeapons.add(previous);
            io.println("Previous weapon stored back in backpack.");
        }
    }

    private void handleEquipArmor(IO io) {
        if (backpackArmors.isEmpty()) {
            io.println("Backpack has no armor.");
            return;
        }
        Hero hero = selectHero(io);
        if (hero == null) {
            return;
        }
        io.println("Choose armor to equip (0 cancel):");
        for (int i = 0; i < backpackArmors.size(); i++) {
            Armor a = backpackArmors.get(i);
            io.println(String.format("  %d) %s  Lv:%d  DEF:%d", i + 1,
                    a.getName(), a.getLevel(), a.getReduction()));
        }
        io.print("Armor #: ");
        int choice = readIndex(io);
        if (choice < 0 || choice >= backpackArmors.size()) {
            return;
        }
        Armor selected = backpackArmors.get(choice);
        if (hero.getLevel() < selected.getLevel()) {
            io.println("Hero level too low for this armor.");
            return;
        }
        Armor previous = hero.getEquippedArmor();
        backpackArmors.remove(choice);
        hero.equipArmor(selected);
        io.println(hero.getName() + " equipped " + selected.getName() + ".");
        if (previous != null) {
            backpackArmors.add(previous);
            io.println("Previous armor stored back in backpack.");
        }
    }

    private Hero selectHero(IO io) {
        io.println("Select hero (0 cancel):");
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            io.println(String.format("  %d) %s (Lv %d)", i + 1, h.getName(), h.getLevel()));
        }
        io.print("Hero #: ");
        int idx = readIndex(io);
        if (idx < 0 || idx >= heroes.size()) {
            return null;
        }
        return heroes.get(idx);
    }

    private int readIndex(IO io) {
        try {
            String raw = io.readLine();
            if (raw == null || raw.trim().isEmpty()) {
                return -1;
            }
            int val = Integer.parseInt(raw.trim());
            if (val <= 0) {
                return -1;
            }
            return val - 1;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void pause(IO io) {
        io.println("Press Enter to continue...");
        io.readLine();
    }
}
