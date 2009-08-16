
package mpv5.db.objects;

import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.utils.images.MPIcon;
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
public class FavouriteTest {

    public FavouriteTest() {
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
     * Test of __getUsersids method, of class Favourite.
     */
    @Test
    public void test__getUsersids() {
        System.out.println("__getUsersids");
        Favourite instance = new Favourite();
        int expResult = 0;
        int result = instance.__getUsersids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setUsersids method, of class Favourite.
     */
    @Test
    public void testSetUsersids() {
        System.out.println("setUsersids");
        int userid = 0;
        Favourite instance = new Favourite();
        instance.setUsersids(userid);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getItemsids method, of class Favourite.
     */
    @Test
    public void test__getItemsids() {
        System.out.println("__getItemsids");
        Favourite instance = new Favourite();
        int expResult = 0;
        int result = instance.__getItemsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setItemsids method, of class Favourite.
     */
    @Test
    public void testSetItemsids() {
        System.out.println("setItemsids");
        int itemid = 0;
        Favourite instance = new Favourite();
        instance.setItemsids(itemid);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isFavourite method, of class Favourite.
     */
    @Test
    public void testIsFavourite() {
        System.out.println("isFavourite");
        DatabaseObject dato = null;
        boolean expResult = false;
        boolean result = Favourite.isFavourite(dato);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeFavourite method, of class Favourite.
     */
    @Test
    public void testRemoveFavourite() {
        System.out.println("removeFavourite");
        DatabaseObject dato = null;
        Favourite.removeFavourite(dato);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUserFavourites method, of class Favourite.
     */
    @Test
    public void testGetUserFavourites() {
        System.out.println("getUserFavourites");
        Favourite[] expResult = null;
        Favourite[] result = Favourite.getUserFavourites();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFavContext method, of class Favourite.
     */
    @Test
    public void testGetFavContext() {
        System.out.println("getFavContext");
        Favourite instance = new Favourite();
        Context expResult = null;
        Context result = instance.getFavContext();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of flush method, of class Favourite.
     */
    @Test
    public void testFlush() {
        System.out.println("flush");
        User user = null;
        Favourite.flush(user);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getView method, of class Favourite.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        Favourite instance = new Favourite();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class Favourite.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        Favourite instance = new Favourite();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}