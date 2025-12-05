package legends.model;

import legends.battle.Fighter;
import legends.items.Weapon;
import legends.items.Armor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import legends.effects.StatusEffect;
import legends.effects.StatusEffect.Type;
import legends.items.Consumable;
import legends.items.ConsumableStack;
import legends.skills.DefaultHeroSkill;
import legends.skills.Skill;
import legends.io.IO;
import java.util.Collections;

public abstract class Hero implements Fighter {
    protected String name;
    protected int level;
    protected int exp;
    protected int hp;
    protected int mp;
    protected int maxMp;
    protected int strength;
    protected int dexterity;
    protected int agility;
    protected int gold;

    protected Weapon equippedWeapon;
    protected Armor equippedArmor;

    protected int healthPotions;
    protected int manaPotions;
    private final List<StatusEffect> statusEffects = new ArrayList<>();
    private final List<ConsumableStack> consumables = new ArrayList<>();

    protected Hero(String name, int level, int hp, int mp,
                   int strength, int dexterity, int agility, int gold) {
        this.name = name;
        this.level = level;
        this.hp = hp;
        this.mp = mp;
        this.maxMp = mp;
        this.strength = strength;
        this.dexterity = dexterity;
        this.agility = agility;
        this.gold = gold;
        this.healthPotions = 3;
        this.manaPotions = 1;
    }

    /**
     * Scale a stat by a percentage and round up so small gains are still impactful.
     */
    protected int scaleStat(int value, double percent) {
        return (int) Math.ceil(value * (1.0 + percent));
    }

    public static Hero createWarrior(String name) {
        int level = 1;
        int hp = level * 100;
        int mp = 80;
        int strength = 22;
        int dexterity = 10;
        int agility = 16;
        int gold = 300;
        return new Warrior(name, level, hp, mp, strength, dexterity, agility, gold);
    }

    public static Hero createSorcerer(String name) {
        int level = 1;
        int hp = level * 100;
        int mp = 120;
        int strength = 12;
        int dexterity = 22;
        int agility = 18;
        int gold = 300;
        return new Sorcerer(name, level, hp, mp, strength, dexterity, agility, gold);
    }

