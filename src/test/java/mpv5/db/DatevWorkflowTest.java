package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Item;
import mpv5.handler.JournalLoader;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The working day of {@link WorkflowTest} with the DATEV plugin, read as a tax
 * adviser: the Buchungsstapel of the year (by payment date) has a line per
 * paid booking and tax rate, money account against revenue / expense account,
 * Soll / Haben, tax keys, document numbers and dates; the sums match the
 * journal.
 */
class DatevWorkflowTest {

    @TempDir
    Path tmp;

    private static Object[][] rows(String sql) throws Exception {
        Object[][] d = QueryHandler.getConnection().freeQuery(sql, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
        return d == null ? new Object[0][] : d;
    }

    @Test
    void aWorkingDayForTheTaxAdviser() throws Exception {
        mpv5.plugin.ExtensionManager.Installed[] datev = new mpv5.plugin.ExtensionManager.Installed[1];
        List<Throwable> failures = new ArrayList<Throwable>();
        WorkflowTest.workingDay(tmp, () -> {
            mpv5.plugin.ExtensionManager m = mpv5.plugin.ExtensionManager.instance();
            datev[0] = m.register(new org.openyabs.datev.DatevPlugin());
            assertTrue(mpv5.ui.dialogs.Popup.silently(() -> m.start(datev[0])), String.valueOf(datev[0].getError()));
            return null;
        }, false, invoice -> {
            try {
                adviser(invoice);
            } catch (Throwable t) {
                failures.add(t);
            }
        });
        if (!failures.isEmpty()) {
            throw new AssertionError(failures.get(0));
        }
    }

    /**
     * @return the fields of a DATEV CSV line, quotes removed
     */
    static List<String> splitCsv(String line) {
        List<String> l = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ';' && !quoted) {
                l.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        l.add(sb.toString());
        return l;
    }

    private static void adviser(Item invoice) throws Exception {
        System.out.println("ACCOUNTS: " + java.util.Arrays.deepToString(rows(
                "SELECT ids, cname, intaccountclass, taxvalue, intaccounttype, frame FROM accounts ORDER BY ids")).replace("], [", "]\n["));
        //the settings a tax adviser enters: adviser and client number, the rest as suggested
        org.openyabs.datev.DatevConfig config = org.openyabs.datev.DatevConfig.load();
        config.beraterNummer = 1001;
        config.mandantenNummer = 10;
        List<mpv5.db.objects.Account> accounts = new ArrayList<mpv5.db.objects.Account>();
        for (mpv5.db.common.DatabaseObject o : mpv5.db.common.DatabaseObject.getObjects(mpv5.db.common.Context.getAccounts())) {
            accounts.add((mpv5.db.objects.Account) o);
        }
        config.prefillFromAccounts(accounts, List.of(new BigDecimal("19"), new BigDecimal("7"), BigDecimal.ZERO), false);
        config.save();
        System.out.println("CONFIG: frame " + config.kontenrahmen + " money " + config.moneyAccountIncome + "/" + config.moneyAccountExpense
                + " revenue " + config.revenueAccounts + " expense " + config.expenseAccounts + " BU " + config.buKeysIncome + "/" + config.buKeysExpense);

        Date now = new Date();
        Date from = DateConverter.getStartOfYear(now);
        Date to = DateConverter.getEndOfYear(now);
        JournalLoader.Loaded j = JournalLoader.loadAllResult(new vTimeframe(from, to), true);

        //the export of the journal (as in the menu and the tax adviser package)
        mpv5.plugin.api.JournalExport export = null;
        for (mpv5.plugin.api.JournalExport e : mpv5.plugin.ExtensionManager.instance().journalExports()) {
            //the booking batch (the plugin also has the document package for DATEV Unternehmen online)
            if (e.title().startsWith("DATEV-Buchungsstapel")) {
                export = e;
            }
        }
        assertNotNull(export, "the DATEV export is in the journal's export menu");
        Map<String, byte[]> pkg = export.adviserPackageEntries(new mpv5.plugin.api.JournalExportContext(
                j.entries, j.totals, from, to, java.util.Collections.emptyList(), j.labels, null, null));
        assertEquals(1, pkg.size());
        String name = pkg.keySet().iterator().next();
        assertTrue(name.startsWith("datev/EXTF_Buchungsstapel_") && name.endsWith(".csv"), name);
        String csv = new String(pkg.get(name), Charset.forName("windows-1252"));
        System.out.println("DATEV CSV:\n" + csv);

        org.openyabs.datev.DatevMapper.Result res = org.openyabs.datev.DatevMapper.map(j.entries, config, from, to);
        for (org.openyabs.datev.DatevMapper.Issue i : res.issues) {
            System.out.println("ISSUE " + i.severity + ": " + i.message);
        }
        assertTrue(res.issues.stream().noneMatch(i -> i.severity == org.openyabs.datev.DatevMapper.Issue.Severity.ERROR), "no errors");

        String[] lines = csv.split("\r\n");
        assertTrue(lines[0].startsWith("\"EXTF\";700;21;\"Buchungsstapel\";13;"), lines[0]);
        assertTrue(lines[0].contains(";1001;10;"), "adviser and client number: " + lines[0]);

        //an independent check of every row (field types, lengths, formats as DATEV's format checker)
        List<String> heads = splitCsv(lines[1]);
        assertEquals(io.github.mrtyldr.datev.core.DatevSchema.CURRENT_V13.columnCount(), heads.size(), "columns of format version 13");
        for (int r = 2; r < lines.length; r++) {
            List<io.github.mrtyldr.datev.core.DatevValidationError> errs = io.github.mrtyldr.datev.core.DatevRowValidator.validate(
                    heads, splitCsv(lines[r]), io.github.mrtyldr.datev.core.DatevValidationMode.STRICT);
            assertEquals(List.of(), errs, "row " + r + ": " + lines[r]);
        }
        //the check is not empty: a wrong amount and date are found
        List<String> bad = splitCsv(lines[2]);
        bad.set(0, "12.34.5");
        bad.set(9, "3299");
        assertFalse(io.github.mrtyldr.datev.core.DatevRowValidator.validate(heads, bad, io.github.mrtyldr.datev.core.DatevValidationMode.STRICT).isEmpty(),
                "a broken row is found");

        //sums: money in (S) = income paid, money out (H) = expenses paid
        BigDecimal soll = BigDecimal.ZERO;
        BigDecimal haben = BigDecimal.ZERO;
        for (org.openyabs.datev.DatevFormat.BookingLine l : res.lines) {
            if ("S".equals(l.sollHaben)) {
                soll = soll.add(l.umsatz);
            } else {
                haben = haben.add(l.umsatz);
            }
            assertFalse(l.gegenkonto.isEmpty(), "every line has a contra account: " + l.belegfeld1);
            assertFalse(l.konto.isEmpty(), "every line has a money account");
            assertEquals(4, l.belegdatum.length(), "date DDMM");
        }
        assertEquals(0, j.totals.incomeGross.compareTo(soll), "income gross " + j.totals.incomeGross + " = Soll " + soll);
        assertEquals(0, j.totals.expenseGross.compareTo(haben), "expenses gross " + j.totals.expenseGross + " = Haben " + haben);

        //the invoice: its two payments, split by the tax rates of its positions, with its number
        long invoiceLines = res.lines.stream().filter(l -> invoice.__getCnumber().equals(l.belegfeld1)).count();
        assertEquals(4, invoiceLines, "2 payments x 2 tax rates");
        BigDecimal invoiceSum = res.lines.stream().filter(l -> invoice.__getCnumber().equals(l.belegfeld1))
                .map(l -> l.umsatz).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("328.40").compareTo(invoiceSum), "the invoice gross: " + invoiceSum);
        //the 19 % part on the 19 % revenue account, the 7 % part on the 7 % one, tax by automatic account or BU key
        for (org.openyabs.datev.DatevFormat.BookingLine l : res.lines) {
            System.out.println("LINE " + l.sollHaben + " " + l.umsatz + " " + l.konto + " / " + l.gegenkonto + " BU '" + l.buSchluessel + "' " + l.belegdatum
                    + " " + l.belegfeld1 + " " + l.buchungstext);
        }
        //automatic accounts (SKR03 8400 / 8300 / 3400) book the tax themselves: no BU key (DATEV rejects it)
        for (org.openyabs.datev.DatevFormat.BookingLine l : res.lines) {
            if (java.util.Set.of("8400", "8300", "3400", "3300").contains(l.gegenkonto)) {
                assertEquals("", l.buSchluessel, "BU key on automatic account " + l.gegenkonto);
            }
        }
        assertTrue(res.lines.stream().anyMatch(l -> "LIEF-7".equals(l.belegfeld1) && "H".equals(l.sollHaben)
                && new BigDecimal("59.50").compareTo(l.umsatz) == 0), "the supplier invoice paid");
    }
}
