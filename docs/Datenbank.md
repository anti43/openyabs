# Datenbank: H2, Derby, Verbindungen und Sicherung

*English summary at the end.*

## Welche Datenbank?

| | Verwendung |
|---|---|
| **H2** (Datei-Datenbank) | **Standard für neue Installationen** „Auf diesem Computer“. Dateien: `<Ordner>/<Name>.mv.db` (dazu ggf. `<Name>.trace.db`). |
| **Apache Derby 10.17** | Bestehende Installationen; wird weiter unterstützt (Derby wird nicht mehr weiterentwickelt). Die Datenbank ist ein Ordner `<Ordner>/<Name>`. |
| **MySQL / MariaDB** | Server für mehrere Arbeitsplätze (Einrichtung im Einrichtungsassistenten unter „Auf einem Datenbank-Server“). |

**Wo liegt die Datenbank?** Neue Installationen schlagen `<Benutzerordner>/.yabs/h2` vor (Windows `C:\Users\<Name>\.yabs\h2`, Linux `/home/<name>/.yabs/h2`, macOS `/Users/<name>/.yabs/h2`); der Ordner kann im Einrichtungsassistenten mit *Ändern …* gewählt werden. Der Programmordner `~/.yabs` (Einstellungen, Cache, Sicherungen) lässt sich mit `-appdir <Ordner>` ändern.

### H2 im Detail

- H2 verwendet dieselbe Struktur und dieselben Updates wie Derby, automatisch umgewandelt (`mpv5.db.common.H2Dialect`): Derby-Kompatibilitätsmodus, **SMALLINT-Spalten werden BIGINT** (keine Überläufe), `LONG VARCHAR` → `VARCHAR(32700)`, `RENAME COLUMN` und Identity-Syntax angepasst.
- Derbys SQL-Trigger (Suchindex, Papierkorb) laufen in H2 über die Java-Klasse `H2Trigger`; ihre Anweisungen stehen in der Tabelle `yabstriggers`.
- Updates beim Start funktionieren wie bei Derby: unterbrochene Updates setzen fort, bereits vorhandene Änderungen werden übersprungen.
- Eine bestehende Derby-Datenbank wird **nicht** automatisch nach H2 umgezogen; beide Varianten laufen nebeneinander (je eine Verbindung).

### Entwicklermodus (`-devmode`)

```bash
java -jar openyabs.jar -devmode
```

(oder `-Dyabs.devmode=true`): H2 läuft **nur im Arbeitsspeicher**, die Einstellungen liegen in einem **temporären Ordner** (z. B. `/tmp/yabs-dev…`). Nichts wird dauerhaft gespeichert, der Einrichtungsassistent erscheint bei jedem Start und zeigt statt eines Ordners den Hinweis „Entwicklermodus … nur im Arbeitsspeicher“. Mit `-appdir <Ordner>` bleiben die Einstellungen erhalten, die Datenbank bleibt trotzdem im Speicher. Für eine echte Installation YaBS **ohne** `-devmode` starten.

## Verbindungs-Manager

*Datei › Verbindungs-Manager* (und beim Start, wenn mehrere Verbindungen angelegt sind):

- Liste aller Datenbanken mit Name, Art (H2/Derby/MySQL) und Ort; die aktuelle ist markiert, eine fehlende Datenbank rot. Der ganze Pfad erscheint als Tooltip.
- **Verbinden:** Benutzer und Passwort eingeben – YaBS wechselt zur gewählten Datenbank und meldet an. Klappt das nicht, bleibt die bisherige Verbindung aktiv. *Benutzer merken* füllt den Namen beim nächsten Mal vor.
- **Neue Datenbank …:** Name eingeben, dann richtet der Einrichtungsassistent die Datenbank ein (ohne Neustart); danach sind Sie als `admin` (Passwort `password`) angemeldet. Abbrechen entfernt den leeren Eintrag.
- **Vorhandene hinzufügen …:** eine vorhandene H2-Datei (`.mv.db`) oder einen Derby-Ordner wählen – Art und Name werden erkannt. Es wird nichts angelegt oder geändert. (MySQL-Verbindungen werden angezeigt und funktionieren, können hier aber vorerst nicht neu hinzugefügt werden.)
- **Umbenennen**, **Entfernen** (nur der Eintrag; die Datenbankdateien bleiben).

Die Verbindungen stehen in der Einstellungsdatei `~/.yabs/settings<Version>.yabs`.

## Zip-Sicherung und Wiederherstellen

*Systemsteuerung › Datenbank › Zip-Sicherung* (nur für lokale H2- und Derby-Datenbanken, nicht im Entwicklermodus):

- **Jetzt sichern:** Die Datenbankverbindung wird geschlossen, die Datenbankdateien werden als `<Name>-<JJJJMMTT-HHMMSS>.zip` in den Sicherungsordner geschrieben, danach wird wieder verbunden.
- **Sicherungsordner** mit Variablen `[HOME] [APPDIR] [DBNAME] [YEAR] [MONTH] [DAY]`, Standard `[APPDIR]/backups/[DBNAME]`; der aufgelöste Pfad wird angezeigt.
- **Sicherungen behalten:** Standard 3 – ältere Zip-Dateien mit Zeitstempel werden gelöscht.
- **Bei jedem Beenden sichern** (optional).
- **Sicherung wiederherstellen …** überschreibt **nie** die aktuelle Datenbank: Die Zip-Datei wird neben sich entpackt (Ordner mit demselben Namen) und als neue Verbindung **„Sicherung <Zeitstempel>“** angelegt. Wechseln über den Verbindungs-Manager.

Die Einstellungen gelten je Verbindung.

### Vorübergehend ausgeblendet

*Datenbank sichern* (Derby-Online-Sicherung), *Als SQL exportieren* (MySQL-Dump), *Sicherung zurückspielen* und das Menü *Werkzeuge › Migration* sind ausgeblendet, bis sie mit H2 getestet sind. Zum Sichern und Wiederherstellen dient die Zip-Sicherung.

---

## English summary

- **New installations use H2**, stored in `<user home>/.yabs/h2` (`<name>.mv.db`); Derby databases keep working. H2 runs the Derby schema and updates, converted (SMALLINT → BIGINT, Derby triggers via the Java trigger `H2Trigger`).
- **Developer mode:** `java -jar openyabs.jar -devmode` (or `-Dyabs.devmode=true`) – H2 in memory only, settings in a temporary folder (e.g. `/tmp/yabs-dev…`); nothing is kept, the setup assistant appears on every start. Start without `-devmode` for a real installation.
- **Connection Manager** (*File › Connection Manager*): list of all databases, sign in and connect in one step (falls back to the previous one on failure), new database via the setup assistant without restart, add an existing H2/Derby database, rename, remove (only the entry).
- **Zip backup** (*Control Panel › Database*): closes the database, zips it as `<name>-<yyyyMMdd-HHmmss>.zip` into a folder with variables (default `[APPDIR]/backups/[DBNAME]`), reconnects; keeps 3 by default; optionally at every exit. Restore unzips next to the zip and adds a new connection "Backup <timestamp>" – nothing is overwritten.
- Derby backup, SQL export, restore and *Tools › Migration* are hidden until they are tested with H2.
