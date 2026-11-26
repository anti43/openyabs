/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openyabs.datev;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import mpv5.db.objects.Account;
import mpv5.globals.GlobalSettings;
import mpv5.handler.JournalCalc;

/**
 * Configuration of the DATEV export plugin, persisted per database via
 * {@link GlobalSettings} (prefix {@code datev.}).
 */
public final class DatevConfig {

    public enum DateMode {
        PAYMENT_DATE,
        DOCUMENT_DATE
    }

    public static final String PREFIX = "datev.";
    // DATEV EXTF-Header-Felder
    private static final String K_BERATERNUMMER = PREFIX + "beraternummer";
    private static final String K_MANDANTENNUMMER = PREFIX + "mandantennummer";
    private static final String K_WJ_BEGINN = PREFIX + "wjbeginn";
    private static final String K_SACHKONTENLAENGE = PREFIX + "sachkontenlaenge";
    private static final String K_KONTENRAHMEN = PREFIX + "kontenrahmen";
    private static final String K_BEZEICHNUNG = PREFIX + "bezeichnung";
    private static final String K_DIKTATKUERZEL = PREFIX + "diktatkuerzel";
    private static final String K_FESTSCHREIBUNG = PREFIX + "festschreibung";
    private static final String K_WAEHRUNG = PREFIX + "waehrung";
    private static final String K_HERKUNFT = PREFIX + "herkunft";
    private static final String K_EXPORTIERT_VON = PREFIX + "exportiertvon";
    private static final String K_BUCHUNGSTYP = PREFIX + "buchungstyp";
    private static final String K_RECHNUNGSLEGUNGSZWECK = PREFIX + "rechnungslegungszweck";
    private static final String K_ANWENDUNGSINFO = PREFIX + "anwendungsinformation";

    // Buchungs-, Konten- und Export-Einstellungen
    private static final String K_BELEGDATUM_MODUS = PREFIX + "belegdatummodus";
    private static final String K_LEISTUNGSDATUM_EXPORTIEREN = PREFIX + "leistungsdatumexportieren";
    private static final String K_GELDKONTO_EINNAHMEN = PREFIX + "geldkontoeinnahmen";
    private static final String K_GELDKONTO_AUSGABEN = PREFIX + "geldkontoausgaben";
    private static final String K_KASSENKONTO = PREFIX + "kassenkonto";
    private static final String K_AUTOMATIKKONTEN = PREFIX + "automatikkonten";
    private static final String K_EIGENE_AUTOMATIKKONTEN = PREFIX + "eigeneautomatikkonten";
    private static final String K_EU_LIEFERKONTO = PREFIX + "eulieferkonto";
    private static final String K_EU_LEISTUNGSKONTO = PREFIX + "euleistungskonto";
    private static final String K_STEUERSCHULDUMKEHR_KONTO = PREFIX + "steuerschuldumkehrkonto";
    private static final String K_AUSFUHRKONTO = PREFIX + "ausfuhrkonto";
    private static final String K_BELEGORDNER = PREFIX + "belegordner";
    /**
     * the folder of the outgoing invoices in Belege online (DATEV
     * Unternehmen online) the document package goes to; DATEV's default
     */
    public static final String DEFAULT_INVOICE_FOLDER = "Ausgangsrechnungen";

    // Steuersatz-Tabellen (Präfixe)
    private static final String K_ERLOESKONTO_PREFIX = PREFIX + "erloeskonto.";
    private static final String K_AUFWANDSKONTO_PREFIX = PREFIX + "aufwandskonto.";
    private static final String K_BU_EINNAHMEN_PREFIX = PREFIX + "buschluesseleinnahmen.";
    private static final String K_BU_AUSGABEN_PREFIX = PREFIX + "buschluesselausgaben.";

