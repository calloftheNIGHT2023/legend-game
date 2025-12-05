package legends;

/**
 * Centralized tuning constants for game balance and difficulty.
 */
public final class Config {
    private Config() {}

    // Experience curve: required EXP per level = level * XP_PER_LEVEL_FACTOR
    public static final int XP_PER_LEVEL_FACTOR = 6;

    // EXP reward multiplier is unused by the new fixed reward rules but kept for compatibility
    public static final int XP_REWARD_MULTIPLIER = 2;

    // Maximum allowed monster level above party max level
    public static final int MAX_MONSTER_LEVEL_DELTA = 3;

    // Bias values applied to monster spawn level based on proximity to boss
    // Stronger contrast: boss-adjacent areas tougher, start areas weaker
    public static final int MONSTER_BIAS_NEAR = 3;
    public static final int MONSTER_BIAS_MID = 1;
    public static final int MONSTER_BIAS_FAR = -2;
    // Distance in tiles considered "near" the boss
    public static final int MONSTER_BIAS_NEAR_DISTANCE = 3;
    // Fractions of max distance for mid/far thresholds
    public static final double MONSTER_BIAS_MID_FRACTION = 0.4;
    public static final double MONSTER_BIAS_FAR_FRACTION = 0.7;

    // Danger display thresholds (closeness to boss)
    // Make low/high thresholds a bit more spread so map colors reflect stronger ramps
    public static final double DANGER_THRESHOLD_LOW = 0.25;
    public static final double DANGER_THRESHOLD_HIGH = 0.75;

    // Autosave toggle
    public static final boolean AUTOSAVE_ENABLED = false;

    // Difficulty setting: "EASY", "NORMAL", "HARD", "TEST"
    public static String DIFFICULTY = "NORMAL";

    // Chance on a COMMON tile to trigger an adventure event instead of battle (0-1)
    public static final double RANDOM_EVENT_CHANCE = 0.15;
    // If an event forces battle, battle starts immediately; otherwise after a normal event we still may start a battle.
    // Chance to still start a battle AFTER a non-forced event.
    public static final double POST_EVENT_BATTLE_CHANCE = 0.30;

}
