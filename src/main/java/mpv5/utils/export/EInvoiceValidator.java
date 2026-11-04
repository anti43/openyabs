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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private final File file;
    private Result result;

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
         * @return the violated rules (human readable)
         */
        public List<String> getErrors() {
            return errors;
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
