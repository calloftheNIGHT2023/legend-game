package legends.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import legends.io.IO;
import legends.model.Hero;
import legends.model.Monster;

/**
 * Provides reusable ASCII/ANSI art assets for heroes, monsters, and combat feedback.
 * Visual styles are centrally registered so new entities can opt-in without editing the renderer.
 */
public final class VisualAssets {
    private VisualAssets() {}

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String WHITE = "\u001B[37m";

    private static final List<String[]> DEFAULT_HERO_ATTACK_FRAMES = immutableCopyFromArray(new String[][] {
        {
            "      /\\",
            " >>>===>",
            "      \\\\" 
        },
        {
            "   __/\\__",
            " -->>==>>",
            "     \\\\" 
        }
    });

    private static final List<String[]> DEFAULT_HERO_SKILL_FRAMES = immutableCopyFromArray(new String[][] {
        {
            "   *\\  //*   ",
            "<<< %s >>>",
            "   //*  \\*   "
        },
        {
            "  ~~~~~~~~~~  ",
            "<< %s >>",
            "  ~~~~~~~~~~  "
        }
    });

    private static final List<String[]> DEFAULT_MONSTER_ATTACK_FRAMES = immutableCopyFromArray(new String[][] {
        {
            "  \\/////  ",
            "  >>===>>  ",
            "  /////\\  "
        },
        {
            "   ,,,,,   ",
            "<<<=====>>>",
            "   '''''   "
        }
    });

    private static final HeroVisual DEFAULT_HERO_VISUAL = new HeroVisual(
        GREEN,
        GREEN + "[H]" + RESET,
        new String[] {
            GREEN + "  /\\  " + RESET,
            GREEN + " [??] " + RESET,
            GREEN + "  ||  " + RESET
        },
        DEFAULT_HERO_ATTACK_FRAMES,
        DEFAULT_HERO_SKILL_FRAMES
    );

    private static final MonsterVisual DEFAULT_MONSTER_VISUAL = new MonsterVisual(
        CYAN,
        DEFAULT_MONSTER_ATTACK_FRAMES
    );

    private static final Map<String, HeroVisual> HERO_VISUALS = new ConcurrentHashMap<>();
    private static final Map<String, MonsterVisual> MONSTER_VISUALS = new ConcurrentHashMap<>();

    static {
        registerHeroVisual("default", DEFAULT_HERO_VISUAL);
        registerHeroVisual("hero", DEFAULT_HERO_VISUAL);

        registerHeroVisual("warrior", new HeroVisual(
            RED,
            RED + "[W]" + RESET,
            new String[] {
                RED + "  /\\  " + RESET,
                RED + " [##] " + RESET,
                RED + "  ||  " + RESET
            },
            DEFAULT_HERO_ATTACK_FRAMES,
            DEFAULT_HERO_SKILL_FRAMES
        ));

        registerHeroVisual("sorcerer", new HeroVisual(
            MAGENTA,
            MAGENTA + "[S]" + RESET,
            new String[] {
                MAGENTA + "  /\\  " + RESET,
                MAGENTA + " (**) " + RESET,
                MAGENTA + "  ||  " + RESET
            },
            DEFAULT_HERO_ATTACK_FRAMES,
            DEFAULT_HERO_SKILL_FRAMES
        ));

        registerHeroVisual("paladin", new HeroVisual(
            CYAN,
            CYAN + "[P]" + RESET,
            new String[] {
                CYAN + "  /\\  " + RESET,
                CYAN + " [+ ] " + RESET,
                CYAN + "  ||  " + RESET
            },
            DEFAULT_HERO_ATTACK_FRAMES,
            DEFAULT_HERO_SKILL_FRAMES
        ));

        registerMonsterVisual("default", DEFAULT_MONSTER_VISUAL);
        registerMonsterVisual("monster", DEFAULT_MONSTER_VISUAL);
        registerMonsterVisual("dragon", new MonsterVisual(RED, DEFAULT_MONSTER_ATTACK_FRAMES));
        registerMonsterVisual("spirit", new MonsterVisual(MAGENTA, DEFAULT_MONSTER_ATTACK_FRAMES));
        registerMonsterVisual("exoskeleton", new MonsterVisual(YELLOW, DEFAULT_MONSTER_ATTACK_FRAMES));
        registerMonsterVisual("finalboss", new MonsterVisual(RED, DEFAULT_MONSTER_ATTACK_FRAMES));
    }

    public static void registerHeroVisual(String className, HeroVisual visual) {
        if (className == null || className.isEmpty() || visual == null) {
            return;
        }
        HERO_VISUALS.put(className.toLowerCase(), visual);
    }

    public static void registerMonsterVisual(String className, MonsterVisual visual) {
        if (className == null || className.isEmpty() || visual == null) {
            return;
        }
        MONSTER_VISUALS.put(className.toLowerCase(), visual);
    }

    public static String[] heroIcon(Hero hero) {
        HeroVisual visual = getHeroVisual(hero);
        String[] icon = visual.getIconLines();
        if (icon.length == 0) {
            return DEFAULT_HERO_VISUAL.getIconLines();
        }
        return icon;
    }

