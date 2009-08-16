
package mpv5.ui.beans;

import mpv5.db.common.Context;
import mpv5.utils.models.MPComboBoxModelItem;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author anti
 */
public class MPCBSelectionChangeReceiverTest {

    public MPCBSelectionChangeReceiverTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of changeSelection method, of class MPCBSelectionChangeReceiver.
     */
    @Test
    public void testChangeSelection() {
        System.out.println("changeSelection");
        MPComboBoxModelItem to = null;
        Context c = null;
        MPCBSelectionChangeReceiver instance = new MPCBSelectionChangeReceiverImpl();
        instance.changeSelection(to, c);
        
        fail("The test case is a prototype.");
    }

    public class MPCBSelectionChangeReceiverImpl  implements MPCBSelectionChangeReceiver {

        public void changeSelection(MPComboBoxModelItem to, Context c) {
        }
    }

}