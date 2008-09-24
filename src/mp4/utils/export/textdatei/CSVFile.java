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

package mp4.utils.export.textdatei;

import java.io.File;
import javax.print.DocFlavor;
import mp4.interfaces.Printable;
import mp4.interfaces.Waitable;
import mp4.utils.files.FileReaderWriter;

/**
 *
 * @author anti43
 */
public class CSVFile extends File implements Waitable, Printable  {
    private FileReaderWriter rw;
    private String fieldSeparator = ",";
    private String[][] data;

    public CSVFile(String name, String[][] data){
        super(name + ".csv");
        rw = new FileReaderWriter(this);
        this.data = data;
    }
    
    
    public void waitFor() {
        for (int i = 0; i < data.length; i++) {
            String[] strings = data[i];
            String line = "";
            for (int j = 0; j < strings.length; j++) {
                String string = strings[j];
                line += string + fieldSeparator;
            }
            rw.write(line.substring(line.length()-1, line.length()));
        }
    }

    public DocFlavor getFlavor() {
        return DocFlavor.CHAR_ARRAY.TEXT_PLAIN;
    }

    public File getFile() {
       return this;
    }

}
