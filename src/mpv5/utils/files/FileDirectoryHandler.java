/*
 * 
 * 
 */
package mpv5.utils.files;

/**
 *
 * Galileo Computing
 */
import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.text.RandomText;

public abstract class FileDirectoryHandler {

    /**
     * Deletes a file now or later
     *
     * @param localFile
     * @param now
     */
    public static void deleteFile(File localFile, boolean now) {
        if (now) {
            try {
                deleteTree(localFile);
            } catch (IOException ex) {
                Log.Debug(ex);
            }
        } else {
            localFile.deleteOnExit();
        }
    }

    /**
     * Deletes a whole directory tree and files in it
     *
     * @param path
     * @throws java.io.IOException
     */
    public synchronized static void deleteTree(File path) throws IOException {
        deleteDirectoryContent(path);
        if (!path.delete()) {
            path.deleteOnExit();
        }
    }

    /**
     * Deletes the content of a directory
     *
     * @param path
     * @throws java.io.IOException
     */
    public synchronized static void deleteDirectoryContent(File path) throws IOException {
        for (File file : path.listFiles()) {
            if (file.isDirectory()) {
                deleteTree(file);
            } else {
                Log.Debug(FileDirectoryHandler.class, "Delete: " + file.getCanonicalPath());
                if (!file.delete()) {
                    file.deleteOnExit();
                }
            }
        }
    }

