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
package org.openyabs.handwerk;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.plugin.api.ExtensionContext;
import org.openyabs.handwerk.HandwerkStore.ContactMeta;
import org.openyabs.handwerk.HandwerkStore.ItemMeta;
import org.openyabs.handwerk.HandwerkStore.LineCostType;
import org.openyabs.handwerk.HandwerkStore.Measurement;
import org.openyabs.handwerk.HandwerkStore.Settings;
import org.openyabs.handwerk.HandwerkStore.Site;
import org.openyabs.handwerk.HandwerkStore.TimeSheet;

/**
 * Pure domain validation, tax/compliance calculations (§ 35a EStG, § 14b UStG,
 * § 13b UStG, § 48b EStG, VOB/B &amp; BGB § 650m retention/warranty), and
 * {@code [hw.*]} template placeholder generation for the Skilled Trades &amp;
 * Construction plugin.
 */
public final class HandwerkValidator {

    public static final BigDecimal FREISTELLUNG_THRESHOLD = new BigDecimal("5000.00");
    public static final BigDecimal CREDIT_35A_RATE = new BigDecimal("0.20");
    public static final BigDecimal CREDIT_35A_MAX = new BigDecimal("1200.00");
    private static final BigDecimal BD100 = new BigDecimal("100");
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyy-MM");

    private HandwerkValidator() {
    }

    /**
     * Pure line-item input mirroring {@link mpv5.db.objects.SubItem} so all
     * compliance calculations can run headlessly in unit tests.
     */
    public static final class LineInput {

        public int subItemId;
        public String sku = "";
        public String description = "";
        public String unit = "";
        public boolean serviceProduct;
        public boolean textLine;
        public BigDecimal netTotal = BigDecimal.ZERO;
        public BigDecimal taxPercent = BigDecimal.ZERO;

        public LineInput() {
        }

        public LineInput(int subItemId, String sku, String description, String unit,
                boolean serviceProduct, BigDecimal netTotal, BigDecimal taxPercent) {
            this.subItemId = subItemId;
            this.sku = sku == null ? "" : sku;
            this.description = description == null ? "" : description;
            this.unit = unit == null ? "" : unit;
            this.serviceProduct = serviceProduct;
            this.netTotal = netTotal == null ? BigDecimal.ZERO : netTotal;
            this.taxPercent = taxPercent == null ? BigDecimal.ZERO : taxPercent;
        }
    }

    public static final class LaborSplitSummary {

