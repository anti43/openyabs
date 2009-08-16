
package mpv5.utils;

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
@Suite.SuiteClasses({mpv5.utils.http.HttpSuite.class,mpv5.utils.ooo.OooSuite.class,mpv5.utils.date.DateSuite.class,mpv5.utils.text.TextSuite.class,mpv5.utils.images.ImagesSuite.class,mpv5.utils.xml.XmlSuite.class,mpv5.utils.oasis.OasisSuite.class,mpv5.utils.reflection.ReflectionSuite.class,mpv5.utils.numberformat.NumberformatSuite.class,mpv5.utils.jobs.JobsSuite.class,mpv5.utils.models.ModelsSuite.class,mpv5.utils.pdf.PdfSuite.class,mpv5.utils.html.HtmlSuite.class,mpv5.utils.tables.TablesSuite.class,mpv5.utils.renderer.RendererSuite.class,mpv5.utils.files.FilesSuite.class,mpv5.utils.ui.UiSuite.class,mpv5.utils.export.ExportSuite.class,mpv5.utils.trees.TreesSuite.class,mpv5.utils.arrays.ArraysSuite.class,mpv5.utils.print.PrintSuite.class,mpv5.utils.numbers.NumbersSuite.class})
public class UtilsSuite {

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