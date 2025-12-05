package legends.items;

public class Weapon implements Item {
    private final String name;
    private final int level;
    private final int price;
    private final int damage;
    private final int hands;

    public Weapon(String name, int level, int price, int damage, int hands) {
        this.name = name;
        this.level = level;
        this.price = price;
        this.damage = damage;
        this.hands = hands;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getPrice() {
        return price;
    }

    public int getDamage() {
        return damage;
    }

    public int getHands() {
        return hands;
    }

    @Override
    public String toString() {
        return name + " (Lv " + level + ", dmg=" + damage + ", hands=" + hands + ", price=" + price + ")";
    }
}
