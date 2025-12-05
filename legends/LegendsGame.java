package legends;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import legends.world.WorldMap;
import legends.world.TileType;
import legends.party.Party;
import legends.model.Hero;
import legends.model.Monster;
import legends.battle.BattleEngine;
import legends.market.Market;
import legends.util.SaveLoad;
import legends.items.Item;

/**
 * Main game controller for Monster&Hero.
 * Manages the game loop, map navigation, combat encounters, and user interactions.
 * The game features:
 * - Tile-based exploration with danger gradients
 * - Turn-based combat with heroes vs monsters
 * - Random adventure events on common tiles
 * - Market system for buying equipment
 * - Difficulty selection affecting hero regeneration
 */
public class LegendsGame {

    private static final int MAP_SIZE = 8;

    // ANSI color/style constants used by the banner and battle UI
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String ITALIC = "\u001B[3m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";

    private final Scanner in = new Scanner(System.in);
    private final Random rng = new Random();

    private WorldMap map;
    private Party party;
    private boolean savePromptShown = false;
    private int floor = 1;

    public void run() {
        printWelcome();
        initGame();
        gameLoop();
        GLOBAL_IO.println("Thanks for playing Monster&hero!");
    }

    private void printWelcome() {
        // Simple ASCII banner and interactive start menu
        while (true) {
            clearConsole();
            // Print banner with gradient coloring
            // Big ASCII 'Monster&hero' - outline/line style using / \ | _ -
            GLOBAL_IO.println(gradient(" __  __  _____  _   _    _____  _______  ______   _____    ", BOLD + ITALIC));
            GLOBAL_IO.println(gradient("|  \\/  ||  _  || \\ | ||  _ | \\|__   __||____| |  __ \\   ", BOLD + ITALIC));
            GLOBAL_IO.println(gradient("| \\  / || | | ||  \\| || |__     | |   | |__    | |__) |  ", BOLD + ITALIC));
            GLOBAL_IO.println(gradient("| |\\/| || | | || . ` |||____ |   | |   |  __|   |  _  /   ", BOLD + ITALIC));
            GLOBAL_IO.println(gradient("| |  | || |_| || |\\  || ___| |   | |   | |____  | | \\ \\  ", BOLD + ITALIC));
            GLOBAL_IO.println(gradient("|_|  |_| \\___/ |_| \\_||_____/   |_|   |______| |_|  \\_\\ ", BOLD + ITALIC));

            GLOBAL_IO.println(gradient("  START YOUR ADVENTURE  ", BOLD + ITALIC));
            GLOBAL_IO.println(gradient("  Press 1 to begin       ", BOLD + ITALIC));
            GLOBAL_IO.println(gradient("  Explore dungeons,      ", BOLD + ITALIC));
            GLOBAL_IO.println(gradient("  battle monsters, and   ", BOLD + ITALIC));
            GLOBAL_IO.println(gradient("  grow your heroes!      ", BOLD + ITALIC));
            GLOBAL_IO.println(gradient("  Good luck, Brave One!  ", BOLD + ITALIC));

            GLOBAL_IO.println(gradient(" _    _  ______  _____   ____    ", BOLD + ITALIC));
            GLOBAL_IO.println(gradient("| |  | ||  ____||  __ \\ / __ \\  ", BOLD + ITALIC));
            GLOBAL_IO.println(gradient("| |__| || |__   | |__) | |  | || ", BOLD + ITALIC));
            GLOBAL_IO.println(gradient("|  __  ||  __|  |  _  /| |  | || ", BOLD + ITALIC));
            GLOBAL_IO.println(gradient("| |  | || |____ | | \\ \\| |__| |", BOLD + ITALIC));
            GLOBAL_IO.println(gradient("|_|  |_||______||_|  \\_\\\\____/ ", BOLD + ITALIC));

            GLOBAL_IO.println("");
            GLOBAL_IO.println("");
            GLOBAL_IO.println("Enter a number:");
            GLOBAL_IO.println("1) Start Game");
            GLOBAL_IO.println("2) View Instructions");
            GLOBAL_IO.println("3) Exit");
            GLOBAL_IO.print(": ");

            String line = LegendsGame.readLineAndClear(in);
            if (line.equals("1")) {
                return; // start the game
            } else if (line.equals("2")) {
                showInstructions();
            } else if (line.equals("3")) {
                GLOBAL_IO.println("Goodbye!");
                System.exit(0);
            } else {
                GLOBAL_IO.println("Invalid input. Press Enter to continue...");
                LegendsGame.readLineAndClear(in);
            }
        }
    }

