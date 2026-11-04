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
package mpv5.utils.xdocreport;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.xdocreport.itext.extension.font.ITextFontRegistry;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Font registry for PDF/A output (ZUGFeRD): every font is embedded.
 * <p>
 * The default registry falls back to the built-in, non embedded Helvetica
 * when a font of the template is not installed (e.g. Arial on Linux), which
 * violates PDF/A. This registry embeds the requested font if it is installed,
 * otherwise a metric compatible installed substitute (Liberation, DejaVu,
 * ...).
 */
public class EmbeddingFontRegistry extends ITextFontRegistry {

    private static final EmbeddingFontRegistry INSTANCE = new EmbeddingFontRegistry();
    private static final List<String> SANS = Arrays.asList("Arial", "Liberation Sans", "Helvetica", "Arimo",
            "DejaVu Sans", "FreeSans", "Noto Sans", "Segoe UI", "Tahoma", "Verdana");
    private static final List<String> SERIF = Arrays.asList("Times New Roman", "Liberation Serif", "Tinos",
            "DejaVu Serif", "FreeSerif", "Noto Serif", "Georgia");
    private static final List<String> MONO = Arrays.asList("Courier New", "Liberation Mono", "Cousine",
            "DejaVu Sans Mono", "FreeMono", "Noto Mono", "Consolas");

    public static EmbeddingFontRegistry getRegistry() {
        return INSTANCE;
    }

    @Override
    public Font getFont(String familyName, String encoding, float size, int style, Color color) {
        for (String candidate : candidates(familyName)) {
            try {
                Font f = FontFactory.getFont(resolveFamilyName(candidate, style), BaseFont.IDENTITY_H,
                        BaseFont.EMBEDDED, size, style, color);
                if (isEmbeddable(f)) {
                    return f;
                }
            } catch (Exception e) {
                //try the next one
            }
        }
        return super.getFont(familyName, encoding, size, style, color);
    }

    private static boolean isEmbeddable(Font f) {
        BaseFont bf = f == null ? null : f.getBaseFont();
        //built-in Type 1 fonts (Helvetica, Times, ...) have no font program to embed
        return bf != null && bf.getFontType() != BaseFont.FONT_TYPE_T1;
    }

    /**
     * @param familyName
     * @return the font itself followed by installed substitutes
     */
    static List<String> candidates(String familyName) {
        List<String> list = new ArrayList<String>();
        String f = familyName == null ? "" : familyName.trim();
        if (!f.isEmpty()) {
            list.add(f);
        }
        String l = f.toLowerCase(Locale.ROOT);
        List<String> generic;
        if (l.contains("mono") || l.contains("courier") || l.contains("consol") || l.contains("typewriter")) {
            generic = MONO;
        } else if (l.contains("serif") && !l.contains("sans") || l.contains("times") || l.contains("georgia")
                || l.contains("cambria") || l.contains("garamond") || l.contains("roman")) {
            generic = SERIF;
        } else {
            generic = SANS;
        }
        for (String g : generic) {
            if (!g.equalsIgnoreCase(f)) {
                list.add(g);
            }
        }
        return list;
    }
}
