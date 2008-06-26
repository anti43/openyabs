/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mp3.classes.utils;

/**
 *
 * @author Galileo Computing
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DirectoryHandler {

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

   
      public static void copyDirectory(File sourceLocation , File targetLocation)
    throws IOException {
        
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            
            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
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

    }
    



