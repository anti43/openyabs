# E-Rechnung in YaBS

Diese Seite beschreibt, was die E-Rechnungspflicht in Deutschland für den
Rechnungsversand bedeutet, wie YaBS E-Rechnungen erstellt und prüft und was
beim **Einzel- und Serienversand per E-Mail** zu beachten ist.

> **Hinweis:** Dies ist keine Rechts- oder Steuerberatung. Die rechtlichen
> Angaben geben den Stand Ende 2026 vereinfacht wieder. Maßgeblich sind § 14 UStG,
> das BMF-Schreiben zur E-Rechnung und die Auskunft Ihrer Steuerberatung.

---

## 1. Rechtlicher Rahmen (B2B, Inland)

| Zeitraum | Ausstellen von Rechnungen an Unternehmen |
|---|---|
| seit 1.1.2025 | Alle Unternehmen müssen E-Rechnungen **empfangen** können. |
| bis 31.12.2026 | Papierrechnungen weiter erlaubt; PDF und andere elektronische Formate nur mit Zustimmung des Empfängers. |
| bis 31.12.2027 | Wie oben, aber nur noch für Unternehmen mit höchstens 800.000 € Vorjahresumsatz. |
| ab 1.1.2028 | E-Rechnung für alle Pflicht. |

- **Was als E-Rechnung gilt:** ein strukturiertes Format nach der europäischen Norm **EN 16931**, z.B. **XRechnung** oder **ZUGFeRD** ab Version 2.0.1 (Profil EN16931 oder höher). Ein reines PDF ist **keine** E-Rechnung, sondern eine „sonstige Rechnung“.
- **Ausnahmen** (weiterhin auch als PDF/Papier):
  - Rechnungen an Privatpersonen (B2C),
  - Kleinbetragsrechnungen bis 250 € brutto,
  - Fahrausweise.
- **Kleinunternehmer:** Kleinunternehmer (§ 19 UStG) müssen E-Rechnungen empfangen, aber keine ausstellen.
- **Öffentliche Auftraggeber (B2G):** Sie verlangen schon länger XRechnungen und dafür eine **Leitweg-ID**. Sie nehmen Rechnungen meist über Portale (z.B. ZRE, OZG-RE) oder Peppol entgegen. Ob eine E-Mail an den Auftraggeber genügt, bitte dort erfragen.
- **Maßgeblich und Aufbewahrung:** Rechtlich maßgeblich ist der **strukturierte Teil** (das XML). Bei ZUGFeRD müssen PDF und XML inhaltlich übereinstimmen. E-Rechnungen sind im Originalformat aufzubewahren (GoBD, derzeit 8 Jahre für Buchungsbelege).

---

## 2. Was YaBS erstellt

| Format | Datei | Geeignet für |
|---|---|---|
| **ZUGFeRD** (Profil EN16931) | PDF/A-3 mit eingebettetem XML | Kunden, die ein lesbares PDF *und* eine E-Rechnung wollen. In der Regel die beste Wahl für B2B. |
| **XRechnung** | XML (UN/CEFACT CII) | Öffentliche Auftraggeber, Buchhaltungssoftware, Portale |
| **PDF + XRechnung** | zwei Anhänge | Kunden, die beides getrennt möchten |
| PDF (normal) | PDF aus der Rechnungsvorlage | Privatkunden, Kleinbeträge, Übergangszeit |

E-Rechnungen werden nur für **Rechnungen, Abschlagsrechnungen** (Typ 326)
und **Anzahlungsrechnungen** erstellt. Anzahlungsrechnungen tragen bei ZUGFeRD
den Typ 386; bei XRechnung den Typ 380, weil XRechnung 386 nicht erlaubt.
Angebote, Aufträge, Lieferscheine usw. werden immer als PDF versendet.
**Gutschriften** (Typ 381) werden derzeit nicht als E-Rechnung unterstützt.

### Wo die Daten herkommen

- **Verkäufer:** *Werkzeuge > Einstellungen > Firmeninformation*. Pflichtfelder für E-Rechnungen sind dort mit `*` markiert.
  - Firma oder Name, Straße, PLZ, Ort, Land
  - Telefon und E-Mail (Kontaktdaten des Verkäufers, BR-DE-5/6/7)
  - **USt-IdNr.** mit Ländercode, z.B. `DE123456789`. Eine Steuernummer allein genügt **nicht** (BR-CO-26). Steht die USt-IdNr. versehentlich im Feld „Steuernummer“, wird sie trotzdem erkannt.
  - **IBAN** (wird geprüft), BIC optional
