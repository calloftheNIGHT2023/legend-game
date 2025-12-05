package legends.battle;

public interface Fighter {
    boolean isAlive();
    void takeDamage(int dmg);
    String getName();
    double getDodgeChance();
}