    /**
     * Standard DATEV SKR03/SKR04 automatic accounts (AM/AV) where DATEV computes
     * VAT automatically and setting a BU-Schlüssel causes an import error or
     * double taxation. Non-automatic accounts (e.g., SKR03 4930 Bürobedarf,
     * 4600 Werbekosten, 0480 GWG; SKR04 6815, 0670) MUST emit a BU-Schlüssel
     * even when YaBS stores a default tax rate on the account.
     */
    private static final java.util.Set<String> SKR03_AUTO_INC_19 = setOf(
            "8400", "8405", "8406", "8410", "8736", "8760", "8790", "8910", "8920", "8921");
    private static final java.util.Set<String> SKR03_AUTO_INC_7 = setOf(
            "8300", "8305", "8306", "8310", "8731", "8750", "8780", "8915");
    private static final java.util.Set<String> SKR03_AUTO_EXP_19 = setOf(
            "3400", "3401", "3410", "3425", "3123", "3736", "3760", "3790");
    private static final java.util.Set<String> SKR03_AUTO_EXP_7 = setOf(
            "3300", "3301", "3420", "3731", "3750", "3780");

    private static final java.util.Set<String> SKR04_AUTO_INC_19 = setOf(
            "4400", "4405", "4406", "4410", "4736", "4760", "4790", "4620", "4645");
    private static final java.util.Set<String> SKR04_AUTO_INC_7 = setOf(
            "4300", "4305", "4306", "4310", "4731", "4750", "4780", "4610");
    private static final java.util.Set<String> SKR04_AUTO_EXP_19 = setOf(
            "5400", "5401", "5410", "5425", "5923", "5736", "5760", "5790");
    private static final java.util.Set<String> SKR04_AUTO_EXP_7 = setOf(
            "5300", "5301", "5420", "5731", "5750", "5780");

    public int beraterNummer = 0;
    public int mandantenNummer = 0;
    public String wjBeginn = LocalDate.now().getYear() + "0101";
    public int sachkontenLaenge = 4;
    /**
     * DATEV chart of accounts code: "03" (SKR03), "04" (SKR04), or ""
     */
    public String kontenrahmen = "03";
    public String bezeichnung = "YaBS Export";
    public String diktatKuerzel = "";
    public boolean festschreibung = false;
    public String waehrung = "EUR";
    /**
     * Header field 8 (Herkunft): 2-char origin code, default "RE".
     */
    public String herkunft = "RE";
    /**
     * Header field 9 (Exportiert von), max 25 chars.
     */
    public String exportiertVon = "";
    /**
     * Header field 19 (Buchungstyp): 1 = Finanzbuchführung, 2 = Jahresabschluss.
     */
    public int buchungstyp = 1;
    /**
     * Header field 20 (Rechnungslegungszweck): 0 = unabhängig, 30 = Steuerrecht, 50 = Handelsrecht.
     */
    public int rechnungslegungszweck = 0;
    /**
     * Header field 31 (Anwendungsinformation), max 16 chars.
     */
    public String anwendungsinformation = "";
    /**
     * Whether to populate col 115 (Leistungsdatum) and col 116 (Datum Zuord. Steuerperiode)
     * when delivery/service date differs from booking date (§ 14 Abs. 4 Nr. 6 UStG).
     */
    public boolean leistungsdatumExportieren = true;
    public DateMode belegdatumMode = DateMode.PAYMENT_DATE;
    public String moneyAccountIncome = "1200";
    public String moneyAccountExpense = "1200";
    public String cashAccount = "";
    public boolean useAutomaticAccounts = true;
    public String euDeliveryAccount = "8125";
    /**
     * services to businesses in the EU, reverse charge (VAT category AE with
     * an EU VAT ID of the customer, § 3a Abs. 2 UStG): SKR03 8336 / SKR04
     * 4336 (#214)
     */
    public String euServiceAccount = "8336";
    /**
     * domestic reverse charge (§ 13b UStG, category AE without an EU VAT ID):
     * SKR03 8337 / SKR04 4337
     */
    public String reverseChargeAccount = "8337";
    /**
     * exports outside the EU (category G, § 4 Nr. 1a UStG): SKR03 8120 /
     * SKR04 4120
     */
    public String exportAccount = "8120";
    /**
     * the company uses the small business exemption (§ 19 UStG, company
     * info): the default account of 0 % is SKR03 8195 / SKR04 4185; read with
     * the company info, not stored with the DATEV settings
     */
    public boolean smallBusiness;
    /**
     * the folder of the outgoing invoices in Belege online (document package,
     * property key 3): each client names it in Belege online
     */
    public String invoiceFolder = DEFAULT_INVOICE_FOLDER;

