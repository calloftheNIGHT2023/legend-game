package legends.model;

public class Paladin extends Hero {
    public Paladin(String name, int level, int hp, int mp,
                   int strength, int dexterity, int agility, int gold) {
        super(name, level, hp, mp, strength, dexterity, agility, gold);
    }

    @Override
    protected void applyFavoredStats(int baseStrength, int baseDexterity, int baseAgility) {
        strength = Math.max(strength, scaleStat(baseStrength, 0.10));
        dexterity = Math.max(dexterity, scaleStat(baseDexterity, 0.10));
    }

    @Override
    public String getSkillName() {
        return "Holy Smite";
    }

    @Override
    public int getSkillMpCost() {
        return 25;
    }

    @Override
    public int castSkillOn(Monster target) {
        if (mp < getSkillMpCost()) {
            return 0;
        }
        mp -= getSkillMpCost();
        int baseDamage = (int) Math.round(strength * 1.2 + dexterity * 1.0);
        int dmg = computeSpellDamage(Math.max(1, baseDamage));
        target.takeDamage(dmg);
        int heal = getMaxHp() / 5;
        hp = Math.min(getMaxHp(), hp + heal);
        legends.LegendsGame.getGlobalIO().println(name + " is healed for " + heal + " HP by holy energy.");
        // paladin skill also grants small temporary resist (SLOW as flavor)
        target.addStatus(new legends.effects.StatusEffect(legends.effects.StatusEffect.Type.SLOW, 1, 1));
        return dmg;
    }
}
