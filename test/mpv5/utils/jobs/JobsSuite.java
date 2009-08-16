
package mpv5.utils.jobs;

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
@Suite.SuiteClasses({mpv5.utils.jobs.WatcherTest.class,mpv5.utils.jobs.WaiterTest.class,mpv5.utils.jobs.JobTest.class,mpv5.utils.jobs.WaitableTest.class,mpv5.utils.jobs.BackGroundJobTest.class})
public class JobsSuite {

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