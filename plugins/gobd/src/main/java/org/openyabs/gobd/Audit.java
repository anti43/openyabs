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
package org.openyabs.gobd;

import static org.openyabs.gobd.GobdUtil.csvCell;
import static org.openyabs.gobd.GobdUtil.fmtDate;
import static org.openyabs.gobd.GobdUtil.fmtDec;
import static org.openyabs.gobd.GobdUtil.str;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import mpv5.db.objects.Item;

/**
 * For the tax audit: the checks of the archive and the hash chains, and the
 * Z3 export (GDPdU / IDEA)
 */
final class Audit {

    private final Gobd g;

    Audit(Gobd g) {
        this.g = g;
    }

    /**
     * Checks the archived invoices and income, both hash chains and the
     * chain of the audit log. Version 2 locks are also checked against the
     * database (amounts, dates, positions ...).
     *
     * @return "OK: ..." or "FEHLER: ..." (all problems, not only the first)
     */
    String checkArchive() throws IOException {
        Object[][] rows = g.q("SELECT document_id, document_number, archive_path, sha256_hash, prev_sha256_hash, hash_version, chain_seq,"
                + " finalized_at, finalized_by, artifact_format, file_sha256, cancels_document_id, record_data FROM gobd_document_lock"
                + " ORDER BY chain_seq ASC, finalized_at ASC, document_id ASC");
        Object[][] income = g.q("SELECT revenue_id, document_number, archive_path, file_sha256, chain_hash, prev_chain_hash, hash_version, chain_seq,"
                + " imported_at, imported_by, original_name, artifact_format, file_size, record_data FROM gobd_income_lock"
                + " ORDER BY chain_seq ASC, imported_at ASC, revenue_id ASC");
        List<String> errors = new ArrayList<String>();
        String expectedPrev = GobdUtil.GENESIS_HASH;
        int verified = 0;
        for (Object[] r : rows) {
            String docNum = str(r[1]);
            Path file = Paths.get(str(r[2]));
            String stored = str(r[3]);
            String storedPrev = str(r[4]);
            boolean v2 = GobdUtil.toInt(r[5], 1) >= 2;
            int before = errors.size();
            if (!storedPrev.equals(expectedPrev)) {
                errors.add("Kettenbruch vor Beleg " + docNum + "! Erwartet: " + expectedPrev + ", Gefunden: " + storedPrev);
            }
            expectedPrev = stored;
            if (!Files.exists(file)) {
                errors.add("Archivdatei fehlt fuer Beleg " + docNum + " (" + file + ")!");
                continue;
            }
            if (v2) {
                String record = str(r[12]);
                if (!GobdUtil.sha256(file).equalsIgnoreCase(str(r[10]))) {
                    errors.add("Datei-Manipulation erkannt bei Beleg " + docNum + "! SHA-256 der Archivdatei weicht ab.");
                }
                if (!GobdUtil.recordHash(record, storedPrev).equalsIgnoreCase(stored)) {
                    errors.add("Ketten-Hash-Abweichung bei Beleg " + docNum + "! Der festgeschriebene Datensatz wurde veraendert.");
                }
                String core = g.itemCoreInDb(GobdUtil.toLong(r[0], -1));
                String now = Gobd.documentRecord(r[6], r[0], r[1], r[7], r[8], r[9], r[10], r[11], core == null ? "<fehlt>" : core,
                        g.positionsCoreInDb(GobdUtil.toLong(r[0], -1)));
                if (!now.equals(record)) {
                    errors.add("Datenbank-Manipulation erkannt bei Beleg " + docNum + "! Abweichende Felder: " + diff(record, now));
                }
            } else if (!GobdUtil.chained(Files.readAllBytes(file), storedPrev).equalsIgnoreCase(stored)) {
                errors.add("Datei-Manipulation erkannt bei Beleg " + docNum + "! Berechneter SHA-256-Hash weicht ab.");
            }
            if (errors.size() == before) {
                verified++;
            }
        }
        String expectedIncomePrev = GobdUtil.GENESIS_HASH;
        int verifiedIncome = 0;
        for (Object[] r : income) {
            String num = str(r[1]);
            Path file = Paths.get(str(r[2]));
            String expectedSha = str(r[3]);
            String storedChain = str(r[4]);
            String storedPrev = str(r[5]);
            boolean v2 = GobdUtil.toInt(r[6], 1) >= 2;
            int before = errors.size();
            if (!storedPrev.equals(expectedIncomePrev)) {
                errors.add("Kettenbruch in Einnahmen-Kette vor Einnahme " + num + "! Erwartet: " + expectedIncomePrev + ", Gefunden: " + storedPrev);
            }
            expectedIncomePrev = storedChain;
            if (!Files.exists(file)) {
                errors.add("Archivdatei fehlt fuer Einnahme " + num + " (" + file + ")!");
                continue;
            }
            if (!GobdUtil.sha256(file).equalsIgnoreCase(expectedSha)) {
                errors.add("Datei-Manipulation erkannt bei Einnahme " + num + "! Datei-SHA-256 weicht ab.");
            }
            if (v2) {
                String record = str(r[13]);
                if (!GobdUtil.recordHash(record, storedPrev).equalsIgnoreCase(storedChain)) {
                    errors.add("Ketten-Hash-Abweichung bei Einnahme " + num + "! Der festgeschriebene Datensatz wurde veraendert.");
                }
                Object[] db = g.row("SELECT cnumber, dateadded, accountsids, netvalue, taxpercentvalue, brutvalue, description FROM revenues WHERE ids = "
                        + GobdUtil.toLong(r[0], -1));
                String now = Gobd.incomeRecord(r[7], r[0], r[1], r[8], r[9], r[10], r[11], r[3], r[12], db == null ? "<fehlt>" : Gobd.moneyCoreOfRow(db));
                if (!now.equals(record)) {
                    errors.add("Datenbank-Manipulation erkannt bei Einnahme " + num + "! Abweichende Felder: " + diff(record, now));
                }
            } else if (!GobdUtil.chained(Files.readAllBytes(file), storedPrev).equalsIgnoreCase(storedChain)) {
                errors.add("Ketten-Hash-Abweichung bei Einnahme " + num + "!");
            }
            if (errors.size() == before) {
                verifiedIncome++;
            }
        }
        int[] audit = checkAuditLog(errors);
        if (!errors.isEmpty()) {
            return "FEHLER: Integritaetsprobleme im GoBD-Archiv:\n- " + String.join("\n- ", errors);
        }
        if (rows.length == 0 && income.length == 0) {
            return "OK: Keine festgeschriebenen Belege oder Einnahmen im GoBD-Archiv vorhanden; Audit-Log-Kette (" + audit[0] + " Eintraege) unversehrt.";
        }
        return "OK: Alle " + verified + " Ausgangsbelege und " + verifiedIncome
                + " Einnahmenbelege sowie deren SHA-256-Hash-Ketten sind lueckenlos und unversehrt; Audit-Log-Kette: " + audit[0] + " Eintraege"
                + (audit[1] > 0 ? " (dazu " + audit[1] + " aeltere Eintraege ohne Hash)" : "") + ".";
    }

