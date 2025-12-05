package legends.skills;

import java.util.Random;
import legends.io.IO;
import legends.model.Hero;
import legends.model.Monster;

public class DefaultHeroSkill implements Skill {
    private final Hero owner;

    public DefaultHeroSkill(Hero owner) {
        this.owner = owner;
    }

    @Override
    public String getName() {
        return owner.getSkillName();
    }

    @Override
    public int getMpCost() {
        return owner.getSkillMpCost();
    }

    @Override
    public int apply(Hero user, Monster target, Random rng, IO io) {
        // delegate to existing hero implementation
        return owner.castSkillOn(target);
    }
}
