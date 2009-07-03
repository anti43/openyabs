/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.models;

import java.io.ByteArrayOutputStream;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.usermanagement.MPSecurityManager;

/**
 *
 * @author hnauheim
 */
public class EURModel extends AccountCalcModel {

  private boolean skr = false;
  private DateSelectorModel dates;

  public EURModel(DateSelectorModel dateModel) {
    super.setFilename("eurform");
    this.dates = dateModel;
    ReturnValue rv = QueryHandler.getConnection().freeSelectQuery(
        "select max(INTPARENTACCOUNT) from ACCOUNTS",
        MPSecurityManager.VIEW, null);
    resultValues = rv.getData();
    if ((Long) resultValues[0][0] > 0) {
      skr = true;
    }
  }

  public void getTableHtml() {
    calculatePeriod();
    super.setDataVector(resultValues, getHeader());
  }

  @Override
  public String[] getHeader() {
    return new String[]{"", "Beschreibung", "Daten"};
  }

  @Override
  public void calculatePeriod() {
    String start = dates.getStartDay();
    String end = dates.getEndDay();

    if (!(start.equals(super.getStart()) || end.equals(super.getEnd()))) {
      String query = "select a.INTACCOUNTCLASS, a.CNAME, a.eurid, sum(i.NETVALUE) " +
          "from items i, accounts a where i.inttype = 0 and i.intstatus = 4 " +
          "and i.DATEEND between '" + start + "' and '" + end +
          "' and i.DEFAULTACCOUNTSIDS = " +
          "a.IDS and a.INTACCOUNTCLASS > 0 group by a.INTACCOUNTCLASS, a.CNAME" +
          ", a.eurid";
      super.calculate(start, end, query);
      if (skr) {
        fillMap("euer");
      }
    }
  }

  public ByteArrayOutputStream getFormHtml() {
    calculatePeriod();
    return super.createHtml();
  }

  public void getPdf() {
    calculatePeriod();
    super.createPdf();
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