    /**
     * Additional client-specific automatic accounts (Individuelle Automatikkonten)
     * for which DATEV expects an empty BU-Schlüssel.
     */
    public final java.util.Set<String> customAutomaticAccounts = new java.util.TreeSet<String>();

    public final Map<BigDecimal, String> revenueAccounts = new TreeMap<BigDecimal, String>(JournalCalc.RATE_ORDER);
    public final Map<BigDecimal, String> expenseAccounts = new TreeMap<BigDecimal, String>(JournalCalc.RATE_ORDER);
    public final Map<BigDecimal, String> buKeysIncome = new TreeMap<BigDecimal, String>(JournalCalc.RATE_ORDER);
    public final Map<BigDecimal, String> buKeysExpense = new TreeMap<BigDecimal, String>(JournalCalc.RATE_ORDER);

    public DatevConfig() {
        applyDefaultBuKeys();
    }

    private static java.util.Set<String> setOf(String... values) {
        return java.util.Collections.unmodifiableSet(new java.util.HashSet<String>(java.util.Arrays.asList(values)));
    }

    /**
     * Formats an integer account number from {@link Account#__getIntaccountclass()}
     * preserving leading zeros for class-0 accounts (e.g., 27 -> "0027", 480 -> "0480",
     * 670 -> "0670") while leaving 4+ digit accounts unchanged.
     */
    public static String formatAccount(int accountClass) {
        if (accountClass <= 0) {
            return "";
        }
        return accountClass < 1000 ? String.format(java.util.Locale.ROOT, "%04d", accountClass) : String.valueOf(accountClass);
    }

    /**
     * Returns true iff {@code accountNo} is an official DATEV AM/AV automatic account
     * in SKR03 or SKR04 matching the given side and tax rate.
     */
    public static boolean isDatevAutomaticAccount(String accountNo, boolean isExpense, BigDecimal rate, boolean skr04) {
        if (isBlank(accountNo) || rate == null || rate.signum() == 0) {
            return false;
        }
        String digits = DatevFormat.digitsOnly(accountNo);
        if (digits.length() < 4) {
            return false;
        }
        String base4 = digits;
        if (digits.length() > 4) {
            String suffix = digits.substring(4);
            for (int i = 0; i < suffix.length(); i++) {
                if (suffix.charAt(i) != '0') {
                    return false;
                }
            }
            base4 = digits.substring(0, 4);
        }
        boolean is19 = rate.compareTo(bd("19")) == 0;
        boolean is7 = rate.compareTo(bd("7")) == 0;
        if (!is19 && !is7) {
            return false;
        }
        if (skr04) {
            if (isExpense) {
                return is19 ? SKR04_AUTO_EXP_19.contains(base4) : SKR04_AUTO_EXP_7.contains(base4);
            }
            return is19 ? SKR04_AUTO_INC_19.contains(base4) : SKR04_AUTO_INC_7.contains(base4);
        } else {
            if (isExpense) {
                return is19 ? SKR03_AUTO_EXP_19.contains(base4) : SKR03_AUTO_EXP_7.contains(base4);
            }
            return is19 ? SKR03_AUTO_INC_19.contains(base4) : SKR03_AUTO_INC_7.contains(base4);
        }
    }

    /**
     * Returns true if {@code accountNo} should omit BU-Schlüssel under this configuration
     * (either an official DATEV AM/AV account for the rate or listed in {@link #customAutomaticAccounts}).
     */
    public boolean isAutomaticAccount(String accountNo, boolean isExpense, BigDecimal rate) {
        if (!useAutomaticAccounts || isBlank(accountNo) || rate == null || rate.signum() == 0) {
            return false;
        }
        String digits = DatevFormat.digitsOnly(accountNo);
        if (customAutomaticAccounts.contains(digits) || customAutomaticAccounts.contains(accountNo.trim())) {
            return true;
        }
        // Only apply SKR03/SKR04 trailing-zero extension when length matches configured SKL
        if (digits.length() > 4 && digits.length() != sachkontenLaenge) {
            return false;
        }
        return isDatevAutomaticAccount(digits, isExpense, rate, "04".equals(kontenrahmen));
    }

