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
package org.openyabs.gmbh;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The information a GmbH (also UG (haftungsbeschränkt), GmbH &amp; Co. KG ...)
 * has to give on all business letters and invoices (§ 35a GmbHG, § 125a HGB):
 * legal form, registered office, register court, register number, all
 * managing directors (and the chair of a supervisory board, if any).
 * <p>
 * Stored in the company info ({@code companyinfo.<key>}), so every template
 * can use the single values as well, also without the plugin.
 */
public final class GmbhData {

    public static final String K_LEGALFORM = "legalform";
    public static final String K_SEAT = "seat";
    public static final String K_COURT = "registercourt";
    public static final String K_NUMBER = "registernumber";
    public static final String K_DIRECTORS = "managingdirectors";
    public static final String K_BOARD = "supervisorychair";
    public static final String K_CAPITAL = "sharecapital";

    /**
     * the keys in the order of the settings page
     */
    public static final String[] KEYS = {K_LEGALFORM, K_SEAT, K_COURT, K_NUMBER, K_DIRECTORS, K_BOARD, K_CAPITAL};

    public String name = "";
    public String legalForm = "GmbH";
    public String seat = "";
    public String registerCourt = "";
    public String registerNumber = "";
    public String managingDirectors = "";
    public String supervisoryChair = "";
    public String shareCapital = "";

    /**
     * @param company the company info (key without prefix to value)
     * @return the data
     */
    public static GmbhData of(Function<String, String> company) {
        GmbhData d = new GmbhData();
        d.name = nvl(company.apply("name")).trim();
        String lf = nvl(company.apply(K_LEGALFORM)).trim();
        d.legalForm = lf.isEmpty() ? guessLegalForm(d.name) : lf;
        d.seat = nvl(company.apply(K_SEAT)).trim();
        if (d.seat.isEmpty()) {
            d.seat = nvl(company.apply("city")).trim();
        }
        d.registerCourt = nvl(company.apply(K_COURT)).trim();
        d.registerNumber = nvl(company.apply(K_NUMBER)).trim();
        d.managingDirectors = nvl(company.apply(K_DIRECTORS)).trim();
        d.supervisoryChair = nvl(company.apply(K_BOARD)).trim();
        d.shareCapital = nvl(company.apply(K_CAPITAL)).trim();
        return d;
    }

    /**
     * @return the stored data of the company info of this database
     */
    public static GmbhData load() {
        return of(k -> mpv5.globals.CompanyInfo.getProperty(k));
    }

    /**
     * Saves the data into the company info
     */
    public void save() {
        mpv5.globals.CompanyInfo.setProperty(K_LEGALFORM, legalForm);
        mpv5.globals.CompanyInfo.setProperty(K_SEAT, seat);
        mpv5.globals.CompanyInfo.setProperty(K_COURT, registerCourt);
        mpv5.globals.CompanyInfo.setProperty(K_NUMBER, registerNumber);
        mpv5.globals.CompanyInfo.setProperty(K_DIRECTORS, managingDirectors);
        mpv5.globals.CompanyInfo.setProperty(K_BOARD, supervisoryChair);
        mpv5.globals.CompanyInfo.setProperty(K_CAPITAL, shareCapital);
        mpv5.globals.CompanyInfo.save();
    }

    /**
     * @param name the company name
     * @return the legal form in the name ("Muster UG (haftungsbeschränkt)"),
     * "GmbH" if none is found
     */
    public static String guessLegalForm(String name) {
        String n = nvl(name);
        String[] forms = {"GmbH & Co. KG", "UG (haftungsbeschränkt) & Co. KG", "UG (haftungsbeschränkt)", "gGmbH", "GmbH"};
        for (String f : forms) {
            if (n.contains(f)) {
                return f;
            }
        }
        return "GmbH";
    }

    /**
     * @return true for a GmbH &amp; Co. KG: the details of the
     * general partner (Komplementär-GmbH) are required as well (§ 125a HGB)
     */
    public boolean isCoKg() {
        return legalForm.contains("Co. KG");
    }

