openyabs - Yet another Business Software 
===

**[Download installation packages here!](https://github.com/anti43/openyabs/releases/)**
 

**[Die deutsche Dokumentation zu Yabs befindet sich hier!](https://docs.google.com/document/d/1VhUi0wS-EJgfV-o89Hl-2wux810mJnqUV1wPxpgVIs4/pub)**

**[Aktueller Build zum Download (latest-master)](https://github.com/andreasweberd/openyabs/releases/tag/latest-master)** – ZIP entpacken und mit `yabs.sh` / `yabs.bat` starten (Java 21 oder neuer).

## Datenbank (Apache Derby 10.17, Java 21)

YaBS nutzt Apache Derby 10.17.1.0, das Java 21 voraussetzt.

- **Ältere Datenbanken** werden beim ersten Start im *Soft-Upgrade*-Modus geöffnet und auf die aktuelle YaBS-Version aktualisiert. Das Dateiformat bleibt dabei unverändert, ältere YaBS-Versionen (Derby 10.8) können dieselbe Datenbank weiterhin öffnen.
- **Unterbrochene Updates** setzen beim nächsten Start dort fort, wo sie aufgehört haben. Bereits durchgeführte Änderungen werden übersprungen. Die Versionsnummer wird nach jedem Schritt gespeichert und bei einem Fehler nicht erhöht.
- **Beschädigte Datenbank** (z. B. „StoredPreparedStatement … ist nicht vorhanden“ und danach „Keine aktuelle Verbindung“): YaBS bietet an, alle Daten in eine neu angelegte Datenbank zu kopieren. Die alte Datenbank bleibt unverändert als `<name>-backup-<Datum>` daneben liegen. Die neu aufgebaute Datenbank hat das Derby-10.17-Format und kann **nicht mehr** mit älteren YaBS-Versionen geöffnet werden; die Sicherung schon. Bei großen Datenbanken dauert das einige Minuten.

Vor jedem Update empfiehlt sich eine Kopie des Datenbankordners.

## E-Rechnung (XRechnung / ZUGFeRD)

YaBS erstellt E-Rechnungen nach EN 16931 und prüft sie mit dem offiziellen Validator.
Ausführlich, mit Rechtslage und Checkliste: **[docs/E-Rechnung.md](docs/E-Rechnung.md)**

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
- **Tipp: Rechnungen automatisch archivieren (Bcc).** YaBS kann jede versendete E-Mail – einzeln und im Serienversand, mit allen Anhängen (PDF, ZUGFeRD, XRechnung) – zusätzlich als Bcc an ein eigenes Archivpostfach senden. Einrichten unter *Werkzeuge > Einstellungen > Benutzereinstellungen*, Bereich E-Mail, Feld **„Bcc (alle E-Mails)“** (mehrere Adressen mit `;` trennen; gilt für alle Benutzer). Der Kunde sieht diese Adresse nicht. Details: [docs/E-Rechnung.md](docs/E-Rechnung.md#archivierung).

===

If you have any question regarding Yabs, please write to the mailing list (english and german) <a href="mailto:openyabs@googlegroups.com">openyabs@googlegroups.com</a>

Mailing list archive:
https://groups.google.com/forum/#!forum/openyabs

Deutsche Beschreibung auf Heise:
http://www.heise.de/download/yabs-yet-another-business-software-1149250.html

Die Kunden- und Rechnungsverwaltungs-Software Yabs bringt alle notwendigen Funktionen mit, um alle Verwaltungsaufgaben mit Kunden, Angeboten und Rechnungen durchzuführen - und das parallel auf Windows, Linux und MacOs.
Das Programm enthält eine Kundenverwaltung, eine Rechnungsverwaltung mit Mahnfunktion und dem Export als PDF-Datei. Auch die Angebote können als PDF-Dateien exportiert und dann auch ausgedruckt werden. Ebenso beinhaltet das Paket eine Verwaltung der Lieferanten und kann Produktlisten importieren, sodass Ihnen viel Handarbeit erspart bleibt. Ebenso ist es möglich, die Listen als HTML-Dateien zu exportieren und Serienbriefe zu erstellen und auszudrucken.

Ubuntu Users:
http://wiki.ubuntuusers.de/Yabs

***
![](https://raw.githubusercontent.com/anti43/openyabs/master/src/mpv5/resources/images/icon2_128.png)


