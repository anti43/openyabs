# SEPA-Lastschrift (SEPA direct debit)

YaBS erstellt aus unbezahlten Rechnungen eine **SEPA-Lastschriftdatei** (XML, Format pain.008.001.08, SEPA-Basislastschrift). Diese Datei laden Sie im Online-Banking Ihrer Bank hoch; die Bank zieht die Beträge bei Ihren Kunden ein.

*English version below.*

## Voraussetzungen

### Bei Ihrer Bank

- Eine **Inkassovereinbarung** (Lastschriftvereinbarung) für SEPA-Basislastschriften. Ohne sie nimmt die Bank die Datei nicht an.
- Online-Banking, in dem Sie **SEPA-Sammeldateien (XML)** hochladen können (z.B. „Datei-Upload“, „Sammler hochladen“ oder eine Finanzsoftware mit Dateiimport).

### Gläubiger-Identifikationsnummer

- Eine **Gläubiger-ID** (z.B. `DE98ZZZ09999999999`). In Deutschland beantragen Sie sie kostenlos online bei der **Deutschen Bundesbank**.

### In YaBS: Firmeninformation

*Systemsteuerung › Firmeninformation*, Kachel *Bankverbindung und Zahlung*:

| Feld | Pflicht | Hinweis |
|---|---|---|
| Firma (oder Vorname und Name) | ja | Ihr Name als Zahlungsempfänger |
| IBAN | ja | Konto, auf dem die Beträge eingehen |
| BIC | nein | Kann leer bleiben |
| Gläubiger-ID (SEPA) | ja | Wird beim Tippen geprüft |

### In YaBS: Kontakte

| Feld | Pflicht | Hinweis |
|---|---|---|
| IBAN | ja | Konto des Kunden. Rechnungen von Kunden ohne gültige IBAN werden übersprungen und angezeigt. |
| BIC | nein | Kann leer bleiben |
| Kundennummer | ja | Ist die **Mandatsreferenz** in der Datei |

### Lastschriftmandat

Jeder Kunde muss Ihnen ein unterschriebenes **SEPA-Lastschriftmandat** erteilt haben. Wichtig:

- **Mandatsreferenz = Kundennummer** des Kontakts in YaBS. Tragen Sie die Kundennummer als Mandatsreferenz auf dem Mandat ein (bzw. teilen Sie sie dem Kunden so mit).
- **Datum des Mandats = Anlagedatum des Kontakts** in YaBS. Das Mandat sollte also spätestens an dem Tag erteilt sein, an dem der Kontakt angelegt wurde.
- Vor jedem Einzug muss der Kunde rechtzeitig informiert werden (**Pre-Notification**, i.d.R. 14 Tage vorher, kürzer, wenn vereinbart) – z.B. auf der Rechnung: „Der Betrag wird am … von Ihrem Konto IBAN … mit der Mandatsreferenz … und unserer Gläubiger-ID … eingezogen.“

## So geht's

1. **Journal** öffnen, die einzuziehenden **unbezahlten Rechnungen** auswählen.
2. Rechtsklick › **SEPA-Lastschriftdatei erstellen**, Speicherort wählen.
3. YaBS zeigt, welche Rechnungen in der Datei sind und welche übersprungen wurden.
4. Die Datei im **Online-Banking hochladen** und freigeben.
5. Nach dem Zahlungseingang die Rechnungen als bezahlt markieren (Rechtsklick › *'Bezahlt' Status setzen*).

## Was in der Datei steht

| | |
|---|---|
| Format | pain.008.001.08, SEPA-Basislastschrift (CORE), Sequenz „wiederkehrend“ (RCUR) |
| Fälligkeit | nächster Werktag (Montag – Freitag) nach dem Erstellen |
| Betrag | Bruttobetrag der Rechnung |
| Verwendungszweck | Art und Nummer, z.B. `Rechnung RE-00042` |
| Bezahlte Rechnungen | werden nicht aufgenommen |
| Sonderzeichen | in den SEPA-Zeichensatz umgewandelt (ä → ae, ß → ss, & → +) |

---

# SEPA direct debit (EN)

YaBS creates a **SEPA direct debit file** (XML, pain.008.001.08, CORE) from unpaid invoices. You upload it in your online banking; your bank collects the amounts from your customers.

## Requirements

- **Your bank**: a direct debit agreement (SEPA Core) and online banking that accepts SEPA XML batch files.
- **Creditor identifier** (e.g. `DE98ZZZ09999999999`); in Germany from the Deutsche Bundesbank.
- **Company Info** (*Control Panel › Company Info*, card *Bank account and payment*): business (or first name and name), **IBAN**, **Creditor ID (SEPA)**; BIC optional.
- **Contacts**: **IBAN** of the customer (BIC optional). Invoices of customers without a valid IBAN are skipped and listed.
- **Mandate**: every customer has signed a SEPA direct debit mandate. The **mandate reference is the customer number**, the **mandate date is the date the contact was created** in YaBS. Inform the customer before each collection (pre-notification, usually 14 days, or as agreed).

## How to

1. Open the **journal**, select the **unpaid invoices** to collect.
2. Right click › **Create SEPA direct debit file**, choose where to save it.
3. YaBS lists the invoices in the file and those skipped.
4. **Upload** the file in your online banking.
5. When the money has arrived, set the invoices to paid (right click › *Set to status: 'Paid'*).

The collection date is the next working day; the amount is the gross amount of the invoice; the reference is type and number of the invoice. Paid invoices are not included.
