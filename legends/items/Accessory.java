package legends.items;

public class Accessory implements Item {
    private final String name;
    private final int level;
    private final int price;
    // stat bonuses (can be zero)
    private final int strengthBonus;
    private final int dexterityBonus;
    private final int agilityBonus;

    public Accessory(String name, int level, int price, int strBonus, int dexBonus, int agiBonus) {
        this.name = name;
        this.level = level;
        this.price = price;
        this.strengthBonus = strBonus;
        this.dexterityBonus = dexBonus;
        this.agilityBonus = agiBonus;
    }

    @Override
    public String getName() { return name; }

    @Override
    public int getLevel() { return level; }

    @Override
    public int getPrice() { return price; }

    public int getStrengthBonus() { return strengthBonus; }
    public int getDexterityBonus() { return dexterityBonus; }
    public int getAgilityBonus() { return agilityBonus; }

    @Override
    public String toString() {
        return name + " (Lv " + level + ", +STR=" + strengthBonus + ", +DEX=" + dexterityBonus + ", +AGI=" + agilityBonus + ", price=" + price + ")";
    }
}
