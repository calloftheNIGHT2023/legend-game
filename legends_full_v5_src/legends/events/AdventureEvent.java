package legends.events;

import java.util.Random;
import legends.io.IO;
import legends.party.Party;
import legends.model.Hero;

/**
 * Represents a simple adventure event that can occur on a COMMON tile.
 * Each event has a name, description and an effect when applied.
 * It may optionally force a battle after resolution.
 */
public class AdventureEvent {
    private final String id;
    private final String name;
    private final String description;
    private final boolean forcesBattle;
    private final Effect effect;

    public interface Effect {
        void apply(Party party, IO io, Random rng);
    }

    public AdventureEvent(String id, String name, String description, boolean forcesBattle, Effect effect) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.forcesBattle = forcesBattle;
        this.effect = effect;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isForcesBattle() { return forcesBattle; }

    public void resolve(Party party, IO io, Random rng) {
        io.println("-- Event encountered: " + name + " --");
        io.println(description);
        try { effect.apply(party, io, rng); } catch (Exception e) { io.println("(Event execution error, ignored)" ); }
    }

    // Helper methods for common effects
    public static void boostRandomStat(Hero h, IO io, Random rng) {
        int which = rng.nextInt(3);
        switch (which) {
            case 0: // strength
                setHeroField(h, "strength", h.getStrength() + 1); io.println(h.getName() + " 's strength increased by +1!"); break;
            case 1: // dexterity
                setHeroField(h, "dexterity", h.getDexterity() + 1); io.println(h.getName() + " 's dexterity increased by +1!"); break;
            default:
                setHeroField(h, "agility", h.getAgility() + 1); io.println(h.getName() + " 's agility increased by +1!"); break;
        }
    }

    private static void setHeroField(Hero h, String field, int val) {
        try {
            java.lang.reflect.Field f = h.getClass().getSuperclass().getDeclaredField(field);
            f.setAccessible(true);
            f.setInt(h, val);
        } catch (Exception e) {
            // ignore
        }
    }
}
