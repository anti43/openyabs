// @name GoBD-Konformitaet (Vollstaendiges MVP)
// @description Erzwingt GoBD-Unveraenderbarkeit (Festschreibung bei Export), erlaubten Offene-Posten-/Zahlungsstatus-Workflow, Loesch-/Papierkorbschutz, Stammdaten-Snapshots, SHA-256-Hash-Verkettung, Stornorechnungen und Z3-Betriebspruefer-Exporte.
// @version 1.4.0
// @author OpenYaBS Community
// @id groovy.gobd-compliance

import mpv5.db.common.Context
import mpv5.db.common.DatabaseObject
import mpv5.db.common.DatabaseObjectModifier
import mpv5.db.common.NodataFoundException
import mpv5.db.common.QueryHandler
import mpv5.db.objects.Contact
import mpv5.db.objects.Item
import mpv5.db.objects.SubItem
import mpv5.db.objects.User
import mpv5.handler.Activity
import mpv5.plugin.api.SearchResult
import mpv5.ui.dialogs.Notificator
import mpv5.ui.dialogs.Popup
import mpv5.ui.panels.ChangeNotApprovedException
import mpv5.usermanagement.MPSecurityManager

import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JFileChooser
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.SwingUtilities
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Desktop
import java.awt.Dimension
import java.awt.FlowLayout
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFilePermission
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// Start-Hash der SHA-256-Kette (64 Nullen)
final String GENESIS_HASH = "0" * 64

// Standard-Archivordner im Plugin-Datenverzeichnis; kann in den Einstellungen frei angepasst werden
final Path defaultArchiveRoot = yabs.dataFolder().toPath().resolve("gobd-archive")

def getArchiveRoot = { ->
    String configuredPath = yabs.settings().get("gobd.archivePath", defaultArchiveRoot.toAbsolutePath().toString())
    Path p = Paths.get(configuredPath.trim())
    Files.createDirectories(p)
    return p
}

// Initial einmal anlegen
getArchiveRoot()

// Zwischenspeicher fuer zuletzt erzeugte Belegdateien (PDF, ZUGFeRD, XRechnung) aus yabs.onActivity
final Map<Long, Path> lastGeneratedFiles = [:].asSynchronized()

// Zwischenspeicher fuer alte Stammdaten-Werte vor dem Speichern (fuer Alt/Neu-Audit-Log)
final Map<String, String> masterDataBeforeSave = [:].asSynchronized()

// Re-Entrancy-Schutz fuer laufende Festschreibungen und Stornierungen
final Set<Integer> finalizingIds = Collections.synchronizedSet(new HashSet<Integer>())
boolean creatingStorno = false

// Sichere SQL-Wrapper ueber QueryHandler.getConnection() (setzt Context.DEFAULT)
def execSql = { String sql ->
    QueryHandler.getConnection().freeQuery(sql.toString(), MPSecurityManager.VIEW, null)
}

def selectRows = { String sql ->
    def res = QueryHandler.getConnection().freeSelectQuery(sql.toString(), MPSecurityManager.VIEW, null)
    if (res == null) return ([] as Object[][])
    return res.getData() ?: ([] as Object[][])
}

// Benachrichtigungs-Helper ueber mpv5.ui.dialogs.Notificator & Popup
def showError = { String msg ->
    if (SwingUtilities.isEventDispatchThread()) {
        Popup.error(msg)
    } else {
        SwingUtilities.invokeLater { Popup.error(msg) }
    }
}

def notifyStatus = { String msg ->
    Notificator.raiseNotification(msg, false)
}

def notifyPopup = { String msg ->
    Notificator.raiseNotification(msg, true)
}

def notifyDocumentEvent = { String msg, DatabaseObject source ->
    if (source != null && source.__getIDS() > 0) {
        Notificator.raiseNotification(msg, true, true, source)
    } else {
        Notificator.raiseNotification(msg, true)
    }
}

// Erzeugt ChangeNotApprovedException unabhaengig von der jeweiligen Konstruktor-Signatur
def vetoException = { String msg ->
    try {
        return ChangeNotApprovedException.getConstructor(String.class).newInstance(msg)
    } catch (Exception ignored) {
        try {
            return ChangeNotApprovedException.getDeclaredConstructor().newInstance()
        } catch (Exception ignored2) {
            return new ChangeNotApprovedException(null)
        }
    }
}

// -----------------------------------------------------------------------------
// 1. Datenbank-Schema-Initialisierung & Migration (H2 / MySQL / MariaDB / PostgreSQL)
// -----------------------------------------------------------------------------
def initSchema = {
    execSql("""
        CREATE TABLE IF NOT EXISTS gobd_document_lock (
            document_id BIGINT PRIMARY KEY,
            chain_seq BIGINT NULL,
            document_number VARCHAR(100) NOT NULL UNIQUE,
            status VARCHAR(20) NOT NULL,
            finalized_at TIMESTAMP NOT NULL,
            finalized_by VARCHAR(100) NOT NULL,
            customer_snapshot TEXT NOT NULL,
            positions_snapshot TEXT NOT NULL,
            archive_path VARCHAR(512) NOT NULL,
            artifact_format VARCHAR(30) NOT NULL,
            sha256_hash CHAR(64) NOT NULL,
            prev_sha256_hash CHAR(64) NOT NULL,
            cancels_document_id BIGINT NULL
        )
    """)

    execSql("""
        CREATE TABLE IF NOT EXISTS gobd_audit_log (
            id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            user_id VARCHAR(100) NOT NULL,
            entity_type VARCHAR(50) NOT NULL,
            entity_id BIGINT NOT NULL,
            action VARCHAR(50) NOT NULL,
            old_value VARCHAR(2000) NULL,
            new_value VARCHAR(2000) NULL,
            file_hash CHAR(64) NULL,
            details VARCHAR(1000) NULL
        )
    """)

    // Migration fuer bestehende Tabellen aus frueheren Versionen
    [
        "ALTER TABLE gobd_document_lock ADD COLUMN IF NOT EXISTS chain_seq BIGINT NULL",
        "ALTER TABLE gobd_document_lock ADD COLUMN IF NOT EXISTS customer_snapshot TEXT NULL",
        "ALTER TABLE gobd_document_lock ADD COLUMN IF NOT EXISTS positions_snapshot TEXT NULL",
        "ALTER TABLE gobd_audit_log ADD COLUMN IF NOT EXISTS old_value VARCHAR(2000) NULL",
        "ALTER TABLE gobd_audit_log ADD COLUMN IF NOT EXISTS new_value VARCHAR(2000) NULL"
    ].each { String alterSql ->
        execSql(alterSql)
    }
}

// -----------------------------------------------------------------------------
// 2. Hilfsfunktionen (Audit-Log, Hash-Verkettung, Snapshots & Validierung)
// -----------------------------------------------------------------------------
def currentUser = {
    try {
        return User.getCurrentUser()?.__getCname() ?: System.getProperty("user.name", "system")
    } catch (Exception ignored) {
        return System.getProperty("user.name", "system")
    }
}

def esc = { Object val ->
    return val == null ? "" : val.toString().replace("'", "''")
}

def trunc = { String s, int maxLen ->
    if (s == null) return null
    return s.length() <= maxLen ? s : (s.substring(0, maxLen - 3) + "...")
}

def escapeXml = { Object raw ->
    if (raw == null) return ""
    return raw.toString()
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}

def csvCell = { Object raw ->
    if (raw == null) return ""
    return raw.toString()
        .replaceAll(/[\r\n]+/, " ")
        .replace("\"", "\"\"")
}

def fmtDec = { Object num ->
    if (num == null) return "0.00"
    try {
        return String.format(Locale.US, "%.2f", Double.parseDouble(num.toString()))
    } catch (Exception ignored) {
        return "0.00"
    }
}

def fmtDate = { Object d ->
    if (d == null) return ""
    if (d instanceof Date) {
        return new SimpleDateFormat("yyyy-MM-dd").format((Date) d)
    }
    String s = d.toString().trim()
    return s.length() >= 10 ? s.substring(0, 10) : s
}

