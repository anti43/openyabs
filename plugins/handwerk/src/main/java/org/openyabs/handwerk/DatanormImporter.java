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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Product;
import mpv5.logging.Log;

/**
 * Pure Java parser and importer for German wholesaler **DATANORM 4.0 &amp; 5.0**
 * files ({@code DATANORM.001}–{@code .999}, {@code DATPREIS.001},
 * {@code DATANORM.RAB}, {@code DATANORM.WRG}).
 * <p>
 * Supports record types {@code V} (header), {@code A} (article core), {@code B}
 * (matchcode / EAN), {@code T} (long text), {@code P} (price updates),
 * {@code R} (discount groups), and {@code S} (product groups), including
 * cent-integer price decoding and price-unit divisors ({@code 0}=/1,
 * {@code 1}=/10, {@code 2}=/100, {@code 3}=/1000).
 */
public final class DatanormImporter {

    public static final class Article {

        public String sku = "";
        public String name = "";
        public String shortText2 = "";
        public String longText = "";
        public String unit = "Stk";
        public String ean = "";
        public String matchcode = "";
        public String discountGroup = "";
        public String productGroup = "";
        /**
         * 1 = list price (Bruttolistenpreis), 2 = net purchase price
         */
        public int priceFlag = 1;
        /**
         * 0 = per 1, 1 = per 10, 2 = per 100, 3 = per 1000
         */
        public int priceUnit = 0;
        public BigDecimal rawUnitPrice = BigDecimal.ZERO;
        public BigDecimal netPurchasePrice = BigDecimal.ZERO;
        public boolean deleted;
    }

    public static final class ParseResult {

        public String version = "4";
        public String supplierInfo = "";
        public String currency = "EUR";
        public final Map<String, Article> articles = new LinkedHashMap<String, Article>();
        public final Map<String, BigDecimal> discountGroups = new HashMap<String, BigDecimal>();
        public final Map<String, String> productGroups = new HashMap<String, String>();
        public int priceUpdates;
    }

    public static final class ImportSummary {

        public int created;
        public int updated;
        public int skipped;
    }

    private DatanormImporter() {
    }

