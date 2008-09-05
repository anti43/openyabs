/*
 *  This file is part of MP by anti43 /GPL.
 *  
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *  
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *  
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mp4.utils.files;

/**
 *
 * @author anti43
 */
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileReaderWriter {

    private String file;
    private BufferedReader datenstrom;
    private char[] charArray;
    private File filer;
    private FileWriter fw;
    private BufferedWriter bw;

    /**
     * A reader/writer helper class for textfiles
     * @param file
     */
    public FileReaderWriter(String file) {
        this.file = file;
    }

    /**
     * 
     * @param text
     * @return true is writing successful
     */
    public boolean write (String text) {
        try {

            filer = new File(file);
            
            if(filer.exists()) {
                filer.delete();
            }
            
            fw = new FileWriter(filer);
            bw = new BufferedWriter(fw);
            bw.write(text);
            bw.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return false;
        }


        return true;

    }

    /**
     * 
     * @return a string with text of file
     */
    public String read() {
        try {
            int charArrayLaenge;


            //Wie heist die Datei?
            File dateiHandle = new File(file);

            //Wie lange ist diese Datei?
            long datenStromLaenge = dateiHandle.length() + 1;


            charArrayLaenge = (int) datenStromLaenge - 1;
            charArray = new char[charArrayLaenge];


            try {


                FileReader dateiInhalt = new FileReader(dateiHandle);


                datenstrom = new BufferedReader(dateiInhalt);


                datenstrom.read(charArray, 0, charArrayLaenge);
            } catch (IOException e) {
                System.out.println(e.toString());
            }

            datenstrom.close();

        } catch (IOException ex) {
            Logger.getLogger(FileReaderWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new String(charArray);
    }
}
