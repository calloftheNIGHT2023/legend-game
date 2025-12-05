package legends.model;

import java.util.List;
import java.util.Random;

public class FinalBoss extends Monster {
    public FinalBoss(String name, int level) {
        super(name, level,
                level * 20,
                level * 8,
                5 + level * 2
        );
    }

    @Override
    public String previewNextAction(Random rng, List<Hero> heroes) {
        if (rng.nextDouble() < 0.3) return "Cataclysm (huge AOE)";
        if (rng.nextDouble() < 0.5) return "Meteor Strike (single heavy)";
        return "Devastating Slash";
    }

    @Override
    public void performAction(Hero target, List<Hero> allHeroes, Random rng, legends.io.IO io) {
        if (target == null) return;
        double roll = rng.nextDouble();
        if (roll < 0.3) {
            int dmg = Math.max(10, this.level * 40);
            io.println(this.name + " unleashes Cataclysm, hitting all heroes for " + dmg + "!");
            for (Hero h : allHeroes) {
                if (!h.isAlive()) continue;
                if (rng.nextDouble() < h.getDodgeChance()) {
                    io.println(h.getName() + " barely avoids some of the devastation!");
                } else {
                    h.takeDamage(dmg);
                    io.println("    " + h.getName() + " HP=" + h.getHp() + "/" + h.getMaxHp());
                }
            }
            return;
        }
        if (roll < 0.6) {
            int dmg = Math.max(20, this.level * 35);
            io.println(this.name + " calls down a Meteor on " + target.getName() + " for " + dmg + " damage!");
            if (rng.nextDouble() < target.getDodgeChance()) {
                io.println(target.getName() + " evades the Meteor!");
            } else {
                target.takeDamage(dmg);
                io.println("    " + target.getName() + " HP=" + target.getHp() + "/" + target.getMaxHp());
            }
            return;
        }
        int dmg = this.getBaseDamage() + this.level * 5;
        if (rng.nextDouble() < target.getDodgeChance()) {
            io.println(target.getName() + " dodged " + this.name + "'s slash!");
        } else {
            target.takeDamage(dmg);
            io.println(this.name + " slashes " + target.getName() + " for " + dmg + " damage.");
            io.println("    " + target.getName() + " HP=" + target.getHp() + "/" + target.getMaxHp());
        }
    }
}
