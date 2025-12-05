package legends.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import legends.io.IO;
import legends.model.Hero;
import legends.model.Monster;
import legends.util.VisualAssets;

public class BattleEngine {
    private final List<Hero> heroes;
    private final List<Monster> monsters;
    private final Random rng;
    private final IO io;

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";

    public BattleEngine(List<Hero> heroes, List<Monster> monsters, Random rng, IO io) {
        this.heroes = heroes;
        this.monsters = monsters;
        this.rng = rng;
        this.io = io;
    }

    public boolean runBattle() {
        // clear once at battle start to give initial clean view (don't clear every round)
        io.clear();
        io.println("--- Battle Start ---");
        while (anyAliveHero() && anyAliveMonster()) {
            printStatus();
            heroesTurn();
            if (!anyAliveMonster()) {
                break;
            }
            monstersTurn();
            for (Hero h : heroes) {
                if (h.isAlive()) {
                    h.regenAfterRound();
                }
            }
            // Tick status effects for heroes and monsters after regen
            for (Hero h : heroes) {
                h.tickStatusEffects(io);
            }
            for (Monster m : monsters) {
                m.tickStatusEffects(io);
            }
        }
        io.println("--- Battle End ---");
        return anyAliveHero();
    }

    private void printStatus() {
        io.println("Heroes:");
        for (Hero h : heroes) {
            String label = VisualAssets.heroBadge(h) + " " + VisualAssets.coloredHeroName(h);
            String hp = hpBar(h.getHp(), h.getMaxHp(), 20);
            String mp = mpBar(h.getMp(), h.getMaxMp(), 10);
                // use Skill API if available for display
                try {
                    legends.skills.Skill sk = h.getSkill();
                    io.println("  " + label + " " + hp + "  MP:" + mp + "  Skill:" + sk.getName() + " (MP:" + sk.getMpCost() + ")");
                } catch (Exception e) {
                    io.println("  " + label + " " + hp + "  MP:" + mp );
                }
            io.println("     HP=" + h.getHp() + "/" + h.getMaxHp() + "  MP=" + h.getMp() + "  HP Pots=" + h.getHealthPotions() + " MP Pots=" + h.getManaPotions());
        }
        io.println("Monsters:");
        for (int i = 0; i < monsters.size(); i++) {
            Monster m = monsters.get(i);
            String bar = hpBar(m.getHp(), m.getMaxHp(), 20);
            String name = String.format("%-12s", m.getName());
              io.println("  [" + (i + 1) + "] " + name + " [" + m.getTier() + "] " + bar + " HP=" + m.getHp() + "/" + m.getMaxHp());
        }
    }

    private String hpBar(int hp, int maxHp, int width) {
        if (maxHp <= 0) {
            maxHp = 1;
        }
        double ratio = (double) hp / maxHp;
        if (ratio < 0.0) {
            ratio = 0.0;
        }
        int filled = (int) Math.round(Math.min(1.0, ratio) * width);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        // choose color by health level
        String fillColor = GREEN;
        if (ratio <= 0.3) {
            fillColor = RED;
        } else if (ratio <= 0.6) {
            fillColor = YELLOW;
        }
        for (int i = 0; i < width; i++) {
            if (i < filled) {
                sb.append(fillColor).append("\u2588").append(RESET);
            } else {
                sb.append(" ");
            }
        }
        sb.append("]");
        // append percentage
        int percent = (int) Math.round(ratio * 100);
        sb.append(" ").append(percent).append("%");
        return sb.toString();
    }

