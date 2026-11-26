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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mpv5.db.objects.Item;
import mpv5.globals.Constants;
import mpv5.handler.JournalCalc;
import mpv5.handler.JournalCalc.Entry;
import org.openyabs.datev.DatevExportDialog.Content;
import org.openyabs.datev.DatevExportDialog.Table;
import org.openyabs.datev.DatevFormat.BookingLine;

/**
 * What the dialogs of the DATEV exports show: period, selection, dates,
 * sums, every booking / document and what is left out
 */
final class DatevExportContent {

    private DatevExportContent() {
    }

    static String day(Date d) {
        return d == null ? "" : new SimpleDateFormat("dd.MM.yyyy").format(d);
    }

    static String period(Date from, Date to) {
        return day(from) + " – " + day(to);
    }

    /**
     * @param config
     * @return how the booking batch selects and dates the documents
     */
    static boolean byPayDate(DatevConfig config) {
        return config.belegdatumMode != DatevConfig.DateMode.DOCUMENT_DATE;
    }

    /**
     * The booking batch
     *
     * @param config
     * @param from
     * @param to
     * @param entries the documents the batch was made of (by pay date or
     * document date, all, without the filters of the journal)
     * @param periodDocs the documents of the period by document date (to
     * list what is not booked, and why)
     * @param result the bookings
     * @param fileName
     * @return the content of the dialog
     */
    static Content batch(DatevConfig config, Date from, Date to, List<Entry> entries, List<Entry> periodDocs,
            DatevMapper.Result result, String fileName) {
        Content c = new Content("DATEV-Buchungsstapel prüfen und speichern");
        boolean pay = byPayDate(config);
        c.fact("Zeitraum:", period(from, to));
        c.fact("Datei:", fileName + " (EXTF 700, Formatversion 13)");
        c.fact("Enthalten:", (pay
                ? "alle bezahlten Rechnungen, Gutschriften, Abschlags-/Anzahlungsrechnungen, Einnahmen und Ausgaben, deren Zahlungsdatum im Zeitraum liegt; Teilzahlungen einzeln mit ihrem Datum"
                : "alle bezahlten Rechnungen, Gutschriften, Abschlags-/Anzahlungsrechnungen, Einnahmen und Ausgaben, deren Belegdatum im Zeitraum liegt")
                + ". Die Filter des Journals gelten für den Export nicht.");
        c.fact("Buchungsdatum:", pay ? "Zahlungsdatum (Ist-Versteuerung / EÜR)" : "Belegdatum (Rechnungsdatum)");
        c.fact("Mandant:", "Berater " + (config.beraterNummer > 0 ? config.beraterNummer : "–") + ", Mandant "
                + (config.mandantenNummer > 0 ? config.mandantenNummer : "–") + ", SKR" + config.kontenrahmen
                + ", Geldkonto " + config.moneyAccountIncome + (config.moneyAccountExpense.equals(config.moneyAccountIncome) ? "" : " / " + config.moneyAccountExpense));
        BigDecimal in = result.sumDebit();
        BigDecimal out = result.sumCredit();
        c.fact("Summen:", "Zugänge (Soll, Einnahmen) " + DatevExportDialog.money(in) + " € · Abgänge (Haben, Ausgaben/Gutschriften) "
                + DatevExportDialog.money(out) + " € · Saldo " + DatevExportDialog.money(in.subtract(out)) + " € · "
                + result.lines.size() + " Buchungszeilen");

        Table t = new Table("Buchungen", "Datum", "Leistungsdatum", "Beleg", "Buchungstext", "Umsatz", "WKZ", "S/H", "Konto", "Gegenkonto", "BU", "EU-USt-IdNr.");
        for (BookingLine l : result.lines) {
            String ld = l.leistungsdatum != null && l.leistungsdatum.length() == 8
                    ? l.leistungsdatum.substring(0, 2) + "." + l.leistungsdatum.substring(2, 4) + "." + l.leistungsdatum.substring(4)
                    : "";
            t.row(ddmm(l.belegdatum, from, to), ld, l.belegfeld1, l.buchungstext, l.umsatz, l.wkz, l.sollHaben, l.konto, l.gegenkonto, l.buSchluessel, l.euLandUstId);
        }
        t.row("", "", "", "Zugänge / Abgänge / Saldo", in.subtract(out), "", "", "", "", "", "");
        t.sumRow = true;
        c.tables.add(t);
        c.tables.add(notBooked(entries, periodDocs, from, to));
        for (DatevMapper.Issue i : result.issues) {
            (i.severity == DatevMapper.Issue.Severity.ERROR ? c.errors : c.notes).add(i.message);
        }
        c.blockOnErrors = !c.errors.isEmpty();
        return c;
    }

