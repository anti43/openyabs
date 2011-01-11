/*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.html;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import mpv5.logging.Log;
import mpv5.utils.date.DateConverter;
import mpv5.utils.files.FileDirectoryHandler;

/**
 *
 * This class writes a HTML file with a
 * table containing the data of the given data model
 * (DefaultTableModel or String[][] representation)
 *
 * 
 */
public class TableHtmlWriter {

    private Object[][] model = null;
    private String[] header = null;
    private File file = FileDirectoryHandler.getTempFile();
    private String prefix = null;

    /**
     * This constructor initializes the HTMLWriter with the
     * given datamodel and the given output file.
     * The table header will be set to header
     * and the header of the created file will be the prefix
     *
     * @param model A Default Table Model
     * @param file A file to write the output
     * @param header The table header {column1, coloumn2,...}
     * @param prefix The page header
     */
    public TableHtmlWriter(DefaultTableModel model, File file, String[] header, String prefix) {
        Object[][] obj = new Object[model.getRowCount()][model.getColumnCount()];

        for (int k = 0; k < obj.length; k++) {


            for (int l = 0; l < obj[k].length; l++) {

                obj[k][l] = model.getValueAt(k, l);
            }

        }

        this.model = obj;
        this.file = file;
        this.header = header;
        this.prefix = prefix;

    }

    /**
     * This constructor initializes the HTMLWriter with the
     * given datamodel and the given output file.
     *
     * @param model A Default Table Model
     * @param file A file to write the output
     */
    public TableHtmlWriter(DefaultTableModel model, File file) {

        Object[][] obj = new Object[model.getRowCount()][model.getColumnCount()];
        String[] head = new String[model.getColumnCount()];

        if (obj.length > 0) {
            for (int l = 0; l < obj[0].length; l++) {
                head[l] = model.getColumnName(l);
            }
        }
        for (int k = 0; k < obj.length; k++) {
            for (int l = 0; l < obj[k].length; l++) {
                obj[k][l] = model.getValueAt(k, l);
            }
        }
        this.model = obj;
        this.file = file;
        this.header = head;
    }

    /**
     * This constructor initializes the HTMLWriter with the
     * given datamodel and the given output file.
     *
     * @param model A 2-dimensional array presenting the table-data.
     * @param file A file to write the output
     */
    public TableHtmlWriter(Object[][] model, File file) {
        this.model = model;
        this.file = file;
    }

    /**
     * This constructor initializes the HTMLWriter with the
     * given datamodel and the given output file.
     * The table header will be set to header
     * and the header of the created file will be the prefix
     *
     * @param model A Default Table Model
     * @param file A file to write the output
     * @param header The table header {column1, coloumn2,...}
     * @param prefix The page header
     */
    public TableHtmlWriter(Object[][] model, File file, String[] header, String prefix) {
        this.model = model;
        this.file = file;
        this.header = header;
        this.prefix = prefix;

    }

    /**
     * This constructor initializes the HTMLWriter with the
     * given datamodel.
     *
     * @param model A Default Table Model
     */
    public TableHtmlWriter(DefaultTableModel model) {


        Object[][] obj = new Object[model.getRowCount()][model.getColumnCount()];


        for (int k = 0; k < obj.length; k++) {


            for (int l = 0; l < obj[k].length; l++) {

                obj[k][l] = model.getValueAt(k, l);
            }

        }

        this.model = obj;

    }

    /**
     * This constructor initializes the HTMLWriter with the
     * given datamodel and the given output file.
     *
     * @param model A 2-dimensional array presenting the table-data
     */
    public TableHtmlWriter(Object[][] model) {
        this.model = model;

    }

    /**
     * This method creates the HTML file
     *
     * @param border The border thickness of the created HTML table
     * @param bordercolor The bordercolor of the created HTML table
     * @return The HTML file
     */
    public File createHtml(Integer border, Color bordercolor) {

        if (getModel() != null) {
            try {
                write(border, bordercolor);
            } catch (IOException ex) {
                mpv5.logging.Log.Debug(ex);//Logger.getLogger(TableHtmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Log.Print("No datamodel given.");
        }


        return getFile();
    }

    /**
     * This method creates the HTML file with standard values
     *
     * @return The created HTML file
     */
    public File createHtml() {


        if (getModel() != null) {
            try {
                write(0, Color.BLACK);
            } catch (IOException ex) {
                mpv5.logging.Log.Debug(ex);//Logger.getLogger(TableHtmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Log.Print("No datamodel given.");
        }

        return getFile();
    }

    private void write(Integer border, Color borderc) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(getFile()));
        String rgb = Integer.toHexString(borderc.getRGB());
        rgb = rgb.substring(2, rgb.length());
        out.write(" <meta http-equiv='Content-Type' content='text/html;'> <HTML>");
        out.write("<head><STYLE TYPE='text/css'><!--TD{font-family: Arial; font-size: 10pt;}" +
                "table {border: 1px solid #666666;border-collapse: collapse;}" +
                "td { border: 1px solid #666666; }" +
                "th { border: 1px solid #666666; }" +
                "-->" +
                "</STYLE></head>");
        if (getPrefix() != null) {
            out.write("<P><H2>" + getPrefix() + "</H2></P><BR>");
        }
        out.write("<TABLE style='empty-cells:show' WIDTH = 100%   CELLPADDING=1px CELLSPACING=0px BORDERCOLOR = #" + rgb + ">\n");
        if (getHeader() != null) {
            out.write("<THEAD>\n<TR VALIGN=TOP>\n");
            for (int k = 0; k < getHeader().length; k++) {
                out.write("<TH WIDTH=*> <P>" + getHeader()[k] + "</P></TH>\n");
            }
            out.write("</TR></THEAD>\n");
        }
        out.write("     <TBODY>\n");
        for (int k = 0; k < getModel().length; k++) {
            out.write("<TR VALIGN=TOP>\n");
            for (int l = 0; l < getModel()[k].length; l++) {
                out.write("<TD WIDTH=*><P>");
                if (getModel()[k][l] != null && !String.valueOf(getModel()[k][l]).equals("null")) {
                    out.write(String.valueOf(getModel()[k][l]).replaceAll("<html>|<pre>| ", "") + " ");
                }
                out.write("</P></TD>\n");
            }
            out.write("</TR>\n");
        }
        out.write("</TBODY></TABLE>\n");
        out.write("<BR><BR>");
        out.write(DateConverter.getDefDateString(new Date()));
        out.write("</HTML>");
        out.close();
    }

    /**
     *
     * @return The datamodel
     */
    public Object[][] getModel() {
        return model;
    }

    /**
     *
     * @param model The data model
     */
    public void setModel(Object[][] model) {
        this.model = model;
    }

    /**
     *
     * @return The tableheader
     */
    public String[] getHeader() {
        return header;
    }

    /**
     *
     * @param header The table header
     */
    public void setHeader(String[] header) {
        this.header = header;
    }

    /**
     *
     * @return The output file
     */
    public File getFile() {
        return file;
    }

    /**
     * The output file
     *
     * @param file
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     *
     * @return The page header
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     *
     * @param prefix The page header
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
