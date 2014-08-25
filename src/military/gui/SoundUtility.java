package military.gui;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SoundUtility implements Runnable {

    private boolean running = true;
    private volatile static SoundUtility instance = null;
    private ArrayList fileToPlay = new ArrayList();
    private boolean playSounds = true;

    public static SoundUtility getInstance() {
        if (instance == null) {
            synchronized (SoundUtility.class) {
                if (instance == null) // Double-Check!
                {
                    instance = new SoundUtility();
                }
            }
        }
        return instance;
    }

    private SoundUtility() {
    }

    public void setPlaySounds(boolean value) {
        playSounds = value;
    }

    private boolean getPlaySounds() {
        return playSounds;
    }

    private void playSound() {
        if (!getPlaySounds()) {
            return;
        }

        String next = getNextFileToPlay();
        while (next != null) {
            File soundFile = new File(next);
            try {
                Clip clip = null;                    // The sound clip

                AudioInputStream source = AudioSystem.getAudioInputStream(soundFile);
                DataLine.Info clipInfo = new DataLine.Info(Clip.class, source.getFormat());
                if (AudioSystem.isLineSupported(clipInfo)) {
                    // Create a local clip to avoid discarding the old clip
                    Clip newClip = (Clip) AudioSystem.getLine(clipInfo);   // Create the clip
                    newClip.open(source);

                    // Deal with previous clip
                    if (clip != null) {
                        if (clip.isActive()) // If it's active
                        {
                            clip.stop();                      // ...stop it
                        }
                        if (clip.isOpen()) // If it's open...
                        {
                            clip.close();                     // ...close it.
                        }
                    }
                    clip = newClip;                       // We have a clip, so discard old
                } else {
                }

                clip.loop(0);
            } catch (UnsupportedAudioFileException e) {
                JOptionPane.showMessageDialog(null, "File not supported",
                        "Unsupported File Type", JOptionPane.WARNING_MESSAGE);
            } catch (LineUnavailableException e) {
                JOptionPane.showMessageDialog(null, "Clip not available", "Clip Error",
                        JOptionPane.WARNING_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "I/O error creating clip: " + e.getMessage(), "Clip Error",
                        JOptionPane.WARNING_MESSAGE);
            }
            next = getNextFileToPlay();
        }
    }

    public synchronized void playSound(String file) {
        addFileToPlay("sounds\\" + file);
        notify();
    }

    public void run() {

        while (isRunning()) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            playSound();
        }
    }

    private boolean isRunning() {
        return running;
    }

    public void setRunning(boolean runningIn) {
        running = runningIn;
    }

    private String getNextFileToPlay() {
        if (fileToPlay.size() == 0) {
            return null;
        }
        String next = (String) fileToPlay.get(0);
        fileToPlay.remove(0);
        return next;
    }

    private void addFileToPlay(String newFileToPlay) {
        fileToPlay.add(newFileToPlay);
        //       LoggingManager.logInfo("SOUNDS: " + fileToPlay.size());
    }

}
