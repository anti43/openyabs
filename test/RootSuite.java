
import com.ComSuite;
import enoa.EnoaSuite;
import mpv5.Mpv5Suite;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import plugins.PluginsSuite;

/**
 *
 * @author anti
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({Mpv5Suite.class, PluginsSuite.class, EnoaSuite.class, ComSuite.class})
public class RootSuite {

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