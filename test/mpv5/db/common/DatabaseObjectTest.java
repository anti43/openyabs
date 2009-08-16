
package mpv5.db.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import javax.swing.JComponent;
import mpv5.handler.SimpleDatabaseObject;
import mpv5.ui.panels.DataPanel;
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
public class DatabaseObjectTest {

    public DatabaseObjectTest() {
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
     * Test of cacheObjects method, of class DatabaseObject.
     */
    @Test
    public void testCacheObjects_0args() {
        System.out.println("cacheObjects");
        DatabaseObject.cacheObjects();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of cacheObjects method, of class DatabaseObject.
     */
    @Test
    public void testCacheObjects_ContextArr() {
        System.out.println("cacheObjects");
        Context[] contextArray = null;
        DatabaseObject.cacheObjects(contextArray);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getCName method, of class DatabaseObject.
     */
    @Test
    public void test__getCName() {
        System.out.println("__getCName");
        DatabaseObject instance = new DatabaseObjectImpl();
        String expResult = "";
        String result = instance.__getCName();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of avoidNulls method, of class DatabaseObject.
     */
    @Test
    public void testAvoidNulls() {
        System.out.println("avoidNulls");
        DatabaseObject instance = new DatabaseObjectImpl();
        instance.avoidNulls();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of ensureUniqueness method, of class DatabaseObject.
     */
    @Test
    public void testEnsureUniqueness() {
        System.out.println("ensureUniqueness");
        DatabaseObject instance = new DatabaseObjectImpl();
        instance.ensureUniqueness();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCName method, of class DatabaseObject.
     */
    @Test
    public void testSetCName() {
        System.out.println("setCName");
        String name = "";
        DatabaseObject instance = new DatabaseObjectImpl();
        instance.setCName(name);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getView method, of class DatabaseObject.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        DatabaseObject instance = new DatabaseObjectImpl();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class DatabaseObject.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        DatabaseObject instance = new DatabaseObjectImpl();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of clone method, of class DatabaseObject.
     */
    @Test
    public void testClone() {
        System.out.println("clone");
        DatabaseObject instance = new DatabaseObjectImpl();
        DatabaseObject expResult = null;
        DatabaseObject result = instance.clone();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getType method, of class DatabaseObject.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        DatabaseObject instance = new DatabaseObjectImpl();
        String expResult = "";
        String result = instance.getType();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getContext method, of class DatabaseObject.
     */
    @Test
    public void testGetContext() {
        System.out.println("getContext");
        DatabaseObject instance = new DatabaseObjectImpl();
        Context expResult = null;
        Context result = instance.getContext();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIDS method, of class DatabaseObject.
     */
    @Test
    public void test__getIDS() {
        System.out.println("__getIDS");
        DatabaseObject instance = new DatabaseObjectImpl();
        Integer expResult = null;
        Integer result = instance.__getIDS();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIDS method, of class DatabaseObject.
     */
    @Test
    public void testSetIDS() {
        System.out.println("setIDS");
        int ids = 0;
        DatabaseObject instance = new DatabaseObjectImpl();
        instance.setIDS(ids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isExisting method, of class DatabaseObject.
     */
    @Test
    public void testIsExisting() {
        System.out.println("isExisting");
        DatabaseObject instance = new DatabaseObjectImpl();
        boolean expResult = false;
        boolean result = instance.isExisting();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setVars method, of class DatabaseObject.
     */
    @Test
    public void testSetVars() {
        System.out.println("setVars");
        DatabaseObject instance = new DatabaseObjectImpl();
        ArrayList expResult = null;
        ArrayList result = instance.setVars();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVars method, of class DatabaseObject.
     */
    @Test
    public void testGetVars() {
        System.out.println("getVars");
        DatabaseObject instance = new DatabaseObjectImpl();
        ArrayList expResult = null;
        ArrayList result = instance.getVars();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of save method, of class DatabaseObject.
     */
    @Test
    public void testSave_0args() {
        System.out.println("save");
        DatabaseObject instance = new DatabaseObjectImpl();
        boolean expResult = false;
        boolean result = instance.save();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of save method, of class DatabaseObject.
     */
    @Test
    public void testSave_boolean() {
        System.out.println("save");
        boolean silent = false;
        DatabaseObject instance = new DatabaseObjectImpl();
        boolean expResult = false;
        boolean result = instance.save(silent);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveImport method, of class DatabaseObject.
     */
    @Test
    public void testSaveImport() {
        System.out.println("saveImport");
        DatabaseObject instance = new DatabaseObjectImpl();
        boolean expResult = false;
        boolean result = instance.saveImport();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of reset method, of class DatabaseObject.
     */
    @Test
    public void testReset() {
        System.out.println("reset");
        DatabaseObject instance = new DatabaseObjectImpl();
        boolean expResult = false;
        boolean result = instance.reset();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class DatabaseObject.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        DatabaseObject instance = new DatabaseObjectImpl();
        boolean expResult = false;
        boolean result = instance.delete();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of lock method, of class DatabaseObject.
     */
    @Test
    public void testLock() {
        System.out.println("lock");
        DatabaseObject instance = new DatabaseObjectImpl();
        boolean expResult = false;
        boolean result = instance.lock();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of release method, of class DatabaseObject.
     */
    @Test
    public void testRelease() {
        System.out.println("release");
        DatabaseObject instance = new DatabaseObjectImpl();
        instance.release();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDbIdentity method, of class DatabaseObject.
     */
    @Test
    public void testGetDbIdentity() {
        System.out.println("getDbIdentity");
        DatabaseObject instance = new DatabaseObjectImpl();
        String expResult = "";
        String result = instance.getDbIdentity();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPanelData method, of class DatabaseObject.
     */
    @Test
    public void testGetPanelData() {
        System.out.println("getPanelData");
        DataPanel source = null;
        DatabaseObject instance = new DatabaseObjectImpl();
        instance.getPanelData(source);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setPanelData method, of class DatabaseObject.
     */
    @Test
    public void testSetPanelData() {
        System.out.println("setPanelData");
        DataPanel target = null;
        DatabaseObject instance = new DatabaseObjectImpl();
        instance.setPanelData(target);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValues method, of class DatabaseObject.
     */
    @Test
    public void testGetValues() {
        System.out.println("getValues");
        DatabaseObject instance = new DatabaseObjectImpl();
        ArrayList expResult = null;
        ArrayList result = instance.getValues();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValues3 method, of class DatabaseObject.
     */
    @Test
    public void testGetValues3() {
        System.out.println("getValues3");
        DatabaseObject instance = new DatabaseObjectImpl();
        ArrayList expResult = null;
        ArrayList result = instance.getValues3();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValues2 method, of class DatabaseObject.
     */
    @Test
    public void testGetValues2() {
        System.out.println("getValues2");
        DatabaseObject instance = new DatabaseObjectImpl();
        ArrayList expResult = null;
        ArrayList result = instance.getValues2();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getObject method, of class DatabaseObject.
     */
    @Test
    public void testGetObject_Context_int() throws Exception {
        System.out.println("getObject");
        Context context = null;
        int id = 0;
        DatabaseObject expResult = null;
        DatabaseObject result = DatabaseObject.getObject(context, id);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getObject method, of class DatabaseObject.
     */
    @Test
    public void testGetObject_Context_String() throws Exception {
        System.out.println("getObject");
        Context context = null;
        String cname = "";
        DatabaseObject expResult = null;
        DatabaseObject result = DatabaseObject.getObject(context, cname);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getObject method, of class DatabaseObject.
     */
    @Test
    public void testGetObject_Context() {
        System.out.println("getObject");
        Context context = null;
        DatabaseObject expResult = null;
        DatabaseObject result = DatabaseObject.getObject(context);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getObjects method, of class DatabaseObject.
     */
    @Test
    public void testGetObjects_Context() throws Exception {
        System.out.println("getObjects");
        Context context = null;
        ArrayList expResult = null;
        ArrayList result = DatabaseObject.getObjects(context);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getObjects method, of class DatabaseObject.
     */
    @Test
    public void testGetObjects_Context_boolean() throws Exception {
        System.out.println("getObjects");
        Context context = null;
        boolean withCached = false;
        ArrayList expResult = null;
        ArrayList result = DatabaseObject.getObjects(context, withCached);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getObjects method, of class DatabaseObject.
     */
    @Test
    public void testGetObjects_Context_QueryCriteria() throws Exception {
        System.out.println("getObjects");
        Context context = null;
        QueryCriteria criterias = null;
        ArrayList expResult = null;
        ArrayList result = DatabaseObject.getObjects(context, criterias);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getObjects method, of class DatabaseObject.
     */
    @Test
    public void testGetObjects_GenericType_QueryCriteria() throws Exception {
        System.out.println("getObjects");
        DatabaseObject template = null;
        QueryCriteria criterias = null;
        ArrayList expResult = null;
        ArrayList result = DatabaseObject.getObjects(template, criterias);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getReferencedObjects method, of class DatabaseObject.
     */
    @Test
    public void testGetReferencedObjects_3args() throws Exception {
        System.out.println("getReferencedObjects");
        DatabaseObject dataOwner = null;
        Context inReference = null;
        DatabaseObject targetType = null;
        ArrayList expResult = null;
        ArrayList result = DatabaseObject.getReferencedObjects(dataOwner, inReference, targetType);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getReferencedObjects method, of class DatabaseObject.
     */
    @Test
    public void testGetReferencedObjects_GenericType_Context() throws Exception {
        System.out.println("getReferencedObjects");
        DatabaseObject dataOwner = null;
        Context inReference = null;
        ArrayList expResult = null;
        ArrayList result = DatabaseObject.getReferencedObjects(dataOwner, inReference);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of fetchDataOf method, of class DatabaseObject.
     */
    @Test
    public void testFetchDataOf_int() throws Exception {
        System.out.println("fetchDataOf");
        int id = 0;
        DatabaseObject instance = new DatabaseObjectImpl();
        boolean expResult = false;
        boolean result = instance.fetchDataOf(id);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIdOf method, of class DatabaseObject.
     */
    @Test
    public void testGetIdOf() throws Exception {
        System.out.println("getIdOf");
        String cname = "";
        DatabaseObject instance = new DatabaseObjectImpl();
        Integer expResult = null;
        Integer result = instance.getIdOf(cname);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of fetchDataOf method, of class DatabaseObject.
     */
    @Test
    public void testFetchDataOf_String() {
        System.out.println("fetchDataOf");
        String cname = "";
        DatabaseObject instance = new DatabaseObjectImpl();
        boolean expResult = false;
        boolean result = instance.fetchDataOf(cname);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class DatabaseObject.
     */
    @Test
    public void testParse_Hashtable() throws Exception {
        System.out.println("parse");
        Hashtable<String, Object> toHashTable = null;
        DatabaseObject instance = new DatabaseObjectImpl();
        instance.parse(toHashTable);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class DatabaseObject.
     */
    @Test
    public void testParse_SimpleDatabaseObject() throws Exception {
        System.out.println("parse");
        SimpleDatabaseObject sdo = null;
        DatabaseObject instance = new DatabaseObjectImpl();
        instance.parse(sdo);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getGroupsids method, of class DatabaseObject.
     */
    @Test
    public void test__getGroupsids() {
        System.out.println("__getGroupsids");
        DatabaseObject instance = new DatabaseObjectImpl();
        int expResult = 0;
        int result = instance.__getGroupsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setGroupsids method, of class DatabaseObject.
     */
    @Test
    public void testSetGroupsids() {
        System.out.println("setGroupsids");
        int groupsids = 0;
        DatabaseObject instance = new DatabaseObjectImpl();
        instance.setGroupsids(groupsids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class DatabaseObject.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        DatabaseObject instance = new DatabaseObjectImpl();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDateadded method, of class DatabaseObject.
     */
    @Test
    public void test__getDateadded() {
        System.out.println("__getDateadded");
        DatabaseObject instance = new DatabaseObjectImpl();
        Date expResult = null;
        Date result = instance.__getDateadded();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDateadded method, of class DatabaseObject.
     */
    @Test
    public void testSetDateadded() {
        System.out.println("setDateadded");
        Date dateadded = null;
        DatabaseObject instance = new DatabaseObjectImpl();
        instance.setDateadded(dateadded);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIntaddedby method, of class DatabaseObject.
     */
    @Test
    public void test__getIntaddedby() {
        System.out.println("__getIntaddedby");
        DatabaseObject instance = new DatabaseObjectImpl();
        int expResult = 0;
        int result = instance.__getIntaddedby();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIntaddedby method, of class DatabaseObject.
     */
    @Test
    public void testSetIntaddedby() {
        System.out.println("setIntaddedby");
        int intaddedby = 0;
        DatabaseObject instance = new DatabaseObjectImpl();
        instance.setIntaddedby(intaddedby);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashCode method, of class DatabaseObject.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        DatabaseObject instance = new DatabaseObjectImpl();
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class DatabaseObject.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object databaseObject = null;
        DatabaseObject instance = new DatabaseObjectImpl();
        boolean expResult = false;
        boolean result = instance.equals(databaseObject);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of compareTo method, of class DatabaseObject.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        DatabaseObject anotherObject = null;
        DatabaseObject instance = new DatabaseObjectImpl();
        int expResult = 0;
        int result = instance.compareTo(anotherObject);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of exists method, of class DatabaseObject.
     */
    @Test
    public void testExists() {
        System.out.println("exists");
        Context cont = null;
        Integer ids = null;
        boolean expResult = false;
        boolean result = DatabaseObject.exists(cont, ids);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isSaved method, of class DatabaseObject.
     */
    @Test
    public void testIsSaved() {
        System.out.println("isSaved");
        DatabaseObject instance = new DatabaseObjectImpl();
        boolean expResult = false;
        boolean result = instance.isSaved();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of Saved method, of class DatabaseObject.
     */
    @Test
    public void testSaved() {
        System.out.println("Saved");
        boolean isSaved = false;
        DatabaseObject instance = new DatabaseObjectImpl();
        instance.Saved(isSaved);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isReadOnly method, of class DatabaseObject.
     */
    @Test
    public void testIsReadOnly() {
        System.out.println("isReadOnly");
        DatabaseObject instance = new DatabaseObjectImpl();
        boolean expResult = false;
        boolean result = instance.isReadOnly();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of ReadOnly method, of class DatabaseObject.
     */
    @Test
    public void testReadOnly() {
        System.out.println("ReadOnly");
        boolean readOnly = false;
        DatabaseObject instance = new DatabaseObjectImpl();
        instance.ReadOnly(readOnly);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isActive method, of class DatabaseObject.
     */
    @Test
    public void testIsActive() {
        System.out.println("isActive");
        DatabaseObject instance = new DatabaseObjectImpl();
        boolean expResult = false;
        boolean result = instance.isActive();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of Active method, of class DatabaseObject.
     */
    @Test
    public void testActive() {
        System.out.println("Active");
        boolean active = false;
        DatabaseObject instance = new DatabaseObjectImpl();
        instance.Active(active);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isAutoLockEnabled method, of class DatabaseObject.
     */
    @Test
    public void testIsAutoLockEnabled() {
        System.out.println("isAutoLockEnabled");
        boolean expResult = false;
        boolean result = DatabaseObject.isAutoLockEnabled();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of AutoLockEnabled method, of class DatabaseObject.
     */
    @Test
    public void testAutoLockEnabled() {
        System.out.println("AutoLockEnabled");
        boolean active = false;
        DatabaseObject.AutoLockEnabled(active);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of resolveReferences method, of class DatabaseObject.
     */
    @Test
    public void testResolveReferences() {
        System.out.println("resolveReferences");
        HashMap<String, Object> map = null;
        DatabaseObject instance = new DatabaseObjectImpl();
        HashMap expResult = null;
        HashMap result = instance.resolveReferences(map);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    public class DatabaseObjectImpl extends DatabaseObject {

        public JComponent getView() {
            return null;
        }

        public MPIcon getIcon() {
            return null;
        }
    }

}