package legends;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import legends.items.Accessory;
import legends.items.Armor;
import legends.items.Consumable;
import legends.items.Item;
import legends.items.Weapon;

/**
 * A small in-project library of canonical items and their background descriptions
 * used by the in-game catalog (图鉴).
 */
public final class ItemLibrary {
    public enum Category { WEAPON, ARMOR, ACCESSORY, CONSUMABLE }

    public static class Entry {
        public final Category cat;
        public final String name;
        public final String desc;
        public final Object sample; // Weapon/Armor/Accessory/Consumable instance

        public Entry(Category cat, String name, String desc, Object sample) {
            this.cat = cat; this.name = name; this.desc = desc; this.sample = sample;
        }
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    static {
        // Weapons
        ENTRIES.add(new Entry(Category.WEAPON, "Sword",
                "A versatile one-handed blade used by many warriors. Balanced damage and speed.",
                new Weapon("Sword", 1, 200, 50, 1)));
        ENTRIES.add(new Entry(Category.WEAPON, "Great Axe",
                "Two-handed axe that deals heavy damage at the cost of speed.",
                new Weapon("Great Axe", 2, 400, 90, 2)));
        ENTRIES.add(new Entry(Category.WEAPON, "Dragonfang Greatsword",
                "A legendary blade forged from dragon fangs. Devastating power against armoured foes.",
                new Weapon("Dragonfang Greatsword", 5, 1500, 220, 2)));
        ENTRIES.add(new Entry(Category.WEAPON, "Stormcaller Spear",
                "Long-reaching spear crackling with stored lightning, favored by battlefield captains.",
                new Weapon("Stormcaller Spear", 3, 650, 130, 2)));
        ENTRIES.add(new Entry(Category.WEAPON, "Twinfang Daggers",
                "Matched blades designed for rapid strikes and agile duelists.",
                new Weapon("Twinfang Daggers", 2, 360, 70, 1)));
        ENTRIES.add(new Entry(Category.WEAPON, "Eclipse Bow",
                "Composite bow harnessing lunar runes to deliver piercing shots over great distance.",
                new Weapon("Eclipse Bow", 4, 900, 160, 2)));
        ENTRIES.add(new Entry(Category.WEAPON, "Frostbrand Sabre",
                "An icy blade that chills foes on contact, ideal for dexterous duelists.",
                new Weapon("Frostbrand Sabre", 3, 520, 110, 1)));
        ENTRIES.add(new Entry(Category.WEAPON, "Thunder Maul",
                "Massive war maul that unleashes concussive shockwaves with each swing.",
                new Weapon("Thunder Maul", 5, 1320, 240, 2)));
        ENTRIES.add(new Entry(Category.WEAPON, "Shadow Pike",
                "A polearm forged in dusksteel, granting reach and keen armor-piercing strikes.",
                new Weapon("Shadow Pike", 4, 840, 150, 2)));
        ENTRIES.add(new Entry(Category.WEAPON, "Celestial Scepter",
                "Channel rod for arcane champions, amplifying spell damage and precision strikes.",
                new Weapon("Celestial Scepter", 6, 1700, 210, 1)));

        // Armors
        ENTRIES.add(new Entry(Category.ARMOR, "Leather Armor",
                "Light armor favored by agile fighters. Provides modest damage reduction.",
                new Armor("Leather Armor", 1, 150, 20)));
        ENTRIES.add(new Entry(Category.ARMOR, "Exo Plate Armor",
                "Heavy plated harness from exoskeleton materials. Superior protection.",
                new Armor("Exo Plate Armor", 4, 1200, 120)));
        ENTRIES.add(new Entry(Category.ARMOR, "Chainmail Hauberk",
                "Linked steel rings offering balanced defense without sacrificing mobility.",
                new Armor("Chainmail Hauberk", 2, 320, 45)));
        ENTRIES.add(new Entry(Category.ARMOR, "Guardian Breastplate",
                "Tempered plate favored by town defenders, sturdy against heavy blows.",
                new Armor("Guardian Breastplate", 3, 540, 70)));
        ENTRIES.add(new Entry(Category.ARMOR, "Mystic Ward Robes",
                "Layered ceremonial cloth infused with wards to mute incoming harm.",
                new Armor("Mystic Ward Robes", 2, 410, 35)));
        ENTRIES.add(new Entry(Category.ARMOR, "Dragonhide Mantle",
                "Scaled cloak stitched from dragon hide, resilient to both flame and fang.",
                new Armor("Dragonhide Mantle", 4, 960, 95)));
        ENTRIES.add(new Entry(Category.ARMOR, "Vanguard Bulwark",
                "Tower shield harness worn by siegebreakers, near-impenetrable on the front line.",
                new Armor("Vanguard Bulwark", 5, 1380, 130)));
        ENTRIES.add(new Entry(Category.ARMOR, "Nightstalker Cloak",
                "Shadowy cloak that absorbs glancing strikes while aiding stealthy movement.",
                new Armor("Nightstalker Cloak", 3, 520, 60)));
        ENTRIES.add(new Entry(Category.ARMOR, "Ember Ward Plate",
                "Forged in volcanic kilns, this armor disperses heat and physical trauma alike.",
                new Armor("Ember Ward Plate", 5, 1480, 140)));
        ENTRIES.add(new Entry(Category.ARMOR, "Crystal Bastion",
                "Prismatic mail laced with mana glass, delivering elite-tier protection.",
                new Armor("Crystal Bastion", 6, 1800, 165)));

        // Accessories
        ENTRIES.add(new Entry(Category.ACCESSORY, "Ring of Fortitude",
                "A humble ring that grants the wearer increased resilience.",
                new Accessory("Ring of Fortitude", 1, 120, 3, 0, 2)));
        ENTRIES.add(new Entry(Category.ACCESSORY, "Amulet of Swiftness",
                "A charm that sharpens reflexes and grants agility.",
                new Accessory("Amulet of Swiftness", 2, 300, 0, 0, 5)));
        ENTRIES.add(new Entry(Category.ACCESSORY, "Sapphire Circlet",
                "Jeweled headpiece that channels focus into precise spellcasting movements.",
                new Accessory("Sapphire Circlet", 3, 420, 0, 4, 4)));
        ENTRIES.add(new Entry(Category.ACCESSORY, "Obsidian Charm",
                "Dark talisman that bolsters raw strength while steadying the bearer's stance.",
                new Accessory("Obsidian Charm", 4, 580, 5, 0, 3)));
        ENTRIES.add(new Entry(Category.ACCESSORY, "Phoenix Feather Brooch",
                "Radiant brooch said to kindle rebirth, lending speed and grace in battle.",
                new Accessory("Phoenix Feather Brooch", 5, 850, 2, 4, 6)));
        ENTRIES.add(new Entry(Category.ACCESSORY, "Emerald Signet",
                "Cut emerald set in silver, invigorating the wearer's vitality and poise.",
                new Accessory("Emerald Signet", 2, 260, 2, 2, 2)));
        ENTRIES.add(new Entry(Category.ACCESSORY, "Crimson Warband",
                "Battle-worn armband that fuels ferocity and decisive strikes.",
                new Accessory("Crimson Warband", 3, 360, 4, 1, 1)));
        ENTRIES.add(new Entry(Category.ACCESSORY, "Lunar Pendant",
                "Moonlit pendant that heightens arcane intuition and nimble footwork.",
                new Accessory("Lunar Pendant", 4, 600, 0, 5, 3)));
        ENTRIES.add(new Entry(Category.ACCESSORY, "Titan's Torque",
                "Heavy torque favored by guardians, imbuing immense strength and resilience.",
                new Accessory("Titan's Torque", 5, 920, 6, 0, 2)));
        ENTRIES.add(new Entry(Category.ACCESSORY, "Gale Anklet",
                "Wind-etched anklet that accelerates strides and precise dodges.",
                new Accessory("Gale Anklet", 4, 540, 1, 2, 5)));

        // Consumables
        ENTRIES.add(new Entry(Category.CONSUMABLE, "Small Health Potion",
                "A common restorative potion that heals a moderate amount of HP.",
                new Consumable("Small Health Potion", 1, 50, Consumable.ConsumeType.HEAL, 50, true)));
        ENTRIES.add(new Entry(Category.CONSUMABLE, "Mana Tonic",
                "A vial of restorative essence to replenish a hero's magical energy.",
                new Consumable("Mana Tonic", 1, 40, Consumable.ConsumeType.RESTORE_MP, 30, true)));
        ENTRIES.add(new Entry(Category.CONSUMABLE, "Antidote",
                "A concoction that neutralizes common poisons.",
                new Consumable("Antidote", 1, 80, Consumable.ConsumeType.ANTIDOTE, 0, true)));
        ENTRIES.add(new Entry(Category.CONSUMABLE, "Stun Bomb",
                "An explosive that briefly stuns a single target when used in battle.",
                new Consumable("Stun Bomb", 2, 150, Consumable.ConsumeType.STUN_BOMB, 0, true)));
        ENTRIES.add(new Entry(Category.CONSUMABLE, "Greater Health Draught",
                "Concentrated elixir that rapidly mends deep wounds.",
                new Consumable("Greater Health Draught", 3, 140, Consumable.ConsumeType.HEAL, 150, true)));
        ENTRIES.add(new Entry(Category.CONSUMABLE, "Grand Mana Elixir",
                "Rare distillate that floods the user with replenished mana reserves.",
                new Consumable("Grand Mana Elixir", 4, 180, Consumable.ConsumeType.RESTORE_MP, 120, true)));
        ENTRIES.add(new Entry(Category.CONSUMABLE, "Panacea Kit",
                "Field kit capable of cleansing almost any toxin or debilitation.",
                new Consumable("Panacea Kit", 3, 210, Consumable.ConsumeType.ANTIDOTE, 0, true)));
        ENTRIES.add(new Entry(Category.CONSUMABLE, "Armor Shard Charge",
                "Shrapnel-packed charge that rips away enemy defenses.",
                new Consumable("Armor Shard Charge", 3, 190, Consumable.ConsumeType.ARMOR_BREAK, 6, true)));
        ENTRIES.add(new Entry(Category.CONSUMABLE, "Thunderflash Bomb",
                "A blinding burst device that guarantees a brief stun.",
                new Consumable("Thunderflash Bomb", 4, 260, Consumable.ConsumeType.STUN_BOMB, 0, true)));
        ENTRIES.add(new Entry(Category.CONSUMABLE, "Renewal Tonic",
                "Balanced tonic that restores both health and morale moderately.",
                new Consumable("Renewal Tonic", 2, 110, Consumable.ConsumeType.HEAL, 80, true)));
        ENTRIES.add(new Entry(Category.CONSUMABLE, "Aether Infusion",
                "Glowing draught used by archmages to recover vast mana reserves.",
                new Consumable("Aether Infusion", 5, 260, Consumable.ConsumeType.RESTORE_MP, 180, true)));
    }

