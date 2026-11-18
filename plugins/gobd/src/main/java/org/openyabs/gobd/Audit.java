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
     * Checks the archived invoices and income and both hash chains
     *
     * @return "OK: ..." or "FEHLER: ..."
     */
    String checkArchive() throws IOException {
        Object[][] rows = g.rows("SELECT document_id, document_number, archive_path, sha256_hash, prev_sha256_hash FROM gobd_document_lock"
                + " ORDER BY COALESCE(chain_seq, document_id) ASC, finalized_at ASC, document_id ASC");
        Object[][] income = g.rows("SELECT revenue_id, document_number, archive_path, file_sha256, chain_hash, prev_chain_hash FROM gobd_income_lock"
                + " ORDER BY chain_seq ASC, imported_at ASC, revenue_id ASC");
        if (rows.length == 0 && income.length == 0) {
            return "Keine festgeschriebenen Belege oder Einnahmen im GoBD-Archiv vorhanden.";
        }
        String expectedPrev = GobdUtil.GENESIS_HASH;
        int verified = 0;
        for (Object[] r : rows) {
            String docNum = str(r[1]);
            Path file = Paths.get(str(r[2]));
            String stored = str(r[3]);
            String storedPrev = str(r[4]);
            if (!storedPrev.equals(expectedPrev)) {
                return "FEHLER: Kettenbruch vor Beleg " + docNum + "! Erwartet: " + expectedPrev + ", Gefunden: " + storedPrev;
            }
            if (!Files.exists(file)) {
                return "FEHLER: Archivdatei fehlt fuer Beleg " + docNum + " (" + file + ")!";
            }
            if (!GobdUtil.chained(Files.readAllBytes(file), storedPrev).equals(stored)) {
                return "FEHLER: Datei-Manipulation erkannt bei Beleg " + docNum + "! Berechneter SHA-256-Hash weicht ab.";
            }
            expectedPrev = stored;
            verified++;
        }
        String expectedIncomePrev = GobdUtil.GENESIS_HASH;
        int verifiedIncome = 0;
        for (Object[] r : income) {
            String num = str(r[1]);
            Path file = Paths.get(str(r[2]));
            String expectedSha = str(r[3]);
            String storedChain = str(r[4]);
            String storedPrev = str(r[5]);
            if (!storedPrev.equals(expectedIncomePrev)) {
                return "FEHLER: Kettenbruch in Einnahmen-Kette vor Einnahme " + num + "! Erwartet: " + expectedIncomePrev + ", Gefunden: " + storedPrev;
            }
            if (!Files.exists(file)) {
                return "FEHLER: Archivdatei fehlt fuer Einnahme " + num + " (" + file + ")!";
            }
            byte[] bytes = Files.readAllBytes(file);
            if (!GobdUtil.sha256(bytes).equalsIgnoreCase(expectedSha)) {
                return "FEHLER: Datei-Manipulation erkannt bei Einnahme " + num + "! Datei-SHA-256 weicht ab.";
            }
            if (!GobdUtil.chained(bytes, storedPrev).equalsIgnoreCase(storedChain)) {
                return "FEHLER: Ketten-Hash-Abweichung bei Einnahme " + num + "!";
            }
            expectedIncomePrev = storedChain;
            verifiedIncome++;
        }
        return "OK: Alle " + verified + " Ausgangsbelege und " + verifiedIncome
                + " Einnahmenbelege sowie deren SHA-256-Hash-Ketten sind lueckenlos und unversehrt.";
    }

    /**
     * Checks the receipts of expenses and income against the database and the
     * SHA256SUMS files
     *
     * @return "OK: ..." or "FEHLER: ..."
     */
    String checkReceipts() throws IOException {
        Object[][] receipts = g.rows("SELECT ids, expensesids, originalname, archivepath, filesize, sha256 FROM expense_receipts ORDER BY ids ASC");
        Object[][] income = g.rows("SELECT revenue_id, document_number, original_name, archive_path, file_size, file_sha256 FROM gobd_income_lock ORDER BY chain_seq ASC");
        if (receipts.length == 0 && income.length == 0) {
            return "Keine archivierten Eingangsbelege oder Einnahmenbelege in der Datenbank erfasst.";
        }
        int verified = 0;
        List<String> errors = new ArrayList<String>();
        for (Object[] r : receipts) {
            String label = "Beleg #" + r[0] + " (" + r[2];
            Path f = Paths.get(str(r[3]));
            String expected = str(r[5]);
            if (!Files.exists(f)) {
                errors.add(label + ", Ausgabe #" + r[1] + "): Datei fehlt im Archiv (" + f + ")!");
                continue;
            }
            if (!GobdUtil.sha256(Files.readAllBytes(f)).equalsIgnoreCase(expected)) {
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
        int verifiedIncome = 0;
        for (Object[] r : income) {
            String label = "Einnahme " + r[1] + " (#" + r[0];
            Path f = Paths.get(str(r[3]));
            String expected = str(r[5]);
            if (!Files.exists(f)) {
                errors.add(label + ", " + r[2] + "): Datei fehlt im Archiv (" + f + ")!");
                continue;
            }
            if (!GobdUtil.sha256(Files.readAllBytes(f)).equalsIgnoreCase(expected)) {
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
            return "OK: Alle " + verified + " Ausgaben-Belege und " + verifiedIncome + " Einnahmen-Belege sind vollstaendig vorhanden und gegen SHA256SUMS geprueft.";
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
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String y = String.valueOf(year);
        Path zipPath = g.archiveRoot().resolve("YaBS_GoBD_Z3_Export_" + year + "_" + ts + ".zip");

        List<Object[]> locks = new ArrayList<Object[]>();
        for (Object[] r : g.rows("SELECT g.document_id, g.document_number, g.status, g.finalized_at, g.finalized_by, g.customer_snapshot,"
                + " g.positions_snapshot, g.archive_path, g.artifact_format, g.sha256_hash, g.prev_sha256_hash, g.cancels_document_id, i.dateadded,"
                + " i.datetodo, i.contactsids, (COALESCE(i.netvalue, 0) - COALESCE(i.discountvalue, 0)), COALESCE(i.taxvalue, 0),"
                + " (COALESCE(i.netvalue, 0) - COALESCE(i.discountvalue, 0) + COALESCE(i.taxvalue, 0)) FROM gobd_document_lock g"
                + " LEFT JOIN items i ON g.document_id = i.ids ORDER BY COALESCE(g.chain_seq, g.document_id) ASC, g.finalized_at ASC")) {
            if (GobdUtil.inYear(r[3], y)) {
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
            for (Object[] p : g.rows("SELECT s.ids, s.itemsids, s.description, s.quantityvalue, s.externalvalue, s.taxpercentvalue FROM subitems s"
                    + " INNER JOIN gobd_document_lock g ON s.itemsids = g.document_id ORDER BY s.ids ASC")) {
                if (docIds.contains(str(p[1]))) {
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
                    "3. Kryptographische Integritaetssicherung (SHA-256 Hash-Kette):",
                    "   Jeder archivierte Originalbeleg unter /belege/ und /belege/einnahmen/",
                    "   ist ueber die Formel SHA256(Datei_Bytes || prev_hash) verkettet.",
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
     * Adds an archived original (once per name)
     */
    private final Set<String> added = new HashSet<String>();

    private void file(ZipOutputStream zos, String folder, Object path) throws IOException {
        if (path == null || path.toString().trim().isEmpty()) {
            return;
        }
        Path f = Paths.get(path.toString());
        String name = folder + f.getFileName();
        if (Files.exists(f) && added.add(name)) {
            zos.putNextEntry(new ZipEntry(name));
            zos.write(Files.readAllBytes(f));
            zos.closeEntry();
        }
    }
}
