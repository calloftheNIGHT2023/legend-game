package legends.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import legends.io.IO;
import legends.party.Party;
import legends.model.Hero;
import legends.items.Consumable;
import legends.items.Weapon;
import legends.items.Armor;

/**
 * Simple hard-coded registry of adventure events. Later can be replaced by data-driven loader.
 */
public final class AdventureEventRegistry {
    private static final List<AdventureEvent> EVENTS = new ArrayList<AdventureEvent>();

    static {
        // 1. Small blessing: boost random stat of a random hero
        EVENTS.add(new AdventureEvent(
            "stat_blessing",
            "Ancient Stone Blessing",
            "You pause before a moss-covered monument. Faint runes shimmer, and power flows into one of your heroes.",
            false,
            new AdventureEvent.Effect() {
                public void apply(Party party, IO io, Random rng) {
                    java.util.List<Hero> hs = party.getHeroes();
                    if (hs.isEmpty()) return;
                    Hero h = hs.get(rng.nextInt(hs.size()));
                    AdventureEvent.boostRandomStat(h, io, rng);
                }
            }
        ));

        // 2. Find discarded gear: give a basic weapon or armor to weakest hero
        EVENTS.add(new AdventureEvent(
            "found_gear",
            "Abandoned Adventurer's Pack",
            "A worn backpack blows open in the wind, revealing gleaming equipment inside.",
            false,
            new AdventureEvent.Effect() {
                public void apply(Party party, IO io, Random rng) {
                    java.util.List<Hero> hs = party.getHeroes();
                    if (hs.isEmpty()) return;
                    Hero weakest = hs.get(0);
                    for (Hero h : hs) if (h.getLevel() < weakest.getLevel()) weakest = h;
                    if (rng.nextBoolean()) {
                        Weapon w = new Weapon("Scavenged Shortsword", Math.max(1, weakest.getLevel()), 0, 10 + weakest.getLevel()*2, 1);
                        weakest.equipWeapon(w);
                        io.println(weakest.getName() + " found and equipped " + w.getName());
                    } else {
                        Armor a = new Armor("Old Leather Armor", Math.max(1, weakest.getLevel()), 0, 8 + weakest.getLevel()*2);
                        weakest.equipArmor(a);
                        io.println(weakest.getName() + " found and equipped " + a.getName());
                    }
                }
            }
        ));

        // 3. Alchemical cache: give consumable(s)
        EVENTS.add(new AdventureEvent(
            "alchemical_cache",
            "Alchemical Remnants",
            "Among a pile of shattered vials, some usable potions remain.",
            false,
            new AdventureEvent.Effect() {
                public void apply(Party party, IO io, Random rng) {
                    java.util.List<Hero> hs = party.getHeroes(); if (hs.isEmpty()) return;
                    Hero h = hs.get(rng.nextInt(hs.size()));
                    Consumable c;
                    if (rng.nextBoolean()) c = new Consumable("Salvaged Health Potion", h.getLevel(), 0, Consumable.ConsumeType.HEAL, 40 + h.getLevel()*5, true);
                    else c = new Consumable("Salvaged Mana Potion", h.getLevel(), 0, Consumable.ConsumeType.RESTORE_MP, 30 + h.getLevel()*5, true);
                    h.addConsumable(c);
                    io.println(h.getName() + " obtained potion: " + c.getName());
                }
            }
        ));

        // 4. Ambush: forces a battle after a short narrative
        EVENTS.add(new AdventureEvent(
            "monster_ambush",
            "Ambush!",
            "Rustling footsteps echo from the shadows. Monsters suddenly leap out!",
            true,
            new AdventureEvent.Effect() {
                public void apply(Party party, IO io, Random rng) {
                    io.println("Monsters are closing in - there's no time to retreat!");
                }
            }
        ));
    }

    private AdventureEventRegistry() {}

    public static AdventureEvent randomEvent(Random rng) {
        if (EVENTS.isEmpty()) return null;
        return EVENTS.get(rng.nextInt(EVENTS.size()));
    }
}