def logAudit = { String entityType, long entityId, String action, String oldVal, String newVal, String fileHash, String details ->
    String safeOld = trunc(oldVal, 2000)
    String safeNew = trunc(newVal, 2000)
    String safeDetails = trunc(details ?: "", 1000)
    String oldSql = safeOld != null ? "'${esc(safeOld)}'" : "NULL"
    String newSql = safeNew != null ? "'${esc(safeNew)}'" : "NULL"
    String hashSql = fileHash != null ? "'${esc(fileHash)}'" : "NULL"
    execSql("""
        INSERT INTO gobd_audit_log (created_at, user_id, entity_type, entity_id, action, old_value, new_value, file_hash, details)
        VALUES (CURRENT_TIMESTAMP, '${esc(currentUser())}', '${esc(entityType)}', ${entityId}, '${esc(action)}', ${oldSql}, ${newSql}, ${hashSql}, '${esc(safeDetails)}')
    """)
}

def getLockRecord = { long docId ->
    def data = selectRows(
        "SELECT document_id, document_number, status, finalized_at, finalized_by, customer_snapshot, positions_snapshot, archive_path, artifact_format, sha256_hash, prev_sha256_hash, cancels_document_id FROM gobd_document_lock WHERE document_id = ${docId}"
    )
    return (data && data.length > 0) ? data[0] : null
}

def fetchSubItems = { Item doc ->
    try {
        return DatabaseObject.getReferencedObjects(doc, Context.getSubItem(), new SubItem(), false) ?: []
    } catch (NodataFoundException ignored) {
        return []
    }
}

def nextSequenceNumber = { String prefix, int year ->
    String pattern = "${prefix}-${year}-"
    int maxNum = 0
    [
        "SELECT document_number FROM gobd_document_lock WHERE document_number LIKE '${esc(pattern)}%'",
        "SELECT cnumber FROM items WHERE cnumber LIKE '${esc(pattern)}%'"
    ].each { String sql ->
        def rows = selectRows(sql)
        rows?.each { r ->
            String val = r[0]?.toString()?.trim() ?: ""
            if (val.startsWith(pattern)) {
                String tail = val.substring(pattern.length())
                if (tail.isInteger()) {
                    maxNum = Math.max(maxNum, tail.toInteger())
                }
            }
        }
    }
    return String.format("%s-%d-%05d", prefix, year, maxNum + 1)
}

def isTaxRelevantDocument = { DatabaseObject obj ->
    if (obj instanceof Item) {
        int type = ((Item) obj).__getInttype()
        return type == Item.TYPE_INVOICE || type == Item.TYPE_CREDIT
    }
    String ctxName = obj?.getContext()?.getDbIdentity()?.toLowerCase() ?: ""
    return ctxName.contains("expense") || ctxName.contains("revenue")
}

def isMovedToTrash = { DatabaseObject obj ->
    try {
        if (obj.respondsTo("__getInvisible")) {
            def v = obj.__getInvisible()
            return v != null && v.toString() != "0" && v.toString().toLowerCase() != "false"
        }
        if (obj.respondsTo("isInvisible")) {
            return Boolean.TRUE == obj.isInvisible()
        }
    } catch (Exception ignored) { }
    return false
}

// Deterministischer Fingerabdruck der steuerrelevanten Kernfelder eines Belegs
// (intstatus und intreminders sind bewusst NICHT enthalten, damit Bezahlt/Gemahnt moeglich bleibt)
def createFinancialCoreSnapshot = { Item doc ->
    return [
        "Num=${doc.__getCnumber() ?: ''}",
        "Type=${doc.__getInttype()}",
        "Contact=${doc.__getContactsids()}",
        "Acc=${doc.__getAccountsids()}",
        "IssueDate=${fmtDate(doc.__getDateadded())}",
        "DeliveryDate=${fmtDate(doc.__getDatetodo())}",
        "EndDate=${fmtDate(doc.__getDateend())}",
        "Net=${fmtDec(doc.__getNetvalue())}",
        "Disc=${fmtDec(doc.__getDiscountvalue())}",
        "Tax=${fmtDec(doc.__getTaxvalue())}",
        "DiscGross=${fmtDec(doc.__getDiscountGrosvalue())}",
        "Desc=${(doc.__getDescription() ?: '').trim()}"
    ].join("|")
}

def formatContactSnapshot = { Contact c ->
    return [
        "ID=${c.__getIDS()}",
        "Nr=${c.__getCNumber() ?: ''}",
        "Firma=${c.__getCompany() ?: ''}",
        "Name=${c.__getPrename() ?: ''} ${c.__getCname() ?: ''}".trim(),
        "Strasse=${c.__getStreet() ?: ''}",
        "PLZ=${c.__getZip() ?: ''}",
        "Ort=${c.__getCity() ?: ''}",
        "Land=${c.__getCountry() ?: ''}",
        "SteuerNr=${c.__getTaxnumber() ?: ''}"
    ].join(" | ")
}

def createContactSnapshot = { long contactId ->
    if (contactId <= 0) return "Kein Kontakt zugewiesen"
    try {
        Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), (int) contactId)
        if (c == null) return "Kontakt #${contactId} (nicht gefunden)"
        return formatContactSnapshot(c)
    } catch (Exception ignored) {
        return "Kontakt #${contactId}"
    }
}

def createPositionsSnapshot = { Item doc ->
    try {
        def subItems = fetchSubItems(doc)
        if (!subItems || subItems.isEmpty()) return "Keine Positionen"
        return subItems.collect { SubItem s ->
            "${fmtDec(s.__getQuantityvalue())}x ${s.__getDescription() ?: ''} @ ${fmtDec(s.__getExternalvalue())} EUR (USt ${fmtDec(s.__getTaxpercentvalue())}%)"
        }.join(" ;; ")
    } catch (Exception ignored) {
        return "Positionen nicht lesbar"
    }
}

def computeChainedSha256 = { byte[] fileBytes, String prevHash ->
    MessageDigest digest = MessageDigest.getInstance("SHA-256")
    digest.update(fileBytes)
    digest.update(prevHash.getBytes(StandardCharsets.UTF_8))
    StringBuilder hex = new StringBuilder(64)
    for (byte b : digest.digest()) {
        hex.append(String.format("%02x", b))
    }
    return hex.toString()
}

def getLatestChainHash = {
    def data = selectRows(
        "SELECT sha256_hash FROM gobd_document_lock ORDER BY COALESCE(chain_seq, document_id) DESC, finalized_at DESC, document_id DESC"
    )
    return (data && data.length > 0 && data[0][0]) ? data[0][0].toString() : GENESIS_HASH
}

def getNextChainSeq = {
    def data = selectRows("SELECT COALESCE(MAX(chain_seq), 0) FROM gobd_document_lock")
    long currentMax = (data && data.length > 0 && data[0][0] != null) ? Long.parseLong(data[0][0].toString()) : 0L
    return currentMax + 1L
}

def makeReadOnly = { Path file ->
    try {
        file.toFile().setReadOnly()
        Set<PosixFilePermission> perms = EnumSet.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.GROUP_READ,
            PosixFilePermission.OTHERS_READ
        )
        Files.setPosixFilePermissions(file, perms)
    } catch (Exception ignored) {
        // Unter Windows greift bereits setReadOnly()
    }
}

def validateMandatoryFields = { Item doc ->
    List<String> errors = []
    String num = doc.__getCnumber()?.trim() ?: ""
    if (!num || num.toUpperCase().startsWith("ENTWURF") || num.toUpperCase().startsWith("DRAFT")) {
        errors << "Endgueltige fortlaufende Rechnungsnummer fehlt (aktuell: '${num}') (§ 14 Abs. 4 Nr. 4 UStG)"
    }
    if (!doc.__getDateadded()) {
        errors << "Ausstellungsdatum fehlt (§ 14 Abs. 4 Nr. 3 UStG)"
    }
    if (!doc.__getDatetodo()) {
        errors << "Leistungs-/Lieferdatum fehlt (§ 14 Abs. 4 Nr. 6 UStG)"
    }
    if (doc.__getContactsids() <= 0) {
        errors << "Leistungsempfaenger/Kontakt fehlt (§ 14 Abs. 4 Nr. 1 UStG)"
    } else {
        try {
            Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), doc.__getContactsids())
            if (c != null && (!c.__getStreet()?.trim() || !c.__getCity()?.trim())) {
                errors << "Vollstaendige Anschrift (Strasse/Ort) des Leistungsempfaengers fehlt (§ 14 Abs. 4 Nr. 1 UStG)"
            }
        } catch (Exception ignored) { }
    }
    if (doc.__getNetvalue() == null || doc.getGrossAmount() == null) {
        errors << "Entgelt / Bruttobetrag fehlt (§ 14 Abs. 4 Nr. 7 UStG)"
    }
    def subItems = fetchSubItems(doc)
    if (!subItems || subItems.isEmpty()) {
        errors << "Mindestens eine Rechnungsposition ist erforderlich (§ 14 Abs. 4 Nr. 5 UStG)"
    }
    return errors
}

