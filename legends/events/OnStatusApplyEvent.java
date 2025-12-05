package legends.events;

import legends.effects.StatusEffect;

public class OnStatusApplyEvent implements Event {
    public final Object source;
    public final Object target;
    public final StatusEffect status;

    public OnStatusApplyEvent(Object source, Object target, StatusEffect status) {
        this.source = source;
        this.target = target;
        this.status = status;
    }
}