    private void showInstructions() {
        clearConsole();
        GLOBAL_IO.println("");
        GLOBAL_IO.println("=== Monster&hero ===");
        GLOBAL_IO.println("");
        GLOBAL_IO.println("Controls:");
        GLOBAL_IO.println("  W/A/S/D - Move up/left/down/right on the map.");
        GLOBAL_IO.println("            Common tiles may trigger random battles.");
        GLOBAL_IO.println("  I       - Show detailed party info (HP/MP, stats, gold, equipment).");
        GLOBAL_IO.println("  M       - Enter the Market if you are standing on a Market tile.");
        GLOBAL_IO.println("  V       - Save the current game progress to disk.");
        GLOBAL_IO.println("  L       - Load the most recent save file (overwrites current progress).");
        GLOBAL_IO.println("  Q       - Quit the game.");
        GLOBAL_IO.println("");
        GLOBAL_IO.println("Tiles:");
        GLOBAL_IO.println("  Inaccessible (red) - cannot be entered.");
        GLOBAL_IO.println("  Market (green)     - buy and equip weapons/armor.");
        GLOBAL_IO.println("  Common (blue)      - normal ground, battles may occur.");
        GLOBAL_IO.println("-------------------------------------");
        GLOBAL_IO.println("Press Enter to return to the main menu...");
        LegendsGame.readLineAndClear(in);
    }

    // Try to clear console for a cleaner 'new page' look. Works on Windows and
    // many UNIX terminals. This is optional and failures are ignored.
    private void clearConsole() {
        try {
            GLOBAL_IO.clear();
        } catch (Exception e) {
            // ignore - if we cannot clear, it's not fatal
        }
    }

    // Public static clear that other classes can call to create a new-page effect.
    public static void clearConsoleStatic() {
        try {
            GLOBAL_IO.clear();
        } catch (Exception e) {
            // ignore
        }
    }

    // Global IO instance used by the game (ConsoleIO by default). This ensures
    // terminal-based display is used; other IO implementations (HeadlessIO)
    // can be swapped for testing or embedding.
    private static final legends.io.IO GLOBAL_IO = new legends.io.ConsoleIO();

    public static legends.io.IO getGlobalIO() {
        return GLOBAL_IO;
    }

    // Read a line using the global IO, then clear the console to create a
    // "new page" effect before the next output. Kept as the old signature for
    // backward compatibility but the Scanner parameter is ignored.
    public static String readLineAndClear(Scanner scanner) {
        String line = "";
        try {
            line = GLOBAL_IO.readLine();
            if (line == null) line = "";
            line = line.trim();
        } catch (Exception e) {
            line = "";
        }
        GLOBAL_IO.clear();
        return line;
    }