def resolveUniqueArchivePath = { Path dir, String baseName, String ext ->
    Path candidate = dir.resolve("${baseName}.${ext}")
    if (!Files.exists(candidate)) return candidate
    String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"))
    return dir.resolve("${baseName}_${ts}.${ext}")
}

def archiveImmutableArtifact = { Item doc, int year, String safeDocNum, String customerSnap ->
    Path yearDir = getArchiveRoot().resolve(String.valueOf(year))
    Files.createDirectories(yearDir)

    Path recentFile = lastGeneratedFiles.get((long) doc.__getIDS())
    if (recentFile != null && Files.exists(recentFile)) {
        String fileName = recentFile.getFileName().toString()
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase() : "pdf"
        String format = ext == "xml" ? "XRECHNUNG_XML" : "ZUGFERD_PDFA3"
        Path targetFile = resolveUniqueArchivePath(yearDir, safeDocNum, ext)
        Files.copy(recentFile, targetFile)
        makeReadOnly(targetFile)
        return [path: targetFile, bytes: Files.readAllBytes(targetFile), format: format]
    }

    Path targetFile = resolveUniqueArchivePath(yearDir, safeDocNum, "xml")
    def subItems = fetchSubItems(doc)
    BigDecimal netAfterDiscount = (doc.__getNetvalue() ?: BigDecimal.ZERO).subtract(doc.__getDiscountvalue() ?: BigDecimal.ZERO)
    BigDecimal taxAmount = doc.__getTaxvalue() ?: BigDecimal.ZERO
    BigDecimal grossAmount = doc.getGrossAmount() ?: BigDecimal.ZERO

    StringBuilder xml = new StringBuilder()
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    xml.append("<GoBDArchivedInvoice xmlns=\"urn:openyabs:gobd:archive:1.0\" standard=\"EN16931\">\n")
    xml.append("  <DocumentId>${doc.__getIDS()}</DocumentId>\n")
    xml.append("  <DocumentNumber>${escapeXml(doc.__getCnumber())}</DocumentNumber>\n")
    xml.append("  <DocumentType>${doc.__getInttype() == Item.TYPE_CREDIT ? '381_CREDIT_NOTE' : '380_INVOICE'}</DocumentType>\n")
    xml.append("  <IssueDate>${escapeXml(fmtDate(doc.__getDateadded()))}</IssueDate>\n")
    xml.append("  <DeliveryDate>${escapeXml(fmtDate(doc.__getDatetodo()))}</DeliveryDate>\n")
    xml.append("  <ContactId>${doc.__getContactsids()}</ContactId>\n")
    xml.append("  <FrozenCustomerSnapshot>${escapeXml(customerSnap)}</FrozenCustomerSnapshot>\n")
    xml.append("  <NetAmount currencyID=\"EUR\">${fmtDec(netAfterDiscount)}</NetAmount>\n")
    xml.append("  <TaxAmount currencyID=\"EUR\">${fmtDec(taxAmount)}</TaxAmount>\n")
    xml.append("  <GrossAmount currencyID=\"EUR\">${fmtDec(grossAmount)}</GrossAmount>\n")
    xml.append("  <LineItems>\n")
    subItems.each { SubItem line ->
        xml.append("    <LineItem>\n")
        xml.append("      <Description>${escapeXml(line.__getDescription())}</Description>\n")
        xml.append("      <Quantity>${fmtDec(line.__getQuantityvalue())}</Quantity>\n")
        xml.append("      <UnitPrice>${fmtDec(line.__getExternalvalue())}</UnitPrice>\n")
        xml.append("      <DiscountPercent>${fmtDec(line.__getDiscount())}</DiscountPercent>\n")
        xml.append("      <TaxRatePercent>${fmtDec(line.__getTaxpercentvalue())}</TaxRatePercent>\n")
        xml.append("    </LineItem>\n")
    }
    xml.append("  </LineItems>\n")
    xml.append("</GoBDArchivedInvoice>\n")

    byte[] bytes = xml.toString().getBytes(StandardCharsets.UTF_8)
    Files.write(targetFile, bytes, StandardOpenOption.CREATE_NEW)
    makeReadOnly(targetFile)
    return [path: targetFile, bytes: bytes, format: "XRECHNUNG_XML"]
}

def finalizeDocument = { Item doc, Long cancelsDocId = null ->
    if (!finalizingIds.add(doc.__getIDS())) {
        return false
    }
    try {
        if (getLockRecord(doc.__getIDS()) != null) {
            notifyStatus("GoBD: Beleg ${doc.__getCnumber()} ist bereits festgeschrieben.")
            return false
        }

        int year = LocalDate.now().getYear()
        String rawNumber = doc.__getCnumber()?.trim() ?: ""
        if (!rawNumber || rawNumber.toUpperCase().startsWith("ENTWURF") || rawNumber.toUpperCase().startsWith("DRAFT")) {
            String prefix = (doc.__getInttype() == Item.TYPE_CREDIT) ? "ST" : "RE"
            String assignedNumber = nextSequenceNumber(prefix, year)
            doc.setCnumber(assignedNumber)
            doc.save(true)
        }

        List<String> validationErrors = validateMandatoryFields(doc)
        if (!validationErrors.isEmpty()) {
            showError("GoBD-Festschreibung abgebrochen (§ 14 Abs. 4 UStG):\n- " + validationErrors.join("\n- "))
            return false
        }

        String safeDocNum = (doc.__getCnumber() ?: "BELEG_${doc.__getIDS()}").replaceAll("[^a-zA-Z0-9._-]", "_")
        String customerSnap = createContactSnapshot((long) doc.__getContactsids())
        String positionsSnap = createPositionsSnapshot(doc)

        Map archived = archiveImmutableArtifact(doc, year, safeDocNum, customerSnap)
        Path targetFile = (Path) archived.path
        byte[] artifactBytes = (byte[]) archived.bytes
        String format = (String) archived.format

        long chainSeq = getNextChainSeq()
        String prevHash = getLatestChainHash()
        String currentHash = computeChainedSha256(artifactBytes, prevHash)
        String cancelsSql = cancelsDocId != null ? String.valueOf(cancelsDocId) : "NULL"

        execSql("""
            INSERT INTO gobd_document_lock
                (document_id, chain_seq, document_number, status, finalized_at, finalized_by, customer_snapshot, positions_snapshot, archive_path, artifact_format, sha256_hash, prev_sha256_hash, cancels_document_id)
            VALUES
                (${doc.__getIDS()}, ${chainSeq}, '${esc(doc.__getCnumber())}', 'FINALIZED', CURRENT_TIMESTAMP, '${esc(currentUser())}', '${esc(customerSnap)}', '${esc(positionsSnap)}', '${esc(targetFile.toString())}', '${esc(format)}', '${currentHash}', '${prevHash}', ${cancelsSql})
        """)

        logAudit("INVOICE", doc.__getIDS(), "FINALIZED", "STATUS=DRAFT", "STATUS=FINALIZED | SNAPSHOT=${customerSnap}", currentHash, "Festgeschrieben: ${doc.__getCnumber()} (${format})")
        notifyDocumentEvent(
            "GoBD-Festschreibung erfolgreich:\nBeleg: ${doc.__getCnumber()} (${format})\nSHA-256: ${currentHash.substring(0, 16)}...\nArchiv: ${targetFile.getFileName()}",
            doc
        )
        return true
    } finally {
        finalizingIds.remove(doc.__getIDS())
    }
}

