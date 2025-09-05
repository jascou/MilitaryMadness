package military.util;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class TelemetryTest {

    @Test
    public void initWithDirectoryCreatesLogFile() throws Exception {
        Path tempDir = java.nio.file.Files.createTempDirectory("mm-logs-");
        try {
            Telemetry.initWithDirectory(tempDir, true);
            Path logFile = tempDir.resolve("military.log");
            // Allow handler to create the file
            java.util.logging.Logger.getLogger("").info("Test log entry");
            // File should exist (may be created upon first write depending on platform)
            // Wait briefly to allow IO
            Thread.sleep(50L);
            Assert.assertTrue("Log file should exist after init", Files.exists(logFile));
        } finally {
            try { java.nio.file.Files.walk(tempDir).sorted(java.util.Comparator.reverseOrder()).forEach(p -> { try { java.nio.file.Files.deleteIfExists(p); } catch (Exception ignored) {} }); } catch (Exception ignored) {}
        }
    }
}
