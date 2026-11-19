# Task: DATEV export (Buchungsstapel, EXTF) as YaBS plugin

Repository: anti43/openyabs. Work as a Java plugin like `plugins/gobd` (own Gradle module, jar, loaded via the plugin manager). Core changes are allowed where the plugin needs an API (see "Gaps to close", 1). Follow `CLAUDE.md` (wiki docs DE/EN as patch, compile locally, tests in CI).

## Goal

From the journal (period + filters) write a DATEV import file **"DATEV-Format Buchungsstapel" (EXTF, CSV, format version 700, category 21)** that a tax adviser imports into DATEV Kanzlei-Rechnungswesen / DATEV Unternehmen online without manual rework. One booking line per document **and tax rate**.

## What already exists (verified)

- **Accounts (`accounts` table, `mpv5.db.objects.Account`) already model a chart of accounts:**
  - `intaccountclass` = the account number (SKR03: 1200 Bank, 1000 Kasse, 8400 Erlöse 19 %, 8300 Erlöse 7 %, 4930 Bürobedarf …),
  - `intaccounttype` = 0 asset/money, 2 expense, 3 income (constants in `Account`),
  - `taxvalue` = the tax rate of the account, `inttaxfid` = UStVA field (Kennzahl), `intprofitfid` = EÜR line, `inttaxuid` = tax key id,
  - `frame` = chart (`de_skr03`), `intparentaccount`/`hierarchypath` = tree.
- **Import:** *Settings › Accounts › import* (`wizard_AccountXMLImport_1`) reads `ext/accounts-german-skr03.xml` (379 SKR03 accounts).
- **Every document has one account:** `items.accountsids`, `expenses.accountsids`, `revenues.accountsids` (chosen in the document / expense / income view).
- **Tax rates per record:** expenses/revenues `taxpercentvalue`; documents through their positions (`subitems.taxpercentvalue`, `totalnetvalue`, `totalbrutvalue`). `mpv5.handler.JournalCalc.split(...)` splits a document's stored net/tax exactly by the rates of its positions (discounts included); payments in the proportion of the invoice.
- **Tax rates control panel** (`tax` table, `mpv5.db.objects.Tax`): names of the rates. Never hard-code rates or accounts.

## Gaps to close

1. **Journal loading is inside `JournalPanel` (private).** Move it into a core service, e.g. `mpv5.handler.JournalLoader` (filter object in, `List<JournalCalc.Entry>` + `Totals` out, entries with `rates`), used by `JournalPanel` and by the plugin. No behaviour change of the journal.
2. **SKR03 file is outdated:** 16 % accounts and old UStVA fields (e.g. Kz 51 → today 81). Check and update `ext/accounts-german-skr03.xml` (names, `taxvalue`, `inttaxfid`), mark it in the wiki. Add **SKR04** (`ext/accounts-german-skr04.xml`), same format. Check the import wizard is reachable and works with both (and that `ext/` files are shipped in the release; otherwise ship them as resources).
3. **Positions have no account.** Not needed for the first version: per document and rate, the account is derived (see mapping). Optional later: `subitems.accountsids`.
4. **Money accounts and persons:** YaBS has no Debitor/Kreditor numbers and no bank/cash account per payment. First version: cash-basis bookings against a money account (setting); optional "Personenkonten" mode later (contact → Debitor 10000-69999 / Kreditor 70000-99999 from the contact number, a setting).

## Plugin settings (control panel page of the plugin, stored per database)

- Beraternummer (1001-9999999), Mandantennummer (1-99999), Wirtschaftsjahresbeginn, Sachkontenlänge (4-8), Kontenrahmen (SKR03/SKR04, from `Account.frame`), Bezeichnung des Stapels, Festschreibung (default off).
- Money account for income and for expenses (default: account with number 1200 of the chart, else ask) – optionally a cash account for documents paid in cash (POS: `Barverkauf`).
- Revenue / expense account per tax rate (fallback when the document's account does not fit the rate): pre-filled from the accounts of type 3 / 2 with matching `taxvalue` (e.g. 19 → 8400, 7 → 8300), editable. Rates come from the tax control panel plus all rates found in the period.
- Use "automatic accounts" (no BU key when the account's `taxvalue` equals the line's rate) – yes/no; BU keys per rate when not automatic (setting, pre-filled: 19 % → 9 / 3, 7 % → 8 / 2 depending on revenue/expense, 0 % → none; verify against current DATEV docs).

## Mapping (one line per document and rate)

| DATEV field | Value |
|---|---|
| Umsatz | gross amount of the rate line (positive), 2 decimals, comma |
| Soll/Haben-Kennzeichen | income: S (money account debit), expense: H; credit notes reversed |
| Konto | money account (or Debitor/Kreditor in person mode) |
| Gegenkonto | document account if its `taxvalue` matches the rate, else the account of the rate from the settings |
| BU-Schlüssel | empty for automatic accounts, else from the settings |
| Belegdatum | payment date (cash basis) as DDMM; document date in the "Belegdatum" field mode setting |
| Belegfeld 1 | document number (max 36 chars, allowed characters only) |
| Buchungstext | contact name / expense name (max 60) |
| Beleglink | optional: file name of the receipt in a ZIP next to it |
| EU-Land u. UStID | from the contact for 0 % EU deliveries (`8125`) |

- Only entries that count (`Entry.counted`), i.e. the EÜR view: paid documents, payments of invoices paid in parts, paid expenses; cancelled ones never.
- Rounding: lines of one document must add up to the stored gross amount (they do with `JournalCalc.split`).
- Header line 1: `"EXTF";700;21;"Buchungsstapel";13;<timestamp yyyyMMddHHmmssSSS>;;"RE";"";"";<Berater>;<Mandant>;<WJ-Beginn yyyyMMdd>;<Sachkontenlänge>;<Datum von>;<Datum bis>;"<Bezeichnung>";"";1;0;<Festschreibung 0/1>;"EUR";…` – take the exact field list and order from the current DATEV developer documentation (developer.datev.de, "Buchungsstapel") and write it down in a test; line 2: the column headers; Windows-1252, CRLF, `;`, text in quotes.
- File name: `EXTF_Buchungsstapel_<from>_<to>.csv`.

## UI

- Journal *Export* menu: entry "DATEV-Buchungsstapel (CSV)" added by the plugin (an export hook/extension point in `JournalPanel` if none exists – keep it generic: plugins can add export entries that get the loaded entries + filter).
- Before writing: a check dialog listing problems (missing settings, documents without account, accounts without number, rates without account, numbers too long) – write anyway / cancel.
- Also add the DATEV file into the tax adviser ZIP when the plugin is active (`datev/EXTF_….csv`).

## Tests (JUnit, run in CI)

- Mapping of invoice 19 %, mixed 19/7 % with document discount, credit note, expense 7 %, payment of an invoice paid in parts, 0 % EU delivery.
- Header line fields and order, encoding (Windows-1252), CRLF, quoting, text length limits.
- Sum of the lines = stored gross amounts.
- Account selection fallback.

## Docs

Wiki pages `DATEV-Export` (DE) and `DATEV-export-(EN)`, linked from the entry pages, `_Sidebar`, `Journal` / `Journal-(EN)` and the plugin pages; as `git format-patch` for the maintainer.

## Out of scope

DATEV XML interface (Belegbilder online), DATEV Debitoren/Kreditoren-Stammdaten export (later), Kassenbuch (DSFinV-K is separate), "Festschreibung" by default.
