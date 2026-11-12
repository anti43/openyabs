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
package mpv5.pos.javapos;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import mpv5.Main;
import mpv5.logging.Log;

/**
 * JavaPOS (and a vendor ADK such as the Epson JavaPOS ADK) linked at runtime:
 * the jars are downloaded or copied into &lt;app folder&gt;/javapos and loaded
 * with an own class loader. YaBS does not depend on them; code that uses
 * JavaPOS gets its classes from {@link #classLoader()} (reflection).
 */
public final class JavaPos {

    /**
     * The JavaPOS standard (controls, contracts, jpos.xml loader) from Maven
     * Central; a vendor ADK archive (.zip, .tar.gz) or jar can be added
     */
    public static final String DEFAULT_URLS
            = "https://repo1.maven.org/maven2/org/javapos/javapos-contracts/1.15.5/javapos-contracts-1.15.5.jar "
            + "https://repo1.maven.org/maven2/org/javapos/javapos-controls/1.15.0/javapos-controls-1.15.0.jar "
            + "https://repo1.maven.org/maven2/org/javapos/javapos-config-loader/4.0.2/javapos-config-loader-4.0.2.jar";
    /**
     * Where the Epson JavaPOS ADK is installed by default
     */
    public static final String[] INSTALLED = {"/opt/EpsonJavaPOS", "C:\\Program Files\\EPSON\\JavaPOS",
        "C:\\Program Files (x86)\\EPSON\\JavaPOS", "/Library/Java/Extensions/EpsonJavaPOS"};
    private static URLClassLoader loader;

    private JavaPos() {
    }

    /**
     * @return &lt;app folder&gt;/javapos
     */
    public static File folder() {
        return new File(Main.MPPATH == null ? System.getProperty("user.home") : Main.MPPATH, "javapos");
    }

    /**
     * @param name a file name
     * @return true for the files taken from an ADK: jars, jpos.xml, native
     * libraries
     */
    static boolean wanted(String name) {
        String n = name.toLowerCase();
        return n.endsWith(".jar") || n.equals("jpos.xml") || n.endsWith(".so") || n.contains(".so.")
                || n.endsWith(".dll") || n.endsWith(".dylib") || n.endsWith(".jnilib");
    }

    /**
     * @return the jars in the folder
     */
    public static List<File> jars() {
        File[] f = folder().listFiles((d, n) -> n.toLowerCase().endsWith(".jar"));
        List<File> l = new ArrayList<File>(f == null ? List.of() : Arrays.asList(f));
        l.sort(null);
        return l;
    }

    /**
     * @return a class loader for the JavaPOS jars (parent: the YaBS class
     * loader); new after every download or import
     */
    public static synchronized ClassLoader classLoader() {
        if (loader == null) {
            List<URL> urls = new ArrayList<URL>();
            try {
                urls.add(folder().toURI().toURL());//jpos.xml as resource
                for (File j : jars()) {
                    urls.add(j.toURI().toURL());
                }
            } catch (Exception e) {
                Log.Debug(JavaPos.class, e);
            }
            loader = new URLClassLoader(urls.toArray(new URL[0]), JavaPos.class.getClassLoader());
            File xml = new File(folder(), "jpos.xml");
            if (xml.isFile() && System.getProperty("jpos.config.populatorFile") == null) {
                System.setProperty("jpos.config.populatorFile", xml.getAbsolutePath());
            }
        }
        return loader;
    }

    private static synchronized void reload() {
        if (loader != null) {
            try {
                loader.close();
            } catch (IOException e) {
                Log.Debug(JavaPos.class, e);
            }
        }
        loader = null;
    }

    /**
     * @param className
     * @return the class from the JavaPOS jars
     * @throws ClassNotFoundException if JavaPOS is not there
     */
    public static Class<?> load(String className) throws ClassNotFoundException {
        return Class.forName(className, true, classLoader());
    }

    /**
     * @return true if the JavaPOS controls (jpos.POSPrinter) can be loaded
     */
    public static boolean available() {
        try {
            Class.forName("jpos.POSPrinter", false, classLoader());
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Downloads the given addresses (separated by spaces or lines): jars are
     * stored, archives (.zip, .tar.gz, .tgz) are unpacked
     *
     * @param urls
     * @return the files stored
     * @throws Exception
     */
    public static List<File> download(String urls) throws Exception {
        List<File> stored = new ArrayList<File>();
        HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20)).build();
        for (String u : urls.trim().split("\\s+")) {
            if (u.isEmpty()) {
                continue;
            }
            String name = u.replaceAll("[?#].*$", "");
            name = name.substring(name.lastIndexOf('/') + 1);
            File tmp = Files.createTempFile("javapos", "-" + name).toFile();
            try {
                HttpResponse<java.nio.file.Path> r = http.send(HttpRequest.newBuilder(URI.create(u)).timeout(Duration.ofMinutes(5)).build(),
                        HttpResponse.BodyHandlers.ofFile(tmp.toPath()));
                if (r.statusCode() / 100 != 2) {
                    throw new IOException(u + ": HTTP " + r.statusCode());
                }
                String type = r.headers().firstValue("content-type").orElse("");
                if (type.startsWith("text/html")) {
                    throw new IOException(u + " is a web page, not a jar or archive: download the ADK there and add it with \"Add ADK\"");
                }
                stored.addAll(importFile(tmp, name));
            } finally {
                tmp.delete();
            }
        }
        reload();
        return stored;
    }

