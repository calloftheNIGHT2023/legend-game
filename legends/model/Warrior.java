package legends.model;

public class Warrior extends Hero {
    public Warrior(String name, int level, int hp, int mp,
                   int strength, int dexterity, int agility, int gold) {
        super(name, level, hp, mp, strength, dexterity, agility, gold);
    }

    @Override
    protected void applyFavoredStats(int baseStrength, int baseDexterity, int baseAgility) {
        strength = Math.max(strength, scaleStat(baseStrength, 0.10));
        agility = Math.max(agility, scaleStat(baseAgility, 0.10));
    }

    @Override
    public String getSkillName() {
        return "Power Strike";
    }

    @Override
    public int getSkillMpCost() {
        return 20;
    }

    @Override
    public int castSkillOn(Monster target) {
        if (mp < getSkillMpCost()) {
            return 0;
        }
        mp -= getSkillMpCost();
        int baseAttack = strength + (equippedWeapon == null ? 0 : equippedWeapon.getDamage());
        int spellBase = (int) Math.round(baseAttack * 1.5);
        int dmg = computeSpellDamage(Math.max(1, spellBase));
        target.takeDamage(dmg);
        // apply armor penetration debuff: 2 turns, potency = level*2
        int potency = Math.max(1, getLevel() * 2);
        target.addStatus(new legends.effects.StatusEffect(legends.effects.StatusEffect.Type.ARMOR_PENETRATE, 2, potency));
        return dmg;
    }
}