    /**
     *
     * @param sourceLocation
     * @param targetLocation
     * @return
     * @throws java.io.IOException
     */
    public static URI copyDirectory(File sourceLocation, File targetLocation) throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
        return targetLocation.toURI();
    }

    /**
     * Copies a file via stream
     *
     * @param source
     * @param target
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static File copyFile2(File source, File target, boolean silent) throws FileNotFoundException, IOException {
        Log.Debug(FileDirectoryHandler.class, "Copying file from "
                + source + " to " + target);
        return new File(copyFile(source, target, silent));
    }

    /**
     * Copies a file via stream
     *
     * @param source
     * @param target
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static File copyFile2(File source, File target) throws FileNotFoundException, IOException {
        Log.Debug(FileDirectoryHandler.class, "Copying file from "
                + source + " to " + target);
        return new File(copyFile(source, target, true));
    }

    /**
     * Copies a file via stream
     *
     * @param sourceFile
     * @param targetDirectory
     * @param targetFilename
     * @param deleteOnExit Shall we delete the NEW file on exit
     * @return
     * @throws java.io.IOException
     */
    public static URI copyFile(File sourceFile, File targetDirectory, String targetFilename, boolean deleteOnExit)
            throws IOException {
        targetFilename = check(targetFilename);
        return copyFile(sourceFile, targetDirectory, targetFilename, deleteOnExit, true);
    }

    /**
     * Copies a file via stream
     *
     * @param sourceFile
     * @param targetDirectory
     * @param targetFilename
     * @param deleteOnExit Shall we delete the NEW file on exit
     * @param silent
     * @return
     * @throws java.io.IOException
     */
    public static URI copyFile(File sourceFile, File targetDirectory, String targetFilename, boolean deleteOnExit, boolean silent)
            throws IOException {
        targetFilename = check(targetFilename);
        FileOutputStream out = null;
        InputStream in = new FileInputStream(sourceFile);
        File outp = null;
        if (targetDirectory != null) {
            outp = new File(targetDirectory + File.separator + targetFilename);
            if (!targetDirectory.exists()) {
                targetDirectory.mkdirs();
            }
            outp.delete();
            out = new FileOutputStream(targetDirectory + File.separator + targetFilename);
        } else {
            outp = new File(targetFilename);
            outp.delete();
            out = new FileOutputStream(targetFilename);
        }


        // Copy the bits from instream to outstream
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();

        if (deleteOnExit) {
            outp.deleteOnExit();
        }

        if (!silent) {
            Notificator.raiseNotification(Messages.FILE_SAVED + " " + outp.getPath(), false);
        }

        return outp.toURI();
    }
    
    
    public static void copyFile(InputStream in, File outFile) throws Exception { 
        FileOutputStream out = new FileOutputStream(outFile);
 
        // Copy the bits from instream to outstream
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * Requests that the file or directory denoted by this abstract pathname be
     * deleted when the virtual machine terminates. Files (or directories) are
     * deleted in the reverse order that they are registered. Invoking this
     * method to delete a file or directory that is already registered for
     * deletion has no effect. Deletion will be attempted only for normal
     * termination of the virtual machine, as defined by the Java Language
     * Specification. Once deletion has been requested, it is not possible to
     * cancel the request.
     *
     * @param path
     * @throws IOException
     */
    public static void deleteTreeOnExit(File path) throws IOException {
        for (File file : path.listFiles()) {
            if (file.isDirectory()) {
                deleteTreeOnExit(file);
            } else {
                Log.Debug(FileDirectoryHandler.class, "Delete On Exit: " + file.getCanonicalPath());
                file.deleteOnExit();
            }
        }
        path.deleteOnExit();
    }

    /**
     *
     * @param directory
     * @param identifier
     * @return A File array with the files (not directories) within the given
     * directory
     */
    @SuppressWarnings("unchecked")
    public static File[] getFilesOfDirectory(File directory) {
        File src;
        ArrayList<File> lstFiles = new ArrayList<File>();
        if (directory.isDirectory()) {
            try {
                src = directory;
                Log.Debug(FileDirectoryHandler.class, "Verzeichnis: " + src);
                File[] files = src.listFiles();
                Log.Debug(FileDirectoryHandler.class, "Dateien analysieren...");
                lstFiles = new ArrayList<java.io.File>();
                if (files != null && files.length > 0) {
                    for (int i = 0, k = 0; i < files.length; i++) {
//                Log.Debug(this,"Datei analysieren: " + files[i].getName());
                        if (files[i].isFile()) {
                            try {
                                lstFiles.add(files[i]);
                                Log.Debug(FileDirectoryHandler.class, "Datei gefunden: " + files[i].getName());
                                k++;
                            } catch (Exception ex) {
                                Log.Debug(FileDirectoryHandler.class, ex.getMessage());
                            }
                        }
                    }
                } else {
                    Log.Debug(FileDirectoryHandler.class, "Keine Datei gefunden.");
                }
            } catch (Exception exception) {
                Log.Debug(FileDirectoryHandler.class, exception);
                Log.Debug(FileDirectoryHandler.class, exception.getMessage());
            }
        }
        return lstFiles.toArray(new File[0]);
    }

    /**
     *
     * @param directory
     * @param identifier
     * @return A File array with the files (not directories) within the given
     * directory
     */
    @SuppressWarnings("unchecked")
    public static File[] getFilesOfDirectory(String directory, String identifier) {
        File src;
        ArrayList<File> lstFiles = null;
        try {
            lstFiles = new ArrayList<java.io.File>();
            src = new File(directory);
            Log.Debug(FileDirectoryHandler.class, "Verzeichnis: " + src);
            File[] files = src.listFiles();
            Log.Debug(FileDirectoryHandler.class, "Dateien analysieren...");
            lstFiles = new ArrayList<java.io.File>();
            if (files != null && files.length > 0) {
                for (int i = 0, k = 0; i < files.length; i++) {
//                Log.Debug(this,"Datei analysieren: " + files[i].getName());
                    if (files[i].isFile() && (identifier == null || files[i].toString().contains(identifier))) {
                        try {
                            lstFiles.add(files[i]);
                            Log.Debug(FileDirectoryHandler.class, "Datei gefunden: " + files[i].getName());
                            k++;
                        } catch (Exception ex) {
                            Log.Debug(FileDirectoryHandler.class, ex.getMessage());
                        }
                    }
                }
            } else {
                Log.Debug(FileDirectoryHandler.class, "Keine Datei gefunden.");
            }
        } catch (Exception exception) {
            Log.Debug(FileDirectoryHandler.class, exception);
            Log.Debug(FileDirectoryHandler.class, exception.getMessage());
        }
        return lstFiles.toArray(new File[0]);
    }

    /**
     * Open a file in default app or as "save as" dialog, depending on the
     * platform
     *
     * @param file
     */
    public static void open(File file) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            try {
                Desktop.getDesktop().open(file);
            } catch (Exception ex) {
                Log.Debug(FileDirectoryHandler.class, ex.getMessage());
                Popup.notice(Messages.FILE_OPEN_FAILED + file.getPath());
            }
        } else {
            new DialogForFile().saveFile(file);
        }
    }

    /**
     * Edit a file in default app or as "save as" dialog, depending on the
     * platform
     *
     * @param file
     */
    public static void edit(File file) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.EDIT)) {
            try {
                Desktop.getDesktop().edit(file);
            } catch (Exception ex) {
                Log.Debug(FileDirectoryHandler.class, ex.getMessage());
                Popup.notice(Messages.FILE_OPEN_FAILED + file.getPath());
            }
        } else {
            new DialogForFile(DialogForFile.FILES_ONLY).saveFile(file);
        }
    }

    public static void save(File file) {
        DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY, new File(file.getName()));
        d.saveFile(file);
    }

    /**
     * Copies a file to a temporary file. The resulting file is NOT linked to
     * the original one and even the original file is locked, the resulting file
     * is not
     *
     * @param file
     * @return The file with a randomly generated suffix
     */
    public static File tempFileClone(File file) {
        return tempFileClone(file, RandomText.getText(3));
    }

    /**
     * Copies a file to a temporary file. The resulting file is NOT linked to
     * the original one and even the original file is locked, the resulting file
     * is not
     *
     * @param file
     * @param suffix
     * @return
     */
    public static File tempFileClone(File file, String suffix) {
        try {
            suffix = check(suffix);
            cacheCheck();
            File fil = new File(copyFile(file, new File(LocalSettings.getProperty(LocalSettings.CACHE_DIR)), RandomText.getString()+ "." + suffix, true));
            fil.deleteOnExit();
            return fil;
        } catch (IOException ex) {
            Log.Debug(FileDirectoryHandler.class, ex);
        }
        return null;
    }

    /**
     * Returns a completely random named temporay file
     *
     * @param suffix no '.' please
     * @return
     */
    public static File getTempFile(String suffix) {
        suffix = check(suffix);
        return getTempFile(RandomText.getString(), suffix);
    }

    /**
     *
     * @return A temporary file with MP suffix (~mp)
     */
    public static File getTempFile() {
        return getTempFile("~yabs");
    }

    /**
     *
     * @param filename
     * @param suffix
     * @return A temporay file with the given name
     */
    public static File getTempFile(String filename, String suffix) {
        cacheCheck();
        filename = check(filename);
        suffix = check(suffix);
        File fil = new File(LocalSettings.getProperty(LocalSettings.CACHE_DIR) + File.separator + filename + "." + suffix);
        fil.deleteOnExit();
        return fil;
    }

    /**
     *
     * @return The temporary directory + File.separator
     */
    public static String getTempDir() {
        cacheCheck();
        return LocalSettings.getProperty(LocalSettings.CACHE_DIR) + File.separator;
    }

    /**
     *
     * @return The temporary directory
     */
    public static String getTempDir2() {
        cacheCheck();
        return LocalSettings.getProperty(LocalSettings.CACHE_DIR);
    }

    /**
     *
     * @return The temporary directory as File object
     */
    public static File getTempDirAsFile() {
        cacheCheck();
        return new File(LocalSettings.getProperty(LocalSettings.CACHE_DIR));
    }

    /**
     * Downloads a file
     *
     * @param address
     * @param localFileName
     * @return Marco Schmidt
     */
    public static File download(String address, String localFileName) {
        localFileName = check(localFileName);
        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;
        try {
            URL url = new URL(address);
            out = new BufferedOutputStream(
                    new FileOutputStream(localFileName));
            conn = url.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            long numWritten = 0;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
                numWritten += numRead;
            }
            Log.Debug(FileDirectoryHandler.class, localFileName + "\t" + numWritten);
        } catch (Exception exception) {
            Log.Debug(FileDirectoryHandler.class, exception);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
            }
        }
        File file = new File(localFileName);