// -----------------------------------------------------------------------------
// 3. Datensatz-Waechter ueber yabs.addRecordModifier(...)
//    - Blockiert UPDATE (steuerrelevante Felder) & DELETE/Papierkorb auf festgeschriebenen Belegen & Positionen
//    - Erlaubt reinen Offene-Posten-/Zahlungsstatus-Workflow (Bezahlt, Gemahnt) & protokolliert im Audit-Log
//    - Protokolliert Stammdaten-Aenderungen (Kontakte) mit Alt- und Neuwert
// -----------------------------------------------------------------------------
def checkWriteAllowed = { DatabaseObject obj, boolean isDelete ->
    // 1. Schutz fuer Rechnungen & Gutschriften (Item)
    if (obj instanceof Item && isTaxRelevantDocument(obj) && obj.__getIDS() > 0) {
        Item doc = (Item) obj
        def lock = getLockRecord(doc.__getIDS())
        if (lock != null) {
            // 1a. Endgueltiges Loeschen UND Verschieben in den Papierkorb sind bei festgeschriebenen Belegen verboten
            if (isDelete || isMovedToTrash(doc)) {
                logAudit("INVOICE", doc.__getIDS(), "BLOCKED_DELETE", lock[5]?.toString(), "Delete/Trash=true", lock[9]?.toString(), "Loesch- oder Papierkorb-Versuch an festgeschriebenem Beleg ${lock[1]} blockiert.")
                showError("GoBD-Loeschschutz (§§ 146, 147 AO):\nBeleg ${lock[1]} ist festgeschrieben und darf weder geloescht noch in den Papierkorb verschoben werden!\nBitte erstellen Sie stattdessen eine Stornorechnung ueber 'Plugins > GoBD: 2. Beleg stornieren'.")
                return false
            }

            // 1b. Pruefen, ob nur Zahlungsstatus (intstatus) oder Mahnstufe (intreminders) geaendert wurde
            def dbRows = selectRows(
                "SELECT cnumber, inttype, contactsids, accountsids, dateadded, datetodo, dateend, netvalue, discountvalue, taxvalue, discountgrosvalue, description, intstatus, intreminders FROM items WHERE ids = ${doc.__getIDS()}"
            )
            if (dbRows && dbRows.length > 0) {
                def db = dbRows[0]
                String dbCore = [
                    "Num=${db[0] ?: ''}",
                    "Type=${db[1]}",
                    "Contact=${db[2]}",
                    "Acc=${db[3]}",
                    "IssueDate=${fmtDate(db[4])}",
                    "DeliveryDate=${fmtDate(db[5])}",
                    "EndDate=${fmtDate(db[6])}",
                    "Net=${fmtDec(db[7])}",
                    "Disc=${fmtDec(db[8])}",
                    "Tax=${fmtDec(db[9])}",
                    "DiscGross=${fmtDec(db[10])}",
                    "Desc=${(db[11]?.toString() ?: '').trim()}"
                ].join("|")
                String newCore = createFinancialCoreSnapshot(doc)

                if (dbCore == newCore) {
                    int oldStatus = db[12] != null ? Integer.parseInt(db[12].toString()) : 0
                    int newStatus = doc.__getIntstatus()
                    int oldReminders = db[13] != null ? Integer.parseInt(db[13].toString()) : 0
                    int newReminders = doc.__getIntreminders()

                    // Manuelles Setzen auf STATUS_CANCELLED ohne Stornobeleg verhindern
                    if (newStatus == Item.STATUS_CANCELLED && oldStatus != Item.STATUS_CANCELLED && !creatingStorno) {
                        logAudit("INVOICE", doc.__getIDS(), "BLOCKED_MANUAL_CANCEL", "intstatus=${oldStatus}", "intstatus=${newStatus}", lock[9]?.toString(), "Manuelles Stornieren ohne Stornorechnung fuer Beleg ${lock[1]} blockiert.")
                        showError("GoBD-Schutz: Ein festgeschriebener Beleg darf nicht einfach auf 'Storniert' gesetzt werden.\nBitte nutzen Sie 'Plugins > GoBD: 2. Beleg stornieren (Stornorechnung)', um eine ordnungsgemaesse Gegenbuchung zu erzeugen.")
                        return false
                    }

                    if (oldStatus != newStatus) {
                        String oldText = Item.getStatusString(oldStatus)
                        String newText = Item.getStatusString(newStatus)
                        logAudit("INVOICE", doc.__getIDS(), "PAYMENT_STATUS_CHANGED", "status=${oldStatus}(${oldText})", "status=${newStatus}(${newText})", lock[9]?.toString(), "Zahlungsstatus fuer Beleg ${lock[1]} geaendert: ${oldText} -> ${newText}")
                        notifyStatus("GoBD-Audit: Status fuer Beleg ${lock[1]} protokolliert (${oldText} -> ${newText}).")
                    }
                    if (oldReminders != newReminders) {
                        logAudit("INVOICE", doc.__getIDS(), "DUNNING_CHANGED", "intreminders=${oldReminders}", "intreminders=${newReminders}", lock[9]?.toString(), "Mahnstufe fuer Beleg ${lock[1]} aktualisiert: ${oldReminders} -> ${newReminders}")
                        notifyStatus("GoBD-Audit: Mahnstufe fuer Beleg ${lock[1]} protokolliert (${oldReminders} -> ${newReminders}).")
                    }
                    return true
                }
            }

            // 1c. Steuerrelevante Felder wurden geaendert -> blockieren
            logAudit("INVOICE", doc.__getIDS(), "BLOCKED_EDIT", lock[5]?.toString(), createFinancialCoreSnapshot(doc), lock[9]?.toString(), "Unzulaessige Aenderung steuerrelevanter Felder an Beleg ${lock[1]} blockiert.")
            showError("GoBD-Schutz (§ 146 Abs. 4 AO):\nBeleg ${lock[1]} ist festgeschrieben! Betraege, Steuern, Datum, Positionen und Empfaenger duerfen nicht mehr geaendert werden.\n(Hinweis: Nur Zahlungsstatus wie 'Bezahlt' und Mahnungen sind erlaubt. Fuer Korrekturen bitte 'Plugins > GoBD: 2. Beleg stornieren' nutzen.)")
            return false
        }
    }

    // 2. Schutz fuer einzelne Rechnungspositionen (SubItem), falls Hauptbeleg festgeschrieben ist
    if (obj instanceof SubItem) {
        SubItem sub = (SubItem) obj
        long parentId = sub.__getItemsids()
        if (parentId > 0) {
            def lock = getLockRecord(parentId)
            if (lock != null) {
                logAudit("SUBITEM", sub.__getIDS(), isDelete ? "BLOCKED_DELETE" : "BLOCKED_EDIT", lock[6]?.toString(), sub.__getDescription(), lock[9]?.toString(), "Positionsaenderung an festgeschriebenem Beleg ${lock[1]} blockiert.")
                showError("GoBD-Schutz: Position gehoert zu festgeschriebenem Beleg ${lock[1]} und ist gesperrt.")
                return false
            }
        }
    }

    // 3. Stammdaten-Historisierung (Kontakte): Alt-Wert vor dem Speichern merken
    if (obj instanceof Contact && obj.__getIDS() > 0 && !isDelete) {
        masterDataBeforeSave.put("CONTACT_${obj.__getIDS()}".toString(), createContactSnapshot(obj.__getIDS()))
    }

    return true
}

