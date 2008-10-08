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
import java.util.ArrayList;
import javax.print.DocFlavor;
import mp4.globals.Constants;
import mp4.interfaces.Printable;
import mp4.interfaces.Waitable;
import mp4.logs.Log;
import mp4.utils.files.FileDirectoryHandler;
import mp4.utils.files.FileReaderWriter;
import mp4.utils.listen.ListenDataUtils;

/**
 *
 * @author anti43
 */
public class TextDatFile extends File implements Waitable, Printable {

    private FileReaderWriter rw;
    private String fieldSeparator = Constants.FELDTRENNER;
    private String[][] data;
    private int mode;

    public TextDatFile(String[][] data) {
        super(FileDirectoryHandler.getTempFile().getPath());
        rw = new FileReaderWriter(this);
        this.data = data;
        mode = 0;
        this.deleteOnExit();
    }

    public TextDatFile(String[][] data, String name) {
        super(name);
        rw = new FileReaderWriter(this);
        this.data = data;
        mode = 0;
    }

    public TextDatFile(File file) {
        super(file.getPath());
        rw = new FileReaderWriter(this);
        mode = 1;
    }

    public void waitFor() {
        switch (mode) {
            case 0:
                print();
                break;
            case 1:
                read();
        }
    }

    public void print() {

        for (int i = 0; i < getData().length; i++) {
            String[] strings = getData()[i];
            String line = "";
            for (int j = 0; j < strings.length; j++) {
                String string = strings[j];
                line += string + getFieldSeparator();
            }  
            line = (line.substring(0, line.length() - getFieldSeparator().length())).replaceAll("[\\r\\n]","");
;
            Log.Debug(line);
            rw.write(line);
        }
    }

    public String[][] read() {
        @SuppressWarnings("unchecked")
        ArrayList<String[]> arr = new ArrayList();
        String[] line = rw.readLines();
        for (int i = 0; i > line.length; i++) {
            arr.add(line[i].split(getFieldSeparator()));
            line = rw.readLines();
            Log.Debug(line);
        }
        data = ListenDataUtils.listToStringArrayArray(arr);
        return data;
    }

    public DocFlavor getFlavor() {
        return DocFlavor.CHAR_ARRAY.TEXT_PLAIN;
    }

    public File getFile() {
        return this;
    }

    public String getFieldSeparator() {
        return fieldSeparator;
    }

    public void setFieldSeparator(String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    public String[][] getData() {
        return data;
    }

    public void setData(String[][] data) {
        this.data = data;
    }
}
