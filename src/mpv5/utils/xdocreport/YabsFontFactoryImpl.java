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

import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.xdocreport.itext.extension.font.ExtendedFontFactoryImp;
import java.awt.Color;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import mpv5.db.objects.Fonts;
import mpv5.logging.Log;

/**
 *
 * @author dev
 */
public class YabsFontFactoryImpl extends ExtendedFontFactoryImp {

    private final HashMap<String, String> paths = new HashMap<String, String>();
    private static final HashMap<String, Font> used = new HashMap<String, Font>();
    private static final HashMap<String, Fonts> cached = new HashMap<String, Fonts>();
    public static YabsFontFactoryImpl instance = new YabsFontFactoryImpl();

    private YabsFontFactoryImpl() {
        super();
    }

    @Override
    public void register(String path, String alias) {
        try {
            java.awt.Font cf = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new File(path));
            paths.put(cf.getFamily(), path);
        } catch (FontFormatException ex) {
            try {
                java.awt.Font cf = java.awt.Font.createFont(java.awt.Font.TYPE1_FONT, new File(path));
                paths.put(cf.getFontName(), path);
                Log.Debug(this, ex);
            } catch (Exception ex1) {
                Log.Debug(this, ex1);
            }
        } catch (Exception e) {
            Log.Debug(this, e);
        }
    }

    @Override
    public int registerDirectories() {
        if (used.isEmpty()) {
            super.registerDirectories();
        }
        return 0;
    }

    @Override
    public Font getFont(String fontname, String encoding, boolean embedded, float size, int style, Color color, boolean cached) {
        Font font;
        Fonts fonts;
        String key = createKey(fontname, encoding, embedded, size, style, color);
        String cachekey = createCacheKey(fontname, encoding, embedded);
        if (used.containsKey(key)) {
            font = used.get(key);
        } else {
            Log.Debug(this, "Font is NOT cached: " + key);
            if (YabsFontFactoryImpl.cached.containsKey(cachekey)) {
                fonts = YabsFontFactoryImpl.cached.get(cachekey);
                font = buildFont(fonts, size, color, style);
                used.put(key, font);
            } else {
                fonts = new Fonts();
                fonts.setCname(fontname);
                fonts.setEncoding(encoding);
                fonts.setIsEmbedded(embedded);
                
                if (paths.containsKey(fontname)) {
                    String get = paths.get(fontname);
                    if (get != null) {
                        String filename = get.substring(get.lastIndexOf(File.separator));
                        fonts.setFilename(filename);
                        File f = new File(get);
                        fonts.setFont(f);
                    } else {
                        fonts.setFilename("empty");
                        fonts.setFont(null);
                    }

                } else {
                    fonts.setFilename("empty");
                    fonts.setFont(null);
                }
                fonts.save();
                font = buildFont(fonts, size, color, style);
                YabsFontFactoryImpl.cached.put(cachekey, fonts);
                used.put(key, font);
                Log.Debug(this, "Font is saved: " + key);
            }
        }
        return font;
    }

    private static Font buildFont(Fonts f, float size, Color color, int style) {
        File tmp = f.__getFont();
        if (!"empty".equals(f.__getFilename()) && (tmp == null || !tmp.exists())) {
            f.delete();
            return null;
        }
        BaseFont basefont;
        Font font = null;
        try {
            if (!"empty".equals(f.__getFilename()) && f.__getFilename().endsWith("ttf")) {
                basefont = BaseFont.createFont(f.__getFont().getAbsolutePath(), f.__getEncoding(), f.__isEmbedded(), true, null, null, true);
                font = new Font(basefont, size, style, color);
            } else {
                font = new Font(Font.UNDEFINED, size, style, color);
            }
        } catch (DocumentException ex) {
            Log.Debug(YabsFontFactoryImpl.class, ex);
        } catch (IOException ex) {
            Log.Debug(YabsFontFactoryImpl.class, ex);
        }
        return font;
    }

    public static void cacheFonts() {
        ArrayList<Fonts> list = Fonts.get();
        for (Fonts f : list) {
            String key = createCacheKey(f.__getCname(), f.__getEncoding(), f.__isEmbedded());
            cached.put(key, f);
            Log.Debug(YabsFontFactoryImpl.class, "cached Font: " + key);
        }
        Log.Debug(YabsFontFactoryImpl.class, "cacheFonts: " + cached.size());
    }

    private static String createKey(String fontname, String encoding, boolean embedded,
            float size, int style, Color color) {
        String key = fontname + "#" + encoding + "#";
        if (embedded) {
            key += "1" + "#" + size + "#" + style + "#";
        } else {
            key += "0" + "#" + size + "#" + style + "#";
        }
        if (color != null) {
            key += color.getRGB();
        } else {
            key += "0";
        }
        return key;
    }

    private static String createCacheKey(String fontname, String encoding, boolean embedded) {
        String key = fontname + "#" + encoding + "#";
        if (embedded) {
            key += "1";
        } else {
            key += "0";
        }

        return key;
    }
}