    public static String coloredHeroName(Hero hero) {
        HeroVisual visual = getHeroVisual(hero);
        return visual.getColor() + hero.getName() + RESET + " (" + hero.getClass().getSimpleName() + ")";
    }

    public static void printHeroAttackEffect(IO io, Hero hero) {
        HeroVisual visual = getHeroVisual(hero);
        String[] frame = pickRandomFrame(visual.getAttackFrames(), DEFAULT_HERO_ATTACK_FRAMES);
        for (String line : frame) {
            io.println(visual.getColor() + line + RESET);
        }
        io.println(visual.getColor() + "   " + hero.getName().toUpperCase() + " STRIKE!" + RESET);
    }

    public static void printHeroSkillEffect(IO io, Hero hero, String skillName) {
        HeroVisual visual = getHeroVisual(hero);
        String label = (skillName == null || skillName.isEmpty()) ? "SKILL" : skillName.toUpperCase();
        String[] frame = pickRandomFrame(visual.getSkillFrames(), DEFAULT_HERO_SKILL_FRAMES);
        for (String raw : frame) {
            String line = raw.contains("%s") ? String.format(raw, label) : raw;
            io.println(visual.getColor() + line + RESET);
        }
    }

    public static void printMonsterAttackEffect(IO io, Monster monster) {
        MonsterVisual visual = getMonsterVisual(monster);
        String[] frame = pickRandomFrame(visual.getAttackFrames(), DEFAULT_MONSTER_ATTACK_FRAMES);
        for (String line : frame) {
            io.println(visual.getColor() + line + RESET);
        }
        String name = monster != null ? monster.getName().toUpperCase() : "MONSTER";
        io.println(visual.getColor() + "   " + name + " LASHES OUT!" + RESET);
    }

    public static String heroBadge(Hero hero) {
        return getHeroVisual(hero).getBadge();
    }

    private static HeroVisual getHeroVisual(Hero hero) {
        if (hero == null) {
            return DEFAULT_HERO_VISUAL;
        }
        String key = hero.getClass().getSimpleName().toLowerCase();
        HeroVisual visual = HERO_VISUALS.get(key);
        if (visual != null) {
            return visual;
        }
        return DEFAULT_HERO_VISUAL;
    }

    private static MonsterVisual getMonsterVisual(Monster monster) {
        if (monster == null) {
            return DEFAULT_MONSTER_VISUAL;
        }
        String key = monster.getClass().getSimpleName().toLowerCase();
        MonsterVisual visual = MONSTER_VISUALS.get(key);
        if (visual != null) {
            return visual;
        }
        return DEFAULT_MONSTER_VISUAL;
    }

    private static String[] pickRandomFrame(List<String[]> frames, List<String[]> fallback) {
        List<String[]> pool = (frames == null || frames.isEmpty()) ? fallback : frames;
        if (pool.isEmpty()) {
            return new String[] { "" };
        }
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    private static List<String[]> immutableCopyFromArray(String[][] data) {
        if (data == null || data.length == 0) {
            return Collections.emptyList();
        }
        List<String[]> frames = new ArrayList<>(data.length);
        for (String[] frame : data) {
            frames.add(frame.clone());
        }
        return Collections.unmodifiableList(frames);
    }

    private static List<String[]> immutableCopy(List<String[]> frames) {
        if (frames == null || frames.isEmpty()) {
            return Collections.emptyList();
        }
        List<String[]> copy = new ArrayList<>(frames.size());
        for (String[] frame : frames) {
            copy.add(frame.clone());
        }
        return Collections.unmodifiableList(copy);
    }

    public static final class HeroVisual {
        private final String color;
        private final String badge;
        private final String[] iconLines;
        private final List<String[]> attackFrames;
        private final List<String[]> skillFrames;

        public HeroVisual(String color, String badge, String[] iconLines,
                          List<String[]> attackFrames, List<String[]> skillFrames) {
            this.color = color == null ? GREEN : color;
            this.badge = badge == null ? (this.color + "[H]" + RESET) : badge;
            this.iconLines = iconLines == null ? new String[0] : iconLines.clone();
            this.attackFrames = immutableCopy(attackFrames);
            this.skillFrames = immutableCopy(skillFrames);
        }

        public String getColor() {
            return color;
        }

        public String getBadge() {
            return badge;
        }

        public String[] getIconLines() {
            return iconLines.clone();
        }

        public List<String[]> getAttackFrames() {
            return attackFrames;
        }

        public List<String[]> getSkillFrames() {
            return skillFrames;
        }
    }

    public static final class MonsterVisual {
        private final String color;
        private final List<String[]> attackFrames;

        public MonsterVisual(String color, List<String[]> attackFrames) {
            this.color = color == null ? CYAN : color;
            this.attackFrames = immutableCopy(attackFrames);
        }

        public String getColor() {
            return color;
        }

        public List<String[]> getAttackFrames() {
            return attackFrames;
        }
    }
}
