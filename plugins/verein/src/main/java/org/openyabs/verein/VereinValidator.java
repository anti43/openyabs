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
package org.openyabs.verein;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mpv5.plugin.api.ExtensionContext;
import org.openyabs.verein.VereinStore.InvoiceData;
import org.openyabs.verein.VereinStore.MemberData;
import org.openyabs.verein.VereinStore.Settings;

/**
 * Pure validation, German BMF amount-in-words conversion, 2026 German
 * non-profit threshold checks (§ 64 Abs. 3 AO 50k EUR, § 55 Abs. 1 Nr. 5 AO
 * 100k EUR, § 63 Abs. 5 AO 5y/3y, § 50 Abs. 4 EStDV 300 EUR, § 10b Abs. 1 S. 8
 * EStG), and {@code [ev.*]} template placeholder generation.
 */
public final class VereinValidator {

    /** 2026 § 64 Abs. 3 AO threshold (Steueränderungsgesetz 2025, from 01.01.2026). */
    public static final BigDecimal COMMERCIAL_THRESHOLD_2026 = new BigDecimal("50000.00");
    /** 2026 § 55 Abs. 1 Nr. 5 AO timely-use exemption threshold (from 01.01.2026). */
    public static final BigDecimal TIMELY_USE_THRESHOLD_2026 = new BigDecimal("100000.00");
    /** § 50 Abs. 4 EStDV simplified donation proof limit per donation. */
    public static final BigDecimal SIMPLIFIED_PROOF_LIMIT = new BigDecimal("300.00");

    public static final DateTimeFormatter DE_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final String[] ONES = {
        "", "ein", "zwei", "drei", "vier", "f\u00fcnf", "sechs", "sieben", "acht", "neun",
        "zehn", "elf", "zw\u00f6lf", "dreizehn", "vierzehn", "f\u00fcnfzehn", "sechzehn",
        "siebzehn", "achtzehn", "neunzehn"
    };
    private static final String[] TENS = {
        "", "", "zwanzig", "drei\u00dfig", "vierzig", "f\u00fcnfzig",
        "sechzig", "siebzig", "achtzig", "neunzig"
    };

    private VereinValidator() {
    }

