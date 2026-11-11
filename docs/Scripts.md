# Scripts (Script manager)

*Control Panel › Script manager* – small Groovy scripts for a kind of record (invoices, offers, contacts, products, …) and a group.

*Deutsche Fassung unten.*

## What a script does: its name

| Name ("Use as") | When | What it can do |
|---|---|---|
| any name, e.g. `greeting` | when exporting with a template | its result is the template field **`<property.greeting>`** – the data is **not** changed |
| `modifyOnSave` | before a record is saved | **change the data**, e.g. clean up a text |
| `executeAfterSave` | after saving | e.g. a notice, another record |
| `executeAfterCreate` | after a record is created | e.g. defaults |
| `modifyOnDelete` | before deleting | throw an exception to stop the deletion |
| `IntItemLabel` | when a document is shown | its result is the name of documents in lists |
| `IntRevenueLabel` | when a revenue is shown | its result is the name of revenues in lists |

Choose the purpose with **Use as**; a line below the name says what the name does.

## In a script

| | |
|---|---|
| `dbo` | the record (e.g. the invoice) |
| `getObject('contact', 'Smith')` | another record by kind and name |
| `getFormField('item.netvaluef')` | a template field of the record |

The last value is the result. Scripts may span several lines.

## Examples

Template field `greeting` for invoices:

```groovy
def name = getFormField('item.contact.cname')
name ? "Dear ${getFormField('item.contact.prename')} ${name}" : "Dear Sir or Madam"
```

In the template: `<property.greeting>`.

`modifyOnSave` for products – no spaces around the name:

```groovy
dbo.setCname(dbo.__getCname().trim())
```

## Test

Write the script, then **Test with a record…** – it runs on a record you choose and shows the result.

## Toolbar actions

**Toolbar actions…** (button in the script manager; was *View › Add toolbar action*): own buttons in a toolbar that run a Groovy script. In these scripts: `MPView` (the main window), `Application`, `QueryHandler`. Takes effect after a restart.

---

# Skripte (Skriptmanager) (DE)

*Systemsteuerung › Skriptmanager* – kleine Groovy-Skripte für eine Art von Datensatz (Rechnungen, Angebote, Kontakte, Artikel …) und eine Gruppe.

- **Beliebiger Name**, z.B. `anrede`: Vorlagenfeld **`<property.anrede>`** mit dem Ergebnis des Skripts – die Daten werden **nicht** geändert.
- **`modifyOnSave`**: vor dem Speichern, kann die Daten **ändern**. **`executeAfterSave`** / **`executeAfterCreate`**: nach dem Speichern / Anlegen. **`modifyOnDelete`**: vor dem Löschen (eine Exception verhindert es).
- **`IntItemLabel`** / **`IntRevenueLabel`**: Name von Belegen / Einnahmen in Listen.
- Im Skript: `dbo` = der Datensatz, `getObject('contact', 'Name')`, `getFormField('item.netvaluef')`. Der letzte Wert ist das Ergebnis; mehrzeilige Skripte sind möglich.
- **Mit einem Datensatz testen…** führt das Skript für einen gewählten Datensatz aus.
- **Werkzeugleisten-Aktionen…** (früher *Ansicht › Werkzeugleiste anpassen*): eigene Schaltflächen, die ein Skript ausführen (`MPView`, `Application`, `QueryHandler`); nach einem Neustart.
