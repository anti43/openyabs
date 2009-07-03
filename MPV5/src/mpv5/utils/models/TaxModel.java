/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.models;

import java.io.ByteArrayOutputStream;
import mpv5.db.common.Context;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.usermanagement.MPSecurityManager;

/**
 *
 * @author hnauheim
 */
public class TaxModel extends AccountCalcModel {

    private boolean skr = false;
    private DateSelectorModel dates;

    public TaxModel(DateSelectorModel dateModel) {
        this.dates = dateModel;
        ReturnValue rv = QueryHandler.getConnection().freeSelectQuery(
                "select max(INTPARENTACCOUNT) from ACCOUNTS where frame = '" + EURModel.DE_SKR03 + "'",
                MPSecurityManager.VIEW, null);
        resultValues = rv.getData();
        skr = rv.hasData();
    }

    @Override
    public void calculatePeriod() {
        String start = dates.getStartDay();
        String end = dates.getEndDay();
        if (!(start.equals(super.getStart())) || (end.equals(super.getEnd()))) {
            String query = Context.getItems().prepareSQLString("select a.EURUID, sum(i.TAXVALUE) " +
                    "from items i, accounts a where i.DATEEND between '" + start +
                    "' and '" + end + "' and i.DEFAULTACCOUNTSIDS = " +
                    "a.IDS and a.INTACCOUNTCLASS > 0 and a.frame = '" + EURModel.DE_SKR03 + "'") +
                    " group by a.EURUID";
            super.calculate(start, end, query);
            fillMap("tax");
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
    public boolean isSkr() {
        return skr;
    }
}
