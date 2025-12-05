package legends.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import legends.battle.Fighter;
import legends.items.Item;
import legends.items.Weapon;
import legends.items.Armor;
import legends.util.VisualAssets;
import legends.ItemLibrary;

public abstract class Monster implements Fighter {
    protected String name;
    protected int level;
    protected int hp;
    protected int baseDamage;
    protected int defense;
    protected double dodge;

    protected Monster(String name, int level, int baseDamage, int defense, double dodge) {
        this.name = name;
        this.level = level;
        this.hp = level * 100;
        this.baseDamage = baseDamage;
        this.defense = defense;
        this.dodge = dodge;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void takeDamage(int dmg) {
        int ap = getArmorPenetrationTotal();
        int effectiveDefense = Math.max(0, defense - ap);
        int reduced = dmg - effectiveDefense;
        if (reduced < 0) reduced = 0;
        hp = Math.max(0, hp - reduced);
        if ("TEST".equalsIgnoreCase(legends.Config.DIFFICULTY)) {
            hp = 0; // developer test difficulty: every hit defeats the monster
        }
        try { legends.events.EventBus.getInstance().fire(new legends.events.OnDamageEvent(null, this, reduced)); } catch (Throwable t) {}
    }

    // Status effects for monsters
    private final java.util.List<legends.effects.StatusEffect> statusEffects = new java.util.ArrayList<>();

    public void addStatus(legends.effects.StatusEffect s) {
        statusEffects.add(s);
        try { legends.events.EventBus.getInstance().fire(new legends.events.OnStatusApplyEvent(null, this, s)); } catch (Throwable t) {}
    }

    public boolean hasStatus(legends.effects.StatusEffect.Type t) {
        for (legends.effects.StatusEffect s : statusEffects) if (!s.isExpired() && s.getType() == t) return true;
        return false;
    }

    public int getArmorPenetrationTotal() {
        int total = 0;
        for (legends.effects.StatusEffect s : statusEffects) {
            if (!s.isExpired() && s.getType() == legends.effects.StatusEffect.Type.ARMOR_PENETRATE) total += s.getPotency();
        }
        return total;
    }

    public void tickStatusEffects(legends.io.IO io) {
        java.util.Iterator<legends.effects.StatusEffect> it = statusEffects.iterator();
        while (it.hasNext()) {
            legends.effects.StatusEffect s = it.next();
            if (s.isExpired()) { it.remove(); continue; }
            switch (s.getType()) {
                case POISON:
                case BURN:
                    int dmg = s.getPotency();
                    hp = Math.max(0, hp - dmg);
                    io.println(this.name + " suffers " + dmg + " from " + s.getType());
                    break;
                case STUN:
                    io.println(this.name + " is stunned (" + s.getRemainingTurns() + " turns left)");
                    break;
                default:
                    break;
            }
            s.tick();
            if (s.isExpired()) it.remove();
        }
    }

    public String getName() {
        return name;
    }

    public double getDodgeChance() {
        double chance = dodge * 0.01;
        if (chance < 0) chance = 0;
        if (chance > 0.95) chance = 0.95;
        return chance;
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public int getDefense() {
        return defense;
    }

    public int getLevel() {
        return level;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return level * 100;
    }

    public String getTier() {
        return (level <= 2) ? "Beginner" : "Advanced";
    }

    @Override
    public String toString() {
        return String.format("%s [%s] (Lv %d) HP=%d DMG=%d DEF=%d DODGE=%.2f",
                name, getTier(), level, hp, baseDamage, defense, dodge);
    }

    public static List<Monster> spawnForParty(java.util.List<Hero> heroes) {
        return spawnForParty(heroes, 0);
    }

    /**
     * Spawn monsters for the given heroes with a level bias.
     * levelBias: negative => weaker monsters, positive => stronger monsters.
     */
    public static List<Monster> spawnForParty(java.util.List<Hero> heroes, int levelBias) {
        int maxLevel = 1;
        for (Hero h : heroes) {
            maxLevel = Math.max(maxLevel, h.getLevel());
        }
        int n = Math.max(1, heroes.size());
        List<Monster> result = new ArrayList<Monster>();
        Random rng = new Random();

        String[] beginnerDragons = { "Young Red Dragon", "Whelp Flamewing", "Tiny Ember Drake" };
        String[] beginnerExos = { "Rusty Shell", "Bronze Carapace", "Cracked Exoskeleton" };
        String[] beginnerSpirits = { "Faint Spirit", "Whispering Shade", "Lost Soul" };

        String[] advancedDragons = { "Ancient Red Dragon", "Crimson Tyrant", "Skyflame Dragon" };
        String[] advancedExos = { "Iron Wall", "Titan Shell", "Obsidian Carapace" };
        String[] advancedSpirits = { "Wailing Banshee", "Void Wraith", "Nightmare Spirit" };

        for (int i = 0; i < n; i++) {
            boolean beginner = (maxLevel <= 2);
            int typeIdx = rng.nextInt(3);
            String chosenType = null;
            String chosenName = null;
            if (typeIdx == 0) {
                chosenType = "Dragon";
                chosenName = beginner
                        ? beginnerDragons[rng.nextInt(beginnerDragons.length)]
                        : advancedDragons[rng.nextInt(advancedDragons.length)];
            } else if (typeIdx == 1) {
                chosenType = "Exoskeleton";
                chosenName = beginner
                        ? beginnerExos[rng.nextInt(beginnerExos.length)]
                        : advancedExos[rng.nextInt(advancedExos.length)];
            } else {
                chosenType = "Spirit";
                chosenName = beginner
                        ? beginnerSpirits[rng.nextInt(beginnerSpirits.length)]
                        : advancedSpirits[rng.nextInt(advancedSpirits.length)];
            }

            int monsterLevel = Math.max(1, maxLevel + levelBias);
            monsterLevel = Math.min(monsterLevel, maxLevel + legends.Config.MAX_MONSTER_LEVEL_DELTA);

            try {
                if (legends.factory.MonsterRegistry.has(chosenType)) {
                    legends.factory.MonsterFactory fac = legends.factory.MonsterRegistry.get(chosenType);
                    Monster m = fac.create(chosenName, monsterLevel);
                    result.add(m);
                } else {
                    if ("Dragon".equals(chosenType)) {
                        result.add(new Dragon(chosenName, monsterLevel));
                    } else if ("Exoskeleton".equals(chosenType)) {
                        result.add(new Exoskeleton(chosenName, monsterLevel));
                    } else {
                        result.add(new Spirit(chosenName, monsterLevel));
                    }
                }
            } catch (Exception e) {
                if ("Dragon".equals(chosenType)) {
                    result.add(new Dragon(chosenName, monsterLevel));
                } else if ("Exoskeleton".equals(chosenType)) {
                    result.add(new Exoskeleton(chosenName, monsterLevel));
                } else {
                    result.add(new Spirit(chosenName, monsterLevel));
                }
            }
        }
        return result;
    }

    public static java.util.List<Item> rewardHeroes(java.util.List<Hero> heroes, java.util.List<Monster> monsters) {
        int totalMonsterLevels = 0;
        List<Item> loot = new ArrayList<>();
        Random rng = new Random();

        for (Monster m : monsters) {
            totalMonsterLevels += Math.max(1, m.level);
            if (m instanceof Dragon) {
                loot.add(new Weapon("Dragonfang Greatsword (Lv " + m.level + ")",
                        m.level,
                        m.level * 300,
                        m.level * 40,
                        2));
            } else if (m instanceof Exoskeleton) {
                loot.add(new Armor("Exo Plate Armor (Lv " + m.level + ")",
                        m.level,
                        m.level * 250,
                        m.level * 30));
            } else if (m instanceof Spirit) {
                loot.add(new Weapon("Spirit Wand (Lv " + m.level + ")",
                        m.level,
                        m.level * 220,
                        m.level * 25,
                        1));
            }

            if (rng.nextDouble() < 0.30) {
                if (!heroes.isEmpty()) {
                    heroes.get(0).addHealthPotion();
                }
            }
            if (rng.nextDouble() < 0.20) {
                if (!heroes.isEmpty()) {
                    heroes.get(0).addManaPotion();
                }
            }

            if (rng.nextDouble() < 0.25) {
                Item randomLoot = ItemLibrary.randomEquipmentForLevel(m.level, rng);
                if (randomLoot != null) {
                    loot.add(randomLoot);
                }
            }
        }

        int goldPerHero = Math.max(0, totalMonsterLevels * 100);
        int expReward = Math.max(1, monsters.size() * 2);

        legends.LegendsGame.getGlobalIO().println("Battle rewards:");
        legends.LegendsGame.getGlobalIO().println("  Gold per surviving hero: " + goldPerHero);
        legends.LegendsGame.getGlobalIO().println("  EXP per surviving hero: " + expReward);
        if (!loot.isEmpty()) {
            legends.LegendsGame.getGlobalIO().println("  Loot dropped:");
            for (Item it : loot) {
                legends.LegendsGame.getGlobalIO().println("    - " + it.toString());
            }
        }

        for (Hero h : heroes) {
            if (!h.isAlive()) {
                h.reviveAtHalf();
            }
            h.gainGold(goldPerHero);
            h.gainExp(expReward);
        }

        return loot;
    }

    /**
     * Provide a short description of the action this monster will take next.
     * Default: normal attack.
     */
    public String previewNextAction(Random rng, java.util.List<Hero> heroes) {
        return "Normal Attack";
    }

    /**
     * Perform the monster's action. Default behavior: deal baseDamage to target hero.
     */
    public void performAction(Hero target, java.util.List<Hero> allHeroes, Random rng, legends.io.IO io) {
        if (target == null || !target.isAlive()) return;
        int dmg = this.getBaseDamage();
        if (rng.nextDouble() < target.getDodgeChance()) {
            io.println(target.getName() + " dodged " + this.getName() + "'s attack!");
        } else {
            VisualAssets.printMonsterAttackEffect(io, this);
            target.takeDamage(dmg);
            io.println(this.getName() + " attacked " + target.getName() + " for " + dmg + " damage.");
            io.println("    " + target.getName() + " HP=" + target.getHp() + "/" + target.getMaxHp());
        }
    }
}
