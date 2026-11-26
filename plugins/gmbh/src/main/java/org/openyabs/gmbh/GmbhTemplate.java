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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryData;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.handler.TemplateHandler;
import mpv5.logging.Log;

/**
 * The demo invoice template of a GmbH: the bundled demo invoice
 * ({@code /extra/templates/demo-00.odt}) with the details of § 35a GmbHG
 * ({@code <gmbh.legalline>}) as first line of the footer, and the check
 * whether a template prints these details.
 */
public final class GmbhTemplate {

    /**
     * the demo invoice of YaBS the template is made from
     */
    public static final String BASE = "/extra/templates/demo-00.odt";
    /**
     * the placeholder of the footer line
     */
    public static final String LEGAL_LINE = "gmbh.legalline";

    private static final String FOOTER = "<style:footer>";
    private static final String LINE = "<text:p text:style-name=\"MP1\"><text:span text:style-name=\"MT2\">"
            + "<text:placeholder text:placeholder-type=\"text\">&lt;" + LEGAL_LINE + "&gt;</text:placeholder>"
            + "</text:span></text:p>";

    private GmbhTemplate() {
    }

    /**
     * @return the bundled demo invoice of YaBS, null if not found
     * @throws IOException
     */
    static byte[] baseTemplate() throws IOException {
        try (InputStream in = mpv5.Main.class.getResourceAsStream(BASE)) {
            return in == null ? null : in.readAllBytes();
        }
    }

    /**
     * @param odt an ODT
     * @return the ODT with the line of the mandatory details as first line of
     * every footer (styles.xml); unchanged parts are copied as they are
     * @throws IOException
     */
    public static byte[] withLegalLine(byte[] odt) throws IOException {
        Map<String, byte[]> files = read(odt);
        byte[] styles = files.get("styles.xml");
        if (styles == null) {
            throw new IOException("styles.xml missing");
        }
        String s = new String(styles, StandardCharsets.UTF_8);
        if (!s.contains(LEGAL_LINE)) {
            if (s.contains(FOOTER)) {
                s = s.replace(FOOTER, FOOTER + LINE);
            } else {
                throw new IOException("no footer in the template");
            }
        }
        files.put("styles.xml", s.getBytes(StandardCharsets.UTF_8));
        return write(files);
    }

    /**
     * @param odt
     * @return true if the content or the styles (header, footer) of the ODT
     * use the line or the register number of the mandatory details
     */
    public static boolean printsDetails(byte[] odt) {
        try {
            Map<String, byte[]> files = read(odt);
            for (String n : new String[]{"content.xml", "styles.xml"}) {
                byte[] b = files.get(n);
                if (b == null) {
                    continue;
                }
                String s = new String(b, StandardCharsets.UTF_8);
                if (s.contains(LEGAL_LINE) || s.contains("gmbh.register") || s.contains("companyinfo." + GmbhData.K_NUMBER)) {
                    return true;
                }
            }
        } catch (IOException e) {
            Log.Debug(GmbhTemplate.class, e.getMessage());
        }
        return false;
    }

    private static Map<String, byte[]> read(byte[] odt) throws IOException {
        Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
        try (ZipInputStream zin = new ZipInputStream(new java.io.ByteArrayInputStream(odt))) {
            ZipEntry e;
            while ((e = zin.getNextEntry()) != null) {
                if (!e.isDirectory()) {
                    files.put(e.getName(), zin.readAllBytes());
                }
            }
        }
        return files;
    }

    private static byte[] write(Map<String, byte[]> files) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zout = new ZipOutputStream(out)) {
            //ODF: "mimetype" first and not compressed
            byte[] mime = files.get("mimetype");
            if (mime != null) {
                ZipEntry m = new ZipEntry("mimetype");
                m.setMethod(ZipEntry.STORED);
                m.setSize(mime.length);
                CRC32 crc = new CRC32();
                crc.update(mime);
                m.setCrc(crc.getValue());
                zout.putNextEntry(m);
                zout.write(mime);
                zout.closeEntry();
            }
            for (Map.Entry<String, byte[]> f : files.entrySet()) {
                if ("mimetype".equals(f.getKey())) {
                    continue;
                }
                zout.putNextEntry(new ZipEntry(f.getKey()));
                zout.write(f.getValue());
                zout.closeEntry();
            }
        }
        return out.toByteArray();
    }

    /**
     * Imports the demo template into the templates (not assigned: the user
     * assigns it in Settings › Templates)
     *
     * @param name the name of the template
     * @return the template, null if it could not be imported
     * @throws Exception
     */
    public static Template importDemo(String name) throws Exception {
        byte[] base = baseTemplate();
        if (base == null) {
            return null;
        }
        File f = mpv5.utils.files.FileDirectoryHandler.getTempFile("gmbh-rechnung", "odt");
        try {
            Files.write(f.toPath(), withLegalLine(base));
            String stored = QueryHandler.instanceOf().clone(Context.getFiles()).insertFileNow(f);
            if (stored == null) {
                return null;
            }
            QueryData x = new QueryData();
            x.add("cname", name);
            x.add("filename", stored);
            x.add("description", "GmbH plugin");
            x.add("dateadded", mpv5.utils.date.DateConverter.getTodayDBDate());
            x.add("intaddedby", User.getCurrentUser().__getIDS());
            x.add("intsize", f.length());
            x.add("mimetype", String.valueOf(Constants.TYPE_INVOICE));
            x.add("format", mpv5.handler.FirstStartSetup.ITEM_FORMAT);
            x.add("groupsids", 1);
            int id = QueryHandler.instanceOf().clone(Context.getTemplate()).insert(x, null);
            if (id <= 0) {
                return null;
            }
            DatabaseObject.uncacheObject(Context.getTemplate(), id);
            TemplateHandler.clearCache();
            return (Template) DatabaseObject.getObject(Context.getTemplate(), id);
        } finally {
            f.delete();
        }
    }

    /**
     * @param name
     * @return true if a template of that name exists
     */
    public static boolean exists(String name) {
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getTemplate())) {
                if (name.equals(o.__getCname())) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.Debug(GmbhTemplate.class, e.getMessage());
        }
        return false;
    }

    private static final Map<String, Boolean> CHECKED = new java.util.concurrent.ConcurrentHashMap<String, Boolean>();

    /**
     * @param t a template
     * @return true if the template prints the details (cached by file name)
     */
    public static boolean printsDetails(Template t) {
        if (t == null) {
            return true;
        }
        String key = t.__getIDS() + "/" + t.__getFilename();
        Boolean b = CHECKED.get(key);
        if (b == null) {
            try {
                File f = t.getFile();
                b = f != null && f.isFile() && printsDetails(Files.readAllBytes(f.toPath()));
            } catch (Exception e) {
                Log.Debug(GmbhTemplate.class, e.getMessage());
                //unknown: no warning
                b = Boolean.TRUE;
            }
            CHECKED.put(key, b);
        }
        return b;
    }
}
