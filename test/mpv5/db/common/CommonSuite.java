
package mpv5.db.common;

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
@Suite.SuiteClasses({mpv5.db.common.QueryHandlerTest.class,mpv5.db.common.QueryCriteriaTest.class,mpv5.db.common.DatabaseInstallationTest.class,mpv5.db.common.DatabaseObjectModifierTest.class,mpv5.db.common.QueryDataTest.class,mpv5.db.common.DatabaseConnectionTest.class,mpv5.db.common.ConnectionTypeHandlerTest.class,mpv5.db.common.DatabaseSearchTest.class,mpv5.db.common.ReturnValueTest.class,mpv5.db.common.DatabaseObjectLockTest.class,mpv5.db.common.ContextTest.class,mpv5.db.common.DatabaseObjectTest.class,mpv5.db.common.SaveStringTest.class,mpv5.db.common.FormattableTest.class})
public class CommonSuite {

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