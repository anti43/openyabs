/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openyabs.gmbh;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import mpv5.globals.Constants;
import mpv5.handler.JournalCalc;
import mpv5.handler.JournalCalc.Entry;
import mpv5.utils.export.EcSalesListExport;
import mpv5.utils.export.VatCategory;

/**
 * A check of the advance VAT return (Umsatzsteuer-Voranmeldung): the amounts
 * of the main fields (Kennzahlen) of the form, computed from the journal of a
 * period. Only a control to compare with the return of the tax adviser or to
 * type into ELSTER - nothing is sent.
 * <ul>
 * <li>accrual basis (Soll-Versteuerung, § 16 UStG, the rule for a GmbH):
 * invoices, deposits, part payments, credit notes and income by document
 * date</li>
 * <li>cash basis (Ist-Versteuerung, § 20 UStG, on application): what was paid
 * in the period (by pay date, payments of invoices paid in parts)</li>
 * <li>input tax (Kz 66): the VAT of the expenses by document date in both
 * cases (deductible with the invoice, § 15 UStG)</li>
 * </ul>
 */
public final class UstvaCalc {

    /**
     * How the VAT is computed
     */
    public enum Basis {
        /**
         * vereinbarte Entgelte (Soll)
         */
        ACCRUAL,
        /**
         * vereinnahmte Entgelte (Ist)
         */
        CASH
    }

    /**
     * the fields of the form used here (the number on the form)
     */
    public static final String KZ_81 = "81", KZ_86 = "86", KZ_87 = "87", KZ_35 = "35", KZ_41 = "41", KZ_21 = "21",
            KZ_43 = "43", KZ_48 = "48", KZ_45 = "45", KZ_66 = "66", KZ_83 = "83";

    private static final BigDecimal R19 = new BigDecimal("19");
    private static final BigDecimal R7 = new BigDecimal("7");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * One field: the base (net, in Euro and Cent as booked; the form takes
     * full Euros, rounded down) and the tax
     */
    public static final class Line {

        public final String kz;
        public BigDecimal base = BigDecimal.ZERO;
        /**
         * the tax as booked (sum of the documents)
         */
        public BigDecimal bookedTax = BigDecimal.ZERO;
        /**
         * the documents that make up the field (number - net)
         */
        public final Map<String, BigDecimal> documents = new LinkedHashMap<String, BigDecimal>();

        Line(String kz) {
            this.kz = kz;
        }

        /**
         * @return the base in full Euros as the form takes it (cents are cut
         * off)
         */
        public BigDecimal formBase() {
            return base.setScale(0, RoundingMode.DOWN);
        }

        void add(String number, BigDecimal net, BigDecimal tax) {
            base = base.add(net);
            bookedTax = bookedTax.add(tax);
            String n = number == null || number.isEmpty() ? "-" : number;
            documents.put(n, documents.getOrDefault(n, BigDecimal.ZERO).add(net));
        }
    }

    /**
     * The result
     */
    public static final class Result {

        public final Basis basis;
        public final Map<String, Line> lines = new LinkedHashMap<String, Line>();
        /**
         * other rates than 19 % and 7 % (Kz 35/36): rate - line
         */
        public final Map<BigDecimal, Line> otherRates = new TreeMap<BigDecimal, Line>();
        public final List<String[]> problems = new ArrayList<String[]>();

        Result(Basis basis) {
            this.basis = basis;
            for (String k : new String[]{KZ_81, KZ_86, KZ_87, KZ_35, KZ_41, KZ_21, KZ_43, KZ_48, KZ_45, KZ_66}) {
                lines.put(k, new Line(k));
            }
        }

        public Line line(String kz) {
            return lines.get(kz);
        }

        /**
         * @return the VAT of Kz 81 as the form computes it: 19 % of the base in
         * full Euros
         */
        public BigDecimal tax81() {
            return pct(line(KZ_81).formBase(), R19);
        }

        /**
         * @return the VAT of Kz 86: 7 % of the base in full Euros
         */
        public BigDecimal tax86() {
            return pct(line(KZ_86).formBase(), R7);
        }

