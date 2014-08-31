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

    private final HashMap<String, String> paths;
    private final HashMap<String, Font> used;
    public static YabsFontFactoryImpl instance = new YabsFontFactoryImpl();

    private YabsFontFactoryImpl() {
        super();
        this.paths = new HashMap<String, String>();
        this.used = new HashMap<String, Font>();
        readUsed();
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
                Log.Debug(this, ex.getLocalizedMessage());
            } catch (FontFormatException ex1) {
                Log.Debug(this, ex1.getLocalizedMessage());
            } catch (IOException ex1) {
                Log.Debug(this, ex1.getLocalizedMessage());
            }
        } catch (IOException ex) {
            Log.Debug(this, ex.getLocalizedMessage());
        } catch (Exception e) {
            Log.Debug(this, e.getLocalizedMessage());
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
        if (used.containsKey(key)) {
            font = used.get(key);
        } else {
            Fonts fonts = new Fonts();
            fonts.setCname(fontname);
            fonts.setEncoding(encoding);
            fonts.setIsEmbedded(embedded);
            fonts.setSize(size);
            fonts.setStyle(style);
            if (color != null) {
                fonts.setColor(color.getRGB());
            } else {
                fonts.setColor(0);
            }
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
            font = buildFont(fonts);
        }
        return font;
    }

    private void readUsed() {
        ArrayList<Fonts> list = Fonts.get();
        for (Fonts f : list) {
            buildFont(f);
        }
    }

    private Font buildFont(Fonts f) {
        File tmp = f.__getFont();
        if (!"empty".equals(f.__getFilename()) && (tmp == null || !tmp.exists())) {
            f.delete();
            return null;
        }
        String key = f.__getCname() + "#" + f.__getEncoding() + "#";
        if (f.__isEmbedded()) {
            key += "1" + "#" + f.__getSize() + "#" + f.__getStyle() + "#";
        } else {
            key += "0" + "#" + +f.__getSize() + "#" + f.__getStyle() + "#";
        }
        key += f.__getColor();
        BaseFont basefont;
        Font font = null;
        try {
            if (!"empty".equals(f.__getFilename())) {
                basefont = BaseFont.createFont(f.__getFont().getAbsolutePath(), f.__getEncoding(), f.__isEmbedded(), true, null, null, true);
                font = new Font(basefont, f.__getSize(), f.__getStyle(), new Color(f.__getColor()));
            } else {
                font = new Font(Font.UNDEFINED, f.__getSize(), f.__getStyle(), new Color(f.__getColor()));
            }
            used.put(key, font);
        } catch (DocumentException ex) {
            Log.Debug(this, ex.getLocalizedMessage());
        } catch (IOException ex) {
            Log.Debug(this, ex.getLocalizedMessage());
        }
        return font;
    }
}
