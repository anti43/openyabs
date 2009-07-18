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
import mpv5.db.common.Context;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.pdf.PDFFormTest;

/**
 *
 * @author hnauheim
 */
public abstract class AccountCalcModel extends DefaultTableModel {

  private String end;
  private String start;
  private Map<String, String> resultMap = new HashMap<String, String>();
  public Object[][] resultValues;
  private String htmlform;
  private String pdfform;
//  public static final String DE_SKR03 = "de_skr03";
  private boolean skr = false;
  private String accScheme;
  private Map<String, String> addressMap;

  public AccountCalcModel() {
//    if (DatabaseObject.getObject(
//        Context.getGroups(), MPV5View.getUser().getGroupsids).
//        getDefaultAccountframe().equals(DE_SKR03)) {
//      skr = true;
//    } //beispielhafter Code
    ReturnValue rv = QueryHandler.getConnection().freeSelectQuery("select " +
        "max(INTPARENTACCOUNT) from accounts where INTPARENTACCOUNT > 0",
        MPSecurityManager.VIEW, null);
    resultValues = rv.getData();
    skr = rv.hasData();
    accScheme = "de_skr03";
    fetchCompData("euer");
  }

  public abstract String[] getHeader();

  public abstract void calculatePeriod();

  public DefaultTableModel getModel() {
    return this;
  }

  private void fetchCompData(String prefix) {
    addressMap = new HashMap<String, String>();
              String query = Context.getGlobalSettings().prepareSQLString("select CNAME, " +
                  "TAX_COMPNAME from globalsettings where cname = 'tax_compname'");
//    String query = new Context(null).prepareSQLString("select CNAME, " +
//        "TAX_COMPNAME from globalsettings where cname = 'tax_compname'", "globalsettings");
//    ReturnValue rv = QueryHandler.getConnection().freeSelectQuery(query, MPSecurityManager.VIEW, null);
//    resultValues = rv.getData();
//    if (resultValues.length > 0) {
//      for (int i = 0; i < resultValues.length; i++) {
//        String s = (String) resultValues[i][0];
//        addressMap.put(s.replace("tax_", prefix), resultValues[i][1].toString());
//      }
//    }
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
   * @param prefix The prefix for the key name
   */
  protected void fillMap(String prefix) {
    resultMap.clear();
    resultMap.putAll(addressMap);
    if (resultValues.length > 0) {
      int cols = resultValues[0].length;
      for (int i = 0; i < resultValues.length; i++) {
        resultMap.put(prefix + resultValues[i][cols - 1], resultValues[i][cols - 2].toString());
      }
    }
  }

  /**
   * Fills a HTML form with the resulting values
   * @return A ByteArrayOutputStream of the generated XHTML file
   */
  public ByteArrayOutputStream createHtml() {
    return (ByteArrayOutputStream) new HtmlFormRenderer().parseHtml(htmlform, getResultMap());
  }

  /**
   * Fills a PDF file with the resulting values.
   * @return The generated PDF file
   */
  public File createPdf() {
    try {
      new PDFFormTest(new File(pdfform)).fillFields((HashMap) getResultMap());
    } catch (com.lowagie.text.DocumentException ex) {
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return null;
  }

  protected void simpleHtml(String select) {
    ReturnValue rv = QueryHandler.getConnection().freeSelectQuery(select, MPSecurityManager.VIEW, null);
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
   * @param name of the chart of accounts without appendix
   */
  public void setFilename(String string) {
    this.htmlform = "/mpv5/resources/de_" + string + ".html";
    this.pdfform = "/mpv5/resources/de_" + string + ".pdf";
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
   * Creates a useful name for an output file
   * @param dates DateSelectorModel to get the date values
   * @param prefix something like "tax" or "income"
   * @return a name for a pdf file
   */
  public String createPrintName(DateSelectorModel dates, String prefix) {
    String s = prefix + dates.getYear();
    String printName;
    switch (dates.getMode()) {
      case 1:
        int mon = Integer.parseInt(dates.getMonth());
        printName = s + "_" + String.format("%1$0,2d", mon);
        break;
      case 3:
        printName = s + "q" + dates.getQuarter();
        break;
      default:
        printName = s;
    }
    return printName + ".pdf";
  }
}
