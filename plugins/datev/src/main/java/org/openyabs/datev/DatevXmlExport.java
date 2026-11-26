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
package org.openyabs.datev;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import mpv5.db.objects.Account;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.globals.Constants;
import mpv5.handler.JournalCalc;
import mpv5.handler.JournalCalc.Entry;
import mpv5.logging.Log;
import mpv5.utils.export.EcSalesListExport;
import mpv5.utils.export.VatCategory;
import mpv5.utils.export.ZugpferdFile;
import org.mustangproject.Invoice;

/**
 * Collects the outgoing invoices of a period for {@link DatevXml}, by their
 * document date (Rechnungsdatum) and independent of the filters of the
 * journal: invoices, deposits, part payments and credit notes, paid or not.
 * <ul>
 * <li>Amounts: the parts by rate of the journal (adding up to the document
 * exactly); parts without VAT split by their VAT category; accounts and BU
 * keys as in the DATEV booking batch ({@link DatevMapper#booking}).</li>
 * <li>Cancelled invoices: exported if a credit note cancels them (GoBD
 * Stornorechnung: the invoice was issued, invoice and cancellation net to
 * zero in DATEV); without one they were never issued and are left out.</li>
 * <li>Files: the archived original (PDF, ZUGFeRD or XRechnung) if a plugin
 * such as GoBD keeps one, else the XRechnung for customers who get
 * e-invoices; never a second, different "original".</li>
 * </ul>
 */
final class DatevXmlExport {

    /**
     * A document that is not exported, and why
     */
    static final class Excluded {

        final String number;
        final Date date;
        final String kind;
        final String customer;
        final BigDecimal gross;
        final String reason;

        Excluded(String number, Date date, String kind, String customer, BigDecimal gross, String reason) {
            this.number = number;
            this.date = date;
            this.kind = kind;
            this.customer = customer;
            this.gross = gross;
            this.reason = reason;
        }
    }

    /**
     * what was collected
     */
    static final class Collected {

        final List<DatevXml.Doc> docs = new ArrayList<DatevXml.Doc>();
        final List<Excluded> excluded = new ArrayList<Excluded>();
        final List<String> problems = new ArrayList<String>();
        final List<String> tooLarge = new ArrayList<String>();
        /**
         * the documents with a 0 % part without VAT category (booked as
         * tax-free domestic sale) and their accounts
         */
        final List<String> zeroDomestic = new ArrayList<String>();
        final Set<String> zeroAccounts = new java.util.TreeSet<String>();
        int einvoices;
        int originals;

        /**
         * @return net, VAT and gross of all documents (credit notes negative)
         */
        BigDecimal[] totals() {
            BigDecimal n = BigDecimal.ZERO;
            BigDecimal t = BigDecimal.ZERO;
            for (DatevXml.Doc d : docs) {
                n = n.add(d.net());
                t = t.add(d.tax());
            }
            return new BigDecimal[]{n, t, n.add(t)};
        }
    }

    private DatevXmlExport() {
    }

    /**
     * @param type
     * @return true for invoices, credit notes, deposits and part payments
     */
    static boolean isOutgoing(int type) {
        return type == Constants.TYPE_INVOICE || type == Constants.TYPE_CREDIT
                || type == Constants.TYPE_DEPOSIT || type == Constants.TYPE_PART_PAYMENT;
    }

    static String kind(int type) {
        switch (type) {
            case Constants.TYPE_CREDIT:
                return "Gutschrift";
            case Constants.TYPE_DEPOSIT:
                return "Anzahlungsrechnung";
            case Constants.TYPE_PART_PAYMENT:
                return "Abschlagsrechnung";
            default:
                return "Rechnung";
        }
    }

