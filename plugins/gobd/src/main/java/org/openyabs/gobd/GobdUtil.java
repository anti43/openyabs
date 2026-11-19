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
package org.openyabs.gobd;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Helpers of the GoBD plugin without database or user interface: hashes,
 * formats, escaping (tested on their own)
 */
public final class GobdUtil {

    /**
     * The start of the SHA-256 hash chains (64 zeros)
     */
    public static final String GENESIS_HASH = repeat('0', 64);

    private GobdUtil() {
    }

    static String repeat(char c, int n) {
        StringBuilder b = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            b.append(c);
        }
        return b.toString();
    }

    /**
     * @param bytes
     * @return SHA-256 of the bytes, lower case hex
     */
    public static String sha256(byte[] bytes) {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            d.update(bytes);
            return hex(d.digest());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * The hash chain: SHA256(file bytes || previous hash)
     *
     * @param bytes
     * @param prevHash
     * @return lower case hex
     */
    public static String chained(byte[] bytes, String prevHash) {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            d.update(bytes);
            d.update(prevHash.getBytes(StandardCharsets.UTF_8));
            return hex(d.digest());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param file
     * @return SHA-256 of the file (read as a stream), lower case hex
     */
    public static String sha256(Path file) throws IOException {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buf = new byte[64 * 1024];
                int n;
                while ((n = in.read(buf)) > 0) {
                    d.update(buf, 0, n);
                }
            }
            return hex(d.digest());
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * The hash chain of records (version 2): SHA256(record || "\n" || previous
     * hash). The record holds the booking data and the SHA-256 of the file, so
     * a change of the database is found as well as a change of the file.
     *
     * @param record see {@link #record}
     * @param prevHash
     * @return lower case hex
     */
    public static String recordHash(String record, String prevHash) {
        return sha256((record + "\n" + prevHash).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @param keyValues key, value, key, value ...
     * @return the canonical record: one "key=value" per line, line breaks and
     * backslashes in the values escaped
     */
    public static String record(Object... keyValues) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            if (b.length() > 0) {
                b.append('\n');
            }
            b.append(keyValues[i]).append('=').append(recordValue(keyValues[i + 1]));
        }
        return b.toString();
    }

    /**
     * @param value
     * @return the value as it is in a record (see {@link #record})
     */
    public static String recordValue(Object value) {
        return str(value).replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n");
    }

    /**
     * @param record see {@link #record}
     * @param key
     * @return the (escaped) value of the key, null if not there
     */
    public static String recordField(String record, String key) {
        if (record == null) {
            return null;
        }
        for (String line : record.split("\n", -1)) {
            if (line.startsWith(key + "=")) {
                return line.substring(key.length() + 1);
            }
        }
        return null;
    }

    private static String hex(byte[] digest) {
        return HexFormat.of().formatHex(digest);
    }

    /**
     * @param val
     * @return for SQL string literals: ' doubled, null as empty
     */
    public static String esc(Object val) {
        return val == null ? "" : val.toString().replace("'", "''");
    }

    /**
     * @param s
     * @param maxLen
     * @return s, shortened to maxLen with "..." at the end
     */
    public static String trunc(String s, int maxLen) {
        if (s == null) {
            return null;
        }
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
    }

    /**
     * @param raw
     * @return text for XML
     */
    public static String escapeXml(Object raw) {
        if (raw == null) {
            return "";
        }
        return raw.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    /**
     * @param raw
     * @return text of a quoted CSV cell: no line breaks, " doubled
     */
    public static String csvCell(Object raw) {
        if (raw == null) {
            return "";
        }
        return raw.toString().replaceAll("[\\r\\n]+", " ").replace("\"", "\"\"");
    }

    /**
     * @param num
     * @return with two decimals and a dot, "0.00" for null or not a number
     */
    public static String fmtDec(Object num) {
        BigDecimal b = decimal(num);
        return b == null ? "0.00" : b.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * For comparing amounts (the guard, the hash records): 6 decimals, so a
     * change below a cent is found as well; the database columns are DOUBLE,
     * so more decimals would differ between memory and database.
     *
     * @param num
     * @return with 6 decimals and a dot, "0.000000" for null or not a number
     */
    public static String fmtExact(Object num) {
        BigDecimal b = decimal(num);
        return (b == null ? BigDecimal.ZERO : b).setScale(6, RoundingMode.HALF_UP).toPlainString();
    }

    private static BigDecimal decimal(Object num) {
        if (num == null) {
            return null;
        }
        if (num instanceof BigDecimal) {
            return (BigDecimal) num;
        }
        try {
            //Double.toString is the shortest exact text of a double
            return new BigDecimal(num.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param text
     * @return text for LIKE ... ESCAPE '!': ! % _ escaped with !
     */
    public static String likeEscape(String text) {
        return text == null ? "" : text.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    /**
     * @param d a date or a database value
     * @return yyyy-MM-dd, "" for null
     */
    public static String fmtDate(Object d) {
        if (d == null) {
            return "";
        }
        if (d instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd").format((Date) d);
        }
        String s = d.toString().trim();
        return s.length() >= 10 ? s.substring(0, 10) : s;
    }

    /**
     * @param d a time stamp or a database value
     * @return yyyy-MM-dd HH:mm:ss, "" for null
     */
    public static String fmtTimestamp(Object d) {
        if (d == null) {
            return "";
        }
        if (d instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) d);
        }
        if (d instanceof java.time.LocalDateTime) {
            return ((java.time.LocalDateTime) d).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        String s = d.toString().trim().replace('T', ' ');
        return s.length() >= 19 ? s.substring(0, 19) : s;
    }

    /**
     * @param o
     * @return the text, "" for null
     */
    public static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    /**
     * @param s
     * @return true if null or only blanks
     */
    public static boolean blank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * @param o a database value
     * @return true for 1 / true
     */
    public static boolean isTrue(Object o) {
        return o != null && (o.toString().equals("1") || o.toString().equalsIgnoreCase("true"));
    }

    /**
     * @param o a database value
     * @param fallback
     * @return the number, fallback if none
     */
    public static int toInt(Object o, int fallback) {
        try {
            return o == null ? fallback : Integer.parseInt(o.toString().trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * @param o a database value
     * @param fallback
     * @return the number, fallback if none
     */
    public static long toLong(Object o, long fallback) {
        try {
            return o == null ? fallback : Long.parseLong(o.toString().trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * @param s
     * @return true if s is a whole number
     */
    public static boolean isInteger(String s) {
        if (blank(s)) {
            return false;
        }
        try {
            Integer.parseInt(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * @param number
     * @return true for no number yet ("", ENTWURF..., DRAFT...)
     */
    public static boolean isDraftNumber(String number) {
        String n = number == null ? "" : number.trim().toUpperCase(Locale.ROOT);
        return n.isEmpty() || n.startsWith("ENTWURF") || n.startsWith("DRAFT");
    }

    /**
     * @param name
     * @return the name for a file: only letters, digits, . _ -
     */
    public static String safeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * @param dir
     * @param baseName
     * @param ext
     * @return dir/baseName.ext, with a time stamp if that exists already
     */
    public static Path uniquePath(Path dir, String baseName, String ext) {
        Path candidate = dir.resolve(baseName + "." + ext);
        if (!Files.exists(candidate)) {
            return candidate;
        }
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        return dir.resolve(baseName + "_" + ts + "." + ext);
    }

    /**
     * Makes an archived file read only (on Windows setReadOnly is enough)
     *
     * @param file
     */
    public static void makeReadOnly(Path file) {
        try {
            file.toFile().setReadOnly();
            Files.setPosixFilePermissions(file, EnumSet.of(PosixFilePermission.OWNER_READ,
                    PosixFilePermission.GROUP_READ, PosixFilePermission.OTHERS_READ));
        } catch (Exception ignored) {
            //no POSIX file system
        }
    }

    /**
     * @param name a resource next to this class
     * @return its text (UTF-8)
     */
    public static String resource(String name) {
        try (InputStream in = GobdUtil.class.getResourceAsStream(name)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource " + name);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param dateText yyyy-MM-dd ... or a date
     * @param year
     * @return true if the date is in the year
     */
    public static boolean inYear(Object dateText, String year) {
        return dateText != null && fmtDate(dateText).startsWith(year);
    }

    /**
     * @param d a date
     * @return its year, the current year for null
     */
    public static int yearOf(Date d) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        if (d != null) {
            c.setTime(d);
        }
        return c.get(java.util.Calendar.YEAR);
    }
}
