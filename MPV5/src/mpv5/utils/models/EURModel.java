/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.models;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.DefaultTableModel;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.pdf.PDFFormTest;

/**
 *
 * @author hnauheim
 */
public class EURModel extends DefaultTableModel {

    private Map eurMap = new HashMap();
    private Object[][] resultValues;
    private final String eurpdf = "/mpv5/resources/de_eurform.pdf";
    private final String eurform = "/mpv5/resources/de_eurform.html";
    private String start;
    private String end;
    private boolean skr = false;

    public EURModel() {
        ReturnValue rv = QueryHandler.getConnection().freeSelectQuery(
                "select max(INTPARENTACCOUNT) from ACCOUNTS",
                MPSecurityManager.VIEW, null);
        resultValues = rv.getData();
        if ((Long) resultValues[0][0] > 0) {
            skr = true;
        }
    }

    public void getPdf() {
        try {
            new PDFFormTest(new File(eurpdf)).fillFields((HashMap) eurMap);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (com.lowagie.text.DocumentException ex) {
            ex.printStackTrace();
        }
    }

    public ByteArrayOutputStream getFormHtml() {
        fillMap();
        return (ByteArrayOutputStream) new HtmlFormRenderer().parseHtml(eurform, eurMap);
    }

    public void getTableHtml(String start, String end) {
        this.start = start;
        this.end = end;
        simpleHtml();
        super.setDataVector(resultValues, getHeader());
    }

    public String[] getHeader() {
        return new String[]{"", "Beschreibung", "Daten"};
    }

    private void fillMap() {
        String select = "select a.EURID, sum(i.TAXVALUE) " +
                "from items i, accounts a where i.DATEEND between '" + start +
                "' and '" + end +
                "' and i.DEFAULTACCOUNTSIDS = " + "a.IDS and a.INTACCOUNTCLASS > 0 group by a.EURID";
        ReturnValue rv = QueryHandler.getConnection().freeSelectQuery(select,
                MPSecurityManager.VIEW, null);
        resultValues = rv.getData();
        for (int i = 0; i < resultValues.length; i++) {
            eurMap.put("euer" + resultValues[i][0], resultValues[i][1].toString());
//      System.out.println("key " + resultValues[i][0] + "  value " + resultValues[i][1].toString());
        }
    }

    private void simpleHtml() {
        String select = "select a.INTACCOUNTCLASS, a.CNAME, sum(i.NETVALUE) " +
                "from items i, accounts a where i.inttype = 0 and i.intstatus = 4 " +
                "and i.DATEEND between '" + start + "' and '" + end +
                "' and i.DEFAULTACCOUNTSIDS = " +
                "a.IDS and a.INTACCOUNTCLASS > 0 group by a.INTACCOUNTCLASS, a.CNAME";
        ReturnValue rv = QueryHandler.getConnection().freeSelectQuery(select,
                MPSecurityManager.VIEW, null);
        resultValues = rv.getData();
    }

    public Map getValuesAsMap() {
        return eurMap;
    }

    public DefaultTableModel getModel() {
        return this;
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
     * @param end the end to set
     */
    public void setEnd(String end) {
        this.end = end;
    }

    /**
     * @return the skr
     */
    public boolean isSkr() {
        return skr;
    }
//  
//  select sum(i.TAXVALUE)
// from items i, accounts a
// where i.DATEEND between '2008-06-01' and '2008-06-30'
// and i.DEFAULTACCOUNTSIDS = a.IDS
// and a.INTACCOUNTCLASS = 8400
//;
//
//  --select *
//-- from items i
//-- where i.DATEEND between '2008-06-01' and '2008-06-30'
//--;
//
//update items i set i.DEFAULTACCOUNTSIDS = 355
// where i.CNAME like '%-19%'
// and  i.DATEEND between '2008-06-01' and '2008-06-30'
//;
//  update items i set i.DEFAULTACCOUNTSIDS = 308
// where i.CNAME like '%-7%'
// and  i.DATEEND between '2008-06-01' and '2008-06-30'
//
//  update accounts set EURID = 112 where EURID = 1
}