    /**
     * @param ddmm the day of a booking (TTMM)
     * @return the day with the year of the period
     */
    private static String ddmm(String ddmm, Date from, Date to) {
        if (ddmm == null || ddmm.length() != 4) {
            return String.valueOf(ddmm);
        }
        String y = new SimpleDateFormat("yyyy").format(to != null ? to : from != null ? from : new Date());
        return ddmm.substring(0, 2) + "." + ddmm.substring(2) + "." + y;
    }

    /**
     * @param e
     * @return the record of an entry (a payment: its invoice)
     */
    static String key(Entry e) {
        if (e.type == Constants.TYPE_PAYMENT || e.obj instanceof Item) {
            return "item:" + e.itemId;
        }
        return e.obj == null ? "?" + System.identityHashCode(e)
                : e.obj.getClass().getSimpleName() + ":" + ((mpv5.db.common.DatabaseObject) e.obj).__getIDS();
    }

    /**
     * The documents of the period (by document date) that are not booked in
     * the batch, or only in part, and why
     *
     * @param entries what the batch was made of
     * @param periodDocs the documents of the period by document date
     * @param from
     * @param to
     * @return the table
     */
    static Table notBooked(List<Entry> entries, List<Entry> periodDocs, Date from, Date to) {
        Table t = new Table("Nicht oder nur teilweise im Stapel", "Datum", "Beleg", "Art", "Kunde / Text", "Brutto", "Grund");
        Set<String> booked = new HashSet<String>();
        Set<String> partly = new HashSet<String>();
        for (Entry e : entries) {
            if (e != null && e.counted) {
                booked.add(key(e));
                if (e.type == Constants.TYPE_PAYMENT) {
                    partly.add(key(e));
                }
            }
        }
        for (Entry e : periodDocs) {
            if (e == null || !e.isAccounting() || e.type == Constants.TYPE_PAYMENT) {
                continue;
            }
            String k = key(e);
            String reason;
            if (partly.contains(k)) {
                reason = "teilweise: die Teilzahlungen im Zeitraum sind gebucht, weitere Zahlungen im Stapel ihres Zeitraums";
            } else if (booked.contains(k)) {
                continue;
            } else if (JournalCalc.isCancelled(e)) {
                reason = "storniert";
            } else if (JournalCalc.isOpen(e)) {
                reason = "offen (nicht bezahlt) – wird mit der Zahlung gebucht";
            } else {
                Date paid = payDate(e);
                reason = paid != null && DatevMapper.outside(paid, from, to)
                        ? "bezahlt am " + day(paid) + " – im Stapel dieses Zeitraums"
                        : "nicht buchungsrelevant";
            }
            t.row(day(e.date), e.number, kind(e), (nvl(e.contact) + " " + nvl(e.prename) + " " + nvl(e.description)).trim(), e.gross, reason);
        }
        return t;
    }

    private static Date payDate(Entry e) {
        if (e.obj instanceof Item) {
            return ((Item) e.obj).__getDateend();
        } else if (e.obj instanceof mpv5.db.objects.Expense) {
            return ((mpv5.db.objects.Expense) e.obj).__getDateend();
        } else if (e.obj instanceof mpv5.db.objects.Revenue) {
            return ((mpv5.db.objects.Revenue) e.obj).__getDateend();
        }
        return null;
    }

    static String kind(Entry e) {
        switch (e.type) {
            case Constants.TYPE_EXPENSE:
                return "Ausgabe";
            case Constants.TYPE_REVENUE:
                return "Einnahme";
            case Constants.TYPE_PAYMENT:
                return "Zahlung";
            default:
                return DatevXmlExport.kind(e.type);
        }
    }

