# YaBS – Hinweise und Neuerungen

Technische Hinweise zu Datenbank, modernen Ansichten und E-Rechnung. Die Funktionsübersicht steht in der [README](../README.md).

## Datenbank (Apache Derby 10.17, Java 21)

YaBS nutzt Apache Derby 10.17.1.0, das Java 21 voraussetzt.

- **Ältere Datenbanken** werden beim ersten Start im *Soft-Upgrade*-Modus geöffnet und auf die aktuelle YaBS-Version aktualisiert. Das Dateiformat bleibt dabei unverändert, ältere YaBS-Versionen (Derby 10.8) können dieselbe Datenbank weiterhin öffnen.
- **Unterbrochene Updates** setzen beim nächsten Start dort fort, wo sie aufgehört haben. Bereits durchgeführte Änderungen werden übersprungen. Die Versionsnummer wird nach jedem Schritt gespeichert und bei einem Fehler nicht erhöht.
- **Beschädigte Datenbank** (z. B. „StoredPreparedStatement … ist nicht vorhanden“ und danach „Keine aktuelle Verbindung“): YaBS bietet an, alle Daten in eine neu angelegte Datenbank zu kopieren. Die alte Datenbank bleibt unverändert als `<name>-backup-<Datum>` daneben liegen. Die neu aufgebaute Datenbank hat das Derby-10.17-Format und kann **nicht mehr** mit älteren YaBS-Versionen geöffnet werden; die Sicherung schon. Bei großen Datenbanken dauert das einige Minuten.

Vor jedem Update empfiehlt sich eine Kopie des Datenbankordners.

## Moderne Ansichten für Kontakte, Produkte und Belege (Vorschau)

Kontakt-, Produkt- und Belegansicht (Angebote, Aufträge, Rechnungen, Anzahlungen, Abschlagsrechnungen, Gutschriften) gibt es zusätzlich in einem modernen, skalierenden Layout (Kopfzeile mit Name und Rollen, Daten in Karten, die sich je nach Fensterbreite auf 1–3 Spalten verteilen, Vorgänge darunter). Alle Funktionen der klassischen Ansicht sind enthalten.

- **Standard seit dieser Version.** Zurück zu den klassischen Ansichten: *Ansicht > Layout > Modernes Aussehen* (Haken entfernen) oder *Werkzeuge > Einstellungen > Benutzereinstellungen*, Kästchen **„Moderne Ansichten für Kontakte, Produkte und Belege“**; gilt für neu geöffnete Registerkarten.
- Oder beim Start: `java -Dyabs.ui=modern -jar openyabs.jar` (bzw. `-Dyabs.ui=classic`); das überschreibt die Benutzereinstellung.

In der modernen Ansicht stehen die Einstellungen des Reiters *E-Rechnung* (Versand, Leitweg-ID, Konto, Zahlungsziel, Skonto) in der Karte **Zahlung**; Haupt- und weitere Adressen (Rechnungs-/Lieferadressen) sind in der Karte **Anschrift** zusammengefasst. In der Vorgangsliste zeigt die Spalte **Inhalt** die Positionen jedes Angebots, Auftrags, jeder Rechnung bzw. Tätigkeitsliste in einer Zeile; der Filter durchsucht sie mit.

In der modernen Produktansicht stehen alle Daten ohne Reiter in Karten (Allgemein, Preise, Einordnung, Beschreibung, Hersteller und Lieferanten, Lager, Staffelpreise, Bild und Dateien). Einkaufspreis, Aufschlag %, Verkaufspreis und Bruttopreis rechnen sich beim Tippen gegenseitig aus. Das Produktbild erscheint im Kopf. Unten stehen die Angebote, Aufträge, Rechnungen und Gutschriften mit diesem Produkt (Kontakt, Menge, Netto der Position, Positionstext) samt Summe.

