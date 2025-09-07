package military.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Validates that core resources are packaged and loadable via classpath-first ResourceLoader.
 */
public class ResourcePackagingTest {

    @Test
    public void unitsTxtLoadsFromClasspath() throws Exception {
        try (InputStream in = ResourceLoader.openTextFromResources("Units.txt")) {
            Assert.assertNotNull("Units.txt should be found on classpath or filesystem", in);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String firstLine = br.readLine();
                // The exact contents vary, but we expect some non-empty content
                Assert.assertNotNull("Units.txt should not be empty", firstLine);
                Assert.assertTrue("Units.txt should have some text", firstLine.trim().length() > 0);
            }
        }
    }
}
