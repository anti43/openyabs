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
package mpv5.utils.numberformat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Invoice arithmetic as defined by EN 16931 (and used by the e-invoice), with
 * exact decimals instead of double:
 * <ol>
 * <li>each line: amount = quantity x price, discount = amount x discount %,
 * line net = amount - discount, each rounded half up to 2 decimals</li>
 * <li>per VAT rate: tax = sum of the line nets x rate, rounded once</li>
 * <li>gross = sum of the line nets + sum of the taxes</li>
 * </ol>
 * So the same service always gives the same totals, independent of how it
 * is split into lines, and the PDF matches the e-invoice to the cent.
 */
public final class InvoiceMath {

    private static final BigDecimal HUNDRED = new BigDecimal(100);

    private InvoiceMath() {
    }

    /**
     * @param value
     * @return rounded half up to 2 decimals (0 for null)
     */
    public static BigDecimal round2(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, RoundingMode.HALF_UP);
    }

    static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * @return quantity x price, rounded (list price of the line, before
     * discount)
     */
    public static BigDecimal lineAmount(BigDecimal quantity, BigDecimal price) {
        return round2(nz(quantity).multiply(nz(price)));
    }

    /**
     * @return the discount of the line: quantity x price x discount %, rounded
     * (the line allowance of the e-invoice)
     */
    public static BigDecimal lineDiscount(BigDecimal quantity, BigDecimal price, BigDecimal discountPercent) {
        if (discountPercent == null || discountPercent.signum() == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return nz(quantity).multiply(nz(price)).multiply(discountPercent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    /**
     * @return the net amount of the line after discount (BT-131)
     */
    public static BigDecimal lineNet(BigDecimal quantity, BigDecimal price, BigDecimal discountPercent) {
        return round2(nz(quantity).multiply(nz(price)).subtract(lineDiscount(quantity, price, discountPercent)));
    }

    /**
     * @return the tax of a single line (for display only, the invoice total
     * is computed per rate, see {@link #totals(List)})
     */
    public static BigDecimal lineTax(BigDecimal quantity, BigDecimal price, BigDecimal discountPercent, BigDecimal taxPercent) {
        return round2(lineNet(quantity, price, discountPercent).multiply(nz(taxPercent)).divide(HUNDRED, 9, RoundingMode.HALF_UP));
    }

    /**
     * An invoice line
     */
    public static final class Line {

        final BigDecimal quantity, price, discountPercent, taxPercent;

        public Line(BigDecimal quantity, BigDecimal price, BigDecimal discountPercent, BigDecimal taxPercent) {
            this.quantity = nz(quantity);
            this.price = nz(price);
            this.discountPercent = nz(discountPercent);
            this.taxPercent = nz(taxPercent);
        }
    }

    /**
     * Totals of an invoice
     */
    public static final class Totals {

        /**
         * sum of quantity x price (before discount)
         */
        public final BigDecimal amount;
        /**
         * sum of the line discounts
         */
        public final BigDecimal discount;
        /**
         * sum of the line nets (after discount)
         */
        public final BigDecimal net;
        /**
         * sum of the taxes per rate
         */
        public final BigDecimal tax;
        /**
         * net + tax
         */
        public final BigDecimal gross;
        /**
         * tax rate -&gt; [taxable amount, tax]
         */
        public final Map<BigDecimal, BigDecimal[]> byRate;

        Totals(BigDecimal amount, BigDecimal discount, BigDecimal net, BigDecimal tax, Map<BigDecimal, BigDecimal[]> byRate) {
            this.amount = amount;
            this.discount = discount;
            this.net = net;
            this.tax = tax;
            this.gross = net.add(tax);
            this.byRate = byRate;
        }
    }

    /**
     * @param lines
     * @return the totals according to EN 16931
     */
    public static Totals totals(List<Line> lines) {
        BigDecimal amount = BigDecimal.ZERO.setScale(2);
        BigDecimal discount = BigDecimal.ZERO.setScale(2);
        BigDecimal net = BigDecimal.ZERO.setScale(2);
        Map<BigDecimal, BigDecimal[]> byRate = new TreeMap<BigDecimal, BigDecimal[]>();
        for (Line l : lines) {
            BigDecimal lineNet = lineNet(l.quantity, l.price, l.discountPercent);
            amount = amount.add(lineAmount(l.quantity, l.price));
            discount = discount.add(lineDiscount(l.quantity, l.price, l.discountPercent));
            net = net.add(lineNet);
            BigDecimal rate = l.taxPercent.stripTrailingZeros();
            BigDecimal[] r = byRate.get(rate);
            if (r == null) {
                r = new BigDecimal[]{BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2)};
                byRate.put(rate, r);
            }
            r[0] = r[0].add(lineNet);
        }
        BigDecimal tax = BigDecimal.ZERO.setScale(2);
        for (Map.Entry<BigDecimal, BigDecimal[]> e : byRate.entrySet()) {
            BigDecimal t = e.getValue()[0].multiply(e.getKey()).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            e.getValue()[1] = t;
            tax = tax.add(t);
        }
        return new Totals(amount, discount, net, tax, byRate);
    }
}