yabs.addRecordModifier([
    modifyOnExplode: { DatabaseObject obj ->
        return obj
    },
    modifyOnSave: { DatabaseObject obj ->
        if (!checkWriteAllowed(obj, false)) {
            throw vetoException("GoBD-Schreibschutz: Datensatz ist festgeschrieben.")
        }
        if (obj instanceof Contact && obj.__getIDS() > 0) {
            Contact c = (Contact) obj
            String oldSnap = masterDataBeforeSave.remove("CONTACT_${c.__getIDS()}".toString())
            String newSnap = formatContactSnapshot(c)
            if (oldSnap != null && oldSnap != newSnap) {
                logAudit("CONTACT", c.__getIDS(), "MASTERDATA_CHANGED", oldSnap, newSnap, null, "Kundenstammdaten geaendert (${c.__getCname()})")
                notifyStatus("GoBD: Stammdaten-Aenderung protokolliert (${c.__getCname()}).")
            }
        }
        return obj
    },
    executeAfterCreate: { DatabaseObject obj ->
        if (obj instanceof Item && isTaxRelevantDocument(obj) && obj.__getIDS() > 0) {
            Item doc = (Item) obj
            logAudit("INVOICE", doc.__getIDS(), "DRAFT_CREATED", null, "Cnumber=${doc.__getCnumber()}", null, "Entwurf angelegt: ${doc.__getCnumber()}")
        }
        return obj
    },
    executeAfterSave: { DatabaseObject obj ->
        return obj
    },
    modifyOnResolve: { Map<String, Object> map ->
        return map
    },
    modifyOnDelete: { DatabaseObject obj ->
        if (!checkWriteAllowed(obj, true)) {
            throw vetoException("GoBD-Loeschschutz: Datensatz unterliegt der Aufbewahrungspflicht.")
        }
        return obj
    }
] as DatabaseObjectModifier)

// -----------------------------------------------------------------------------
// 4. Reaktion auf Aktivitaeten ueber yabs.onActivity(...)
//    - Automatische Festschreibung beim PDF-/E-Rechnungs-Export (GoBD-Standard: aktiv)
// -----------------------------------------------------------------------------
yabs.onActivity({ Activity.Entry entry ->
    if (entry.kind == Activity.Kind.CREATED && entry.file != null && entry.id > 0) {
        lastGeneratedFiles.put((long) entry.id, entry.file.toPath())
        if (creatingStorno || finalizingIds.contains((int) entry.id)) return
        String entryCtx = entry.context?.getDbIdentity() ?: ""
        String invCtx = Context.getInvoice()?.getDbIdentity() ?: ""
        String credCtx = Context.getCredit()?.getDbIdentity() ?: ""
        if ((entryCtx == invCtx || entryCtx == credCtx) && getLockRecord(entry.id) == null) {
            boolean autoLock = yabs.settings().getBoolean("gobd.autoLockOnExport", true)
            if (autoLock) {
                try {
                    Item doc = (Item) DatabaseObject.getObject(entry.context, (int) entry.id)
                    if (doc != null) {
                        finalizeDocument(doc, null)
                    }
                } catch (Exception ex) {
                    showError("GoBD-Auto-Festschreibung fehlgeschlagen:\n${ex.getMessage()}")
                }
            } else {
                notifyStatus("Belegdatei erstellt (${entry.file.getName()}). Jetzt ueber 'Plugins > GoBD: 1. Beleg festschreiben' sichern.")
            }
        }
    }
})

// -----------------------------------------------------------------------------
// 5. GDPdU / GoBD Beschreibungsstandard-Generator (index.xml & gdpdu-01-09-2004.dtd)
// -----------------------------------------------------------------------------
def buildGdpduDtd = {
    return """<?xml version="1.0" encoding="UTF-8"?>
<!ELEMENT DataSet (Version, DataSupplier?, Media+)>
<!ELEMENT Version (#PCDATA)>
<!ELEMENT DataSupplier (Name, Location, Comment?)>
<!ELEMENT Name (#PCDATA)>
<!ELEMENT Location (#PCDATA)>
<!ELEMENT Comment (#PCDATA)>
<!ELEMENT Media (Name, Table+)>
<!ELEMENT Table (URL, Name, Description?, Validity?, UTF8?, DecimalSymbol?, DigitGroupingSymbol?, VariableLength)>
<!ELEMENT URL (#PCDATA)>
<!ELEMENT Description (#PCDATA)>
<!ELEMENT Validity (Range, Format?)>
<!ELEMENT Range (From, To?)>
<!ELEMENT From (#PCDATA)>
<!ELEMENT To (#PCDATA)>
<!ELEMENT Format (#PCDATA)>
<!ELEMENT UTF8 EMPTY>
<!ELEMENT DecimalSymbol (#PCDATA)>
<!ELEMENT DigitGroupingSymbol (#PCDATA)>
<!ELEMENT VariableLength (ColumnDelimiter?, RecordDelimiter?, TextEncapsulator?, VariablePrimaryKey*, VariableColumn*, ForeignKey*)>
<!ELEMENT ColumnDelimiter (#PCDATA)>
<!ELEMENT RecordDelimiter (#PCDATA)>
<!ELEMENT TextEncapsulator (#PCDATA)>
<!ELEMENT VariablePrimaryKey (Name, Description?, (AlphaNumeric | Numeric | Date))>
<!ELEMENT VariableColumn (Name, Description?, (AlphaNumeric | Numeric | Date))>
<!ELEMENT AlphaNumeric EMPTY>
<!ELEMENT Numeric (ImpliedAccuracy | Accuracy)?>
<!ELEMENT ImpliedAccuracy (#PCDATA)>
<!ELEMENT Accuracy (#PCDATA)>
<!ELEMENT Date (Format)?>
<!ELEMENT ForeignKey (Name+, References)>
<!ELEMENT References (#PCDATA)>
"""
}

def buildGdpduIndexXml = { int exportYear ->
    return """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE DataSet SYSTEM "gdpdu-01-09-2004.dtd">
<DataSet>
  <Version>1.0</Version>
  <DataSupplier>
    <Name>OpenYaBS GoBD-Export</Name>
    <Location>Deutschland</Location>
    <Comment>Maschinell auswertbarer Z3-Datentraegerueberlassungs-Export nach GoBD / GDPdU fuer Pruefungsjahr ${exportYear}</Comment>
  </DataSupplier>
  <Media>
    <Name>GoBD_Z3_${exportYear}</Name>
    <Table>
      <URL>rechnungen.csv</URL>
      <Name>Rechnungsjournal</Name>
      <Description>Festgeschriebene Ausgangsrechnungen und Stornobelege inkl. SHA-256-Hashkette</Description>
      <UTF8/>
      <DecimalSymbol>.</DecimalSymbol>
      <DigitGroupingSymbol>,</DigitGroupingSymbol>
      <VariableLength>
        <ColumnDelimiter>;</ColumnDelimiter>
        <RecordDelimiter>&#x0A;</RecordDelimiter>
        <TextEncapsulator>"</TextEncapsulator>
        <VariablePrimaryKey><Name>document_id</Name><Numeric><Accuracy>0</Accuracy></Numeric></VariablePrimaryKey>
        <VariableColumn><Name>document_number</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>status</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>issue_date</Name><Date><Format>YYYY-MM-DD</Format></Date></VariableColumn>
        <VariableColumn><Name>delivery_date</Name><Date><Format>YYYY-MM-DD</Format></Date></VariableColumn>
        <VariableColumn><Name>contact_id</Name><Numeric><Accuracy>0</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>net_amount</Name><Numeric><Accuracy>2</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>tax_amount</Name><Numeric><Accuracy>2</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>gross_amount</Name><Numeric><Accuracy>2</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>artifact_format</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>sha256_hash</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>prev_sha256_hash</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>cancels_document_id</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>customer_snapshot</Name><AlphaNumeric/></VariableColumn>
      </VariableLength>
    </Table>
    <Table>
      <URL>rechnungspositionen.csv</URL>
      <Name>Rechnungspositionen</Name>
      <Description>Einzelpositionen aller festgeschriebenen Rechnungen und Gutschriften</Description>
      <UTF8/>
      <DecimalSymbol>.</DecimalSymbol>
      <DigitGroupingSymbol>,</DigitGroupingSymbol>
      <VariableLength>
        <ColumnDelimiter>;</ColumnDelimiter>
        <RecordDelimiter>&#x0A;</RecordDelimiter>
        <TextEncapsulator>"</TextEncapsulator>
        <VariablePrimaryKey><Name>position_id</Name><Numeric><Accuracy>0</Accuracy></Numeric></VariablePrimaryKey>
        <VariableColumn><Name>document_id</Name><Numeric><Accuracy>0</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>description</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>quantity</Name><Numeric><Accuracy>2</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>unit_price</Name><Numeric><Accuracy>2</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>tax_rate</Name><Numeric><Accuracy>2</Accuracy></Numeric></VariableColumn>
        <ForeignKey>
          <Name>document_id</Name>
          <References>Rechnungsjournal</References>
        </ForeignKey>
      </VariableLength>
    </Table>
    <Table>
      <URL>kunden.csv</URL>
      <Name>Kundenstammdaten</Name>
      <Description>Kunden- und Lieferantenstammdaten</Description>
      <UTF8/>
      <DecimalSymbol>.</DecimalSymbol>
      <DigitGroupingSymbol>,</DigitGroupingSymbol>
      <VariableLength>
        <ColumnDelimiter>;</ColumnDelimiter>
        <RecordDelimiter>&#x0A;</RecordDelimiter>
        <TextEncapsulator>"</TextEncapsulator>
        <VariablePrimaryKey><Name>contact_id</Name><Numeric><Accuracy>0</Accuracy></Numeric></VariablePrimaryKey>
        <VariableColumn><Name>customer_number</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>company</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>name</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>street</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>zip</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>city</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>country</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>tax_number</Name><AlphaNumeric/></VariableColumn>
      </VariableLength>
    </Table>
    <Table>
      <URL>audit_log.csv</URL>
      <Name>GoBD_Audit_Protokoll</Name>
      <Description>Unveraenderliches Historien- und Zugriffsprotokoll (inkl. Alt/Neu-Werten)</Description>
      <UTF8/>
      <DecimalSymbol>.</DecimalSymbol>
      <DigitGroupingSymbol>,</DigitGroupingSymbol>
      <VariableLength>
        <ColumnDelimiter>;</ColumnDelimiter>
        <RecordDelimiter>&#x0A;</RecordDelimiter>
        <TextEncapsulator>"</TextEncapsulator>
        <VariablePrimaryKey><Name>id</Name><Numeric><Accuracy>0</Accuracy></Numeric></VariablePrimaryKey>
        <VariableColumn><Name>created_at</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>user_id</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>entity_type</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>entity_id</Name><Numeric><Accuracy>0</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>action</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>old_value</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>new_value</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>file_hash</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>details</Name><AlphaNumeric/></VariableColumn>
      </VariableLength>
    </Table>
  </Media>
</DataSet>
"""
}

