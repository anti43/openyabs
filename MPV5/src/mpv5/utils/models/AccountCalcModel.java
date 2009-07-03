/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.models;

import com.lowagie.text.DocumentException;
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
public abstract class AccountCalcModel extends DefaultTableModel {

  private String end;
  private String start;
  private Map<String, String> resultMap = new HashMap<String, String>();
  public Object[][] resultValues;
  private String htmlform;
  private String pdfform;

     /**
     * German SKR03 Account Frame
     */
  public static final String DE_SKR03 = "de_SKR03";

  public abstract String[] getHeader();

  public abstract void calculatePeriod();

  public DefaultTableModel getModel() {
    return this;
  }

  public void calculate(String start, String end, String query) {
    this.start = start;
    this.end = end;
    ReturnValue rv = QueryHandler.getConnection().freeSelectQuery(query, MPSecurityManager.VIEW, null);
    resultValues = rv.getData();
  }

  protected void fillMap(String prefix) {
    resultMap.clear();
    if (resultValues.length > 0) {
      int cols = resultValues[0].length;
      for (int i = 0; i < resultValues.length; i++) {
        resultMap.put(prefix + resultValues[i][cols - 2], resultValues[i][cols - 1].toString());
      }
    }
  }

  public ByteArrayOutputStream createHtml() {
    System.out.println("htmlform  " + htmlform);
    return (ByteArrayOutputStream) new HtmlFormRenderer().parseHtml(htmlform, getResultMap());
  }

  public void createPdf() {
    try {
      new PDFFormTest(new File(pdfform)).fillFields((HashMap) getResultMap());
    } catch (DocumentException ex) {
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
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
   * @return the calcMap
   */
  public Map getResultMap() {
    return resultMap;
  }

  /**
   * @param calcMap the calcMap to set
   */
  public void setResultMap(Map<String, String> resultMap) {
    this.resultMap = resultMap;
  }

  void setFilename(String string) {
    this.htmlform = "/mpv5/resources/de_" + string + ".html";
    this.pdfform = "/mpv5/resources/de_" + string + ".pdf";
  }
}
