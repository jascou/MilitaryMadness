package military.engine.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Very small synchronous event bus for domain events.
 */
public class EventBus {
    private static final EventBus INSTANCE = new EventBus();
    public static EventBus getInstance() { return INSTANCE; }

    private final List<Consumer<GameEvent>> listeners = new CopyOnWriteArrayList<>();

    public void register(Consumer<GameEvent> listener) {
        listeners.add(listener);
    }

    public void unregister(Consumer<GameEvent> listener) {
        listeners.remove(listener);
    }

    public void post(GameEvent event) {
        for (Consumer<GameEvent> l : listeners) {
            try { l.accept(event); } catch (Exception ignore) {}
        }
    }
}