//        file.deleteOnExit();
        return file;
    }

    /**
     * Downloads a file
     *
     * @param address
     * @return
     */
    public static File download(String address) {
        int lastSlashIndex = address.lastIndexOf('/');
        if (lastSlashIndex >= 0
                && lastSlashIndex < address.length() - 1) {
            return download(address, address.substring(lastSlashIndex + 1));
        } else {
            Log.Debug(FileDirectoryHandler.class, "Could not figure out local file name for "
                    + address);
        }
        return null;
    }

    /**
     * Copies a file via streaming
     *
     * @param sourceFile
     * @param outp
     * @param silent
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static URI copyFile(File sourceFile, File outp, boolean silent) throws FileNotFoundException, IOException {
        InputStream in = new FileInputStream(sourceFile);

        OutputStream out = new FileOutputStream(outp);

        // Copy the bits from instream to outstream
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();

        if (!silent) {
            Notificator.raiseNotification(Messages.FILE_SAVED + " " + outp.getPath(), false);
        }

        return outp.toURI();
    }

    /**
     * Copies a file via streaming
     *
     * @param sourceFile
     * @param outp
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static URI copyFile(File sourceFile, File outp) throws FileNotFoundException, IOException {
        return copyFile(sourceFile, outp, true);
    }

    private static void cacheCheck() {
        cacheCheck(true);
    }

    private static void cacheCheck(boolean tryagain) {
        File e = null;
        try {
            e = new File(LocalSettings.getProperty(LocalSettings.CACHE_DIR));
        } catch (Exception ex) {//avoid npe
            Log.Debug(ex);
            LocalSettings.setProperty(LocalSettings.CACHE_DIR, System.getProperty("java.io.tmpdir") + File.separator + Constants.FALLBACK_CACHE_DIR);
            e = new File(System.getProperty("java.io.tmpdir"), Constants.FALLBACK_CACHE_DIR);
        }
        if (!e.exists()) {
            e.mkdirs();
        }
        //Cannot access Cache dir?
        if (tryagain && (!e.isDirectory() || !e.canWrite() || e.listFiles() == null)) {
            LocalSettings.setProperty(LocalSettings.CACHE_DIR, System.getProperty("java.io.tmpdir") + File.separator + Constants.FALLBACK_CACHE_DIR);
            cacheCheck(false);
            try {
                FileDirectoryHandler.deleteTreeOnExit(getTempDirAsFile());
            } catch (IOException ex) {
                Log.Debug(ex);
            }
        } else if ((!e.isDirectory() || !e.canWrite() || e.listFiles() == null)) {
            throw new IllegalStateException("Cache directory not writeable: " + String.valueOf(e));
        }
    }

    private static synchronized String check(String filename) {
        return filename.replaceAll("[?:\\\\/*\\\"\\\"<>|]", "-").replace("..", ".");
    }

    /**
     * Unzips the file (if it is a zip file, returns the original file if it is
     * not a zip file). If the given file is a zip file containing multiple
     * files, returns a temporary directory containing the extracted files.
     *
     * @param file
     * @return
     */
    public static File unzipFile(File file) {
        if (file.getName().endsWith(".zip") || file.getName().endsWith(".jar")) {
            File c = getnewTemporaryDirectory();
            UnZip.deflate(file.getPath(), c.getPath());
            if (c.listFiles().length == 1) {
                File k = c.listFiles()[0];
                if (k.isFile()) {
                    return k;
                } else if (k.listFiles().length == 1) {
                    return (k.listFiles()[0].isFile()) ? k.listFiles()[0] : c;
                }
            }

            return c;
        } else {
            return file;
        }
    }

    /**
     * Creates a new, RW, temporary directory within the cache directory
     *
     * @return
     */
    public static File getnewTemporaryDirectory() {
        File f = getTempDirAsFile();
        File t = new File(f.getPath() + File.separator + RandomText.getText() + File.separator + RandomText.getText());
        t.mkdirs();
        t.getParentFile().setWritable(true);
        t.getParentFile().setReadable(true);
        return t;
    }

    /**
     * Deletes the content of a directory, omitting files which contain $omit in
     * their name No Subdirectories are deleted!
     *
     * @param path
     * @param omit
     * @throws IOException
     */
    public static void deleteDirectoryContent(File path, String... omit) throws IOException {
        for (File file : path.listFiles()) {
            List<String> o = Arrays.asList(omit);
            String name = file.getName();
            boolean rd = true;
            for (int i = 0; i < o.size(); i++) {
                String string = o.get(i);
                if (name.toLowerCase().contains(string.toLowerCase())) {
                    rd = false;
                }
            }
            if (rd) {
                file.delete();
            }
        }
    }

    /**
     * Deletes the content of a directory, omitting files which contain $omit in
     * their name ALL Subdirectories are deleted!
     *
     * @param path
     * @param omit
     * @throws IOException
     */
    public static void deleteDirectoryContent2(File path, String... omit) throws IOException {
        for (File file : path.listFiles()) {
            if (file.isDirectory()) {
                deleteDirectoryContent2(file, omit);
            }
            List<String> o = Arrays.asList(omit);
            String name = file.getName();
            boolean rd = true;
            for (int i = 0; i < o.size(); i++) {
                String string = o.get(i);
                if (name.toLowerCase().contains(string.toLowerCase())) {
                    rd = false;
                }
            }
            if (rd) {
                file.delete();
            }
        }
    }

    public static void writeTo(File f, Object data) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        try (OutputStream out = new FileOutputStream(f); 
                InputStream in = new ByteArrayInputStream(String.valueOf(data).getBytes("utf-8"))) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }
}
