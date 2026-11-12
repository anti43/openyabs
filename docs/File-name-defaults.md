# Dateinamengrundeinstellung

*Kurzfassung; ausführlich auf Englisch unten (full English version below).*

*Systemsteuerung → Benutzereinstellungen → Dateinamengrundeinstellung*

Das Format bestimmt, **wo** exportierte Dateien (PDF, ODT, ...) gespeichert werden und **wie sie heißen**. Es gilt für den aktuellen Benutzer.

- Alles **vor dem letzten `/`** ist der Ordner, unterhalb des **Basisverzeichnisses** aus *Systemsteuerung → Lokale Einstellungen* (sonst unterhalb Ihres Benutzerordners). Fehlende Ordner werden angelegt.
- Der **letzte Teil** ist der Dateiname; die Endung (`.pdf`, ...) wird angehängt. In Dateinamen unzulässige Zeichen werden durch `_` ersetzt.
- Text und Variablen lassen sich beliebig kombinieren; Groß-/Kleinschreibung der Variablen ist egal.
- **Leeres Format:** Ordner und Dateiname werden jedes Mal gewählt.

Mit der Auswahl *An der Cursorposition einfügen* fügen Sie eine Variable oder einen `/` ein; der blaue Kasten zeigt eine Vorschau mit Beispielwerten.

Beispiel: `[YEAR]/[type]/[cname]_[contact.cname]` ergibt `…/2026/Rechnung/RE-00042_Meier.pdf`.

Die Variablen sind oben in den Tabellen beschrieben.

---

# File name defaults (EN)

*Control Panel → User properties → Filename Defaults*

The format decides **where** exported files (PDF, ODT, ...) are saved and **how they are named**. It applies to the current user.

## How it works

- Everything **before the last `/`** is the folder. It is created below the **base directory** set in *Control Panel → Local options*; if none is set, below your home folder. Missing folders are created automatically.
- The **last part** is the file name. The extension (`.pdf`, ...) is added automatically. Characters not allowed in file names (`\ / : * ? " < > |`) are replaced by `_`.
- Text and variables can be combined freely. Variables are written in square brackets; upper/lower case does not matter.
- **Empty format:** you choose the folder and file name each time.

Use the dropdown *Insert at cursor* to add a variable or a `/` at the cursor position. The blue box shows a preview with sample values.

## Examples

| Format | Result (base directory `/home/me/yabs`) |
|---|---|
| `[cname]` | `/home/me/yabs/RE-00042.pdf` |
| `[YEAR]/[cname]` | `/home/me/yabs/2026/RE-00042.pdf` |
| `[YEAR]/[type]/[cname]_[contact.cname]` | `/home/me/yabs/2026/Invoice/RE-00042_Smith.pdf` |
| `Customers/[contact.cnumber]/[YEAR]-[MONTH]_[cname]` | `/home/me/yabs/Customers/K-00007/2026-11_RE-00042.pdf` |
| `[YEAR]/Q[QUARTER]/[type]_[cname]` | `/home/me/yabs/2026/Q4/Invoice_RE-00042.pdf` |

## Variables

### Document (invoices, offers, orders, ...)

| Variable | Meaning |
|---|---|
| `[cname]` | Number of the document, e.g. `RE-00042` |
| `[type]` | Type of the document, e.g. `Invoice`, `Offer` |
| `[contact.cname]` | Name of the contact |
| `[contact.prename]` | First name of the contact |
| `[contact.company]` | Company of the contact |
| `[contact.cnumber]` | Number of the contact (customer number) |
| `[contact.title]` | Title of the contact |
| `[contact.country]` | Country of the contact |

Any other field of the document can be used by its name as well, e.g. `[description]`.

### Date and user (all objects)

| Variable | Meaning |
|---|---|
| `[YEAR]` | Current year, e.g. `2026` |
| `[YEAR_SHORT]` | Current year, two digits, e.g. `26` |
| `[MONTH]` | Current month, number |
| `[MONTH_NAME]` | Current month, name |
| `[DAY]` | Current day of the month |
| `[QUARTER]` | Current quarter, `1` - `4` |
| `[DATENUMERIC]` | Current date as number |
| `[OBJECT_YEAR]` | Year the document was created |
| `[OBJECT_MONTH]` | Month the document was created |
| `[GROUP_NAME]` | Group of the document |
| `[CURRENT_USER]` | User who exports |
| `[CREATE_USER]` | User who created the document |
