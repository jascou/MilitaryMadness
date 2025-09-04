package military.engine.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Very small synchronous event bus for domain events.
 * Threading: callbacks execute on the posting thread; consumers must marshal to EDT if needed.
 */
public class EventBus {
    private static final EventBus INSTANCE = new EventBus();

    /**
     * @return global singleton bus instance for simplicity in current architecture.
     */
    public static EventBus getInstance() { return INSTANCE; }

    private final List<Consumer<GameEvent>> listeners = new CopyOnWriteArrayList<>();

    /**
     * Register a listener for all GameEvent instances.
     */
    public void register(Consumer<GameEvent> listener) {
        listeners.add(listener);
    }

    /**
     * Remove a previously registered listener.
     */
    public void unregister(Consumer<GameEvent> listener) {
        listeners.remove(listener);
    }

    /**
     * Post a domain event to all listeners synchronously.
     */
    public void post(GameEvent event) {
        for (Consumer<GameEvent> l : listeners) {
            try {
                l.accept(event);
            } catch (Exception ex) {
                java.util.logging.Logger log = military.util.Logs.getLogger(EventBus.class);
                log.fine("Listener threw during event dispatch: " + ex.toString());
            }
        }
    }
}