    /**
     * @return the missing required details (localized by the caller: the
     * keys of {@link #KEYS}); empty if complete
     */
    public List<String> missing() {
        List<String> m = new ArrayList<String>();
        if (legalForm.trim().isEmpty()) {
            m.add(K_LEGALFORM);
        }
        if (seat.trim().isEmpty()) {
            m.add(K_SEAT);
        }
        if (registerCourt.trim().isEmpty()) {
            m.add(K_COURT);
        }
        if (registerNumber.trim().isEmpty()) {
            m.add(K_NUMBER);
        }
        if (managingDirectors.trim().isEmpty()) {
            m.add(K_DIRECTORS);
        }
        return m;
    }

    /**
     * @param court the register court, e.g. "Amtsgericht Charlottenburg" or
     * "Charlottenburg"
     * @return the court with "Amtsgericht" in front if no court type is named
     */
    static String court(String court) {
        String c = nvl(court).trim();
        if (c.isEmpty() || c.toLowerCase().startsWith("amtsgericht") || c.toLowerCase().startsWith("ag ")
                || c.toLowerCase().startsWith("registergericht")) {
            return c;
        }
        return "Amtsgericht " + c;
    }

    /**
     * @return the register entry, e.g. "Amtsgericht Charlottenburg, HRB
     * 12345 B"
     */
    public String registerLine() {
        String c = court(registerCourt);
        String n = registerNumber.trim();
        if (!n.isEmpty() && !n.toUpperCase().startsWith("HR")) {
            n = "HRB " + n;
        }
        return join(", ", c, n);
    }

    /**
     * @param seatLabel "Sitz" / "Registered office"
     * @param registerLabel "Registergericht" / "Register court"
     * @param directorsLabel "Geschäftsführer" / "Managing directors"
     * @param boardLabel "Vorsitzender des Aufsichtsrats" / "Chair of the
     * supervisory board"
     * @return the line for the footer, e.g. "Muster GmbH · Sitz: Berlin ·
     * Registergericht: Amtsgericht Charlottenburg, HRB 12345 B ·
     * Geschäftsführer: Anna Muster, Ben Beispiel"
     */
    public String legalLine(String seatLabel, String registerLabel, String directorsLabel, String boardLabel) {
        List<String> parts = new ArrayList<String>();
        String company = name;
        if (!company.isEmpty() && !legalForm.isEmpty() && !company.contains(legalForm)) {
            company = company + " " + legalForm;
        }
        if (!company.isEmpty()) {
            parts.add(company);
        }
        if (!seat.isEmpty()) {
            parts.add(seatLabel + ": " + seat);
        }
        String reg = registerLine();
        if (!reg.isEmpty()) {
            parts.add(registerLabel + ": " + reg);
        }
        if (!managingDirectors.isEmpty()) {
            parts.add(directorsLabel + ": " + directors());
        }
        if (!supervisoryChair.isEmpty()) {
            parts.add(boardLabel + ": " + supervisoryChair);
        }
        return String.join(" · ", parts);
    }

    /**
     * @return the managing directors, separated by ", " (entered with commas,
     * semicolons or one per line)
     */
    public String directors() {
        List<String> l = new ArrayList<String>();
        for (String s : managingDirectors.split("[,;\\n]")) {
            if (!s.trim().isEmpty()) {
                l.add(s.trim());
            }
        }
        return String.join(", ", l);
    }

    /**
     * @param seatLabel
     * @param registerLabel
     * @param directorsLabel
     * @param boardLabel
     * @return the template fields {@code gmbh.*}
     */
    public Map<String, Object> templateFields(String seatLabel, String registerLabel, String directorsLabel, String boardLabel) {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("gmbh.legalform", legalForm);
        m.put("gmbh.seat", seat);
        m.put("gmbh.registercourt", court(registerCourt));
        m.put("gmbh.registernumber", registerNumber);
        m.put("gmbh.register", registerLine());
        m.put("gmbh.managingdirectors", directors());
        m.put("gmbh.supervisorychair", supervisoryChair);
        m.put("gmbh.sharecapital", shareCapital);
        m.put("gmbh.legalline", legalLine(seatLabel, registerLabel, directorsLabel, boardLabel));
        return m;
    }

    static String nvl(String s) {
        return s == null || "null".equals(s) ? "" : s;
    }

    static String join(String sep, String... parts) {
        StringBuilder b = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.trim().isEmpty()) {
                if (b.length() > 0) {
                    b.append(sep);
                }
                b.append(p.trim());
            }
        }
        return b.toString();
    }
}
