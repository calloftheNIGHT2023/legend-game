package legends;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import legends.io.HeadlessIO;
import legends.battle.BattleEngine;
import legends.model.Hero;
import legends.model.Monster;

public class BattleSmokeTest {
    public static void main(String[] args) {
        HeadlessIO io = new HeadlessIO();
        // Preload many "1" choices so heroes always select normal attack and target 1
        for (int i = 0; i < 200; i++) {
            io.addInput("1");
        }

        List<Hero> heroes = new ArrayList<Hero>();
        heroes.add(Hero.createWarrior("TestWarrior"));

        List<Monster> monsters = Monster.spawnForParty(heroes);

        BattleEngine be = new BattleEngine(heroes, monsters, new Random(42), io);
        boolean heroesWin = be.runBattle();

        // Print captured outputs so we can inspect logs
        for (String line : io.getOutputs()) {
            System.out.print(line);
        }
        System.out.println("Heroes win: " + heroesWin);
    }
}
