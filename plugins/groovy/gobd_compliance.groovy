// @name GoBD-Konformitaet (Vollstaendiges MVP)
// @description Erzwingt GoBD-Unveraenderbarkeit (Festschreibung bei Export), erlaubten Offene-Posten-/Zahlungsstatus-Workflow, Loesch-/Papierkorbschutz, Stammdaten-Snapshots, SHA-256-Hash-Verkettung, Archiv-Integritaet und GDPdU-Export
// @version 1.5.0
// @author OpenYaBS Community
// @id groovy.gobd-compliance
// @minYabsVersion 2.0.9

import mpv5.YabsViewProxy
import mpv5.db.common.Context
import mpv5.db.common.DatabaseObject
import mpv5.db.common.DatabaseObjectModifier
import mpv5.db.common.NodataFoundException
import mpv5.db.common.QueryCriteria
import mpv5.db.common.QueryHandler
import mpv5.db.objects.Account
import mpv5.db.objects.Contact
import mpv5.db.objects.Expense
import mpv5.db.objects.Item
import mpv5.db.objects.Revenue
import mpv5.db.objects.SubItem
import mpv5.db.objects.User
import mpv5.handler.Activity
import mpv5.handler.ExpenseReceipts
import mpv5.logging.Log
import mpv5.plugin.api.SearchResult
import mpv5.ui.dialogs.Notificator
import mpv5.ui.panels.ChangeNotApprovedException
import mpv5.ui.panels.DataPanel
import mpv5.ui.panels.RevenuePanel
import mpv5.usermanagement.MPSecurityManager

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile
import org.w3c.dom.Document

import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JFileChooser
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTabbedPane
import mpv5.ui.modern.ModernTheme
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Desktop
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.ByteArrayInputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFilePermission
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Timer
import java.util.TimerTask
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
Timer incomeWatchTimer = null
final Set<String> seenWatchFiles = Collections.synchronizedSet(new HashSet<String>())

// Sichere SQL-Wrapper ueber QueryHandler.getConnection() (setzt Context.DEFAULT)
def execSql = { String sql ->
    QueryHandler.getConnection().freeQuery(sql.toString(), MPSecurityManager.VIEW, null)
}

def selectRows = { String sql ->
    def res = QueryHandler.getConnection().freeSelectQuery(sql.toString(), MPSecurityManager.VIEW, null)
    if (res == null) return ([] as Object[][])
    return res.getData() ?: ([] as Object[][])
}