        public BigDecimal laborNet = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        public BigDecimal laborVat = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        public BigDecimal laborGross = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        public BigDecimal materialNet = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        public BigDecimal materialVat = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        public BigDecimal materialGross = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        public BigDecimal estimatedCredit35a = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public static final class RetentionResult {

        public String effectiveBasis = HandwerkStore.BASIS_GROSS;
        public BigDecimal retentionAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        public BigDecimal payableAfterRetention = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Classifies an invoice line into one of the § 35a cost types
     * ({@code LABOR}, {@code TRAVEL}, {@code MACHINE}, {@code MATERIAL},
     * {@code OTHER}), honoring explicit per-line overrides first.
     */
    public static String classifyLine(LineInput line, LineCostType override) {
        if (override != null && override.costType != null && !override.costType.trim().isEmpty()) {
            return override.costType.trim().toUpperCase(Locale.ROOT);
        }
        if (line == null || line.textLine) {
            return HandwerkStore.COST_OTHER;
        }
        String sku = safe(line.sku).toUpperCase(Locale.ROOT);
        String desc = safe(line.description).toLowerCase(Locale.ROOT);
        String unit = safe(line.unit).toLowerCase(Locale.ROOT);

        if (sku.startsWith("HW-ANFAHRT") || sku.startsWith("HW-TRAVEL")
                || desc.contains("anfahrt") || desc.contains("fahrtkost")
                || desc.contains("kilometer") || desc.contains("travel")
                || "km".equals(unit)) {
            return HandwerkStore.COST_TRAVEL;
        }
        if (sku.startsWith("HW-MASCHINE") || sku.startsWith("HW-MACHINE")
                || desc.contains("maschine") || desc.contains("ger\u00fcst")
                || desc.contains("bagger") || desc.contains("equipment")
                || desc.contains("crane") || desc.contains("scaffold")) {
            return HandwerkStore.COST_MACHINE;
        }
        if (sku.startsWith("HW-MEISTER") || sku.startsWith("HW-GESELLE")
                || sku.startsWith("HW-AZUBI") || sku.startsWith("HW-LOHN")
                || sku.startsWith("HW-LABOR") || line.serviceProduct
                || "std".equals(unit) || "std.".equals(unit) || "h".equals(unit) || "hr".equals(unit)
                || desc.contains("meister") || desc.contains("geselle") || desc.contains("azubi")
                || desc.contains("arbeitszeit") || desc.contains("arbeitslohn") || desc.contains("lohn")
                || desc.contains("montage") || desc.contains("wartung") || desc.contains("reparatur")
                || desc.contains("stunde") || desc.contains("regie") || desc.contains("labor")
                || desc.contains("service") || desc.contains("installation")) {
            return HandwerkStore.COST_LABOR;
        }
        return HandwerkStore.COST_MATERIAL;
    }

    public static boolean isTaxDeductible35a(String costType) {
        return HandwerkStore.COST_LABOR.equals(costType)
                || HandwerkStore.COST_TRAVEL.equals(costType)
                || HandwerkStore.COST_MACHINE.equals(costType);
    }

    /**
     * Computes § 35a EStG labor/travel/machine vs. material net, VAT, gross,
     * and estimated 20 % income tax credit (capped at EUR 1,200.00 per year).
     */
    public static LaborSplitSummary computeLaborSplit(
            List<LineInput> lines,
            Map<Integer, LineCostType> overrides,
            BigDecimal docDiscountPercent) {
        LaborSplitSummary s = new LaborSplitSummary();
        if (lines == null || lines.isEmpty()) {
            return s;
        }
        Map<Integer, LineCostType> map = overrides == null ? Collections.emptyMap() : overrides;
        BigDecimal factor = BigDecimal.ONE;
        if (docDiscountPercent != null && docDiscountPercent.signum() > 0) {
            factor = BigDecimal.ONE.subtract(docDiscountPercent.divide(BD100, 6, RoundingMode.HALF_UP));
            if (factor.signum() < 0) {
                factor = BigDecimal.ZERO;
            }
        }

        BigDecimal laborNet = BigDecimal.ZERO;
        BigDecimal laborVat = BigDecimal.ZERO;
        BigDecimal matNet = BigDecimal.ZERO;
        BigDecimal matVat = BigDecimal.ZERO;

        for (LineInput line : lines) {
            if (line == null || line.textLine || line.netTotal == null) {
                continue;
            }
            BigDecimal lineNet = line.netTotal.multiply(factor);
            BigDecimal taxRate = line.taxPercent == null ? BigDecimal.ZERO : line.taxPercent;
            BigDecimal lineVat = lineNet.multiply(taxRate).divide(BD100, 6, RoundingMode.HALF_UP);

            LineCostType ov = map.get(line.subItemId);
            String type = classifyLine(line, ov);
            BigDecimal laborShare;
            if (ov != null && ov.laborSharePercent != null) {
                laborShare = clampPercent(ov.laborSharePercent).divide(BD100, 6, RoundingMode.HALF_UP);
                if (!isTaxDeductible35a(type) && ov.laborSharePercent.compareTo(BD100) == 0) {
                    laborShare = BigDecimal.ZERO;
                }
            } else {
                laborShare = isTaxDeductible35a(type) ? BigDecimal.ONE : BigDecimal.ZERO;
            }
            BigDecimal matShare = BigDecimal.ONE.subtract(laborShare);

            laborNet = laborNet.add(lineNet.multiply(laborShare));
            laborVat = laborVat.add(lineVat.multiply(laborShare));
            matNet = matNet.add(lineNet.multiply(matShare));
            matVat = matVat.add(lineVat.multiply(matShare));
        }

        s.laborNet = laborNet.setScale(2, RoundingMode.HALF_UP);
        s.laborVat = laborVat.setScale(2, RoundingMode.HALF_UP);
        s.laborGross = s.laborNet.add(s.laborVat).setScale(2, RoundingMode.HALF_UP);
        s.materialNet = matNet.setScale(2, RoundingMode.HALF_UP);
        s.materialVat = matVat.setScale(2, RoundingMode.HALF_UP);
        s.materialGross = s.materialNet.add(s.materialVat).setScale(2, RoundingMode.HALF_UP);

        if (s.laborGross.signum() > 0) {
            BigDecimal credit = s.laborGross.multiply(CREDIT_35A_RATE).setScale(2, RoundingMode.HALF_UP);
            s.estimatedCredit35a = credit.min(CREDIT_35A_MAX);
        }
        return s;
    }

    /**
     * Computes VOB/B / BGB § 650m retention (*Sicherheitseinbehalt*) and payable
     * amount after retention. Under § 13b UStG (Reverse Charge), VOB/B § 17
     * requires calculating retention on the net amount.
     */
    public static RetentionResult computeRetention(BigDecimal netTotal, BigDecimal grossTotal, ItemMeta meta) {
        RetentionResult res = new RetentionResult();
        BigDecimal net = netTotal == null ? BigDecimal.ZERO : netTotal;
        BigDecimal gross = grossTotal == null ? net : grossTotal;

        if (meta == null) {
            res.payableAfterRetention = gross.setScale(2, RoundingMode.HALF_UP);
            return res;
        }
        boolean forceNet = meta.reverseChargeEnabled || HandwerkStore.BASIS_NET.equalsIgnoreCase(meta.retentionBasis);
        res.effectiveBasis = forceNet ? HandwerkStore.BASIS_NET : HandwerkStore.BASIS_GROSS;

        BigDecimal base = forceNet ? net : gross;
        BigDecimal ret;
        if (meta.retentionPercent != null && meta.retentionPercent.signum() > 0) {
            ret = base.multiply(meta.retentionPercent).divide(BD100, 2, RoundingMode.HALF_UP);
        } else if (meta.retentionAmount != null && meta.retentionAmount.signum() > 0) {
            ret = meta.retentionAmount.setScale(2, RoundingMode.HALF_UP);
        } else {
            ret = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        res.retentionAmount = ret;
        BigDecimal payableBase = meta.reverseChargeEnabled ? net : gross;
        res.payableAfterRetention = payableBase.subtract(ret).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return res;
    }

    /**
     * Computes the statutory warranty expiry date from the acceptance date
     * (4 years for VOB/B § 13 Abs. 4, 5 years for BGB § 634a Abs. 1 Nr. 2, or
     * custom positive {@code warrantyYears}).
     */
    public static String computeWarrantyUntil(String acceptanceDate, String contractType, int warrantyYears) {
        LocalDate acc = parseDate(acceptanceDate);
        if (acc == null) {
            return "";
        }
        int years = warrantyYears > 0
                ? warrantyYears
                : (HandwerkStore.CONTRACT_VOB_B.equalsIgnoreCase(contractType) ? 4 : 5);
        return acc.plusYears(years).toString();
    }

    public static LocalDate parseDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String s = raw.trim();
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(s, DMY);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return YearMonth.parse(s, YM).atEndOfMonth();
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }

    public static boolean isCertificateExpired(String validUntil, LocalDate today) {
        LocalDate d = parseDate(validUntil);
        if (d == null) {
            return false;
        }
        LocalDate ref = today == null ? LocalDate.now() : today;
        return d.isBefore(ref);
    }

    public static boolean isCertificateExpiringSoon(String validUntil, int leadDays, LocalDate today) {
        LocalDate d = parseDate(validUntil);
        if (d == null) {
            return false;
        }
        LocalDate ref = today == null ? LocalDate.now() : today;
        return !d.isBefore(ref) && !d.isAfter(ref.plusDays(Math.max(1, leadDays)));
    }

    /**
     * Runs all statutory German &amp; European skilled-trades compliance checks
     * and returns a list of localized warning messages (empty when all checks
     * pass).
     */
    public static List<String> validate(
            int itemType,
            ContactMeta contactMeta,
            Site site,
            ItemMeta itemMeta,
            List<LineInput> lines,
            Map<Integer, LineCostType> overrides,
            BoqDocument boq,
            BigDecimal documentGross,
            BigDecimal annualCustomerVolume,
            Settings settings,
            LocalDate today,
            ExtensionContext yabs) {
        List<String> warnings = new ArrayList<String>();
        ContactMeta cm = contactMeta == null ? new ContactMeta() : contactMeta;
        ItemMeta im = itemMeta == null ? new ItemMeta() : itemMeta;
        Settings cfg = settings == null ? new Settings() : settings;
        LocalDate ref = today == null ? LocalDate.now() : today;
        List<LineInput> safeLines = lines == null ? Collections.emptyList() : lines;

        boolean isBillingDoc = itemType == Item.TYPE_INVOICE
                || itemType == Item.TYPE_PART_PAYMENT
                || itemType == Item.TYPE_DEPOSIT;

        boolean hasPositiveVat = false;
        for (LineInput l : safeLines) {
            if (l != null && !l.textLine && l.taxPercent != null && l.taxPercent.signum() > 0) {
                hasPositiveVat = true;
                break;
            }
        }

        // 1. § 35a EStG checks
        if (im.laborTaxCreditEnabled) {
            if (im.cashPayment) {
                warnings.add(tr(yabs, "val.35a.cash",
                        "§ 35a EStG: Cash payment is NOT tax-deductible. Bank transfer is required."));
            }
            LaborSplitSummary split = computeLaborSplit(safeLines, overrides, BigDecimal.ZERO);
            if (!safeLines.isEmpty() && split.laborGross.signum() <= 0) {
                warnings.add(tr(yabs, "val.35a.noLabor",
                        "§ 35a EStG: Labor/travel/machine share is 0.00. Classify labor lines or check positions."));
            }
        }

        // 2. § 14b Abs. 1 S. 5 UStG 2-year retention notice for private property work
        if (isBillingDoc
                && HandwerkStore.CUSTOMER_PRIVATE.equalsIgnoreCase(cm.customerKind)
                && !im.privateRetentionNotice) {
            warnings.add(tr(yabs, "val.14b.missing",
                    "§ 14b Abs. 1 S. 5 UStG: Enable the 2-year invoice retention notice for private customers."));
        }

        // 3. § 13b UStG Reverse Charge checks
        boolean hasValidUst1Tg = !safe(cm.reverseChargeCertNo).isEmpty()
                && !safe(cm.reverseChargeCertUntil).isEmpty()
                && !isCertificateExpired(cm.reverseChargeCertUntil, ref);

        if (im.reverseChargeEnabled) {
            if (safe(cm.reverseChargeCertNo).isEmpty() || safe(cm.reverseChargeCertUntil).isEmpty()) {
                warnings.add(tr(yabs, "val.13b.missingCert",
                        "§ 13b UStG: Customer USt 1 TG certificate is missing."));
            } else if (isCertificateExpired(cm.reverseChargeCertUntil, ref)) {
                warnings.add(tr(yabs, "val.13b.expiredCert",
                        "§ 13b UStG: Customer USt 1 TG certificate expired on {0}.",
                        cm.reverseChargeCertUntil));
            }
            if (hasPositiveVat) {
                warnings.add(tr(yabs, "val.13b.nonzeroVat",
                        "§ 13b UStG: Reverse Charge is enabled, but at least one line still carries VAT > 0%."));
            }
        } else if (isBillingDoc
                && HandwerkStore.CUSTOMER_CONSTRUCTION_13B.equalsIgnoreCase(cm.customerKind)
                && hasValidUst1Tg
                && hasPositiveVat) {
            warnings.add(tr(yabs, "val.13b.expected",
                    "§ 13b UStG: Customer is marked as construction business (USt 1 TG valid until {0}), but VAT > 0% is billed.",
                    cm.reverseChargeCertUntil));
        }

        // 4. § 48b EStG Bauabzugssteuer (15 % withholding tax) check
        boolean isCommercialCustomer = !HandwerkStore.CUSTOMER_PRIVATE.equalsIgnoreCase(cm.customerKind);
        BigDecimal gross = documentGross == null ? BigDecimal.ZERO : documentGross;
        BigDecimal volume = annualCustomerVolume == null ? gross : annualCustomerVolume.max(gross);
        if (isBillingDoc && isCommercialCustomer && volume.compareTo(FREISTELLUNG_THRESHOLD) > 0) {
            if (safe(cfg.ownFreistellungTaxNo).isEmpty() || safe(cfg.ownFreistellungValidUntil).isEmpty()) {
                warnings.add(tr(yabs, "val.48b.missingOwn",
                        "§ 48b EStG: Commercial invoice/annual volume exceeds EUR 5,000, but your own Freistellungsbescheinigung is not configured in plugin settings."));
            } else if (isCertificateExpired(cfg.ownFreistellungValidUntil, ref)) {
                warnings.add(tr(yabs, "val.48b.expiredOwn",
                        "§ 48b EStG: Your own Freistellungsbescheinigung expired on {0}. Customer may withhold 15 % Bauabzugssteuer.",
                        cfg.ownFreistellungValidUntil));
            }
        }

        // 5. VOB/B & BGB § 650m retention cap checks
        if (im.retentionPercent != null && im.retentionPercent.signum() > 0) {
            if (HandwerkStore.CUSTOMER_PRIVATE.equalsIgnoreCase(cm.customerKind)
                    && im.retentionPercent.compareTo(new BigDecimal("5.00")) > 0) {
                warnings.add(tr(yabs, "val.retention.consumerCap",
                        "BGB § 650m: Retention exceeds the 5 % statutory cap for consumer construction contracts ({0} %).",
                        im.retentionPercent.toPlainString()));
            }
            if (itemType == Item.TYPE_PART_PAYMENT
                    && im.retentionPercent.compareTo(new BigDecimal("10.00")) > 0) {
                warnings.add(tr(yabs, "val.retention.vobCap",
                        "VOB/B § 17 Abs. 6: Single partial-payment retention exceeds 10 % ({0} %).",
                        im.retentionPercent.toPlainString()));
            }
        }

        // 6. BoQ / GAEB unpriced mandatory positions
        if (boq != null) {
            int unpriced = boq.countMandatoryUnpriced();
            if (unpriced > 0) {
                warnings.add(tr(yabs, "val.boq.unpriced",
                        "BoQ / GAEB: {0} mandatory position(s) still have unit price 0.00.",
                        unpriced));
            }
        }

        return warnings;
    }

    /**
     * Generates all {@code hw.*} template placeholders for OpenYaBS
     * {@code .fodt} / {@code .odt} / PDF documents. Never returns {@code null}
     * values so unresolved tokens never leak into exported PDFs.
     */
    public static Map<String, Object> templateFields(
            ContactMeta contactMeta,
            Site site,
            ItemMeta itemMeta,
            LaborSplitSummary split,
            BigDecimal netTotal,
            BigDecimal grossTotal,
            BoqDocument boq,
            List<Measurement> measurements,
            List<TimeSheet> timeSheets,
            Settings settings,
            ExtensionContext yabs) {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        ContactMeta cm = contactMeta == null ? new ContactMeta() : contactMeta;
        ItemMeta im = itemMeta == null ? new ItemMeta() : itemMeta;
        LaborSplitSummary sp = split == null ? new LaborSplitSummary() : split;
        Settings cfg = settings == null ? new Settings() : settings;

        m.put("hw.chamberName", safe(cfg.chamberName));
        m.put("hw.chamberNumber", safe(cfg.chamberNumber));

        String siteName = site != null ? safe(site.siteName) : "";
        String siteProjectNo = site != null ? safe(site.projectNo) : "";
        String siteAddress = !safe(im.siteSnapshot).isEmpty()
                ? safe(im.siteSnapshot)
                : (site != null ? site.formattedAddress() : "");
        String contractType = site != null ? safe(site.contractType) : safe(cfg.defaultContractType);

        m.put("hw.siteName", siteName);
        m.put("hw.siteProjectNo", siteProjectNo);
        m.put("hw.siteAddress", siteAddress);
        m.put("hw.contractType", HandwerkStore.CONTRACT_VOB_B.equalsIgnoreCase(contractType) ? "VOB/B" : "BGB");

        // § 35a EStG split
        m.put("hw.laborNet", fmt(sp.laborNet));
        m.put("hw.laborVat", fmt(sp.laborVat));
        m.put("hw.laborGross", fmt(sp.laborGross));
        m.put("hw.materialNet", fmt(sp.materialNet));
        m.put("hw.materialVat", fmt(sp.materialVat));
        m.put("hw.materialGross", fmt(sp.materialGross));
        m.put("hw.taxCredit35a", fmt(sp.estimatedCredit35a));
        m.put("hw.notice35a", im.laborTaxCreditEnabled
                ? tr(yabs, "tpl.notice35a",
                        "Contained labor, travel and machine costs according to § 35a EStG: {0} EUR net + {1} EUR VAT = {2} EUR gross (material: {3} EUR gross). Bank transfer required.",
                        fmt(sp.laborNet), fmt(sp.laborVat), fmt(sp.laborGross), fmt(sp.materialGross))
                : "");

        // § 14b UStG 2-year notice
        m.put("hw.notice14b", im.privateRetentionNotice
                ? tr(yabs, "tpl.notice14b",
                        "Note pursuant to § 14b Abs. 1 S. 5 UStG: As a private recipient of services related to real estate, you are obliged to retain this invoice for two years.")
                : "");

        // § 13b UStG Reverse Charge
        m.put("hw.notice13b", im.reverseChargeEnabled
                ? tr(yabs, "tpl.notice13b",
                        "Reverse Charge pursuant to § 13b Abs. 2 Nr. 4 UStG (Steuerschuldnerschaft des Leistungsempfängers).")
                : "");
        m.put("hw.ust1tgNo", safe(cm.reverseChargeCertNo));
        m.put("hw.ust1tgValidUntil", safe(cm.reverseChargeCertUntil));

        // § 48b EStG Freistellungsbescheinigung
        m.put("hw.freistellungTaxNo", safe(cfg.ownFreistellungTaxNo));
        m.put("hw.freistellungSecNo", safe(cfg.ownFreistellungSecNo));
        m.put("hw.freistellungValidUntil", safe(cfg.ownFreistellungValidUntil));
        boolean hasOwnFreistellung = !safe(cfg.ownFreistellungTaxNo).isEmpty()
                && !safe(cfg.ownFreistellungValidUntil).isEmpty();
        m.put("hw.freistellungNotice", hasOwnFreistellung
                ? tr(yabs, "tpl.freistellung",
                        "Tax exemption certificate (§ 48b EStG): Tax No. {0}, Security No. {1}, valid until {2}.",
                        safe(cfg.ownFreistellungTaxNo), safe(cfg.ownFreistellungSecNo), safe(cfg.ownFreistellungValidUntil))
                : "");

        // Retention (Sicherheitseinbehalt)
        RetentionResult ret = computeRetention(netTotal, grossTotal, im);
        m.put("hw.retentionPercent", im.retentionPercent != null && im.retentionPercent.signum() > 0
                ? fmt(im.retentionPercent) + " %" : "");
        m.put("hw.retentionBasis", ret.effectiveBasis);
        m.put("hw.retentionAmount", ret.retentionAmount.signum() > 0 ? fmt(ret.retentionAmount) : "0.00");
        m.put("hw.payableAfterRetention", fmt(ret.payableAfterRetention));
        m.put("hw.retentionReleaseDate", safe(im.retentionReleaseDate));
        m.put("hw.retentionBondRef", safe(im.retentionBondRef));

        // Acceptance & warranty
        m.put("hw.acceptanceDate", site != null ? safe(site.acceptanceDate) : "");
        m.put("hw.warrantyYears", site != null && site.warrantyYears > 0 ? String.valueOf(site.warrantyYears) : "");
        m.put("hw.warrantyUntil", site != null ? safe(site.warrantyUntil) : "");
        m.put("hw.maintenanceDue", site != null ? safe(site.maintenanceDue) : "");

        // BoQ / GAEB
        m.put("hw.boqFormat", boq != null ? safe(boq.formatCode) : "");
        m.put("hw.boqProject", boq != null ? safe(boq.projectName) : "");
        m.put("hw.boqLabel", boq != null ? safe(boq.boqLabel) : "");
        m.put("hw.boqSummary", boq != null
                ? boq.positions.size() + " Pos. (" + fmt(boq.totalNet()) + " " + safe(boq.currency) + ")"
                : "");

        // Measurements & time sheets
        StringBuilder measSb = new StringBuilder();
        if (measurements != null) {
            for (Measurement ms : measurements) {
                if (measSb.length() > 0) {
                    measSb.append("\n");
                }
                if (!safe(ms.ordinalNo).isEmpty()) {
                    measSb.append("OZ ").append(ms.ordinalNo).append(" ");
                }
                measSb.append(safe(ms.description));
                if (!safe(ms.formula).isEmpty()) {
                    measSb.append(": ").append(ms.formula).append(" = ");
                } else {
                    measSb.append(": ");
                }
                measSb.append(ms.computedQty == null ? "0" : ms.computedQty.stripTrailingZeros().toPlainString());
                if (!safe(ms.unit).isEmpty()) {
                    measSb.append(" ").append(ms.unit);
                }
            }
        }
        m.put("hw.measurementsSummary", measSb.toString());

        StringBuilder tsSb = new StringBuilder();
        BigDecimal totalHours = BigDecimal.ZERO;
        if (timeSheets != null) {
            for (TimeSheet ts : timeSheets) {
                if (ts.hours != null) {
                    totalHours = totalHours.add(ts.hours);
                }
                if (tsSb.length() > 0) {
                    tsSb.append("\n");
                }
                tsSb.append(safe(ts.workDate)).append(" | ")
                        .append(safe(ts.workerName)).append(" (").append(safe(ts.workerRole)).append("): ")
                        .append(fmt(ts.hours)).append(" h");
                if (ts.travelKm != null && ts.travelKm.signum() > 0) {
                    tsSb.append(", ").append(fmt(ts.travelKm)).append(" km");
                }
                if (!safe(ts.description).isEmpty()) {
                    tsSb.append(" - ").append(safe(ts.description));
                }
            }
        }
        m.put("hw.timeSheetsSummary", tsSb.toString());
        m.put("hw.totalHours", fmt(totalHours));

        return m;
    }

    /**
     * Helper to build a {@link LineInput} from an OpenYaBS
     * {@link mpv5.db.objects.SubItem}.
     */
    public static LineInput fromSubItem(mpv5.db.objects.SubItem si) {
        LineInput li = new LineInput();
        if (si == null) {
            return li;
        }
        li.subItemId = si.__getIDS();
        li.description = safe(si.__getDescription());
        li.unit = safe(si.__getMeasure());
        li.textLine = si.getInttype() == mpv5.db.objects.SubItem.TYPE_TEXT;
        li.taxPercent = si.__getTaxpercentvalue() == null ? BigDecimal.ZERO : si.__getTaxpercentvalue();
        BigDecimal net = si.__getTotalnetvalue();
        if (net == null) {
            BigDecimal qty = si.effectiveQuantity();
            BigDecimal up = si.__getExternalvalue() == null ? BigDecimal.ZERO : si.__getExternalvalue();
            net = qty.multiply(up);
        }
        li.netTotal = net;
        try {
            Product p = si.getProduct();
            if (p != null) {
                li.sku = safe(p.__getCnumber());
                li.serviceProduct = p.__getInttype() == Product.TYPE_SERVICE;
            }
        } catch (Throwable ignored) {
        }
        return li;
    }

    private static BigDecimal clampPercent(BigDecimal p) {
        if (p == null) {
            return BigDecimal.ZERO;
        }
        if (p.signum() < 0) {
            return BigDecimal.ZERO;
        }
        if (p.compareTo(BD100) > 0) {
            return BD100;
        }
        return p;
    }

    private static String fmt(BigDecimal b) {
        return (b == null ? BigDecimal.ZERO : b).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String tr(ExtensionContext yabs, String key, String fallback, Object... args) {
        if (yabs != null) {
            return yabs.trOrDefault(key, fallback, args);
        }
        if (args != null && args.length > 0) {
            try {
                return java.text.MessageFormat.format(fallback, args);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return fallback;
    }
}
