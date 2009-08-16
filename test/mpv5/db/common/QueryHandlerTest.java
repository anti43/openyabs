
package mpv5.db.common;

import java.io.File;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import mpv5.db.objects.User;
import mpv5.ui.panels.DataPanel;
import mpv5.utils.date.vTimeframe;
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
public class QueryHandlerTest {

    public QueryHandlerTest() {
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
     * Test of instanceOf method, of class QueryHandler.
     */
    @Test
    public void testInstanceOf() {
        System.out.println("instanceOf");
        QueryHandler expResult = null;
        QueryHandler result = QueryHandler.instanceOf();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setRowLimit method, of class QueryHandler.
     */
    @Test
    public void testSetRowLimit() {
        System.out.println("setRowLimit");
        int limit = 0;
        QueryHandler.setRowLimit(limit);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of checkConstraint method, of class QueryHandler.
     */
    @Test
    public void testCheckConstraint() {
        System.out.println("checkConstraint");
        String[] constraint = null;
        Object[] values = null;
        QueryHandler instance = null;
        boolean expResult = false;
        boolean result = instance.checkConstraint(constraint, values);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of checkUniqueness method, of class QueryHandler.
     */
    @Test
    public void testCheckUniqueness_String_JTextFieldArr() {
        System.out.println("checkUniqueness");
        String uniqueColumns = "";
        JTextField[] object = null;
        QueryHandler instance = null;
        boolean expResult = false;
        boolean result = instance.checkUniqueness(uniqueColumns, object);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getColumn method, of class QueryHandler.
     */
    @Test
    public void testGetColumn() throws Exception {
        System.out.println("getColumn");
        String columnName = "";
        int maximumRowCount = 0;
        QueryHandler instance = null;
        Object[] expResult = null;
        Object[] result = instance.getColumn(columnName, maximumRowCount);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getColumns method, of class QueryHandler.
     */
    @Test
    public void testGetColumns() throws Exception {
        System.out.println("getColumns");
        String[] columnNames = null;
        int maximumRowCount = 0;
        QueryHandler instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.getColumns(columnNames, maximumRowCount);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContext method, of class QueryHandler.
     */
    @Test
    public void testSetContext() {
        System.out.println("setContext");
        Context context = null;
        QueryHandler instance = null;
        QueryHandler expResult = null;
        QueryHandler result = instance.setContext(context);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of select method, of class QueryHandler.
     */
    @Test
    public void testSelect_int() throws Exception {
        System.out.println("select");
        int id = 0;
        QueryHandler instance = null;
        ReturnValue expResult = null;
        ReturnValue result = instance.select(id);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of select method, of class QueryHandler.
     */
    @Test
    public void testSelect_String_QueryCriteria() throws Exception {
        System.out.println("select");
        String columns = "";
        QueryCriteria criterias = null;
        QueryHandler instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.select(columns, criterias);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of select method, of class QueryHandler.
     */
    @Test
    public void testSelect_3args_1() throws Exception {
        System.out.println("select");
        String columns = "";
        QueryCriteria criterias = null;
        vTimeframe time = null;
        QueryHandler instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.select(columns, criterias, time);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of select method, of class QueryHandler.
     */
    @Test
    public void testSelect_QueryCriteria() throws Exception {
        System.out.println("select");
        QueryCriteria criterias = null;
        QueryHandler instance = null;
        ReturnValue expResult = null;
        ReturnValue result = instance.select(criterias);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of select method, of class QueryHandler.
     */
    @Test
    public void testSelect_0args() throws Exception {
        System.out.println("select");
        QueryHandler instance = null;
        ReturnValue expResult = null;
        ReturnValue result = instance.select();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of selectIndexes method, of class QueryHandler.
     */
    @Test
    public void testSelectIndexes() throws Exception {
        System.out.println("selectIndexes");
        QueryHandler instance = null;
        ReturnValue expResult = null;
        ReturnValue result = instance.selectIndexes();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValuesFor method, of class QueryHandler.
     */
    @Test
    public void testGetValuesFor_4args() {
        System.out.println("getValuesFor");
        String[] columns = null;
        String needle = "";
        String value = "";
        boolean exactMatch = false;
        QueryHandler instance = null;
        Object[] expResult = null;
        Object[] result = instance.getValuesFor(columns, needle, value, exactMatch);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of select method, of class QueryHandler.
     */
    @Test
    public void testSelect_4args_1() {
        System.out.println("select");
        String what = "";
        String[] where = null;
        String datecolumn = "";
        vTimeframe zeitraum = null;
        QueryHandler instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.select(what, where, datecolumn, zeitraum);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of selectYearlySums method, of class QueryHandler.
     */
    @Test
    public void testSelectYearlySums() {
        System.out.println("selectYearlySums");
        String what = "";
        String[] where = null;
        vTimeframe zeitraum = null;
        String additionalCondition = "";
        QueryHandler instance = null;
        ArrayList expResult = null;
        ArrayList result = instance.selectYearlySums(what, where, zeitraum, additionalCondition);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of selectMonthlySums method, of class QueryHandler.
     */
    @Test
    public void testSelectMonthlySums() {
        System.out.println("selectMonthlySums");
        String what = "";
        String[] where = null;
        vTimeframe zeitraum = null;
        String additionalCondition = "";
        QueryHandler instance = null;
        ArrayList expResult = null;
        ArrayList result = instance.selectMonthlySums(what, where, zeitraum, additionalCondition);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of select method, of class QueryHandler.
     */
    @Test
    public void testSelect_6args() {
        System.out.println("select");
        String what = "";
        String[] where = null;
        String leftJoinTable = "";
        String leftJoinKey = "";
        String order = "";
        Boolean like = null;
        QueryHandler instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.select(what, where, leftJoinTable, leftJoinKey, order, like);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of selectCountBetween method, of class QueryHandler.
     */
    @Test
    public void testSelectCountBetween() {
        System.out.println("selectCountBetween");
        Date date1 = null;
        Date date2 = null;
        QueryHandler instance = null;
        int expResult = 0;
        int result = instance.selectCountBetween(date1, date2);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTable method, of class QueryHandler.
     */
    @Test
    public void testSetTable() {
        System.out.println("setTable");
        String newTable = "";
        QueryHandler instance = null;
        instance.setTable(newTable);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTable method, of class QueryHandler.
     */
    @Test
    public void testGetTable() {
        System.out.println("getTable");
        QueryHandler instance = null;
        String expResult = "";
        String result = instance.getTable();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setWaitCursorFor method, of class QueryHandler.
     */
    @Test
    public void testSetWaitCursorFor() {
        System.out.println("setWaitCursorFor");
        JFrame main = null;
        QueryHandler.setWaitCursorFor(main);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of truncate method, of class QueryHandler.
     */
    @Test
    public void testTruncate() {
        System.out.println("truncate");
        String dbIdentity = "";
        QueryHandler instance = null;
        instance.truncate(dbIdentity);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of checkUniqueness method, of class QueryHandler.
     */
    @Test
    public void testCheckUniqueness_QueryData_intArr() {
        System.out.println("checkUniqueness");
        QueryData vals = null;
        int[] uniquecols = null;
        QueryHandler instance = null;
        boolean expResult = false;
        boolean result = instance.checkUniqueness(vals, uniquecols);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of checkUniqueness method, of class QueryHandler.
     */
    @Test
    public void testCheckUniqueness_String_String() {
        System.out.println("checkUniqueness");
        String column = "";
        String value = "";
        QueryHandler instance = null;
        boolean expResult = false;
        boolean result = instance.checkUniqueness(column, value);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of stop method, of class QueryHandler.
     */
    @Test
    public void testStop() {
        System.out.println("stop");
        QueryHandler instance = null;
        instance.stop();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of start method, of class QueryHandler.
     */
    @Test
    public void testStart() {
        System.out.println("start");
        QueryHandler instance = null;
        instance.start();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValue method, of class QueryHandler.
     */
    @Test
    public void testGetValue() throws Exception {
        System.out.println("getValue");
        String columnName = "";
        String compareColumn = "";
        Object compareValue = null;
        QueryHandler instance = null;
        Object expResult = null;
        Object result = instance.getValue(columnName, compareColumn, compareValue);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValuesFor method, of class QueryHandler.
     */
    @Test
    public void testGetValuesFor_int() throws Exception {
        System.out.println("getValuesFor");
        int id = 0;
        QueryHandler instance = null;
        Map expResult = null;
        Map result = instance.getValuesFor(id);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCount method, of class QueryHandler.
     */
    @Test
    public void testGetCount() {
        System.out.println("getCount");
        QueryHandler instance = null;
        Integer expResult = null;
        Integer result = instance.getCount();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of insert method, of class QueryHandler.
     */
    @Test
    public void testInsert() {
        System.out.println("insert");
        QueryData what = null;
        String jobmessage = "";
        QueryHandler instance = null;
        int expResult = 0;
        int result = instance.insert(what, jobmessage);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of insertHistoryItem method, of class QueryHandler.
     */
    @Test
    public void testInsertHistoryItem() {
        System.out.println("insertHistoryItem");
        String message = "";
        String username = "";
        String dbidentity = "";
        int item = 0;
        int groupid = 0;
        QueryHandler instance = null;
        instance.insertHistoryItem(message, username, dbidentity, item, groupid);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of insertLock method, of class QueryHandler.
     */
    @Test
    public void testInsertLock() throws Exception {
        System.out.println("insertLock");
        Context context = null;
        int id = 0;
        User user = null;
        QueryHandler instance = null;
        boolean expResult = false;
        boolean result = instance.insertLock(context, id, user);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeLock method, of class QueryHandler.
     */
    @Test
    public void testRemoveLock() {
        System.out.println("removeLock");
        Context context = null;
        int id = 0;
        User user = null;
        QueryHandler instance = null;
        instance.removeLock(context, id, user);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of update method, of class QueryHandler.
     */
    @Test
    public void testUpdate_3args_1() {
        System.out.println("update");
        String columnName = "";
        Integer id = null;
        Object value = null;
        QueryHandler instance = null;
        instance.update(columnName, id, value);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of update method, of class QueryHandler.
     */
    @Test
    public void testUpdate_3args_2() {
        System.out.println("update");
        QueryData what = null;
        String[] where = null;
        String jobmessage = "";
        QueryHandler instance = null;
        instance.update(what, where, jobmessage);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of update method, of class QueryHandler.
     */
    @Test
    public void testUpdate_3args_3() {
        System.out.println("update");
        QueryData q = null;
        QueryCriteria criteria = null;
        String jobmessage = "";
        QueryHandler instance = null;
        instance.update(q, criteria, jobmessage);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of update method, of class QueryHandler.
     */
    @Test
    public void testUpdate_3args_4() {
        System.out.println("update");
        QueryData q = null;
        int doId = 0;
        String jobmessage = "";
        QueryHandler instance = null;
        instance.update(q, doId, jobmessage);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of selectLast method, of class QueryHandler.
     */
    @Test
    public void testSelectLast_String_StringArr() throws Exception {
        System.out.println("selectLast");
        String what = "";
        String[] where = null;
        QueryHandler instance = null;
        Object[] expResult = null;
        Object[] result = instance.selectLast(what, where);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of selectFirst method, of class QueryHandler.
     */
    @Test
    public void testSelectFirst_String_StringArr() throws Exception {
        System.out.println("selectFirst");
        String what = "";
        String[] where = null;
        QueryHandler instance = null;
        Object[] expResult = null;
        Object[] result = instance.selectFirst(what, where);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of selectFirst method, of class QueryHandler.
     */
    @Test
    public void testSelectFirst_3args() throws Exception {
        System.out.println("selectFirst");
        String what = "";
        String[] where = null;
        boolean searchFoLike = false;
        QueryHandler instance = null;
        Object[] expResult = null;
        Object[] result = instance.selectFirst(what, where, searchFoLike);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of selectLast method, of class QueryHandler.
     */
    @Test
    public void testSelectLast_3args() throws Exception {
        System.out.println("selectLast");
        String what = "";
        String[] where = null;
        boolean searchFoLike = false;
        QueryHandler instance = null;
        Object[] expResult = null;
        Object[] result = instance.selectLast(what, where, searchFoLike);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of select method, of class QueryHandler.
     */
    @Test
    public void testSelect_5args_1() {
        System.out.println("select");
        String what = "";
        String[] where = null;
        String leftJoinTable = "";
        String leftJoinKey = "";
        String order = "";
        QueryHandler instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.select(what, where, leftJoinTable, leftJoinKey, order);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of select method, of class QueryHandler.
     */
    @Test
    public void testSelect_4args_2() {
        System.out.println("select");
        String what = "";
        String[] where = null;
        String leftJoinTable = "";
        String leftJoinKey = "";
        QueryHandler instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.select(what, where, leftJoinTable, leftJoinKey);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of select method, of class QueryHandler.
     */
    @Test
    public void testSelect_String_StringArr() {
        System.out.println("select");
        String what = "";
        String[] where = null;
        QueryHandler instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.select(what, where);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of select method, of class QueryHandler.
     */
    @Test
    public void testSelect_3args_2() {
        System.out.println("select");
        String what = "";
        String[] whereColumns = null;
        Object[] haveValues = null;
        QueryHandler instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.select(what, whereColumns, haveValues);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of select method, of class QueryHandler.
     */
    @Test
    public void testSelect_4args_3() {
        System.out.println("select");
        String what = "";
        String[] where = null;
        String order = "";
        boolean like = false;
        QueryHandler instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.select(what, where, order, like);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of buildPreparedSelectStatement method, of class QueryHandler.
     */
    @Test
    public void testBuildPreparedSelectStatement() throws Exception {
        System.out.println("buildPreparedSelectStatement");
        String[] columns = null;
        String[] conditionColumns = null;
        String order = "";
        boolean like = false;
        QueryHandler instance = null;
        PreparedStatement expResult = null;
        PreparedStatement result = instance.buildPreparedSelectStatement(columns, conditionColumns, order, like);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of executeStatement method, of class QueryHandler.
     */
    @Test
    public void testExecuteStatement() throws Exception {
        System.out.println("executeStatement");
        PreparedStatement statement = null;
        Object[] values = null;
        QueryHandler instance = null;
        ReturnValue expResult = null;
        ReturnValue result = instance.executeStatement(statement, values);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class QueryHandler.
     */
    @Test
    public void testDelete_StringArrArr_String() {
        System.out.println("delete");
        String[][] where = null;
        String jobmessage = "";
        QueryHandler instance = null;
        boolean expResult = false;
        boolean result = instance.delete(where, jobmessage);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class QueryHandler.
     */
    @Test
    public void testDelete_3args() {
        System.out.println("delete");
        String[] whereColumns = null;
        Object[] haveValues = null;
        String jobmessage = "";
        QueryHandler instance = null;
        boolean expResult = false;
        boolean result = instance.delete(whereColumns, haveValues, jobmessage);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class QueryHandler.
     */
    @Test
    public void testDelete_StringArrArr() throws Exception {
        System.out.println("delete");
        String[][] where = null;
        QueryHandler instance = null;
        instance.delete(where);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class QueryHandler.
     */
    @Test
    public void testDelete_QueryCriteria() {
        System.out.println("delete");
        QueryCriteria criterias = null;
        QueryHandler instance = null;
        instance.delete(criterias);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of clone method, of class QueryHandler.
     */
    @Test
    public void testClone_String() {
        System.out.println("clone");
        String tablename = "";
        QueryHandler instance = null;
        QueryHandler expResult = null;
        QueryHandler result = instance.clone(tablename);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnection method, of class QueryHandler.
     */
    @Test
    public void testGetConnection() {
        System.out.println("getConnection");
        QueryHandler expResult = null;
        QueryHandler result = QueryHandler.getConnection();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of clone method, of class QueryHandler.
     */
    @Test
    public void testClone_Context() {
        System.out.println("clone");
        Context context = null;
        QueryHandler instance = null;
        QueryHandler expResult = null;
        QueryHandler result = instance.clone(context);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of clone method, of class QueryHandler.
     */
    @Test
    public void testClone_Context_int() {
        System.out.println("clone");
        Context context = null;
        int limit = 0;
        QueryHandler instance = null;
        QueryHandler expResult = null;
        QueryHandler result = instance.clone(context, limit);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of clone method, of class QueryHandler.
     */
    @Test
    public void testClone_3args() {
        System.out.println("clone");
        Context context = null;
        int limit = 0;
        boolean inBackground = false;
        QueryHandler instance = null;
        QueryHandler expResult = null;
        QueryHandler result = instance.clone(context, limit, inBackground);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of clone method, of class QueryHandler.
     */
    @Test
    public void testClone_Context_DataPanel() {
        System.out.println("clone");
        Context c = null;
        DataPanel viewToBeNotified = null;
        QueryHandler instance = null;
        QueryHandler expResult = null;
        QueryHandler result = instance.clone(c, viewToBeNotified);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of select method, of class QueryHandler.
     */
    @Test
    public void testSelect_5args_2() {
        System.out.println("select");
        String what = "";
        String[] where = null;
        String order = "";
        boolean like = false;
        boolean integer = false;
        QueryHandler instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.select(what, where, order, like, integer);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of selectCount method, of class QueryHandler.
     */
    @Test
    public void testSelectCount() {
        System.out.println("selectCount");
        String what = "";
        String condition = "";
        QueryHandler instance = null;
        int expResult = 0;
        int result = instance.selectCount(what, condition);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of freeQuery method, of class QueryHandler.
     */
    @Test
    public void testFreeQuery_3args() {
        System.out.println("freeQuery");
        String string = "";
        int action = 0;
        String jobmessage = "";
        QueryHandler instance = null;
        ReturnValue expResult = null;
        ReturnValue result = instance.freeQuery(string, action, jobmessage);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of freeQuery method, of class QueryHandler.
     */
    @Test
    public void testFreeQuery_4args() {
        System.out.println("freeQuery");
        String query = "";
        JTextArea log = null;
        int action = 0;
        String jobmessage = "";
        QueryHandler instance = null;
        ReturnValue expResult = null;
        ReturnValue result = instance.freeQuery(query, log, action, jobmessage);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of freeUpdateQuery method, of class QueryHandler.
     */
    @Test
    public void testFreeUpdateQuery_3args() {
        System.out.println("freeUpdateQuery");
        String query = "";
        int action = 0;
        String jobmessage = "";
        QueryHandler instance = null;
        ReturnValue expResult = null;
        ReturnValue result = instance.freeUpdateQuery(query, action, jobmessage);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of freeUpdateQuery method, of class QueryHandler.
     */
    @Test
    public void testFreeUpdateQuery_4args() {
        System.out.println("freeUpdateQuery");
        String query = "";
        JTextArea log = null;
        int action = 0;
        String jobmessage = "";
        QueryHandler instance = null;
        ReturnValue expResult = null;
        ReturnValue result = instance.freeUpdateQuery(query, log, action, jobmessage);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of freeSelectQuery method, of class QueryHandler.
     */
    @Test
    public void testFreeSelectQuery() {
        System.out.println("freeSelectQuery");
        String query = "";
        int action = 0;
        String jobmessage = "";
        QueryHandler instance = null;
        ReturnValue expResult = null;
        ReturnValue result = instance.freeSelectQuery(query, action, jobmessage);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of insertFile method, of class QueryHandler.
     */
    @Test
    public void testInsertFile_File() throws Exception {
        System.out.println("insertFile");
        File file = null;
        QueryHandler instance = null;
        String expResult = "";
        String result = instance.insertFile(file);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of retrieveFiles method, of class QueryHandler.
     */
    @Test
    public void testRetrieveFiles() throws Exception {
        System.out.println("retrieveFiles");
        String filename = "";
        QueryHandler instance = null;
        ArrayList expResult = null;
        ArrayList result = instance.retrieveFiles(filename);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of retrieveFile method, of class QueryHandler.
     */
    @Test
    public void testRetrieveFile_String_File() {
        System.out.println("retrieveFile");
        String name = "";
        File targetFile = null;
        QueryHandler instance = null;
        File expResult = null;
        File result = instance.retrieveFile(name, targetFile);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of retrieveFile method, of class QueryHandler.
     */
    @Test
    public void testRetrieveFile_String() {
        System.out.println("retrieveFile");
        String name = "";
        QueryHandler instance = null;
        File expResult = null;
        File result = instance.retrieveFile(name);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeFile method, of class QueryHandler.
     */
    @Test
    public void testRemoveFile() throws Exception {
        System.out.println("removeFile");
        String fileid = "";
        QueryHandler instance = null;
        instance.removeFile(fileid);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of insertFile method, of class QueryHandler.
     */
    @Test
    public void testInsertFile_3args() {
        System.out.println("insertFile");
        File file = null;
        DatabaseObject dataOwner = null;
        SaveString descriptiveText = null;
        QueryHandler instance = null;
        boolean expResult = false;
        boolean result = instance.insertFile(file, dataOwner, descriptiveText);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStatistics method, of class QueryHandler.
     */
    @Test
    public void testGetStatistics() {
        System.out.println("getStatistics");
        QueryHandler instance = null;
        String expResult = "";
        String result = instance.getStatistics();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}