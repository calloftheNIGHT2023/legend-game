package legends.items;

public class Armor implements Item {
    private final String name;
    private final int level;
    private final int price;
    private final int reduction;

    public Armor(String name, int level, int price, int reduction) {
        this.name = name;
        this.level = level;
        this.price = price;
        this.reduction = reduction;
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

    public int getReduction() {
        return reduction;
    }

    @Override
    public String toString() {
        return name + " (Lv " + level + ", def=" + reduction + ", price=" + price + ")";
    }
}
