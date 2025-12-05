package legends.items;

import legends.model.Hero;
import legends.model.Monster;
import legends.effects.StatusEffect;
import legends.io.IO;

public class Consumable implements Item {
    public enum ConsumeType { HEAL, RESTORE_MP, ANTIDOTE, STUN_BOMB, ARMOR_BREAK }

    private final String name;
    private final int level;
    private final int price;
    private final ConsumeType type;
    private final int potency;
    private final boolean usableInBattle;

    public Consumable(String name, int level, int price, ConsumeType type, int potency, boolean usableInBattle) {
        this.name = name;
        this.level = level;
        this.price = price;
        this.type = type;
        this.potency = potency;
        this.usableInBattle = usableInBattle;
    }

    @Override
    public String getName() { return name; }

    @Override
    public int getLevel() { return level; }

    @Override
    public int getPrice() { return price; }

    public boolean isUsableInBattle() { return usableInBattle; }

    public boolean needsTarget() {
        return type == ConsumeType.STUN_BOMB || type == ConsumeType.ARMOR_BREAK;
    }

    public ConsumeType getType() { return type; }

    public boolean apply(Hero user, Monster target, IO io) {
        switch (type) {
            case HEAL:
                int healed = user.heal(potency);
                io.println(user.getName() + " used " + name + " and healed " + healed + " HP.");
                return healed > 0;
            case RESTORE_MP:
                int restored = user.restoreMp(potency);
                io.println(user.getName() + " used " + name + " and restored " + restored + " MP.");
                return restored > 0;
            case ANTIDOTE:
                user.removeStatus(StatusEffect.Type.POISON);
                io.println(user.getName() + " used " + name + " and removed poison.");
                return true;
            case STUN_BOMB:
                if (target == null) {
                    io.println("No target to use " + name);
                    return false;
                }
                target.addStatus(new StatusEffect(StatusEffect.Type.STUN, 1, 0));
                io.println(user.getName() + " used " + name + " to stun " + target.getName());
                return true;
            case ARMOR_BREAK:
                if (target == null) {
                    io.println("No target to use " + name);
                    return false;
                }
                target.addStatus(new StatusEffect(StatusEffect.Type.ARMOR_PENETRATE, 2, potency));
                io.println(user.getName() + " used " + name + " to break armor of " + target.getName());
                return true;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return name + " (Lv " + level + ", " + type + ", potency=" + potency + ", price=" + price + ")";
    }
}