    /**
     * Checks the hash chain of the audit log
     *
     * @param errors gets the problems
     * @return {chained entries, older entries without hash}
     */
    int[] checkAuditLog(List<String> errors) {
        Object[][] rows = g.q("SELECT id, created_at, user_id, entity_type, entity_id, action, old_value, new_value, file_hash, details, prev_hash, row_hash"
                + " FROM gobd_audit_log ORDER BY id ASC");
        String expected = GobdUtil.GENESIS_HASH;
        boolean started = false;
        int chained = 0;
        int legacy = 0;
        for (Object[] a : rows) {
            String hash = str(a[11]);
            if (hash.isEmpty()) {
                if (started) {
                    errors.add("Audit-Log-Eintrag #" + a[0] + " ohne Hash innerhalb der Kette (nachtraeglich eingefuegt?)!");
                } else {
                    legacy++;
                }
                continue;
            }
            started = true;
            String prev = str(a[10]);
            if (!prev.equals(expected)) {
                errors.add("Kettenbruch im Audit-Log vor Eintrag #" + a[0] + " (Eintrag geloescht oder eingefuegt?)!");
            }
            String record = Gobd.auditRecord(a[1], str(a[2]), str(a[3]), a[4], str(a[5]), (String) a[6], (String) a[7], (String) a[8], str(a[9]));
            if (!GobdUtil.recordHash(record, prev).equals(hash)) {
                errors.add("Audit-Log-Eintrag #" + a[0] + " wurde veraendert!");
            }
            expected = hash;
            chained++;
        }
        return new int[]{chained, legacy};
    }

