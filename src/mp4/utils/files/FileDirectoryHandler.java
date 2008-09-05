/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mp4.utils.files;

/**
 *
 * @author Galileo Computing
 */
import mp4.logs.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class FileDirectoryHandler {

    public static void deleteTree(File path) throws IOException {
        for (File file : path.listFiles()) {
            if (file.isDirectory()) {
                deleteTree(file);
            } else {
                Log.Debug("Delete: " + file.getCanonicalPath(), true);
                file.delete();
            }
        }
        path.delete();
    }

    /**
     * 
     * @param sourceLocation
     * @param targetLocation
     * @throws java.io.IOException
     */
    public static void copyDirectory(File sourceLocation, File targetLocation)
            throws IOException {

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
      
    }

    public static URI copyFile(File sourceFile, File targetDirectory, String targetFilename)
            throws IOException {

            InputStream in = new FileInputStream(sourceFile);
            File outp = new File(targetDirectory + File.separator + targetFilename);
            OutputStream out = new FileOutputStream(targetDirectory + File.separator + targetFilename);

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
    



