
package mpv5.utils.models;

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
@Suite.SuiteClasses({mpv5.utils.models.MPComboBoxModelItemTest.class,mpv5.utils.models.MPTreeModelTest.class,mpv5.utils.models.MPTableModelRowTest.class,mpv5.utils.models.MPTableModelTest.class,mpv5.utils.models.hn.HnSuite.class,mpv5.utils.models.ImportTableModelTest.class,mpv5.utils.models.MPComboboxModelTest.class})
public class ModelsSuite {

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