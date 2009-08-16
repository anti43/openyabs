
package mpv5.ui;

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
@Suite.SuiteClasses({mpv5.ui.misc.MiscSuite.class,mpv5.ui.menus.MenusSuite.class,mpv5.ui.toolbars.ToolbarsSuite.class,mpv5.ui.panels.PanelsSuite.class,mpv5.ui.frames.FramesSuite.class,mpv5.ui.beans.BeansSuite.class,mpv5.ui.dialogs.DialogsSuite.class,mpv5.ui.popups.PopupsSuite.class})
public class UiSuite {

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