Alle Aktionen zum ganzen Datensatz stehen in einer Befehlsleiste oben (Speichern hervorgehoben, Löschen rechts abgesetzt, Seltenes unter *Mehr*). Tastenkürzel, solange der Fokus in der Ansicht ist:

| Kürzel | Aktion |
|---|---|
| Strg+S | Speichern |
| Strg+Umschalt+S | Als neu speichern |
| Strg+R | Änderungen verwerfen |
| Strg+P / Strg+Umschalt+P | Drucken / PDF |
| Strg+E | E-Mail |
| Strg+F | Vorgangsliste filtern |
| Strg+Umschalt+F | Suchleiste links |
| Strg+C / Strg+V | Datensatz in die Ablage / aus der Ablage (in Textfeldern und Tabellen: Text kopieren/einfügen wie gewohnt) |
| Strg+D | Favorit an/aus |
| Strg+Entf | Löschen (mit Rückfrage; in Textfeldern: Wort löschen) |
| Strg+Umschalt+H | Verlauf (Kontakt) |
| Strg+F1 | Übersicht aller Tastenkürzel |

**Löschen** fragt mit einer Liste nach, was mitgelöscht wird: was in den Papierkorb geht (beim Kontakt auch alle Angebote, Aufträge, Rechnungen, Gutschriften und deren Positionen), was endgültig gelöscht wird (gespeicherte Einstellungen wie Leitweg-ID und Zahlungsbedingungen, Staffelpreise eines Produkts) und was erhalten bleibt (Adressen, Dateien, Tätigkeitslisten, Positionen mit dem Produkt). *Abbrechen* ist vorgewählt.

**Alle Tastenkürzel** zeigt *Hilfe > Tastenkürzel* (Strg+F1); die Kürzel des Menüs werden dort direkt aus dem Menü gelesen. Geändert im Hauptmenü, damit nichts doppelt belegt ist und nichts überrascht: *Beenden* Strg+Q (vorher Strg+X), *Als neu speichern* Strg+Umschalt+S (vorher Strg+N), *Kopieren* in die Ablage Strg+C und *Einfügen* Strg+V (vorher Strg+N / Strg+B; in Textfeldern und Tabellen bleibt Text kopieren/einfügen), *Alle Registerkarten schließen* Strg+Umschalt+W (vorher Strg+L, das weiterhin die Log-Konsole öffnet).

In der modernen Belegansicht stehen Kunde, Belegdaten, Termine, Freitext, Dateien und Eigenschaften in Karten, die Positionen mit den Summen (Netto − Rabatt + Steuer = **Gesamt**) bilden den Hauptbereich. Vorschau, weitere Belege (Lieferschein, Auftragsbestätigung, Drucken mit AB), E-Rechnung, *Erstellen* (Auftrag, Rechnung, Anzahlung, Abschlagsrechnung, Gutschrift), Mahnen, Termin, Vergleich mit dem Auftrag und referenzierte Belege stehen in der Befehlsleiste. Strg+F springt in die Positionen.

**Tätigkeitslisten** sind ausgeblendet (Navigation, Listen im Kontakt, Rückfragen beim Erstellen von Rechnungen, Löschbestätigung). Daten und Funktionen bleiben erhalten; einblenden mit `java -Dyabs.activitylists=true -jar openyabs.jar`.

**Hauptfenster:** Navigation links über die volle Höhe, Registerkarten flach in einer scrollbaren Zeile (Schließen-Kreuz pro Karte), schlichtere Werkzeug- und Statusleiste; beim ersten Start passt sich die Fenstergröße dem Bildschirm an. Das gilt immer, unabhängig von der Einstellung für die modernen Ansichten.

