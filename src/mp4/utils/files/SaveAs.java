/*
 * 
 *  *  This file is part of MP by anti43 /GPL.
 *  *  
 *  *      MP is free software: you can redistribute it and/or modify
 *  *      it under the terms of the GNU General Public License as published by
 *  *      the Free Software Foundation, either version 3 of the License, or
 *  *      (at your option) any later version.
 *  *  
 *  *      MP is distributed in the hope that it will be useful,
 *  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 * *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *      GNU General Public License for more details.
 *  *  
 *  *      You should have received a copy of the GNU General Public License
 *  *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package mp4.utils.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import mp4.globals.Constants;

/**
 *
 * @author anti43
 */
public class SaveAs {
    private File file3 = new File(Constants.USER_HOME+File.separator+"Desktop");

    public SaveAs(File file) throws FileNotFoundException, IOException {

        String wd = Constants.USER_HOME+File.separator+"Desktop";

        JFileChooser fc = new JFileChooser(new File(wd));
        
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int rc = fc.showDialog(null, "Speichern");


        if (rc == JFileChooser.APPROVE_OPTION) {
            File file2 = fc.getSelectedFile();

            if (file2.isDirectory()) {
                file3 = new File(file2 + File.separator + file.getName());
                if(file3.exists()) {
                    file3.delete();
                }
                file3 = new File(file2 + File.separator + file.getName());

                FileReader in = new FileReader(file);
                FileWriter out = new FileWriter(file3);
                int c;

                try {
                    while ((c = in.read()) != -1) {
                        out.write(c);
                    }
                    file.delete();
                } catch (IOException iOException) {
                } finally {


                    in.close();
                    out.close();
                }

            }

        } else {
//            System.out.println("File chooser cancel button clicked");
        }

    }

    public String getName() {
       return file3.getAbsolutePath();
    }
}