    /**
     * Pre-fills standard German DATEV BU keys:
     * 19% -> 3 (output VAT) / 9 (input VAT),
     * 7% -> 2 (output VAT) / 8 (input VAT),
     * 0% -> "" (no tax key).
     */
    public void applyDefaultBuKeys() {
        buKeysIncome.putIfAbsent(bd("19"), "3");
        buKeysIncome.putIfAbsent(bd("7"), "2");
        buKeysIncome.putIfAbsent(bd("0"), "");
        buKeysExpense.putIfAbsent(bd("19"), "9");
        buKeysExpense.putIfAbsent(bd("7"), "8");
        buKeysExpense.putIfAbsent(bd("0"), "");
    }

    /**
     * Detects Kontenrahmen (SKR03 vs SKR04) and fills missing money, EU,
     * revenue and expense accounts from the given chart of accounts and tax
     * rates.
     *
     * @param accounts accounts from the database (may be empty)
     * @param rates tax rates from the Tax control panel / period (may be empty)
     * @param overwriteExisting true to overwrite existing values with detected defaults
     */
    public void prefillFromAccounts(List<Account> accounts, Collection<BigDecimal> rates, boolean overwriteExisting) {
        String detectedFrame = detectKontenrahmen(accounts);
        if (overwriteExisting || isBlank(kontenrahmen)) {
            if (!isBlank(detectedFrame)) {
                kontenrahmen = detectedFrame;
            }
        }
        boolean skr04 = "04".equals(kontenrahmen);
        String defaultBank = skr04 ? "1800" : "1200";
        String defaultCash = skr04 ? "1600" : "1000";
        String defaultEu = skr04 ? "4125" : "8125";

        if (overwriteExisting || isBlank(moneyAccountIncome)) {
            String bank = findAccountNumber(accounts, defaultBank);
            moneyAccountIncome = bank != null ? bank : defaultBank;
        }
        if (overwriteExisting || isBlank(moneyAccountExpense)) {
            String bank = findAccountNumber(accounts, defaultBank);
            moneyAccountExpense = bank != null ? bank : defaultBank;
        }
        if (overwriteExisting && findAccountNumber(accounts, defaultCash) != null) {
            cashAccount = defaultCash;
        }
        if (overwriteExisting || isBlank(euDeliveryAccount)) {
            euDeliveryAccount = defaultEu;
        }
        if (overwriteExisting || isBlank(euServiceAccount)) {
            euServiceAccount = skr04 ? "4336" : "8336";
        }
        if (overwriteExisting || isBlank(reverseChargeAccount)) {
            reverseChargeAccount = skr04 ? "4337" : "8337";
        }
        if (overwriteExisting || isBlank(exportAccount)) {
            exportAccount = skr04 ? "4120" : "8120";
        }

        if (rates != null) {
            for (BigDecimal r : rates) {
                if (r == null) {
                    continue;
                }
                BigDecimal key = JournalCalc.rateKey(r);
                if (overwriteExisting || isBlank(revenueAccounts.get(key))) {
                    String found = findDefaultAccountForRate(accounts, Account.INCOME, key, skr04, smallBusiness);
                    if (found != null) {
                        revenueAccounts.put(key, found);
                    }
                }
                if (overwriteExisting || isBlank(expenseAccounts.get(key))) {
                    String found = findDefaultAccountForRate(accounts, Account.EXPENSE, key, skr04, smallBusiness);
                    if (found != null) {
                        expenseAccounts.put(key, found);
                    }
                }
            }
        }
        applyDefaultBuKeys();
    }

    /**
     * Detects "03" or "04" from {@link Account#__getFrame()}.
     */
    public static String detectKontenrahmen(List<Account> accounts) {
        if (accounts == null) {
            return "";
        }
        int skr03 = 0;
        int skr04 = 0;
        for (Account a : accounts) {
            String f = a.__getFrame();
            if (f == null) {
                continue;
            }
            String low = f.toLowerCase();
            if (low.contains("skr04")) {
                skr04++;
            } else if (low.contains("skr03")) {
                skr03++;
            }
        }
        if (skr04 > skr03) {
            return "04";
        }
        if (skr03 > 0) {
            return "03";
        }
        return "";
    }

