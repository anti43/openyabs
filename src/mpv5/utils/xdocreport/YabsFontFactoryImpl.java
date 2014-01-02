/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.xdocreport;

import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.xdocreport.itext.extension.font.ExtendedFontFactoryImp;
import java.awt.Color;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.HashMap;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryData;
import mpv5.db.common.QueryHandler;

/**
 *
 * @author dev
 */
public class YabsFontFactoryImpl extends ExtendedFontFactoryImp {

    private final HashMap<String, String> paths;

    public YabsFontFactoryImpl() {
        super();
        this.paths = new HashMap<String, String>();
        readUsed();
    }

    @Override
    public void register(String path, String alias) {
        super.register(path, alias);
        save(path, alias);
    }

    @Override
    public int registerDirectories() {
        if (!checkCache()) {
            return super.registerDirectories();
        } else {
            for (String s : paths.values()) {
                super.register(s, null);
            }
        }

        return paths.size();
    }

    @Override
    public Font getFont(String fontname, String encoding, boolean embedded, float size, int style, Color color, boolean cached) {
        if (!paths.containsKey(fontname.toLowerCase())) {
            try {
                QueryData qd = new QueryData();
                qd.add("used", 1);
                QueryCriteria qc = new QueryCriteria("cname", fontname.toLowerCase());
                qc.addAndCondition("terminal", getMac());
                
                Object[][] data = QueryHandler.instanceOf()
                        .clone("fontsforitext")
                        .select(qc)
                        .getData();
                Integer id = Integer.parseInt(data[0][0].toString());
                QueryHandler.instanceOf()
                        .clone("fontsforitext")
                        .update(qd, id, fontname + " succesfully cached!");
                 paths.put(fontname.toLowerCase(), null);
            } catch (NodataFoundException ex) {
                
            }
        }
        return super.getFont(fontname, encoding, embedded, size, style, color, cached);
    }

    /*
     * This is a Copy from FontFactoryImp (as of itext which is an super-class)
     */
    private void save(String path, String alias) {
        try {
            if (path.toLowerCase().endsWith(".ttf") || path.toLowerCase().endsWith(".otf") || path.toLowerCase().indexOf(".ttc,") > 0) {
                Object allNames[] = BaseFont.getAllFontNames(path, BaseFont.WINANSI, null);
                saveIfNeeded(((String) allNames[0]).toLowerCase(), path);
                if (alias != null) {
                    saveIfNeeded(alias.toLowerCase(), path);
                }
                // register all the font names with all the locales
                String[][] names = (String[][]) allNames[2]; //full name
                for (String[] name : names) {
                    saveIfNeeded(name[3].toLowerCase(), path);
                }
            } else if (path.toLowerCase().endsWith(".ttc")) {
                if (alias != null) {
                    System.err.println("class FontFactory: You can't define an alias for a true type collection.");
                }
                String[] names = BaseFont.enumerateTTCNames(path);
                for (int i = 0; i < names.length; i++) {
                    register(path + "," + i);
                }
            } else if (path.toLowerCase().endsWith(".afm") || path.toLowerCase().endsWith(".pfm")) {
                BaseFont bf = BaseFont.createFont(path, BaseFont.CP1252, false);
                String fullName = bf.getFullFontName()[0][3].toLowerCase();
                String psName = bf.getPostscriptFontName().toLowerCase();
                saveIfNeeded(psName, path);
                saveIfNeeded(fullName, path);
            }
        } catch (DocumentException de) {
            // this shouldn't happen
            throw new ExceptionConverter(de);
        } catch (IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }

    private void saveIfNeeded(String name, String path) {
        try {
            QueryCriteria qc = new QueryCriteria("cname", name);
            qc.addAndCondition("terminal", getMac());
            QueryHandler.instanceOf()
                    .clone("fontsforitext")
                    .select(qc)
                    .getData();
        } catch (NodataFoundException ex) {
            QueryData q = new QueryData();
            q.add("cname", name);
            q.add("path", path);
            q.add("terminal", getMac());
            q.add("used", 0);
            QueryHandler.instanceOf()
                    .clone("fontsforitext")
                    .insert(q, name + " succesfully cached!");
        }
    }

    private void readUsed() {
        try {
            String mac = getMac();
            QueryCriteria qc = new QueryCriteria("used", 1);
            qc.addAndCondition("terminal", mac);
            Object[][] data = QueryHandler.instanceOf()
                    .clone("fontsforitext")
                    .select(qc)
                    .getData();
            for (Object[] o : data) {
                paths.put((String) o[1], (String) o[2]);
            }
        } catch (NodataFoundException ex) {
            //  Log.Debug(ex);
        }
    }

    private boolean checkCache() {
        if (paths.isEmpty()) {
            try {
                QueryCriteria qc = new QueryCriteria("terminal", getMac());

                QueryHandler.instanceOf()
                        .clone("fontsforitext")
                        .select(qc)
                        .getData();
                return true;
            } catch (NodataFoundException ex) {
                return false;
            }
        } else {
            return true;
        }
    }

    private String getMac() {
        try {
            NetworkInterface ni = NetworkInterface.getNetworkInterfaces().nextElement();
            if (ni == null)
                return "unknown";
            
            byte[] mac = ni.getHardwareAddress();
            if (mac == null)
                return "unknown";
            
            StringBuilder sb = new StringBuilder(18);
            for (byte b : mac) {
                if (sb.length() > 0)
                    sb.append(':');
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (SocketException ex) {
            return "unknown";
        }
    }
}
