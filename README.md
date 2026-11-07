# YaBS – Yet another Business Software

**Quotes, orders, invoices, e-invoices, customers, products and bookkeeping for freelancers and small businesses – on your own computer, with your data in your own hands.**

Free and open source (GPL v3) · Windows, Linux and macOS · No cloud, no account, no subscription

**[⬇ Download the latest version](https://github.com/andreasweberd/openyabs/releases)** · [Deutsche Hinweise](docs/Hinweise.md) · [E-Rechnung (XRechnung / ZUGFeRD)](docs/E-Rechnung.md)

![An order in YaBS: customer, document data and dates in cards, the positions with net, VAT and total below](docs/images/invoice.png)

---

## Contents

- [Why YaBS](#why-yabs)
- [Your data stays with you](#your-data-stays-with-you)
- [Features at a glance](#features-at-a-glance)
- [Customers, suppliers and manufacturers](#customers-suppliers-and-manufacturers)
- [Quotes, orders, invoices and more](#quotes-orders-invoices-and-more)
- [Correct amounts, to the cent](#correct-amounts-to-the-cent)
- [E-invoicing: XRechnung and ZUGFeRD](#e-invoicing-xrechnung-and-zugferd)
- [Printing, PDF, templates and e-mail](#printing-pdf-templates-and-e-mail)
- [Getting paid: due dates, cash discount, reminders](#getting-paid-due-dates-cash-discount-reminders)
- [Products, services and stock](#products-services-and-stock)
- [Bookkeeping: journal, expenses and revenues](#bookkeeping-journal-expenses-and-revenues)
- [Scheduling and recurring invoices](#scheduling-and-recurring-invoices)
- [A modern, fast user interface](#a-modern-fast-user-interface)
- [Teams: users, groups and rights](#teams-users-groups-and-rights)
- [Import, export and moving from other software](#import-export-and-moving-from-other-software)
- [For power users: scripts, plugins and SQL](#for-power-users-scripts-plugins-and-sql)
- [Installation](#installation)
- [Updating and your existing data](#updating-and-your-existing-data)
- [Building from source](#building-from-source)
- [Help, community and license](#help-community-and-license)

---

## Why YaBS

- **Everything in one place**: contacts, quotes, orders, invoices, deposits, credit notes, reminders, products, stock, expenses and a journal – connected to each other.
- **Your data stays on your computer**: no cloud, no login, no tracking, works offline. See [below](#your-data-stays-with-you).
- **Ready for the e-invoice obligation**: XRechnung and ZUGFeRD according to EN 16931, checked with the official validator before they leave the house.
- **Your own layout**: invoices and letters come from your own LibreOffice / OpenOffice templates (ODT) or PDF forms.
- **Free forever**: open source under the GPL v3. No license fees, no per-user prices, no lock-in.
- **Runs everywhere**: Windows, Linux and macOS, alone or with several users on a shared database.

## Your data stays with you

This is what sets YaBS apart from the many online invoicing services:

- **No cloud, no account.** YaBS is a desktop application. There is nothing to sign up for, and nobody but you can switch it off, raise the price or read your figures.
- **All data is local.** Customers, invoices, prices and your bookkeeping are stored in a database on your own computer (embedded Apache Derby) – or, if you want, on your own server in your office (MySQL / MariaDB).
- **Works offline.** Writing quotes, invoicing and bookkeeping need no internet connection. YaBS only goes online when *you* do something that needs it, such as sending an e-mail through *your* mail server.
- **No telemetry, no tracking, no ads.**
- **Your files are normal files.** PDFs, e-invoices and exports go to folders you choose. The database is a plain folder you can back up, copy to a USB stick or put into your existing backup.
- **Easy for data protection (GDPR).** Customer data is not handed to a third-party processor – it never leaves your machine.
- **You can always get your data out.** Export contacts and documents to CSV, XML, vCard or HTML at any time – and the source code is open, so the format is never a secret.
- **It keeps working.** Software on your own computer doesn't disappear when a start-up is bought or a service is discontinued.

## Features at a glance

| Area | What you get |
|---|---|
| Contacts | Customers, suppliers, manufacturers and companies; several addresses; bank accounts; notes, phone calls, files and a full history |
| Documents | Quotes, orders, invoices, deposits, part payments, credit notes, delivery notes, order confirmations, reminders |
| E-invoice | XRechnung (XML), ZUGFeRD (PDF with embedded XML), validated per EN 16931, delivery format per customer |
| Output | Print, PDF, ODT from your own templates, e-mail with attachments, serial e-mails, mass printing, EPC QR code (GiroCode) |
| Products | Products and services, purchase/sale prices, markup, staggered prices, images, EAN, product groups and lists, suppliers |
| Stock | Inventory, thresholds, purchase orders to suppliers, stock manager |
| Money | Exact decimal arithmetic, VAT per rate, discounts, cash discount (Skonto), due dates, reminder fees, deposits deducted in the final invoice |
| Bookkeeping | Journal with revenue, expenses, profit and VAT, accounts, expenses and revenues, direct debit export (DTAUS) |
| Planning | Calendar, due date overview, recurring invoices, dashboard with open amounts |
| Working | Modern card layout, tabs, back/forward, favourites, search, keyboard shortcuts, trash bin, undo of unsaved changes |
| Teams | Several users, groups, rights, record locking, own settings per user |
| Data | Local Derby database or MySQL server, CSV / XML / vCard / HTML import and export, migration tools |
| Extend | Groovy scripts and console, plugins, SQL query window, web shop interface |

## Customers, suppliers and manufacturers

![A contact with name, roles, address, communication, business and payment data and all documents of the contact](docs/images/contact.png)

- **One contact, several roles**: a contact can be customer, supplier, manufacturer and/or company at the same time – shown as chips next to the name and switched with one click.
- **Everything about a contact on one page**: name, addresses (main, billing and delivery addresses), phone, mail, web, VAT ID, bank account, payment terms and notes.
- **All documents of a contact** below the data: quotes, orders, invoices, credit notes and products, each with a one-line summary of its positions, a filter and the total.
- **Create documents from the contact**: new quote, order, invoice, credit note or product with one click.
- **Payment terms per customer**: due days or "payable immediately", cash discount (% and days), the bank account the customer pays to, and the e-invoice delivery format with Leitweg-ID.
- **Communication**: phone call / conversation notes, files attached to the contact, mail directly from the contact, website opens in the browser.
- **History**: every change is recorded – who changed what and when.
- **Checks while typing**: VAT IDs and IBANs are validated.

## Quotes, orders, invoices and more

- **The whole sales process**: quote → order → deposit / part payment → invoice → credit note, each created from the previous one with one click. The previous document is marked as finished and referenced.
- **Final invoices with deposits**: the final invoice contains the order's positions and deducts all deposits and part payments – automatically and with the right VAT.
- **Positions as you need them**: quantity, unit, product, free text (multi-line), net price, VAT rate, discount per line, optional extra column, text-only lines; move lines up and down, paste products, product lists or whole documents.
- **Live totals**: net, discount, VAT and total update while you type.
- **Status tracking**: queued, in progress, paused, finished, paid – with the payment date booked automatically.
- **Document numbers** from your own number formats (e.g. `R-2026-001`), per document type.
- **Delivery notes and order confirmations** from the same document, optionally printed together.
- **Combine several invoices** into one collective invoice from the document search.
- **Compare an invoice with its order** to see what has been delivered and billed.
- **Find documents** by number, customer, date, status or text.

## Correct amounts, to the cent

Invoicing software has to calculate correctly. YaBS calculates with exact decimal numbers, never with binary floating point:

- Every line is rounded to the cent, the VAT is calculated **once per VAT rate** on the sum of the lines – exactly as EN 16931 (the e-invoice standard) requires. The PDF and the e-invoice always show the same amounts.
- Several VAT rates on one invoice are listed separately, also rates like 7.7 %.
- Line discounts, staggered prices, cash discount and reminder fees are calculated the same way everywhere – on screen, in the PDF, in the e-invoice, in the journal and in the direct debit file.
- Numbers are read and shown in your language's format (e.g. `1.234,56` in German).

## E-invoicing: XRechnung and ZUGFeRD

- **XRechnung** (XML), **ZUGFeRD** (PDF/A-3 with embedded XML) or both, according to **EN 16931**.
- **Validated with the official validator** before saving or sending – invalid e-invoices are not sent, and YaBS tells you why.
- **Per customer**: choose the delivery format (normal PDF, ZUGFeRD, XRechnung or PDF + XRechnung), the Leitweg-ID for public authorities, due days and cash discount.
- **Works with e-mail and serial e-mail**: each customer receives the format they need.
- **Several bank accounts**: choose per customer which account appears on the invoice.
- All details, the legal background and a checklist: **[docs/E-Rechnung.md](docs/E-Rechnung.md)** (German).

## Printing, PDF, templates and e-mail

- **Your own layout**: design invoices, quotes, reminders and letters in LibreOffice / OpenOffice Writer (ODT) or as PDF forms, with fields for all data, position tables, VAT groups, payment terms and an **EPC QR code (GiroCode)** for instant bank transfers.
- **Templates per document type, user and group** – for example different layouts for different companies or departments.
- **PDF and ODT export** with file names and folders from your own patterns (e.g. by year and customer). File dialogs always open in your configured folder or the last one you used.
- **E-mail directly from YaBS** through your own mail server (SMTP), with the PDF and/or e-invoice attached, using message templates.
- **Serial e-mails and mass printing** from the journal – for example all open invoices of a month.
- **Automatic archive**: every sent e-mail can also go to your own archive mailbox (Bcc).
- **Auto-PDF**: optionally save a PDF every time a document is saved.

## Getting paid: due dates, cash discount, reminders

- **Due dates** from the customer, the template or your company defaults.
- **Cash discount (Skonto)**: percent and days per customer, with discount amount and the amount to pay in the template.
- **Overdue invoices at a glance** on the dashboard.
- **Reminders in stages** (e.g. payment reminder, 1st and 2nd reminder) with your own texts and fees per stage, printed or mailed.
- **Direct debit**: export a DTAUS file for collecting invoice amounts.

## Products, services and stock

![A product with prices, classification, description, manufacturer and suppliers, stock and the documents it was used in](docs/images/product.png)

- **Products and services** with number, unit, EAN, manufacturer reference, description, URL and image.
- **Prices that calculate themselves**: enter cost and markup, sale price or gross price – the other values follow while you type.
- **Staggered prices**: price per minimum quantity; in a document the price follows the quantity automatically (unless you typed a price yourself).
- **Product groups and product lists** (e.g. a standard package you add to a quote with one click, optionally with a percentage surcharge).
- **Manufacturers and suppliers** per product.
- **Where was it sold?** Every product shows the quotes, orders, invoices and credit notes it appears in, with quantities and amounts.
- **Stock**: stock level and threshold, automatic reduction when invoicing, stock manager, purchase orders to suppliers.

## Bookkeeping: journal, expenses and revenues

![The journal with all documents of a period, filters and totals for revenue, expenses, profit and VAT](docs/images/journal.png)

- **Journal**: all documents, expenses and revenues of a period, filtered by type, status, group, user and account – with totals for volume, revenue, expenses, profit and VAT.
- **Cash basis**: revenue and expenses count once they are paid.
- **Expenses and revenues** outside of invoices (e.g. receipts), with VAT and account.
- **Accounts** for your chart of accounts; documents and receipts are assigned to them.
- **Dashboard**: open quotes, orders and invoices, overdue invoices, upcoming events.
- **Export** the journal to PDF, CSV or your own template – e.g. for your tax advisor.

## Scheduling and recurring invoices

- **Calendar** with events for documents and contacts.
- **Recurring invoices**: schedule an invoice to be created again every *n* months until an end date – YaBS creates them from the dashboard with one confirmation.
- **Due date warnings** for invoices.

## A modern, fast user interface

- **Modern look by default**: clean cards, rounded controls, clear tabs – and the **classic views are one click away** (*View › Layout › Modern look*).
- **Responsive layout**: the data cards arrange themselves in one to three columns depending on the window size; it scales with large fonts and HiDPI screens.
- **Tabs and navigation**: several records open side by side, back and forward like a browser, favourites, a clipboard for records, a search bar.
- **Keyboard shortcuts** for everything frequent: `Ctrl+S` save, `Ctrl+P` print, `Ctrl+Shift+P` PDF, `Ctrl+E` mail, `Ctrl+F` filter, `Ctrl+C` / `Ctrl+V` copy and paste records, `Ctrl+F1` shows all shortcuts.
- **Safe deleting**: before deleting, YaBS lists everything that would be deleted with it; deleted records go to a trash bin first.
- **Your language**: English and German included; further translations can be made inside the application.

## Teams: users, groups and rights

- **Several users** with their own settings, signatures, templates and default values.
- **Groups** (e.g. companies, branches or departments) with their own documents, number formats and templates.
- **Rights** per user: view, export, edit, create/delete, administrate.
- **Record locking**: while someone edits a record, others can't overwrite it.
- **Shared database**: run YaBS on several computers against one MySQL / MariaDB server in your network – still without any cloud.

## Import, export and moving from other software

- **Import** contacts and products from CSV, vCard and XML.
- **Export** contacts, documents and lists to CSV, XML, vCard, HTML and PDF.
- **Migration tools**: from older YaBS versions and MP 4.5, and from the local Derby database to a MySQL server.
- **Web shop interface** to exchange contacts and orders with a shop.

## For power users: scripts, plugins and SQL

- **Groovy console and script manager**: automate recurring tasks or add your own calculations and document labels.
- **Plugins** extend YaBS with your own functions.
- **SQL query window** for your own evaluations – your data is a normal SQL database.
- **Add your own actions** to the toolbar.

## Installation

YaBS runs on **Windows, Linux and macOS**.

**Installer packages** (bring their own Java – nothing else to install):

| System | Package | Install |
|---|---|---|
| Windows | `.msi` | double-click |
| Debian, Ubuntu, Linux Mint | `.deb` | `sudo apt install ./yabs_*.deb` |
| Fedora, openSUSE, RHEL | `.rpm` | `sudo dnf install ./yabs-*.rpm` |

**ZIP for all systems** (needs **Java 21** or newer, e.g. from [adoptium.net](https://adoptium.net)): unpack and start `yabs.bat` (Windows) or `yabs.sh` (Linux, macOS) – or `java -jar openyabs.jar`.

All downloads: **[Releases](https://github.com/andreasweberd/openyabs/releases)** – `latest-master` is the current development build.

On the first start an assistant creates the local database and asks for your company data. That's it – no account, no internet connection needed.

## Updating and your existing data

- Older YaBS databases are opened and updated automatically on the first start; an interrupted update continues where it stopped.
- If a database is damaged, YaBS offers to copy all data into a newly created database and keeps the old one as a backup next to it.
- Tip: make a copy of the database folder before every update – it's just a folder.

Details (German): [docs/Hinweise.md](docs/Hinweise.md).

## Building from source

```bash
./gradlew test distributionZip
```

The ZIP is created in `build/distributions/`. Java 21 is required. Pushes to `master` and version tags build the ZIP and the installer packages on GitHub Actions.

## Help, community and license

- Questions (English and German): mailing list [openyabs@googlegroups.com](mailto:openyabs@googlegroups.com) · [archive](https://groups.google.com/forum/#!forum/openyabs)
- German documentation: [Handbuch](https://docs.google.com/document/d/1VhUi0wS-EJgfV-o89Hl-2wux810mJnqUV1wPxpgVIs4/pub) · [Hinweise und Neuerungen](docs/Hinweise.md) · [E-Rechnung](docs/E-Rechnung.md)
- Original project: [anti43/openyabs](https://github.com/anti43/openyabs) · [heise download](http://www.heise.de/download/yabs-yet-another-business-software-1149250.html) · [ubuntuusers wiki](http://wiki.ubuntuusers.de/Yabs)

YaBS is free software under the **GNU General Public License v3**. Libraries and their licenses: [licenses-list.txt](licenses-list.txt).

![](https://raw.githubusercontent.com/anti43/openyabs/master/src/mpv5/resources/images/icon2_128.png)
