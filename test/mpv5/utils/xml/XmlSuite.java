
package mpv5.utils.xml;

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
@Suite.SuiteClasses({mpv5.utils.xml.XMLReaderTest.class,mpv5.utils.xml.XMLRpcClientTest.class,mpv5.utils.xml.XMLWriterTest.class})
public class XmlSuite {

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