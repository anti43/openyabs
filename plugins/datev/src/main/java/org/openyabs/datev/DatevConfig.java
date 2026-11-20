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
    private static final String K_BERATER = PREFIX + "beraternummer";
    private static final String K_MANDANT = PREFIX + "mandantennummer";
    private static final String K_WJ_BEGINN = PREFIX + "wjbeginn";
    private static final String K_SACHKONTENLAENGE = PREFIX + "sachkontenlaenge";
    private static final String K_KONTENRAHMEN = PREFIX + "kontenrahmen";
    private static final String K_BEZEICHNUNG = PREFIX + "bezeichnung";
    private static final String K_DIKTAT = PREFIX + "diktatkuerzel";
    private static final String K_FESTSCHREIBUNG = PREFIX + "festschreibung";
    private static final String K_WAEHRUNG = PREFIX + "waehrung";
    private static final String K_BELEGDATUM_MODE = PREFIX + "belegdatumMode";
    private static final String K_MONEY_INCOME = PREFIX + "moneyAccountIncome";
    private static final String K_MONEY_EXPENSE = PREFIX + "moneyAccountExpense";
    private static final String K_CASH_ACCOUNT = PREFIX + "cashAccount";
    private static final String K_AUTOMATIC = PREFIX + "useAutomaticAccounts";
    private static final String K_EU_ACCOUNT = PREFIX + "euDeliveryAccount";
    private static final String K_EU_SERVICE_ACCOUNT = PREFIX + "euServiceAccount";
    private static final String K_REVERSE_CHARGE_ACCOUNT = PREFIX + "reverseChargeAccount";
    private static final String K_EXPORT_ACCOUNT = PREFIX + "exportAccount";
    private static final String K_REV_PREFIX = PREFIX + "revAccount.";
    private static final String K_EXP_PREFIX = PREFIX + "expAccount.";
    private static final String K_BU_INC_PREFIX = PREFIX + "buKeyIncome.";
    private static final String K_BU_EXP_PREFIX = PREFIX + "buKeyExpense.";

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

    public final Map<BigDecimal, String> revenueAccounts = new TreeMap<BigDecimal, String>(JournalCalc.RATE_ORDER);
    public final Map<BigDecimal, String> expenseAccounts = new TreeMap<BigDecimal, String>(JournalCalc.RATE_ORDER);
    public final Map<BigDecimal, String> buKeysIncome = new TreeMap<BigDecimal, String>(JournalCalc.RATE_ORDER);
    public final Map<BigDecimal, String> buKeysExpense = new TreeMap<BigDecimal, String>(JournalCalc.RATE_ORDER);

    public DatevConfig() {
        applyDefaultBuKeys();
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
                    String found = findDefaultAccountForRate(accounts, Account.INCOME, key, skr04);
                    if (found != null) {
                        revenueAccounts.put(key, found);
                    }
                }
                if (overwriteExisting || isBlank(expenseAccounts.get(key))) {
                    String found = findDefaultAccountForRate(accounts, Account.EXPENSE, key, skr04);
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
            if (a.__getIntaccountclass() > 0 && String.valueOf(a.__getIntaccountclass()).equals(number)) {
                return number;
            }
        }
        return null;
    }

    private static String findDefaultAccountForRate(List<Account> accounts, int accountType, BigDecimal rate, boolean skr04) {
        // Prefer canonical DATEV standard accounts if present in the chart
        String preferred = canonicalAccount(accountType, rate, skr04);
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
                    return String.valueOf(a.__getIntaccountclass());
                }
            }
        }
        return preferred;
    }

    static String canonicalAccount(int accountType, BigDecimal rate, boolean skr04) {
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
                return skr04 ? "4120" : "8120";
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
        c.beraterNummer = readInt(K_BERATER, 0);
        c.mandantenNummer = readInt(K_MANDANT, 0);
        c.wjBeginn = readString(K_WJ_BEGINN, LocalDate.now().getYear() + "0101");
        c.sachkontenLaenge = readInt(K_SACHKONTENLAENGE, 4);
        c.kontenrahmen = readString(K_KONTENRAHMEN, "03");
        c.bezeichnung = readString(K_BEZEICHNUNG, "YaBS Export");
        c.diktatKuerzel = readString(K_DIKTAT, "");
        c.festschreibung = readBool(K_FESTSCHREIBUNG, false);
        c.waehrung = readString(K_WAEHRUNG, "EUR");
        String mode = readString(K_BELEGDATUM_MODE, DateMode.PAYMENT_DATE.name());
        try {
            c.belegdatumMode = DateMode.valueOf(mode);
        } catch (Exception e) {
            c.belegdatumMode = DateMode.PAYMENT_DATE;
        }
        c.moneyAccountIncome = readString(K_MONEY_INCOME, "04".equals(c.kontenrahmen) ? "1800" : "1200");
        c.moneyAccountExpense = readString(K_MONEY_EXPENSE, "04".equals(c.kontenrahmen) ? "1800" : "1200");
        c.cashAccount = readString(K_CASH_ACCOUNT, "");
        c.useAutomaticAccounts = readBool(K_AUTOMATIC, true);
        c.euDeliveryAccount = readString(K_EU_ACCOUNT, "04".equals(c.kontenrahmen) ? "4125" : "8125");
        c.euServiceAccount = readString(K_EU_SERVICE_ACCOUNT, "04".equals(c.kontenrahmen) ? "4336" : "8336");
        c.reverseChargeAccount = readString(K_REVERSE_CHARGE_ACCOUNT, "04".equals(c.kontenrahmen) ? "4337" : "8337");
        c.exportAccount = readString(K_EXPORT_ACCOUNT, "04".equals(c.kontenrahmen) ? "4120" : "8120");

        readRateMap(K_REV_PREFIX, c.revenueAccounts);
        readRateMap(K_EXP_PREFIX, c.expenseAccounts);
        readRateMap(K_BU_INC_PREFIX, c.buKeysIncome);
        readRateMap(K_BU_EXP_PREFIX, c.buKeysExpense);
        c.applyDefaultBuKeys();
        return c;
    }

    /**
     * Saves the DATEV settings to {@link GlobalSettings} (per database).
     */
    public void save() {
        GlobalSettings.setProperty(K_BERATER, String.valueOf(beraterNummer));
        GlobalSettings.setProperty(K_MANDANT, String.valueOf(mandantenNummer));
        GlobalSettings.setProperty(K_WJ_BEGINN, safe(wjBeginn));
        GlobalSettings.setProperty(K_SACHKONTENLAENGE, String.valueOf(sachkontenLaenge));
        GlobalSettings.setProperty(K_KONTENRAHMEN, safe(kontenrahmen));
        GlobalSettings.setProperty(K_BEZEICHNUNG, safe(bezeichnung));
        GlobalSettings.setProperty(K_DIKTAT, safe(diktatKuerzel));
        GlobalSettings.setProperty(K_FESTSCHREIBUNG, String.valueOf(festschreibung));
        GlobalSettings.setProperty(K_WAEHRUNG, isBlank(waehrung) ? "EUR" : waehrung.trim().toUpperCase());
        GlobalSettings.setProperty(K_BELEGDATUM_MODE, (belegdatumMode == null ? DateMode.PAYMENT_DATE : belegdatumMode).name());
        GlobalSettings.setProperty(K_MONEY_INCOME, safe(moneyAccountIncome));
        GlobalSettings.setProperty(K_MONEY_EXPENSE, safe(moneyAccountExpense));
        GlobalSettings.setProperty(K_CASH_ACCOUNT, safe(cashAccount));
        GlobalSettings.setProperty(K_AUTOMATIC, String.valueOf(useAutomaticAccounts));
        GlobalSettings.setProperty(K_EU_ACCOUNT, safe(euDeliveryAccount));
        GlobalSettings.setProperty(K_EU_SERVICE_ACCOUNT, safe(euServiceAccount));
        GlobalSettings.setProperty(K_REVERSE_CHARGE_ACCOUNT, safe(reverseChargeAccount));
        GlobalSettings.setProperty(K_EXPORT_ACCOUNT, safe(exportAccount));

        writeRateMap(K_REV_PREFIX, revenueAccounts);
        writeRateMap(K_EXP_PREFIX, expenseAccounts);
        writeRateMap(K_BU_INC_PREFIX, buKeysIncome);
        writeRateMap(K_BU_EXP_PREFIX, buKeysExpense);
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
