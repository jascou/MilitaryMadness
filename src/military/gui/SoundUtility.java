package military.gui;

import military.util.Logs;
import military.util.SoundPlayer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe sound player using a BlockingQueue and a dedicated consumer loop.
 * UI dialogs are not used here; errors are logged. UI surfaces messages at boundaries.
 */
public class SoundUtility implements Runnable, SoundPlayer {

    private static final Logger LOGGER = Logs.getLogger(SoundUtility.class);

    private volatile boolean running = true;
    private volatile static SoundUtility instance = null;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private volatile boolean playSounds = true;

    public static SoundUtility getInstance() {
        if (instance == null) {
            synchronized (SoundUtility.class) {
                if (instance == null) { // Double-Check!
                    instance = new SoundUtility();
                }
            }
        }
        return instance;
    }

    private SoundUtility() { }

    @Override
    public void setPlaySounds(boolean value) {
        playSounds = value;
    }

    private boolean getPlaySounds() {
        return playSounds;
    }

    private void playOne(String path) {
        if (!getPlaySounds() || path == null) {
            return;
        }
        File soundFile = new File(path);
        Clip clip = null;
        try (AudioInputStream source = AudioSystem.getAudioInputStream(soundFile)) {
            DataLine.Info clipInfo = new DataLine.Info(Clip.class, source.getFormat());
            if (AudioSystem.isLineSupported(clipInfo)) {
                clip = (Clip) AudioSystem.getLine(clipInfo);
                clip.open(source);
                clip.loop(0);
            }
        } catch (UnsupportedAudioFileException e) {
            LOGGER.log(Level.WARNING, "Unsupported audio file: {0}", path);
        } catch (LineUnavailableException e) {
            LOGGER.log(Level.WARNING, "Audio line unavailable for: {0}", path);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "I/O error creating clip for {0}: {1}", new Object[]{path, e.getMessage()});
        } finally {
            if (clip != null) {
                try {
                    if (clip.isActive()) {
                        clip.stop();
                    }
                    if (clip.isOpen()) {
                        clip.close();
                    }
                } catch (Exception ignore) { }
            }
        }
    }

    @Override
    public void playSound(String file) {
        String path = military.Config.soundsDir().resolve(file).toString();
        queue.offer(path);
    }

    @Override
    public void shutdown() {
        running = false;
        Thread.currentThread().interrupt();
    }

    @Override
    public void run() {
        while (running) {
            try {
                String next = queue.take(); // blocks
                playOne(next);
            } catch (InterruptedException ie) {
                // allow exit when shutdown sets running=false
                if (!running) {
                    break;
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Unexpected error in sound thread: {0}", ex.toString());
            }
        }
        // Drain any remaining without blocking, just in case
        queue.forEach(this::playOne);
        queue.clear();
    }
}
