/*
 * 
 * 
 */
package mpv5.utils.files;

/**
 *
 * @author Galileo Computing
 */
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import mpv5.logging.Log;
import mpv5.utils.text.RandomText;

public class FileDirectoryHandler {

    /**
     * Deletes a file now or later
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
     * @param path
     * @throws java.io.IOException
     */
    public static void deleteTree(File path) throws IOException {
        for (File file : path.listFiles()) {
            if (file.isDirectory()) {
                deleteTree(file);
            } else {
                Log.Debug(FileDirectoryHandler.class, "Delete: " + file.getCanonicalPath(), true);
                file.delete();
            }
        }
        path.delete();
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
     * @param sourceFile
     * @param targetDirectory
     * @param targetFilename
     * @return
     * @throws java.io.IOException
     */
    public static URI copyFile(File sourceFile, File targetDirectory, String targetFilename)
            throws IOException {
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

        return outp.toURI();
    }

    public static void deleteTreeOnExit(File path) throws IOException {
        for (File file : path.listFiles()) {
            if (file.isDirectory()) {
                deleteTreeOnExit(file);
            } else {
                Log.Debug(FileDirectoryHandler.class, "Delete On Exit: " + file.getCanonicalPath(), true);
                file.deleteOnExit();
            }
        }
        path.deleteOnExit();
    }

    /**
     *
     * @param directory
     * @param identifier
     * @return A File array with the files (not directories) within the given directory
     */
    @SuppressWarnings("unchecked")
    public static File[] getFilesOfDirectory(String directory, String identifier) {
        File src;
        try {
            lstFiles = new ArrayList<java.io.File>();
            src = new File(directory);
            Log.Debug(FileDirectoryHandler.class, "Verzeichnis: " + src, true);
            File[] files = src.listFiles();
            Log.Debug(FileDirectoryHandler.class, "Dateien analysieren...", true);
            lstFiles = new ArrayList<java.io.File>();

            for (int i = 0, k = 0; i < files.length; i++) {
//                Log.Debug(this,"Datei analysieren: " + files[i].getName());
                if (files[i].isFile() && files[i].toString().contains(identifier)) {
                    try {
//                        String[] fileinfo = new String[3];
//                        fileinfo[0] = String.valueOf(k);
//                        fileinfo[1] = files[i].getName();
//                        fileinfo[2] = files[i].getCanonicalPath();
                        lstFiles.add(files[i]);
                        Log.Debug(FileDirectoryHandler.class, "Datei gefunden: " + files[i].getName(), true);
                        k++;
                    } catch (Exception ex) {
                        Log.Debug(FileDirectoryHandler.class, ex.getMessage(), true);
                    }
                }
            }
            if (files.length == 0) {
//                String[] fileinfo = new String[3];
//                fileinfo [2] = "Keine Datei vorhanden";
//                lstFiles.add(fileinfo);
                Log.Debug(FileDirectoryHandler.class, "Keine Datei gefunden.");
            }
        } catch (Exception exception) {
            Log.Debug(FileDirectoryHandler.class, exception);
            Log.Debug(FileDirectoryHandler.class, exception.getMessage(), true);
        }
        return lstFiles.toArray(new File[0]);
    }
    private static ArrayList<File> lstFiles;

    /**
     * Copies a file to a temporary file. The resulting file is NOT linked
     * to the original one and even the original file is locked,
     * the resulting file is not
     * @param file
     * @return The file with a randomly generated suffix
     */
    public static File tempFileClone(File file) {
        return tempFileClone(file, new RandomText(3).getString());
    }

    /**
     * Copies a file to a temporary file. The resulting file is NOT linked
     * to the original one and even the original file is locked,
     * the resulting file is not
     * @param file
     * @param suffix
     * @return
     */
    public static File tempFileClone(File file, String suffix) {
        try {
            File fil = new File(copyFile(file, new File(System.getProperty("java.io.tmpdir")), new RandomText().getString() + "." + suffix));
            fil.deleteOnExit();
            return fil;
        } catch (IOException ex) {
            Log.Debug(FileDirectoryHandler.class, ex);
        }
        return null;
    }

    /**
     * Returns a completely random named temporay file
     * @param suffix
     * @return
     */
    public static File getTempFile(String suffix) {
        File fil = new File(System.getProperty("java.io.tmpdir") + File.separator + new RandomText(8).getString() + "." + suffix);
        fil.deleteOnExit();
        return fil;
    }

    /**
     *
     * @return A temporary file with MP suffix (~mp)
     */
    public static File getTempFile() {
        return getTempFile("~mp");
    }

    /**
     * Downloads a file
     * @param address
     * @param localFileName
     * @return
     * @author Marco Schmidt
     */
    public static File download(String address, String localFileName) {
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
     * @param address
     * @return
     */
    public static File download(String address) {
        int lastSlashIndex = address.lastIndexOf('/');
        if (lastSlashIndex >= 0 &&
                lastSlashIndex < address.length() - 1) {
            return download(address, address.substring(lastSlashIndex + 1));
        } else {
            Log.Debug(FileDirectoryHandler.class, "Could not figure out local file name for " +
                    address);
        }
        return null;
    }

    /**
     * Copies a file via streaming
     * @param sourceFile
     * @param outp
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static URI copyFile(File sourceFile, File outp) throws FileNotFoundException, IOException {
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

        return outp.toURI();
    }
}
    