    /**
     * @return the ids of the invoices a credit note in the database cancels
     * or corrects (not in the trash bin)
     */
    static Set<Integer> cancelledByCreditNote() {
        Set<Integer> s = new HashSet<Integer>();
        try {
            mpv5.db.common.ReturnValue rv = mpv5.db.common.QueryHandler.instanceOf().clone(mpv5.db.common.Context.getCredit())
                    .freeSelectQuery("SELECT refinvoiceids FROM items WHERE refinvoiceids > 0 AND invisible = 0 AND inttype = "
                            + Item.TYPE_CREDIT, //NOI18N
                            mpv5.usermanagement.MPSecurityManager.VIEW, null);
            if (rv != null && rv.hasData()) {
                for (Object[] r : rv.getData()) {
                    if (r[0] != null) {
                        s.add(Integer.valueOf(String.valueOf(r[0])));
                    }
                }
            }
        } catch (Exception e) {
            Log.Debug(DatevXmlExport.class, e);
        }
        return s;
    }

    /**
     * Loads the outgoing documents of the period by document date (all of
     * them, the filters of the journal do not apply) and collects them
     *
     * @param from
     * @param to
     * @param config
     * @param problemText (number, reason) - text for an invoice without a
     * valid XRechnung
     * @return the documents
     * @throws Exception
     */
    static Collected load(Date from, Date to, DatevConfig config, BiFunction<String, String, String> problemText) throws Exception {
        List<Entry> entries = mpv5.handler.JournalLoader.loadAll(new mpv5.utils.date.vTimeframe(from, to), false);
        EcSalesListExport.EntryInspector baseInsp = EcSalesListExport.dbInspector();
        DatevMapper.ZeroCategoryLookup zeroLookup = DatevMapper.dbZeroCategoryLookup();
        EcSalesListExport.EntryInspector datevInsp = new EcSalesListExport.EntryInspector() {
            @Override
            public String vatIdOf(Entry entry) {
                return baseInsp.vatIdOf(entry);
            }

            @Override
            public String customerNameOf(Entry entry) {
                return baseInsp.customerNameOf(entry);
            }

            @Override
            public Map<VatCategory, BigDecimal> netByCategory(Entry entry) {
                Map<VatCategory, BigDecimal> m = zeroLookup.zeroNetByCategory(entry);
                return m != null ? m : Collections.emptyMap();
            }
        };
        return collect(entries, config, cancelledByCreditNote(), DatevMapper.dbAccountLookup(),
                datevInsp, configuredRates(), true, problemText);
    }

    static Set<BigDecimal> configuredRates() {
        Set<BigDecimal> rates = new HashSet<BigDecimal>();
        try {
            for (mpv5.db.objects.Tax t : mpv5.db.objects.Tax.all()) {
                if (t.__getTaxvalue() != null) {
                    rates.add(JournalCalc.rateKey(t.__getTaxvalue()));
                }
            }
        } catch (Exception e) {
            Log.Debug(DatevXmlExport.class, e.getMessage());
        }
        return rates;
    }

