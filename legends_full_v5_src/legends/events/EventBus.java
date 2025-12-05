package legends.events;

import java.util.ArrayList;
import java.util.List;

public class EventBus {
    private static final EventBus INSTANCE = new EventBus();

    public static EventBus getInstance() { return INSTANCE; }

    private final List<EventListener<Event>> listeners = new ArrayList<>();

    private EventBus() {}

    public synchronized void register(EventListener<Event> l) {
        if (!listeners.contains(l)) listeners.add(l);
    }

    public synchronized void unregister(EventListener<Event> l) {
        listeners.remove(l);
    }

    @SuppressWarnings("unchecked")
    public synchronized void fire(Event e) {
        // shallow copy to avoid concurrent modification
        List<EventListener<Event>> copy = new ArrayList<>(listeners);
        for (EventListener<Event> l : copy) {
            try { l.onEvent(e); } catch (Throwable ex) { /* ignore listener errors */ }
        }
    }
}
