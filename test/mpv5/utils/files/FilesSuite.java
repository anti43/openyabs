
package mpv5.utils.files;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author anti
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({mpv5.utils.files.FileDirectoryHandlerTest.class,mpv5.utils.files.ZipTest.class,mpv5.utils.files.StreamsTest.class,mpv5.utils.files.TableHtmlWriterTest.class,mpv5.utils.files.FileActionHandlerTest.class,mpv5.utils.files.UnZipTest.class,mpv5.utils.files.FileReaderWriterTest.class,mpv5.utils.files.JarFinderTest.class,mpv5.utils.files.TextDatFileTest.class})
public class FilesSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}