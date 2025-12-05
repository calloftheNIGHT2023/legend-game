package legends.effects;

public class StatusEffect {
    public enum Type {
        POISON,
        BURN,
        STUN,
        ARMOR_PENETRATE,
        SLOW
    }

    private final Type type;
    private int remainingTurns;
    private final int potency; // damage per turn or magnitude

    public StatusEffect(Type type, int turns, int potency) {
        this.type = type;
        this.remainingTurns = Math.max(1, turns);
        this.potency = potency;
    }

    public Type getType() { return type; }
    public int getRemainingTurns() { return remainingTurns; }
    public int getPotency() { return potency; }

    public void tick() { remainingTurns = Math.max(0, remainingTurns - 1); }

    public boolean isExpired() { return remainingTurns <= 0; }
}