    private static String findAccountNumber(List<Account> accounts, String number) {
        if (accounts == null || isBlank(number)) {
            return null;
        }
        for (Account a : accounts) {
            if (a.__getIntaccountclass() > 0 && formatAccount(a.__getIntaccountclass()).equals(number)) {
                return number;
            }
        }
        return null;
    }

    private static String findDefaultAccountForRate(List<Account> accounts, int accountType, BigDecimal rate, boolean skr04, boolean smallBusiness) {
        // Prefer canonical DATEV standard accounts if present in the chart
        String preferred = canonicalAccount(accountType, rate, skr04, smallBusiness);
        if (preferred != null && findAccountNumber(accounts, preferred) != null) {
            return preferred;
        }
        if (accounts != null) {
            for (Account a : accounts) {
                if (a.__getIntaccountclass() <= 0) {
                    continue;
                }
                boolean typeMatch = accountType == Account.INCOME
                        ? a.__getIntaccounttype() == Account.INCOME
                        : (a.__getIntaccounttype() == Account.EXPENSE || a.__getIntaccounttype() == Account.COST);
                if (typeMatch && rateMatches(a.__getTaxvalue(), rate)) {
                    return formatAccount(a.__getIntaccountclass());
                }
            }
        }
        return preferred;
    }

    static String canonicalAccount(int accountType, BigDecimal rate, boolean skr04) {
        return canonicalAccount(accountType, rate, skr04, false);
    }

    /**
     * The standard account of a rate. Sales at 0 % without a VAT category are
     * tax-free domestic sales (SKR03 8100 / SKR04 4100, § 4 Nr. 8 ff. UStG,
     * without input tax deduction) - not exports (8120 / 4120, § 4 Nr. 1a
     * UStG): DATEV reports the export account in field 43 of the VAT return.
     * Exports, intra-community supplies and reverse charge have their own
     * accounts by the VAT category of the rate (G, K, AE). A small business
     * (§ 19 UStG) books its sales on SKR03 8195 / SKR04 4185.
     *
     * @param accountType
     * @param rate
     * @param skr04
     * @param smallBusiness the company uses § 19 UStG
     * @return the account, null if there is no standard
     */
    static String canonicalAccount(int accountType, BigDecimal rate, boolean skr04, boolean smallBusiness) {
        if (rate == null) {
            return null;
        }
        if (accountType == Account.INCOME) {
            if (rate.compareTo(bd("19")) == 0) {
                return skr04 ? "4400" : "8400";
            }
            if (rate.compareTo(bd("7")) == 0) {
                return skr04 ? "4300" : "8300";
            }
            if (rate.compareTo(BigDecimal.ZERO) == 0) {
                if (smallBusiness) {
                    return skr04 ? "4185" : "8195";
                }
                return skr04 ? "4100" : "8100";
            }
        } else {
            if (rate.compareTo(bd("19")) == 0) {
                return skr04 ? "5400" : "3400";
            }
            if (rate.compareTo(bd("7")) == 0) {
                return skr04 ? "5300" : "3300";
            }
            if (rate.compareTo(BigDecimal.ZERO) == 0) {
                return skr04 ? "6300" : "4900";
            }
        }
        return null;
    }

    public static boolean rateMatches(double accountTaxValue, BigDecimal rate) {
        if (rate == null) {
            return false;
        }
        BigDecimal accRate = BigDecimal.valueOf(accountTaxValue);
        return accRate.subtract(rate).abs().compareTo(new BigDecimal("0.01")) <= 0;
    }

