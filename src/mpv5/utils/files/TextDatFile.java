/*
 *  This file is part of YaBS.
 *  
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *  
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *  
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.DocFlavor;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import mpv5.db.common.DatabaseObject;
import mpv5.logging.Log;

import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.jobs.Waitable;

/**
 * This is a helper class for reading and writing csv files
 * 
 */
public class TextDatFile extends File implements Waitable {

    private static final long serialVersionUID = 2059941918698508985L;
    private FileReaderWriter rw;
    private String fieldSeparator = ";";
    private String[][] data;
    private String[] header;
    private int mode;
    private DefaultTableModel model;
    private JTable table;

    /**
     * Constructs a new text file
     */
    public TextDatFile() {
        super(FileDirectoryHandler.getTempFile().getPath());
        rw = new FileReaderWriter(this);
    }

    /**
     * Constructs a new temporary text file
     * @param data The data to write to the file
     * @param header 
     */
    public TextDatFile(String[][] data, String[] header) {
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
    public TextDatFile(String[][] data, String[] header, String name) {
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
    public TextDatFile(File file) {
        super(file.getPath());
        rw = new FileReaderWriter(this);
        mode = 1;
    }

    /**
     * Constructs a new file and sets 'read' mode
     * @param file The file (to read from)
     * @param table The table to retrieve the data
     */
    public TextDatFile(File file, JTable table) {
        super(file.getPath());
        rw = new FileReaderWriter(this);
        this.table = table;
        mode = 2;
    }

    public void parse(ArrayList<DatabaseObject> dbobjarr) {
        String[] headers;
        Method[] headerx;
           if (dbobjarr.size() > 0) {
                    headerx = dbobjarr.get(0).getVars().values().toArray(new Method[]{});
                    headers = new String[headerx.length];
                    for (int i = 0; i < headerx.length; i++) {
                        Method method = headerx[i];
                        headers[i] = method.getName().substring(5);
                    }
                    data = new String[dbobjarr.size()][dbobjarr.get(0).getVars().size()];
                    for (int i = 0; i < dbobjarr.size(); i++) {
                        DatabaseObject databaseObject = dbobjarr.get(i);
                        List<String[]> temp = databaseObject.getValues();

                        for (int j = 0; j < temp.size(); j++) {
                            String[] strings = temp.get(j);
                            data[i][j] = strings[1];
                        }
                    }

                   setData(data);
                   setHeader(headers);
                }
    }

    @Override
    public Exception waitFor() {

            switch (mode) {
                case 0:
                    print();
                    break;
                case 1:
                    read();
                    break;
                case 2:
                    readToTable();
                    break;
            }

        return null;
    }

    /**
     * Write the file to disk
     */
    public void print() {

        if (header != null) {
            rw.writeLine(header, getFieldSeparator());
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
            rw.write(line);
        }
    }

    /**
     * Reads the file, no headers given
     * @return The data from file
     */
    public String[][] read() {
        @SuppressWarnings("unchecked")
        ArrayList<String[]> arr = new ArrayList();
        String[] line = rw.readLines();
        Log.Debug(this, "Reading file: " + this.getPath() + " (" + line.length + " lines)");
        for (int i = 0; i < line.length; i++) {
            arr.add(line[i].split(getFieldSeparator()));
            line = rw.readLines();
//            Log.Debug(this, "Line.. " + i);
        }
        data = ArrayUtilities.listToStringArrayArray(arr);
        model = new DefaultTableModel(data, header);
        return data;
    }

    /**
     * Reads the file, assuming the first line contains the header
     * @return The data from file
     */
    public DefaultTableModel readToTable() {
        @SuppressWarnings("unchecked")
        ArrayList<String[]> arr = new ArrayList();
        String[] line = rw.readLines();
        Log.Debug(this, "Reading file: " + this.getPath() + " (" + line.length + " lines)");
        header = line[0].split(getFieldSeparator());
        for (int i = 1; i < line.length; i++) {
            arr.add(line[i].split(getFieldSeparator()));
//            Log.Debug(this, "Line.. " + arr.get(i-1));
        }
        data = ArrayUtilities.listToStringArrayArray(arr);
        model = new DefaultTableModel(data, header);
        if (table != null) {
            this.table.setModel(model);
        }
        return model;
    }

    public DocFlavor getFlavor() {
        return DocFlavor.CHAR_ARRAY.TEXT_PLAIN;
    }

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

    /**
     * Create a new file
     * @param filename
     * @return
     */
    public File createFile(String filename) {
        File f = FileDirectoryHandler.getTempFile(filename, "csv");
        try {
            print();
            FileDirectoryHandler.copyFile(this, f);
        } catch (FileNotFoundException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(TextDatFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(TextDatFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return f;
    }
}
