package legends.events;

public interface EventListener<E extends Event> {
    void onEvent(E e);
}
