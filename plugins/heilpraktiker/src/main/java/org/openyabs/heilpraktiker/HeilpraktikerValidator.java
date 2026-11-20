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
package org.openyabs.heilpraktiker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openyabs.heilpraktiker.GebuehCatalog.Entry;
import org.openyabs.heilpraktiker.GebuehCatalog.ParsedCode;
import org.openyabs.heilpraktiker.HeilpraktikerStore.InvoiceData;
import org.openyabs.heilpraktiker.HeilpraktikerStore.PatientData;
import org.openyabs.heilpraktiker.HeilpraktikerStore.Settings;

/**
 * Pure validation, GDPR/privacy and template-field logic for German
 * Heilpraktiker invoices (GebüH 85/2002 fee ranges & combination rules,
 * § 4 Nr. 14 Buchst. a UStG, § 630a BGB Honorarvereinbarung, § 630f Abs. 3 BGB
 * 10-year retention, Art. 9 GDPR / § 203 StGB diagnosis handling).
 */
public final class HeilpraktikerValidator {

    /**
     * § 630f Abs. 3 BGB: treatment documentation must be kept 10 years after
     * completion of treatment.
     */
    public static final int YEARS_TREATMENT_RETENTION = 10;

    /**
     * § 14b UStG / § 147 AO (since 2025): invoices must be kept 8 years.
     */
    public static final int YEARS_INVOICE_RETENTION = 8;

    private static final Pattern DE_DATE = Pattern.compile("\\b(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4})\\b");
    private static final Pattern ISO_DATE = Pattern.compile("\\b(\\d{4})-(\\d{2})-(\\d{2})\\b");

    public static final class LineItem {

        public String sku = "";
        public String description = "";
        public BigDecimal unitPrice = BigDecimal.ZERO;
        public BigDecimal taxRate = BigDecimal.ZERO;
        public BigDecimal quantity = BigDecimal.ONE;

        public LineItem() {
        }

        public LineItem(String sku, String description, BigDecimal unitPrice, BigDecimal taxRate) {
            this.sku = sku == null ? "" : sku;
            this.description = description == null ? "" : description;
            this.unitPrice = unitPrice == null ? BigDecimal.ZERO : unitPrice;
            this.taxRate = taxRate == null ? BigDecimal.ZERO : taxRate;
        }
    }

    private HeilpraktikerValidator() {
    }

    /**
     * @return {@code true} for insurance types that typically require a
     * diagnosis on the invoice for reimbursement (PKV, Beihilfe,
     * Zusatzversicherung)
     */
    public static boolean isReimbursementInsurance(String insuranceType) {
        if (isBlank(insuranceType)) {
            return false;
        }
        String lower = insuranceType.trim().toLowerCase(Locale.ROOT);
        return lower.contains("pkv") || lower.contains("beihilfe") || lower.contains("zusatz");
    }

    /**
     * Resolves whether the diagnosis (ICD-10 and plain text) should be printed
     * on the invoice:
     * <ol>
     * <li>Explicit invoice setting ({@link InvoiceData#printDiagnosis}) wins,</li>
     * <li>otherwise explicit patient default ({@link PatientData#printDiagnosis}),</li>
     * <li>otherwise {@code true} for PKV/Beihilfe/Zusatzversicherung and
     * {@code false} for Selbstzahler.</li>
     * </ol>
     */
    public static boolean shouldPrintDiagnosis(PatientData patient, InvoiceData invoice) {
        if (invoice != null && invoice.printDiagnosis != null) {
            return invoice.printDiagnosis.booleanValue();
        }
        if (patient != null && patient.printDiagnosis != null) {
            return patient.printDiagnosis.booleanValue();
        }
        return isReimbursementInsurance(patient == null ? null : patient.insuranceType);
    }