    /**
     * Loads the DATEV settings from {@link GlobalSettings} (per database).
     */
    public static DatevConfig load() {
        DatevConfig c = new DatevConfig();
        c.smallBusiness = mpv5.utils.export.SmallBusiness.isEnabled();
        c.beraterNummer = readInt(K_BERATERNUMMER, 0);
        c.mandantenNummer = readInt(K_MANDANTENNUMMER, 0);
        c.wjBeginn = readString(K_WJ_BEGINN, LocalDate.now().getYear() + "0101");
        c.sachkontenLaenge = readInt(K_SACHKONTENLAENGE, 4);
        c.kontenrahmen = readString(K_KONTENRAHMEN, "03");
        c.bezeichnung = readString(K_BEZEICHNUNG, "YaBS Export");
        c.diktatKuerzel = readString(K_DIKTATKUERZEL, "");
        c.festschreibung = readBool(K_FESTSCHREIBUNG, false);
        c.waehrung = readString(K_WAEHRUNG, "EUR");
        c.herkunft = readString(K_HERKUNFT, "RE");
        c.exportiertVon = readString(K_EXPORTIERT_VON, "");
        c.buchungstyp = readInt(K_BUCHUNGSTYP, 1);
        c.rechnungslegungszweck = readInt(K_RECHNUNGSLEGUNGSZWECK, 0);
        c.anwendungsinformation = readString(K_ANWENDUNGSINFO, "");
        c.leistungsdatumExportieren = readBool(K_LEISTUNGSDATUM_EXPORTIEREN, true);
        String customAuto = readString(K_EIGENE_AUTOMATIKKONTEN, "");
        if (!isBlank(customAuto)) {
            for (String part : customAuto.split("[,;\\s]+")) {
                String d = DatevFormat.digitsOnly(part);
                if (!d.isEmpty()) {
                    c.customAutomaticAccounts.add(d);
                }
            }
        }
        String mode = readString(K_BELEGDATUM_MODUS, DateMode.PAYMENT_DATE.name());
        try {
            c.belegdatumMode = DateMode.valueOf(mode);
        } catch (Exception e) {
            c.belegdatumMode = DateMode.PAYMENT_DATE;
        }
        c.moneyAccountIncome = readString(K_GELDKONTO_EINNAHMEN, "04".equals(c.kontenrahmen) ? "1800" : "1200");
        c.moneyAccountExpense = readString(K_GELDKONTO_AUSGABEN, "04".equals(c.kontenrahmen) ? "1800" : "1200");
        c.cashAccount = readString(K_KASSENKONTO, "");
        c.useAutomaticAccounts = readBool(K_AUTOMATIKKONTEN, true);
        c.euDeliveryAccount = readString(K_EU_LIEFERKONTO, "04".equals(c.kontenrahmen) ? "4125" : "8125");
        c.euServiceAccount = readString(K_EU_LEISTUNGSKONTO, "04".equals(c.kontenrahmen) ? "4336" : "8336");
        c.reverseChargeAccount = readString(K_STEUERSCHULDUMKEHR_KONTO, "04".equals(c.kontenrahmen) ? "4337" : "8337");
        c.exportAccount = readString(K_AUSFUHRKONTO, "04".equals(c.kontenrahmen) ? "4120" : "8120");
        c.invoiceFolder = readString(K_BELEGORDNER, DEFAULT_INVOICE_FOLDER);

        readRateMap(K_ERLOESKONTO_PREFIX, c.revenueAccounts);
        readRateMap(K_AUFWANDSKONTO_PREFIX, c.expenseAccounts);
        readRateMap(K_BU_EINNAHMEN_PREFIX, c.buKeysIncome);
        readRateMap(K_BU_AUSGABEN_PREFIX, c.buKeysExpense);
        c.applyDefaultBuKeys();
        return c;
    }

    /**
     * The location of the settings page, for messages
     */
    public static final String SETTINGS_PATH = "Einstellungen › Plugins › DATEV-Export";

    /**
     * @return true if the DATEV settings were saved once for this database
     */
    public static boolean isSaved() {
        return GlobalSettings.hasProperty(K_BERATERNUMMER);
    }

