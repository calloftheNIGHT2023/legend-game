package legends.skills;

import java.util.Random;
import legends.io.IO;
import legends.model.Hero;
import legends.model.Monster;

public interface Skill {
    String getName();
    int getMpCost();
    // apply the skill; return damage or effect magnitude as int
    int apply(Hero user, Monster target, Random rng, IO io);
}
