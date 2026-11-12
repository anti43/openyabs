# YaBS – Yet another Business Software

**Quotes, orders, invoices, e-invoices, customers, products and bookkeeping for freelancers and small businesses – on your own computer, with your data in your own hands.**

Free and open source (GPL v3) · Windows, Linux and macOS · No cloud, no account, no subscription

**[⬇ Download](https://github.com/anti43/openyabs/releases)** · **[Website with all features](https://anti43.github.io/openyabs/)** · [Deutsche Hinweise](docs/Hinweise.md) · [E-Rechnung (XRechnung / ZUGFeRD)](docs/E-Rechnung.md)

![An order in YaBS: customer, document data and dates in cards, the positions with net, VAT and total below](docs/images/invoice.png)

## Highlights

- **Everything in one place**: contacts, quotes, orders, invoices, credit notes, reminders, products, stock, expenses and journal
- **E-invoices**: XRechnung and ZUGFeRD according to EN 16931, checked before sending
- **Your own layout**: ODT (LibreOffice / OpenOffice) and PDF templates, with [EPC QR code (GiroCode)](docs/QR-Code.md)
- **Your data stays with you**: local database or your own MySQL / MariaDB server – no cloud, no account, no tracking
- **Switching from Fakturama 2?** *File › Import › Fakturama 2*, choose Fakturama's `Database.script` – contacts, products, documents and expenses come along with their numbers and dates
- **Extendable**: Groovy scripts and [plugins](docs/Plugins.md)

All features: **[anti43.github.io/openyabs](https://anti43.github.io/openyabs/)**

## Auf Deutsch

**YaBS ist ein kostenloses Rechnungsprogramm (Open Source, GPL v3) für Selbstständige, Freiberufler und kleine Unternehmen** – für Windows, Linux und macOS, ohne Cloud, ohne Konto, ohne Abo.

- **Angebote, Aufträge, Rechnungen**, Abschlags- und Anzahlungsrechnungen, Gutschriften, Lieferscheine und Mahnungen
- **E-Rechnung**: XRechnung und ZUGFeRD nach EN 16931, vor dem Versand geprüft – [mehr zur E-Rechnung](docs/E-Rechnung.md)
- **Kunden, Lieferanten, Artikel und Lager**, Ausgaben und Buchungsjournal mit Umsatzsteuer
- **Eigene Vorlagen** in LibreOffice / OpenOffice (ODT) mit **GiroCode** (EPC-QR-Code zum Bezahlen per Banking-App)
- **Umstieg von Fakturama 2**: *Datei › Import › Fakturama 2* – Kontakte, Artikel, Belege und Ausgaben werden mit Belegnummern und Datum übernommen
- **Datenschutz**: alle Daten bleiben auf Ihrem Rechner oder Ihrem eigenen Datenbankserver

Ausführliche deutsche Beschreibung: [README auf Deutsch](docs/README.de.md) · [Hinweise und Neuerungen](docs/Hinweise.md) · **[Webseite](https://anti43.github.io/openyabs/)** (Sprache oben rechts auf DE umschalten)

## Installation

YaBS runs on **Windows, Linux and macOS**.

**Installer packages** (bring their own Java – nothing else to install):

| System | Package | Install |
|---|---|---|
| Windows | `.msi` | double-click |
| Debian, Ubuntu, Linux Mint | `.deb` | `sudo apt install ./yabs_*.deb` |
| Fedora, openSUSE, RHEL | `.rpm` | `sudo dnf install ./yabs-*.rpm` |
| macOS (Apple silicon: `arm64`, Intel: `x86_64`) | `.dmg` | open, drag YaBS into *Applications*; the first time right-click › *Open* (the package is not signed) |

**ZIP for all systems** (needs **Java 21** or newer, e.g. from [adoptium.net](https://adoptium.net)): unpack and start `yabs.bat` (Windows) or `yabs.sh` (Linux, macOS) – or `java -jar openyabs.jar`.

All downloads: **[Releases](https://github.com/anti43/openyabs/releases)** – `latest-master` is the current development build.

On the first start one window asks where your data should be stored: **on this computer** (one click) or on a **database server** (PostgreSQL, MySQL / MariaDB, Derby). On this computer the data goes to `<user home>/.yabs/h2` (H2 database). The setup runs in the background; languages, countries, an invoice template and demo templates for all document types are set up automatically, German systems start in German, and you are signed in as `admin` (password `password` – change it under *Settings › Users*). Your company data goes to *Settings › Company Info*. No account, no internet connection needed.

Updating: older databases (also Derby) are updated automatically on the first start – make a copy of the database folder before, or use the zip backup. Details: [Database: H2, Derby, connections and backup](docs/Datenbank.md) · [Hinweise](docs/Hinweise.md).

Trying things out without touching your data: `java -jar openyabs.jar -devmode` (database in memory only, settings in a temporary folder).

## Building from source

```bash
./gradlew test distributionZip
```

The ZIP is created in `build/distributions/`. Java 21 is required. After `./gradlew build` (or `jar`), the libraries are copied to `build/libs/lib/`, so `java -jar build/libs/openyabs.jar` starts right away (a `-cp` option is ignored by `java -jar`). Pushes to `master` and version tags build the ZIP and the installer packages on GitHub Actions.

Versions: builds of `master` are **2.0.&lt;build number&gt;** (shown on the splash screen and in *Help › About*), releases carry the version of their tag (`v2.1.0` → 2.1.0), local builds are `2.0-dev` (`./gradlew distributionZip -PreleaseVersion=2.0.5` sets one).

## Help, community and license

- Plugins: [docs/Plugins.md](docs/Plugins.md) · Templates and all fields: [docs/Vorlagen.md](docs/Vorlagen.md) · Cash register and TSE: [docs/Kasse-TSE.md](docs/Kasse-TSE.md)
- Questions (English and German): mailing list [openyabs@googlegroups.com](mailto:openyabs@googlegroups.com) · [archive](https://groups.google.com/forum/#!forum/openyabs)
- German documentation: [Handbuch](https://docs.google.com/document/d/1VhUi0wS-EJgfV-o89Hl-2wux810mJnqUV1wPxpgVIs4/pub) · [Hinweise und Neuerungen](docs/Hinweise.md) · [E-Rechnung](docs/E-Rechnung.md)
- Original project: [anti43/openyabs](https://github.com/anti43/openyabs) · [heise download](http://www.heise.de/download/yabs-yet-another-business-software-1149250.html) · [ubuntuusers wiki](http://wiki.ubuntuusers.de/Yabs)

YaBS is free software under the **GNU General Public License v3**. Libraries and their licenses: [licenses-list.txt](licenses-list.txt).

![](https://raw.githubusercontent.com/anti43/openyabs/master/src/mpv5/resources/images/icon2_128.png)
