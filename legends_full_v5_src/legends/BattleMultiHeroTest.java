package legends;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import legends.io.HeadlessIO;
import legends.battle.BattleEngine;
import legends.model.Hero;
import legends.model.Monster;
import legends.items.Consumable;

public class BattleMultiHeroTest {
    public static void main(String[] args) {
        HeadlessIO io = new HeadlessIO();

        // Simplified inputs: feed a large number of "1" so every action = normal attack, target = monster 1.
        for (int i = 0; i < 120; i++) io.addInput("1");

        List<Hero> heroes = new ArrayList<Hero>();
        Hero h1 = Hero.createWarrior("WarriorA");
        Hero h2 = Hero.createSorcerer("SorcererB");
        heroes.add(h1);
        heroes.add(h2);

        // (Consumables omitted for simplified deterministic test scenario)

        List<Monster> monsters = Monster.spawnForParty(heroes);

        BattleEngine be = new BattleEngine(heroes, monsters, new Random(12345), io);
        boolean heroesWin = be.runBattle();

        // also write outputs to a file for environments where stdout may be captured
        java.util.List<String> outs = io.getOutputs();
        StringBuilder sb = new StringBuilder();
        for (String line : outs) sb.append(line);
        sb.append("Heroes win: ").append(heroesWin).append("\n");
        try {
            java.nio.file.Path outPath = java.nio.file.Paths.get("out", "battle_multi_output.txt");
            java.nio.file.Files.createDirectories(outPath.getParent());
            java.nio.file.Files.write(outPath, sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            // fallback to stdout
            for (String line : outs) System.out.print(line);
            System.out.println("Heroes win: " + heroesWin);
        }
    }
}