    private String mpBar(int mp, int maxMp, int width) {
        if (maxMp <= 0) {
            maxMp = 1;
        }
        double ratio = (double) mp / maxMp;
        if (ratio < 0.0) {
            ratio = 0.0;
        }
        int filled = (int) Math.round(Math.min(1.0, ratio) * width);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < width; i++) {
            if (i < filled) {
                sb.append(CYAN).append("\u2588").append(RESET);
            } else {
                sb.append(" ");
            }
        }
        sb.append("]");
        int percent = (int) Math.round(ratio * 100);
        sb.append(" ").append(percent).append("%");
        return sb.toString();
    }

    private void heroesTurn() {
        io.println("Heroes' turn:");
        for (Hero h : heroes) {
            if (!h.isAlive()) {
                continue;
            }
            try { legends.events.EventBus.getInstance().fire(new legends.events.OnTurnStartEvent(h)); } catch (Throwable t) {}
            heroAction(h);
            if (!anyAliveMonster()) {
                return;
            }
        }
    }

    private void heroAction(Hero h) {
        while (true) {
            io.println("");
            io.println("Action for " + h.getName() + ":");
            io.println("  1) Normal Attack");
            io.println("  2) Use Skill (" + h.getSkillName() + ", MP cost: " + h.getSkillMpCost() + ")");
            io.println("  3) Use Health Potion (+" + h.getHealthPotionHealAmount() + " HP)");
            io.println("  4) Use Mana Potion (+" + h.getManaPotionRestoreAmount() + " MP)");
            io.println("  5) Skip");
            io.println("  6) Use Item");
            io.print("Choose (1-5): ");

            int choice;
            try {
                String raw = io.readLine();
                choice = Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                continue;
            }

            switch (choice) {
                case 1:
                    normalAttack(h);
                    return;
                case 2:
                    if (h.getMp() < h.getSkillMpCost()) {
                        io.println("Not enough MP to use " + h.getSkillName() + ".");
                        break;
                    }
                    skillAttack(h);
                    return;
                case 3:
                    if (h.useHealthPotion()) {
                        io.println(h.getName() + " used a health potion. HP is now " + h.getHp() + "/" + h.getMaxHp());
                        io.println("    " + h.getName() + " " + hpBar(h.getHp(), h.getMaxHp(), 20));
                        return;
                    } else {
                            io.println("No health potions left!");
                        break;
                    }
                case 4:
                    if (h.useManaPotion()) {
                            io.println(h.getName() + " used a mana potion. MP is now " + h.getMp());
                            io.println("    " + h.getName() + " " + mpBar(h.getMp(), h.getMaxMp(), 10));
                        return;
                    } else {
                            io.println("No mana potions left!");
                        break;
                    }
                case 5:
                    io.println(h.getName() + " skips the turn.");
                    return;
                case 6:
                    java.util.List<legends.items.ConsumableStack> stacks = h.getConsumables();
                    if (stacks.isEmpty()) {
                        io.println("No items in inventory.");
                        break;
                    }
                    io.println("Items:");
                    for (int i = 0; i < stacks.size(); i++) {
                        legends.items.ConsumableStack s = stacks.get(i);
                        legends.items.Consumable c = s.getConsumable();
                        legends.items.Consumable.ConsumeType t = c.getType();
                        boolean usable = true;
                        switch (t) {
                            case HEAL:
                                usable = h.getHp() < h.getMaxHp();
                                break;
                            case RESTORE_MP:
                                usable = h.getMp() < h.getMaxMp();
                                break;
                            case ANTIDOTE:
                                usable = h.hasStatus(legends.effects.StatusEffect.Type.POISON);
                                break;
                            case STUN_BOMB:
                            case ARMOR_BREAK:
                                usable = false;
                                for (Monster m : monsters) if (m.isAlive()) { usable = true; break; }
                                break;
                            default:
                                usable = true;
                        }

                        io.println(String.format("  %d) %s x%d  Lv:%d  %s", i + 1, c.getName(), s.getCount(), c.getLevel(), (usable ? "" : "(not usable)")));
                    }

                    io.print("Choose item to use (0 cancel): ");
                    int useIdx;
                    try {
                        String raw = io.readLine();
                        useIdx = Integer.parseInt(raw);
                    } catch (NumberFormatException e) {
                        break;
                    }
                    if (useIdx <= 0 || useIdx > stacks.size()) {
                        break;
                    }
                    legends.items.ConsumableStack chosenStack = stacks.get(useIdx - 1);
                    legends.items.Consumable chosen = chosenStack.getConsumable();
                    // recompute usability
                    boolean usable = true;
                    legends.items.Consumable.ConsumeType chosenType = chosen.getType();
                    switch (chosenType) {
                        case HEAL:
                            usable = h.getHp() < h.getMaxHp();
                            break;
                        case RESTORE_MP:
                            usable = h.getMp() < h.getMaxMp();
                            break;
                        case ANTIDOTE:
                            usable = h.hasStatus(legends.effects.StatusEffect.Type.POISON);
                            break;
                        case STUN_BOMB:
                        case ARMOR_BREAK:
                            usable = false;
                            for (Monster m : monsters) if (m.isAlive()) { usable = true; break; }
                            break;
                        default:
                            usable = true;
                    }

                    if (!usable) {
                        io.println("That item cannot be used right now.");
                        break;
                    }

                    Monster target = null;
                    if (chosen.needsTarget()) {
                        target = chooseTarget();
                        if (target == null) break;
                    }

                    // confirmation
                    io.print("Confirm use " + chosen.getName() + "? (y/n): ");
                    String conf = io.readLine();
                    if (conf == null || !conf.equalsIgnoreCase("y")) {
                        io.println("Cancelled.");
                        break;
                    }

                    boolean ok = h.useConsumableByIndex(useIdx - 1, target, io);
                    if (!ok) {
                        io.println("Failed to use item.");
                    }
                    return;
                default:
                    io.println("Invalid choice.");
            }
        }
    }

    private Monster chooseTarget() {
        List<Monster> alive = new ArrayList<>();
        for (Monster m : monsters) {
            if (m.isAlive()) {
                alive.add(m);
            }
        }
        if (alive.isEmpty()) {
            return null;
        }
        if (alive.size() == 1) {
            return alive.get(0);
        }
        io.print("Choose target monster by index: ");
        int idx;
        try {
            String raw = io.readLine();
            idx = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return alive.get(0);
        }
        if (idx <= 0 || idx > monsters.size() || !monsters.get(idx - 1).isAlive()) {
            return alive.get(0);
        }
        return monsters.get(idx - 1);
    }

    private void normalAttack(Hero h) {
        Monster target = chooseTarget();
        if (target == null) {
            return;
        }
        int dmg = h.calcAttackDamage();
        if (rng.nextDouble() < target.getDodgeChance()) {
            io.println(YELLOW + target.getName() + " dodged " + h.getName() + "'s attack!" + RESET);
        } else {
            VisualAssets.printHeroAttackEffect(io, h);
            target.takeDamage(dmg);
            io.println(h.getName() + " attacked " + target.getName() + " for " + RED + dmg + RESET + " damage.");
            io.println("    " + target.getName() + " " + hpBar(target.getHp(), target.getMaxHp(), 20)
                    + " HP=" + target.getHp() + "/" + target.getMaxHp());
        }
    }

    private void skillAttack(Hero h) {
        // support declarative AOE skills registered via SkillRegistry
        legends.skills.Skill sk = h.getSkill();
        if (sk == null) {
            sk = new legends.skills.DefaultHeroSkill(h);
        }
        String skillLabel = (sk.getName() == null || sk.getName().isEmpty())
                ? h.getSkillName()
                : sk.getName();

        if (sk instanceof legends.skills.DeclarativeSkill) {
            legends.skills.DeclarativeSkill ds = (legends.skills.DeclarativeSkill) sk;
            if (ds.getTarget() == legends.skills.DeclarativeSkill.Target.AOE) {
                // apply to all alive monsters
                List<Monster> alive = new ArrayList<>();
                for (Monster m : monsters) if (m.isAlive()) alive.add(m);
                if (alive.isEmpty()) return;
                VisualAssets.printHeroSkillEffect(io, h, skillLabel);
                int total = ds.applyToTargets(h, alive, rng, io);
                io.println(h.getName() + " used " + sk.getName() + " (AOE) total damage: " + RED + total + RESET);
                for (Monster m : monsters) {
                    if (!m.isAlive()) continue;
                    io.println("    " + m.getName() + " " + hpBar(m.getHp(), m.getMaxHp(), 20) + " HP=" + m.getHp() + "/" + m.getMaxHp());
                }
                return;
            }
        }

        Monster target = chooseTarget();
        if (target == null) {
            return;
        }
        if (rng.nextDouble() < target.getDodgeChance()) {
            io.println(YELLOW + target.getName() + " dodged " + h.getName() + "'s skill!" + RESET);
        } else {
            // use Skill API when available
            try {
                VisualAssets.printHeroSkillEffect(io, h, skillLabel);
                int dmg = sk.apply(h, target, rng, io);
                io.println(h.getName() + " used " + sk.getName() + " on "
                    + target.getName() + " for " + RED + dmg + RESET + " damage.");
                io.println("    " + target.getName() + " " + hpBar(target.getHp(), target.getMaxHp(), 20)
                    + " HP=" + target.getHp() + "/" + target.getMaxHp());
            } catch (Exception e) {
                VisualAssets.printHeroSkillEffect(io, h, skillLabel);
                int dmg = h.castSkillOn(target);
                io.println(h.getName() + " used " + h.getSkillName() + " on "
                    + target.getName() + " for " + RED + dmg + RESET + " damage.");
                io.println("    " + target.getName() + " " + hpBar(target.getHp(), target.getMaxHp(), 20)
                    + " HP=" + target.getHp() + "/" + target.getMaxHp());
            }
        }
    }

    private void monstersTurn() {
        io.println("Monsters' turn:");
        // Show previews of planned actions
        for (int i = 0; i < monsters.size(); i++) {
            Monster m = monsters.get(i);
            if (!m.isAlive()) continue;
            String preview = m.previewNextAction(rng, heroes);
            io.println("  [" + (i + 1) + "] " + m.getName() + " plans: " + preview);
        }
        // Execute actions
        for (Monster m : monsters) {
            if (!m.isAlive()) continue;
            try { legends.events.EventBus.getInstance().fire(new legends.events.OnTurnStartEvent(m)); } catch (Throwable t) {}
            Hero target = pickRandomAliveHero();
            if (target == null) return;
            m.performAction(target, heroes, rng, io);
        }
    }

    private boolean anyAliveHero() {
        for (Hero h : heroes) {
            if (h.isAlive()) {
                return true;
            }
        }
        return false;
    }

    private boolean anyAliveMonster() {
        for (Monster m : monsters) {
            if (m.isAlive()) {
                return true;
            }
        }
        return false;
    }

    private Hero pickRandomAliveHero() {
        List<Hero> alive = new ArrayList<>();
        for (Hero h : heroes) {
            if (h.isAlive()) {
                alive.add(h);
            }
        }
        if (alive.isEmpty()) {
            return null;
        }
        return alive.get(rng.nextInt(alive.size()));
    }
}
