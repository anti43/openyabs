/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.utils.jobs.Waitable;
import org.mustangproject.validator.ZUGFeRDValidator;

/**
 * Checks electronic invoices (XRechnung XML, ZUGFeRD / Factur-X PDF) with the
 * official rules: XML schema, EN 16931 and XRechnung schematron and - for PDF
 * files - PDF/A-3 (veraPDF). Uses the Mustang validator.
 * <p>
 * Can be used directly ({@link #validate(File)}) or as {@link Waitable} in a
 * background job.
 */
public class EInvoiceValidator implements Waitable {

    private static final Pattern SUMMARY = Pattern.compile("<summary status=\"(\\w+)\"");
    private static final Pattern MESSAGE = Pattern.compile("<(fatal|error|warning)\\b[^>]*>(.*?)</\\1>", Pattern.DOTALL);
    private static final Pattern PDF_PART = Pattern.compile("<pdf>(.*?)</pdf>", Pattern.DOTALL);
    private static final Pattern PDF_FAILED = Pattern.compile("status=failed, message=(.*?), location=", Pattern.DOTALL);
    private static final Pattern RULE = Pattern.compile("\\[(?:ID )?([A-Z][A-Z0-9]*(?:-[A-Z0-9]+)*(?:-[a-z])?)\\]");
    /**
     * Rule id -> what the user has to do in YaBS
     */
    private static final Map<String, Messages> HINTS = new HashMap<String, Messages>();
    private final File file;
    private Result result;

    static {
        hint(Messages.ZUGFERD_HINT_VATID_PREFIX, "BR-CO-09");
        hint(Messages.ZUGFERD_HINT_SELLER_VATID, "BR-CO-26", "BR-DE-16", "BR-S-02", "BR-Z-02", "BR-E-02",
                "BR-AE-02", "BR-IC-02", "BR-G-02");
        hint(Messages.ZUGFERD_HINT_BUYER_TAXNUMBER, "FX-SCH-A-000570", "FX-SCH-A-000426");
        hint(Messages.ZUGFERD_HINT_SELLER_CONTACT, "BR-DE-2", "BR-DE-5", "BR-DE-6", "BR-DE-7", "BR-DE-27", "BR-DE-28");
        hint(Messages.ZUGFERD_HINT_SELLER_ADDRESS, "BR-DE-3", "BR-DE-4", "BR-08", "BR-09");
        hint(Messages.ZUGFERD_HINT_BUYER_ADDRESS, "BR-DE-8", "BR-DE-9", "BR-10", "BR-11");
        hint(Messages.ZUGFERD_HINT_BUYER_REFERENCE, "BR-DE-15");
        hint(Messages.ZUGFERD_HINT_IBAN, "BR-DE-1", "BR-DE-23-a", "BR-DE-19", "BR-50", "BR-61");
        hint(Messages.ZUGFERD_HINT_PAYTERM, "BR-CO-25");
        hint(Messages.ZUGFERD_HINT_BUYER_EMAIL, "PEPPOL-EN16931-R010");
        hint(Messages.ZUGFERD_HINT_EMPTY, "PEPPOL-EN16931-R008");
    }

    private static void hint(Messages hint, String... rules) {
        for (String rule : rules) {
            HINTS.put(rule, hint);
        }
    }

    /**
     * @param error a message of the validator
     * @return what to do in YaBS to fix the error, null if unknown
     */
    public static String hint(String error) {
        if (error == null) {
            return null;
        }
        if (error.startsWith("PDF/A:")) {
            return error.toLowerCase().contains("font") || error.contains("glyph")
                    ? Messages.ZUGFERD_HINT_FONTS.toString() : Messages.ZUGFERD_HINT_PDFA.toString();
        }
        Matcher m = RULE.matcher(error);
        while (m.find()) {
            Messages h = HINTS.get(m.group(1));
            if (h != null) {
                return h.toString();
            }
        }
        return null;
    }

    /**
     * @param error a message of the validator
     * @return the rule id (e.g. BR-CO-09) or null
     */
    static String rule(String error) {
        Matcher m = RULE.matcher(error == null ? "" : error);
        return m.find() ? m.group(1) : null;
    }