    /**
     * @return {@code true} if the invoice has a diagnosis for a non-PKV/Beihilfe
     * patient (e.g. Selbstzahler) and neither the invoice nor the patient has an
     * explicit {@code printDiagnosis} decision yet (so the user should be asked)
     */
    public static boolean needsPrintDiagnosisPrompt(PatientData patient, InvoiceData invoice) {
        if (invoice == null || invoice.printDiagnosis != null) {
            return false;
        }
        if (patient != null && patient.printDiagnosis != null) {
            return false;
        }
        if (isReimbursementInsurance(patient == null ? null : patient.insuranceType)) {
            return false;
        }
        return !isBlank(invoice.diagnosisText) || !isBlank(invoice.icd10Code);
    }

    /**
     * Returns a German GDPR / § 203 StGB warning if an invoice with a printed
     * diagnosis is about to be sent via unencrypted e-mail, or {@code null} if
     * no diagnosis will be included on the invoice.
     */
    public static String mailWarning(PatientData patient, InvoiceData invoice) {
        if (invoice == null) {
            return null;
        }
        if (isBlank(invoice.diagnosisText) && isBlank(invoice.icd10Code)) {
            return null;
        }
        if (!shouldPrintDiagnosis(patient, invoice)) {
            return null;
        }
        return "Achtung \u2013 Gesundheitsdaten (Art. 9 DSGVO / \u00a7 203 StGB):\n\n"
                + "Diese Rechnung enth\u00e4lt eine Diagnose, die auf der Rechnung (PDF) gedruckt wird.\n"
                + "Der Versand per unverschl\u00fcsselter E-Mail ist f\u00fcr besondere Kategorien personenbezogener Daten "
                + "ohne ausdr\u00fcckliche Einwilligung bzw. Inhaltsverschl\u00fcsselung datenschutzrechtlich problematisch.\n\n"
                + "Alternativen: Postversand, passwortgesch\u00fctztes PDF oder Option \u201eDiagnose auf Rechnung drucken\u201c "
                + "f\u00fcr Selbstzahler deaktivieren.\n\n"
                + "Rechnung trotzdem per E-Mail versenden?";
    }

    /**
     * Tax-exempt medical treatments under § 4 Nr. 14 UStG do not require an
     * e-invoice (§ 14 Abs. 2 UStG). Returns {@code true} if e-invoice delivery
     * should be skipped in favour of a plain PDF.
     */
    public static boolean shouldSkipEInvoice(InvoiceData invoice, boolean hasHpData, Settings settings) {
        if (settings == null || !settings.skipEInvoiceForTaxExempt) {
            return false;
        }
        return hasHpData && invoice != null && invoice.heilbehandlung;
    }

    /**
     * Extracts the latest valid date from a free-text treatment dates string
     * (supports {@code dd.MM.yyyy} and {@code yyyy-MM-dd}, single dates or
     * lists/ranges).
     *
     * @return the latest date found, or {@code null} if none could be parsed
     */
    public static LocalDate parseLatestTreatmentDate(String treatmentDates) {
        if (isBlank(treatmentDates)) {
            return null;
        }
        LocalDate latest = null;
        Matcher de = DE_DATE.matcher(treatmentDates);
        while (de.find()) {
            try {
                int day = Integer.parseInt(de.group(1));
                int month = Integer.parseInt(de.group(2));
                int year = Integer.parseInt(de.group(3));
                LocalDate d = LocalDate.of(year, month, day);
                if (latest == null || d.isAfter(latest)) {
                    latest = d;
                }
            } catch (Exception ignored) {
            }
        }
        Matcher iso = ISO_DATE.matcher(treatmentDates);
        while (iso.find()) {
            try {
                LocalDate d = LocalDate.parse(iso.group(0), DateTimeFormatter.ISO_LOCAL_DATE);
                if (latest == null || d.isAfter(latest)) {
                    latest = d;
                }
            } catch (Exception ignored) {
            }
        }
        return latest;
    }

    /**
     * Computes the § 630f Abs. 3 BGB retention end date: 10 years after the
     * given treatment date.
     */
    public static LocalDate calculateTreatmentRetentionUntil(LocalDate treatmentDate) {
        if (treatmentDate == null) {
            return null;
        }
        return treatmentDate.plusYears(YEARS_TREATMENT_RETENTION);
    }

