package legends.model;

public class Sorcerer extends Hero {
    public Sorcerer(String name, int level, int hp, int mp,
                    int strength, int dexterity, int agility, int gold) {
        super(name, level, hp, mp, strength, dexterity, agility, gold);
    }

    @Override
    protected void applyFavoredStats(int baseStrength, int baseDexterity, int baseAgility) {
        dexterity = Math.max(dexterity, scaleStat(baseDexterity, 0.10));
        agility = Math.max(agility, scaleStat(baseAgility, 0.10));
    }

    @Override
    public String getSkillName() {
        return "Arcane Blast";
    }

    @Override
    public int getSkillMpCost() {
        return 30;
    }

    @Override
    public int castSkillOn(Monster target) {
        if (mp < getSkillMpCost()) {
            return 0;
        }
        mp -= getSkillMpCost();
        int baseDamage = (int) Math.round(dexterity * 1.5 + strength * 0.5);
        int dmg = computeSpellDamage(Math.max(1, baseDamage));
        target.takeDamage(dmg);
        // apply burn effect: 3 turns, potency = dmg/5
        int potency = Math.max(1, dmg / 5);
        target.addStatus(new legends.effects.StatusEffect(legends.effects.StatusEffect.Type.BURN, 3, potency));
        return dmg;
    }
}
