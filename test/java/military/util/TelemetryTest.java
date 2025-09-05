package military.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TelemetryTest {

    @Test
    public void initWithDirectoryCreatesLogFileAndWrites() throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory("mm-telemetry-test-");
        try {
            // Initialize telemetry to write logs under tempDir
            Telemetry.initWithDirectory(tempDir, false);
            // Log a message via root logger (Telemetry attaches a FileHandler to root)
            Logger root = Logger.getLogger("");
            root.log(Level.INFO, "TelemetryTest message");
            // Give a tiny bit of time for handler to flush
            Thread.sleep(50);
            Path logFile = tempDir.resolve("military.log");
            Assert.assertTrue("Log file should exist after init", Files.exists(logFile));
            long size = Files.size(logFile);
            Assert.assertTrue("Log file should not be empty after a write", size > 0);
        } finally {
            // Best-effort cleanup of temp directory contents
            try {
                Files.walk(tempDir)
                        .sorted((a,b) -> b.getNameCount() - a.getNameCount())
                        .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignored) {} });
            } catch (Exception ignored) {}
        }
    }
}