    /**
     * The document package
     *
     * @param col the documents
     * @param from
     * @param to
     * @param folder the folder in Belege online
     * @param fileName
     * @param settingsSaved false: the standard accounts are used
     * @param chart the chart of accounts (03 / 04)
     * @return the content of the dialog
     */
    static Content documentPackage(DatevXmlExport.Collected col, Date from, Date to, String folder, String fileName,
            boolean settingsSaved, String chart) {
        Content c = new Content("DATEV-Belegpaket prüfen und speichern");
        int invoices = 0;
        int credits = 0;
        int withOriginal = 0;
        int withXml = 0;
        for (DatevXml.Doc d : col.docs) {
            if (d.credit) {
                credits++;
            } else {
                invoices++;
            }
            boolean pdf = false;
            boolean xml = false;
            for (String f : d.files.keySet()) {
                pdf |= f.endsWith(".pdf");
                xml |= f.endsWith(".xml");
            }
            withOriginal += pdf ? 1 : 0;
            withXml += xml ? 1 : 0;
        }
        BigDecimal[] sum = col.totals();
        c.fact("Zeitraum:", period(from, to));
        c.fact("Datei:", fileName + " (DATEV XML-Schnittstelle online 6.0, für DATEV Belegtransfer)");
        c.fact("Enthalten:", "alle Rechnungen, Anzahlungs- und Abschlagsrechnungen und Gutschriften, deren Rechnungsdatum im Zeitraum liegt"
                + " – bezahlt oder offen; stornierte nur mit ihrer Stornorechnung. Die Filter des Journals gelten für den Export nicht.");
        c.fact("Datum in DATEV:", "Rechnungsdatum; dazu Leistungsdatum und bei bezahlten Rechnungen das Zahlungsdatum");
        c.fact("Ziel:", "DATEV Unternehmen online, Rechnungsordner „" + folder + "“");
        c.fact("Summen:", invoices + " Rechnungen, " + credits + " Gutschriften · netto " + DatevExportDialog.money(sum[0]) + " € · USt "
                + DatevExportDialog.money(sum[1]) + " € · brutto " + DatevExportDialog.money(sum[2]) + " € (Gutschriften abgezogen)");
        c.fact("Belegdateien:", withOriginal + " mit archiviertem Original (PDF/ZUGFeRD), " + withXml + " mit XRechnung, "
                + (col.docs.size() - withOriginal - withXml) + " nur mit den Rechnungsdaten");
        Table t = new Table("Belege", "Datum", "Beleg", "Art", "Kunde", "Netto", "USt", "Brutto", "Konten (Steuersatz)", "Datei");
        for (DatevXml.Doc d : col.docs) {
            List<String> acc = new ArrayList<String>();
            for (DatevXml.Line l : d.lines) {
                acc.add((l.account.isEmpty() ? "?" : l.account) + (l.bu == null || l.bu.isEmpty() ? "" : "/BU " + l.bu)
                        + " (" + l.rate.stripTrailingZeros().toPlainString() + " %" + (l.category == null ? "" : " " + l.category.code()) + ")");
            }
            t.row(day(d.date), d.number, d.kind, d.customerName, d.net(), d.tax(), d.gross(), String.join(", ", acc),
                    d.files.isEmpty() ? "–" : String.join(", ", d.files.keySet()));
        }
        t.row("", "", "", "Summe", sum[0], sum[1], sum[2], "", "");
        t.sumRow = true;
        c.tables.add(t);
        Table left = new Table("Nicht exportiert", "Datum", "Beleg", "Art", "Kunde / Text", "Brutto", "Grund");
        for (DatevXmlExport.Excluded e : col.excluded) {
            left.row(day(e.date), e.number, e.kind, e.customer, e.gross, e.reason);
        }
        c.tables.add(left);
        if (!settingsSaved) {
            c.notes.add("Die DATEV-Einstellungen wurden noch nicht gespeichert: verwendet werden die Standardkonten des SKR" + chart
                    + " (" + DatevConfig.SETTINGS_PATH + ").");
        }
        for (String n : col.tooLarge) {
            c.notes.add(n + ": ohne Belegdatei, weil die Datei größer als 20 MB ist (Grenze von DATEV).");
        }
        c.notes.addAll(col.problems);
        c.notes.add("Aufbau nach den Musterdateien und XSD-Schemata von DATEV (Version 6.0), nicht von DATEV zertifiziert:"
                + " den ersten Import bitte mit der Steuerberatung prüfen.");
        if (col.docs.size() > DatevXml.MAX_DOCUMENTS) {
            c.errors.add("Mehr als " + DatevXml.MAX_DOCUMENTS + " Belege: DATEV nimmt das Paket nicht an. Bitte einen kürzeren Zeitraum exportieren.");
            c.blockOnErrors = true;
        }
        return c;
    }

    static String nvl(String s) {
        return s == null || "null".equals(s) ? "" : s;
    }
}
