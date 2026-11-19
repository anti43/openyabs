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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openyabs.heilpraktiker.GebuehCatalog.Entry;
import org.openyabs.heilpraktiker.GebuehCatalog.ParsedCode;
import org.openyabs.heilpraktiker.HeilpraktikerStore.InvoiceData;
import org.openyabs.heilpraktiker.HeilpraktikerStore.PatientData;
import org.openyabs.heilpraktiker.HeilpraktikerStore.Settings;

/**
 * Pure validation and template-field logic for German Heilpraktiker invoices
 * (GebüH 85/2002 fee ranges & combination rules, § 4 Nr. 14 Buchst. a UStG,
 * § 630a BGB Honorarvereinbarung, PKV/Beihilfe mandatory fields).
 */
public final class HeilpraktikerValidator {

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
     * invoice.
     */
    public static Map<String, Object> templateFields(PatientData patient, InvoiceData invoice, Settings settings) {
        PatientData p = patient == null ? new PatientData() : patient;
        InvoiceData inv = invoice == null ? new InvoiceData() : invoice;
        Settings s = settings == null ? new Settings() : settings;

        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("hp.patientDob", safe(p.birthDate));
        m.put("hp.insuranceType", isBlank(p.insuranceType) ? "Selbstzahler" : p.insuranceType.trim());
        m.put("hp.honorarvereinbarung", p.honorarvereinbarung ? "ja" : "nein");
        m.put("hp.icd10", safe(inv.icd10Code));
        m.put("hp.diagnosis", safe(inv.diagnosisText));
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
