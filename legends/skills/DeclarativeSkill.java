package legends.skills;

import java.util.Random;
import legends.io.IO;
import legends.model.Hero;
import legends.model.Monster;
import legends.effects.StatusEffect;

public class DeclarativeSkill implements Skill {
    public enum Target { SINGLE, AOE }

    private final String name;
    private final int mpCost;
    private final double strengthMultiplier;
    private final double dexterityMultiplier;
    private final Target target;
    private final double chance; // 0..1
    // optional status effect
    private final StatusEffect.Type statusType;
    private final int statusDuration;
    private final int statusPotency;

    public DeclarativeSkill(String name, int mpCost, double strengthMultiplier, double dexterityMultiplier,
            Target target, double chance,
            StatusEffect.Type statusType, int statusDuration, int statusPotency) {
        this.name = name;
        this.mpCost = mpCost;
        this.strengthMultiplier = strengthMultiplier;
        this.dexterityMultiplier = dexterityMultiplier;
        this.target = target;
        this.chance = chance;
        this.statusType = statusType;
        this.statusDuration = statusDuration;
        this.statusPotency = statusPotency;
    }

    @Override
    public String getName() { return name; }

    @Override
    public int getMpCost() { return mpCost; }

    @Override
    public int apply(Hero user, Monster target, Random rng, IO io) {
        if (user.getMp() < mpCost) {
            io.println(user.getName() + " does not have enough MP to use " + name + ".");
            return 0;
        }
        // deduct MP using public API if available
        try {
            boolean ok = user.spendMp(mpCost);
            if (!ok) {
                io.println(user.getName() + " does not have enough MP to use " + name + ".");
                return 0;
            }
        } catch (NoSuchMethodError e) {
            // fallback: try to call castSkillOn via DefaultHeroSkill (unlikely)
        }

        // simple damage formula
        int baseDamage = (int) Math.round(user.getStrength() * strengthMultiplier + user.getDexterity() * dexterityMultiplier);
        int dmg = user.computeSpellDamage(Math.max(1, baseDamage));
        // chance to apply
        if (chance < 1.0 && rng.nextDouble() > chance) {
            io.println(user.getName() + " used " + name + " but it failed to trigger.");
            return 0;
        }

        if (dmg > 0 && target != null) {
            target.takeDamage(dmg);
            io.println(user.getName() + " used " + name + " on " + target.getName() + " for " + dmg + " damage.");
        }

        if (statusType != null && target != null) {
            target.addStatus(new StatusEffect(statusType, statusDuration, statusPotency));
            io.println(user.getName() + " applied " + statusType + " to " + target.getName());
        }

        return dmg;
    }

    public Target getTarget() { return target; }

    /**
     * Apply this declarative skill to multiple targets (AOE). Returns total damage dealt.
     */
    public int applyToTargets(Hero user, java.util.List<Monster> targets, Random rng, IO io) {
        if (user.getMp() < mpCost) {
            io.println(user.getName() + " does not have enough MP to use " + name + ".");
            return 0;
        }
        boolean ok = user.spendMp(mpCost);
        if (!ok) {
            io.println(user.getName() + " does not have enough MP to use " + name + ".");
            return 0;
        }

        int total = 0;
        for (Monster t : targets) {
            if (!t.isAlive()) continue;
            if (rng.nextDouble() < t.getDodgeChance()) {
                io.println(t.getName() + " dodged " + user.getName() + "'s " + name + "!");
                continue;
            }
            int baseDamage = (int) Math.round(user.getStrength() * strengthMultiplier + user.getDexterity() * dexterityMultiplier);
            int dmg = user.computeSpellDamage(Math.max(1, baseDamage));
            // chance to apply
            if (chance < 1.0 && rng.nextDouble() > chance) {
                io.println(user.getName() + " used " + name + " but it failed to trigger on " + t.getName() + ".");
                continue;
            }
            if (dmg > 0) {
                t.takeDamage(dmg);
                io.println(user.getName() + " used " + name + " on " + t.getName() + " for " + dmg + " damage.");
                total += dmg;
            }
            if (statusType != null) {
                t.addStatus(new StatusEffect(statusType, statusDuration, statusPotency));
                io.println(user.getName() + " applied " + statusType + " to " + t.getName());
            }
        }
        return total;
    }
}