    /**
     * Loads the DATEV settings and fills what was not saved from the chart of
     * accounts: the chart (SKR03/SKR04) if never saved, the money and EU
     * accounts and the accounts of each tax rate of the settings and the
     * given entries (as the settings page suggests them). The export uses
     * these, so it does not fail on rate accounts never saved.
     *
     * @param entries the journal rows to export (their rates), may be null
     * @return the settings
     */
    public static DatevConfig loadWithDefaults(Collection<JournalCalc.Entry> entries) {
        DatevConfig c = load();
        List<Account> accounts = dbAccounts();
        if (!GlobalSettings.hasProperty(K_KONTENRAHMEN)) {
            String detected = detectKontenrahmen(accounts);
            if (!isBlank(detected) && !detected.equals(c.kontenrahmen)) {
                c.kontenrahmen = detected;
                //the accounts not saved follow the detected chart
                if (!GlobalSettings.hasProperty(K_GELDKONTO_EINNAHMEN)) {
                    c.moneyAccountIncome = "";
                }
                if (!GlobalSettings.hasProperty(K_GELDKONTO_AUSGABEN)) {
                    c.moneyAccountExpense = "";
                }
                if (!GlobalSettings.hasProperty(K_EU_LIEFERKONTO)) {
                    c.euDeliveryAccount = "";
                }
                if (!GlobalSettings.hasProperty(K_EU_LEISTUNGSKONTO)) {
                    c.euServiceAccount = "";
                }
                if (!GlobalSettings.hasProperty(K_STEUERSCHULDUMKEHR_KONTO)) {
                    c.reverseChargeAccount = "";
                }
                if (!GlobalSettings.hasProperty(K_AUSFUHRKONTO)) {
                    c.exportAccount = "";
                }
            }
        }
        java.util.Set<BigDecimal> rates = dbTaxRates();
        if (entries != null) {
            for (JournalCalc.Entry e : entries) {
                if (e != null && e.rates != null) {
                    for (BigDecimal r : e.rates.keySet()) {
                        if (r != null) {
                            rates.add(JournalCalc.rateKey(r));
                        }
                    }
                }
            }
        }
        c.prefillFromAccounts(accounts, rates, false);
        return c;
    }

    /**
     * @return the accounts of the database, empty if not available
     */
    static List<Account> dbAccounts() {
        List<Account> list = new java.util.ArrayList<Account>();
        try {
            for (mpv5.db.common.DatabaseObject o : mpv5.db.common.DatabaseObject.getObjects(mpv5.db.common.Context.getAccounts())) {
                list.add((Account) o);
            }
        } catch (Exception e) {
            mpv5.logging.Log.Debug(DatevConfig.class, e.getMessage());
        }
        return list;
    }

    /**
     * @return the tax rates of the database, 19/7/0 % if none
     */
    static java.util.Set<BigDecimal> dbTaxRates() {
        java.util.Set<BigDecimal> set = new java.util.TreeSet<BigDecimal>(JournalCalc.RATE_ORDER);
        try {
            for (BigDecimal r : mpv5.utils.export.JournalAdviserPackage.rateLabels().keySet()) {
                if (r != null) {
                    set.add(JournalCalc.rateKey(r));
                }
            }
        } catch (Exception e) {
            mpv5.logging.Log.Debug(DatevConfig.class, e.getMessage());
        }
        if (set.isEmpty()) {
            set.add(JournalCalc.rateKey(bd("19")));
            set.add(JournalCalc.rateKey(bd("7")));
            set.add(JournalCalc.rateKey(BigDecimal.ZERO));
        }
        return set;
    }