// -----------------------------------------------------------------------------
// 6. Menueeintraege im Menue „Plugins“ & in „Search anything“ (yabs.addCommand)
// -----------------------------------------------------------------------------
def selectInvoiceDialog = { String title, boolean onlyLocked ->
    String sql = onlyLocked
        ? "SELECT i.ids, i.cnumber, (COALESCE(i.netvalue, 0) - COALESCE(i.discountvalue, 0) + COALESCE(i.taxvalue, 0)) FROM items i INNER JOIN gobd_document_lock g ON i.ids = g.document_id WHERE g.status = 'FINALIZED' ORDER BY i.ids DESC"
        : "SELECT i.ids, i.cnumber, (COALESCE(i.netvalue, 0) - COALESCE(i.discountvalue, 0) + COALESCE(i.taxvalue, 0)) FROM items i LEFT JOIN gobd_document_lock g ON i.ids = g.document_id WHERE g.document_id IS NULL AND i.inttype IN (${Item.TYPE_INVOICE}, ${Item.TYPE_CREDIT}) ORDER BY i.ids DESC"
    def rows = selectRows(sql)
    if (!rows || rows.length == 0) {
        notifyPopup(onlyLocked ? "Keine festgeschriebenen Rechnungen vorhanden." : "Keine offenen (unfestgeschriebenen) Rechnungen vorhanden.")
        return null
    }
    String[] choices = rows.collect { "${it[0]} | ${it[1]} (${fmtDec(it[2])} EUR)" } as String[]
    String selected = (String) JOptionPane.showInputDialog(
        null, "Bitte Beleg auswaehlen:", title, JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]
    )
    if (!selected) return null
    int id = Integer.parseInt(selected.split("\\|")[0].trim())
    return (Item) DatabaseObject.getObject(Context.getInvoice(), id)
}

yabs.addCommand("GoBD: 1. Beleg festschreiben", "Schreibt eine Rechnung unveraenderbar nach GoBD fest (inkl. Stammdaten-Snapshot & SHA-256 Hash-Kette)", {
    Item doc = selectInvoiceDialog("GoBD-Festschreibung (§ 146 Abs. 4 AO)", false)
    if (doc != null) {
        finalizeDocument(doc, null)
    }
})

yabs.addCommand("GoBD: 2. Beleg stornieren (Stornorechnung)", "Erstellt eine festgeschriebene Stornorechnung fuer einen gesperrten Beleg", {
    Item original = selectInvoiceDialog("GoBD-Stornorechnung erstellen", true)
    if (original == null) return

    def origLock = getLockRecord(original.__getIDS())
    if (origLock == null || origLock[2].toString() == "CANCELLED") {
        showError("Dieser Beleg wurde bereits storniert oder ist nicht festgeschrieben.")
        return
    }

    int yr = LocalDate.now().getYear()
    String stornoNum = nextSequenceNumber("ST", yr)

    creatingStorno = true
    try {
        Item storno = new Item()
        storno.setContext(Context.getCredit())
        storno.setContactsids(original.__getContactsids())
        storno.setAccountsids(original.__getAccountsids())
        storno.setGroupsids(original.__getGroupsids())
        storno.setInttype(Item.TYPE_CREDIT)
        storno.setCnumber(stornoNum)
        storno.setDateadded(new Date())
        storno.setDatetodo(original.__getDatetodo() ?: new Date())
        storno.setNetvalue((original.__getNetvalue() ?: BigDecimal.ZERO).negate())
        storno.setTaxvalue((original.__getTaxvalue() ?: BigDecimal.ZERO).negate())
        storno.setDiscountvalue((original.__getDiscountvalue() ?: BigDecimal.ZERO).negate())
        storno.setDiscountGrosvalue((original.__getDiscountGrosvalue() ?: BigDecimal.ZERO).negate())
        storno.setDescription("Stornorechnung zu Rechnung Nr. ${original.__getCnumber()}")
        storno.save(true)

        if (storno.__getIDS() <= 0) {
            showError("Stornorechnung konnte nicht in der Datenbank gespeichert werden.")
            return
        }

        def origLines = fetchSubItems(original)
        origLines.each { SubItem line ->
            SubItem revLine = new SubItem()
            revLine.setItemsids(storno.__getIDS())
            revLine.setOrdernr(line.getOrdernr())
            revLine.setCname(line.__getCname()?.trim() ? line.__getCname() : " ")
            revLine.setMeasure(line.__getMeasure() ?: "")
            revLine.setDescription(line.__getDescription() ?: "")
            revLine.setQuantityvalue((line.__getQuantityvalue() ?: BigDecimal.ZERO).negate())
            revLine.setExternalvalue(line.__getExternalvalue() ?: BigDecimal.ZERO)
            revLine.setInternalvalue(line.__getInternalvalue() ?: BigDecimal.ZERO)
            revLine.setDiscount(line.__getDiscount() ?: BigDecimal.ZERO)
            revLine.setTaxpercentvalue(line.__getTaxpercentvalue() ?: BigDecimal.ZERO)
            revLine.setDatedelivery(storno.__getDatetodo())
            revLine.setDateadded(new Date())
            revLine.setGroupsids(storno.__getGroupsids())
            revLine.save()
        }

        if (finalizeDocument(storno, (long) original.__getIDS())) {
            execSql("UPDATE gobd_document_lock SET status = 'CANCELLED' WHERE document_id = ${original.__getIDS()}")
            // Auch den YaBS-Status des Originalbelegs konsistent auf STATUS_CANCELLED setzen
            try {
                original.setIntstatus(Item.STATUS_CANCELLED)
                original.save(true)
            } catch (Exception ignored) { }
            logAudit("INVOICE", original.__getIDS(), "CANCELLED", "STATUS=FINALIZED", "STATUS=CANCELLED BY ${storno.__getCnumber()}", origLock[9]?.toString(), "Storniert durch Stornobeleg ${storno.__getCnumber()}")
            notifyDocumentEvent("Beleg ${original.__getCnumber()} storniert durch ${storno.__getCnumber()}.", original)
        }
    } finally {
        creatingStorno = false
    }
})