- **Käufer:** Rechnungsadresse des Kontakts (Firma/Name, Straße, PLZ, Ort, Land) und die **E-Mail-Adresse des Kontakts**, die in der E-Rechnung Pflicht ist (PEPPOL-EN16931-R010).
  - **USt-IdNr. des Kunden** (Feld „UST-ID“): nur eine gültige USt-IdNr. wird übernommen. Eine Steuernummer ist für den Käufer in EN 16931 nicht vorgesehen und wird weggelassen. YaBS weist darauf hin.
  - **Leitweg-ID / Käuferreferenz** (Reiter „E-Rechnung“ beim Kontakt): Pflicht für öffentliche Auftraggeber. Ist sie leer, wird die Kundennummer verwendet (sonst die Rechnungsnummer).
- **Fälligkeit:** Rechnungsdatum + **Zahlungsbedingung (Tage) des Kontakts**. Ohne eigene Zahlungsbedingung gelten 14 Tage.
- **Positionen:**
  - Textzeilen und Zeilen mit Menge 0 werden übersprungen.
  - Rabatte werden als Positionsnachlass übertragen.
  - Einheiten werden auf UN/ECE-Codes abgebildet: `Stk` → C62, `Std.` → HUR, `pauschal` → LS usw. Unbekannte Einheiten werden C62.
- **0 % USt.:** Positionen ohne Umsatzsteuer erhalten die Kategorie „E“ (steuerbefreit) mit dem Befreiungsgrund aus der Benutzereigenschaft `companyinfo.taxexemptionreason`. Dafür gibt es in der Firmeninformation noch kein eigenes Feld. Ohne diese Eigenschaft lautet der Grund „Umsatzsteuerbefreit“. Für Kleinunternehmer kann hier z.B. „Kein Ausweis von Umsatzsteuer, da Kleinunternehmer gemäß § 19 UStG“ eingetragen werden.

### Prüfung

YaBS prüft jede E-Rechnung zweimal:

1. **Vorprüfung der Daten:** Pflichtfelder, Format von USt-IdNr. und IBAN, Land. Jeder Hinweis nennt das Menü und das Feld, in dem korrigiert werden muss.
2. **Offizieller Validator** (Mustang, enthält die KoSIT-/EN16931-Regeln) prüft:
   - XML-Schema,
   - EN 16931- und XRechnung-Regeln,
   - bei ZUGFeRD zusätzlich die PDF/A-3-Konformität (veraPDF).

   Die Meldungen werden, soweit bekannt, in Handlungsanweisungen übersetzt (z.B. „USt-IdNr. im Format DE123456789 eintragen …“). Die Regelnummer steht jeweils in Klammern.

Eine einzelne Datei lässt sich jederzeit prüfen, z.B. vor dem manuellen Versand: in einer Rechnung
Schaltfläche **E-Rechnung > E-Rechnung prüfen…**.

---

## 3. Versand per E-Mail

### Einstellung pro Kunde

Im Kontakt, Reiter **„E-Rechnung“**, Feld **„Rechnungen per E-Mail“**:

- **PDF (normal):** Standard für alle Kontakte ohne eigene Einstellung
- **ZUGFeRD (PDF mit eingebetteter E-Rechnung)**
- **XRechnung (nur XML)**
- **PDF + XRechnung (XML)**

Die Einstellung und die Leitweg-ID werden beim **Speichern des Kontakts**
übernommen. Sie gelten für den Einzelversand aus der Rechnung und für den
Serienversand.

### Einzelversand (aus der Rechnung)

- **Ablauf:** YaBS erstellt die Anhänge im gewählten Format, prüft sie und sendet die E-Mail mit der gewählten E-Mail-Vorlage.
- **Daten unvollständig oder E-Rechnung ungültig:** Es wird **nichts gesendet**. YaBS zeigt die Gründe mit Korrekturhinweisen an.
- **Danach:** Daten korrigieren und erneut senden. Alternativ für diesen Kunden vorübergehend „PDF (normal)“ einstellen, wenn der Kunde zustimmt (siehe Übergangsregeln oben).

### Serienversand (Journal)

Im Journal Rechnungen markieren und per E-Mail versenden:

- **Format:** Jede Rechnung geht im Format des jeweiligen Kunden hinaus. In einem Lauf können also PDF-, ZUGFeRD- und XRechnung-Kunden gemischt sein.
- **E-Mail-Vorlage:** Sie wird einmal für den ganzen Lauf ausgewählt. Ihr Text gilt für alle Formate. Ein Satz wie „Im Anhang erhalten Sie Ihre Rechnung“ passt daher für alle.
- **Bereits bezahlte Rechnungen** werden wie bisher nicht versendet.
- **Unvollständige oder ungültige E-Rechnungen:**
  - Sie werden **übersprungen**; der Lauf geht mit der nächsten Rechnung weiter.
  - Das **Protokoll am Ende** (Fenster „Fertig“) listet jede übersprungene Rechnung mit Rechnungsnummer, Format und Grund.
  - Danach die Daten korrigieren und nur diese Rechnungen erneut senden.
