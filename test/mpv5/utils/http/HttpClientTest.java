
package mpv5.utils.http;

import org.apache.http.HttpResponse;
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
public class HttpClientTest {

    public HttpClientTest() {
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
     * Test of request method, of class HttpClient.
     */
    @Test
    public void testRequest() throws Exception {
        System.out.println("request");
        String srequest = "";
        HttpClient instance = null;
        HttpResponse expResult = null;
        HttpResponse result = instance.request(srequest);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}