    /**
     * Decodes raw DATANORM bytes using UTF-8 if valid, otherwise Windows-1252 /
     * CP850 fallback typical of German wholesaler exports.
     */
    public static String decodeBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        String utf8 = new String(bytes, StandardCharsets.UTF_8);
        if (!utf8.contains("\uFFFD")) {
            return utf8;
        }
        try {
            return new String(bytes, Charset.forName("windows-1252"));
        } catch (Exception e) {
            return new String(bytes, StandardCharsets.ISO_8859_1);
        }
    }

    /**
     * Parses one or more DATANORM text payloads (e.g. {@code DATANORM.RAB},
     * {@code DATANORM.WRG}, {@code DATANORM.001}, {@code DATPREIS.001}) into a
     * merged {@link ParseResult}.
     */
    public static ParseResult parse(String... contents) {
        ParseResult res = new ParseResult();
        if (contents == null) {
            return res;
        }
        for (String content : contents) {
            if (content == null || content.isEmpty()) {
                continue;
            }
            parseSingle(content, res);
        }
        // Resolve effective net purchase prices using discount groups for list prices (priceFlag == 1)
        for (Article a : res.articles.values()) {
            BigDecimal base = a.rawUnitPrice == null ? BigDecimal.ZERO : a.rawUnitPrice;
            if (a.priceFlag == 1 && !a.discountGroup.isEmpty() && res.discountGroups.containsKey(a.discountGroup)) {
                BigDecimal disc = res.discountGroups.get(a.discountGroup);
                BigDecimal factor = BigDecimal.ONE.subtract(disc.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP));
                a.netPurchasePrice = base.multiply(factor).setScale(2, RoundingMode.HALF_UP);
            } else {
                a.netPurchasePrice = base.setScale(2, RoundingMode.HALF_UP);
            }
        }
        return res;
    }

    private static void parseSingle(String content, ParseResult res) {
        String cleaned = content.startsWith("\uFEFF") ? content.substring(1) : content;
        String[] lines = cleaned.replace("\r\n", "\n").split("\n");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] f = line.split(";", -1);
            if (f.length == 0) {
                continue;
            }
            String rec = f[0].trim().toUpperCase(Locale.ROOT);
            if ("V".equals(rec)) {
                parseHeader(f, rawLine, res);
            } else if ("A".equals(rec)) {
                parseArticleA(f, res);
            } else if ("B".equals(rec)) {
                parseArticleB(f, res);
            } else if ("T".equals(rec)) {
                parseTextT(f, res);
            } else if ("P".equals(rec)) {
                parsePriceP(f, res);
            } else if ("R".equals(rec)) {
                parseDiscountR(f, res);
            } else if ("S".equals(rec)) {
                parseGroupS(f, res);
            }
        }
    }

    private static void parseHeader(String[] f, String rawLine, ParseResult res) {
        if (f.length >= 2) {
            // DATANORM 5 semicolon-delimited header: V;050;... or V;04;...
            String ver = f[1].trim();
            if (ver.startsWith("05") || ver.startsWith("5")) {
                res.version = "5";
            } else if (ver.startsWith("04") || ver.startsWith("4")) {
                res.version = "4";
            }
            if (f.length >= 4 && !f[3].trim().isEmpty()) {
                res.supplierInfo = f[3].trim();
            }
            for (String col : f) {
                String c = col.trim().toUpperCase(Locale.ROOT);
                if ("EUR".equals(c) || "CHF".equals(c) || "USD".equals(c)) {
                    res.currency = c;
                }
            }
        } else if (rawLine.length() >= 120) {
            // DATANORM 4 fixed-width header
            res.version = "4";
            res.supplierInfo = rawLine.substring(10, Math.min(50, rawLine.length())).trim();
        }
    }

    private static void parseArticleA(String[] f, ParseResult res) {
        // Standard DATANORM 4 & 5 A-record:
        // [0]=A, [1]=processingFlag(N/A/L), [2]=sku, [3]=textFlag, [4]=shortText1, [5]=shortText2,
        // [6]=priceFlag(1/2), [7]=priceUnit(0/1/2/3), [8]=unit, [9]=priceCents, [10]=discountGroup, [11]=mainGroup
        if (f.length < 10) {
            return;
        }
        String action = f[1].trim().toUpperCase(Locale.ROOT);
        String sku = f[2].trim();
        if (sku.isEmpty()) {
            return;
        }
        Article a = res.articles.computeIfAbsent(sku, k -> {
            Article x = new Article();
            x.sku = k;
            return x;
        });
        if ("L".equals(action)) {
            a.deleted = true;
            return;
        }
        a.deleted = false;
        String t1 = f[4].trim();
        String t2 = f[5].trim();
        if (!t1.isEmpty()) {
            a.name = t1;
        }
        if (!t2.isEmpty()) {
            a.shortText2 = t2;
        }
        a.priceFlag = parseInt(f[6], 1);
        a.priceUnit = parseInt(f[7], 0);
        String unit = f[8].trim();
        if (!unit.isEmpty()) {
            a.unit = unit;
        }
        a.rawUnitPrice = decodeDatanormPrice(f[9], a.priceUnit);
        if (f.length > 10 && !f[10].trim().isEmpty()) {
            a.discountGroup = f[10].trim();
        }
        if (f.length > 11 && !f[11].trim().isEmpty()) {
            a.productGroup = f[11].trim();
        }
    }

    private static void parseArticleB(String[] f, ParseResult res) {
        // [0]=B, [1]=flag, [2]=sku, [3]=matchcode, ..., [9] or [10]=EAN
        if (f.length < 4) {
            return;
        }
        String sku = f[2].trim();
        if (sku.isEmpty()) {
            return;
        }
        Article a = res.articles.get(sku);
        if (a == null) {
            return;
        }
        if (!f[3].trim().isEmpty()) {
            a.matchcode = f[3].trim();
        }
        for (int i = 4; i < f.length; i++) {
            String candidate = f[i].trim();
            if (candidate.matches("\\d{8}|\\d{12}|\\d{13}|\\d{14}")) {
                a.ean = candidate;
                break;
            }
        }
    }

    private static void parseTextT(String[] f, ParseResult res) {
        // [0]=T, [1]=flag, [2]=sku, [3]=lineNo, [4]=text1, [5]=text2...
        if (f.length < 5) {
            return;
        }
        String sku = f[2].trim();
        Article a = res.articles.get(sku);
        if (a == null) {
            return;
        }
        StringBuilder sb = new StringBuilder(a.longText);
        for (int i = 4; i < f.length; i++) {
            String part = f[i].trim();
            if (!part.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(part);
            }
        }
        a.longText = sb.toString();
    }

    private static void parsePriceP(String[] f, ParseResult res) {
        // DATPREIS: P;flag;sku;priceFlag;priceCents;... OR P;sku;priceFlag;priceCents
        if (f.length < 4) {
            return;
        }
        int offset = (f[1].trim().length() == 1 && Character.isLetter(f[1].trim().charAt(0))) ? 1 : 0;
        if (f.length <= offset + 3) {
            return;
        }
        String sku = f[1 + offset].trim();
        if (sku.isEmpty()) {
            return;
        }
        Article a = res.articles.get(sku);
        if (a == null) {
            return;
        }
        a.priceFlag = parseInt(f[2 + offset], a.priceFlag);
        a.rawUnitPrice = decodeDatanormPrice(f[3 + offset], a.priceUnit);
        res.priceUpdates++;
    }

    private static void parseDiscountR(String[] f, ParseResult res) {
        // R;discountGroup;discountPercent OR R;flag;discountGroup;discountPercent
        if (f.length < 3) {
            return;
        }
        int offset = (f.length >= 4 && f[1].trim().length() == 1 && Character.isLetter(f[1].trim().charAt(0))) ? 1 : 0;
        String code = f[1 + offset].trim();
        if (code.isEmpty()) {
            return;
        }
        String val = f[2 + offset].trim();
        BigDecimal pct;
        if (val.contains(".") || val.contains(",")) {
            pct = parseDecimal(val, BigDecimal.ZERO);
        } else {
            String digits = val.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) {
                return;
            }
            BigDecimal raw = new BigDecimal(digits);
            pct = raw.compareTo(new BigDecimal("100")) > 0 ? raw.movePointLeft(2) : raw;
        }
        res.discountGroups.put(code, pct);
    }

    private static void parseGroupS(String[] f, ParseResult res) {
        if (f.length < 3) {
            return;
        }
        String code = f[1].trim();
        String label = f.length >= 4 && !f[3].trim().isEmpty() ? f[3].trim() : f[2].trim();
        if (!code.isEmpty() && !label.isEmpty()) {
            res.productGroups.put(code, label);
        }
    }

    /**
     * Decodes a DATANORM price field (cent integer without decimal separator)
     * divided by the DATANORM price unit ({@code 0}=1, {@code 1}=10,
     * {@code 2}=100, {@code 3}=1000).
     */
    public static BigDecimal decodeDatanormPrice(String raw, int priceUnit) {
        if (raw == null || raw.trim().isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        String s = raw.trim();
        BigDecimal base;
        if (s.contains(".") || s.contains(",")) {
            base = parseDecimal(s, BigDecimal.ZERO);
        } else {
            String digits = s.replaceAll("[^0-9-]", "");
            if (digits.isEmpty() || "-".equals(digits)) {
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
            base = new BigDecimal(digits).movePointLeft(2);
        }
        BigDecimal divisor;
        switch (priceUnit) {
            case 1:
                divisor = new BigDecimal("10");
                break;
            case 2:
                divisor = new BigDecimal("100");
                break;
            case 3:
                divisor = new BigDecimal("1000");
                break;
            default:
                divisor = BigDecimal.ONE;
                break;
        }
        return base.divide(divisor, 4, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Upserts parsed DATANORM articles into OpenYaBS's {@link Product} catalog
     * as {@link Product#TYPE_PRODUCT} (material), applying the configured sales
     * markup percentage over net purchase price.
     */
    public static ImportSummary importToProducts(ParseResult parsed, BigDecimal markupPercent, int defaultTaxId) {
        ImportSummary summary = new ImportSummary();
        if (parsed == null || parsed.articles.isEmpty()) {
            return summary;
        }
        BigDecimal markup = markupPercent == null ? BigDecimal.ZERO : markupPercent;
        BigDecimal factor = BigDecimal.ONE.add(markup.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP));

        Map<String, Product> existingBySku = new HashMap<String, Product>();
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getProduct())) {
                Product p = (Product) o;
                if (p.__getCnumber() != null && !p.__getCnumber().trim().isEmpty()) {
                    existingBySku.put(p.__getCnumber().trim().toUpperCase(Locale.ROOT), p);
                }
            }
        } catch (Exception e) {
            Log.Debug(DatanormImporter.class, e);
        }

        List<Article> list = new ArrayList<Article>(parsed.articles.values());
        for (Article a : list) {
            if (a.deleted || a.sku.isEmpty()) {
                summary.skipped++;
                continue;
            }
            BigDecimal buyPrice = a.netPurchasePrice == null ? BigDecimal.ZERO : a.netPurchasePrice;
            BigDecimal sellPrice = buyPrice.multiply(factor).setScale(2, RoundingMode.HALF_UP);
            String desc = buildDescription(a);

            Product existing = existingBySku.get(a.sku.toUpperCase(Locale.ROOT));
            if (existing != null) {
                existing.setInternalnetvalue(buyPrice);
                existing.setExternalnetvalue(sellPrice);
                if (!a.unit.isEmpty()) {
                    existing.setMeasure(a.unit);
                }
                if (!a.ean.isEmpty()) {
                    existing.setEan(a.ean);
                }
                if (!desc.isEmpty()) {
                    existing.setDescription(desc);
                }
                if (existing.save(true)) {
                    summary.updated++;
                } else {
                    summary.skipped++;
                }
            } else {
                Product p = new Product();
                p.setInttype(Product.TYPE_PRODUCT);
                p.setCnumber(a.sku);
                p.setCname(a.name.isEmpty() ? a.sku : a.name);
                p.setDescription(desc);
                p.setInternalnetvalue(buyPrice);
                p.setExternalnetvalue(sellPrice);
                p.setMeasure(a.unit.isEmpty() ? "Stk" : a.unit);
                if (!a.ean.isEmpty()) {
                    p.setEan(a.ean);
                }
                if (defaultTaxId > 0) {
                    p.setTaxids(defaultTaxId);
                }
                if (p.save(true)) {
                    summary.created++;
                    existingBySku.put(a.sku.toUpperCase(Locale.ROOT), p);
                } else {
                    summary.skipped++;
                }
            }
        }
        return summary;
    }

    private static String buildDescription(Article a) {
        StringBuilder sb = new StringBuilder();
        if (!a.shortText2.isEmpty()) {
            sb.append(a.shortText2);
        }
        if (!a.longText.isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(a.longText);
        }
        return sb.toString();
    }

    private static int parseInt(String raw, int fallback) {
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static BigDecimal parseDecimal(String raw, BigDecimal fallback) {
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        try {
            return new BigDecimal(raw.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
