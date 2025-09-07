package military.util;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Centralized logger provider. Uses java.util.logging with a ConsoleHandler.
 * Keep configuration minimal to avoid external dependencies.
 */
public final class Logs {
    private static volatile boolean initialized = false;

    private Logs() { }

    private static void initIfNeeded() {
        if (initialized) return;
        synchronized (Logs.class) {
            if (initialized) return;
            try {
                LogManager.getLogManager().readConfiguration(); // respect system props if set
            } catch (SecurityException | IOException ignored) {
            }
            Logger root = Logger.getLogger("");
            // Ensure a simple console handler is present
            boolean hasConsole = false;
            for (Handler h : root.getHandlers()) {
                if (h instanceof ConsoleHandler) {
                    hasConsole = true;
                    h.setLevel(Level.INFO);
                }
            }
            if (!hasConsole) {
                ConsoleHandler ch = new ConsoleHandler();
                ch.setFormatter(new SimpleFormatter());
                ch.setLevel(Level.INFO);
                root.addHandler(ch);
            }
            root.setLevel(Level.INFO);
            initialized = true;
        }
    }

    /**
     * Retrieve a Logger for the given class with default configuration.
     */
    public static Logger getLogger(Class<?> cls) {
        initIfNeeded();
        return Logger.getLogger(cls.getName());
        
    }
}
