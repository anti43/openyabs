/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.models;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.table.DefaultTableModel;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.Company;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;
import mpv5.usermanagement.MPSecurityManager;

/**
 *
 * @author hnauheim
 */
public abstract class AccountCalcModel extends DefaultTableModel {

    private String end;
    private String start;
    private String prefix;
    private Map<String, String> resultMap = new HashMap<String, String>();
    public Object[][] resultValues;
    private String htmlform;
    private String pdfform;
    private boolean skr = false;
    private String accScheme;
    private Map<String, String> addressMap = new HashMap<String, String>();
    private Company company;

    public AccountCalcModel(String prefix) {
        this.prefix = prefix;
        try {
            ReturnValue rv = QueryHandler.getConnection().freeSelectQuery("select " +
                    "distinct FRAME from accounts where GROUPSIDS =" + MPView.getUser().__getGroupsids(),
                    MPSecurityManager.VIEW, null);
            resultValues = rv.getData();
            skr = rv.hasData();
            accScheme = (String) resultValues[1][0];//will not work if NO account frame is available
            setFilename();
            fetchCompanyData();
        } catch (Exception e) {
            Popup.notice(Messages.NO_ACCOUNTS);
            Log.Debug(e);
        }
    }

    public abstract String[] getHeader();

    public abstract void calculatePeriod();

    public DefaultTableModel getModel() {
        return this;
    }

    private void fetchCompanyData() throws NodataFoundException {
        //Check if the company is existing
        company = (Company) DatabaseObject.getObject(Context.getCompanies(), MPView.getUser().__getCompsids());
        addressMap = QueryHandler.instanceOf().clone(Context.getCompanies()).getValuesFor(MPView.getUser().__getCompsids());

        // Wofür ist der prefix ? //////////////////////////////////////////////
        HashMap<String, String> temp = new HashMap<String, String>();
        Set<String> k = addressMap.keySet();
        String[] arr = k.toArray(new String[0]);
        for (int i = 0; i < arr.length; i++) {
            String string = arr[i];
            temp.put(prefix + "_" + string, addressMap.get(string));//mit oder ohne Unterstrich?
        }
        addressMap = temp;
        /// ? ////////////////////////////////////////////////////////////////

        Log.Print(addressMap);
    }

    /**
     * Calculation of values within a given time, grouped by account numbers
     * @param start SQL formatted end day
     * @param end SQL formatted end day
     * @param query SQL query
     */
    public void calculate(String start, String end, String query) {
        this.start = start;
        this.end = end;
        ReturnValue rv = QueryHandler.getConnection().freeSelectQuery(query, MPSecurityManager.VIEW, null);
        resultValues = rv.getData();
    }

    /**
     * Fills a Map with form field names as keys and their values
     */
    protected void fillMap() {
        if (company != null) {
            resultMap.clear();
            resultMap.putAll(addressMap);
            if (resultValues.length > 0) {
                int cols = resultValues[0].length;
                for (int i = 0; i < resultValues.length; i++) {
                    resultMap.put(prefix + resultValues[i][cols - 1], resultValues[i][cols - 2].toString());
                }
            }
        }
    }

    /**
     * Fills a HTML form with the resulting values
     * @return A ByteArrayOutputStream of the generated XHTML file
     */
    public ByteArrayOutputStream createHtml() {
        return (ByteArrayOutputStream) new HtmlFormRenderer().parseHtml(getHtmlform(), getResultMap());
    }

    protected void simpleHtml(String select) {
        ReturnValue rv = QueryHandler.getConnection().freeSelectQuery(
                select, MPSecurityManager.VIEW, null);
        resultValues = rv.getData();
    }

    /**
     * @return the end
     */
    public String getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(String end) {
        this.end = end;
    }

    /**
     * @return the start
     */
    public String getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(String start) {
        this.start = start;
    }

    /**
     * @return the resultMap
     */
    public Map<String, String> getResultMap() {
        return resultMap;
    }

    /**
     * @param resultMap the resultMap to set
     */
    public void setResultMap(Map<String, String> resultMap) {
        this.resultMap = resultMap;
    }

    /**
     * The file names for html and pdf templates
     */
    private void setFilename() {
        String lang = MPView.getUser().__getLocale();
        lang = lang.substring(lang.length() - 2).toLowerCase() + "_";
        lang = "de_";
        String s = "/mpv5/resources/" + lang + prefix + "form.";
        this.htmlform = s + "html";
        this.pdfform = s + "pdf";
    }

    /**
     * @return whether a complete chart of accounts exists
     */
    public boolean isSkr() {
        return skr;
    }

    /**
     * @return the name of the accounting scheme
     */
    public String getSkr() {
        return accScheme;
    }

    /**
     * @return the htmlform
     */
    public String getHtmlform() {
        return htmlform;
    }

    /**
     * @return the pdfform
     */
    public String getPdfform() {
        return pdfform;
    }

    /**
     * Creates a useful name for an output file
     * @param dates DateSelectorModel to get the date values
     * @return a name for a pdf file
     */
    public String createPrintName(DateComboBoxModel dates) {
        String s = prefix + dates.getYear();
        switch (dates.getMode()) {
            case 1:
                int mon = Integer.parseInt(dates.getMonth());
                this.prefix = s + "_" + String.format("%1$0,2d", mon);
                break;
            case 3:
                this.prefix = s + "q" + dates.getQuarter();
                break;
            default:
                this.prefix = s;
        }
        return this.prefix + ".pdf";
    }

    void createPdf() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
