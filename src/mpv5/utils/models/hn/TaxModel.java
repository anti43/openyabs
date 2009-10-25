package mpv5.utils.models.hn;

import java.io.ByteArrayOutputStream;
import mpv5.db.common.Context;

/**
 *
 * @author hnauheim
 */
public class TaxModel extends AccountCalcModel {

  private boolean skr = false;
  private DateComboBoxModel dates;

  public TaxModel(DateComboBoxModel dateModel) {
    super("tax");
    this.dates = dateModel;
  }

  @Override
  public void calculatePeriod() {
    String start = dates.getStartDay();
    String end = dates.getEndDay();
    if (!(start.equals(super.getStart())) || (end.equals(super.getEnd()))) {
      String query = Context.getItem().prepareSQLString("select a.INTPROFITFID, sum(i.TAXVALUE) " +
          "from items i, accounts a where i.DATEEND between '" + start +
          "' and '" + end + "' and i.DEFAULTACCOUNTSIDS = " +
          "a.IDS and a.INTACCOUNTCLASS > 0 and a.frame = '" +
          super.getSkr() + "'") +
          " group by a.INTPROFITFD";
      super.calculate(start, end, query);
      fillMap();
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

  @Override
  public String[] getHeader() {
    return null;
  }

  /**
   * @return the skr
   */
  @Override
  public boolean isSkr() {
    return skr;
  }
}
