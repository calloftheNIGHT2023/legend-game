package legends.model;

public class Spirit extends Monster {
    public Spirit(String name, int level) {
        super(name, level,
                level * 10,
                level * 4,
                15 + level * 2
        );
    }

    @Override
    public String previewNextAction(java.util.Random rng, java.util.List<Hero> heroes) {
        if (rng.nextDouble() < 0.30) {
            return "Mimic (copies target attack)";
        }
        return "Ethereal Strike";
    }

    @Override
    public void performAction(Hero target, java.util.List<Hero> allHeroes, java.util.Random rng, legends.io.IO io) {
        if (this.hasStatus(legends.effects.StatusEffect.Type.STUN)) {
            io.println(this.name + " is stunned and cannot act!");
            return;
        }
        if (target == null || !target.isAlive()) return;
        if (rng.nextDouble() < 0.30) {
            // Mimic: copy target's normal attack damage
            int mimic = target.calcAttackDamage();
            io.println(this.name + " mimics " + target.getName() + "'s attack for " + mimic + " damage!");
            if (rng.nextDouble() < target.getDodgeChance()) {
                io.println(target.getName() + " dodged the mimic!");
            } else {
                target.takeDamage(mimic);
                io.println("    " + target.getName() + " HP=" + target.getHp() + "/" + target.getMaxHp());
            }
            return;
        }
        int dmg = this.getBaseDamage();
        if (rng.nextDouble() < target.getDodgeChance()) {
            io.println(target.getName() + " phased away from " + this.name + "'s strike!");
        } else {
            target.takeDamage(dmg);
            io.println(this.name + " struck " + target.getName() + " for " + dmg + " damage.");
            io.println("    " + target.getName() + " HP=" + target.getHp() + "/" + target.getMaxHp());
        }
    }
}
