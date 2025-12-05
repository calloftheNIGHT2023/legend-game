package legends.model;

public class Dragon extends Monster {
    public Dragon(String name, int level) {
        super(name, level,
                level * 12,
                level * 4,
                8 + level * 1
        );
    }

    @Override
    public String previewNextAction(java.util.Random rng, java.util.List<Hero> heroes) {
        if (rng.nextDouble() < 0.25) {
            return "Fire Breath (AOE)";
        }
        return "Claw Swipe";
    }

    @Override
    public void performAction(Hero target, java.util.List<Hero> allHeroes, java.util.Random rng, legends.io.IO io) {
        if (this.hasStatus(legends.effects.StatusEffect.Type.STUN)) {
            io.println(this.name + " is stunned and cannot act!");
            return;
        }
        // 25% chance to use Fire Breath affecting all heroes
        if (rng.nextDouble() < 0.25) {
            int aoe = Math.max(1, this.level * 15);
            io.println(this.name + " uses Fire Breath! Deals " + aoe + " fire damage to all heroes.");
            for (Hero h : allHeroes) {
                if (!h.isAlive()) continue;
                if (rng.nextDouble() < h.getDodgeChance()) {
                    io.println(h.getName() + " dodged the fire!");
                } else {
                    h.takeDamage(aoe);
                    io.println("    " + h.getName() + " HP=" + h.getHp() + "/" + h.getMaxHp());
                }
            }
            return;
        }
        // otherwise normal stronger claw
        int dmg = this.getBaseDamage() + Math.max(1, this.level * 2);
        if (target == null || !target.isAlive()) return;
        if (rng.nextDouble() < target.getDodgeChance()) {
            io.println(target.getName() + " dodged " + this.name + "'s claw!");
        } else {
            target.takeDamage(dmg);
            io.println(this.name + " slashed " + target.getName() + " for " + dmg + " damage.");
            io.println("    " + target.getName() + " HP=" + target.getHp() + "/" + target.getMaxHp());
        }
    }
}