    /**
     * Copies an ADK: an installed ADK folder, an archive or a jar
     *
     * @param f
     * @return the files stored
     * @throws IOException
     */
    public static List<File> importFrom(File f) throws IOException {
        List<File> l = f.isDirectory() ? importFolder(f) : importFile(f, f.getName());
        reload();
        return l;
    }

    private static List<File> importFolder(File dir) throws IOException {
        List<File> l = new ArrayList<File>();
        File[] children = dir.listFiles();
        if (children != null) {
            for (File c : children) {
                if (c.isDirectory()) {
                    l.addAll(importFolder(c));
                } else if (wanted(c.getName())) {
                    try (InputStream in = new FileInputStream(c)) {
                        l.add(store(c.getName(), in));
                    }
                }
            }
        }
        return l;
    }

    static List<File> importFile(File f, String name) throws IOException {
        String n = name.toLowerCase();
        List<File> l = new ArrayList<File>();
        if (n.endsWith(".zip")) {
            try (ZipInputStream in = new ZipInputStream(new FileInputStream(f))) {
                for (ZipEntry e = in.getNextEntry(); e != null; e = in.getNextEntry()) {
                    String base = e.getName().substring(e.getName().lastIndexOf('/') + 1);
                    if (!e.isDirectory() && wanted(base)) {
                        l.add(store(base, in));
                    }
                }
            }
        } else if (n.endsWith(".tar.gz") || n.endsWith(".tgz") || n.endsWith(".tar")) {
            try (InputStream raw = new BufferedInputStream(new FileInputStream(f));
                    InputStream in = n.endsWith(".tar") ? raw : new GZIPInputStream(raw)) {
                untar(in, l);
            }
        } else if (wanted(name)) {
            try (InputStream in = new FileInputStream(f)) {
                l.add(store(name, in));
            }
        } else {
            throw new IOException(name + ": not a jar, .zip or .tar.gz");
        }
        return l;
    }

    /**
     * A minimal tar reader (ustar): regular files only
     */
    private static void untar(InputStream in, List<File> l) throws IOException {
        byte[] header = new byte[512];
        while (in.readNBytes(header, 0, 512) == 512) {
            String name = text(header, 0, 100);
            if (name.isEmpty()) {
                break;
            }
            String prefix = text(header, 345, 155);
            if (!prefix.isEmpty()) {
                name = prefix + "/" + name;
            }
            String sizeText = text(header, 124, 12).trim();
            long size = sizeText.isEmpty() ? 0 : Long.parseLong(sizeText, 8);
            char type = (char) header[156];
            String base = name.substring(name.lastIndexOf('/') + 1);
            if ((type == '0' || type == 0) && wanted(base)) {
                l.add(store(base, new LimitedStream(in, size)));
            } else {
                in.skipNBytes(size);
            }
            long pad = (512 - size % 512) % 512;
            in.skipNBytes(pad);
        }
    }

    private static String text(byte[] b, int off, int len) {
        int end = off;
        while (end < off + len && b[end] != 0) {
            end++;
        }
        return new String(b, off, end - off, StandardCharsets.UTF_8);
    }

    private static File store(String name, InputStream in) throws IOException {
        File dir = folder();
        dir.mkdirs();
        File target = new File(dir, new File(name).getName());
        File tmp = new File(dir, target.getName() + ".part");
        try (OutputStream out = new FileOutputStream(tmp)) {
            in.transferTo(out);
        }
        Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    /**
     * Reads at most n bytes, does not close the underlying stream
     */
    private static final class LimitedStream extends InputStream {

        private final InputStream in;
        private long left;

        LimitedStream(InputStream in, long n) {
            this.in = in;
            this.left = n;
        }

        @Override
        public int read() throws IOException {
            if (left <= 0) {
                return -1;
            }
            int b = in.read();
            if (b >= 0) {
                left--;
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (left <= 0) {
                return -1;
            }
            int r = in.read(b, off, (int) Math.min(len, left));
            if (r > 0) {
                left -= r;
            }
            return r;
        }
    }

    /**
     * @return the first installed Epson ADK folder, null if none
     */
    public static File installedAdk() {
        for (String p : INSTALLED) {
            File f = new File(p);
            if (f.isDirectory()) {
                return f;
            }
        }
        return null;
    }
}
