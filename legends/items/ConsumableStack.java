package legends.items;

public class ConsumableStack {
    private final Consumable consumable;
    private int count;

    public ConsumableStack(Consumable consumable, int count) {
        this.consumable = consumable;
        this.count = Math.max(0, count);
    }

    public Consumable getConsumable() { return consumable; }

    public int getCount() { return count; }

    public void increment(int n) { if (n > 0) count += n; }

    public boolean decrementOne() {
        if (count <= 0) return false;
        count--;
        return true;
    }

    // Stacking equality: same class, name, type and potency
    public boolean matches(Consumable other) {
        if (other == null) return false;
        Consumable c = this.consumable;
        return c.getName().equals(other.getName())
            && c.getLevel() == other.getLevel();
    }
}
