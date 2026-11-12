# Datenbank: H2, Derby, Verbindungen und Sicherung

*English summary at the end.*

## Welche Datenbank?

| | Verwendung |
|---|---|
| **H2** (Datei-Datenbank) | **Standard für neue Installationen** „Auf diesem Computer“. Dateien: `<Ordner>/<Name>.mv.db` (dazu ggf. `<Name>.trace.db`). |
| **Apache Derby 10.17** | Bestehende Installationen; wird weiter unterstützt (Derby wird nicht mehr weiterentwickelt). Die Datenbank ist ein Ordner `<Ordner>/<Name>`. |
| **MySQL / MariaDB** | Server für mehrere Arbeitsplätze (Einrichtungsassistent › „Auf einem Datenbank-Server“ › MySQL / MariaDB, Adresse `host:3306`). |
| **PostgreSQL** | Server für mehrere Arbeitsplätze (Einrichtungsassistent › „Auf einem Datenbank-Server“ › PostgreSQL, Adresse `host:5432`). |

**Wo liegt die Datenbank?** Neue Installationen schlagen `<Benutzerordner>/.yabs/h2` vor (Windows `C:\Users\<Name>\.yabs\h2`, Linux `/home/<name>/.yabs/h2`, macOS `/Users/<name>/.yabs/h2`); der Ordner kann im Einrichtungsassistenten mit *Ändern …* gewählt werden. Der Programmordner `~/.yabs` (Einstellungen, Cache, Sicherungen) lässt sich mit `-appdir <Ordner>` ändern.

### H2 im Detail

- H2 verwendet dieselbe Struktur und dieselben Updates wie Derby, automatisch umgewandelt (`mpv5.db.common.H2Dialect`): Derby-Kompatibilitätsmodus, **SMALLINT-Spalten werden BIGINT** (keine Überläufe), `LONG VARCHAR` → `VARCHAR(32700)`, `RENAME COLUMN` und Identity-Syntax angepasst.
- Derbys SQL-Trigger (Suchindex, Papierkorb) laufen in H2 über die Java-Klasse `H2Trigger`; ihre Anweisungen stehen in der Tabelle `yabstriggers`.
- Updates beim Start funktionieren wie bei Derby: unterbrochene Updates setzen fort, bereits vorhandene Änderungen werden übersprungen.
- Eine bestehende Derby-Datenbank wird **nicht** automatisch nach H2 umgezogen; beide Varianten laufen nebeneinander (je eine Verbindung).

### Neue Datenbanken: eine Struktur-Datei statt aller Updates

Neue Datenbanken (H2, MySQL/MariaDB, PostgreSQL) werden in **einem Schritt** aus der Datei `src/main/resources/db/schema.sql` angelegt – der Struktur der aktuellen Version, neutral geschrieben. Die Updates laufen nur noch für bestehende Datenbanken, die älter sind.

- Ein reines ANSI-SQL für alle Datenbanken geht nicht ganz: Tabellen, Schlüssel und Indizes sind portabel, es unterscheiden sich Identitätsspalten (MySQL `AUTO_INCREMENT`), große Felder (PostgreSQL `BYTEA`/`TEXT`, MySQL `LONGBLOB`/`LONGTEXT`), Zeitstempel (MySQL `DATETIME`), lange Texte (MySQL: `TEXT`, Zeilen sind auf 65 535 Byte begrenzt) und vor allem die **Trigger** für Suchindex und Papierkorb. Darum gibt es **eine** neutrale Datei und je Datenbank einen kleinen Umsetzer (`mpv5.db.common.SqlDialect`):
  - **H2:** Trigger über die Java-Klasse `H2Trigger`.
  - **PostgreSQL:** je Trigger eine PL/pgSQL-Funktion.
  - **MySQL/MariaDB:** Zeilen-Trigger; das Aufräumen des Papierkorbs ist Teil der Papierkorb-Trigger jeder Tabelle (MySQL-Trigger dürfen die eigene Tabelle nicht ändern).
- Derby verwendet weiter die alte Struktur und die Updates.
- **Für Entwickler – ein neues Datenbank-Update:** das Update in `DatabaseUpdater` eintragen (Derby- und MySQL-Syntax; PostgreSQL in `UPDATES_POSTGRES`), `Constants.DATABASE_VERSION` erhöhen, dann `./gradlew generateSchema`. Der Test `SchemaTest` schlägt fehl, solange die Datei nicht passt; mit `YABS_TEST_POSTGRES` / `YABS_TEST_MYSQL` (JDBC-URL einer leeren Datenbank, z. B. `jdbc:postgresql://localhost:5432/yabs?user=yabs`) prüft er die Datei auch auf PostgreSQL und MySQL, `AppOnPostgresTest` / `AppOnMySQLTest` spielen dort eine Neuinstallation mit dem echten Programmcode durch.

### PostgreSQL