**Modernes Erscheinungsbild für alles andere:** Mit eingeschalteten modernen Ansichten verwendet YaBS das Erscheinungsbild *YaBS Modern* (auf Basis von [FlatLaf](https://www.formdev.com/flatlaf/), Apache-2.0), damit Listen, Einstellungen, Dialoge, Menüs und die übrigen klassischen Masken zu den modernen Ansichten passen: abgerundete Felder und Schaltflächen mit leichtem Verlauf und Schatten, Popups und Menüs mit Schatten, Tabellen mit Zeilenlinien und größerer Zeilenhöhe, fette Abschnittsüberschriften. Alte Rahmen (geätzt, Relief, harte Linien) der klassischen Masken werden dabei durch abgerundete ersetzt, ohne die Masken selbst zu ändern.

- Gilt, solange kein anderes Erscheinungsbild gewählt ist (ein früher gespeichertes System-/Metal-Erscheinungsbild zählt als „nicht gewählt“).
- *Ansicht > Layout > Modernes Aussehen* schaltet moderne Ansichten und Aussehen gemeinsam ein oder aus, *Ansicht > Layout > System* schaltet zurück auf das Erscheinungsbild des Betriebssystems; andere Erscheinungsbilder wie bisher in den Benutzereinstellungen.
- Farben und Größen stehen in `src/main/resources/mpv5/ui/modern/YabsLookAndFeel.properties`.

**Dateidialoge** (Speichern, Öffnen, Ordner wählen) sind größer, merken sich ihre Größe und haben rechts eine Leiste **Orte** (Zuletzt verwendet, Ablageordner aus den lokalen Einstellungen, Persönlicher Ordner, Schreibtisch, Dokumente). Sie starten immer in einem sinnvollen Ordner:

- Belege (ODT/PDF exportieren, Mahnungen, Massendruck): im **Ablageordner aus den lokalen Einstellungen**, falls einer eingestellt ist (samt Unterordnern aus dem Speicherformat des Benutzers), sonst im zuletzt gewählten Ordner.
- Alle anderen Dateidialoge: im **zuletzt gewählten Ordner**, sonst im Ablageordner, sonst auf dem Schreibtisch bzw. im persönlichen Ordner.
- Der zuletzt gewählte Ordner wird in den lokalen Einstellungen (`lastfiledir`) gespeichert und gilt auch nach einem Neustart. Ein vorgeschlagener Dateiname ohne Ordner landet nicht mehr im Programmordner, sondern dort.

Die Auswahl der Ansichten trifft `YabsViewProxy` (`registerModernView`); alle anderen Ansichten bleiben unverändert.

## E-Rechnung (XRechnung / ZUGFeRD)

YaBS erstellt E-Rechnungen nach EN 16931 und prüft sie mit dem offiziellen Validator.
Ausführlich, mit Rechtslage und Checkliste: **[docs/E-Rechnung.md](E-Rechnung.md)**

- **Formate:** ZUGFeRD (PDF mit eingebetteter E-Rechnung), XRechnung (XML) oder beides, erstellt über die Schaltfläche *E-Rechnung* in der Rechnung.
- **Versand per E-Mail, pro Kunde einstellbar** (Kontakt, Reiter *E-Rechnung*): PDF (normal), ZUGFeRD, XRechnung oder PDF + XRechnung. Gilt für den Einzel- und den Serienversand. Ungültige E-Rechnungen werden nicht verschickt; der Grund wird angezeigt bzw. im Protokoll des Serienversands aufgeführt.
- **Sammelrechnungen** (*Vorgangssuche*, Rechtsklick *Zu neuer Rechnung zusammenfassen*): optional E-Rechnungen gemäß Kundeneinstellung als Dateien erzeugen und/oder die neuen Rechnungen direkt per E-Mail senden (mit Sicherheitsabfrage).
- **Firmendaten:** Pflichtfelder sind in der Firmeninformation mit `*` markiert. USt-IdNr. und IBAN werden beim Eingeben geprüft.
- **Zahlungsziel:** Das Fälligkeitsdatum in der E-Rechnung kommt
  1. vom Kunden (Kästchen *Sofort fällig* im Reiter *E-Rechnung*, sonst Zahlungsbedingung), sonst
  2. aus der Rechnungsvorlage (*Werkzeuge > Einstellungen > Vorlagen Manager*, Feld *Zahlungsziel* (Tage; englisch *Pay days*)), sonst
  3. aus dem Standard in der Firmeninformation, sonst
  4. 14 Tage.

  0 Tage in Vorlage oder Firmeninformation (bzw. *Sofort fällig* beim Kunden) heißt: fällig am Rechnungsdatum, Zahlungsbedingung „Zahlbar sofort ohne Abzug“.

  Wer das Zahlungsziel als Text in Vorlagen pro Kundengruppe pflegt, trägt die Tage dort zusätzlich ein, damit PDF und E-Rechnung übereinstimmen.
- **Mehrere Bankkonten, Zahlungsziel und Skonto pro Kunde:** Weitere Konten unter *Firmeninformation > Weitere Bankkonten…*. Im Kontakt, Reiter *E-Rechnung*, wird festgelegt:
  - auf welches Konto der Kunde überweist,
  - sein Zahlungsziel oder *Sofort fällig*,
  - sein Skonto (% und Tage).

  Leer bedeutet jeweils: Standard aus der Firmeninformation (Konto aus IBAN/BIC, Felder *Zahlungsziel*, *Skonto %*, *Skonto Tage*). Die Werte erscheinen in der E-Rechnung (Skonto im XRechnung-Format `#SKONTO#…#`) und als Felder in den Vorlagen:

  | Feld | Inhalt |
  |---|---|
  | `item.paymentiban`, `item.paymentbic`, `item.paymentbank`, `item.paymentaccountname` | Konto, auf das der Kunde zahlt |
  | `item.paymentdays`, `item.paymentduedate` | Zahlungsziel in Tagen, Fälligkeitsdatum |
  | `item.paymentterms` | „Zahlbar bis 29.11.2026“ bzw. „Zahlbar sofort ohne Abzug“ |
  | `item.skontopercent`, `item.skontodays`, `item.skontodate` | Skonto: Prozent, Tage, Datum |
  | `item.skontoamount`, `item.skontopayamount` | Skontobetrag, Zahlbetrag mit Skonto |

  Ohne Skonto sind die Skonto-Felder leer.
- **Ablage und Dateinamen wie bei den PDFs:** Der Speichern-Dialog der E-Rechnung schlägt denselben Ordner und Dateinamen vor wie der PDF-Export (*Benutzereinstellungen > Filename Defaults*). Mail-Anhänge heißen ebenso.
- **Tipp: Rechnungen automatisch archivieren (Bcc).** YaBS kann jede versendete E-Mail – einzeln und im Serienversand, mit allen Anhängen (PDF, ZUGFeRD, XRechnung) – zusätzlich als Bcc an ein eigenes Archivpostfach senden. Einrichten unter *Werkzeuge > Einstellungen > Benutzereinstellungen*, Bereich E-Mail, Feld **„Bcc (alle E-Mails)“** (mehrere Adressen mit `;` trennen; gilt für alle Benutzer). Der Kunde sieht diese Adresse nicht. Details: [docs/E-Rechnung.md](E-Rechnung.md#archivierung).

## Einstellungen und Bedienung

- **Linux unter Windows (WSL):** Das Hauptfenster bleibt nicht mehr grau, bis es verschoben wird – YaBS setzt unter WSL automatisch `_JAVA_AWT_WM_NONREPARENTING=1`.
- **Dateinamengrundeinstellung** (*Benutzereinstellungen*): Variablen und `/` (Unterordner) über die Auswahl *An der Cursorposition einfügen* einsetzen; eine Vorschau zeigt Pfad und Dateinamen. Alle Variablen: [Wiki: File name defaults](https://github.com/anti43/openyabs/wiki/File-name-defaults).
- **Journal, Rechtsklick *PDF* / *ODT*:** speichert wie der Beleg selbst – Ordner und Dateiname aus der Dateinamengrundeinstellung, Eintrag in der Historie. Eine Abfrage zeigt Anzahl und Zielordner.
- **EPC-QR-Code (GiroCode)** auf Rechnungen: Bild mit dem Namen `QRCode.img` in der ODT-Vorlage; die mitgelieferte Vorlage enthält es. Details: [QR-Code](QR-Code.md).
- **Mitgelieferte Rechnungsvorlage:** Hat ein Benutzer bei der Anmeldung keine Rechnungsvorlage, wird die Demo-Vorlage eingelesen und „Alle Gruppen“ zugeordnet (einmal pro Benutzer).
- **SEPA-Lastschrift statt DTAUS:** Im Journal unbezahlte Rechnungen auswählen, Rechtsklick *SEPA-Lastschriftdatei erstellen*. Fehlen IBAN oder Gläubiger-ID (*Firmeninformation*), erscheint nur der Hinweis, keine Datei. Voraussetzungen: [SEPA-Lastschrift](SEPA-Lastschrift.md). Die früheren *Kontoeinstellungen* der Benutzereinstellungen (nur für DTAUS) sind entfernt.
- **Journal-Menü:** *Per E-Mail senden* ist jetzt übersetzt.
- **Benutzereinstellungen, *Datum in Belegen* und *Export (Vorlagen und Journal)*:** Jede Option hat einen klaren Namen und darunter eine Erklärung, was sie an und aus bewirkt. Die wirkungslose Option „Externe Office Dokumente über URL laden“ ist entfernt; „Nur echte Positionen nummerieren“ zeigt den tatsächlichen Stand (Standard: an).
- **Benutzereinstellungen, *Neue Positionen*** (früher „Rechnung Standardwerte“): Einheit, Anzahl und MwSt.-Satz jeder neuen Position; darunter, aus welchen Artikelfeldern der Text einer Position entsteht (Kästchen wirken sofort, mit Vorschau). Das unbenutzte Feld „Ship Tax“ ist entfernt.
- **Positionen im Beleg:** Die Tabelle nutzt die ganze Breite, Spalten sind so breit wie Überschrift und Inhalt, die Spaltennamen stimmen (Anzahl, Artikel, Einheit, Text, Rabatt %, Nettopreis, MwSt %, Gesamt). Statt der Schaltfläche „A“ gibt es per Rechtsklick: *Artikel wählen…* (bzw. *Auftrag einfügen…*), Zeile darüber/darunter einfügen, Duplizieren, nach oben/unten, Kopieren, Einfügen, Zeile leeren, Entfernen.
- **Regional und Sprache, *Währung, Zahlen- und Datumsformat*:** Zur Auswahl stehen nur Formate mit Land (das Land bestimmt die Währung); ein Beispiel zeigt Zahl, Betrag und Datum. Das Datumsformat gilt überall, auch für „Zahlbar bis …“ in E-Rechnungen. Neue Benutzer übernehmen das aktuelle Format (früher das ungültige `en_UK`, das ohne Währungssymbol anzeigte).
- **Skriptmanager** (*Systemsteuerung*): Auswahl *Verwenden als* (Vorlagenfeld, vor/nach dem Speichern, nach dem Anlegen, vor dem Löschen, Name in Listen) mit Erklärung, was der Name bewirkt; ausführliche Hilfe im Fenster; *Werkzeugleisten-Aktionen…* (früher *Ansicht*) und *Dokumentation* als Schaltflächen. Mehrzeilige Skripte laufen jetzt (vorher nur einzeilige). Details: [Skripte](Scripts.md).
- **Groovy-Plugins bearbeiten** (*Systemsteuerung › Plugin-Manager*): Der Editor in YaBS stürzte mit `NoClassDefFoundError … NodeWithPublicModifier` ab – einige Bibliotheken (JavaParser, ASM, groovy-swing …) wurden mitgeliefert, aber beim Start mit `java -jar` nicht gefunden. Behoben; fehlt dennoch etwas, schaltet der Editor nur die Hervorhebung ab. Eine leere Plugin-Datei öffnet mit einem lauffähigen Grundgerüst.
- **Lagerverwaltung** jetzt in der Navigation unter *Artikel* (nicht mehr unter *Werkzeuge*): Barcode scannen oder Code eingeben, Enter – der Artikel wird sofort gesucht, jeder weitere Scan zählt hoch. *Zugang* oder *Abgang* wählen, *Buchen* ändert den Lagerbestand. Die Tabelle zeigt Bestand jetzt und neuen Bestand; nicht gefundene Codes sind rot.
- **Datenbank sichern, als SQL exportieren, Sicherung zurückspielen** (früher *Werkzeuge › Migration*): jetzt in der *Systemsteuerung*, Gruppe *Datenbank*, mit Erklärung.
- **Lokale Einstellungen, Ordner:** Unter dem Ablage- und dem Sicherungsordner steht, welcher Ordner gerade benutzt wird – auch wenn das Feld leer ist (dann der zuletzt gewählte Ordner). Sicherungen und Datenbank-Exporte beginnen ohne Sicherungsordner nicht mehr im Programmordner. Die Proxy-Felder zeigen die gespeicherten Werte (wurden bisher leer bzw. falsch angezeigt); die Einstellungsdatei enthält jede Verbindung nur noch einmal.
- **Standardschriftart** (*Systemsteuerung › Standardschriftart*): *Anwenden* ändert die offenen Fenster sofort; alle Fenster und Dialoge nutzen die Schrift erst **nach einem Neustart**. Darauf weist die Karte hin, und nach *Speichern* erscheint ein Hinweis. Die Schrift gilt für diesen Computer, die Größe ändert *Ansicht › Zoom*.
- **Umstieg von Fakturama 2:** *Datei › Import › Fakturama 2* – siehe [README](README.de.md#import-export-und-umstieg-von-anderer-software).

### Settings and usage (EN)

- **WSL:** the main window is no longer grey until moved (`_JAVA_AWT_WM_NONREPARENTING=1` is set automatically).
- **File name defaults:** insert variables and `/` from a list, with a preview. Variables: [wiki](https://github.com/anti43/openyabs/wiki/File-name-defaults).
- **Journal PDF/ODT:** saved like from the document – folder and name from the file name defaults, history entry.
- **EPC QR code (GiroCode):** image named `QRCode.img` in ODT templates – [QR-Code](QR-Code.md).
- **Demo invoice template:** imported and assigned to "All Groups" at login if a user has no invoice template.
- **SEPA direct debit** replaces DTAUS (journal, right click); requirements: [SEPA-Lastschrift](SEPA-Lastschrift.md). The user properties' *Account settings* are gone.
- **User properties:** *Document dates*, *Export* and *New positions* explain every option; unused options removed.
- **Positions:** full-width table, correct column names, right-click menu instead of the "A" button.
- **Script manager:** *Use as* choice with an explanation of what the name does, help text, *Toolbar actions…* and *Documentation* buttons; multi-line scripts run now. Details: [Scripts](Scripts.md).
- **Groovy plugin editor:** no longer crashes with `NoClassDefFoundError … NodeWithPublicModifier` (runtime libraries were missing from the jar's class path); an empty plugin file opens with a working skeleton.
- **Stock management** is in the navigation under *Products*: scan or type a code, Enter; stock in or out, *Book*.
- **Back up / export as SQL / restore** (was *Tools › Migration*): in the *Control Panel*, group *Database*.
- **Local options, folders:** each folder field says which folder is used now, also when empty (the folder chosen last); backups no longer start in the program folder; proxy fields show the saved values.
- **Default font:** *Apply* changes the open windows; all windows use it only after a restart – the card says so, and a message appears after *Save*.
- **Regional settings:** only formats with a country (currency), an example of number, amount and date; dates follow the format everywhere.
