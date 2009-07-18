/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.models;

import java.io.ByteArrayOutputStream;
import java.util.ResourceBundle;
import mpv5.db.common.Context;

/**
 *
 * @author hnauheim
 */
public class ProfitModel extends AccountCalcModel {

  private DateSelectorModel dates;

  public ProfitModel(DateSelectorModel dateModel) {
    super.setFilename("profitform");
    this.dates = dateModel;
  }

  public void fetchResults() {
    calculatePeriod();
    super.setDataVector(resultValues, getHeader());
  }

  @Override
  public String[] getHeader() {
    ResourceBundle bundle = ResourceBundle.getBundle("mpv5/resources/languages/Panels");
    return new String[]{
          bundle.getString("ProfitModel.account"),
          bundle.getString("ProfitModel.descr"),
          bundle.getString("ProfitModel.data")
        };
  }

  @Override
  public void calculatePeriod() {
    String start = dates.getStartDay();
    String end = dates.getEndDay();

    if (!(start.equals(super.getStart()) || end.equals(super.getEnd()))) {
      String query = Context.getItems().prepareSQLString(
          "select a.INTACCOUNTCLASS, a.CNAME, sum(i.NETVALUE), a.profitfid " +
          "from items i, accounts a where i.inttype = 0 and i.intstatus = 4 " +
          "and i.DATEEND between '" + start + "' and '" + end +
          "' and i.DEFAULTACCOUNTSIDS = " +
          "a.IDS and a.INTACCOUNTCLASS > 0 and a.frame = '" +
          super.getSkr() + "'", "a") +
          " group by a.INTACCOUNTCLASS, a.CNAME, a.profitfid";
      super.calculate(start, end, query);
      if (super.isSkr()) {
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

  public String getOutFileName() {
    return createPrintName(dates, "profit");
  }

//
//update items i set i.DEFAULTACCOUNTSIDS = 354
// where i.CNAME like '%-19%'
// and  i.DATEEND between '2008-01-01' and '2008-12-31';
//update items i set i.DEFAULTACCOUNTSIDS = 307
// where i.CNAME like '%-7%'
// and  i.DATEEND between '2008-01-01' and '2008-12-31';
//
//  update accounts set PROFITFID = 112 where PROFITFID = 1;
//  update accounts set frame = 'de_skr03' where frame = 'SKR03 HN';
}