yabs.addCommand("GoBD: 3. Archiv-Integritaet pruefen", "Prueft alle archivierten Belege und die gesamte SHA-256-Hash-Kette", {
    yabs.background({
        def rows = selectRows(
            "SELECT document_id, document_number, archive_path, sha256_hash, prev_sha256_hash FROM gobd_document_lock ORDER BY COALESCE(chain_seq, document_id) ASC, finalized_at ASC, document_id ASC"
        )

        if (!rows || rows.length == 0) {
            return "Keine festgeschriebenen Belege im GoBD-Archiv vorhanden."
        }

        String expectedPrev = GENESIS_HASH
        int verifiedCount = 0

        for (def row : rows) {
            String docNum = row[1].toString()
            Path file = Paths.get(row[2].toString())
            String storedHash = row[3].toString()
            String storedPrev = row[4].toString()

            if (storedPrev != expectedPrev) {
                return "FEHLER: Kettenbruch vor Beleg ${docNum}! Erwartet: ${expectedPrev}, Gefunden: ${storedPrev}"
            }
            if (!Files.exists(file)) {
                return "FEHLER: Archivdatei fehlt fuer Beleg ${docNum} (${file})!"
            }
            String actualHash = computeChainedSha256(Files.readAllBytes(file), storedPrev)
            if (actualHash != storedHash) {
                return "FEHLER: Datei-Manipulation erkannt bei Beleg ${docNum}! Berechneter SHA-256-Hash weicht ab."
            }
            expectedPrev = storedHash
            verifiedCount++
        }
        return "OK: Alle ${verifiedCount} festgeschriebenen Belege und die SHA-256-Hash-Kette sind lueckenlos und unversehrt."
    }, { String report ->
        if (report.startsWith("FEHLER")) {
            Popup.error(report)
        } else {
            notifyPopup(report)
        }
    })
})

