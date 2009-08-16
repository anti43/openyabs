
package mpv5.handler;

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
@Suite.SuiteClasses({mpv5.handler.SDBObjectGeneratorTest.class,mpv5.handler.FormFieldsHandlerTest.class,mpv5.handler.VariablesHandlerTest.class,mpv5.handler.FormatHandlerTest.class,mpv5.handler.MPEnumTest.class})
public class HandlerSuite {

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