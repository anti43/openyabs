# Vorlagen: Demo-Vorlagen und alle Felder

*English summary at the end.*

## Demo-Vorlagen

Neue Installationen bringen **18 Demo-Vorlagen** mit – eine je Vorlagenart, mit Platzhalter-Logo („Ihr Logo – Musterfirma GmbH“). Sie werden beim Einrichten importiert und dem Administrator für **Alle Gruppen** zugewiesen; im Vorlagen-Manager heißen sie „Demo - <Art>“.

| Art (Typ-Nr.) | Inhalt |
|---|---|
| Rechnung (0), Auftrag (1), Angebot (2), Lieferschein (3), Auftragsbestätigung (4), Vertrag (11), Serienbrief (13), Gutschrift (20), Abschlagsrechnung (21), Anzahlungsrechnung (22) | Beleg mit Positionstabelle und Summen; Felder `item.*`, `item.contact.*`, `companyinfo.*` |
| Artikeldatenblatt (5), Leistungsbeschreibung (6) | `product.*` |
| Mahnung (7) | `reminder.*` und der Beleg `item.*` |
| Kontaktdatenblatt (8) | `contact.*` |
| Journal (9) | Tabelle `xtable1` |
| Bestellung (10) | `productorder.*`, Lieferant `productorder.contact.*`, Positionen |
| Gesprächsnotiz (12) | `conversation.*`, `conversation.contact.*` |
| Tätigkeitsliste (14) | `activitylist.*`, `activitylist.contact.*`, Positionen |

- Die **Demo-Rechnung** wird importiert, aber nicht zugewiesen – die normale Rechnungsvorlage bleibt die aktive.
- Jede Demo-Vorlage hat am Ende eine Seite **„Verfügbare Felder – <Art>“**: links der Feldname, rechts der fertige Platzhalter zum Kopieren in eigene Vorlagen (in LibreOffice: Platzhalter kopieren und einfügen). Die Seite in der eigenen Vorlage einfach löschen.
- Das Logo ist ein Bild in der Kopfzeile: in LibreOffice anklicken, *Bild ersetzen*.
- Positionstabellen sind Tabellen mit dem Namen `xtable1` (Spalten nach dem Format der Vorlage, z. B. `1,2,4,5,7,9`).

## Bilder in Vorlagen

| Bildname | Inhalt |
|---|---|
| `QRCode.img` | EPC-QR-Code (GiroCode) auf Rechnungen – siehe [QR-Code](QR-Code.md) |
| `TSEQRCode.img` | QR-Code der TSE-Daten auf dem Kassenbon – siehe [Kasse und TSE](Kasse-TSE.md) |

Den Namen vergibt man in LibreOffice unter *Bild › Eigenschaften › Optionen › Name*. Ohne Daten wird das Bild entfernt.

## Demo-Vorlagen neu erzeugen

Die Vorlagen werden aus der deutschen Standardvorlage `src/main/resources/extra/DE-Yabs-2.0.odt` erzeugt; die Feldlisten kommen aus der Datenbankstruktur:

```bash
python3 tools/templates/gen.py .
```

Ergebnis: `src/main/resources/extra/templates/demo-XX.odt` (XX = Typ-Nr.). Logo: `tools/templates/logo.png`. Danach neu bauen; bestehende Installationen importieren die Vorlagen nicht automatisch (Vorlagen-Manager › Importieren).

---

## English summary

- New installations import **18 demo templates** (one per template type, named "Demo - <type>") with a dummy logo, assigned to the administrator for all groups; the demo invoice is imported but the regular invoice template stays assigned.
- Each demo template ends with a page **"Verfügbare Felder"** listing every field of its type with a ready-to-copy placeholder; delete the page in your own template.
- Image fields: `QRCode.img` (GiroCode on invoices), `TSEQRCode.img` (TSE QR code on receipts).
- Regenerate with `python3 tools/templates/gen.py .` (output `src/main/resources/extra/templates/demo-XX.odt`).
