package legends.items;

public interface Item {
    String getName();
    int getLevel();
    int getPrice();

    default int getSellPrice() {
        int price = getPrice();
        int half = price / 2;
        return Math.max(1, half);
    }
}
