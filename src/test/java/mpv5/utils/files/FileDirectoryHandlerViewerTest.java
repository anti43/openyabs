package mpv5.utils.files;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * The command line of the PDF viewer set in External programs (issue #147)
 */
public class FileDirectoryHandlerViewerTest {

    private final File pdf = new File("/tmp/a b/x.pdf");
    private final String path = pdf.getAbsolutePath();

    @Test
    public void programOnlyGetsThePath() {
        assertEquals(Arrays.asList("evince", path), FileDirectoryHandler.viewerCommand("evince", pdf));
    }

    @Test
    public void argumentsAreKept() {
        assertEquals(Arrays.asList("okular", "--unique", path), FileDirectoryHandler.viewerCommand(" okular  --unique ", pdf));
    }

    @Test
    public void placeholderIsReplaced() {
        assertEquals(Arrays.asList("C:\\Program Files\\V\\v.exe", "/open", path, "/x"),
                FileDirectoryHandler.viewerCommand("\"C:\\Program Files\\V\\v.exe\" /open {file} /x", pdf));
    }
}
