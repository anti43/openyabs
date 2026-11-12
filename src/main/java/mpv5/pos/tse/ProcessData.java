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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * The data a TSE signs for a sale and the content of the receipt QR code, as
 * described in DSFinV-K / BSI TR-03153 (to be checked against the current
 * versions before productive use)
 */
public final class ProcessData {

    private ProcessData() {
    }

    private static final DateTimeFormatter UTC = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    /**
     * @return e.g. 2026-11-12T09:15:02.123Z
     */
    public static String utc(Instant i) {
        return i == null ? "" : UTC.format(i);
    }

    /**
     * Position of a VAT rate in the process data: general (19 %, 16 %),
     * reduced (7 %, 5 %), average 10.7 %, average 5.5 %, 0 %
     */
    static int slot(BigDecimal rate) {
        double r = rate == null ? 0 : rate.doubleValue();
        if (r == 0) {
            return 4;
        } else if (Math.abs(r - 7) < 0.001 || Math.abs(r - 5) < 0.001) {
            return 1;
        } else if (Math.abs(r - 10.7) < 0.001) {
            return 2;
        } else if (Math.abs(r - 5.5) < 0.001) {
            return 3;
        }
        return 0;
    }

    private static String amount(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Process data of a sale (Kassenbeleg-V1):
     * Beleg^gross per VAT rate (19_7_10.7_5.5_0)^payment:type
     *
     * @param grossPerRate gross amounts by VAT rate in percent
     * @param paid the amount paid
     * @param cash true: cash (Bar), false: card etc. (Unbar)
     * @return e.g. Beleg^11.90_2.14_0.00_0.00_0.00^14.04:Bar
     */
    public static String kassenbeleg(Map<BigDecimal, BigDecimal> grossPerRate, BigDecimal paid, boolean cash) {
        BigDecimal[] slots = {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        for (Map.Entry<BigDecimal, BigDecimal> e : grossPerRate.entrySet()) {
            int s = slot(e.getKey());
            slots[s] = slots[s].add(e.getValue());
        }
        StringBuilder b = new StringBuilder("Beleg^");
        for (int i = 0; i < slots.length; i++) {
            b.append(i == 0 ? "" : "_").append(amount(slots[i]));
        }
        return b.append('^').append(amount(paid)).append(':').append(cash ? "Bar" : "Unbar").toString();
    }

    /**
     * Content of the QR code of a receipt (V0;register;process type;process
     * data;transaction;counter;start;end;algorithm;time format;signature;key)
     */
    public static String qr(String registerSerial, String processType, String processData, Tse.Finished f) {
        return String.join(";", "V0", registerSerial, processType, processData, String.valueOf(f.transactionNumber),
                String.valueOf(f.signatureCounter), utc(f.start), utc(f.end), f.algorithm, f.timeFormat, f.signature,
                f.publicKey == null ? "" : f.publicKey);
    }
}
