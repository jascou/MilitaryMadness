package military.util;

/**
 * Abstraction for playing short sound effects.
 */
public interface SoundPlayer {
    void setPlaySounds(boolean value);
    void playSound(String file);
    void shutdown();
}