    /**
     * @param entries the journal by document date
     * @param config the DATEV settings
     * @param cancelledByCredit the ids of the invoices a credit note cancels
     * @param accounts the accounts of the documents (may be null)
     * @param insp the VAT categories and VAT IDs (may be null)
     * @param rates the configured rates (for a part of unknown rate)
     * @param withFiles add the archived originals and XRechnung files (needs
     * the database)
     * @param problemText (number, reason) - text
     * @return the documents
     */
    static Collected collect(List<Entry> entries, DatevConfig config, Set<Integer> cancelledByCredit,
            DatevMapper.AccountLookup accounts, EcSalesListExport.EntryInspector insp, Collection<BigDecimal> rates,
            boolean withFiles, BiFunction<String, String, String> problemText) {
        Collected c = new Collected();
        Set<Integer> seen = new HashSet<Integer>();
        for (Entry e : entries) {
            if (e == null || !isOutgoing(e.type) || e.type == Constants.TYPE_PAYMENT) {
                continue;
            }
            Item item = e.obj instanceof Item ? (Item) e.obj : null;
            int id = item != null ? item.__getIDS() : e.itemId;
            if (id > 0 && !seen.add(id)) {
                continue;
            }
            Date date = item != null && item.__getDateadded() != null ? item.__getDateadded() : e.date;
            String customer = (nvl(e.contact) + " " + nvl(e.prename)).trim();
            boolean cancelled = JournalCalc.isCancelled(e);
            if (cancelled && (cancelledByCredit == null || !cancelledByCredit.contains(id))) {
                c.excluded.add(new Excluded(e.number, date, kind(e.type), customer, e.gross,
                        "storniert ohne Stornorechnung (gilt als nie ausgestellt)"));
                continue;
            }
            if (nz(e.gross).signum() == 0 && nz(e.net).signum() == 0) {
                c.excluded.add(new Excluded(e.number, date, kind(e.type), customer, e.gross, "Betrag 0,00"));
                continue;
            }
            DatevXml.Doc d = new DatevXml.Doc();
            d.kind = kind(e.type) + (cancelled ? " (storniert)" : "");
            d.cancelled = cancelled;
            d.number = nvl(e.number);
            d.date = date;
            d.credit = e.type == Constants.TYPE_CREDIT;
            d.text = nvl(e.description);
            d.customerName = customer;
            d.customerCity = nvl(e.city);
            Contact k = null;
            if (item != null) {
                d.deliveryDate = item.__getDatetodo();
                if (!d.credit && item.__getIntstatus() == Item.STATUS_PAID) {
                    d.paidAt = item.__getDateend();
                }
                try {
                    k = item.getContact();
                } catch (Exception ex) {
                    Log.Debug(DatevXmlExport.class, ex.getMessage());
                }
                try {
                    String cur = ZugpferdFile.findCurrency(item, k);
                    if (cur != null && !cur.trim().isEmpty()) {
                        d.currency = cur.trim();
                    }
                } catch (Exception ex) {
                    Log.Debug(DatevXmlExport.class, ex.getMessage());
                }
            }
            if (k != null) {
                d.customerNumber = nvl(k.__getCNumber());
                if (d.customerName.isEmpty()) {
                    d.customerName = ZugpferdFile.firstNonEmpty(k.__getCompany(), ZugpferdFile.join(" ", k.__getPrename(), k.__getCname()));
                }
                if (d.customerCity.isEmpty()) {
                    d.customerCity = nvl(k.__getCity());
                }
                if (!d.credit && d.paidAt == null && d.date != null && k.__getPayterm() > 0) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(d.date);
                    cal.add(java.util.Calendar.DAY_OF_MONTH, k.__getPayterm());
                    d.dueDate = cal.getTime();
                }
            }
            Map<VatCategory, BigDecimal> cats = Collections.emptyMap();
            if (insp != null) {
                try {
                    d.vatId = nvl(insp.vatIdOf(e));
                    Map<VatCategory, BigDecimal> m = insp.netByCategory(e);
                    if (m != null) {
                        cats = m;
                    }
                } catch (Exception ex) {
                    Log.Debug(DatevXmlExport.class, ex.getMessage());
                }
            }
            Account docAccount = accounts == null ? null : accounts.accountOf(e);
            lines(c, d, e, cats, docAccount, config, accounts, rates);
            if (withFiles && item != null) {
                original(c, d, item);
                //without an archived original: the XRechnung for customers who get e-invoices (what they got),
                //nothing for customers who get plain PDFs (no second, different "original")
                if (d.files.isEmpty() && k != null && mpv5.utils.export.EInvoiceDelivery.effectiveFor(k, item).isEInvoice()) {
                    eInvoice(c, d, item, problemText);
                }
            }
            c.docs.add(d);
        }
        if (!c.zeroDomestic.isEmpty()) {
            c.problems.add(DatevMapper.zeroDomesticNote(c.zeroDomestic, c.zeroAccounts, config));
        }
        return c;
    }

    /**
     * The lines of a document: one per rate of the journal, parts without VAT
     * by VAT category; the lines add up to net and VAT of the document
     */
    static void lines(Collected c, DatevXml.Doc d, Entry e, Map<VatCategory, BigDecimal> cats, Account docAccount,
            DatevConfig config, DatevMapper.AccountLookup accounts, Collection<BigDecimal> rates) {
        Map<BigDecimal, BigDecimal[]> parts = e.rates;
        if (parts == null || parts.isEmpty()) {
            parts = new TreeMap<BigDecimal, BigDecimal[]>(JournalCalc.RATE_ORDER);
            parts.put(null, new BigDecimal[]{nz(e.net), nz(e.tax)});
        }
        String vat = DatevFormat.sanitizeEuVatId(d.vatId);
        for (Map.Entry<BigDecimal, BigDecimal[]> p : parts.entrySet()) {
            BigDecimal rawNet = nz(p.getValue()[0]);
            BigDecimal rawTax = nz(p.getValue().length > 1 ? p.getValue()[1] : null);
            BigDecimal net = d.credit ? rawNet.abs() : rawNet;
            BigDecimal tax = d.credit ? rawTax.abs() : rawTax;
            if (net.signum() == 0 && tax.signum() == 0) {
                continue;
            }
            BigDecimal rate = p.getKey();
            if (rate == null) {
                if (tax.signum() != 0 && net.signum() != 0) {
                    //a part of unknown rate: the rate from its amounts
                    rate = JournalCalc.normalizeRate(tax.abs().multiply(new BigDecimal(100)).divide(net.abs(), 4, RoundingMode.HALF_UP), rates);
                    c.problems.add(d.number + ": Steuersatz unbekannt, aus den Beträgen ermittelt (" + rate.stripTrailingZeros().toPlainString() + " %).");
                } else {
                    rate = BigDecimal.ZERO;
                }
            }
            rate = JournalCalc.rateKey(rate);
            if (rate.signum() != 0) {
                d.lines.add(line(c, d, rate, net, tax, null, docAccount, vat, config, accounts));
                continue;
            }
            //without VAT: by VAT category (intra-community supply, reverse charge, export, exempt ...)
            Map<VatCategory, BigDecimal> zero = new LinkedHashMap<VatCategory, BigDecimal>();
            for (Map.Entry<VatCategory, BigDecimal> cat : cats.entrySet()) {
                if (cat.getKey() != VatCategory.S && cat.getValue() != null && cat.getValue().signum() != 0) {
                    zero.put(cat.getKey(), cat.getValue().abs());
                }
            }
            if (zero.size() <= 1) {
                d.lines.add(line(c, d, rate, net, BigDecimal.ZERO, zero.isEmpty() ? null : zero.keySet().iterator().next(),
                        docAccount, vat, config, accounts));
            } else {
                List<BigDecimal> split = JournalCalc.distribute(net.abs(), new ArrayList<BigDecimal>(zero.values()));
                int i = 0;
                for (VatCategory cat : zero.keySet()) {
                    BigDecimal partNet = net.signum() < 0 ? split.get(i++).negate() : split.get(i++);
                    d.lines.add(line(c, d, rate, partNet, BigDecimal.ZERO, cat, docAccount, vat, config, accounts));
                }
                if (tax.signum() != 0) {
                    d.lines.get(d.lines.size() - 1).tax = tax;
                }
            }
        }
        //the check: the lines are the document
        BigDecimal expected = d.credit
                ? nz(e.net).abs().add(nz(e.tax).abs())
                : nz(e.net).add(nz(e.tax));
        BigDecimal written = BigDecimal.ZERO;
        for (DatevXml.Line l : d.lines) {
            written = written.add(l.gross().setScale(2, RoundingMode.HALF_UP));
        }
        if (written.compareTo(expected.setScale(2, RoundingMode.HALF_UP)) != 0) {
            c.problems.add(d.number + ": Summe der Zeilen " + DatevFormat.formatAmount(written) + " weicht vom Beleg ab ("
                    + DatevFormat.formatAmount(expected) + ").");
        }
    }

    private static DatevXml.Line line(Collected c, DatevXml.Doc d, BigDecimal rate, BigDecimal net, BigDecimal tax, VatCategory cat,
            Account docAccount, String vat, DatevConfig config, DatevMapper.AccountLookup accounts) {
        DatevMapper.Booking b = DatevMapper.booking(rate, false, docAccount, cat, vat, config, accounts);
        DatevXml.Line l = new DatevXml.Line(rate, net, tax, b.account);
        l.bu = b.bu;
        l.category = cat;
        if (DatevConfig.isBlank(b.account)) {
            c.problems.add(d.number + ": kein Erlöskonto für " + rate.stripTrailingZeros().toPlainString()
                    + " % (wird in DATEV zugeordnet; Konten unter " + DatevConfig.SETTINGS_PATH + ").");
        } else if (DatevXml.accountNo(b.account).isEmpty()) {
            c.problems.add(d.number + ": Konto " + b.account + " ist keine 4- bis 8-stellige Kontonummer (wird in DATEV zugeordnet).");
        }
        if (b.uncategorizedZero && !c.zeroDomestic.contains(d.number)) {
            c.zeroDomestic.add(d.number);
            c.zeroAccounts.add(b.account);
        }
        if (b.exportAccountForNonExport) {
            c.problems.add(DatevMapper.exportAccountNote(d.number, b.account, config));
        }
        if (b.goodsAssumed) {
            c.problems.add(d.number + ": 0 %-Umsatz an ein Unternehmen in der EU ohne USt-Kategorie, als innergemeinschaftliche"
                    + " Lieferung (Konto " + b.account + ") gebucht. Für Leistungen dem Kunden einen Steuersatz mit Kategorie AE zuordnen.");
        }
        return l;
    }

    /**
     * the archived original of a locked (finalized) invoice
     */
    private static void original(Collected c, DatevXml.Doc d, Item item) {
        try {
            mpv5.plugin.ExtensionManager.LockedDocument ld = mpv5.plugin.ExtensionManager.instance().lockedDocument(item);
            if (ld != null && ld.file != null && ld.file.isFile()) {
                String name = ld.file.getName().toLowerCase();
                if (ld.file.length() > DatevXml.MAX_FILE) {
                    c.tooLarge.add(d.number);
                } else if (name.endsWith(".pdf")) {
                    //the PDF (or ZUGFeRD) that was sent: the invoice image
                    d.files.put(DatevXml.imageName(d) + ".pdf", Files.readAllBytes(ld.file.toPath()));
                    c.originals++;
                } else if (name.endsWith(".xml")) {
                    //an archived XRechnung is the original e-invoice
                    d.files.put(DatevXml.imageName(d) + ".xml", Files.readAllBytes(ld.file.toPath()));
                    c.einvoices++;
                }
            }
        } catch (Throwable t) {
            Log.Debug(DatevXmlExport.class, t.getMessage());
        }
    }

    private static void eInvoice(Collected c, DatevXml.Doc d, Item item, BiFunction<String, String, String> problemText) {
        try {
            if (!ZugpferdFile.supports(item)) {
                return;
            }
            Invoice inv = ZugpferdFile.createInvoice(item, ZugpferdFile.Format.XRECHNUNG);
            List<String> p = ZugpferdFile.validate(inv);
            if (!p.isEmpty()) {
                c.problems.add(problemText.apply(d.number, String.join(", ", p)));
                return;
            }
            File tmp = File.createTempFile("yabs-xrechnung", ".xml");
            try {
                ZugpferdFile.writeXml(inv, tmp);
                d.files.put(DatevXml.imageName(d) + ".xml", Files.readAllBytes(tmp.toPath()));
                c.einvoices++;
            } finally {
                tmp.delete();
            }
        } catch (Exception ex) {
            Log.Debug(DatevXmlExport.class, ex);
            c.problems.add(problemText.apply(d.number, ex.getMessage()));
        }
    }

    static BigDecimal nz(BigDecimal b) {
        return b == null ? BigDecimal.ZERO : b;
    }

    static String nvl(String s) {
        return s == null || "null".equals(s) ? "" : s;
    }
}