- Einrichtungsassistent › *Auf einem Datenbank-Server* › **PostgreSQL**, Adresse `host:5432`, Benutzer, Passwort und Datenbankname. YaBS legt die Datenbank an (der Benutzer braucht das Recht `CREATEDB`) oder verwendet eine vorhandene leere Datenbank.
- Getestet mit PostgreSQL 16. Treiber `org.postgresql.Driver` (wird mitgeliefert).

### MySQL / MariaDB: Hinweise und Grenzen

- **Treiber:** MySQL Connector/J 8.4 (mitgeliefert). Der alte Connector/J 5.1 konnte mit aktuellen Servern nicht mehr verbinden. Bestehende Verbindungen (`com.mysql.jdbc.Driver`) funktionieren unverändert.
- **Neue Datenbanken** werden mit `utf8mb4` angelegt. Getestet mit MariaDB 10.11.
- **Suche und Papierkorb brauchen Trigger.** MySQL-Datenbanken hatten bisher **keine** Trigger – Suchindex und Papierkorb blieben leer. Neue Datenbanken bekommen sie; bestehende mit dem Update **1.1986**, das auch Suchindex und Papierkorb aus den vorhandenen Daten füllt.
- **Grenzen:** Zum Anlegen der Trigger braucht der Datenbank-Benutzer das Recht **`TRIGGER`**. Ist auf dem Server das Binärlog aktiv (`log_bin = ON`, Replikation), verlangt MySQL zusätzlich `SUPER` oder `log_bin_trust_function_creators = 1`. Fehlt das, läuft das Update trotzdem durch, YaBS zeigt einen Hinweis, und **Suche und Papierkorb funktionieren nicht** (alles andere schon). Abhilfe: Recht vergeben (`GRANT TRIGGER ON yabs.* TO 'yabs'@'%';`) bzw. `SET GLOBAL log_bin_trust_function_creators = 1;` und YaBS neu starten – oder die Datenbank-Version in `globalsettings` (`yabs_dbversion`) auf `1.1985` zurücksetzen, damit das Update erneut läuft.
- **Alte MySQL-Updates repariert:** Die Updates 1.1974 bis 1.1982 waren in Derby-Syntax geschrieben (Spalte ohne Typ, `RENAME COLUMN`, `IDENTITY`, `BLOB(2G)`) – MySQL-Datenbanken kamen nicht über Version 1.1973 hinaus. Sie sind jetzt in MySQL-Syntax; ältere MySQL-Datenbanken werden beim nächsten Start bis zur aktuellen Version aktualisiert.
- Unterschiede zu H2/PostgreSQL, die MySQL-typisch sind: Text-Vergleiche ignorieren Groß-/Kleinschreibung (Sortierung `utf8mb4_general_ci`), Zeitstempel ohne Zeitzone (`DATETIME`).

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

- **PostgreSQL** is supported (setup assistant › database server › PostgreSQL, `host:5432`); **MySQL/MariaDB** uses Connector/J 8.4.
- **New databases** (H2, MySQL, PostgreSQL) are created in one step from `src/main/resources/db/schema.sql`, the neutral structure of the current version, converted per database by `SqlDialect` (identity columns, large objects, triggers); updates only run for older existing databases. A new update: add it to `DatabaseUpdater`, raise `Constants.DATABASE_VERSION`, run `./gradlew generateSchema`.
- **MySQL limits:** search and trash bin need triggers – the user needs the `TRIGGER` privilege (with binary logging also `SUPER` or `log_bin_trust_function_creators = 1`); without them the update 1.1986 goes on with a warning and search/trash bin do not work. Existing MySQL databases get the triggers and a filled search index with update 1.1986; the old MySQL updates 1.1974–1.1982 (Derby syntax) were fixed.
- **New installations use H2**, stored in `<user home>/.yabs/h2` (`<name>.mv.db`); Derby databases keep working. H2 runs the Derby schema and updates, converted (SMALLINT → BIGINT, Derby triggers via the Java trigger `H2Trigger`).
- **Developer mode:** `java -jar openyabs.jar -devmode` (or `-Dyabs.devmode=true`) – H2 in memory only, settings in a temporary folder (e.g. `/tmp/yabs-dev…`); nothing is kept, the setup assistant appears on every start. Start without `-devmode` for a real installation.
- **Connection Manager** (*File › Connection Manager*): list of all databases, sign in and connect in one step (falls back to the previous one on failure), new database via the setup assistant without restart, add an existing H2/Derby database, rename, remove (only the entry).
- **Zip backup** (*Control Panel › Database*): closes the database, zips it as `<name>-<yyyyMMdd-HHmmss>.zip` into a folder with variables (default `[APPDIR]/backups/[DBNAME]`), reconnects; keeps 3 by default; optionally at every exit. Restore unzips next to the zip and adds a new connection "Backup <timestamp>" – nothing is overwritten.
- Derby backup, SQL export, restore and *Tools › Migration* are hidden until they are tested with H2.
