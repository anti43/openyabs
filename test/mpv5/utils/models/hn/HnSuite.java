
package mpv5.utils.models.hn;

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
@Suite.SuiteClasses({mpv5.utils.models.hn.ProfitModelTest.class,mpv5.utils.models.hn.TaxModelTest.class,mpv5.utils.models.hn.AccountCalcModelTest.class,mpv5.utils.models.hn.DateComboBoxModelTest.class,mpv5.utils.models.hn.StartEndDaysTest.class,mpv5.utils.models.hn.HtmlFormRendererTest.class})
public class HnSuite {

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