- **Häufige Gründe, wenn viele Rechnungen übersprungen werden:**
  - eine fehlende USt-IdNr. in der Firmeninformation (betrifft *alle* E-Rechnungen),
  - fehlende E-Mail-Adressen oder Adressen bei einzelnen Kunden.
- **Dauer:**
  - Die Prüfung mit dem offiziellen Validator dauert pro E-Rechnung etwa 1–3 Sekunden.
  - Die erste Prüfung nach dem Programmstart dauert deutlich länger (die Regelwerke werden geladen).
  - Große Serien mit E-Rechnungen brauchen daher länger als reine PDF-Serien.
- **Dateinamen:**
  - Die Anhänge heißen wie die Rechnung, z.B. `R-2026-0042.pdf` / `R-2026-0042.xml`.
  - ZUGFeRD-Dateien sind PDFs und öffnen sich wie gewohnt.
- **E-Mail-Server:** E-Rechnungen sind etwas größer als einfache PDFs (ZUGFeRD ist PDF/A-3 mit eingebetteten Schriften). Bei sehr vielen Mails in kurzer Zeit können E-Mail-Anbieter Versandlimits haben.

### Archivierung

YaBS archiviert versendete E-Rechnungen **nicht automatisch**. Die
Aufbewahrung im Originalformat (siehe Abschnitt 1) ist Aufgabe des Absenders.
Zwei einfache Wege:

- **Bcc an ein eigenes Archivpostfach:** globale Eigenschaft `org.openyabs.exportproperty.mailbcc` unter *Werkzeuge > Einstellungen > Erweitert* (englisch: *Global Options*) setzen. Jede versendete Rechnung landet dann mit allen Anhängen im Archiv.
- **Datei zusätzlich speichern:** in der Rechnung über die Schaltfläche *E-Rechnung > XRechnung (XML)* bzw. *ZUGFeRD (PDF…)*.

---

## 4. Checkliste vor der Umstellung

1. **Firmeninformation vervollständigen:**
   - alle `*`-Felder,
   - USt-IdNr. mit `DE`,
   - gültige IBAN.

   Ungültige Werte werden rot markiert.
2. **Je Kunde:** Rechnungsadresse, E-Mail-Adresse und ggf. USt-IdNr. (nur USt-IdNr., keine Steuernummer) prüfen.
3. **Öffentliche Auftraggeber:** Leitweg-ID im Reiter „E-Rechnung“ eintragen und den Einreichungsweg klären (Portal, Peppol, E-Mail).
4. **Format wählen:** im Reiter „E-Rechnung“ das gewünschte Format einstellen. Für B2B-Kunden ist ZUGFeRD meist die passende Wahl.
5. **Testen:**
   - eine Testrechnung an sich selbst senden und den Anhang mit *E-Rechnung > E-Rechnung prüfen…* kontrollieren;
   - optional zusätzlich mit einem externen Validator prüfen, z.B. dem KoSIT-Validator oder dem Prüfwerkzeug Ihrer Buchhaltungssoftware.
6. **Rechnungsvorlage:** Für ZUGFeRD werden alle Schriften eingebettet. Ist eine Schrift der Vorlage nicht installiert, nimmt YaBS eine metrisch passende Ersatzschrift (z.B. Liberation statt Arial). Bei sehr speziellen Schriften die Vorlage auf eine installierte Schrift umstellen.

---

## 5. Technischer Überblick (für Entwickler)

| Klasse | Aufgabe |
|---|---|
| `mpv5.utils.export.ZugpferdFile` | erstellt die E-Rechnung (Mustang), Vorprüfung `validate()` |
| `mpv5.utils.export.EInvoiceValidator` | offizieller Validator, Meldungen → Hinweise |
| `mpv5.utils.export.EInvoiceDelivery` | Versandformat je Kontakt (ValueProperty `einvoice.delivery`) |
| `mpv5.utils.export.InvoiceMail` | erzeugt und prüft die Mail-Anhänge |
| `mpv5.utils.export.Export#mail` | Einzel- und Serienversand, wählt das Format |
| `mpv5.mail.SimpleMail` | Versand mit mehreren Anhängen |
| `mpv5.utils.text.Iban` | Prüfung von IBAN und BIC |

Die Leitweg-ID ist die ValueProperty `leitweg-id` am Kontakt oder an der
Rechnung; der Wert an der Rechnung hat Vorrang.