    /**
     * Saves the DATEV settings to {@link GlobalSettings} (per database).
     */
    public void save() {
        GlobalSettings.setProperty(K_BERATERNUMMER, String.valueOf(beraterNummer));
        GlobalSettings.setProperty(K_MANDANTENNUMMER, String.valueOf(mandantenNummer));
        GlobalSettings.setProperty(K_WJ_BEGINN, safe(wjBeginn));
        GlobalSettings.setProperty(K_SACHKONTENLAENGE, String.valueOf(sachkontenLaenge));
        GlobalSettings.setProperty(K_KONTENRAHMEN, safe(kontenrahmen));
        GlobalSettings.setProperty(K_BEZEICHNUNG, safe(bezeichnung));
        GlobalSettings.setProperty(K_DIKTATKUERZEL, safe(diktatKuerzel));
        GlobalSettings.setProperty(K_FESTSCHREIBUNG, String.valueOf(festschreibung));
        GlobalSettings.setProperty(K_WAEHRUNG, isBlank(waehrung) ? "EUR" : waehrung.trim().toUpperCase());
        GlobalSettings.setProperty(K_HERKUNFT, isBlank(herkunft) ? "RE" : safe(herkunft));
        GlobalSettings.setProperty(K_EXPORTIERT_VON, safe(exportiertVon));
        GlobalSettings.setProperty(K_BUCHUNGSTYP, String.valueOf(buchungstyp));
        GlobalSettings.setProperty(K_RECHNUNGSLEGUNGSZWECK, String.valueOf(rechnungslegungszweck));
        GlobalSettings.setProperty(K_ANWENDUNGSINFO, safe(anwendungsinformation));
        GlobalSettings.setProperty(K_LEISTUNGSDATUM_EXPORTIEREN, String.valueOf(leistungsdatumExportieren));
        GlobalSettings.setProperty(K_EIGENE_AUTOMATIKKONTEN, String.join(",", customAutomaticAccounts));
        GlobalSettings.setProperty(K_BELEGDATUM_MODUS, (belegdatumMode == null ? DateMode.PAYMENT_DATE : belegdatumMode).name());
        GlobalSettings.setProperty(K_GELDKONTO_EINNAHMEN, safe(moneyAccountIncome));
        GlobalSettings.setProperty(K_GELDKONTO_AUSGABEN, safe(moneyAccountExpense));
        GlobalSettings.setProperty(K_KASSENKONTO, safe(cashAccount));
        GlobalSettings.setProperty(K_AUTOMATIKKONTEN, String.valueOf(useAutomaticAccounts));
        GlobalSettings.setProperty(K_EU_LIEFERKONTO, safe(euDeliveryAccount));
        GlobalSettings.setProperty(K_EU_LEISTUNGSKONTO, safe(euServiceAccount));
        GlobalSettings.setProperty(K_STEUERSCHULDUMKEHR_KONTO, safe(reverseChargeAccount));
        GlobalSettings.setProperty(K_AUSFUHRKONTO, safe(exportAccount));
        GlobalSettings.setProperty(K_BELEGORDNER, isBlank(invoiceFolder) ? DEFAULT_INVOICE_FOLDER : safe(invoiceFolder));

        writeRateMap(K_ERLOESKONTO_PREFIX, revenueAccounts);
        writeRateMap(K_AUFWANDSKONTO_PREFIX, expenseAccounts);
        writeRateMap(K_BU_EINNAHMEN_PREFIX, buKeysIncome);
        writeRateMap(K_BU_AUSGABEN_PREFIX, buKeysExpense);
        GlobalSettings.save();
    }

    private static void readRateMap(String prefix, Map<BigDecimal, String> target) {
        List<String[]> list = GlobalSettings.getProperties(prefix).getList();
        for (String[] pair : list) {
            if (pair == null || pair.length < 2 || pair[0] == null) {
                continue;
            }
            try {
                BigDecimal rate = JournalCalc.rateKey(new BigDecimal(pair[0].trim()));
                String val = pair[1] == null || "null".equals(pair[1]) ? "" : pair[1].trim();
                target.put(rate, val);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static void writeRateMap(String prefix, Map<BigDecimal, String> map) {
        for (String[] existing : GlobalSettings.getProperties(prefix).getList()) {
            if (existing != null && existing.length > 0 && existing[0] != null) {
                GlobalSettings.removeProperty(prefix + existing[0]);
            }
        }
        for (Map.Entry<BigDecimal, String> e : map.entrySet()) {
            if (e.getKey() != null) {
                GlobalSettings.setProperty(prefix + JournalCalc.rateKey(e.getKey()).toPlainString(), safe(e.getValue()));
            }
        }
    }

    private static String readString(String key, String fallback) {
        if (!GlobalSettings.hasProperty(key)) {
            return fallback;
        }
        String v = GlobalSettings.getProperty(key);
        return v == null || "null".equals(v) ? fallback : v;
    }

    private static int readInt(String key, int fallback) {
        if (!GlobalSettings.hasProperty(key)) {
            return fallback;
        }
        try {
            return Integer.parseInt(GlobalSettings.getProperty(key).trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static boolean readBool(String key, boolean fallback) {
        if (!GlobalSettings.hasProperty(key)) {
            return fallback;
        }
        return GlobalSettings.getBooleanProperty(key, fallback);
    }

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