// Benachrichtigungs-Helper ausschliesslich ueber mpv5.ui.dialogs.Notificator
def showError = { String msg ->
    Notificator.raiseNotification(msg, true)
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

// Aktualisiert den aktuell geoeffneten Tab, falls er den angegebenen Beleg anzeigt
def reloadCurrentTabIfShowing = { DatabaseObject doc ->
    if (doc == null || doc.__getIDS() <= 0) return
    Runnable updateUi = {
        try {
            DataPanel currentTab = YabsViewProxy.instance().getCurrentTab()
            if (currentTab != null && currentTab.getDataOwner() != null
                    && currentTab.getDataOwner().getClass() == doc.getClass()
                    && currentTab.getDataOwner().__getIDS() == doc.__getIDS()) {
                currentTab.setDataOwner(doc, true)
            }
        } catch (Exception ignored) { }
    }
    if (SwingUtilities.isEventDispatchThread()) {
        updateUi.run()
    } else {
        SwingUtilities.invokeLater(updateUi)
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

    [
        "ALTER TABLE gobd_document_lock ADD COLUMN IF NOT EXISTS chain_seq BIGINT NULL",
        "ALTER TABLE gobd_document_lock ADD COLUMN IF NOT EXISTS customer_snapshot TEXT NULL",
        "ALTER TABLE gobd_document_lock ADD COLUMN IF NOT EXISTS positions_snapshot TEXT NULL",
        "ALTER TABLE gobd_audit_log ADD COLUMN IF NOT EXISTS old_value VARCHAR(2000) NULL",
        "ALTER TABLE gobd_audit_log ADD COLUMN IF NOT EXISTS new_value VARCHAR(2000) NULL"
    ].each { String alterSql ->
        execSql(alterSql)
    }

    // Eingangsbelege zu Ausgaben: Die Dateien selbst liegen NICHT in der Datenbank (keine BLOBs),
    // sondern byte-identisch im Archivordner (<Archiv>/eingangsbelege/<Jahr>/) neben einer SHA256SUMS-Datei.
    // Die Tabelle haelt nur Metadaten, Datei-Hash und eine eigene SHA-256-Hash-Kette.
    execSql("""
        CREATE TABLE IF NOT EXISTS gobd_receipt (
            id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
            expense_id BIGINT NOT NULL,
            chain_seq BIGINT NOT NULL,
            receipt_ref VARCHAR(250) NULL,
            original_name VARCHAR(512) NOT NULL,
            archive_path VARCHAR(1024) NOT NULL,
            file_size BIGINT NOT NULL,
            artifact_format VARCHAR(30) NOT NULL,
            sha256 CHAR(64) NOT NULL,
            chain_hash CHAR(64) NOT NULL,
            prev_chain_hash CHAR(64) NOT NULL,
            expense_snapshot VARCHAR(2000) NULL,
            imported_at TIMESTAMP NOT NULL,
            imported_by VARCHAR(100) NOT NULL
        )
    """)

    // Einnahmen-Festschreibung & Belegarchiv (ZUGFeRD / XRechnung fuer Revenue):
    // Archiviert die Originaldatei unveraendert unter <Archiv>/einnahmen/<Jahr>/ inkl. SHA256SUMS und Hash-Kette.
    execSql("""
        CREATE TABLE IF NOT EXISTS gobd_income_lock (
            revenue_id BIGINT PRIMARY KEY,
            chain_seq BIGINT NOT NULL,
            document_number VARCHAR(100) NOT NULL,
            status VARCHAR(20) NOT NULL,
            imported_at TIMESTAMP NOT NULL,
            imported_by VARCHAR(100) NOT NULL,
            original_name VARCHAR(512) NOT NULL,
            archive_path VARCHAR(1024) NOT NULL,
            file_size BIGINT NOT NULL,
            artifact_format VARCHAR(30) NOT NULL,
            file_sha256 CHAR(64) NOT NULL UNIQUE,
            chain_hash CHAR(64) NOT NULL,
            prev_chain_hash CHAR(64) NOT NULL,
            financial_snapshot VARCHAR(2000) NOT NULL
        )
    """)
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
        return type == Item.TYPE_INVOICE
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
    if (doc == null || doc.__getInttype() != Item.TYPE_INVOICE) {
        return false
    }
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
        boolean numberAssigned = false
        if (!rawNumber || rawNumber.toUpperCase().startsWith("ENTWURF") || rawNumber.toUpperCase().startsWith("DRAFT")) {
            String prefix = "RE"
            String assignedNumber = nextSequenceNumber(prefix, year)
            doc.setCnumber(assignedNumber)
            numberAssigned = true
        }

        // Status auf 'abgeschlossen' setzen und Enddatum pruefen/setzen
        doc.setIntstatus(Item.STATUS_FINISHED)
        Date workedOn = doc.__getDatetodo() ?: doc.__getDateadded() ?: new Date()
        Date end = doc.__getDateend()
        if (end == null || (workedOn != null && end.before(workedOn))) {
            Date targetEnd = null
            try {
                Contact c = doc.getContact()
                int payterm = c != null ? c.__getPayterm() : 0
                if (payterm > 0) {
                    Date base = doc.__getDateadded() ?: workedOn
                    Date due = mpv5.utils.date.DateConverter.addDays(base, payterm)
                    if (due != null && !due.before(workedOn)) {
                        targetEnd = due
                    }
                }
            } catch (Exception ignored) { }
            if (targetEnd == null) {
                targetEnd = workedOn ?: new Date()
            }
            doc.setDateend(targetEnd)
        }
        doc.save(true)

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
                (${doc.__getIDS()}, ${chainSeq}, '${esc(doc.__getCnumber())}', 'FINALIZED', CURRENT_TIMESTAMP, '${esc(currentUser())}', '${esc(customerSnap)}', '${esc(positionsSnap)}', '${esc(targetFile.toString())}', '${esc(format)}', '${esc(currentHash)}', '${esc(prevHash)}', ${cancelsSql})
        """)

        reloadCurrentTabIfShowing(doc)

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
// 2b. ZUGFeRD PDF/A-3 & XRechnung XML Einnahmen-Importer & Belegarchiv (Revenue)
// -----------------------------------------------------------------------------
def computeRawSha256 = { byte[] fileBytes ->
    MessageDigest digest = MessageDigest.getInstance("SHA-256")
    digest.update(fileBytes)
    StringBuilder hex = new StringBuilder(64)
    for (byte b : digest.digest()) {
        hex.append(String.format("%02x", b))
    }
    return hex.toString()
}

def getIncomeLockRecord = { long revId ->
    def data = selectRows(
        "SELECT revenue_id, chain_seq, document_number, status, imported_at, imported_by, original_name, archive_path, file_size, artifact_format, file_sha256, chain_hash, prev_chain_hash, financial_snapshot FROM gobd_income_lock WHERE revenue_id = ${revId}"
    )
    return (data && data.length > 0) ? data[0] : null
}

def getLatestIncomeChainHash = {
    def data = selectRows(
        "SELECT chain_hash FROM gobd_income_lock ORDER BY chain_seq DESC, imported_at DESC, revenue_id DESC"
    )
    return (data && data.length > 0 && data[0][0]) ? data[0][0].toString() : GENESIS_HASH
}

def getNextIncomeChainSeq = {
    def data = selectRows("SELECT COALESCE(MAX(chain_seq), 0) FROM gobd_income_lock")
    long currentMax = (data && data.length > 0 && data[0][0] != null) ? Long.parseLong(data[0][0].toString()) : 0L
    return currentMax + 1L
}

def createRevenueFinancialSnapshot = { Revenue rev ->
    return [
        "Num=${rev.__getCnumber() ?: ''}",
        "Date=${fmtDate(rev.__getDateadded())}",
        "Acc=${rev.__getAccountsids()}",
        "Net=${fmtDec(rev.__getNetvalue())}",
        "TaxPct=${fmtDec(rev.__getTaxpercentvalue())}",
        "Gross=${fmtDec(rev.__getBrutvalue())}",
        "Desc=${(rev.__getDescription() ?: '').trim()}"
    ].join("|")
}

def createExpenseFinancialSnapshot = { Expense exp ->
    return [
        "Num=${exp.__getCnumber() ?: ''}",
        "Date=${fmtDate(exp.__getDateadded())}",
        "Acc=${exp.__getAccountsids()}",
        "Net=${fmtDec(exp.__getNetvalue())}",
        "TaxPct=${fmtDec(exp.__getTaxpercentvalue())}",
        "Gross=${fmtDec(exp.__getBrutvalue())}",
        "Desc=${(exp.__getDescription() ?: '').trim()}"
    ].join("|")
}

def defaultIncomeAccountId = {
    try {
        def rows = selectRows("SELECT ids FROM accounts WHERE intaccounttype = ${Account.INCOME} ORDER BY ids ASC")
        if (rows && rows.length > 0 && rows[0][0] != null) {
            return Integer.parseInt(rows[0][0].toString())
        }
    } catch (Exception ignored) { }
    return 1
}

def extractEmbeddedEinvoiceXml = { Path pdfPath ->
    PDDocument doc = null
    try {
        doc = PDDocument.load(pdfPath.toFile())
        def names = doc.getDocumentCatalog()?.getNames()
        def efTree = names?.getEmbeddedFiles()
        if (efTree == null) return null

        List<String> targetNames = ["factur-x.xml", "zugferd-invoice.xml", "xrechnung.xml"]
        Map<String, PDComplexFileSpecification> allFiles = [:]
        def collectFromNode
        collectFromNode = { node ->
            if (node == null) return
            try {
                Map<String, PDComplexFileSpecification> nodeMap = node.getNames()
                if (nodeMap != null) {
                    allFiles.putAll(nodeMap)
                }
            } catch (Exception ignored) { }
            try {
                def kids = node.getKids()
                if (kids != null) {
                    kids.each { child -> collectFromNode(child) }
                }
            } catch (Exception ignored) { }
        }
        collectFromNode(efTree)

        PDComplexFileSpecification chosen = null
        for (String preferred : targetNames) {
            def entry = allFiles.find { k, v ->
                String fn = (v?.getFileUnicode() ?: v?.getFile() ?: k ?: "").toLowerCase()
                return fn == preferred || fn.endsWith("/" + preferred)
            }
            if (entry != null) {
                chosen = entry.value
                break
            }
        }
        if (chosen == null) {
            def fallback = allFiles.find { k, v ->
                String fn = (v?.getFileUnicode() ?: v?.getFile() ?: k ?: "").toLowerCase()
                return fn.endsWith(".xml")
            }
            chosen = fallback?.value
        }
        if (chosen == null) return null
        PDEmbeddedFile ef = chosen.getEmbeddedFileUnicode() ?: chosen.getEmbeddedFile()
        return ef?.toByteArray()
    } catch (Exception ignored) {
        return null
    } finally {
        if (doc != null) {
            try { doc.close() } catch (Exception ignored) { }
        }
    }
}

def parseEinvoiceDate = { String raw ->
    if (raw == null) return null
    String s = raw.trim()
    if (s.isEmpty()) return null
    for (String pattern : ["yyyyMMdd", "yyyy-MM-dd", "dd.MM.yyyy"]) {
        try {
            SimpleDateFormat df = new SimpleDateFormat(pattern)
            df.setLenient(false)
            return df.parse(s.length() > 10 && pattern == "yyyy-MM-dd" ? s.substring(0, 10) : s)
        } catch (Exception ignored) { }
    }
    return null
}

def parseEinvoiceDecimal = { String raw ->
    if (raw == null) return null
    String s = raw.trim().replace(",", ".")
    if (s.isEmpty()) return null
    try {
        return new BigDecimal(s).setScale(2, RoundingMode.HALF_UP)
    } catch (Exception ignored) {
        return null
    }
}

def parseEinvoiceXml = { byte[] xmlBytes ->
    try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance()
        dbf.setNamespaceAware(false)
        try {
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        } catch (Exception ignored) { }
        Document dom = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xmlBytes))
        XPath xp = XPathFactory.newInstance().newXPath()

        def evalText = { List<String> exprs ->
            for (String expr : exprs) {
                try {
                    String val = (String) xp.evaluate(expr, dom, XPathConstants.STRING)
                    if (val != null && !val.trim().isEmpty()) {
                        return val.trim()
                    }
                } catch (Exception ignored) { }
            }
            return ""
        }

        // BT-1: Rechnungsnummer / Belegnummer
        String docNumber = evalText([
            "//*[local-name()='ExchangedDocument']/*[local-name()='ID']",
            "/*[local-name()='Invoice' or local-name()='CreditNote']/*[local-name()='ID']"
        ])

        // BT-2: Ausstellungsdatum
        String issueDateStr = evalText([
            "//*[local-name()='ExchangedDocument']/*[local-name()='IssueDateTime']/*[local-name()='DateTimeString']",
            "/*[local-name()='Invoice' or local-name()='CreditNote']/*[local-name()='IssueDate']"
        ])
        Date issueDate = parseEinvoiceDate(issueDateStr)

        // BT-9: Faelligkeitsdatum (optional)
        String dueDateStr = evalText([
            "//*[local-name()='SpecifiedTradePaymentTerms']/*[local-name()='DueDateDateTime']/*[local-name()='DateTimeString']",
            "/*[local-name()='Invoice' or local-name()='CreditNote']/*[local-name()='DueDate']",
            "//*[local-name()='PaymentMeans']/*[local-name()='PaymentDueDate']"
        ])
        Date dueDate = parseEinvoiceDate(dueDateStr) ?: issueDate

        // BT-27 / BT-44: Name des Partners (SellerTradeParty oder BuyerTradeParty)
        String sellerName = evalText([
            "//*[local-name()='SellerTradeParty']/*[local-name()='Name']",
            "//*[local-name()='AccountingSupplierParty']//*[local-name()='PartyName']/*[local-name()='Name']",
            "//*[local-name()='AccountingSupplierParty']//*[local-name()='PartyLegalEntity']/*[local-name()='RegistrationName']"
        ])
        String buyerName = evalText([
            "//*[local-name()='BuyerTradeParty']/*[local-name()='Name']",
            "//*[local-name()='AccountingCustomerParty']//*[local-name()='PartyName']/*[local-name()='Name']",
            "//*[local-name()='AccountingCustomerParty']//*[local-name()='PartyLegalEntity']/*[local-name()='RegistrationName']"
        ])
        String partnerName = sellerName ?: buyerName

        // BT-109: Nettobetrag (TaxBasisTotalAmount / TaxExclusiveAmount)
        String netStr = evalText([
            "//*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']/*[local-name()='TaxBasisTotalAmount']",
            "//*[local-name()='LegalMonetaryTotal']/*[local-name()='TaxExclusiveAmount']",
            "//*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']/*[local-name()='LineTotalAmount']"
        ])
        BigDecimal netAmount = parseEinvoiceDecimal(netStr)

        // BT-112: Bruttobetrag (GrandTotalAmount / TaxInclusiveAmount / PayableAmount)
        String grossStr = evalText([
            "//*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']/*[local-name()='GrandTotalAmount']",
            "//*[local-name()='LegalMonetaryTotal']/*[local-name()='TaxInclusiveAmount']",
            "//*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']/*[local-name()='DuePayableAmount']",
            "//*[local-name()='LegalMonetaryTotal']/*[local-name()='PayableAmount']"
        ])
        BigDecimal grossAmount = parseEinvoiceDecimal(grossStr)

        // BT-119: Steuersatz in Prozent
        String taxRateStr = evalText([
            "//*[local-name()='ApplicableTradeTax']/*[local-name()='RateApplicablePercent']",
            "//*[local-name()='TaxSubtotal']/*[local-name()='TaxCategory']/*[local-name()='Percent']",
            "//*[local-name()='ClassifiedTaxCategory']/*[local-name()='Percent']"
        ])
        BigDecimal taxPercent = parseEinvoiceDecimal(taxRateStr)
        if (taxPercent == null) {
            if (netAmount != null && grossAmount != null && netAmount.compareTo(BigDecimal.ZERO) > 0) {
                taxPercent = grossAmount.subtract(netAmount)
                    .multiply(new BigDecimal("100"))
                    .divide(netAmount, 2, RoundingMode.HALF_UP)
            } else {
                taxPercent = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
            }
        }

        List<String> errors = []
        if (!docNumber) errors << "Pflichtfeld BT-1 (Rechnungsnummer / ExchangedDocument/ID) fehlt"
        if (issueDate == null) errors << "Pflichtfeld BT-2 (Ausstellungsdatum / IssueDateTime) fehlt oder ungueltig"
        if (netAmount == null) errors << "Pflichtfeld BT-109 (Nettobetrag / TaxBasisTotalAmount) fehlt"
        if (grossAmount == null) errors << "Pflichtfeld BT-112 (Bruttobetrag / GrandTotalAmount) fehlt"

        return [
            valid        : errors.isEmpty(),
            errors       : errors,
            documentNumber: docNumber,
            issueDate    : issueDate,
            dueDate      : dueDate,
            partnerName  : partnerName,
            netAmount    : netAmount,
            grossAmount  : grossAmount,
            taxPercent   : taxPercent
        ]
    } catch (Exception ex) {
        return [
            valid : false,
            errors: ["XML-Struktur konnte nicht nach EN 16931 gelesen werden: ${ex.getMessage()}"]
        ]
    }
}

def importZugferdIncomeFile = { Path sourceFile, Revenue targetRevenue = null, boolean silent = false ->
    if (sourceFile == null || !Files.exists(sourceFile) || !Files.isRegularFile(sourceFile)) {
        if (!silent) showError("GoBD-Einnahmen-Import: Datei existiert nicht (${sourceFile}).")
        return null
    }

    byte[] fileBytes = Files.readAllBytes(sourceFile)
    String fileSha256 = computeRawSha256(fileBytes)

    // 1. Dublettenpruefung anhand SHA-256 (file_sha256)
    def existingHashRows = selectRows("SELECT revenue_id, document_number FROM gobd_income_lock WHERE file_sha256 = '${esc(fileSha256)}'")
    if (existingHashRows && existingHashRows.length > 0) {
        if (!silent) {
            showError("GoBD-Dublettenschutz:\nDie Datei '${sourceFile.getFileName()}' (SHA-256: ${fileSha256.substring(0, 12)}…) wurde bereits als Einnahme '${existingHashRows[0][1]}' (#${existingHashRows[0][0]}) importiert!")
        }
        return null
    }

    String origName = sourceFile.getFileName().toString()
    String lowerName = origName.toLowerCase()
    byte[] xmlBytes = null
    String artifactFormat = "RECEIPT_FILE"

    if (lowerName.endsWith(".pdf")) {
        xmlBytes = extractEmbeddedEinvoiceXml(sourceFile)
        if (xmlBytes != null) {
            artifactFormat = "ZUGFERD_PDFA3"
        }
    } else if (lowerName.endsWith(".xml")) {
        xmlBytes = fileBytes
        artifactFormat = "XRECHNUNG_XML"
    }

    Map parsed = null
    if (xmlBytes != null) {
        parsed = parseEinvoiceXml(xmlBytes)
        if (!parsed.valid) {
            if (!silent) {
                showError("GoBD-Einnahmen-Import abgelehnt (EN 16931 Validierungsfehler in '${origName}'):\n- " + parsed.errors.join("\n- "))
            }
            return null
        }
    } else if (targetRevenue == null) {
        if (!silent) {
            showError("GoBD-Einnahmen-Import abgelehnt:\nDatei '${origName}' enthaelt kein eingebettetes ZUGFeRD / Factur-X / XRechnung XML (factur-x.xml / zugferd-invoice.xml / xrechnung.xml).")
        }
        return null
    }

    String docNum = parsed != null ? parsed.documentNumber.toString() : (targetRevenue.__getCnumber() ?: origName)
    def existingNumRows = selectRows("SELECT revenue_id, file_sha256 FROM gobd_income_lock WHERE document_number = '${esc(docNum)}'")
    if (existingNumRows && existingNumRows.length > 0 && (targetRevenue == null || Long.parseLong(existingNumRows[0][0].toString()) != (long) targetRevenue.__getIDS())) {
        if (!silent) {
            showError("GoBD-Dublettenschutz:\nEine Einnahme mit der Belegnummer '${docNum}' ist bereits festgeschrieben (Einnahme #${existingNumRows[0][0]})!")
        }
        return null
    }

    Revenue rev = targetRevenue != null ? targetRevenue : new Revenue()
    Date issueDate = parsed != null ? (Date) parsed.issueDate : (rev.__getDateadded() ?: new Date())
    Date dueDate = parsed != null ? (Date) (parsed.dueDate ?: parsed.issueDate) : (rev.__getDateend() ?: issueDate)

    if (parsed != null) {
        String partner = parsed.partnerName?.toString()?.trim() ?: ""
        String desc = partner ? "${partner} - E-Rechnung ${docNum} (${origName})" : "E-Rechnung ${docNum} (${origName})"
        rev.setCnumber(docNum)
        rev.setCname(docNum)
        rev.setDescription(desc)
        rev.setNetvalue((BigDecimal) parsed.netAmount)
        rev.setTaxpercentvalue((BigDecimal) parsed.taxPercent)
        rev.setBrutvalue((BigDecimal) parsed.grossAmount)
        rev.setDateadded(issueDate)
        rev.setDateend(dueDate)
        if (rev.__getAccountsids() <= 0) {
            rev.setAccountsids(defaultIncomeAccountId())
        }
        if (rev.__getGroupsids() <= 0) {
            rev.setGroupsids(1)
        }
        if (targetRevenue == null) {
            rev.setStatus(Item.STATUS_QUEUED)
            rev.setIspaid(false)
        }
    }

    if (!rev.save(true) || rev.__getIDS() <= 0) {
        if (!silent) showError("GoBD-Einnahmen-Import: Einnahme '${docNum}' konnte nicht gespeichert werden.")
        return null
    }

    // Sicherstellen, dass Ausstellungsdatum (BT-2), Faelligkeitsdatum (BT-9) und Belegnummer (BT-1) exakt persistiert sind
    if (parsed != null) {
        rev.setCnumber(docNum)
        rev.setCname(docNum)
        rev.setDateadded(issueDate)
        rev.setDateend(dueDate)
        rev.save(true)
    }

    int year = issueDate.toInstant().atZone(ZoneId.systemDefault()).getYear()
    Path yearDir = getArchiveRoot().resolve("einnahmen").resolve(String.valueOf(year))
    Files.createDirectories(yearDir)

    String ext = origName.contains(".") ? origName.substring(origName.lastIndexOf(".") + 1).toLowerCase() : "pdf"
    String safeBase = docNum.replaceAll("[^a-zA-Z0-9._-]", "_")
    Path archivedFile = resolveUniqueArchivePath(yearDir, safeBase, ext)
    Files.copy(sourceFile, archivedFile, StandardCopyOption.REPLACE_EXISTING)
    makeReadOnly(archivedFile)

    // SHA256SUMS im Jahresordner aktualisieren
    Path sumsFile = yearDir.resolve("SHA256SUMS")
    String sumsLine = "${fileSha256}  ${archivedFile.getFileName()}\n"
    Files.write(
        sumsFile,
        sumsLine.getBytes(StandardCharsets.UTF_8),
        Files.exists(sumsFile) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE
    )

    long chainSeq = getNextIncomeChainSeq()
    String prevChainHash = getLatestIncomeChainHash()
    String chainHash = computeChainedSha256(fileBytes, prevChainHash)
    String finSnap = createRevenueFinancialSnapshot(rev)

    execSql("""
        INSERT INTO gobd_income_lock
            (revenue_id, chain_seq, document_number, status, imported_at, imported_by, original_name, archive_path, file_size, artifact_format, file_sha256, chain_hash, prev_chain_hash, financial_snapshot)
        VALUES
            (${rev.__getIDS()}, ${chainSeq}, '${esc(docNum)}', 'LOCKED', CURRENT_TIMESTAMP, '${esc(currentUser())}', '${esc(origName)}', '${esc(archivedFile.toString())}', ${fileBytes.length}, '${esc(artifactFormat)}', '${esc(fileSha256)}', '${esc(chainHash)}', '${esc(prevChainHash)}', '${esc(finSnap)}')
    """)

    logAudit("REVENUE", rev.__getIDS(), "INCOME_IMPORTED", null, finSnap, fileSha256, "Einnahme ${docNum} (${artifactFormat}) revisionssicher importiert und festgeschrieben.")
    reloadCurrentTabIfShowing(rev)
    try {
        RevenuePanel.instanceOf().refresh()
        RevenuePanel.instanceOf().setDataOwner(rev, true)
    } catch (Exception ignored) { }
    try {
        mpv5.ui.panels.ModernRevenuePanel.instanceOf().refresh()
        mpv5.ui.panels.ModernRevenuePanel.instanceOf().setDataOwner(rev, true)
    } catch (Exception ignored) { }

    if (!silent) {
        String notifMsg = (parsed == null) ?
            "GoBD-Einnahmen-Beleg archiviert:\nBeleg: ${docNum} (${artifactFormat})\nNetto: ${fmtDec(rev.__getNetvalue())} EUR | Brutto: ${fmtDec(rev.__getBrutvalue())} EUR\nSHA-256: ${fileSha256.substring(0, 16)}...\nArchiv: ${archivedFile.getFileName()}\n(Hinweis: Einfacher PDF-Beleg - Werte koennen nicht automatisch auf rechnerische Richtigkeit geprueft werden.)" :
            "GoBD-Einnahmen-Import erfolgreich:\nBeleg: ${docNum} (${artifactFormat})\nNetto: ${fmtDec(rev.__getNetvalue())} EUR | Brutto: ${fmtDec(rev.__getBrutvalue())} EUR\nSHA-256: ${fileSha256.substring(0, 16)}...\nArchiv: ${archivedFile.getFileName()}"
        notifyDocumentEvent(notifMsg, rev)
    } else {
        notifyStatus("GoBD Auto-Import: Einnahme ${docNum} (${origName}) importiert und festgeschrieben.")
    }
    return rev
}

// -----------------------------------------------------------------------------
// 3. Datensatz-Waechter ueber yabs.addRecordModifier(...)
// -----------------------------------------------------------------------------
def checkWriteAllowed = { DatabaseObject obj, boolean isDelete ->
    if (obj instanceof Item && isTaxRelevantDocument(obj) && obj.__getIDS() > 0) {
        Item doc = (Item) obj
        def lock = getLockRecord(doc.__getIDS())
        if (lock != null) {
            if (isDelete || isMovedToTrash(doc)) {
                logAudit("INVOICE", doc.__getIDS(), "BLOCKED_DELETE", lock[5]?.toString(), "Delete/Trash=true", lock[9]?.toString(), "Loesch- oder Papierkorb-Versuch an festgeschriebenem Beleg ${lock[1]}.")
                showError("GoBD-Loeschschutz (§§ 146, 147 AO):\nBeleg ${lock[1]} ist festgeschrieben und darf weder geloescht noch in den Papierkorb verschoben werden!\nBitte erstellen Sie stattdessen eine Stornorechnung.")
                return false
            }

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

                    if (newStatus == Item.STATUS_CANCELLED && oldStatus != Item.STATUS_CANCELLED && !creatingStorno) {
                        logAudit("INVOICE", doc.__getIDS(), "BLOCKED_MANUAL_CANCEL", "intstatus=${oldStatus}", "intstatus=${newStatus}", lock[9]?.toString(), "Manuelles Stornieren ohne Stornorechnung verboten.")
                        showError("GoBD-Schutz: Ein festgeschriebener Beleg darf nicht einfach auf 'Storniert' gesetzt werden.\nBitte nutzen Sie 'Plugins > GoBD: 2. Beleg stornieren (Stornorechnung)'.")
                        return false
                    }

                    if (oldStatus != newStatus) {
                        String oldText = Item.getStatusString(oldStatus)
                        String newText = Item.getStatusString(newStatus)
                        logAudit("INVOICE", doc.__getIDS(), "PAYMENT_STATUS_CHANGED", "status=${oldStatus}(${oldText})", "status=${newStatus}(${newText})", lock[9]?.toString(), "Zahlungsstatus fuer Beleg ${lock[1]} protokolliert.")
                        notifyStatus("GoBD-Audit: Status fuer Beleg ${lock[1]} protokolliert (${oldText} -> ${newText}).")
                    }
                    if (oldReminders != newReminders) {
                        logAudit("INVOICE", doc.__getIDS(), "DUNNING_CHANGED", "intreminders=${oldReminders}", "intreminders=${newReminders}", lock[9]?.toString(), "Mahnstufe fuer Beleg ${lock[1]} protokolliert.")
                        notifyStatus("GoBD-Audit: Mahnstufe fuer Beleg ${lock[1]} protokolliert (${oldReminders} -> ${newReminders}).")
                    }
                    return true
                }
            }

            logAudit("INVOICE", doc.__getIDS(), "BLOCKED_EDIT", lock[5]?.toString(), createFinancialCoreSnapshot(doc), lock[9]?.toString(), "Unzulaessige Aenderung steuerrelevanter Felder an Beleg ${lock[1]}.")
            showError("GoBD-Schutz (§ 146 Abs. 4 AO):\nBeleg ${lock[1]} ist festgeschrieben! Betraege, Steuern, Datum, Positionen und Empfaenger duerfen nicht mehr geaendert werden.\n(Hinweis: Nur Zahlungsstatus-/Mahnstufen-Aenderungen werden protokolliert.)")
            return false
        }
    }

    if (obj instanceof SubItem) {
        SubItem sub = (SubItem) obj
        long parentId = sub.__getItemsids()
        if (parentId > 0) {
            def lock = getLockRecord(parentId)
            if (lock != null) {
                logAudit("SUBITEM", sub.__getIDS(), isDelete ? "BLOCKED_DELETE" : "BLOCKED_EDIT", lock[6]?.toString(), sub.__getDescription(), lock[9]?.toString(), "Positionsaenderung an festgeschriebenem Beleg ${lock[1]} verhindert.")
                showError("GoBD-Schutz: Position gehoert zu festgeschriebenem Beleg ${lock[1]} und ist gesperrt.")
                return false
            }
        }
    }

    if (obj instanceof Revenue && obj.__getIDS() > 0) {
        Revenue rev = (Revenue) obj
        def incLock = getIncomeLockRecord(rev.__getIDS())
        def receiptRows = selectRows("SELECT ids, originalname, archivepath, sha256 FROM revenue_receipts WHERE revenuesids = ${rev.__getIDS()}")
        boolean hasReceipts = receiptRows && receiptRows.length > 0
        if (incLock != null || hasReceipts) {
            String lockHash = incLock ? incLock[10]?.toString() : receiptRows[0][3]?.toString()
            String docNum = incLock ? incLock[2]?.toString() : (rev.__getCnumber() ?: "#" + rev.__getIDS())
            if (isDelete || isMovedToTrash(rev)) {
                logAudit("REVENUE", rev.__getIDS(), "BLOCKED_DELETE", incLock ? incLock[13]?.toString() : "Receipts=${receiptRows.length}", "Delete/Trash=true", lockHash, "Loesch- oder Papierkorb-Versuch an GoBD-gesperrter Einnahme ${docNum}.")
                showError("GoBD-Loeschschutz (§§ 146, 147 AO):\nEinnahme ${docNum} ist durch einen archivierten Originalbeleg festgeschrieben und darf weder geloescht noch in den Papierkorb verschoben werden!")
                return false
            }

            def dbRows = selectRows(
                "SELECT cnumber, dateadded, accountsids, netvalue, taxpercentvalue, brutvalue, description, status, dateend, ispaid FROM revenues WHERE ids = ${rev.__getIDS()}"
            )
            if (dbRows && dbRows.length > 0) {
                def db = dbRows[0]
                String dbCore = [
                    "Num=${db[0] ?: ''}",
                    "Date=${fmtDate(db[1])}",
                    "Acc=${db[2]}",
                    "Net=${fmtDec(db[3])}",
                    "TaxPct=${fmtDec(db[4])}",
                    "Gross=${fmtDec(db[5])}",
                    "Desc=${(db[6]?.toString() ?: '').trim()}"
                ].join("|")
                String newCore = createRevenueFinancialSnapshot(rev)

                if (dbCore == newCore) {
                    int oldStatus = db[7] != null ? Integer.parseInt(db[7].toString()) : 0
                    int newStatus = rev.__getStatus()
                    String oldEnd = fmtDate(db[8])
                    String newEnd = fmtDate(rev.__getDateend())
                    boolean oldPaid = db[9] != null && (db[9].toString() == "1" || db[9].toString().equalsIgnoreCase("true"))
                    boolean newPaid = rev.__getIspaid()
                    if (oldStatus != newStatus || oldEnd != newEnd || oldPaid != newPaid) {
                        logAudit("REVENUE", rev.__getIDS(), "INCOME_PAYMENT_STATUS_CHANGED", "status=${oldStatus}|ispaid=${oldPaid}|dateend=${oldEnd}", "status=${newStatus}|ispaid=${newPaid}|dateend=${newEnd}", lockHash, "Zahlungsstatus/Zahlungsdatum fuer Einnahme ${docNum} protokolliert.")
                        notifyStatus("GoBD-Audit: Zahlungsstatus fuer Einnahme ${docNum} protokolliert.")
                    }
                    return true
                }
            }

            logAudit("REVENUE", rev.__getIDS(), "BLOCKED_EDIT", incLock ? incLock[13]?.toString() : "Receipts=${receiptRows.length}", createRevenueFinancialSnapshot(rev), lockHash, "Unzulaessige Aenderung steuerrelevanter Felder an Einnahme ${docNum}.")
            showError("GoBD-Schutz (§ 146 Abs. 4 AO):\nEinnahme ${docNum} ist festgeschrieben! Betraege, Steuersatz, Leistungsdatum, Konto, Nummer und Beschreibung duerfen nicht mehr geaendert werden.\n(Hinweis: Nur Zahlungsstatus und Zahlungsdatum bleiben aenderbar.)")
            return false
        }
    }

    if (obj instanceof Expense && obj.__getIDS() > 0) {
        Expense exp = (Expense) obj
        def receiptRows = selectRows("SELECT ids, originalname, archivepath, sha256 FROM expense_receipts WHERE expensesids = ${exp.__getIDS()}")
        boolean hasReceipts = receiptRows && receiptRows.length > 0
        if (hasReceipts) {
            String firstReceiptHash = receiptRows[0][3]?.toString()
            if (isDelete || isMovedToTrash(exp)) {
                logAudit("EXPENSE", exp.__getIDS(), "BLOCKED_DELETE", "Receipts=${receiptRows.length}", "Delete/Trash=true", firstReceiptHash, "Loeschung einer Ausgabe mit archivierten Belegen verhindert.")
                showError("GoBD-Schutz (Rz. 61-64 AO):\nAusgabe #${exp.__getIDS()} besitzt ${receiptRows.length} archivierte Eingangsbelege und darf nicht geloescht werden!")
                return false
            }

            def dbRows = selectRows(
                "SELECT cnumber, dateadded, accountsids, netvalue, taxpercentvalue, brutvalue, description, ispaid, dateend FROM expenses WHERE ids = ${exp.__getIDS()}"
            )
            if (dbRows && dbRows.length > 0) {
                def db = dbRows[0]
                String dbCore = [
                    "Num=${db[0] ?: ''}",
                    "Date=${fmtDate(db[1])}",
                    "Acc=${db[2]}",
                    "Net=${fmtDec(db[3])}",
                    "TaxPct=${fmtDec(db[4])}",
                    "Gross=${fmtDec(db[5])}",
                    "Desc=${(db[6]?.toString() ?: '').trim()}"
                ].join("|")
                String newCore = createExpenseFinancialSnapshot(exp)

                if (dbCore == newCore) {
                    boolean oldPaid = db[7] != null && (db[7].toString() == "1" || db[7].toString().equalsIgnoreCase("true"))
                    boolean newPaid = exp.__getIspaid()
                    String oldEnd = fmtDate(db[8])
                    String newEnd = fmtDate(exp.__getDateend())
                    if (oldPaid != newPaid || oldEnd != newEnd) {
                        logAudit("EXPENSE", exp.__getIDS(), "EXPENSE_PAYMENT_STATUS_CHANGED", "ispaid=${oldPaid}|dateend=${oldEnd}", "ispaid=${newPaid}|dateend=${newEnd}", firstReceiptHash, "Zahlungsstatus/Zahlungsdatum fuer Ausgabe ${exp.__getCnumber() ?: ('#' + exp.__getIDS())} protokolliert.")
                        notifyStatus("GoBD-Audit: Zahlungsstatus fuer Ausgabe ${exp.__getCnumber() ?: ('#' + exp.__getIDS())} protokolliert.")
                    }
                    return true
                }

                logAudit("EXPENSE", exp.__getIDS(), "BLOCKED_EDIT", dbCore, newCore, firstReceiptHash, "Unzulaessige Aenderung steuerrelevanter Felder an Ausgabe ${exp.__getCnumber() ?: ('#' + exp.__getIDS())}.")
                showError("GoBD-Schutz (§ 146 Abs. 4 AO):\nAusgabe ${exp.__getCnumber() ?: ('#' + exp.__getIDS())} ist mit ${receiptRows.length} archivierten Originalbeleg(en) verknuepft und festgeschrieben!\nBetraege, Steuersatz, Belegdatum, Konto, Nummer und Beschreibung duerfen nicht mehr geaendert werden.\n(Hinweis: Nur Zahlungsstatus und Zahlungsdatum bleiben aenderbar.)")
                return false
            }
        }
    }

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
//    - Automatische Festschreibung beim PDF-/E-Rechnungs-Export (inkl. Tab-Fallback)
// -----------------------------------------------------------------------------
yabs.onActivity({ Activity.Entry entry ->
    Log.Debug(this, "GoBD onActivity: kind=${entry.kind}, id=${entry.id}, context=${entry.context?.getDbIdentity()}, file=${entry.file}, detail=${entry.detail}, text=${entry.text}")
    if (entry.file == null || entry.kind?.name() == "DELETED") return

    String fileName = entry.file.getName().toLowerCase()
    if (!(fileName.endsWith(".pdf") || fileName.endsWith(".xml"))) return

    String entryCtx = entry.context?.getDbIdentity()?.toLowerCase() ?: ""
    if (entryCtx.contains("order") || entryCtx.contains("offer") || entryCtx.contains("confirmation") || entryCtx.contains("delivery")) {
        return
    }

    Item doc = null
    String invCtx = Context.getInvoice()?.getDbIdentity()?.toLowerCase() ?: "invoice"

    if (entry.id > 0 && entryCtx == invCtx) {
        try {
            DatabaseObject dbo = DatabaseObject.getObject(entry.context ?: Context.getInvoice(), (int) entry.id)
            if (dbo instanceof Item) {
                Item item = (Item) dbo
                if (item.__getInttype() == Item.TYPE_INVOICE) {
                    doc = item
                    lastGeneratedFiles.put((long) doc.__getIDS(), entry.file.toPath())
                }
            }
        } catch (Exception ignored) { }
    } else if (entry.id <= 0) {
        try {
            DataPanel currentTab = YabsViewProxy.instance().getCurrentTab()
            if (currentTab != null && currentTab.getDataOwner() instanceof Item) {
                Item tabItem = (Item) currentTab.getDataOwner()
                if (tabItem.__getIDS() > 0 && tabItem.__getInttype() == Item.TYPE_INVOICE) {
                    doc = tabItem
                    lastGeneratedFiles.put((long) doc.__getIDS(), entry.file.toPath())
                }
            }
        } catch (Exception ignored) { }
    }

    if (doc == null || doc.__getIDS() <= 0 || doc.__getInttype() != Item.TYPE_INVOICE) return
    if (creatingStorno || finalizingIds.contains((int) doc.__getIDS())) return

    if (getLockRecord(doc.__getIDS()) == null) {
        boolean autoLock = yabs.settings().getBoolean("gobd.autoLockOnExport", true)
        if (autoLock) {
            try {
                finalizeDocument(doc, null)
            } catch (Exception ex) {
                showError("GoBD-Auto-Festschreibung fehlgeschlagen:\n${ex.getMessage()}")
            }
        } else {
            notifyStatus("Belegdatei erstellt (${entry.file.getName()}). Jetzt ueber 'Plugins > GoBD: 1. Beleg festschreiben' sichern.")
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
    <Table>
      <URL>ausgaben.csv</URL>
      <Name>Ausgabenjournal</Name>
      <Description>Ausgaben und Betriebsausgaben nach GoBD Rz. 61-64</Description>
      <UTF8/>
      <DecimalSymbol>.</DecimalSymbol>
      <DigitGroupingSymbol>,</DigitGroupingSymbol>
      <VariableLength>
        <ColumnDelimiter>;</ColumnDelimiter>
        <RecordDelimiter>&#x0A;</RecordDelimiter>
        <TextEncapsulator>"</TextEncapsulator>
        <VariablePrimaryKey><Name>expense_id</Name><Numeric><Accuracy>0</Accuracy></Numeric></VariablePrimaryKey>
        <VariableColumn><Name>cnumber</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>description</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>date_added</Name><Date><Format>YYYY-MM-DD</Format></Date></VariableColumn>
        <VariableColumn><Name>date_paid</Name><Date><Format>YYYY-MM-DD</Format></Date></VariableColumn>
        <VariableColumn><Name>is_paid</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>account_id</Name><Numeric><Accuracy>0</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>net_amount</Name><Numeric><Accuracy>2</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>tax_percent</Name><Numeric><Accuracy>2</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>gross_amount</Name><Numeric><Accuracy>2</Accuracy></Numeric></VariableColumn>
      </VariableLength>
    </Table>
    <Table>
      <URL>eingangsbelege.csv</URL>
      <Name>Eingangsbelege</Name>
      <Description>Eingangsbelege und Scans zu Ausgaben inkl. SHA-256 Pruefsummen</Description>
      <UTF8/>
      <DecimalSymbol>.</DecimalSymbol>
      <DigitGroupingSymbol>,</DigitGroupingSymbol>
      <VariableLength>
        <ColumnDelimiter>;</ColumnDelimiter>
        <RecordDelimiter>&#x0A;</RecordDelimiter>
        <TextEncapsulator>"</TextEncapsulator>
        <VariablePrimaryKey><Name>receipt_id</Name><Numeric><Accuracy>0</Accuracy></Numeric></VariablePrimaryKey>
        <VariableColumn><Name>expense_id</Name><Numeric><Accuracy>0</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>original_name</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>file_size</Name><Numeric><Accuracy>0</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>sha256</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>mime_type</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>imported_at</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>imported_by</Name><AlphaNumeric/></VariableColumn>
        <ForeignKey>
          <Name>expense_id</Name>
          <References>Ausgabenjournal</References>
        </ForeignKey>
      </VariableLength>
    </Table>
    <Table>
      <URL>einnahmen.csv</URL>
      <Name>Einnahmenjournal</Name>
      <Description>Sonstige Einnahmen und gutgeschriebene ZUGFeRD-/XRechnung-Abrechnungen inkl. SHA-256-Hashkette</Description>
      <UTF8/>
      <DecimalSymbol>.</DecimalSymbol>
      <DigitGroupingSymbol>,</DigitGroupingSymbol>
      <VariableLength>
        <ColumnDelimiter>;</ColumnDelimiter>
        <RecordDelimiter>&#x0A;</RecordDelimiter>
        <TextEncapsulator>"</TextEncapsulator>
        <VariablePrimaryKey><Name>revenue_id</Name><Numeric><Accuracy>0</Accuracy></Numeric></VariablePrimaryKey>
        <VariableColumn><Name>cnumber</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>description</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>date_added</Name><Date><Format>YYYY-MM-DD</Format></Date></VariableColumn>
        <VariableColumn><Name>date_paid</Name><Date><Format>YYYY-MM-DD</Format></Date></VariableColumn>
        <VariableColumn><Name>is_paid</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>account_id</Name><Numeric><Accuracy>0</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>net_amount</Name><Numeric><Accuracy>2</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>tax_percent</Name><Numeric><Accuracy>2</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>gross_amount</Name><Numeric><Accuracy>2</Accuracy></Numeric></VariableColumn>
        <VariableColumn><Name>artifact_format</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>file_sha256</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>chain_hash</Name><AlphaNumeric/></VariableColumn>
        <VariableColumn><Name>prev_chain_hash</Name><AlphaNumeric/></VariableColumn>
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
        ? "SELECT i.ids, i.cnumber, (COALESCE(i.netvalue, 0) - COALESCE(i.discountvalue, 0) + COALESCE(i.taxvalue, 0)) FROM items i INNER JOIN gobd_document_lock g ON i.ids = g.document_id WHERE g.status = 'FINALIZED' AND i.inttype = ${Item.TYPE_INVOICE}"
        : "SELECT i.ids, i.cnumber, (COALESCE(i.netvalue, 0) - COALESCE(i.discountvalue, 0) + COALESCE(i.taxvalue, 0)) FROM items i LEFT JOIN gobd_document_lock g ON i.ids = g.document_id WHERE g.document_id IS NULL AND i.inttype = ${Item.TYPE_INVOICE} ORDER BY i.ids DESC"
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

def stornoDocument = { Item original ->
    if (original == null) return false

    def origLock = getLockRecord(original.__getIDS())
    if (origLock == null || origLock[2].toString() == "CANCELLED") {
        showError("Dieser Beleg wurde bereits storniert oder ist nicht festgeschrieben.")
        return false
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
            return false
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
            try {
                original.setIntstatus(Item.STATUS_CANCELLED)
                original.save(true)
            } catch (Exception ignored) { }
            reloadCurrentTabIfShowing(original)
            logAudit("INVOICE", original.__getIDS(), "CANCELLED", "STATUS=FINALIZED", "STATUS=CANCELLED BY ${storno.__getCnumber()}", origLock[9]?.toString(), "Storniert durch Stornobeleg ${storno.__getCnumber()}")
            notifyDocumentEvent("Beleg ${original.__getCnumber()} storniert durch ${storno.__getCnumber()}.", original)
            return true
        }
        return false
    } finally {
        creatingStorno = false
    }
}

yabs.addCommand("GoBD: 1. Beleg festschreiben", "Schreibt eine Rechnung unveraenderbar nach GoBD fest (inkl. Stammdaten-Snapshot & SHA-256 Hash-Kette)", {
    Item doc = selectInvoiceDialog("GoBD-Festschreibung (§ 146 Abs. 4 AO)", false)
    if (doc != null) {
        finalizeDocument(doc, null)
    }
})

yabs.addCommand("GoBD: 2. Beleg stornieren (Stornorechnung)", "Erstellt eine festgeschriebene Stornorechnung fuer einen gesperrten Beleg", {
    Item original = selectInvoiceDialog("GoBD-Stornorechnung erstellen", true)
    if (original != null) {
        stornoDocument(original)
    }
})

yabs.addCommand("GoBD: 3. Archiv-Integritaet pruefen", "Prueft alle archivierten Belege und die gesamte SHA-256-Hash-Kette", {
    yabs.background({
        def rows = selectRows(
            "SELECT document_id, document_number, archive_path, sha256_hash, prev_sha256_hash FROM gobd_document_lock ORDER BY COALESCE(chain_seq, document_id) ASC, finalized_at ASC, document_id ASC"
        )
        def incomeRows = selectRows(
            "SELECT revenue_id, document_number, archive_path, file_sha256, chain_hash, prev_chain_hash FROM gobd_income_lock ORDER BY chain_seq ASC, imported_at ASC, revenue_id ASC"
        )

        if ((!rows || rows.length == 0) && (!incomeRows || incomeRows.length == 0)) {
            return "Keine festgeschriebenen Belege oder Einnahmen im GoBD-Archiv vorhanden."
        }

        String expectedPrev = GENESIS_HASH
        int verifiedCount = 0

        for (def row : (rows ?: [])) {
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

        String expectedIncomePrev = GENESIS_HASH
        int verifiedIncomeCount = 0
        for (def iRow : (incomeRows ?: [])) {
            String incNum = iRow[1].toString()
            Path incFile = Paths.get(iRow[2].toString())
            String expectedFileSha = iRow[3].toString()
            String storedChainHash = iRow[4].toString()
            String storedPrevChain = iRow[5].toString()

            if (storedPrevChain != expectedIncomePrev) {
                return "FEHLER: Kettenbruch in Einnahmen-Kette vor Einnahme ${incNum}! Erwartet: ${expectedIncomePrev}, Gefunden: ${storedPrevChain}"
            }
            if (!Files.exists(incFile)) {
                return "FEHLER: Archivdatei fehlt fuer Einnahme ${incNum} (${incFile})!"
            }
            byte[] incBytes = Files.readAllBytes(incFile)
            String actualFileSha = computeRawSha256(incBytes)
            if (!actualFileSha.equalsIgnoreCase(expectedFileSha)) {
                return "FEHLER: Datei-Manipulation erkannt bei Einnahme ${incNum}! Datei-SHA-256 weicht ab."
            }
            String actualChainHash = computeChainedSha256(incBytes, storedPrevChain)
            if (!actualChainHash.equalsIgnoreCase(storedChainHash)) {
                return "FEHLER: Ketten-Hash-Abweichung bei Einnahme ${incNum}!"
            }
            expectedIncomePrev = storedChainHash
            verifiedIncomeCount++
        }

        return "OK: Alle ${verifiedCount} Ausgangsbelege und ${verifiedIncomeCount} Einnahmenbelege sowie deren SHA-256-Hash-Ketten sind lueckenlos und unversehrt."
    }, { String report ->
        if (report.startsWith("FEHLER")) {
            showError(report)
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

            // Ausgaben (Expenses) des Pruefungsjahres
            def allExpenses = selectRows("SELECT ids, cnumber, description, dateadded, dateend, ispaid, accountsids, netvalue, taxpercentvalue, brutvalue FROM expenses ORDER BY ids ASC")
            def expenses = allExpenses.findAll { e -> e[3]?.toString()?.startsWith(String.valueOf(exportYear)) }
            zos.putNextEntry(new ZipEntry("ausgaben.csv"))
            StringBuilder sbExp = new StringBuilder("expense_id;cnumber;description;date_added;date_paid;is_paid;account_id;net_amount;tax_percent;gross_amount\n")
            expenses.each { e ->
                sbExp.append("${e[0]};\"${csvCell(e[1])}\";\"${csvCell(e[2])}\";${fmtDate(e[3])};${fmtDate(e[4])};${e[5] != null && e[5].toString() == '1' ? 'JA' : 'NEIN'};${e[6] ?: 0};${fmtDec(e[7])};${fmtDec(e[8])};${fmtDec(e[9])}\n")
            }
            zos.write(sbExp.toString().getBytes(StandardCharsets.UTF_8))
            zos.closeEntry()

            // Eingangsbelege zu Ausgaben
            def allReceipts = selectRows("SELECT ids, expensesids, originalname, archivepath, filesize, sha256, mimetype, imported_at, imported_by FROM expense_receipts ORDER BY ids ASC")
            def receipts = allReceipts.findAll { r -> r[7]?.toString()?.startsWith(String.valueOf(exportYear)) }
            zos.putNextEntry(new ZipEntry("eingangsbelege.csv"))
            StringBuilder sbRec = new StringBuilder("receipt_id;expense_id;original_name;file_size;sha256;mime_type;imported_at;imported_by\n")
            receipts.each { r ->
                sbRec.append("${r[0]};${r[1]};\"${csvCell(r[2])}\";${r[4]};\"${csvCell(r[5])}\";\"${csvCell(r[6])}\";\"${csvCell(r[7])}\";\"${csvCell(r[8])}\"\n")
            }
            zos.write(sbRec.toString().getBytes(StandardCharsets.UTF_8))
            zos.closeEntry()

            // Einnahmen (Revenues + gobd_income_lock) des Pruefungsjahres
            def allRevenues = selectRows(
                "SELECT r.ids, r.cnumber, r.description, r.dateadded, r.dateend, r.status, r.accountsids, r.netvalue, r.taxpercentvalue, r.brutvalue, l.artifact_format, l.file_sha256, l.chain_hash, l.prev_chain_hash, l.archive_path FROM revenues r LEFT JOIN gobd_income_lock l ON r.ids = l.revenue_id ORDER BY r.ids ASC"
            )
            def revenues = allRevenues.findAll { rv -> rv[3]?.toString()?.startsWith(String.valueOf(exportYear)) }
            zos.putNextEntry(new ZipEntry("einnahmen.csv"))
            StringBuilder sbRev = new StringBuilder("revenue_id;cnumber;description;date_added;date_paid;is_paid;account_id;net_amount;tax_percent;gross_amount;artifact_format;file_sha256;chain_hash;prev_chain_hash\n")
            revenues.each { rv ->
                boolean paid = rv[5] != null && rv[5].toString() == String.valueOf(Item.STATUS_PAID)
                sbRev.append("${rv[0]};\"${csvCell(rv[1])}\";\"${csvCell(rv[2])}\";${fmtDate(rv[3])};${fmtDate(rv[4])};${paid ? 'JA' : 'NEIN'};${rv[6] ?: 0};${fmtDec(rv[7])};${fmtDec(rv[8])};${fmtDec(rv[9])};\"${csvCell(rv[10])}\";\"${csvCell(rv[11])}\";\"${csvCell(rv[12])}\";\"${csvCell(rv[13])}\"\n")
            }
            zos.write(sbRev.toString().getBytes(StandardCharsets.UTF_8))
            zos.closeEntry()

            // Ausgangsbelege
            locks.each { r ->
                Path belegFile = Paths.get(r[7].toString())
                if (Files.exists(belegFile)) {
                    zos.putNextEntry(new ZipEntry("belege/${belegFile.getFileName()}"))
                    zos.write(Files.readAllBytes(belegFile))
                    zos.closeEntry()
                }
            }

            // Eingangsbelege (Originale)
            receipts.each { r ->
                Path recFile = Paths.get(r[3].toString())
                if (Files.exists(recFile)) {
                    zos.putNextEntry(new ZipEntry("belege/eingangsbelege/${recFile.getFileName()}"))
                    zos.write(Files.readAllBytes(recFile))
                    zos.closeEntry()
                }
            }

            // Einnahmenbelege (Originale aus gobd_income_lock)
            revenues.each { rv ->
                if (rv[14] != null && !rv[14].toString().trim().isEmpty()) {
                    Path incFile = Paths.get(rv[14].toString())
                    if (Files.exists(incFile)) {
                        zos.putNextEntry(new ZipEntry("belege/einnahmen/${incFile.getFileName()}"))
                        zos.write(Files.readAllBytes(incFile))
                        zos.closeEntry()
                    }
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

yabs.addCommand("GoBD: 5. Pruefer-Ansicht oeffnen (Z1 Read-Only Tab)", "Oeffnet die schreibgeschuetzte 4-Tab GoBD-Z1-Prueferansicht (§ 147 Abs. 6 AO)", {
    JPanel rootPanel = new JPanel(new BorderLayout(8, 8))
    rootPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))

    JLabel summaryLabel = new JLabel("GoBD Z1-Prueferansicht (§ 147 Abs. 6 AO – Unmittelbarer schreibgeschuetzter Datenzugriff)")
    summaryLabel.setFont(ModernTheme.font(1, Font.BOLD))

    JComboBox<String> yearCombo = new JComboBox<String>(["Alle Jahre"] as String[])
    JButton refreshBtn = ModernTheme.actionButton(new JButton("Aktualisieren"), null)
    JButton openSelectedBtn = ModernTheme.actionButton(new JButton("Markierten Originalbeleg oeffnen"), null)

    JPanel topBar = new JPanel(new BorderLayout(12, 0))
    topBar.add(summaryLabel, BorderLayout.WEST)

    JPanel topControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0))
    topControls.add(new JLabel("Pruefungszeitraum:"))
    topControls.add(yearCombo)
    topControls.add(openSelectedBtn)
    topControls.add(refreshBtn)
    topBar.add(topControls, BorderLayout.EAST)

    String[] invoiceCols = ["ID", "Belegnummer", "Status", "Festgeschrieben am", "Benutzer", "Format", "SHA-256 Hash", "Storno-Bezug", "Archivpfad"] as String[]
    String[] incomeCols = ["Einnahme-ID", "Belegnummer", "Status", "Importiert am", "Benutzer", "Originaldatei", "Format", "Datei-SHA-256", "Ketten-Hash", "Archivpfad"] as String[]
    String[] expenseCols = ["Ausgabe-ID", "Belegnummer", "Beschreibung", "Datum", "Brutto (EUR)", "Belegstatus", "Originaldatei", "SHA-256", "Archivpfad"] as String[]
    String[] auditCols = ["ID", "Zeitstempel", "Benutzer", "Objekttyp", "Objekt-ID", "Aktion", "Alter Wert", "Neuer Wert", "Details"] as String[]

    DefaultTableModel invoiceModel = new DefaultTableModel(invoiceCols, 0) {
        @Override
        boolean isCellEditable(int row, int column) { return false }
    }
    DefaultTableModel incomeModel = new DefaultTableModel(incomeCols, 0) {
        @Override
        boolean isCellEditable(int row, int column) { return false }
    }
    DefaultTableModel expenseModel = new DefaultTableModel(expenseCols, 0) {
        @Override
        boolean isCellEditable(int row, int column) { return false }
    }
    DefaultTableModel auditModel = new DefaultTableModel(auditCols, 0) {
        @Override
        boolean isCellEditable(int row, int column) { return false }
    }

    def createReadOnlyTable = { DefaultTableModel model, int archiveColIdx ->
        JTable tbl = new JTable(model) {
            @Override
            boolean isCellEditable(int row, int column) { return false }

            @Override
            String getToolTipText(MouseEvent e) {
                int vRow = rowAtPoint(e.getPoint())
                if (vRow >= 0 && archiveColIdx >= 0) {
                    int mRow = convertRowIndexToModel(vRow)
                    Object p = getModel().getValueAt(mRow, archiveColIdx)
                    if (p != null && !p.toString().trim().isEmpty()) {
                        return "Doppelklick oeffnet Originalbeleg: ${p}"
                    }
                }
                return super.getToolTipText(e)
            }
        }
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        tbl.setAutoCreateRowSorter(true)
        if (archiveColIdx >= 0 && archiveColIdx < tbl.getColumnModel().getColumnCount()) {
            def col = tbl.getColumnModel().getColumn(archiveColIdx)
            col.setMinWidth(0)
            col.setMaxWidth(0)
            col.setPreferredWidth(0)
        }
        return tbl
    }

    JTable invoiceTable = createReadOnlyTable(invoiceModel, 8)
    JTable incomeTable = createReadOnlyTable(incomeModel, 9)
    JTable expenseTable = createReadOnlyTable(expenseModel, 8)
    JTable auditTable = createReadOnlyTable(auditModel, -1)

    // Hervorhebung fehlender Eingangsbelege nach GoBD Rz. 61-64 ("Keine Buchung ohne Beleg")
    expenseTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        @Override
        Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column)
            if (!isSelected) {
                int mRow = tbl.convertRowIndexToModel(row)
                String statusVal = tbl.getModel().getValueAt(mRow, 5)?.toString() ?: ""
                if (statusVal.contains("Fehlt")) {
                    c.setBackground(new Color(254, 243, 199))
                    c.setForeground(new Color(146, 64, 14))
                } else {
                    c.setBackground(new Color(220, 252, 231))
                    c.setForeground(new Color(22, 101, 52))
                }
            }
            return c
        }
    })

    def openArchiveFromTable = { JTable tbl, int archiveColIdx ->
        if (tbl == null || archiveColIdx < 0) return
        int vRow = tbl.getSelectedRow()
        if (vRow < 0) {
            notifyPopup("Bitte markieren Sie zuerst eine Zeile mit verknuepftem Originalbeleg.")
            return
        }
        int mRow = tbl.convertRowIndexToModel(vRow)
        String archivePath = tbl.getModel().getValueAt(mRow, archiveColIdx)?.toString()?.trim() ?: ""
        if (archivePath.isEmpty()) {
            showError("GoBD-Pruefung (Rz. 61): Zu diesem Datensatz ist kein archivierter Originalbeleg hinterlegt!")
            return
        }
        Path f = Paths.get(archivePath)
        if (!Files.exists(f)) {
            showError("Archivierte Originaldatei nicht gefunden:\n${archivePath}")
            return
        }
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(f.toFile())
            } catch (Exception ex) {
                notifyPopup("Archivierte Belegdatei:\n${f.toAbsolutePath()}")
            }
        } else {
            notifyPopup("Archivierte Belegdatei:\n${f.toAbsolutePath()}")
        }
    }

    [
        [table: invoiceTable, col: 8],
        [table: incomeTable,  col: 9],
        [table: expenseTable, col: 8]
    ].each { Map entry ->
        JTable t = (JTable) entry.table
        int colIdx = (int) entry.col
        t.addMouseListener(new MouseAdapter() {
            @Override
            void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && t.getSelectedRow() >= 0) {
                    openArchiveFromTable(t, colIdx)
                }
            }
        })
    }

    JTabbedPane tabs = new JTabbedPane()
    tabs.addTab("1. Ausgangsrechnungen & Stornos", new JScrollPane(invoiceTable))
    tabs.addTab("2. Importierte Einnahmen (ZUGFeRD)", new JScrollPane(incomeTable))
    tabs.addTab("3. Ausgaben & Eingangsbelege", new JScrollPane(expenseTable))
    tabs.addTab("4. GoBD Audit-Protokoll", new JScrollPane(auditTable))

    tabs.addChangeListener({
        openSelectedBtn.setEnabled(tabs.getSelectedIndex() in [0, 1, 2])
    })

    openSelectedBtn.addActionListener({
        int idx = tabs.getSelectedIndex()
        if (idx == 0) openArchiveFromTable(invoiceTable, 8)
        else if (idx == 1) openArchiveFromTable(incomeTable, 9)
        else if (idx == 2) openArchiveFromTable(expenseTable, 8)
    })

    boolean updatingYears = false
    def reloadZ1Data = {
        String selectedYear = (yearCombo.getSelectedItem()?.toString() ?: "Alle Jahre").trim()
        boolean filterByYear = (selectedYear != "Alle Jahre" && !selectedYear.isEmpty())

        def invRows = [] as Object[][]
        try {
            invRows = selectRows(
                "SELECT document_id, document_number, status, finalized_at, finalized_by, artifact_format, sha256_hash, cancels_document_id, archive_path FROM gobd_document_lock ORDER BY COALESCE(chain_seq, document_id) DESC, finalized_at DESC"
            )
        } catch (Exception ignored) { }

        def incRows = [] as Object[][]
        try {
            incRows = selectRows(
                "SELECT revenue_id, document_number, status, imported_at, imported_by, original_name, artifact_format, file_sha256, chain_hash, archive_path FROM gobd_income_lock ORDER BY chain_seq DESC, imported_at DESC"
            )
        } catch (Exception ignored) { }

        def expRows = [] as Object[][]
        try {
            expRows = selectRows(
                "SELECT e.ids, e.cnumber, e.description, e.dateadded, e.brutvalue, r.originalname, r.sha256, r.archivepath FROM expenses e LEFT JOIN expense_receipts r ON e.ids = r.expensesids WHERE COALESCE(e.invisible, 0) = 0 ORDER BY e.ids DESC"
            )
        } catch (Exception ignored) { }

        def audRows = [] as Object[][]
        try {
            audRows = selectRows(
                "SELECT id, created_at, user_id, entity_type, entity_id, action, old_value, new_value, details FROM gobd_audit_log ORDER BY id DESC"
            )
        } catch (Exception ignored) { }

        Set<String> years = new TreeSet<String>(Collections.reverseOrder())
        def collectYear = { Object tsObj ->
            String s = fmtDate(tsObj)
            if (s.length() >= 4 && s.substring(0, 4).isInteger()) {
                years.add(s.substring(0, 4))
            }
        }
        invRows?.each { collectYear(it[3]) }
        incRows?.each { collectYear(it[3]) }
        expRows?.each { collectYear(it[3]) }
        audRows?.each { collectYear(it[1]) }

        updatingYears = true
        try {
            yearCombo.removeAllItems()
            yearCombo.addItem("Alle Jahre")
            years.each { String y -> yearCombo.addItem(y) }
            if (filterByYear && years.contains(selectedYear)) {
                yearCombo.setSelectedItem(selectedYear)
            } else {
                yearCombo.setSelectedItem("Alle Jahre")
                filterByYear = false
            }
        } finally {
            updatingYears = false
        }

        invoiceModel.setRowCount(0)
        (invRows ?: []).findAll { !filterByYear || fmtDate(it[3]).startsWith(selectedYear) }.each { r ->
            invoiceModel.addRow([r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7] ?: "", r[8] ?: ""] as Object[])
        }

        incomeModel.setRowCount(0)
        (incRows ?: []).findAll { !filterByYear || fmtDate(it[3]).startsWith(selectedYear) }.each { r ->
            incomeModel.addRow([r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8], r[9] ?: ""] as Object[])
        }

        expenseModel.setRowCount(0)
        int missingReceipts = 0
        (expRows ?: []).findAll { !filterByYear || fmtDate(it[3]).startsWith(selectedYear) }.each { r ->
            String origName = r[5]?.toString()?.trim() ?: ""
            String sha = r[6]?.toString()?.trim() ?: ""
            String archPath = r[7]?.toString()?.trim() ?: ""
            boolean hasReceipt = !origName.isEmpty() && !archPath.isEmpty()
            if (!hasReceipt) missingReceipts++
            String receiptStatus = hasReceipt ? "🔒 Archiviert" : "⚠️ Fehlt (Rz. 61)"
            expenseModel.addRow([
                r[0],
                r[1] ?: "",
                r[2] ?: "",
                fmtDate(r[3]),
                fmtDec(r[4]),
                receiptStatus,
                origName,
                sha,
                archPath
            ] as Object[])
        }

        auditModel.setRowCount(0)
        (audRows ?: []).findAll { !filterByYear || fmtDate(it[1]).startsWith(selectedYear) }.each { r ->
            auditModel.addRow([r[0], r[1], r[2], r[3], r[4], r[5], r[6] ?: "", r[7] ?: "", r[8] ?: ""] as Object[])
        }

        tabs.setTitleAt(0, "1. Ausgangsrechnungen & Stornos (${invoiceModel.getRowCount()})")
        tabs.setTitleAt(1, "2. Importierte Einnahmen / ZUGFeRD (${incomeModel.getRowCount()})")
        tabs.setTitleAt(2, "3. Ausgaben & Eingangsbelege (${expenseModel.getRowCount()}${missingReceipts > 0 ? ' | ⚠️ ' + missingReceipts + ' ohne Beleg' : ''})")
        tabs.setTitleAt(3, "4. GoBD Audit-Protokoll (${auditModel.getRowCount()})")

        summaryLabel.setText(
            "GoBD Z1-Prueferansicht (§ 147 Abs. 6 AO) – ${invoiceModel.getRowCount()} Ausgangsbelege | " +
            "${incomeModel.getRowCount()} ZUGFeRD-Einnahmen | ${expenseModel.getRowCount()} Ausgaben | ${auditModel.getRowCount()} Audit-Eintraege"
        )
    }

    yearCombo.addActionListener({
        if (!updatingYears) {
            reloadZ1Data()
        }
    })
    refreshBtn.addActionListener({
        reloadZ1Data()
    })

    reloadZ1Data()
    rootPanel.add(topBar, BorderLayout.NORTH)
    rootPanel.add(tabs, BorderLayout.CENTER)
    yabs.openTab("GoBD Z1-Prueferjournal", rootPanel)
})

yabs.addCommand("GoBD: 6. Eingangsbelege pruefen (SHA256SUMS)", "Prueft alle archivierten Eingangsbelege und Einnahmenbelege gegen die Datenbank und die SHA256SUMS-Dateien", {
    yabs.background({
        def receiptRows = selectRows("SELECT ids, expensesids, originalname, archivepath, filesize, sha256 FROM expense_receipts ORDER BY ids ASC")
        def incomeRows = selectRows("SELECT revenue_id, document_number, original_name, archive_path, file_size, file_sha256 FROM gobd_income_lock ORDER BY chain_seq ASC")
        if ((!receiptRows || receiptRows.length == 0) && (!incomeRows || incomeRows.length == 0)) {
            return "Keine archivierten Eingangsbelege oder Einnahmenbelege in der Datenbank erfasst."
        }
        int verified = 0
        List<String> errors = []
        for (def row : (receiptRows ?: [])) {
            long rId = Long.parseLong(row[0].toString())
            long expId = Long.parseLong(row[1].toString())
            String origName = row[2].toString()
            Path f = Paths.get(row[3].toString())
            String expectedSha = row[5].toString()

            if (!Files.exists(f)) {
                errors << "Beleg #${rId} (${origName}, Ausgabe #${expId}): Datei fehlt im Archiv (${f})!"
                continue
            }
            byte[] bytes = Files.readAllBytes(f)
            String actualSha = computeRawSha256(bytes)
            if (!actualSha.equalsIgnoreCase(expectedSha)) {
                errors << "Beleg #${rId} (${origName}, Ausgabe #${expId}): SHA-256 stimmt NICHT ueberein!"
                continue
            }

            Path parentDir = f.getParent()
            Path sumsFile = parentDir.resolve("SHA256SUMS")
            if (Files.exists(sumsFile)) {
                String content = new String(Files.readAllBytes(sumsFile), StandardCharsets.UTF_8)
                if (!content.contains(expectedSha)) {
                    errors << "Beleg #${rId} (${origName}): SHA-256 fehlt in ${sumsFile.getFileName()}!"
                    continue
                }
            }
            verified++
        }

        int verifiedIncome = 0
        for (def iRow : (incomeRows ?: [])) {
            long revId = Long.parseLong(iRow[0].toString())
            String docNum = iRow[1].toString()
            String origName = iRow[2].toString()
            Path f = Paths.get(iRow[3].toString())
            String expectedSha = iRow[5].toString()

            if (!Files.exists(f)) {
                errors << "Einnahme ${docNum} (#${revId}, ${origName}): Datei fehlt im Archiv (${f})!"
                continue
            }
            byte[] bytes = Files.readAllBytes(f)
            String actualSha = computeRawSha256(bytes)
            if (!actualSha.equalsIgnoreCase(expectedSha)) {
                errors << "Einnahme ${docNum} (#${revId}, ${origName}): SHA-256 stimmt NICHT ueberein!"
                continue
            }

            Path parentDir = f.getParent()
            Path sumsFile = parentDir.resolve("SHA256SUMS")
            if (!Files.exists(sumsFile)) {
                errors << "Einnahme ${docNum} (#${revId}): SHA256SUMS fehlt in ${parentDir}!"
                continue
            }
            String content = new String(Files.readAllBytes(sumsFile), StandardCharsets.UTF_8)
            if (!content.contains(expectedSha)) {
                errors << "Einnahme ${docNum} (#${revId}): SHA-256 fehlt in ${sumsFile}!"
                continue
            }
            verifiedIncome++
        }

        if (errors.isEmpty()) {
            return "OK: Alle ${verified} Ausgaben-Belege und ${verifiedIncome} Einnahmen-Belege sind vollstaendig vorhanden und gegen SHA256SUMS geprueft."
        } else {
            return "FEHLER: Integritaetsprobleme bei Belegen:\n- " + errors.join("\n- ")
        }
    }, { String report ->
        if (report.startsWith("FEHLER")) {
            showError(report)
        } else {
            notifyPopup(report)
        }
    })
})

yabs.addCommand("GoBD: 7. ZUGFeRD / XRechnung als Einnahme importieren", "Importiert ZUGFeRD-PDF/A-3 oder XRechnung-XML als GoBD-festgeschriebene Einnahme (Revenue)", {
    JFileChooser chooser = new JFileChooser()
    String watchDir = yabs.settings().get("gobd.incomeWatchPath", "")
    if (watchDir && Files.exists(Paths.get(watchDir))) {
        chooser.setCurrentDirectory(Paths.get(watchDir).toFile())
    }
    chooser.setDialogTitle("ZUGFeRD PDF/A-3 oder XRechnung XML als Einnahme importieren")
    chooser.setMultiSelectionEnabled(true)
    chooser.setFileFilter(new FileNameExtensionFilter("E-Rechnungen (ZUGFeRD PDF/A-3, XRechnung XML)", "pdf", "xml"))
    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        File[] selected = chooser.getSelectedFiles()
        if ((selected == null || selected.length == 0) && chooser.getSelectedFile() != null) {
            selected = [chooser.getSelectedFile()] as File[]
        }
        selected?.each { File f ->
            importZugferdIncomeFile(f.toPath(), null, false)
        }
    }
})

// Optionaler Hintergrund-Watcher fuer automatischen ZUGFeRD-/XRechnung-Einnahmen-Import
def configureIncomeWatchTimer = {
    if (incomeWatchTimer != null) {
        try { incomeWatchTimer.cancel() } catch (Exception ignored) { }
        incomeWatchTimer = null
    }
    boolean enabled = yabs.settings().getBoolean("gobd.autoImportIncome", false)
    String watchPathStr = yabs.settings().get("gobd.incomeWatchPath", "").trim()
    if (!enabled || watchPathStr.isEmpty()) return

    Path watchDir = Paths.get(watchPathStr)
    if (!Files.isDirectory(watchDir)) return

    incomeWatchTimer = new Timer("gobd-income-watch", true)
    incomeWatchTimer.scheduleAtFixedRate(new TimerTask() {
        @Override
        void run() {
            try {
                if (!Files.isDirectory(watchDir)) return
                Files.list(watchDir).withCloseable { stream ->
                    stream.filter { Path p ->
                        String fn = p.getFileName().toString().toLowerCase()
                        Files.isRegularFile(p) && (fn.endsWith(".pdf") || fn.endsWith(".xml"))
                    }.forEach { Path p ->
                        String key = p.toAbsolutePath().toString() + ":" + Files.size(p)
                        if (seenWatchFiles.add(key)) {
                            importZugferdIncomeFile(p, null, true)
                        }
                    }
                }
            } catch (Exception ignored) { }
        }
    }, 5000L, 15000L)
}

// -----------------------------------------------------------------------------
// 7. Einstellungsseite unter Settings > Plugins (yabs.addSettingsPage)
// -----------------------------------------------------------------------------
yabs.addSettingsPage("GoBD-Konformitaet", "Einstellungen fuer automatische Beleg-Festschreibung, GoBD-Archivpfad und ZUGFeRD-Einnahmen-Import", {
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

    // ZUGFeRD / XRechnung Einnahmen-Ueberwachungsordner (gobd.incomeWatchPath & gobd.autoImportIncome)
    boolean currentAutoIncome = yabs.settings().getBoolean("gobd.autoImportIncome", false)
    JCheckBox autoIncomeCheck = new JCheckBox("ZUGFeRD / XRechnung Einnahmen-Belege aus Ueberwachungsordner automatisch importieren", currentAutoIncome)
    autoIncomeCheck.setAlignmentX(Component.LEFT_ALIGNMENT)
    autoIncomeCheck.setBorder(BorderFactory.createEmptyBorder(14, 0, 4, 0))
    autoIncomeCheck.addActionListener({
        yabs.settings().setBoolean("gobd.autoImportIncome", autoIncomeCheck.isSelected())
        configureIncomeWatchTimer()
        notifyStatus("GoBD Einnahmen-Auto-Import Einstellung gespeichert.")
    })

    JLabel watchLabel = new JLabel("Ueberwachungsordner fuer ZUGFeRD / XRechnung Einnahmen (gobd.incomeWatchPath):")
    watchLabel.setAlignmentX(Component.LEFT_ALIGNMENT)
    watchLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0))

    JPanel watchRowPanel = new JPanel(new BorderLayout(6, 0))
    watchRowPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
    JTextField watchPathField = new JTextField(yabs.settings().get("gobd.incomeWatchPath", ""), 30)
    watchRowPanel.add(watchPathField, BorderLayout.CENTER)

    JPanel watchBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0))
    JButton browseWatchBtn = new JButton("...")
    JButton saveWatchBtn = new JButton("Speichern")

    def saveIncomeWatchPath = { String newWatchStr ->
        try {
            String trimmed = newWatchStr?.trim() ?: ""
            if (!trimmed.isEmpty()) {
                Path wp = Paths.get(trimmed).toAbsolutePath()
                Files.createDirectories(wp)
                yabs.settings().set("gobd.incomeWatchPath", wp.toString())
                watchPathField.setText(wp.toString())
            } else {
                yabs.settings().set("gobd.incomeWatchPath", "")
                watchPathField.setText("")
            }
            configureIncomeWatchTimer()
            notifyStatus("GoBD Einnahmen-Ueberwachungsordner gespeichert.")
        } catch (Exception ex) {
            showError("Ungueltiger Ordnerpfad fuer Einnahmen-Import:\n${ex.getMessage()}")
        }
    }

    browseWatchBtn.addActionListener({
        JFileChooser chooser = new JFileChooser(watchPathField.getText() ?: getArchiveRoot().toString())
        chooser.setDialogTitle("Ueberwachungsordner fuer Einnahmen auswaehlen")
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
        chooser.setAcceptAllFileFilterUsed(false)
        if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            saveIncomeWatchPath(chooser.getSelectedFile().getAbsolutePath())
        }
    })
    saveWatchBtn.addActionListener({
        saveIncomeWatchPath(watchPathField.getText())
    })
    watchBtnPanel.add(browseWatchBtn)
    watchBtnPanel.add(saveWatchBtn)
    watchRowPanel.add(watchBtnPanel, BorderLayout.EAST)
    Dimension watchPref = watchRowPanel.getPreferredSize()
    watchRowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) watchPref.getHeight()))

    panel.add(autoLockCheck)
    panel.add(pathLabel)
    panel.add(rowPanel)
    panel.add(autoIncomeCheck)
    panel.add(watchLabel)
    panel.add(watchRowPanel)
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
    def incomeRows = selectRows(
        "SELECT revenue_id, document_number, status, imported_at, archive_path FROM gobd_income_lock WHERE LOWER(document_number) LIKE '%${esc(q)}%' ORDER BY chain_seq DESC, imported_at DESC"
    )

    List<SearchResult> results = []
    (rows ?: []).each { r ->
        results << new SearchResult(
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
    (incomeRows ?: []).each { ir ->
        results << new SearchResult(
            "GoBD-Einnahme ${ir[1]} [${ir[2]}]",
            "Importiert am ${ir[3]} – Klick oeffnet Originalbeleg",
            {
                Path f = Paths.get(ir[4].toString())
                if (Files.exists(f) && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(f.toFile())
                }
            }
        )
    }
    return results
})

// -----------------------------------------------------------------------------
// 9. Visuelle Sperranzeige & Feldsperre in den Panels (yabs.onView)
// -----------------------------------------------------------------------------
yabs.onView({ DataPanel panel ->
    if (panel == null) return
    DatabaseObject owner = panel.getDataOwner()

    // -------------------------------------------------------------------------
    // A. Eingangsbelege & Ausgaben (GoBD Rz. 61–64: Keine Buchung ohne Beleg)
    // -------------------------------------------------------------------------
    if (owner instanceof Expense) {
        Expense exp = (Expense) owner
        if (exp.__getIDS() <= 0) {
            panel.setContentLocked(false)
            panel.setPluginBadge(null)
            panel.setPluginBanner(null)
            return
        }

        def receipts = selectRows("SELECT ids, originalname, archivepath, sha256, filesize FROM expense_receipts WHERE expensesids = ${exp.__getIDS()}")
        boolean hasReceipts = receipts && receipts.length > 0

        if (hasReceipts) {
            String firstArchivePath = receipts[0][2]?.toString() ?: ""
            String firstSha = receipts[0][3]?.toString() ?: ""
            String shortSha = firstSha.length() >= 12 ? firstSha.substring(0, 12) : firstSha

            // Ausgabe mit revisionssicher archiviertem Beleg: Finanzfelder & Belegentfernung sperren
            panel.setContentLocked(true)

            JLabel badge = new JLabel("🔒 GoBD BELEGABLAGE (${receipts.length})")
            badge.setOpaque(true)
            badge.setBackground(new Color(220, 252, 231))
            badge.setForeground(new Color(22, 101, 52))
            badge.setFont(ModernTheme.font(0, Font.BOLD))
            badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(74, 222, 128), 1, true),
                BorderFactory.createEmptyBorder(ModernTheme.px(2), ModernTheme.px(8), ModernTheme.px(2), ModernTheme.px(8))
            ))
            badge.setToolTipText("GoBD Rz. 61–64 & § 146 Abs. 4 AO | ${receipts.length} Beleg(e) revisionssicher archiviert | SHA-256: ${firstSha}")
            panel.setPluginBadge(badge)

            JPanel banner = new JPanel(new BorderLayout(ModernTheme.px(12), 0))
            banner.setBackground(new Color(220, 252, 231))
            banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(74, 222, 128)),
                ModernTheme.padding(5, 12)
            ))
            JLabel info = new JLabel(
                "🔒 GoBD-Festschreibung: Ausgabe besitzt ${receipts.length} verknuepfte(n) Originalbeleg(e) im Jahresarchiv (SHA-256: ${shortSha}…). " +
                "Betraege, Steuer, Konto & Belegentfernung sind gesperrt; Zahlungsstatus und Zahlungsdatum bleiben buchbar."
            )
            info.setFont(ModernTheme.font(1, Font.BOLD))
            info.setForeground(new Color(22, 101, 52))
            banner.add(info, BorderLayout.CENTER)

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0))
            actions.setOpaque(false)
            JButton openBtn = ModernTheme.actionButton(new JButton("Originalbeleg oeffnen"), null)
            openBtn.setToolTipText(firstArchivePath)
            openBtn.addActionListener({
                Path f = Paths.get(firstArchivePath)
                if (Files.exists(f)) {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().open(f.toFile())
                        } catch (Exception ex) {
                            notifyPopup("Archivierte Belegdatei:\n${f.toAbsolutePath()}")
                        }
                    } else {
                        notifyPopup("Archivierte Belegdatei:\n${f.toAbsolutePath()}")
                    }
                } else {
                    showError("Archivdatei nicht gefunden:\n${firstArchivePath}")
                }
            })
            actions.add(openBtn)
            banner.add(actions, BorderLayout.EAST)
            panel.setPluginBanner(banner)
        } else {
            // Ausgabe OHNE Beleg: Felder offen lassen, Warnung nach GoBD Rz. 61 anzeigen
            panel.setContentLocked(false)

            JLabel badge = new JLabel("⚠️ GoBD: OHNE BELEG")
            badge.setOpaque(true)
            badge.setBackground(new Color(254, 243, 199))
            badge.setForeground(new Color(146, 64, 14))
            badge.setFont(ModernTheme.font(0, Font.BOLD))
            badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(245, 158, 11), 1, true),
                BorderFactory.createEmptyBorder(ModernTheme.px(2), ModernTheme.px(8), ModernTheme.px(2), ModernTheme.px(8))
            ))
            badge.setToolTipText("GoBD Rz. 61–64: Keine Buchung ohne Beleg! Bitte scannen oder fuegen Sie den Eingangsbeleg an.")
            panel.setPluginBadge(badge)

            JPanel banner = new JPanel(new BorderLayout(ModernTheme.px(12), 0))
            banner.setBackground(new Color(254, 243, 199))
            banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(245, 158, 11)),
                ModernTheme.padding(5, 12)
            ))
            JLabel info = new JLabel("⚠️ GoBD-Hinweis: Zu dieser Ausgabe ist noch kein Eingangsbeleg hinterlegt (Grundsatz: „Keine Buchung ohne Beleg“, GoBD Rz. 61–64).")
            info.setFont(ModernTheme.font(1, Font.PLAIN))
            info.setForeground(new Color(146, 64, 14))
            banner.add(info, BorderLayout.CENTER)

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0))
            actions.setOpaque(false)
            JButton attachBtn = ModernTheme.actionButton(new JButton("Beleg verknuepfen & festschreiben"), null)
            attachBtn.addActionListener({
                JFileChooser chooser = new JFileChooser()
                chooser.setDialogTitle("Eingangsbeleg / Scan fuer Ausgabe auswaehlen (PDF, XML, Bild)")
                chooser.setFileFilter(new FileNameExtensionFilter("Belegdateien (PDF, XML, PNG, JPG)", "pdf", "xml", "png", "jpg", "jpeg"))
                if (chooser.showOpenDialog(panel instanceof Component ? (Component) panel : null) == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
                    try {
                        File selFile = chooser.getSelectedFile()
                        def rec = ExpenseReceipts.attach(exp, selFile)
                        if (rec != null) {
                            boolean isPlainPdf = selFile.getName().toLowerCase().endsWith(".pdf") && ZugpferdExpenseImport.parse(selFile) == null
                            String auditMsg = isPlainPdf ?
                                "Eingangsbeleg ${rec.originalName} (einfaches PDF - rechnerische Richtigkeit der Belegwerte nicht automatisiert geprueft) verknuepft und Ausgabe festgeschrieben." :
                                "Eingangsbeleg ${rec.originalName} verknuepft und Ausgabe festgeschrieben."
                            logAudit("EXPENSE", exp.__getIDS(), "EXPENSE_RECEIPT_ATTACHED", null, createExpenseFinancialSnapshot(exp), rec.sha256, auditMsg)
                            panel.setDataOwner(exp, true)
                            String notif = isPlainPdf ?
                                "GoBD-Eingangsbeleg archiviert & Ausgabe festgeschrieben:\nDatei: ${rec.originalName}\nSHA-256: ${rec.sha256.substring(0, 16)}...\n(Hinweis: Einfacher PDF-Beleg - Werte koennen nicht automatisch auf rechnerische Richtigkeit geprueft werden.)" :
                                "GoBD-Eingangsbeleg archiviert & Ausgabe festgeschrieben:\nDatei: ${rec.originalName}\nSHA-256: ${rec.sha256.substring(0, 16)}..."
                            notifyDocumentEvent(notif, exp)
                        }
                    } catch (Exception ex) {
                        showError("Fehler beim Archivieren des Eingangsbelegs:\n${ex.getMessage()}")
                    }
                }
            })
            actions.add(attachBtn)
            banner.add(actions, BorderLayout.EAST)
            panel.setPluginBanner(banner)
        }
        return
    }

    // -------------------------------------------------------------------------
    // A2. Einnahmen / Gutschriften (Revenue) - GoBD Rz. 61–64 & § 146 Abs. 4 AO
    // -------------------------------------------------------------------------
    if (owner instanceof Revenue) {
        Revenue rev = (Revenue) owner
        if (rev.__getIDS() <= 0) {
            panel.setContentLocked(false)
            panel.setPluginBadge(null)
            panel.setPluginBanner(null)
            return
        }

        def incLock = getIncomeLockRecord(rev.__getIDS())
        def receipts = selectRows("SELECT ids, originalname, archivepath, sha256, filesize FROM revenue_receipts WHERE revenuesids = ${rev.__getIDS()}")
        boolean hasReceipts = (receipts && receipts.length > 0)
        if (incLock != null || hasReceipts) {
            String docNum = incLock ? (incLock[2]?.toString() ?: rev.__getCnumber()) : (rev.__getCnumber() ?: "#" + rev.__getIDS())
            String archivePath = incLock ? (incLock[7]?.toString() ?: "") : (receipts ? receipts[0][2]?.toString() : "")
            String format = incLock ? (incLock[9]?.toString() ?: "ZUGFERD_PDFA3") : "RECEIPT_FILE"
            String fileSha = incLock ? (incLock[10]?.toString() ?: "") : (receipts ? receipts[0][3]?.toString() : "")
            String shortSha = fileSha.length() >= 12 ? fileSha.substring(0, 12) : fileSha

            panel.setContentLocked(true)

            JLabel badge = new JLabel(incLock ? "🔒 GoBD E-RECHNUNG" : "🔒 GoBD BELEG-ARCHIV")
            badge.setOpaque(true)
            badge.setBackground(new Color(220, 252, 231))
            badge.setForeground(new Color(22, 101, 52))
            badge.setFont(ModernTheme.font(0, Font.BOLD))
            badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(74, 222, 128), 1, true),
                BorderFactory.createEmptyBorder(ModernTheme.px(2), ModernTheme.px(8), ModernTheme.px(2), ModernTheme.px(8))
            ))
            badge.setToolTipText("GoBD § 146 Abs. 4 AO | Einnahme ${docNum} (${format}) | SHA-256: ${fileSha}")
            panel.setPluginBadge(badge)

            JPanel banner = new JPanel(new BorderLayout(ModernTheme.px(12), 0))
            banner.setBackground(new Color(220, 252, 231))
            banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(74, 222, 128)),
                ModernTheme.padding(6, 12)
            ))
            String infoText = incLock ?
                "🔒 GoBD-Festschreibung: Einnahme ${docNum} (${format}, SHA-256: ${shortSha}…) ist mit einem Originalbeleg im Jahresarchiv verknuepft. Betraege, Steuer, Konto & Datum sind gesperrt; Zahlungsstatus und Zahlungsdatum bleiben buchbar." :
                "🔒 GoBD-Festschreibung: Einnahme ${docNum} ist mit ${receipts.length} archivierten Beleg(en) verknuepft. Betraege, Steuer, Konto & Datum sind gesperrt; Zahlungsstatus und Zahlungsdatum bleiben buchbar."
            JLabel info = new JLabel(infoText)
            info.setFont(ModernTheme.font(1, Font.BOLD))
            info.setForeground(new Color(22, 101, 52))
            banner.add(info, BorderLayout.CENTER)

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0))
            actions.setOpaque(false)
            JButton openBtn = ModernTheme.actionButton(new JButton("Originalbeleg oeffnen"), null)
            openBtn.setToolTipText(archivePath)
            openBtn.addActionListener({
                Path f = Paths.get(archivePath)
                if (Files.exists(f)) {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().open(f.toFile())
                        } catch (Exception ex) {
                            notifyPopup("Archivierte Belegdatei:\n${f.toAbsolutePath()}")
                        }
                    } else {
                        notifyPopup("Archivierte Belegdatei:\n${f.toAbsolutePath()}")
                    }
                } else {
                    showError("Archivdatei nicht gefunden:\n${archivePath}")
                }
            })
            actions.add(openBtn)
            banner.add(actions, BorderLayout.EAST)
            panel.setPluginBanner(banner)
        } else {
            panel.setContentLocked(false)

            JLabel badge = new JLabel("⚠️ GoBD: OHNE BELEG")
            badge.setOpaque(true)
            badge.setBackground(new Color(254, 243, 199))
            badge.setForeground(new Color(146, 64, 14))
            badge.setFont(ModernTheme.font(0, Font.BOLD))
            badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(245, 158, 11), 1, true),
                BorderFactory.createEmptyBorder(ModernTheme.px(2), ModernTheme.px(8), ModernTheme.px(2), ModernTheme.px(8))
            ))
            badge.setToolTipText("GoBD Rz. 61–64: Keine Buchung ohne Beleg! Bitte verknuepfen Sie eine ZUGFeRD-/XRechnung-Datei oder einen Beleg.")
            panel.setPluginBadge(badge)

            JPanel banner = new JPanel(new BorderLayout(ModernTheme.px(12), 0))
            banner.setBackground(new Color(254, 243, 199))
            banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(245, 158, 11)),
                ModernTheme.padding(6, 12)
            ))
            JLabel info = new JLabel("⚠️ GoBD-Hinweis: Zu dieser Einnahme ist noch kein Originalbeleg hinterlegt (Grundsatz: „Keine Buchung ohne Beleg“, GoBD Rz. 61–64).")
            info.setFont(ModernTheme.font(1, Font.PLAIN))
            info.setForeground(new Color(146, 64, 14))
            banner.add(info, BorderLayout.CENTER)

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0))
            actions.setOpaque(false)
            JButton linkBtn = ModernTheme.actionButton(new JButton("ZUGFeRD / Beleg verknuepfen"), null)
            linkBtn.addActionListener({
                JFileChooser chooser = new JFileChooser()
                chooser.setDialogTitle("ZUGFeRD / XRechnung / Beleg fuer Einnahme auswaehlen")
                chooser.setFileFilter(new FileNameExtensionFilter("Belegdateien (PDF, XML, PNG, JPG)", "pdf", "xml", "png", "jpg", "jpeg"))
                if (chooser.showOpenDialog(panel instanceof Component ? (Component) panel : null) == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
                    importZugferdIncomeFile(chooser.getSelectedFile().toPath(), rev, false)
                }
            })
            actions.add(linkBtn)
            banner.add(actions, BorderLayout.EAST)
            panel.setPluginBanner(banner)
        }
        return
    }

    // -------------------------------------------------------------------------
    // B. Ausgangsrechnungen (Item) - nur fuer Rechnungen, niemals Auftraege oder Angebote
    // -------------------------------------------------------------------------
    if (!(owner instanceof Item)) {
        return
    }
    Item doc = (Item) owner
    if (doc.__getInttype() != Item.TYPE_INVOICE || doc.__getIDS() <= 0) {
        panel.setContentLocked(false)
        panel.setPluginBadge(null)
        panel.setPluginBanner(null)
        return
    }

    def lock = getLockRecord(doc.__getIDS())
    if (lock != null) {
        String docNum = lock[1]?.toString() ?: doc.__getCnumber()
        String lockStatus = lock[2]?.toString() ?: "FINALIZED"
        String finalizedAt = lock[3]?.toString() ?: ""
        String finalizedBy = lock[4]?.toString() ?: ""
        String archivePath = lock[7]?.toString() ?: ""
        String format = lock[8]?.toString() ?: "ARCHIVE"
        String sha256 = lock[9]?.toString() ?: ""
        String shortHash = sha256.length() >= 12 ? sha256.substring(0, 12) : sha256
        boolean cancelled = (lockStatus == "CANCELLED")

        // 1. Inhaltliche Felder & Positionen im UI sperren (Zahlungsstatus/Mahnungen bleiben frei)
        panel.setContentLocked(true)

        // 2. Kompaktes GoBD-Badge in der Kopfzeile neben dem Status-Pill
        Color badgeBg = cancelled ? new Color(254, 226, 226) : new Color(254, 243, 199)
        Color badgeFg = cancelled ? new Color(153, 27, 27) : new Color(146, 64, 14)
        Color badgeBorder = cancelled ? new Color(248, 113, 113) : new Color(245, 158, 11)
        JLabel badge = new JLabel(cancelled ? "🔒 GoBD STORNIERT" : "🔒 GoBD FESTGESCHRIEBEN")
        badge.setOpaque(true)
        badge.setBackground(badgeBg)
        badge.setForeground(badgeFg)
        badge.setFont(ModernTheme.font(0, Font.BOLD))
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(badgeBorder, 1, true),
            BorderFactory.createEmptyBorder(ModernTheme.px(2), ModernTheme.px(8), ModernTheme.px(2), ModernTheme.px(8))
        ))
        badge.setToolTipText("GoBD § 146 Abs. 4 AO | Festgeschrieben am ${finalizedAt} von ${finalizedBy} | Format: ${format} | SHA-256: ${sha256}")
        panel.setPluginBadge(badge)

        // 3. Hinweis- und Aktionsbanner unter der ModernCommandBar
        JPanel banner = new JPanel(new BorderLayout(ModernTheme.px(12), 0))
        banner.setBackground(badgeBg)
        banner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, badgeBorder),
            ModernTheme.padding(6, 12)
        ))

        JLabel info = new JLabel(
            "🔒 GoBD-Festschreibung (§ 146 Abs. 4 AO): Beleg ${docNum} [${lockStatus}] ist unveraenderbar archiviert (${format}, SHA-256: ${shortHash}…). " +
            "Steuerrelevante Felder & Positionen sind gesperrt; Zahlungsstatus, Teilzahlungen & Mahnungen bleiben buchbar."
        )
        info.setFont(ModernTheme.font(1, Font.BOLD))
        info.setForeground(badgeFg)
        banner.add(info, BorderLayout.CENTER)

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0))
        actions.setOpaque(false)

        JButton openArchiveBtn = ModernTheme.actionButton(new JButton("Originalbeleg oeffnen"), null)
        openArchiveBtn.setToolTipText(archivePath)
        openArchiveBtn.addActionListener({
            Path f = Paths.get(archivePath)
            if (Files.exists(f)) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(f.toFile())
                    } catch (Exception ex) {
                        notifyPopup("Archivierte Belegdatei:\n${f.toAbsolutePath()}")
                    }
                } else {
                    notifyPopup("Archivierte Belegdatei:\n${f.toAbsolutePath()}")
                }
            } else {
                showError("Archivdatei nicht gefunden:\n${archivePath}")
            }
        })
        actions.add(openArchiveBtn)

        if (!cancelled && doc.__getInttype() == Item.TYPE_INVOICE) {
            JButton stornoBtn = ModernTheme.actionButton(new JButton("Stornorechnung erstellen"), null)
            stornoBtn.addActionListener({
                int confirm = JOptionPane.showConfirmDialog(
                    panel instanceof Component ? (Component) panel : null,
                    "Soll fuer den festgeschriebenen Beleg ${docNum} jetzt eine GoBD-konforme Stornorechnung erstellt werden?",
                    "GoBD-Stornorechnung (§ 146 Abs. 4 AO)",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                )
                if (confirm == JOptionPane.YES_OPTION) {
                    stornoDocument(doc)
                }
            })
            actions.add(stornoBtn)
        }

        banner.add(actions, BorderLayout.EAST)
        panel.setPluginBanner(banner)
    } else {
        // Gespeicherter Entwurf (noch nicht festgeschrieben)
        panel.setContentLocked(false)

        JLabel draftBadge = new JLabel("GoBD: Entwurf (offen)")
        draftBadge.setOpaque(true)
        draftBadge.setBackground(ModernTheme.accentSoft())
        draftBadge.setForeground(ModernTheme.accent())
        draftBadge.setFont(ModernTheme.font(0, Font.BOLD))
        draftBadge.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(2), ModernTheme.px(8), ModernTheme.px(2), ModernTheme.px(8)))
        draftBadge.setToolTipText("Dieser Beleg ist noch nicht nach GoBD festgeschrieben und wird beim Export automatisch gesichert.")
        panel.setPluginBadge(draftBadge)

        JPanel draftBanner = new JPanel(new BorderLayout(ModernTheme.px(12), 0))
        draftBanner.setBackground(ModernTheme.accentSoft())
        draftBanner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.border()),
            ModernTheme.padding(5, 12)
        ))
        JLabel draftInfo = new JLabel("GoBD-Hinweis: Dieser Beleg ist noch ein bearbeitbarer Entwurf und wird beim PDF-/E-Rechnungs-Export automatisch festgeschrieben.")
        draftInfo.setFont(ModernTheme.font(1, Font.PLAIN))
        draftInfo.setForeground(ModernTheme.foreground())
        draftBanner.add(draftInfo, BorderLayout.CENTER)

        JButton lockNowBtn = ModernTheme.actionButton(new JButton("Jetzt GoBD-festschreiben"), null)
        lockNowBtn.addActionListener({
            finalizeDocument(doc, null)
        })
        JPanel draftActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0))
        draftActions.setOpaque(false)
        draftActions.add(lockNowBtn)
        draftBanner.add(draftActions, BorderLayout.EAST)
        panel.setPluginBanner(draftBanner)
    }
})

// Initialisierung beim Starten des Plugins ausfuehren
initSchema()
configureIncomeWatchTimer()
notifyStatus("GoBD-Konformitaets-Plugin (v1.5.0) vollstaendig geladen.")

// Aufraeum-Closure beim Stoppen/Deaktivieren/Neuladen des Plugins
onStop = {
    try {
        logAudit("PLUGIN", 0L, "PLUGIN_STOPPED", "STATE=RUNNING", "STATE=STOPPED", null, "GoBD-Konformitaets-Plugin wurde gestoppt oder deaktiviert.")
    } catch (Exception ignored) { }
    if (incomeWatchTimer != null) {
        try { incomeWatchTimer.cancel() } catch (Exception ignored) { }
        incomeWatchTimer = null
    }
    seenWatchFiles.clear()
    creatingStorno = false
    finalizingIds.clear()
    lastGeneratedFiles.clear()
    masterDataBeforeSave.clear()
}

