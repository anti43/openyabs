package mpv5.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalFilesTest {

    @Test
    void summarizesAndWipesTheContent(@TempDir Path dir) throws Exception {
        Files.write(dir.resolve("a.pdf"), new byte[100]);
        Files.write(dir.resolve("b.pdf"), new byte[50]);
        Files.createDirectories(dir.resolve("sub"));
        Files.write(dir.resolve("sub/t.odt"), new byte[10]);
        LocalFiles.Summary s = LocalFiles.summarize(dir.toFile());
        assertEquals(3, s.files);
        assertEquals(160, s.bytes);
        assertEquals(2, (int) s.kinds.get("pdf"));
        assertEquals(0, LocalFiles.wipe(dir.toFile()));
        assertTrue(dir.toFile().isDirectory());
        assertEquals(0, dir.toFile().listFiles().length);
        assertEquals("1.5 KB", LocalFiles.size(1536).replace(',', '.'));
    }
}