    /**
     * @param file the file to check (.xml or .pdf)
     */
    public EInvoiceValidator(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    /**
     * @return the result after {@link #waitFor()}
     */
    public Result getResult() {
        return result;
    }

    @Override
    public Exception waitFor() {
        try {
            mpv5.YabsViewProxy.instance().setWaiting(true);
            result = validate(file);
            return null;
        } catch (Exception ex) {
            Log.Debug(ex);
            return ex;
        } finally {
            mpv5.YabsViewProxy.instance().setWaiting(false);
        }
    }

    /**
     * Validate an electronic invoice
     *
     * @param file .xml or .pdf
     * @return the result
     * @throws FileNotFoundException
     */
    public static Result validate(File file) throws FileNotFoundException {
        if (file == null || !file.isFile()) {
            throw new FileNotFoundException(String.valueOf(file));
        }
        ZUGFeRDValidator validator = new ZUGFeRDValidator();
        String report = validator.validate(file.getAbsolutePath());
        return parse(report);
    }

    /**
     * Parse the report of the Mustang validator
     *
     * @param report
     * @return
     */
    static Result parse(String report) {
        Set<String> errors = new LinkedHashSet<String>();
        Set<String> warnings = new LinkedHashSet<String>();
        boolean valid = report != null && report.contains("<summary");

        if (report != null) {
            Matcher s = SUMMARY.matcher(report);
            while (s.find()) {
                if (!"valid".equals(s.group(1))) {
                    valid = false;
                }
            }
            Matcher pdf = PDF_PART.matcher(report);
            if (pdf.find()) {
                Matcher f = PDF_FAILED.matcher(pdf.group(1));
                while (f.find()) {
                    errors.add("PDF/A: " + clean(f.group(1)));
                }
            }
            Matcher m = MESSAGE.matcher(report);
            while (m.find()) {
                String text = clean(m.group(2));
                if (text.isEmpty()) {
                    continue;
                }
                if ("warning".equals(m.group(1))) {
                    warnings.add(text);
                } else {
                    errors.add(text);
                }
            }
        }
        if (!errors.isEmpty()) {
            valid = false;
        }
        return new Result(valid, new ArrayList<String>(errors), new ArrayList<String>(warnings), report);
    }

    private static String clean(String text) {
        return text.replaceAll("<[^>]*>", "")
                .replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'")
                .replace("&amp;", "&")
                //the source stylesheet is of no interest for the user
                .replaceAll("\\s*from /xslt/\\S*\\)?\\s*$", "")
                .replaceAll("\\s+", " ").trim();
    }

    /**
     * Validation result
     */
    public static class Result {

        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        private final String report;

        public Result(boolean valid, List<String> errors, List<String> warnings, String report) {
            this.valid = valid;
            this.errors = Collections.unmodifiableList(errors);
            this.warnings = Collections.unmodifiableList(warnings);
            this.report = report;
        }

        /**
         * @return true if the file conforms to all rules
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * @return the violated rules (as reported by the validator)
         */
        public List<String> getErrors() {
            return errors;
        }

        /**
         * @return the errors as instructions for the user: what to change
         * where in YaBS, followed by the rule ids. Errors without a known fix
         * are returned as reported by the validator.
         */
        public List<String> getProblems() {
            Map<String, Set<String>> byHint = new LinkedHashMap<String, Set<String>>();
            List<String> unknown = new ArrayList<String>();
            for (String error : errors) {
                String h = hint(error);
                if (h == null) {
                    unknown.add(error);
                    continue;
                }
                Set<String> rules = byHint.get(h);
                if (rules == null) {
                    rules = new LinkedHashSet<String>();
                    byHint.put(h, rules);
                }
                String rule = error.startsWith("PDF/A:") ? "PDF/A-3" : rule(error);
                if (rule != null) {
                    rules.add(rule);
                }
            }
            List<String> list = new ArrayList<String>();
            for (Map.Entry<String, Set<String>> e : byHint.entrySet()) {
                list.add(e.getKey() + (e.getValue().isEmpty() ? "" : " " + e.getValue()));
            }
            list.addAll(unknown);
            return list;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        /**
         * @return the full XML report of the validator
         */
        public String getReport() {
            return report;
        }

        @Override
        public String toString() {
            return (valid ? "valid" : "invalid") + " " + errors;
        }
    }
}
