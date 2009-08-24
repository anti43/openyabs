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
package mpv5.utils.csv;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.print.DocFlavor;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import mpv5.logging.Log;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileReaderWriter;
import mpv5.utils.jobs.Waitable;
import mpv5.utils.print.Printable;

/**
 * This is a helper class for reading and writing csv files
 * @author anti43
 */
public class CSVReaderWriter extends File implements Waitable, Printable {

    private static final long serialVersionUID = 2059941918698508985L;
    private FileReaderWriter rw;
    private String fieldSeparator = ";";
    private String[][] data;
    private String[] header;
    private int mode;
    private DefaultTableModel model;


    /**
     * Constructs a new temporary text file
     * @param data The data to write to the file
     * @param header 
     */
    public CSVReaderWriter(String[][] data, String[] header) {
        super(FileDirectoryHandler.getTempFile().getPath());
        rw = new FileReaderWriter(this);
        this.data = data;
        this.header = header;
        mode = 0;
        this.deleteOnExit();
    }

    /**
     * Constructs a new text file
     * @param data The data to write to the file
     * @param header 
     * @param name The name/path of the file
     */
    public CSVReaderWriter(String[][] data, String[] header, String name) {
        super(name);
        rw = new FileReaderWriter(this);
        this.data = data;
        this.header = header;
        mode = 0;
    }

    /**
     * Constructs a new file and sets 'read' mode
     * @param file The file (to read from)
     */
    public CSVReaderWriter(File file) {
        super(file.getPath());
        rw = new FileReaderWriter(this);
        mode = 1;
    }

     /**
     * Constructs a new file and sets 'read' mode
     * @param file The file (to read from)
     */
    public CSVReaderWriter(File file, String separator) {
        super(file.getPath());
        rw = new FileReaderWriter(this);
        fieldSeparator = separator;
        mode = 1;
    }


    /**
     * Write the file to disk
     */
    public void print() {

        if (header != null) {
            rw.writeLine(header, ";");
        }

        for (int i = 0; i < getData().length; i++) {
            String[] strings = getData()[i];
            String line = "";
            for (int j = 0; j < strings.length; j++) {
                String string = strings[j];
                line += string + getFieldSeparator();
            }
            line = (line.substring(0, line.length() - getFieldSeparator().length())).replaceAll("[\\r\\n]", "");
//            Log.Debug(this, line);
            rw.write("\""+line.replaceAll("\\\"", "'")+"\"");
        }
    }

    /**
     * Reads the file, no headers given
     * @return The data from file
     */
    public List<Object[]> read() {
        @SuppressWarnings("unchecked")
        ArrayList<Object[]> arr = new ArrayList();
        String[] line = rw.readLines();
        Log.Debug(this, "Reading file: " + this.getPath() + " (" + line.length + " lines)");
        for (int i = 0; i < line.length; i++) {
//            Log.PrintArray(line[i].split(getFieldSeparator()));
            arr.add(line[i].split(getFieldSeparator()));
            line = rw.readLines();
        }
        return arr;
    }

//    /**
//     * Reads the file, assuming the first line contains the header
//     * @return The data from file
//     */
//    public DefaultTableModel readToTable() {
//        @SuppressWarnings("unchecked")
//        ArrayList<String[]> arr = new ArrayList();
//        String[] line = rw.readLines();
//        Log.Debug(this, "Reading file: " + this.getPath() + " (" + line.length + " lines)");
//        header = line[0].split(getFieldSeparator());
//        for (int i = 1; i < line.length; i++) {
//            arr.add(line[i].split(getFieldSeparator()));
////            Log.Debug(this, "Line.. " + arr.get(i-1));
//        }
//        data = ListenDataUtils.listToStringArrayArray(arr);
//        model = new DefaultTableModel(data, header);
//        if(table != null) {
//            this.table.setModel(model);
//        }
//        return model;
//    }
    @Override
    public DocFlavor getFlavor() {
        return DocFlavor.CHAR_ARRAY.TEXT_PLAIN;
    }

    @Override
    public File getFile() {
        return this;
    }

    /**
     * 
     * @return the field separating char
     */
    public String getFieldSeparator() {
        return fieldSeparator;
    }

    /**
     * Set the field separating char (default: ';')
     * @param fieldSeparator
     */
    public void setFieldSeparator(String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    /**
     * 
     * @return The data
     */
    public String[][] getData() {
        return data;
    }

    /**
     * 
     * @param data Set the data of the file
     */
    public void setData(String[][] data) {
        this.data = data;
    }

    public String[] getHeader() {
        return header;
    }

    public void setHeader(String[] header) {
        this.header = header;
    }

    public DefaultTableModel getModel() {
        return model;
    }

    public Exception waitFor() {
        try {
            switch (mode) {
                case 0:
                    print();
                    break;
                case 1:
                    read();
                    break;
                case 2:
//                readToTable();
                    break;
            }
        } catch (Exception ex) {
            return ex;
        }
        return null;
    }
}
