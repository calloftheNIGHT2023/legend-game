package legends.events;

public class OnTurnStartEvent implements Event {
    public final Object actor; // Hero or Monster
    public OnTurnStartEvent(Object actor) { this.actor = actor; }
}