yabs.addCommand("GoBD: 4. Betriebspruefer-Export (Z3 / IDEA ZIP)", "Erzeugt ein IDEA-kompatibles Z3-Prueferpaket (index.xml, gdpdu-01-09-2004.dtd, CSVs & Originalbelege)", {
    String defaultYear = String.valueOf(LocalDate.now().getYear())
    String yearInput = JOptionPane.showInputDialog(null, "Pruefungsjahr fuer den Z3-Export eingeben:", defaultYear)
    if (!yearInput || !yearInput.isInteger()) return
    int exportYear = yearInput.toInteger()

    yabs.background({
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        Path zipPath = getArchiveRoot().resolve("YaBS_GoBD_Z3_Export_${exportYear}_${ts}.zip")

        def allLocks = selectRows(
            "SELECT g.document_id, g.document_number, g.status, g.finalized_at, g.finalized_by, g.customer_snapshot, g.positions_snapshot, g.archive_path, g.artifact_format, g.sha256_hash, g.prev_sha256_hash, g.cancels_document_id, i.dateadded, i.datetodo, i.contactsids, (COALESCE(i.netvalue, 0) - COALESCE(i.discountvalue, 0)), COALESCE(i.taxvalue, 0), (COALESCE(i.netvalue, 0) - COALESCE(i.discountvalue, 0) + COALESCE(i.taxvalue, 0)) FROM gobd_document_lock g LEFT JOIN items i ON g.document_id = i.ids ORDER BY COALESCE(g.chain_seq, g.document_id) ASC, g.finalized_at ASC"
        )
        def locks = allLocks.findAll { r -> r[3]?.toString()?.startsWith(String.valueOf(exportYear)) }
        Set<String> exportedDocIds = locks.collect { r -> r[0].toString() } as Set<String>

        def allPositions = selectRows(
            "SELECT s.ids, s.itemsids, s.description, s.quantityvalue, s.externalvalue, s.taxpercentvalue FROM subitems s INNER JOIN gobd_document_lock g ON s.itemsids = g.document_id ORDER BY s.ids ASC"
        )
        def positions = allPositions.findAll { p -> exportedDocIds.contains(p[1]?.toString()) }

        def contacts = selectRows(
            "SELECT ids, cnumber, company, cname, street, zip, city, country, taxnumber FROM contacts ORDER BY ids ASC"
        )

        def allAudits = selectRows(
            "SELECT id, created_at, user_id, entity_type, entity_id, action, old_value, new_value, file_hash, details FROM gobd_audit_log ORDER BY id ASC"
        )
        def audits = allAudits.findAll { a -> a[1]?.toString()?.startsWith(String.valueOf(exportYear)) }

        new ZipOutputStream(Files.newOutputStream(zipPath)).withCloseable { ZipOutputStream zos ->
            zos.putNextEntry(new ZipEntry("index.xml"))
            zos.write(buildGdpduIndexXml(exportYear).getBytes(StandardCharsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(new ZipEntry("gdpdu-01-09-2004.dtd"))
            zos.write(buildGdpduDtd().getBytes(StandardCharsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(new ZipEntry("rechnungen.csv"))
            StringBuilder sbDocs = new StringBuilder("document_id;document_number;status;issue_date;delivery_date;contact_id;net_amount;tax_amount;gross_amount;artifact_format;sha256_hash;prev_sha256_hash;cancels_document_id;customer_snapshot\n")
            locks.each { r ->
                sbDocs.append("${r[0]};\"${csvCell(r[1])}\";\"${csvCell(r[2])}\";${fmtDate(r[12])};${fmtDate(r[13])};${r[14] ?: 0};${fmtDec(r[15])};${fmtDec(r[16])};${fmtDec(r[17])};\"${csvCell(r[8])}\";\"${csvCell(r[9])}\";\"${csvCell(r[10])}\";\"${csvCell(r[11])}\";\"${csvCell(r[5])}\"\n")
            }
            zos.write(sbDocs.toString().getBytes(StandardCharsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(new ZipEntry("rechnungspositionen.csv"))
            StringBuilder sbPos = new StringBuilder("position_id;document_id;description;quantity;unit_price;tax_rate\n")
            positions.each { p ->
                sbPos.append("${p[0]};${p[1]};\"${csvCell(p[2])}\";${fmtDec(p[3])};${fmtDec(p[4])};${fmtDec(p[5])}\n")
            }
            zos.write(sbPos.toString().getBytes(StandardCharsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(new ZipEntry("kunden.csv"))
            StringBuilder sbCust = new StringBuilder("contact_id;customer_number;company;name;street;zip;city;country;tax_number\n")
            contacts.each { c ->
                sbCust.append("${c[0]};\"${csvCell(c[1])}\";\"${csvCell(c[2])}\";\"${csvCell(c[3])}\";\"${csvCell(c[4])}\";\"${csvCell(c[5])}\";\"${csvCell(c[6])}\";\"${csvCell(c[7])}\";\"${csvCell(c[8])}\"\n")
            }
            zos.write(sbCust.toString().getBytes(StandardCharsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(new ZipEntry("audit_log.csv"))
            StringBuilder sbAudit = new StringBuilder("id;created_at;user_id;entity_type;entity_id;action;old_value;new_value;file_hash;details\n")
            audits.each { a ->
                sbAudit.append("${a[0]};\"${csvCell(a[1])}\";\"${csvCell(a[2])}\";\"${csvCell(a[3])}\";${a[4]};\"${csvCell(a[5])}\";\"${csvCell(a[6])}\";\"${csvCell(a[7])}\";\"${csvCell(a[8])}\";\"${csvCell(a[9])}\"\n")
            }
            zos.write(sbAudit.toString().getBytes(StandardCharsets.UTF_8))
            zos.closeEntry()

            locks.each { r ->
                Path belegFile = Paths.get(r[7].toString())
                if (Files.exists(belegFile)) {
                    zos.putNextEntry(new ZipEntry("belege/${belegFile.getFileName()}"))
                    zos.write(Files.readAllBytes(belegFile))
                    zos.closeEntry()
                }
            }

            zos.putNextEntry(new ZipEntry("VERFAHRENSDOKUMENTATION_TECHNIK.txt"))
            String readme = [
                "=======================================================================",
                "OpenYaBS - Technische System- und Verfahrensdokumentation nach GoBD",
                "=======================================================================",
                "Pruefungsjahr: ${exportYear}",
                "Export-Zeitpunkt: ${ts}",
                "",
                "1. Unveraenderbarkeit (§ 146 Abs. 4 AO):",
                "   Festgeschriebene Belege (gobd_document_lock) und deren Positionen",
                "   sind durch den DatabaseObjectModifier gegen steuerrelevante Aenderungen",
                "   sowie gegen Loeschen und Papierkorb gesperrt. Reine Zahlungs- und",
                "   Mahnstatus-Aenderungen bleiben erlaubt und werden protokolliert.",
                "   Inhaltliche Korrekturen erfolgen ausschliesslich ueber Stornorechnungen (ST-*).",
                "",
                "2. Stammdaten-Snapshots:",
                "   Empfaengeranschrift, Steuernummer und Positionen werden zum Zeitpunkt",
                "   der Festschreibung unveraenderbar in gobd_document_lock sowie im",
                "   archivierten Originalbeleg gespeichert.",
                "",
                "3. Kryptographische Integritaetssicherung (SHA-256 Hash-Kette):",
                "   Jeder archivierte Originalbeleg unter /belege/ ist ueber die Formel",
                "   SHA256(Datei_Bytes || prev_sha256_hash) verkettet (Genesis: 64 Nullen).",
                "",
                "4. Maschinelle Auswertbarkeit (Z3-Zugriff / IDEA):",
                "   Dieses Archiv enthaelt eine vollstaendige index.xml sowie die",
                "   offizielle gdpdu-01-09-2004.dtd zum direkten Import in IDEA."
            ].join("\n")
            zos.write(readme.getBytes(StandardCharsets.UTF_8))
            zos.closeEntry()
        }

        return zipPath
    }, { Path zipPath ->
        logAudit("EXPORT", exportYear, "Z3_EXPORTED", null, zipPath.getFileName().toString(), null, "Z3 Prueferexport erstellt: ${zipPath.getFileName()}")
        notifyPopup("GoBD Z3-Export erstellt:\n${zipPath}")
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(zipPath.getParent().toFile())
        }
    })
})

yabs.addCommand("GoBD: 5. Pruefer-Ansicht oeffnen (Z1 Read-Only Tab)", "Oeffnet eine schreibgeschuetzte Tabelle aller festgeschriebenen Belege", {
    def lockRows = selectRows(
        "SELECT document_id, document_number, status, finalized_at, finalized_by, artifact_format, sha256_hash, cancels_document_id FROM gobd_document_lock ORDER BY COALESCE(chain_seq, document_id) DESC, finalized_at DESC"
    )

    String[] cols = ["ID", "Belegnummer", "Status", "Festgeschrieben am", "Benutzer", "Format", "SHA-256 Hash", "Storno-Bezug"] as String[]
    JTable table = new JTable(lockRows, cols) {
        @Override
        boolean isCellEditable(int row, int column) {
            return false
        }
    }
    JPanel panel = new JPanel(new BorderLayout(8, 8))
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
    panel.add(new JLabel("GoBD Z1-Prueferansicht (Schreibgeschuetztes Belegjournal - ${lockRows.length} Belege)"), BorderLayout.NORTH)
    panel.add(new JScrollPane(table), BorderLayout.CENTER)
    yabs.openTab("GoBD Z1-Prueferjournal", panel)
})

// -----------------------------------------------------------------------------
// 7. Einstellungsseite unter Settings > Plugins (yabs.addSettingsPage)
// -----------------------------------------------------------------------------
yabs.addSettingsPage("GoBD-Konformitaet", "Einstellungen fuer automatische Beleg-Festschreibung und GoBD-Archivpfad", {
    JPanel panel = new JPanel()
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))
    panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12))

    boolean currentAutoLock = yabs.settings().getBoolean("gobd.autoLockOnExport", true)
    JCheckBox autoLockCheck = new JCheckBox("Rechnungen beim Export (PDF / ZUGFeRD / XRechnung) automatisch festschreiben (GoBD-Standard)", currentAutoLock)
    autoLockCheck.setAlignmentX(Component.LEFT_ALIGNMENT)
    autoLockCheck.addActionListener({
        yabs.settings().setBoolean("gobd.autoLockOnExport", autoLockCheck.isSelected())
        notifyStatus("GoBD-Einstellung gespeichert.")
    })

    JLabel pathLabel = new JLabel("Revisionssicherer GoBD-Archivordner:")
    pathLabel.setAlignmentX(Component.LEFT_ALIGNMENT)
    pathLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0))

    JPanel rowPanel = new JPanel(new BorderLayout(6, 0))
    rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT)

    JTextField pathField = new JTextField(getArchiveRoot().toAbsolutePath().toString(), 30)
    rowPanel.add(pathField, BorderLayout.CENTER)

    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0))
    JButton browseBtn = new JButton("...")
    browseBtn.setToolTipText("Archivordner auswaehlen")
    JButton savePathBtn = new JButton("Speichern")

    def saveArchivePath = { String newPathStr ->
        try {
            Path newPath = Paths.get(newPathStr.trim()).toAbsolutePath()
            Files.createDirectories(newPath)
            yabs.settings().set("gobd.archivePath", newPath.toString())
            pathField.setText(newPath.toString())
            logAudit("SETTINGS", 0L, "ARCHIVE_PATH_CHANGED", null, newPath.toString(), null, "GoBD-Archivordner geaendert")
            notifyPopup("GoBD-Archivordner gespeichert:\n${newPath}")
        } catch (Exception ex) {
            showError("Ungueltiger oder nicht beschreibbarer Ordnerpfad:\n${ex.getMessage()}")
        }
    }

    browseBtn.addActionListener({
        JFileChooser chooser = new JFileChooser(pathField.getText())
        chooser.setDialogTitle("GoBD-Archivordner auswaehlen")
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
        chooser.setAcceptAllFileFilterUsed(false)
        if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            saveArchivePath(chooser.getSelectedFile().getAbsolutePath())
        }
    })

    savePathBtn.addActionListener({
        saveArchivePath(pathField.getText())
    })

    btnPanel.add(browseBtn)
    btnPanel.add(savePathBtn)
    rowPanel.add(btnPanel, BorderLayout.EAST)

    Dimension pref = rowPanel.getPreferredSize()
    rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) pref.getHeight()))

    panel.add(autoLockCheck)
    panel.add(pathLabel)
    panel.add(rowPanel)
    return (JComponent) panel
})

// -----------------------------------------------------------------------------
// 8. Suchanbieter fuer „Search anything“ (Shift+Shift) via yabs.addSearchProvider
// -----------------------------------------------------------------------------
yabs.addSearchProvider({ String text ->
    if (!text || text.trim().length() < 2) return []
    String q = text.trim().toLowerCase()

    def rows = selectRows(
        "SELECT document_id, document_number, status, finalized_at, archive_path FROM gobd_document_lock WHERE LOWER(document_number) LIKE '%${esc(q)}%' ORDER BY COALESCE(chain_seq, document_id) DESC, finalized_at DESC"
    )

    return (rows ?: []).collect { r ->
        new SearchResult(
            "GoBD-Beleg ${r[1]} [${r[2]}]",
            "Festgeschrieben am ${r[3]} – Klick oeffnet Originalbeleg",
            {
                Path f = Paths.get(r[4].toString())
                if (Files.exists(f) && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(f.toFile())
                }
            }
        )
    }
})

// Initialisierung beim Starten des Plugins ausfuehren
initSchema()
notifyStatus("GoBD-Konformitaets-Plugin (v1.4.0) vollstaendig geladen.")

// Aufraeum-Closure beim Stoppen/Deaktivieren/Neuladen des Plugins
onStop = {
    creatingStorno = false
    finalizingIds.clear()
    lastGeneratedFiles.clear()
    masterDataBeforeSave.clear()
}
