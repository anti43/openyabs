
package mpv5.utils.renderer;

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
@Suite.SuiteClasses({mpv5.utils.renderer.LazyCellEditorTest.class,mpv5.utils.renderer.ComboBoxRendererForTooltipTest.class,mpv5.utils.renderer.CellRendererWithMPComboBoxTest.class,mpv5.utils.renderer.TableCellRendererForDezimalTest.class})
public class RendererSuite {

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