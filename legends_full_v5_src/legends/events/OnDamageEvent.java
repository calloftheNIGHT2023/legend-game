package legends.events;

import legends.model.Hero;
import legends.model.Monster;

public class OnDamageEvent implements Event {
    public final Object source; // attacker
    public final Object target; // defender
    public final int amount;

    public OnDamageEvent(Object source, Object target, int amount) {
        this.source = source;
        this.target = target;
        this.amount = amount;
    }
}
