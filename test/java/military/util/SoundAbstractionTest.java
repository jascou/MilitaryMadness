package military.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SoundAbstractionTest {

    private static class FakeSoundPlayer implements SoundPlayer {
        final List<String> played = new ArrayList<>();
        boolean on = true;

        @Override
        public void setPlaySounds(boolean value) {
            on = value;
        }

        @Override
        public void playSound(String file) {
            if (on) played.add(file);
        }

        @Override
        public void shutdown() { /* no-op */ }
    }

    @Test
    public void testFakeSoundPlayerCapturesCalls() {
        FakeSoundPlayer sp = new FakeSoundPlayer();
        sp.playSound("click.wav");
        sp.playSound("boom.wav");
        Assert.assertEquals(2, sp.played.size());
        Assert.assertTrue(sp.played.contains("click.wav"));
        Assert.assertTrue(sp.played.contains("boom.wav"));

        sp.setPlaySounds(false);
        sp.playSound("ignored.wav");
        Assert.assertEquals("When disabled, no new sounds should be captured", 2, sp.played.size());
    }
}