    // Return the input string with a left-to-right color gradient applied.
    private String gradient(String text, String style) {
        String[] colors = new String[] { RED, YELLOW, GREEN, CYAN, BLUE, MAGENTA };
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            String color = colors[i % colors.length];
            sb.append(color).append(style).append(ch).append(RESET);
        }
        return sb.toString();
    }

    private void initGame() {
        // Prompt for difficulty selection
        selectDifficulty();
        floor = 1;
        map = WorldMap.generateRandom(MAP_SIZE, MAP_SIZE, rng);
        relocateBossForCurrentFloor();
        // Do not prompt for loading here. The user requested the saved-game
        // prompt to appear while viewing the map, so we defer load until
        // the first map display in the main loop.
        party = createInitialParty();
        int[] start = map.findAnyAccessibleNonBoss();
        party.setPosition(start[0], start[1]);
    }

    /**
     * Prompt user to select difficulty level at game start.
     * EASY mode provides hero regeneration, while NORMAL and HARD do not.
     */
    private void selectDifficulty() {
        GLOBAL_IO.println("");
        GLOBAL_IO.println("=== Select Difficulty ===");
        GLOBAL_IO.println("1) EASY   - Forgiving encounters for new players");
        GLOBAL_IO.println("2) NORMAL - Standard combat challenge");
        GLOBAL_IO.println("3) HARD   - Stronger monsters and harsher battles");
        GLOBAL_IO.println("4) TEST   - Developer mode, all monsters fall in one hit");
        GLOBAL_IO.print("Choose (1-4, default 2): ");
        int choice = readIntInRange(1, 4, 2);
        switch (choice) {
            case 1:
                legends.Config.DIFFICULTY = "EASY";
                GLOBAL_IO.println("Selected: EASY mode");
                break;
            case 3:
                legends.Config.DIFFICULTY = "HARD";
                GLOBAL_IO.println("Selected: HARD mode");
                break;
            case 4:
                legends.Config.DIFFICULTY = "TEST";
                GLOBAL_IO.println("Selected: TEST mode (monsters are defeated in one hit)");
                break;
            default:
                legends.Config.DIFFICULTY = "NORMAL";
                GLOBAL_IO.println("Selected: NORMAL mode");
                break;
        }
        GLOBAL_IO.println("");
    }

    private Party createInitialParty() {
        GLOBAL_IO.print("How many heroes (1-3)? ");
        int count = readIntInRange(1, 3);

        List<Hero> heroes = new ArrayList<Hero>();
        for (int i = 0; i < count; i++) {
            GLOBAL_IO.println("");
            GLOBAL_IO.println("Choose class for hero " + (i + 1) + ":");
            GLOBAL_IO.println("  1) Warrior  - Frontline fighter. High Strength & Agility,");
            GLOBAL_IO.println("                excels at physical attacks and close combat.");
            GLOBAL_IO.println("  2) Sorcerer - Powerful mage. High Dexterity & Agility,");
            GLOBAL_IO.println("                relies on spells and MP for burst damage.");
            GLOBAL_IO.println("  3) Paladin  - Holy knight. Balanced Strength & Dexterity,");
            GLOBAL_IO.println("                durable and versatile in both offense & defense.");
            GLOBAL_IO.print("Your choice (1-3): ");
            int choice = readIntInRange(1, 3);
            GLOBAL_IO.print("Enter hero name: ");
            String name = LegendsGame.readLineAndClear(in);
            if (name.isEmpty()) {
                name = "Hero" + (i + 1);
            }

            Hero h;
            if (choice == 1) {
                h = Hero.createWarrior(name);
            } else if (choice == 2) {
                h = Hero.createSorcerer(name);
            } else {
                h = Hero.createPaladin(name);
            }
            heroes.add(h);
        }
        return new Party(heroes);
    }

    private int readIntInRange(int lo, int hi) {
        return readIntInRange(lo, hi, lo);
    }

    private int readIntInRange(int lo, int hi, int defaultVal) {
        while (true) {
            try {
                String line = LegendsGame.readLineAndClear(in);
                if (line == null || line.isEmpty()) return defaultVal;
                int x = Integer.parseInt(line);
                if (x < lo || x > hi) {
                    throw new NumberFormatException();
                }
                return x;
            } catch (NumberFormatException e) {
                GLOBAL_IO.print("Please enter an integer in [" + lo + "," + hi + "]: ");
            }
        }
    }

    private void gameLoop() {
        boolean running = true;
        while (running) {
            // clear screen at the start of each loop to create a new-page effect
            clearConsole();
            // Before showing the map the first time, check for a saved game and
            // prompt the user to load it (per user request).
            if (!savePromptShown) {
                try {
                    java.nio.file.Path savePath = java.nio.file.Paths.get("save", "savegame.json");
                    if (java.nio.file.Files.exists(savePath)) {
                        GLOBAL_IO.print("Saved game found. Load saved game now while viewing map? (y/n): ");
                        String ans = GLOBAL_IO.readLine();
                        if (ans != null && ans.equalsIgnoreCase("y")) {
                            try {
                                legends.party.Party loaded = legends.util.SaveLoad.loadGame(map);
                                if (loaded != null) {
                                    party = loaded;
                                }
                            } catch (Exception e) {
                                GLOBAL_IO.println("Failed to load saved game: " + e.getMessage());
                                LegendsGame.readLineAndClear(in);
                            }
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
                savePromptShown = true;
            }
            GLOBAL_IO.println("Current Floor: " + floor);
            map.print(party);
            GLOBAL_IO.print("Enter command (W/A/S/D, I, M, C, V=Save, L=Load, Q): ");
            String line = LegendsGame.readLineAndClear(in);
            if (line.isEmpty()) {
                continue;
            }
            char c = Character.toUpperCase(line.charAt(0));
            boolean skipTileProcessing = false;

            switch (c) {
                case 'W':
                    move(-1, 0);
                    break;
                case 'S':
                    move(1, 0);
                    break;
                case 'A':
                    move(0, -1);
                    break;
                case 'D':
                    move(0, 1);
                    break;
                case 'I':
                    showInfo();
                    break;
                case 'C':
                    // Open in-game catalog (图鉴)
                    Catalog.open(party, LegendsGame.getGlobalIO());
                    break;
                case 'M':
                    enterMarketIfAny();
                    break;
                case 'V':
                    try {
                        SaveLoad.saveGame(party, map);
                        GLOBAL_IO.println("Game saved to save/savegame.json.");
                    } catch (Exception e) {
                        GLOBAL_IO.println("Failed to save game: " + e.getMessage());
                    }
                    skipTileProcessing = true;
                    break;
                case 'L':
                    try {
                        Party loadedParty = SaveLoad.loadGame(map);
                        if (loadedParty != null) {
                            party = loadedParty;
                            GLOBAL_IO.println("Saved game loaded.");
                        } else {
                            GLOBAL_IO.println("No saved game found to load.");
                        }
                    } catch (Exception e) {
                        GLOBAL_IO.println("Failed to load saved game: " + e.getMessage());
                    }
                    skipTileProcessing = true;
                    break;
                case 'Q':
                    running = false;
                    break;
                default:
                    GLOBAL_IO.println("Unknown command.");
            }

            if (!running) {
                break;
            }

            if (skipTileProcessing) {
                GLOBAL_IO.println("Press Enter to continue...");
                LegendsGame.readLineAndClear(in);
                continue;
            }

            TileType tile = map.getTileType(party.getRow(), party.getCol());
            if (tile == TileType.BOSS) {
                startBossBattle();
            } else if (tile == TileType.COMMON) {
                // Decide event or battle or nothing
                handleCommonTile();
            }
        }
    }

    /**
     * Handle events on common tiles: may trigger random adventure events or battles.
     * Uses Config probabilities: RANDOM_EVENT_CHANCE, POST_EVENT_BATTLE_CHANCE.
     */
    private void handleCommonTile() {
        double roll = rng.nextDouble();
        if (roll < legends.Config.RANDOM_EVENT_CHANCE) {
            // trigger adventure event
            legends.events.AdventureEvent ev = legends.events.AdventureEventRegistry.randomEvent(rng);
            if (ev != null) {
                GLOBAL_IO.println("");
                ev.resolve(party, GLOBAL_IO, rng);
                GLOBAL_IO.println("");
                if (ev.isForcesBattle()) {
                    GLOBAL_IO.println("The event triggers combat!");
                    maybeStartBattle();
                    return;
                } else {
                    // After a non-forced event we still might start a battle
                    if (rng.nextDouble() < legends.Config.POST_EVENT_BATTLE_CHANCE) {
                        GLOBAL_IO.println("You proceed cautiously, but monsters lurking ahead leap out!");
                        maybeStartBattle();
                    } else {
                        GLOBAL_IO.println("No further danger. You continue exploring safely.");
                    }
                    return;
                }
            }
        }
        // If no event triggered, maybe start a battle using existing logic
        maybeStartBattle();
    }

    private void startBossBattle() {
        GLOBAL_IO.println("You have encountered the Final Boss!");
        // create a challenging boss based on party average level
        int avgLevel = 1;
        java.util.List<Hero> heroes = party.getHeroes();
        if (!heroes.isEmpty()) {
            int sum = 0; for (Hero h : heroes) sum += h.getLevel(); avgLevel = Math.max(1, sum / heroes.size());
        }
        int bossLevel = Math.max(3, avgLevel + 2 + (floor - 1));
        final java.util.List<Monster> monsters = new java.util.ArrayList<Monster>();
        String bossName = (floor == 1) ? "Final Overlord" : getFloorOrdinal(floor) + " Floor Boss";
        monsters.add(new legends.model.FinalBoss(bossName, bossLevel));

        GLOBAL_IO.println("Prepare yourself for the ultimate fight...");
        GLOBAL_IO.println("Press Enter to begin the boss fight.");
        LegendsGame.readLineAndClear(in);

        BattleEngine engine = new BattleEngine(party.getHeroes(), monsters, rng, LegendsGame.getGlobalIO());
        boolean heroesWin = engine.runBattle();
        if (!heroesWin) {
            GLOBAL_IO.println("All heroes have fallen. Game over.");
            System.exit(0);
        } else {
            GLOBAL_IO.println("You defeated " + bossName + "!");
            List<Item> loot = Monster.rewardHeroes(party.getHeroes(), monsters);
            party.stashLoot(loot, GLOBAL_IO);
            // Fully restore all heroes after battle
            for (Hero h : party.getHeroes()) {
                h.fullRecover();
            }
            GLOBAL_IO.println("");
            GLOBAL_IO.println("--- Battle Summary ---");
            party.printInfo(GLOBAL_IO);
            GLOBAL_IO.println("Press Enter to continue...");
            LegendsGame.readLineAndClear(in);
            // remove boss tile so the boss won't respawn
            map.setTileTypeAt(party.getRow(), party.getCol(), TileType.COMMON);
            handlePostBossVictory();
        }
    }

    private void move(int dr, int dc) {
        int nr = party.getRow() + dr;
               int nc = party.getCol() + dc;
        if (!map.inBounds(nr, nc)) {
            GLOBAL_IO.println("You cannot move outside the world.");
            return;
        }
        if (map.getTileType(nr, nc) == TileType.INACCESSIBLE) {
            GLOBAL_IO.println("That tile is inaccessible.");
            return;
        }
        party.setPosition(nr, nc);
    }

    private void showInfo() {
        party.openBackpack(GLOBAL_IO);
    }

    private void enterMarketIfAny() {
        TileType tile = map.getTileType(party.getRow(), party.getCol());
        if (tile != TileType.MARKET) {
            GLOBAL_IO.println("No market here.");
            return;
        }
        Market market = Market.exampleMarket();
        market.enter(party, LegendsGame.getGlobalIO());
    }

    /**
     * Potentially start a battle encounter (30% chance).
     * Monster levels are biased based on proximity to boss using Config thresholds.
     * Offers flee/fight/prepare options before combat starts.
     */
    private void maybeStartBattle() {
        if (rng.nextDouble() > 0.3) {
            return;
        }
        // Compute proximity to boss to determine monster strength bias
        int[] bossPos = map.findBossPosition();
        int pr = party.getRow(), pc = party.getCol();
        int dist;
        if (bossPos[0] >= 0) {
            dist = Math.abs(pr - bossPos[0]) + Math.abs(pc - bossPos[1]);
        } else {
            dist = (MAP_SIZE - 1) * 2; // fallback: far
        }
        int maxDist = (MAP_SIZE - 1) * 2;

        // Compute level bias using values from Config for easier tuning
        int levelBias;
        if (dist <= legends.Config.MONSTER_BIAS_NEAR_DISTANCE) {
            levelBias = legends.Config.MONSTER_BIAS_NEAR;
        } else if (dist <= Math.max(1, (int) Math.floor(maxDist * legends.Config.MONSTER_BIAS_MID_FRACTION))) {
            levelBias = legends.Config.MONSTER_BIAS_MID;
        } else if (dist >= (int) Math.ceil(maxDist * legends.Config.MONSTER_BIAS_FAR_FRACTION)) {
            levelBias = legends.Config.MONSTER_BIAS_FAR;
        } else {
            levelBias = 0;
        }

        // Each new floor raises the baseline monster strength for added challenge
        levelBias += (floor - 1) * 2;

        // Spawn monsters with a level bias based on proximity to boss (config-driven)
        java.util.List<Monster> monsters = Monster.spawnForParty(party.getHeroes(), levelBias);
        // Preparation loop: user can choose to fight, flee, or prepare (use potions)
        while (true) {
            GLOBAL_IO.println("Monsters encountered:");
            // Compute average hero level for danger coloring
            int sumLv = 0; for (Hero h : party.getHeroes()) sumLv += h.getLevel();
            int avgLv = party.getHeroes().isEmpty() ? 1 : Math.max(1, sumLv / party.getHeroes().size());
            for (int i = 0; i < monsters.size(); i++) {
                Monster m = monsters.get(i);
                int diff = m.getLevel() - avgLv;
                String color = GREEN;
                if (diff >= 2) color = RED; else if (diff == 1) color = YELLOW; else color = GREEN;
                GLOBAL_IO.println("  [" + (i + 1) + "] " + color + m.getName() + " (Lv " + m.getLevel() + ")" + RESET + " HP=" + m.getHp() + "/" + m.getMaxHp());
            }
            GLOBAL_IO.println("");
            GLOBAL_IO.println("Choose:");
            GLOBAL_IO.println("  1) Fight");
            GLOBAL_IO.println("  2) Attempt to flee");
            GLOBAL_IO.println("  3) Prepare (use potions)");
            GLOBAL_IO.print(": ");

            String choice = LegendsGame.readLineAndClear(in);
            if (choice.equals("1")) {
                // start battle
                GLOBAL_IO.println("Battle starts!");
                BattleEngine engine = new BattleEngine(party.getHeroes(), monsters, rng, LegendsGame.getGlobalIO());
                boolean heroesWin = engine.runBattle();
                if (!heroesWin) {
                    GLOBAL_IO.println("All heroes have fallen. Game over.");
                    System.exit(0);
                } else {
                    GLOBAL_IO.println("Heroes won the battle!");
                    List<Item> loot = Monster.rewardHeroes(party.getHeroes(), monsters);
                    party.stashLoot(loot, GLOBAL_IO);
                    // Fully restore all heroes after battle
                    for (Hero h : party.getHeroes()) {
                        h.fullRecover();
                    }
                    // Show party info and wait for player to acknowledge before clearing
                    GLOBAL_IO.println("");
                    GLOBAL_IO.println("--- Battle Summary ---");
                    party.printInfo(GLOBAL_IO);
                    GLOBAL_IO.println("Press Enter to continue...");
                    LegendsGame.readLineAndClear(in);
                }
                break;
            } else if (choice.equals("2")) {
                // flee attempt: chance depends on party average agility and number of monsters
                double fleeChance = computeFleeChance(monsters);
                GLOBAL_IO.println(String.format("Attempting to flee... (Chance: %.0f%%)", fleeChance * 100.0));
                if (rng.nextDouble() < fleeChance) {
                    GLOBAL_IO.println("You successfully fled from the battle.");
                    return;
                } else {
                    GLOBAL_IO.println("Flee failed! Battle starts!");
                    BattleEngine engine = new BattleEngine(party.getHeroes(), monsters, rng, LegendsGame.getGlobalIO());
                    boolean heroesWin = engine.runBattle();
                    if (!heroesWin) {
                        GLOBAL_IO.println("All heroes have fallen. Game over.");
                        System.exit(0);
                    } else {
                        GLOBAL_IO.println("Heroes won the battle!");
                        List<Item> loot = Monster.rewardHeroes(party.getHeroes(), monsters);
                        party.stashLoot(loot, GLOBAL_IO);
                        // Fully restore all heroes after battle
                        for (Hero h : party.getHeroes()) {
                            h.fullRecover();
                        }
                        GLOBAL_IO.println("");
                        GLOBAL_IO.println("--- Battle Summary ---");
                        party.printInfo(GLOBAL_IO);
                        GLOBAL_IO.println("Press Enter to continue...");
                        LegendsGame.readLineAndClear(in);
                    }
                    break;
                }
            } else if (choice.equals("3")) {
                // Preparation: allow using potions on heroes
                while (true) {
                    GLOBAL_IO.println("Prepare - choose a hero to manage (0 to go back):");
                    java.util.List<Hero> heroes = party.getHeroes();
                    for (int i = 0; i < heroes.size(); i++) {
                        Hero h = heroes.get(i);
                        GLOBAL_IO.println("  " + (i + 1) + ") " + h.getName() + " HP=" + h.getHp() + "/" + h.getMaxHp() + " MP=" + h.getMp() + " (HP pots=" + h.getHealthPotions() + ", MP pots=" + h.getManaPotions() + ")");
                    }
                    GLOBAL_IO.print(": ");
                    String s = LegendsGame.readLineAndClear(in);
                    int idx = -1;
                    try {
                        idx = Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        idx = -1;
                    }
                    if (idx == 0) {
                        break; // back to main battle menu
                    }
                    if (idx < 1 || idx > heroes.size()) {
                        GLOBAL_IO.println("Invalid hero index.");
                        continue;
                    }
                    Hero h = heroes.get(idx - 1);
                    GLOBAL_IO.println("Selected " + h.getName() + ". Choose:");
                    GLOBAL_IO.println("  1) Use health potion");
                    GLOBAL_IO.println("  2) Use mana potion");
                    GLOBAL_IO.println("  3) Back");
                    GLOBAL_IO.print(": ");
                    String act = LegendsGame.readLineAndClear(in);
                    if (act.equals("1")) {
                        if (h.useHealthPotion()) {
                            GLOBAL_IO.println(h.getName() + " used a health potion. HP is now " + h.getHp() + "/" + h.getMaxHp());
                        } else {
                            GLOBAL_IO.println("No health potions left for " + h.getName());
                        }
                    } else if (act.equals("2")) {
                        if (h.useManaPotion()) {
                            GLOBAL_IO.println(h.getName() + " used a mana potion. MP is now " + h.getMp());
                        } else {
                            GLOBAL_IO.println("No mana potions left for " + h.getName());
                        }
                    } else {
                        // back to hero selection
                    }
                }
            } else {
                GLOBAL_IO.println("Invalid choice.");
            }
        }
    }
    /**
     * Handle player choice after defeating the current boss.
     */
    private void handlePostBossVictory() {
        while (true) {
            GLOBAL_IO.println("");
            GLOBAL_IO.println("What would you like to do next?");
            GLOBAL_IO.println("  1) End the adventure");
            GLOBAL_IO.println("  2) Continue to the next floor");
            GLOBAL_IO.print(": ");
            String choice = LegendsGame.readLineAndClear(in);
            if ("1".equals(choice)) {
                printCongratsBanner();
                GLOBAL_IO.println("Thanks for playing Monster&hero!");
                System.exit(0);
            } else if ("2".equals(choice)) {
                advanceToNextFloor();
                return;
            } else {
                GLOBAL_IO.println("Please enter 1 or 2 to choose.");
            }
        }
    }

    /**
     * Regenerate the world for the next floor, increasing challenge.
     */
    private void advanceToNextFloor() {
        floor++;
        GLOBAL_IO.println("");
        GLOBAL_IO.println("=== Ascending to " + getFloorOrdinal(floor) + " Floor ===");
        map = WorldMap.generateRandom(MAP_SIZE, MAP_SIZE, rng);
        relocateBossForCurrentFloor();
        int[] spawn = map.findAnyAccessibleNonBoss();
        party.setPosition(spawn[0], spawn[1]);
        GLOBAL_IO.println("The world reshapes itself. Enemies feel more dangerous.");
        GLOBAL_IO.println("Press Enter to continue your journey...");
        LegendsGame.readLineAndClear(in);
    }

    /**
     * Position the boss tile based on the current floor, rotating corners each floor.
     */
    private void relocateBossForCurrentFloor() {
        if (map == null) {
            return;
        }
        int rows = map.getRows();
        int cols = map.getCols();
        int targetRow;
        int targetCol;
        switch ((floor - 1) % 4) {
            case 0: // bottom-right
                targetRow = rows - 1;
                targetCol = cols - 1;
                break;
            case 1: // top-left
                targetRow = 0;
                targetCol = 0;
                break;
            case 2: // top-right
                targetRow = 0;
                targetCol = cols - 1;
                break;
            default: // bottom-left
                targetRow = rows - 1;
                targetCol = 0;
                break;
        }
        int[] pos = map.findNearestAccessibleTo(targetRow, targetCol);
        map.moveBossTo(pos[0], pos[1]);
    }

    private String getFloorOrdinal(int value) {
        switch (value) {
            case 1: return "First";
            case 2: return "Second";
            case 3: return "Third";
            case 4: return "Fourth";
            case 5: return "Fifth";
            default:
                return value + getOrdinalSuffix(value);
        }
    }

    private String getOrdinalSuffix(int value) {
        int mod100 = value % 100;
        if (mod100 >= 11 && mod100 <= 13) {
            return "th";
        }
        switch (value % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    /**
     * Print celebratory banner when the player chooses to end the adventure.
     */
    private void printCongratsBanner() {
        String[] lines = new String[] {
            "  ██████╗  ██████╗ ███╗   ██╗ ██████╗ ██████╗  █████╗ ████████╗ ██╗   ██╗██╗     █████╗ ████████╗██║ ██████╗ ███╗   ██╗",
            " ██╔════╝ ██╔═══██╗████╗  ██║██╔════╝██╔══██╗ ██╔══██╗╚══██╔══╝ ██║   ██║██║    ██╔══██╗╚══██╔══╝██║██╔═══██╗████╗  ██║",
            " ██║      ██║   ██║██╔██╗ ██║██║ ███╗██████╔╝ ███████║   ██║    ██║   ██║██║    ███████║   ██║   ██║██║   ██║██╔██╗ ██║",
            " ██║      ██║   ██║██║╚██╗██║██║  ██║██╔══██╗ ██╔══██║   ██║    ██║   ██║██║    ██╔══██║   ██║   ██║██║   ██║██║╚██╗██║",
            " ╚██████╔╝╚██████╔╝██║ ╚████║╚██████╗██║  ██║ ██║  ██║   ██║     ╚████╔╝ ██████╔██║  ██║   ██║   ██║╚██████╔╝██║ ╚████║",
            "  ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝╚═╝  ╚═╝ ╚═╝  ╚═╝   ╚═╝      ╚═══╝  ╚═════╝ ╚═╝╚═╝    ╚═╝   ╚═╝ ╚═════╝      ╚═══╝"
        };
        GLOBAL_IO.println("");
        for (String line : lines) {
            GLOBAL_IO.println(gradient(line, BOLD));
        }
        GLOBAL_IO.println("");
    }

    /**
     * Compute flee chance based on party average agility and monster count.
     * Formula: base (15%) + agility factor - per-monster penalty (15% each)
     * Clamped to [5%, 95%] range for balance.
     */
    private double computeFleeChance(java.util.List<Monster> monsters) {
        java.util.List<Hero> heroes = party.getHeroes();
        if (heroes == null || heroes.isEmpty()) {
            return 0.05; // very low chance if no heroes (shouldn't happen)
        }
        double sumAg = 0.0;
        for (Hero h : heroes) {
            sumAg += h.getAgility();
        }
        double avgAg = sumAg / heroes.size();

        // Normalize agility into a factor (e.g., avgAg around 10-25 maps to 0.0-0.6)
        double agFactor = avgAg / 40.0; // conservative scaling

        // Each extra monster reduces chance
        double monsterPenalty = monsters.size() * 0.15;

        double base = 0.15;
        double chance = base + agFactor - monsterPenalty;

        if (chance < 0.05) chance = 0.05;
        if (chance > 0.95) chance = 0.95;
        return chance;
    }
}
