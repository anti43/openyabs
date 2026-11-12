/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.pos.tse;

import java.util.HashMap;
import java.util.Map;
import mpv5.db.common.Context;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.logging.Log;
import mpv5.usermanagement.MPSecurityManager;

/**
 * The TSE data of each sale (table postransactions): written once after the
 * sale, never changed
 */
public final class PosTransactions {

    private PosTransactions() {
    }

    /**
     * What is stored for a sale
     */
    public static final class Record {

        public int invoice;
        public String clientId = "";
        public String tseType = "";
        public String processType = Tse.KASSENBELEG;
        public String processData = "";
        public boolean failed;
        public String failure = "";
        public Tse.Finished signed;
    }

    private static String q(String s) {
        return s == null ? "NULL" : "'" + s.replace("'", "''") + "'";
    }

    /**
     * @param r the record of a sale
     */
    public static void save(Record r) {
        Tse.Finished f = r.signed;
        String sql = "INSERT INTO postransactions (itemsids, clientid, tsetype, txnumber, starttime, endtime, sigcounter, signature, "
                + "tseserial, algorithm, timeformat, publickey, processtype, processdata, tsefailed, failure, dateadded) VALUES ("
                + r.invoice + ", " + q(r.clientId) + ", " + q(r.tseType) + ", "
                + (f == null ? "0" : String.valueOf(f.transactionNumber)) + ", "
                + q(f == null ? "" : ProcessData.utc(f.start)) + ", " + q(f == null ? "" : ProcessData.utc(f.end)) + ", "
                + (f == null ? "0" : String.valueOf(f.signatureCounter)) + ", " + q(f == null ? "" : f.signature) + ", "
                + q(f == null ? "" : f.serial) + ", " + q(f == null ? "" : f.algorithm) + ", " + q(f == null ? "" : f.timeFormat) + ", "
                + q(f == null ? "" : f.publicKey) + ", " + q(r.processType) + ", " + q(r.processData) + ", "
                + (r.failed ? 1 : 0) + ", " + q(r.failure) + ", " + q(mpv5.utils.date.DateConverter.getTodayDBDate()) + ")";
        QueryHandler.instanceOf().clone(Context.getInvoice()).freeUpdateQuery(sql, MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE, null);
    }

    /**
     * @param invoice
     * @return the stored values of the sale (column - value), empty if none
     */
    public static Map<String, String> of(int invoice) {
        Map<String, String> m = new HashMap<String, String>();
        try {
            ReturnValue rv = QueryHandler.instanceOf().clone(Context.getInvoice()).freeSelectQuery(
                    "SELECT clientid, tsetype, txnumber, starttime, endtime, sigcounter, signature, tseserial, algorithm, "
                    + "timeformat, publickey, processtype, processdata, tsefailed, failure FROM postransactions WHERE itemsids = " + invoice,
                    MPSecurityManager.VIEW, null);
            if (rv.hasData()) {
                String[] cols = {"clientid", "tsetype", "txnumber", "starttime", "endtime", "sigcounter", "signature", "tseserial",
                    "algorithm", "timeformat", "publickey", "processtype", "processdata", "tsefailed", "failure"};
                Object[] row = rv.getData()[0];
                for (int i = 0; i < cols.length && i < row.length; i++) {
                    m.put(cols[i], row[i] == null ? "" : String.valueOf(row[i]));
                }
            }
        } catch (Exception e) {
            Log.Debug(PosTransactions.class, e);
        }
        return m;
    }
}