    /**
     * Returns true if the member should receive an annual dues invoice for
     * {@code billingYear}. Skips {@code PASSIVE_FREE}, {@code HONORARY},
     * {@code EXITED}, members without a {@code tierSku}, and members whose
     * {@code exitDate} is prior to {@code billingYear-01-01}.
     */
    public static boolean isBillableForYear(MemberData m, int billingYear) {
        if (m == null || safe(m.tierSku).isEmpty()) {
            return false;
        }
        String st = VereinStore.normStatus(m.status);
        if (VereinStore.STATUS_PASSIVE_FREE.equals(st)
                || VereinStore.STATUS_HONORARY.equals(st)
                || VereinStore.STATUS_EXITED.equals(st)) {
            return false;
        }
        LocalDate exit = parseFlexibleDate(m.exitDate);
        if (exit != null && exit.isBefore(LocalDate.of(billingYear, 1, 1))) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether the association's tax-exemption notice is expired on
     * {@code today} per § 63 Abs. 5 AO (exact-day 5 years for
     * Freistellungsbescheid, 3 years for § 60a AO Feststellungsbescheid).
     */
    public static boolean isBescheidExpired(Settings s, LocalDate today) {
        if (s == null || safe(s.freistellungDate).isEmpty() || today == null) {
            return false;
        }
        LocalDate issued = parseDeDate(s.freistellungDate);
        if (issued == null) {
            return false;
        }
        int maxYears = VereinStore.BESCHEID_60A.equalsIgnoreCase(s.bescheidType) ? 3 : 5;
        return !issued.plusYears(maxYears).isAfter(today);
    }

    /**
     * Validates a German association invoice or donation receipt and returns
     * localized warnings via {@link ExtensionContext#tr(String, Object...)}.
     */
    public static List<String> validate(
            MemberData member,
            InvoiceData inv,
            List<BigDecimal> lineTaxRates,
            BigDecimal grossTotal,
            Settings settings,
            LocalDate today,
            ExtensionContext yabs) {
        List<String> warnings = new ArrayList<String>();
        Settings s = settings == null ? new Settings() : settings;
        InvoiceData d = inv == null ? new InvoiceData() : inv;
        LocalDate now = today == null ? LocalDate.now() : today;
        String sphere = VereinStore.normSphere(d.taxSphere);

        // VAT checks per sphere
        if (lineTaxRates != null) {
            if (VereinStore.SPHERE_IDEELL.equals(sphere)) {
                for (BigDecimal rate : lineTaxRates) {
                    if (rate != null && rate.signum() > 0) {
                        warnings.add(tr(yabs, "val.ideellVat", rate.stripTrailingZeros().toPlainString()));
                        break;
                    }
                }
            } else if (VereinStore.SPHERE_WIRTSCHAFTLICH.equals(sphere) && !s.kleinunternehmer) {
                boolean hasZero = false;
                for (BigDecimal rate : lineTaxRates) {
                    if (rate == null || rate.signum() == 0) {
                        hasZero = true;
                        break;
                    }
                }
                if (hasZero) {
                    warnings.add(tr(yabs, "val.wirtschaftVat"));
                }
            }
        }

        // SEPA direct debit validation
        if (d.sepaDirectDebit) {
            if (member == null || safe(member.sepaMandateRef).isEmpty() || safe(member.sepaMandateDate).isEmpty()) {
                warnings.add(tr(yabs, "val.sepaMandateMissing"));
            }
            if (safe(s.creditorId).isEmpty()) {
                warnings.add(tr(yabs, "val.sepaCreditorMissing"));
            }
        }

        // Donation receipt (§ 10b EStG, § 63 Abs. 5 AO, § 50 Abs. 4 EStDV)
        if (d.isDonation) {
            if (safe(s.freistellungDate).isEmpty()) {
                warnings.add(tr(yabs, "val.freistellungMissing"));
            } else {
                LocalDate issued = parseDeDate(s.freistellungDate);
                if (issued == null) {
                    warnings.add(tr(yabs, "val.freistellungInvalid", s.freistellungDate));
                } else {
                    int maxYears = VereinStore.BESCHEID_60A.equalsIgnoreCase(s.bescheidType) ? 3 : 5;
                    if (!issued.plusYears(maxYears).isAfter(now)) {
                        warnings.add(tr(yabs, "val.freistellungExpired", s.freistellungDate, maxYears));
                    }
                }
            }
            if (VereinStore.DONATION_MITGLIEDSBEITRAG.equalsIgnoreCase(d.donationKind) && !s.duesTaxDeductible) {
                warnings.add(tr(yabs, "val.duesNotDeductible"));
            }
            if (grossTotal != null && grossTotal.signum() > 0 && grossTotal.compareTo(SIMPLIFIED_PROOF_LIMIT) <= 0) {
                warnings.add(tr(yabs, "val.simplifiedProofHint",
                        grossTotal.setScale(2, RoundingMode.HALF_UP).toPlainString()));
            }
        }
        return warnings;
    }

    /**
     * Builds all {@code [ev.*]} placeholders for ODT/FODT/PDF templates.
     */
    public static Map<String, Object> templateFields(
            MemberData member,
            InvoiceData inv,
            BigDecimal grossTotal,
            Settings settings,
            ExtensionContext yabs) {
        Settings s = settings == null ? new Settings() : settings;
        InvoiceData d = inv == null ? new InvoiceData() : inv;
        Map<String, Object> m = new LinkedHashMap<String, Object>();

        String memberId = member == null ? "" : safe(member.memberId);
        String statusKey = member == null ? VereinStore.STATUS_ACTIVE : VereinStore.normStatus(member.status);
        String mandateRef = member == null ? "" : safe(member.sepaMandateRef);
        String mandateDate = member == null ? "" : safe(member.sepaMandateDate);
        String creditorId = safe(s.creditorId);

        String sepaNotice = (!mandateRef.isEmpty() && (d.sepaDirectDebit || !mandateDate.isEmpty()))
                ? tr(yabs, "sepa.notice.debit", creditorId, mandateRef, mandateDate)
                : tr(yabs, "sepa.notice.transfer", memberId);

        m.put("ev.memberId", memberId);
        m.put("ev.status", tr(yabs, "status." + statusKey));
        m.put("ev.joinDate", member == null ? "" : safe(member.joinDate));
        m.put("ev.sepaMandateRef", mandateRef);
        m.put("ev.sepaMandateDate", mandateDate);
        m.put("ev.creditorId", creditorId);
        m.put("ev.sepaPreNotification", sepaNotice);
        m.put("ev.taxSphere", tr(yabs, "sphere." + VereinStore.normSphere(d.taxSphere)));
        m.put("ev.fiscalYear", safe(d.fiscalYear).isEmpty() ? String.valueOf(LocalDate.now().getYear()) : safe(d.fiscalYear));
        m.put("ev.finanzamt", safe(s.finanzamt));
        m.put("ev.taxNumber", safe(s.taxNumber));
        m.put("ev.bescheidType", tr(yabs, "settings.bescheid."
                + (VereinStore.BESCHEID_60A.equalsIgnoreCase(s.bescheidType)
                        ? VereinStore.BESCHEID_60A : VereinStore.BESCHEID_FREISTELLUNG)));
        m.put("ev.freistellungDate", safe(s.freistellungDate));
        m.put("ev.freistellungPeriod", safe(s.freistellungPeriod));
        m.put("ev.purposeClause", safe(s.purposeClause));
        m.put("ev.amountInWords", amountToGermanWords(grossTotal == null ? BigDecimal.ZERO : grossTotal));
        String dk = VereinStore.DONATION_MITGLIEDSBEITRAG.equalsIgnoreCase(d.donationKind)
                ? VereinStore.DONATION_MITGLIEDSBEITRAG : VereinStore.DONATION_SPENDE;
        m.put("ev.donationKind", tr(yabs, "donationKind." + dk));
        m.put("ev.waiverNotice", d.waiverReimbursement ? tr(yabs, "waiver.yes") : tr(yabs, "waiver.no"));
        return m;
    }

    /**
     * Converts a EUR amount into real German number words as required on
     * official BMF Zuwendungsbestätigungen (supports 0 to 999,999,999.99 EUR).
     */
    public static String amountToGermanWords(BigDecimal amount) {
        if (amount == null) {
            return "null Euro";
        }
        BigDecimal abs = amount.abs().setScale(2, RoundingMode.HALF_UP);
        long euros = abs.longValue();
        int cents = abs.remainder(BigDecimal.ONE).movePointRight(2).intValue();

        StringBuilder sb = new StringBuilder();
        if (euros == 0) {
            sb.append("null Euro");
        } else if (euros == 1) {
            sb.append("ein Euro");
        } else {
            sb.append(numberToGermanWords(euros)).append(" Euro");
        }
        if (cents > 0) {
            sb.append(" ").append(cents == 1 ? "ein Cent" : numberToGermanWords(cents) + " Cent");
        }
        return sb.toString();
    }

    static String numberToGermanWords(long n) {
        if (n == 0) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        long millions = n / 1_000_000;
        long rem = n % 1_000_000;
        if (millions > 0) {
            if (millions == 1) {
                sb.append("eine Million");
            } else {
                sb.append(underThousand((int) millions)).append(" Millionen");
            }
            if (rem > 0) {
                sb.append(" ");
            }
        }
        long thousands = rem / 1_000;
        int rest = (int) (rem % 1_000);
        if (thousands > 0) {
            if (thousands == 1) {
                sb.append("eintausend");
            } else {
                sb.append(underThousand((int) thousands)).append("tausend");
            }
        }
        if (rest > 0) {
            sb.append(underThousand(rest));
        }
        return sb.toString();
    }

    private static String underThousand(int n) {
        StringBuilder sb = new StringBuilder();
        int hundreds = n / 100;
        int rem = n % 100;
        if (hundreds > 0) {
            sb.append(ONES[hundreds]).append("hundert");
        }
        if (rem > 0) {
            if (rem < 20) {
                sb.append(rem == 1 ? "eins" : ONES[rem]);
            } else {
                int ones = rem % 10;
                int tens = rem / 10;
                if (ones > 0) {
                    sb.append(ONES[ones]).append("und");
                }
                sb.append(TENS[tens]);
            }
        }
        return sb.toString();
    }

    /**
     * Generates a UTF-8 semicolon-separated CSV summary for the 4 non-profit
     * tax spheres.
     */
    public static String buildSphereReportCsv(Map<String, BigDecimal> totals, ExtensionContext yabs) {
        StringBuilder sb = new StringBuilder("Steuersphaere;Einnahmen_Brutto_EUR\r\n");
        for (String sp : VereinStore.SPHERES) {
            BigDecimal sum = totals == null || totals.get(sp) == null ? BigDecimal.ZERO : totals.get(sp);
            sb.append(tr(yabs, "sphere." + sp))
                    .append(';')
                    .append(sum.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ','))
                    .append("\r\n");
        }
        return sb.toString();
    }

    public static LocalDate parseDeDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim(), DE_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    static LocalDate parseFlexibleDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String s = raw.trim();
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException ignored) {
        }
        return parseDeDate(s);
    }

    private static String tr(ExtensionContext yabs, String key, Object... args) {
        return yabs == null ? key : yabs.tr(key, args);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