    /**
     * Parses an ISO ({@code yyyy-MM-dd}) or German ({@code dd.MM.yyyy}) date
     * string, returning {@code null} if blank or invalid.
     */
    public static LocalDate parseDate(String text) {
        if (isBlank(text)) {
            return null;
        }
        String t = text.trim();
        try {
            return LocalDate.parse(t, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {
        }
        return parseLatestTreatmentDate(t);
    }

    /**
     * @return {@code true} if {@code retentionUntilIso} is a valid date strictly
     * before {@code today}
     */
    public static boolean isRetentionExpired(String retentionUntilIso, LocalDate today) {
        LocalDate until = parseDate(retentionUntilIso);
        LocalDate ref = today == null ? LocalDate.now() : today;
        return until != null && until.isBefore(ref);
    }

    /**
     * Validates a Heilpraktiker invoice and returns human-readable German
     * warnings (empty list if everything looks compliant).
     */
    public static List<String> validate(PatientData patient, InvoiceData invoice, List<LineItem> lines, GebuehCatalog catalog) {
        List<String> warnings = new ArrayList<String>();
        PatientData p = patient == null ? new PatientData() : patient;
        InvoiceData inv = invoice == null ? new InvoiceData() : invoice;

        if (inv.heilbehandlung) {
            if (isBlank(inv.diagnosisText) && isBlank(inv.icd10Code)) {
                warnings.add("Fehlende Diagnose (Klartext / ICD-10-GM): PKV und Beihilfe lehnen Rechnungen ohne Diagnose i. d. R. ab.");
            }
            if (isBlank(p.birthDate)) {
                warnings.add("Fehlendes Geburtsdatum des Patienten (erforderlich f\u00fcr PKV/Beihilfe-Zuordnung).");
            }
            if (isBlank(inv.treatmentDates)) {
                warnings.add("Fehlendes Behandlungsdatum / Behandlungszeitraum (wichtig f\u00fcr Sammelrechnungen).");
            }
        }

        if (lines == null || lines.isEmpty()) {
            return warnings;
        }

        List<String> usedCodes = new ArrayList<String>();
        Map<String, Integer> codeCounts = new HashMap<String, Integer>();

        for (LineItem line : lines) {
            if (line == null) {
                continue;
            }
            // VAT rate plausibility check
            BigDecimal tax = line.taxRate == null ? BigDecimal.ZERO : line.taxRate;
            if (inv.heilbehandlung && tax.signum() > 0) {
                warnings.add("Position '" + label(line) + "' hat " + tax.stripTrailingZeros().toPlainString()
                        + " % USt: Medizinisch indizierte Heilbehandlungen sind nach \u00a7 4 Nr. 14 Buchst. a UStG steuerfrei (0 %).");
            } else if (!inv.heilbehandlung && tax.signum() == 0) {
                warnings.add("Position '" + label(line) + "' hat 0 % USt, ist aber nicht als Heilbehandlung markiert "
                        + "(Pr\u00e4vention/Wellness unterliegt i. d. R. 19 % USt, au\u00dfer bei Kleinunternehmerregelung \u00a7 19 UStG).");
            }

            if (catalog == null) {
                continue;
            }
            ParsedCode parsed = catalog.parse(line.sku);
            if (parsed == null) {
                parsed = catalog.parse(line.description);
            }
            if (parsed == null) {
                continue;
            }
            Entry entry = catalog.get(parsed.code);
            if (entry == null) {
                continue;
            }
            usedCodes.add(entry.code);
            codeCounts.put(entry.code, codeCounts.getOrDefault(entry.code, 0) + 1);

            // Fee upper-bound check against GebüH maxFee
            BigDecimal price = line.unitPrice == null ? BigDecimal.ZERO : line.unitPrice;
            if (price.compareTo(entry.maxFee) > 0 && !p.honorarvereinbarung) {
                warnings.add("Geb\u00fcH " + (parsed.analog ? "A " : "") + entry.code + " (" + price.toPlainString()
                        + " EUR) \u00fcberschreitet den Geb\u00fcH-H\u00f6chstsatz (" + entry.maxFee.toPlainString()
                        + " EUR) ohne hinterlegte Honorarvereinbarung (\u00a7 630a BGB).");
            }
        }

        // Combination & exclusion rules
        Set<String> uniqueCodes = new HashSet<String>(usedCodes);
        Set<String> reportedPairs = new HashSet<String>();
        for (String code : uniqueCodes) {
            Entry entry = catalog.get(code);
            if (entry == null) {
                continue;
            }
            if (entry.aloneOnly && uniqueCodes.size() > 1) {
                warnings.add("Geb\u00fcH " + code + " (" + entry.title + ") ist nur als alleinige Leistung berechnungsf\u00e4hig.");
            }
            if (!entry.onlyAllowedWith.isEmpty() && uniqueCodes.size() > 1) {
                for (String other : uniqueCodes) {
                    if (!other.equals(code) && !entry.onlyAllowedWith.contains(other)) {
                        warnings.add("Geb\u00fcH " + code + " wird i. d. R. nur allein oder neben "
                                + String.join(" / ", entry.onlyAllowedWith) + " erstattet (kombiniert mit " + other + ").");
                        break;
                    }
                }
            }
            for (String excl : entry.excludesWith) {
                if (uniqueCodes.contains(excl)) {
                    String pair = code.compareTo(excl) < 0 ? code + "|" + excl : excl + "|" + code;
                    if (reportedPairs.add(pair)) {
                        warnings.add("Unzul\u00e4ssige Kombination: Geb\u00fcH " + code + " und Geb\u00fcH " + excl
                                + " schlie\u00dfen sich gegenseitig aus.");
                    }
                }
            }
            int count = codeCounts.getOrDefault(code, 0);
            if (entry.maxPerCase > 0 && count > entry.maxPerCase && uniqueCodes.size() > 1) {
                warnings.add("Geb\u00fcH " + code + " ist " + count + "-mal enthalten (neben anderen Leistungen i. d. R. maximal "
                        + entry.maxPerCase + "-mal pro Behandlungsfall erstattungsf\u00e4hig).");
            }
            if (entry.maxPerSession > 0 && count > entry.maxPerSession) {
                warnings.add("Geb\u00fcH " + code + " ist " + count + "-mal enthalten (maximal "
                        + entry.maxPerSession + "-mal je Sitzung berechnungsf\u00e4hig).");
            }
        }
        return warnings;
    }

    /**
     * Builds the ODT/PDF template placeholders ({@code hp.*}) for a patient and
     * invoice. Respects {@link #shouldPrintDiagnosis(PatientData, InvoiceData)}
     * so diagnoses are omitted when printing is disabled (Art. 9 GDPR).
     */
    public static Map<String, Object> templateFields(PatientData patient, InvoiceData invoice, Settings settings) {
        PatientData p = patient == null ? new PatientData() : patient;
        InvoiceData inv = invoice == null ? new InvoiceData() : invoice;
        Settings s = settings == null ? new Settings() : settings;
        boolean printDiag = shouldPrintDiagnosis(p, inv);

        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("hp.patientDob", safe(p.birthDate));
        m.put("hp.insuranceType", isBlank(p.insuranceType) ? "Selbstzahler" : p.insuranceType.trim());
        m.put("hp.honorarvereinbarung", p.honorarvereinbarung ? "ja" : "nein");
        m.put("hp.printDiagnosis", printDiag ? "ja" : "nein");
        m.put("hp.icd10", printDiag ? safe(inv.icd10Code) : "");
        m.put("hp.diagnosis", printDiag ? safe(inv.diagnosisText) : "");
        m.put("hp.treatmentDates", safe(inv.treatmentDates));
        m.put("hp.ustgNotice", inv.heilbehandlung ? safe(s.ustgNotice) : "");
        m.put("hp.practitionerTitle", safe(s.practitionerTitle));
        m.put("hp.licenseAuthority", safe(s.licenseAuthority));
        return m;
    }

    private static String label(LineItem l) {
        if (!isBlank(l.sku)) {
            return l.sku.trim();
        }
        return isBlank(l.description) ? "(ohne Bezeichnung)" : l.description.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