    public static List<Entry> entriesFor(Category c) {
        List<Entry> out = new ArrayList<>();
        for (Entry e : ENTRIES) if (e.cat == c) out.add(e);
        return out;
    }

        public static Item randomEquipmentForLevel(int level, Random rng) {
                if (rng == null) {
                        rng = new Random();
                }
                List<Item> candidates = new ArrayList<>();
                for (Entry e : ENTRIES) {
                        if (!(e.sample instanceof Item)) continue;
                        Item sample = (Item) e.sample;
                        if (sample instanceof Weapon || sample instanceof Armor) {
                                int sampleLevel = sample.getLevel();
                                if (Math.abs(sampleLevel - level) <= 2) {
                                        candidates.add(sample);
                                }
                        }
                }
                if (candidates.isEmpty()) {
                        for (Entry e : ENTRIES) {
                                if (e.sample instanceof Weapon || e.sample instanceof Armor) {
                                        candidates.add((Item) e.sample);
                                }
                        }
                }
                if (candidates.isEmpty()) {
                        return null;
                }
                Item template = candidates.get(rng.nextInt(candidates.size()));
                Item copy = cloneItem(template);
                return copy != null ? copy : cloneItem(candidates.get(0));
        }

        private static Item cloneItem(Item sample) {
                if (sample instanceof Weapon) {
                        Weapon w = (Weapon) sample;
                        return new Weapon(w.getName(), w.getLevel(), w.getPrice(), w.getDamage(), w.getHands());
                }
                if (sample instanceof Armor) {
                        Armor a = (Armor) sample;
                        return new Armor(a.getName(), a.getLevel(), a.getPrice(), a.getReduction());
                }
                return null;
        }
}
