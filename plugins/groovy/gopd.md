# GoBD-Konformitäts-Plugin für OpenYaBS

Das Plugin **`gobd_compliance.groovy`** (ID: `groovy.gobd-compliance`, Version `1.4.2`) erweitert OpenYaBS um zentrale technische Schutzmechanismen zur Unterstützung der **GoBD** (*Grundsätze zur ordnungsmäßigen Führung und Aufbewahrung von Büchern, Aufzeichnungen und Unterlagen in elektronischer Form sowie zum Datenzugriff*) sowie der Anforderungen aus **§§ 146, 147 AO** und **§ 14 Abs. 4 UStG**.

---

## Inhaltsverzeichnis

1. [Funktionsumfang im Überblick](#1-funktionsumfang-im-überblick)
2. [Installation & Einrichtung](#2-installation--einrichtung)
3. [Lebenszyklus einer Rechnung (Entwurf → Festschreibung → Zahlung)](#3-lebenszyklus-einer-rechnung)
4. [Menübefehle & Schnellsuche](#4-menübefehle--schnellsuche)
5. [Z3-Betriebsprüfer-Export (IDEA / GDPdU)](#5-z3-betriebsprüfer-export-idea--gdpdu)
6. [Datenbankschema & Archivstruktur](#6-datenbankschema--archivstruktur)
7. [Wichtige organisatorische Hinweise](#7-wichtige-organisatorische-hinweise)
8. [Fehlende Funktionen / Nicht abgedeckt (Missing)](#8-fehlende-funktionen--nicht-abgedeckt-missing)

---

## 1. Funktionsumfang im Überblick

* **Automatische Festschreibung beim Export:** Sobald eine Ausgangsrechnung oder Gutschrift als **PDF**, **ZUGFeRD** oder **XRechnung (XML)** erzeugt wird, wird sie automatisch festgeschrieben (optional in den Einstellungen konfigurierbar).
* **Unveränderbarkeit (`DatabaseObjectModifier`):** Festgeschriebene Belege und deren Positionen (`SubItem`) sind gegen nachträgliche Änderungen steuerrelevanter Felder sowie gegen Löschen und Verschieben in den Papierkorb gesperrt.
* **Erlaubter Offene-Posten-Workflow:** Reine Statuswechsel im Zahlungs- und Mahnwesen (z. B. *Offen* → *Bezahlt*, Mahnstufe) bleiben nach der Festschreibung möglich und werden lückenlos protokolliert.
* **Stammdaten- & Positions-Snapshots:** Empfängeranschrift, Steuernummer und Rechnungspositionen werden zum Zeitpunkt der Festschreibung eingefroren. Spätere Änderungen an Kundenstammdaten verändern archivierte Belege nicht.
* **Kryptographische Hash-Verkettung (SHA-256):** Jeder archivierte Beleg wird mit dem Hash seines Vorgängers verkettet, sodass fehlende oder manipulierte Dateien sofort erkannt werden.
* **Stornorechnungen statt Bearbeitung:** Korrekturen erfolgen ausschließlich über eine festgeschriebene Gegenbuchung (`ST-YYYY-NNNNN`).
* **Z1- & Z3-Prüferzugriff:** Schreibgeschützte Prüferansicht direkt in YaBS (Z1) sowie vollständiger GDPdU-/IDEA-kompatibler ZIP-Export inkl. `index.xml` und `gdpdu-01-09-2004.dtd` (Z3).

---

## 2. Installation & Einrichtung

### Installation
1. Die Datei **`gobd_compliance.groovy`** in den Unterordner `plugins/` des YaBS-Programmverzeichnisses kopieren *(oder über **Einstellungen › Plugin-Manager › Neues Groovy-Plugin…** anlegen)*.
2. Im Plugin-Manager auf **Neu laden** klicken *(bzw. im Plugin-Editor **`F5`** zum Prüfen und **`Strg+S`** zum Starten drücken)*.
3. Nach erfolgreichem Start erscheint in der Statuszeile:  
   `GoBD-Konformitaets-Plugin (v1.4.2) vollstaendig geladen.`

> **Hinweis zur Datenbankmigration:** Die benötigten Tabellen `gobd_document_lock` und `gobd_audit_log` (inkl. eventueller Schema-Erweiterungen aus älteren Versionen) werden beim Plugin-Start automatisch angelegt.

### Einstellungen
Unter **Einstellungen › Plugins › GoBD-Konformitaet** stehen folgende Optionen zur Verfügung:

| Einstellung | Standard | Beschreibung |
| :--- | :--- | :--- |
| **Automatische Festschreibung beim Export** | Aktiv (`true`) | Schreibt Rechnungen und Gutschriften beim Erzeugen von PDF / ZUGFeRD / XRechnung automatisch fest. |
| **Revisionssicherer GoBD-Archivordner** | `<YaBS-Datenordner>/gobd-archive` | Zielverzeichnis für alle unveränderbaren Originalbelege (nach Jahren unterteilt) und Z3-Export-ZIPs. |

---

## 3. Lebenszyklus einer Rechnung

### Phase 1: Entwurf (frei bearbeitbar)
* Solange eine Rechnung (`Item.TYPE_INVOICE`) oder Gutschrift (`Item.TYPE_CREDIT`) noch **nicht** festgeschrieben ist, kann sie beliebig bearbeitet oder gelöscht werden.
* Das Anlegen neuer Entwürfe wird als `DRAFT_CREATED` protokolliert.

### Phase 2: Festschreibung (*Inverkehrbringen*)
Die Festschreibung erfolgt **automatisch beim PDF-/E-Rechnungs-Export** oder **manuell** über *Plugins › GoBD: 1. Beleg festschreiben*:

1. **Nummernvergabe:** Besitzt der Beleg noch keine endgültige Nummer (oder beginnt diese mit `ENTWURF` bzw. `DRAFT`), vergibt das Plugin automatisch die nächste fortlaufende Nummer (`RE-JJJJ-NNNNN` bzw. `ST-JJJJ-NNNNN`).
2. **Pflichtangaben-Prüfung (§ 14 Abs. 4 UStG):** Geprüft werden u. a.:
   * Endgültige fortlaufende Rechnungsnummer
   * Ausstellungsdatum (`dateadded`) und Leistungs-/Lieferdatum (`datetodo`)
   * Zugewiesener Kontakt mit Straße und Ort
   * Netto- und Bruttobetrag sowie mindestens eine Rechnungsposition
3. **Archivierung:** Die zuletzt erzeugte Datei (PDF/A-3 ZUGFeRD oder XRechnung XML) wird unveränderbar in `<Archivordner>/<Jahr>/` abgelegt und auf Dateisystemebene schreibgeschützt (`read-only`). Liegt noch keine Exportdatei vor, erzeugt das Plugin ein EN-16931-konformes Archiv-XML inkl. eingefrorener Kundendaten.
4. **Hash-Verkettung:** Der Prüfsummen-Eintrag wird über folgende Formel berechnet und in `gobd_document_lock` gespeichert:
   $$\text{SHA256}(\text{Datei-Bytes}_n \parallel \text{Hash}_{n-1})$$
   *(Der Initialwert / Genesis-Hash für den ersten Beleg besteht aus 64 Nullen.)*
5. **Bestätigung:** Eine Meldung über den `Notificator` zeigt Belegnummer, Format, Hash-Präfix und Archivdatei an und schreibt den Vorgang in das YaBS-Verlaufsprotokoll.

### Phase 3: Nach der Festschreibung (Zwei getrennte Ebenen)

| Bereich | Verhalten nach Festschreibung | Audit-Log-Aktion |
| :--- | :--- | :--- |
| **Steuerrelevante Belegdaten** (Beträge, Steuern, Rabatte, Datum, Nummer, Beschreibung, Kunde, Positionen) | **Gesperrt** (Änderung wird blockiert) | `BLOCKED_EDIT` |
| **Löschen / Papierkorb** (Beleg oder einzelne Positionen) | **Gesperrt** (Löschen wird blockiert) | `BLOCKED_DELETE` |
| **Manuelles Setzen auf „Storniert“** (ohne Stornorechnung) | **Gesperrt** (Verweis auf Storno-Funktion) | `BLOCKED_MANUAL_CANCEL` |
| **Zahlungsstatus** (`intstatus`, z. B. *Bezahlt*) | **Erlaubt** | `PAYMENT_STATUS_CHANGED` |
| **Mahnstufe** (`intreminders`) | **Erlaubt** | `DUNNING_CHANGED` |
| **Kundenstammdaten** (`Contact`) | **Erlaubt** (nur mit Alt-/Neu-Protokoll; archivierte Belege bleiben unverändert) | `MASTERDATA_CHANGED` |

---

## 4. Menübefehle & Schnellsuche

Alle Funktionen sind im Menü **Plugins** sowie über **Alles durchsuchen (`Umschalt + Umschalt`)** erreichbar:

1. **GoBD: 1. Beleg festschreiben**  
   Listet alle noch offenen (nicht festgeschriebenen) Rechnungen und Gutschriften zur manuellen Festschreibung auf.
2. **GoBD: 2. Beleg stornieren (Stornorechnung)**  
   Erzeugt für einen festgeschriebenen Beleg eine Stornorechnung (`ST-JJJJ-NNNNN`, `Item.TYPE_CREDIT`) mit negierten Beträgen und Mengen, schreibt diese sofort fest, setzt den Originalbeleg auf `CANCELLED` (in `gobd_document_lock` sowie `items.intstatus`) und aktualisiert die geöffnete Belegansicht.
3. **GoBD: 3. Archiv-Integritaet pruefen**  
   Prüft im Hintergrund, ob alle archivierten Dateien vorhanden sind und ob die gesamte SHA-256-Hash-Kette lückenlos und unverfälscht ist.
4. **GoBD: 4. Betriebspruefer-Export (Z3 / IDEA ZIP)**  
   Erstellt für ein frei wählbares Prüfungsjahr ein vollständiges Z3-Exportpaket (siehe [Abschnitt 5](#5-z3-betriebsprüfer-export-idea--gdpdu)).
5. **GoBD: 5. Pruefer-Ansicht oeffnen (Z1 Read-Only Tab)**  
   Öffnet im YaBS-Hauptfenster einen strikt schreibgeschützten Tab mit dem Belegjournal (`gobd_document_lock`).

### Schnellsuche (`Umschalt + Umschalt`)
Die Eingabe einer festgeschriebenen Belegnummer listet den entsprechenden GoBD-Datensatz direkt in den Suchergebnissen. Ein Klick darauf öffnet den archivierten Originalbeleg.

---

## 5. Z3-Betriebsprüfer-Export (IDEA / GDPdU)

Der Export erzeugt eine ZIP-Datei (`YaBS_GoBD_Z3_Export_<Jahr>_<Zeitstempel>.zip`) im konfigurierten Archivordner mit folgendem Inhalt:

* **`index.xml`** & **`gdpdu-01-09-2004.dtd`** – Offizielle Beschreibungsdateien für den direkten Import in Prüfsoftware wie **IDEA** (inkl. Spaltendefinitionen, UTF-8-Kennzeichnung und Fremdschlüssel-Verknüpfungen).
* **`rechnungen.csv`** – Alle im Prüfungsjahr festgeschriebenen Rechnungen und Stornobelege (inkl. Netto, Steuer, Brutto, Datumsangaben `YYYY-MM-DD`, SHA-256-Hashes, Storno-Bezug und Kunden-Snapshot).
* **`rechnungspositionen.csv`** – Alle Einzelpositionen zu den exportierten Rechnungen des gewählten Prüfungsjahres.
* **`kunden.csv`** – Aktuelle Kunden- und Lieferantenstammdaten.
* **`audit_log.csv`** – Vollständiges Änderungs- und Zugriffsprotokoll des Prüfungsjahres (inkl. `old_value` und `new_value`).
* **`/belege/`** – Alle archivierten Originalbelege (`.pdf` / `.xml`) des Prüfungsjahres.
* **`VERFAHRENSDOKUMENTATION_TECHNIK.txt`** – Kurzbeschreibung der technischen Sperr-, Snapshot- und Hash-Mechanismen.

---

## 6. Datenbankschema & Archivstruktur

### Tabelle `gobd_document_lock`
Speichert den Sperrzustand, eingefrorene Snapshots und die kryptographische Hash-Kette pro festgeschriebenem Beleg:
* `document_id` (PK), `chain_seq`, `document_number` (UNIQUE), `status` (`FINALIZED` / `CANCELLED`)
* `finalized_at`, `finalized_by`
* `customer_snapshot`, `positions_snapshot`
* `archive_path`, `artifact_format` (`ZUGFERD_PDFA3` / `XRECHNUNG_XML`)
* `sha256_hash`, `prev_sha256_hash`, `cancels_document_id`

### Tabelle `gobd_audit_log`
Anfügendes (*append-only*) Ereignisprotokoll mit Zeitstempel, Benutzer, Entität (`INVOICE`, `SUBITEM`, `CONTACT`, `SETTINGS`, `EXPORT`), Aktion, Alt-/Neuwert (`old_value`, `new_value`), Dateihash und Detailbeschreibung.

---

## 7. Wichtige organisatorische Hinweise

Ein Software-Plugin allein stellt noch keine vollständige GoBD-Konformität sicher. Ergänzend sind folgende organisatorische Maßnahmen erforderlich:

1. **Revisionssichere Datensicherung:** Der konfigurierte GoBD-Archivordner sowie die YaBS-Datenbank müssen regelmäßig gesichert und über die gesetzlichen Aufbewahrungsfristen (nach § 147 Abs. 3 AO regelmäßig **8 Jahre** für Buchungsbelege/Rechnungen bzw. **10 Jahre** für Bücher und Abschlüsse) unveränderbar aufbewahrt werden (z. B. WORM-/Versionierungs-Backup).
2. **Datenbank-Zugriffsschutz:** Direkte Schreibrechte auf die Datenbank außerhalb von OpenYaBS (insbesondere `UPDATE`/`DELETE` auf `gobd_document_lock`, `gobd_audit_log` und `items`) sollten für reguläre Benutzer unterbunden werden.
3. **Betriebliche Verfahrensdokumentation:** Die mitgelieferte `VERFAHRENSDOKUMENTATION_TECHNIK.txt` deckt nur den technischen Teil des Plugins ab und sollte um Ihre individuellen betrieblichen Abläufe (Rechnungseingang, Belegablage, Backup-Konzept, Zugriffsrechte) ergänzt werden.

---

## 8. Fehlende Funktionen / Nicht abgedeckt (Missing)

Folgende Anforderungen liegen **außerhalb des aktuellen Funktionsumfangs** (*Out of Scope*) dieses Plugins und werden derzeit noch nicht technisch durchgesetzt:

### Eingangsbelege & Ausgaben (*„Keine Buchung ohne Beleg“*, GoBD Rz. 61–64)
Das Plugin konzentriert sich aktuell auf **Ausgangsrechnungen und Gutschriften (`Item`)**. Für Ausgaben (*Expenses*) und Eingangsrechnungen greift noch keine automatische Beleg- und Archivierungspflicht:

* **Verknüpfungspflicht:** In den GoBD gilt der Grundsatz *„Keine Buchung ohne Beleg“*. Jede in YaBS erfasste Ausgabe muss eindeutig mit dem zugehörigen Eingangsbeleg verknüpft sein (entweder direkt als hochgeladene Datei am Datensatz oder über eine eindeutige Belegnummer/Referenz auf ein externes Belegarchiv). Dies wird vom Plugin derzeit **nicht** erzwungen oder validiert.
* **Aufbewahrung im digitalen Originalformat (E-Rechnungspflicht):** Digital empfangene Eingangsrechnungen (z. B. als PDF, ZUGFeRD-PDF/A-3 oder XRechnung-XML) müssen zwingend in ihrem **digitalen Originalformat** unveränderbar archiviert werden – ein bloßes Ausdrucken und Abheften in Papierform reicht steuerrechtlich nicht aus. Das Plugin übernimmt aktuell **keine** automatische Hash-Verkettung, Sperrung oder Z3-Exportierung für hochgeladene Eingangsrechnungen/Ausgabenbelege.

*(Diese Punkte müssen bis zu einer eventuellen Erweiterung organisatorisch – z. B. durch disziplinierte Dateiablage am Ausgaben-Datensatz bzw. ein revisionssicheres Belegarchiv außerhalb des Plugins – sichergestellt werden.)*

---

*Hinweis: Diese Dokumentation beschreibt die technische Funktionsweise des OpenYaBS-Plugins und ersetzt keine steuerliche oder rechtliche Beratung.*
