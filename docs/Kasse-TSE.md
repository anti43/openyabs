# Kasse und TSE (KassenSichV)

> **Keine Rechtsberatung.** In Deutschland braucht eine elektronische Kasse für echte Barverkäufe eine **zertifizierte technische Sicherheitseinrichtung (TSE)** (§ 146a AO, KassenSichV); Kasse und TSE sind dem Finanzamt zu melden (ELSTER). **YaBS unterstützt noch keine zertifizierte TSE.** Die Kasse von YaBS ist bis dahin nur für Tests und Vorführungen gedacht. Vor dem Einsatz für echte Barverkäufe: mit Ihrer Steuerberatung klären.

*English version below.*

## Stand

Umgesetzt (Grundlage aus Issue #29):

- **TSE-Schnittstelle** in YaBS (`mpv5.pos.tse.Tse`): Vorgang starten, abschließen (signieren).
- **Auswahl** unter *Systemsteuerung › Kasse*, Karte *TSE*:
  - **Keine** – wie bisher; Belege tragen „Demo-Beleg ohne TSE“.
  - **Test-TSE (nicht zertifiziert)** – verhält sich wie eine TSE (Transaktionsnummer, Signaturzähler, Signatur, Seriennummer), schützt aber nichts; Belege tragen „Test-TSE (nicht zertifiziert)“.
  - **Kassen-Seriennummer (Client-ID)**: vorbelegt mit dem Rechnernamen.
- **Ablauf eines Verkaufs**: TSE-Vorgang starten → Rechnung buchen → Vorgang mit den Umsätzen je Steuersatz und der Zahlung abschließen (Prozesstyp `Kassenbeleg-V1`, Prozessdaten `Beleg^19%_7%_10,7%_5,5%_0%^Betrag:Bar`) → speichern → Beleg.
- **TSE-Ausfall**: Der Verkauf geht weiter; der Datensatz wird als ausgefallen markiert (mit Grund), der Beleg zeigt „TSE ausgefallen“.
- **Speicherung**: Tabelle `postransactions` (Datenbankversion 1.1985) – je Verkauf Transaktionsnummer, Start, Ende, Signaturzähler, Signatur, TSE-Seriennummer, Algorithmus, Zeitformat, öffentlicher Schlüssel, Prozesstyp, Prozessdaten, Ausfall.
- **Beleg** (Vorlage *Kassenbon*): neue Felder

  | Feld | Inhalt |
  |---|---|
  | `pos.tse.note` | Hinweis: ohne TSE / Test-TSE / TSE ausgefallen (bei zertifizierter TSE leer) |
  | `pos.tse.transaction`, `pos.tse.counter` | Transaktionsnummer, Signaturzähler |
  | `pos.tse.start`, `pos.tse.end` | Beginn und Ende (Ortszeit) |
  | `pos.tse.signature`, `pos.tse.algorithm` | Prüfwert (Signatur), Signaturalgorithmus |
  | `pos.tse.serial`, `pos.register.serial` | Seriennummer der TSE und der Kasse |
  | `pos.tse.processdata`, `pos.tse.qrdata` | Prozessdaten, Inhalt des Beleg-QR-Codes (DSFinV-K) |
  | `pos.tse.failed` | „TSE ausgefallen“, sonst leer |

  Die mitgelieferte Vorlage enthält die Felder (neue Installationen). Bestehende Kassenbon-Vorlagen: die Felder im Vorlagen-Manager ergänzen.

### Beleg-QR-Code und Storno

- **QR-Code auf dem Bon:** Ein Bild namens `TSEQRCode.img` in der Vorlage wird mit dem QR-Code der TSE-Daten (`pos.tse.qrdata`, DSFinV-K-Format `V0;…`) gefüllt und ohne TSE-Daten entfernt. Die mitgelieferte Kassenbon-Vorlage enthält es (3 × 3 cm, zentriert). Eigene Vorlagen: ein Bild einfügen und im Dialog *Bild › Optionen* den Namen `TSEQRCode.img` vergeben.
- **Storno:** Auf dem Bon-Bildschirm *Stornieren* (letzter Verkauf) oder oben *Storno* mit der Nummer eines Verkaufs. Gebucht wird eine bezahlte Gutschrift mit denselben Positionen, die auf den Verkauf verweist; die TSE signiert sie als eigenen Vorgang (`Kassenbeleg-V1` mit negativen Beträgen, z. B. `Beleg^-11.90_…^-11.90:Bar`). Jeder Verkauf kann einmal storniert werden. Auf dem Storno-Bon steht `STORNO zu <Nummer>` (Feld `pos.storno`).

### JavaPOS / ADK (zur Laufzeit geladen)

In der Karte *TSE* der Kasse: **ADK herunterladen** lädt die eingetragenen Adressen (durch Leerzeichen getrennt) in den Ordner `<Programmordner>/javapos`. Standard ist JavaPOS von Maven Central (`javapos-contracts`, `javapos-controls`, `javapos-config-loader`). Jars werden gespeichert, aus `.zip`/`.tar.gz` werden Jars, `jpos.xml` und native Bibliotheken übernommen. Das **Epson JavaPOS ADK** gibt es bei Epson als Installationsprogramm (Windows/Linux): installieren, dann übernimmt **ADK hinzufügen …** die Dateien aus dem Installationsordner (z. B. `/opt/EpsonJavaPOS`, `C:\Program Files\EPSON\JavaPOS`) oder aus einem Archiv.

YaBS hängt nicht davon ab: Die Jars werden mit einem eigenen ClassLoader geladen (`mpv5.pos.javapos.JavaPos.classLoader()` / `load("jpos.POSPrinter")`), Code für die Epson-TSE nutzt sie per Reflection. Die Karte zeigt, ob JavaPOS gefunden wurde. Eine `jpos.xml` im Ordner wird als `jpos.config.populatorFile` gesetzt. Native Bibliotheken brauchen ggf. `-Djava.library.path=<Programmordner>/javapos` beim Start.

Noch offen (siehe Issue #29):

- **Zertifizierte TSE** (Epson TSE über das Epson JavaPOS ADK; Einrichtung mit PIN/PUK, Client-Registrierung, Zeitsynchronisation, Zertifikats- und Speicherwarnungen). Das ADK wird nicht mitgeliefert; Herunterladen/Übernehmen und Laden zur Laufzeit gibt es (oben), die TSE-Ansteuerung selbst fehlt noch.
- **Exporte**: TSE-Daten (TAR), DSFinV-K, Z-Bon/Tagesabschluss, Kassenbuch.
- Teil-Retouren (einzelne Positionen), sonstige Vorgänge (Schublade), Trainingsmodus.
- Verfahrensdokumentation (Vorlage) und Angaben für die ELSTER-Meldung.

Alle Angaben sind vor einem produktiven Einsatz gegen die aktuellen Fassungen von AO, KassenSichV, BSI TR-03153 / TR-03151 und DSFinV-K zu prüfen.

---

# Cash register and TSE (EN)

> **No legal advice.** In Germany an electronic cash register used for real cash sales needs a **certified TSE** (§ 146a AO, KassenSichV), and register and TSE must be reported to the tax office. **YaBS does not support a certified TSE yet**; until then the register is for tests and demonstrations only.

Done: the TSE interface; *Control Panel › Cash register*, card *TSE*: **none** or **test TSE (not certified)** and the register serial (client ID); every sale starts a TSE transaction before booking and finishes it with the totals per VAT rate and the payment (`Kassenbeleg-V1`); a TSE failure does not stop the sale and is recorded and printed; the data is stored in the table `postransactions` (database version 1.1985); receipt fields `pos.tse.*` (see the table above).

Still to do (issue #29): a certified TSE (Epson via JavaPOS ADK), TAR / DSFinV-K exports, daily closing, cancellations as signed transactions, the QR code image, procedure documentation and notification data.