    /**
     * @return the keys of the record lines that differ
     */
    static String diff(String expected, String actual) {
        List<String> keys = new ArrayList<String>();
        String[] a = expected.split("\n", -1);
        String[] b = actual.split("\n", -1);
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            String x = i < a.length ? a[i] : "";
            String y = i < b.length ? b[i] : "";
            if (!x.equals(y)) {
                String line = x.isEmpty() ? y : x;
                keys.add(line.contains("=") ? line.substring(0, line.indexOf('=')) : line);
            }
        }
        return keys.isEmpty() ? "-" : String.join(", ", keys);
    }

    /**
     * Checks the receipts of expenses and income against the database, the
     * hash chain of the receipts and the SHA256SUMS files
     *
     * @return "OK: ..." or "FEHLER: ..."
     */
    String checkReceipts() throws IOException {
        g.chainReceipts();
        Object[][] receipts = g.rows("SELECT ids, expensesids, originalname, archivepath, filesize, sha256 FROM expense_receipts ORDER BY ids ASC");
        Object[][] chain = g.q("SELECT id, expense_id, chain_seq, receipt_ref, original_name, archive_path, file_size, sha256, chain_hash, prev_chain_hash,"
                + " imported_at, imported_by, record_data FROM gobd_receipt ORDER BY chain_seq ASC");
        Object[][] income = g.q("SELECT revenue_id, document_number, original_name, archive_path, file_size, file_sha256 FROM gobd_income_lock ORDER BY chain_seq ASC");
        if (receipts.length == 0 && income.length == 0 && chain.length == 0) {
            return "Keine archivierten Eingangsbelege oder Einnahmenbelege in der Datenbank erfasst.";
        }
        int verified = 0;
        List<String> errors = new ArrayList<String>();
        Set<String> refs = new HashSet<String>();
        for (Object[] r : receipts) {
            refs.add(str(r[0]));
            String label = "Beleg #" + r[0] + " (" + r[2];
            Path f = Paths.get(str(r[3]));
            String expected = str(r[5]);
            if (!Files.exists(f)) {
                errors.add(label + ", Ausgabe #" + r[1] + "): Datei fehlt im Archiv (" + f + ")!");
                continue;
            }
            if (!GobdUtil.sha256(f).equalsIgnoreCase(expected)) {
                errors.add(label + ", Ausgabe #" + r[1] + "): SHA-256 stimmt NICHT ueberein!");
                continue;
            }
            Path sums = f.getParent().resolve("SHA256SUMS");
            if (Files.exists(sums) && !new String(Files.readAllBytes(sums), StandardCharsets.UTF_8).contains(expected)) {
                errors.add(label + "): SHA-256 fehlt in " + sums.getFileName() + "!");
                continue;
            }
            verified++;
        }
        //the chain of the receipts: also finds receipts removed from the database
        String expectedPrev = GobdUtil.GENESIS_HASH;
        for (Object[] c : chain) {
            String label = "Kette: Beleg #" + c[3] + " (" + c[4] + ")";
            String prev = str(c[9]);
            if (!prev.equals(expectedPrev)) {
                errors.add(label + ": Kettenbruch (Eintrag geloescht oder eingefuegt?)!");
            }
            expectedPrev = str(c[8]);
            String record = str(c[12]);
            if (!GobdUtil.recordHash(record, prev).equalsIgnoreCase(str(c[8]))) {
                errors.add(label + ": Ketten-Hash-Abweichung, der Datensatz wurde veraendert!");
            }
            if (!refs.contains(str(c[3]))) {
                errors.add(label + ": Beleg wurde aus der Datenbank (expense_receipts) entfernt!");
            }
            Object[] exp = g.row("SELECT cnumber, dateadded, accountsids, netvalue, taxpercentvalue, brutvalue, description FROM expenses WHERE ids = "
                    + GobdUtil.toLong(c[1], -1));
            String now = Gobd.receiptRecord(c[2], c[3], c[1], c[4], c[6], c[7], c[10], c[11], exp == null ? "<fehlt>" : Gobd.moneyCoreOfRow(exp));
            if (!now.equals(record)) {
                errors.add(label + ": Datenbank-Manipulation erkannt! Abweichende Felder: " + diff(record, now));
            }
        }
        int verifiedIncome = 0;
        for (Object[] r : income) {
            String label = "Einnahme " + r[1] + " (#" + r[0];
            Path f = Paths.get(str(r[3]));
            String expected = str(r[5]);
            if (!Files.exists(f)) {
                errors.add(label + ", " + r[2] + "): Datei fehlt im Archiv (" + f + ")!");
                continue;
            }
            if (!GobdUtil.sha256(f).equalsIgnoreCase(expected)) {
                errors.add(label + ", " + r[2] + "): SHA-256 stimmt NICHT ueberein!");
                continue;
            }
            Path sums = f.getParent().resolve("SHA256SUMS");
            if (!Files.exists(sums)) {
                errors.add(label + "): SHA256SUMS fehlt in " + f.getParent() + "!");
                continue;
            }
            if (!new String(Files.readAllBytes(sums), StandardCharsets.UTF_8).contains(expected)) {
                errors.add(label + "): SHA-256 fehlt in " + sums + "!");
                continue;
            }
            verifiedIncome++;
        }
        if (errors.isEmpty()) {
            return "OK: Alle " + verified + " Ausgaben-Belege (" + chain.length + " davon in der Hash-Kette) und " + verifiedIncome
                    + " Einnahmen-Belege sind vollstaendig vorhanden und gegen SHA256SUMS geprueft.";
        }
        return "FEHLER: Integritaetsprobleme bei Belegen:\n- " + String.join("\n- ", errors);
    }

    /**
     * @param year
     * @return index.xml of the GDPdU description standard for the year
     */
    static String indexXml(int year) {
        return GobdUtil.resource("index.xml").replace("@YEAR@", String.valueOf(year));
    }

    /**
     * @return gdpdu-01-09-2004.dtd
     */
    static String dtd() {
        return GobdUtil.resource("gdpdu-01-09-2004.dtd");
    }

    /**
     * Writes the Z3 export (index.xml, DTD, CSV files, originals) of a year
     *
     * @param year
     * @return the ZIP file
     */
    synchronized Path exportZ3(int year) throws IOException {
        added.clear();
        addedPaths.clear();
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String y = String.valueOf(year);
        Path zipPath = g.archiveRoot().resolve("YaBS_GoBD_Z3_Export_" + year + "_" + ts + ".zip");

        List<Object[]> locks = new ArrayList<Object[]>();
        for (Object[] r : g.rows("SELECT g.document_id, g.document_number, g.status, g.finalized_at, g.finalized_by, g.customer_snapshot,"
                + " g.positions_snapshot, g.archive_path, g.artifact_format, g.sha256_hash, g.prev_sha256_hash, g.cancels_document_id, i.dateadded,"
                + " i.datetodo, i.contactsids, (COALESCE(i.netvalue, 0) - COALESCE(i.discountvalue, 0)), COALESCE(i.taxvalue, 0),"
                + " (COALESCE(i.netvalue, 0) - COALESCE(i.discountvalue, 0) + COALESCE(i.taxvalue, 0)) FROM gobd_document_lock g"
                + " LEFT JOIN items i ON g.document_id = i.ids ORDER BY g.chain_seq ASC, g.finalized_at ASC")) {
            //the year of the invoice (issue date), as for income and expenses; the day of finalizing only without one
            if (GobdUtil.inYear(r[12] != null ? r[12] : r[3], y)) {
                locks.add(r);
            }
        }
        Set<String> docIds = new HashSet<String>();
        for (Object[] r : locks) {
            docIds.add(str(r[0]));
        }

        try (OutputStream out = Files.newOutputStream(zipPath); ZipOutputStream zos = new ZipOutputStream(out)) {
            entry(zos, "index.xml", indexXml(year));
            entry(zos, "gdpdu-01-09-2004.dtd", dtd());

            StringBuilder docs = new StringBuilder("document_id;document_number;status;issue_date;delivery_date;contact_id;net_amount;tax_amount;"
                    + "gross_amount;artifact_format;sha256_hash;prev_sha256_hash;cancels_document_id;customer_snapshot\n");
            for (Object[] r : locks) {
                docs.append(r[0]).append(";").append(q(r[1])).append(";").append(q(r[2])).append(";").append(fmtDate(r[12])).append(";")
                        .append(fmtDate(r[13])).append(";").append(r[14] == null ? 0 : r[14]).append(";").append(fmtDec(r[15])).append(";")
                        .append(fmtDec(r[16])).append(";").append(fmtDec(r[17])).append(";").append(q(r[8])).append(";").append(q(r[9])).append(";")
                        .append(q(r[10])).append(";").append(q(r[11])).append(";").append(q(r[5])).append("\n");
            }
            entry(zos, "rechnungen.csv", docs.toString());

            StringBuilder pos = new StringBuilder("position_id;document_id;description;quantity;unit_price;tax_rate\n");
            List<String> ids = new ArrayList<String>();
            for (String id : docIds) {
                ids.add(String.valueOf(GobdUtil.toLong(id, -1)));
            }
            //only the positions of the invoices of the year (chunks: no endless IN list)
            for (int from = 0; from < ids.size(); from += 500) {
                String in = String.join(", ", ids.subList(from, Math.min(ids.size(), from + 500)));
                for (Object[] p : g.rows("SELECT s.ids, s.itemsids, s.description, s.quantityvalue, s.externalvalue, s.taxpercentvalue FROM subitems s"
                        + " WHERE s.itemsids IN (" + in + ") ORDER BY s.itemsids ASC, s.ids ASC")) {
                    pos.append(p[0]).append(";").append(p[1]).append(";").append(q(p[2])).append(";").append(fmtDec(p[3])).append(";")
                            .append(fmtDec(p[4])).append(";").append(fmtDec(p[5])).append("\n");
                }
            }
            entry(zos, "rechnungspositionen.csv", pos.toString());

            StringBuilder cust = new StringBuilder("contact_id;customer_number;company;name;street;zip;city;country;tax_number\n");
            for (Object[] c : g.rows("SELECT ids, cnumber, company, cname, street, zip, city, country, taxnumber FROM contacts ORDER BY ids ASC")) {
                cust.append(c[0]);
                for (int i = 1; i <= 8; i++) {
                    cust.append(";").append(q(c[i]));
                }
                cust.append("\n");
            }
            entry(zos, "kunden.csv", cust.toString());

            StringBuilder audit = new StringBuilder("id;created_at;user_id;entity_type;entity_id;action;old_value;new_value;file_hash;details\n");
            for (Object[] a : g.rows("SELECT id, created_at, user_id, entity_type, entity_id, action, old_value, new_value, file_hash, details"
                    + " FROM gobd_audit_log ORDER BY id ASC")) {
                if (GobdUtil.inYear(a[1], y)) {
                    audit.append(a[0]).append(";").append(q(a[1])).append(";").append(q(a[2])).append(";").append(q(a[3])).append(";")
                            .append(a[4]).append(";").append(q(a[5])).append(";").append(q(a[6])).append(";").append(q(a[7])).append(";")
                            .append(q(a[8])).append(";").append(q(a[9])).append("\n");
                }
            }
            entry(zos, "audit_log.csv", audit.toString());

            StringBuilder exp = new StringBuilder("expense_id;cnumber;description;date_added;date_paid;is_paid;account_id;net_amount;tax_percent;gross_amount\n");
            for (Object[] e : g.rows("SELECT ids, cnumber, description, dateadded, dateend, ispaid, accountsids, netvalue, taxpercentvalue, brutvalue"
                    + " FROM expenses ORDER BY ids ASC")) {
                if (GobdUtil.inYear(e[3], y)) {
                    exp.append(e[0]).append(";").append(q(e[1])).append(";").append(q(e[2])).append(";").append(fmtDate(e[3])).append(";")
                            .append(fmtDate(e[4])).append(";").append(e[5] != null && e[5].toString().equals("1") ? "JA" : "NEIN").append(";")
                            .append(e[6] == null ? 0 : e[6]).append(";").append(fmtDec(e[7])).append(";").append(fmtDec(e[8])).append(";")
                            .append(fmtDec(e[9])).append("\n");
                }
            }
            entry(zos, "ausgaben.csv", exp.toString());

            List<Object[]> receipts = new ArrayList<Object[]>();
            StringBuilder rec = new StringBuilder("receipt_id;expense_id;original_name;file_size;sha256;mime_type;imported_at;imported_by\n");
            for (Object[] r : g.rows("SELECT ids, expensesids, originalname, archivepath, filesize, sha256, mimetype, imported_at, imported_by"
                    + " FROM expense_receipts ORDER BY ids ASC")) {
                if (GobdUtil.inYear(r[7], y)) {
                    receipts.add(r);
                    rec.append(r[0]).append(";").append(r[1]).append(";").append(q(r[2])).append(";").append(r[4]).append(";")
                            .append(q(r[5])).append(";").append(q(r[6])).append(";").append(q(r[7])).append(";").append(q(r[8])).append("\n");
                }
            }
            entry(zos, "eingangsbelege.csv", rec.toString());

            List<Object[]> revenues = new ArrayList<Object[]>();
            StringBuilder rev = new StringBuilder("revenue_id;cnumber;description;date_added;date_paid;is_paid;account_id;net_amount;tax_percent;"
                    + "gross_amount;artifact_format;file_sha256;chain_hash;prev_chain_hash\n");
            for (Object[] r : g.rows("SELECT r.ids, r.cnumber, r.description, r.dateadded, r.dateend, r.status, r.accountsids, r.netvalue,"
                    + " r.taxpercentvalue, r.brutvalue, l.artifact_format, l.file_sha256, l.chain_hash, l.prev_chain_hash, l.archive_path"
                    + " FROM revenues r LEFT JOIN gobd_income_lock l ON r.ids = l.revenue_id ORDER BY r.ids ASC")) {
                if (GobdUtil.inYear(r[3], y)) {
                    revenues.add(r);
                    boolean paid = r[5] != null && r[5].toString().equals(String.valueOf(Item.STATUS_PAID));
                    rev.append(r[0]).append(";").append(q(r[1])).append(";").append(q(r[2])).append(";").append(fmtDate(r[3])).append(";")
                            .append(fmtDate(r[4])).append(";").append(paid ? "JA" : "NEIN").append(";").append(r[6] == null ? 0 : r[6]).append(";")
                            .append(fmtDec(r[7])).append(";").append(fmtDec(r[8])).append(";").append(fmtDec(r[9])).append(";").append(q(r[10]))
                            .append(";").append(q(r[11])).append(";").append(q(r[12])).append(";").append(q(r[13])).append("\n");
                }
            }
            entry(zos, "einnahmen.csv", rev.toString());

            for (Object[] r : locks) {
                file(zos, "belege/", r[7]);
            }
            for (Object[] r : receipts) {
                file(zos, "belege/eingangsbelege/", r[3]);
            }
            for (Object[] r : revenues) {
                file(zos, "belege/einnahmen/", r[14]);
            }
            entry(zos, "VERFAHRENSDOKUMENTATION_TECHNIK.txt", String.join("\n",
                    "=======================================================================",
                    "OpenYaBS - Technische System- und Verfahrensdokumentation nach GoBD",
                    "=======================================================================",
                    "Pruefungsjahr: " + year,
                    "Export-Zeitpunkt: " + ts,
                    "",
                    "1. Unveraenderbarkeit (§ 146 Abs. 4 AO):",
                    "   Festgeschriebene Belege (gobd_document_lock, gobd_income_lock) und deren",
                    "   Positionen sind durch den DatabaseObjectModifier gegen steuerrelevante",
                    "   Aenderungen sowie gegen Loeschen und Papierkorb gesperrt. Reine Zahlungs-",
                    "   und Mahnstatus-Aenderungen bleiben erlaubt und werden protokolliert.",
                    "   Inhaltliche Korrekturen erfolgen ausschliesslich ueber Stornorechnungen (ST-*).",
                    "",
                    "2. Stammdaten-Snapshots:",
                    "   Empfaengeranschrift, Steuernummer und Positionen werden zum Zeitpunkt",
                    "   der Festschreibung unveraenderbar in gobd_document_lock sowie im",
                    "   archivierten Originalbeleg gespeichert.",
                    "",
                    "3. Kryptographische Integritaetssicherung (SHA-256 Hash-Ketten):",
                    "   Festschreibungen (Rechnungen, Einnahmen, Eingangsbelege) und das",
                    "   Audit-Log sind je eine Hash-Kette: hash = SHA256(Datensatz || \"\\n\" || prev_hash).",
                    "   Der Datensatz (hash_version 2, Spalte record_data) enthaelt den SHA-256",
                    "   der Archivdatei sowie Nummer, Datum, Kontakt, Betraege, Steuer und",
                    "   Positionen; geprueft wird gegen Archivdatei UND aktuelle Datenbank.",
                    "   Aeltere Festschreibungen (ohne hash_version): SHA256(Datei_Bytes || prev_hash).",
                    "   chain_seq ist eindeutig (kein Verzweigen der Kette).",
                    "",
                    "4. Maschinelle Auswertbarkeit (Z3-Zugriff / IDEA):",
                    "   Dieses Archiv enthaelt eine vollstaendige index.xml sowie die",
                    "   offizielle gdpdu-01-09-2004.dtd zum direkten Import in IDEA."));
        }
        return zipPath;
    }

    private static String q(Object o) {
        return "\"" + csvCell(o) + "\"";
    }

    private static void entry(ZipOutputStream zos, String name, String text) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(text.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    /**
     * The names in the ZIP and the files added (each file once, no name
     * twice)
     */
    private final Set<String> added = new HashSet<String>();
    private final Set<Path> addedPaths = new HashSet<Path>();

    /**
     * Adds an archived original under folder/year folder/name (the archive
     * has a folder per year; the same name in two years is not lost)
     */
    private void file(ZipOutputStream zos, String folder, Object path) throws IOException {
        if (path == null || path.toString().trim().isEmpty()) {
            return;
        }
        Path f = Paths.get(path.toString());
        if (!Files.isRegularFile(f) || !addedPaths.add(f.toAbsolutePath().normalize())) {
            return;
        }
        String parent = f.getParent() == null || f.getParent().getFileName() == null ? "" : f.getParent().getFileName() + "/";
        String fileName = f.getFileName().toString();
        String name = folder + parent + fileName;
        for (int i = 2; !added.add(name); i++) {
            int dot = fileName.lastIndexOf('.');
            name = folder + parent + (dot > 0 ? fileName.substring(0, dot) + "_" + i + fileName.substring(dot) : fileName + "_" + i);
        }
        zos.putNextEntry(new ZipEntry(name));
        Files.copy(f, zos);
        zos.closeEntry();
    }
}
