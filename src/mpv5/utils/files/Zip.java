package mpv5.utils.files;

/**
 *
 * @author unknown
 */
import java.util.zip.*;
import java.io.*;
import java.util.*;
import mpv5.logging.Log;

public class Zip {

    private static long size = 0;
    private static int numOfFiles = 0;
    private static ZipOutputStream cpZipOutputStream = null;
    private static String strSource = "";
    private static String strTarget = "";

    /**
     * 
     */
    public Zip() {
    }

    /**
     * 
     * @param source directory
     * @param target zipfile
     */
    public static void zip(String source, String target) {
        Zip.strSource = source;
        Zip.strTarget = target;

        try {
            File cpFile = new File(source);
            if (!cpFile.isFile() && !cpFile.isDirectory()) {
                Log.Debug(Zip.class, "\nSource file/directory Not Found!");
                return;
            }
            FileOutputStream fos = new FileOutputStream(target);
            cpZipOutputStream = new ZipOutputStream(fos);
            cpZipOutputStream.setLevel(9);
            zipFiles(cpFile);
            cpZipOutputStream.finish();
            cpZipOutputStream.close();
            Log.Debug(Zip.class, "\n Finished creating zip file " + target + " from source " + source);
            Log.Debug(Zip.class, "\n Total of  " + numOfFiles + " files are Zipped ");
            Log.Debug(Zip.class, "\n Total of  " + size + " bytes are Zipped  ");
        } catch (Exception e) {
            Log.Debug(Zip.class, e);
        }
    }

    private static void zipFiles(File cpFile) {

        int byteCount;
        final int DATA_BLOCK_SIZE = 2048;
        FileInputStream cpFileInputStream;


        if (cpFile.isDirectory()) {
            if (cpFile.getName().equalsIgnoreCase(".lck")) { //if directory name is .metadata, skip it.
                return;
            }
            File[] fList = cpFile.listFiles();
            for (int i = 0; i < fList.length; i++) {
                zipFiles(fList[i]);
            }
        } else {
            try {
                if (cpFile.getAbsolutePath().equalsIgnoreCase(strTarget)) {
                    return;
                }
                Log.Debug(Zip.class, "Zipping " + cpFile);
                size += cpFile.length();
                //String strAbsPath = cpFile.getAbsolutePath();
                numOfFiles++;
                String strAbsPath = cpFile.getPath();
                String strZipEntryName = strAbsPath.substring(strSource.length() + 1, strAbsPath.length());


//                byte[] b = new byte[ (int)(cpFile.length()) ];

                cpFileInputStream = new FileInputStream(cpFile);
                ZipEntry cpZipEntry = new ZipEntry(strZipEntryName);
                cpZipOutputStream.putNextEntry(cpZipEntry);

                byte[] b = new byte[DATA_BLOCK_SIZE];
                while ((byteCount = cpFileInputStream.read(b, 0, DATA_BLOCK_SIZE)) != -1) {
                    cpZipOutputStream.write(b, 0, byteCount);
                }

                //cpZipOutputStream.write(b, 0, (int)cpFile.length());
                cpZipOutputStream.closeEntry();
            } catch (Exception e) {
                Log.Debug(Zip.class, e);
            }
        }
    }

    /**
     * Extracts an entry of a zip file to a specified directory.
     * @param zipFile the zip file to extract from
     * @param zipEntry the entry of the zip file to extract
     * @param toDir the target directory
     * @throws java.io.IOException
     */
    public static void extract(ZipFile zipFile, ZipEntry zipEntry, File toDir) throws IOException {
        File file = new File(toDir, zipEntry.getName());
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            InputStream istr = zipFile.getInputStream(zipEntry);
            bis = new BufferedInputStream(istr);
            FileOutputStream fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);

            Streams s = new Streams();

            s.copy(bis, bos);
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
    }
}

class Streams {

    /** Creates a new instance of Streams */
    public Streams() {
    }

    /**
     * Reads the lines of an input stream.
     *
     * Is this good?
     * Does not close (any?) stream!
     * Return type may change to List<String> in future!?
     * @param input the input stream
     * @throws java.io.IOException
     * @return the lines read from the input stream
     */
    public String[] readLines(InputStream input) throws IOException {
        return readLines(new InputStreamReader(new BufferedInputStream(input)));
    }

    /**
     * Reads the lines from a Reader.
     *
     * Is this good?
     * Does not close (any?) stream!
     * Return type may change to List<String> in future!?
     * @param reader the reader
     * @throws java.io.IOException
     * @return the lines read from the reader
     */
    @SuppressWarnings(value = "unchecked")
    public String[] readLines(Reader reader) throws IOException {
        List lines = new ArrayList();
        String line;
        BufferedReader breader = new BufferedReader(reader);
        while ((line = breader.readLine()) != null) {
            lines.add(line);
        }
        return (String[]) lines.toArray(new String[lines.size()]);
    }

    /**
     * Copies bytes from an InputStream to an OutputStream.
     * @param in the InputStream
     * @param out the OutputStream
     * @throws java.io.IOException
     */
    public void copy(InputStream in, OutputStream out) throws IOException {
        int c;

        while ((c = in.read()) != -1) {
            out.write(c);
        }
    }
}