        /**
         * @return the VAT of Kz 36 (other rates), as booked
         */
        public BigDecimal tax36() {
            BigDecimal t = BigDecimal.ZERO;
            for (Line l : otherRates.values()) {
                t = t.add(l.bookedTax);
            }
            return t.setScale(2, RoundingMode.HALF_UP);
        }

        /**
         * @return the input tax (Kz 66)
         */
        public BigDecimal tax66() {
            return line(KZ_66).bookedTax.setScale(2, RoundingMode.HALF_UP);
        }

        /**
         * @return the advance payment (Kz 83): VAT minus input tax; negative:
         * a refund
         */
        public BigDecimal kz83() {
            return tax81().add(tax86()).add(tax36()).subtract(tax66());
        }

        /**
         * @return the booked VAT of the income side (to compare with Kz 81,
         * 86, 36: differences come from rounding to full Euros)
         */
        public BigDecimal bookedVat() {
            return line(KZ_81).bookedTax.add(line(KZ_86).bookedTax).add(tax36()).setScale(2, RoundingMode.HALF_UP);
        }

        void problem(String key, String number) {
            problems.add(new String[]{key, number == null ? "" : number});
        }
    }

    private UstvaCalc() {
    }

    static BigDecimal pct(BigDecimal base, BigDecimal rate) {
        return base.multiply(rate).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    /**
     * @param e
     * @return true for the documents counted on accrual basis
     */
    static boolean isAccrualIncome(Entry e) {
        if (JournalCalc.isCancelled(e)) {
            return false;
        }
        switch (e.type) {
            case Constants.TYPE_INVOICE:
            case Constants.TYPE_CREDIT:
            case Constants.TYPE_DEPOSIT:
            case Constants.TYPE_PART_PAYMENT:
            case Constants.TYPE_REVENUE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Computes the fields
     *
     * @param basis accrual or cash
     * @param income the journal for the income side: by document date
     * (accrual) or by pay date with {@link JournalCalc#compute} applied
     * (cash)
     * @param expenses the journal by document date (its expenses are the
     * input tax)
     * @param inspector the VAT categories of the documents and the VAT IDs of
     * the customers ({@link EcSalesListExport#dbInspector()})
     * @return the fields
     */
    public static Result compute(Basis basis, List<Entry> income, List<Entry> expenses, EcSalesListExport.EntryInspector inspector) {
        Result r = new Result(basis);
        for (Entry e : income) {
            if (e == null || !e.isIncomeSide()) {
                continue;
            }
            boolean counts = basis == Basis.ACCRUAL ? isAccrualIncome(e)
                    : e.counted && e.type != Constants.TYPE_OFFER && e.type != Constants.TYPE_ORDER;
            if (!counts) {
                continue;
            }
            if (basis == Basis.ACCRUAL && (e.type == Constants.TYPE_DEPOSIT || e.type == Constants.TYPE_PART_PAYMENT)) {
                r.problem("ustva.problem.deposit", e.number);
            }
            income(r, e, e.type == Constants.TYPE_CREDIT ? BigDecimal.ONE.negate() : BigDecimal.ONE, inspector);
        }
        for (Entry e : expenses) {
            if (e == null || e.type != Constants.TYPE_EXPENSE) {
                continue;
            }
            //the VAT of the expenses; reverse charge purchases (§ 13b UStG, Kz 46/47, 84/85) are not covered
            r.line(KZ_66).add(e.number, nz(e.net), nz(e.tax));
        }
        return r;
    }

    private static void income(Result r, Entry e, BigDecimal sign, EcSalesListExport.EntryInspector inspector) {
        Map<BigDecimal, BigDecimal[]> rates = e.rates == null || e.rates.isEmpty() ? null : e.rates;
        if (rates == null) {
            rates = new TreeMap<BigDecimal, BigDecimal[]>(JournalCalc.RATE_ORDER);
            rates.put(null, new BigDecimal[]{nz(e.net), nz(e.tax)});
        }
        BigDecimal zero = BigDecimal.ZERO;
        for (Map.Entry<BigDecimal, BigDecimal[]> re : rates.entrySet()) {
            BigDecimal rate = re.getKey();
            BigDecimal net = nz(re.getValue()[0]).multiply(sign);
            BigDecimal tax = nz(re.getValue().length > 1 ? re.getValue()[1] : null).multiply(sign);
            if (net.signum() == 0 && tax.signum() == 0) {
                continue;
            }
            if (rate == null) {
                if (tax.signum() != 0) {
                    //a tax of unknown rate: from the amounts
                    rate = net.signum() == 0 ? null : tax.multiply(HUNDRED).divide(net, 0, RoundingMode.HALF_UP);
                    r.problem("ustva.problem.unknownrate", e.number);
                    if (rate == null) {
                        continue;
                    }
                } else {
                    zero = zero.add(net);
                    continue;
                }
            }
            if (rate.signum() == 0) {
                zero = zero.add(net);
            } else if (rate.compareTo(R19) == 0) {
                r.line(KZ_81).add(e.number, net, tax);
            } else if (rate.compareTo(R7) == 0) {
                r.line(KZ_86).add(e.number, net, tax);
            } else {
                BigDecimal k = JournalCalc.rateKey(rate);
                Line l = r.otherRates.get(k);
                if (l == null) {
                    l = new Line(KZ_35);
                    r.otherRates.put(k, l);
                }
                l.add(e.number, net, tax);
                r.line(KZ_35).add(e.number, net, tax);
            }
        }
        if (zero.signum() != 0) {
            zeroRated(r, e, zero, inspector);
        }
    }

    /**
     * Splits the amount without VAT by the VAT categories of the document
     * (in the proportion of their net amounts)
     */
    private static void zeroRated(Result r, Entry e, BigDecimal amount, EcSalesListExport.EntryInspector inspector) {
        Map<VatCategory, BigDecimal> cats = new LinkedHashMap<VatCategory, BigDecimal>();
        if (inspector != null && (e.itemId > 0 || e.obj instanceof mpv5.db.objects.Item)) {
            try {
                Map<VatCategory, BigDecimal> all = inspector.netByCategory(e);
                if (all != null) {
                    for (Map.Entry<VatCategory, BigDecimal> c : all.entrySet()) {
                        if (c.getKey() != null && c.getKey() != VatCategory.S && c.getValue() != null && c.getValue().signum() != 0) {
                            cats.put(c.getKey(), c.getValue().abs());
                        }
                    }
                }
            } catch (Exception ex) {
                mpv5.logging.Log.Debug(UstvaCalc.class, ex.getMessage());
            }
        }
        if (cats.isEmpty()) {
            //0 % without category (e.g. income without VAT): not taxable / not assigned
            r.line(KZ_45).add(e.number, amount, BigDecimal.ZERO);
            r.problem("ustva.problem.nocategory", e.number);
            return;
        }
        List<BigDecimal> weights = new ArrayList<BigDecimal>(cats.values());
        List<BigDecimal> parts = JournalCalc.distribute(amount, weights);
        int i = 0;
        for (VatCategory c : cats.keySet()) {
            BigDecimal part = i < parts.size() ? parts.get(i) : BigDecimal.ZERO;
            i++;
            String kz = kzOf(c);
            if (c == VatCategory.K) {
                String vatId = inspector.vatIdOf(e);
                if (!EcSalesListExport.isOtherEuVatId(vatId)) {
                    r.problem("ustva.problem.euvatid", e.number);
                }
            } else if (c == VatCategory.AE) {
                String vatId = inspector.vatIdOf(e);
                if (!EcSalesListExport.isOtherEuVatId(vatId)) {
                    //domestic reverse charge (§ 13b Abs. 5 UStG, Kz 60) or a customer outside the EU (Kz 45)
                    kz = KZ_45;
                    r.problem("ustva.problem.reversecharge", e.number);
                }
            }
            r.line(kz).add(e.number, part, BigDecimal.ZERO);
        }
    }

    /**
     * @param c
     * @return the field of the category of a supply without VAT
     */
    static String kzOf(VatCategory c) {
        switch (c) {
            case K:
                return KZ_41;
            case AE:
                return KZ_21;
            case G:
                return KZ_43;
            case E:
                return KZ_48;
            case Z:
                return KZ_87;
            default:
                return KZ_45;
        }
    }

    static BigDecimal nz(BigDecimal b) {
        return b == null ? BigDecimal.ZERO : b;
    }
}
