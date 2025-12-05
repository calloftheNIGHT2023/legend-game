package legends.model;

public class Exoskeleton extends Monster {
    public Exoskeleton(String name, int level) {
        super(name, level,
                level * 8,
                level * 10,
                5 + level * 0.5
        );
    }

    @Override
    public String previewNextAction(java.util.Random rng, java.util.List<Hero> heroes) {
        if (rng.nextDouble() < 0.20) {
            return "Shield Bash (heavy)";
        }
        return "Crushing Strike";
    }

    @Override
    public void performAction(Hero target, java.util.List<Hero> allHeroes, java.util.Random rng, legends.io.IO io) {
        if (this.hasStatus(legends.effects.StatusEffect.Type.STUN)) {
            io.println(this.name + " is stunned and cannot act!");
            return;
        }
        if (target == null || !target.isAlive()) return;
        if (rng.nextDouble() < 0.20) {
            int dmg = this.getBaseDamage() + Math.max(1, this.level * 6);
            io.println(this.name + " uses Shield Bash on " + target.getName() + "! ");
            if (rng.nextDouble() < target.getDodgeChance()) {
                io.println(target.getName() + " evaded the bash!");
            } else {
                target.takeDamage(dmg);
                io.println("    -> " + target.getName() + " takes " + dmg + " damage. HP=" + target.getHp() + "/" + target.getMaxHp());
            }
            return;
        }
        int dmg = this.getBaseDamage();
        if (rng.nextDouble() < target.getDodgeChance()) {
            io.println(target.getName() + " dodged " + this.name + "'s strike!");
        } else {
            target.takeDamage(dmg);
            io.println(this.name + " crushed " + target.getName() + " for " + dmg + " damage.");
            io.println("    " + target.getName() + " HP=" + target.getHp() + "/" + target.getMaxHp());
        }
    }
}