    public static Hero createPaladin(String name) {
        int level = 1;
        int hp = level * 100;
        int mp = 100;
        int strength = 20;
        int dexterity = 18;
        int agility = 12;
        int gold = 300;
        return new Paladin(name, level, hp, mp, strength, dexterity, agility, gold);
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void takeDamage(int dmg) {
        int armorPen = getArmorPenetrationTotal();
        int effectiveArmor = Math.max(0, getArmorReduction() - armorPen);
        int reduced = dmg - effectiveArmor;
        if (reduced < 0) {
            reduced = 0;
        }
        hp = Math.max(0, hp - reduced);
        // fire damage event
        try { legends.events.EventBus.getInstance().fire(new legends.events.OnDamageEvent(null, this, reduced)); } catch (Throwable t) {}
    }

    public void addStatus(StatusEffect s) {
        statusEffects.add(s);
        try { legends.events.EventBus.getInstance().fire(new legends.events.OnStatusApplyEvent(null, this, s)); } catch (Throwable t) {}
    }

    public boolean hasStatus(Type t) {
        for (StatusEffect s : statusEffects) if (s.getType() == t && !s.isExpired()) return true;
        return false;
    }

    public int getArmorPenetrationTotal() {
        int total = 0;
        for (StatusEffect s : statusEffects) {
            if (!s.isExpired() && s.getType() == Type.ARMOR_PENETRATE) total += s.getPotency();
        }
        return total;
    }

    public void tickStatusEffects(legends.io.IO io) {
        Iterator<StatusEffect> it = statusEffects.iterator();
        while (it.hasNext()) {
            StatusEffect s = it.next();
            if (s.isExpired()) { it.remove(); continue; }
            switch (s.getType()) {
                case POISON:
                case BURN:
                    int dmg = s.getPotency();
                    hp = Math.max(0, hp - dmg);
                    io.println(name + " suffers " + dmg + " from " + s.getType());
                    break;
                case STUN:
                    // stun handled by heroAction check
                    io.println(name + " is stunned (" + s.getRemainingTurns() + " turns left)");
                    break;
                case SLOW:
                    io.println(name + " is slowed (" + s.getRemainingTurns() + " turns left)");
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
        return agility * 0.002;
    }

    public int getArmorReduction() {
        return equippedArmor == null ? 0 : equippedArmor.getReduction();
    }

    public int calcAttackDamage() {
        int weaponDamage = equippedWeapon == null ? 0 : equippedWeapon.getDamage();
        double scaling = 0.40 + (level * 0.03);
        double total = strength * scaling + weaponDamage;
        if (weaponDamage == 0) {
            total += strength * 0.15; // bare-hand compensation keeps unarmed strikes viable
        }
        return (int) Math.max(2, Math.round(total));
    }

    public void regenAfterRound() {
        hp = Math.min(getMaxHp(), (int) Math.ceil(hp * 1.10));
        mp = Math.min(getMaxMp(), (int) Math.ceil(mp * 1.10));
    }

    /**
     * Fully restore hero HP and MP after battle victory.
     * This provides recovery between combat encounters.
     */
    public void fullRecover() {
        hp = getMaxHp();
        mp = getMaxMp();
    }

    public void gainGold(int amount) {
        gold += amount;
    }

    public boolean spendGold(int amount) {
        if (gold < amount) {
            return false;
        }
        gold -= amount;
        return true;
    }

    public void gainExp(int amount) {
        exp += amount;
        // Leveling factor driven by Config
        while (exp >= level * legends.Config.XP_PER_LEVEL_FACTOR) {
            exp -= level * legends.Config.XP_PER_LEVEL_FACTOR;
            levelUp();
        }
    }

    protected void levelUp() {
        level++;
        hp = level * 100;
        maxMp = (int) Math.ceil(maxMp * 1.10);
        mp = Math.min(maxMp, (int) Math.ceil(mp * 1.10));

        int baseStrength = strength;
        int baseDexterity = dexterity;
        int baseAgility = agility;

        strength = scaleStat(strength, 0.05);
        dexterity = scaleStat(dexterity, 0.05);
        agility = scaleStat(agility, 0.05);

        applyFavoredStats(baseStrength, baseDexterity, baseAgility);
        legends.LegendsGame.getGlobalIO().println(name + " leveled up to " + level + "!");
    }

    public int getMaxMp() {
        return maxMp;
    }

    protected abstract void applyFavoredStats(int baseStrength, int baseDexterity, int baseAgility);

    public abstract String getSkillName();
    public abstract int getSkillMpCost();
    public abstract int castSkillOn(Monster target);

    public int getHealthPotions() {
        return healthPotions;
    }

    public int getManaPotions() {
        return manaPotions;
    }

    public void addHealthPotion() {
        healthPotions++;
    }

    public void addManaPotion() {
        manaPotions++;
    }

    public int getHealthPotionHealAmount() {
        return getMaxHp() / 2;
    }

    public int getManaPotionRestoreAmount() {
        return 50;
    }

    public boolean useHealthPotion() {
        if (healthPotions <= 0) {
            return false;
        }
        healthPotions--;
        int heal = getHealthPotionHealAmount();
        hp = Math.min(getMaxHp(), hp + heal);
        return true;
    }

    public boolean useManaPotion() {
        if (manaPotions <= 0) {
            return false;
        }
        manaPotions--;
        int restore = getManaPotionRestoreAmount();
        mp = Math.min(getMaxMp(), mp + restore);
        return true;
    }

    // Consumable inventory API
    public void addConsumable(Consumable c) {
        if (c == null) return;
        for (ConsumableStack s : consumables) {
            if (s.matches(c)) {
                s.increment(1);
                return;
            }
        }
        consumables.add(new ConsumableStack(c, 1));
    }

    public Consumable removeConsumable(int idx) {
        if (idx < 0 || idx >= consumables.size()) return null;
        ConsumableStack s = consumables.get(idx);
        if (s.getCount() <= 1) {
            consumables.remove(idx);
            return s.getConsumable();
        } else {
            s.decrementOne();
            return s.getConsumable();
        }
    }

    /**
     * Remove an entire consumable stack (used when selling items).
     */
    public ConsumableStack removeConsumableStack(int idx) {
        if (idx < 0 || idx >= consumables.size()) return null;
        return consumables.remove(idx);
    }

    public java.util.List<ConsumableStack> getConsumables() {
        return Collections.unmodifiableList(consumables);
    }

    public int heal(int amount) {
        if (amount <= 0) return 0;
        int before = this.hp;
        this.hp = Math.min(getMaxHp(), this.hp + amount);
        return this.hp - before;
    }

    public int restoreMp(int amount) {
        if (amount <= 0) return 0;
        int before = this.mp;
        this.mp = Math.min(getMaxMp(), this.mp + amount);
        return this.mp - before;
    }

    public void removeStatus(Type t) {
        if (t == null) return;
        java.util.Iterator<StatusEffect> it = statusEffects.iterator();
        while (it.hasNext()) {
            StatusEffect s = it.next();
            if (s.getType() == t) it.remove();
        }
    }

    public boolean useConsumableByIndex(int idx, Monster target, IO io) {
        if (idx < 0 || idx >= consumables.size()) return false;
        ConsumableStack stack = consumables.get(idx);
        Consumable c = stack.getConsumable();
        boolean ok = c.apply(this, target, io);
        if (ok) {
            // consume one
            if (!stack.decrementOne()) {
                // if count was zero (shouldn't happen) remove
                consumables.remove(idx);
            }
            if (stack.getCount() == 0) consumables.remove(idx);
        }
        return ok;
    }

    public void equipWeapon(Weapon w) {
        this.equippedWeapon = w;
    }

    public void equipArmor(Armor a) {
        this.equippedArmor = a;
    }

    public Weapon getEquippedWeapon() {
        return equippedWeapon;
    }

    public Armor getEquippedArmor() {
        return equippedArmor;
    }

    public void reviveAtHalf() {
        this.hp = getMaxHp() / 2;
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

    public int getMp() {
        return mp;
    }

    /**
     * Consume MP from hero. Returns true if enough MP and deducted, false otherwise.
     */
    public boolean spendMp(int amount) {
        if (amount <= 0) return true;
        if (mp < amount) return false;
        mp -= amount;
        return true;
    }

    public Skill getSkill() {
        Skill s = legends.skills.SkillRegistry.getSkillForHeroClass(this.getClass().getSimpleName());
        if (s != null) return s;
        return new DefaultHeroSkill(this);
    }

    public int getGold() {
        return gold;
    }

    public int getStrength() {
        return strength;
    }

    public int getDexterity() {
        return dexterity;
    }

    public int getAgility() {
        return agility;
    }

    /**
     * Compute final spell damage using the global spell formula.
     */
    public int computeSpellDamage(int spellBaseDamage) {
        if (spellBaseDamage <= 0) {
            return 0;
        }
        double bonus = (dexterity / 10000.0) * spellBaseDamage;
        return (int) Math.round(spellBaseDamage + bonus);
    }

    @Override
    public String toString() {
        return String.format(
                "%s (Lv %d) HP=%d/%d MP=%d STR=%d DEX=%d AGI=%d Gold=%d HP Pots=%d MP Pots=%d",
                name, level, hp, getMaxHp(), mp, getMaxMp(), strength, dexterity, agility, gold,
                healthPotions, manaPotions);
    }
}
