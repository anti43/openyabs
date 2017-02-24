/*
 * 
 *  *  This file is part of YaBS.
 *  *  
 *  *      YaBS is free software: you can redistribute it and/or modify
 *  *      it under the terms of the GNU General Public License as published by
 *  *      the Free Software Foundation, either version 3 of the License, or
 *  *      (at your option) any later version.
 *  *  
 *  *      YaBS is distributed in the hope that it will be useful,
 *  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 * *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *      GNU General Public License for more details.
 *  *  
 *  *      You should have received a copy of the GNU General Public License
 *  *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package mpv5.utils.files;

import fr.opensagres.xdocreport.utils.StringEscapeUtils;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import mpv5.logging.Log;
import mpv5.utils.date.DateConverter;

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
    private Color borderColor = Color.black;
    private boolean showHorizontalLines = true;
    private boolean showVerticalLines = true;

    /**
     * This constructor initializes the HTMLWriter with the
     * given datamodel and the given output file.
     * The table header will be set to header
     * and the header of the created file will be the prefix
     * @param model
     * @param file
     * @param showHorizontalLines
     * @param showVerticalLines
     */
    public TableHtmlWriter(JTable model, File file, boolean showHorizontalLines, boolean showVerticalLines) {
        Object[][] obj = new Object[model.getRowCount()][model.getColumnCount()-1];

        for (int k = 0; k < obj.length; k++) {
            for (int l = 1; l < obj[k].length; l++) {
                if (model.getValueAt(k, l) instanceof Date) {
                    obj[k][l - 1] = DateConverter.getDefDateString((Date) model.getValueAt(k, l));
                } else {
                    obj[k][l - 1] = model.getValueAt(k, l);
                }
            }
        }
        String[] head = new String[model.getColumnCount()-1];
        for (int i = 1; i < head.length; i++) {
            head[i - 1] = model.getColumnName(i);
        }
        this.header = head;
        this.model = obj;
        this.file = file;
        this.showHorizontalLines = showHorizontalLines;
        this.showVerticalLines = showVerticalLines;

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
        for (int k = 0; k < obj.length; k++) {
            for (int l = 0; l < obj[k].length; l++) {
                obj[k][l] = model.getValueAt(k, l);
            }
        }
        this.model = obj;
        this.file = file;
        String[] head = new String[model.getColumnCount()];
        for (int i = 0; i < head.length; i++) {
            head[i] = model.getColumnName(i);
        }
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
     * given datamodele.
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
        String[] head = new String[model.getColumnCount()];
        for (int i = 0; i < head.length; i++) {
            head[i] = model.getColumnName(i);
        }
        this.header = head;
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

        String bo = "";
        if (!showHorizontalLines || !showVerticalLines) {
            if (!showHorizontalLines && !showVerticalLines) {
                border = 0;
            } else if (showHorizontalLines) {
                bo = "RULES=COLS";
            } else if (showHorizontalLines) {
                bo = " RULES=ROWS";
            }
        }


        BufferedWriter out = new BufferedWriter(new FileWriter(getFile()));
        String rgb = Integer.toHexString(borderc.getRGB());
        rgb = rgb.substring(2, rgb.length());
        out.write(" <meta http-equiv='Content-Type' content='text/html;'> <HTML>");
        out.write("<head><STYLE TYPE='text/css'><!--TD{font-family: Arial; font-size: 10pt;}"
                + "table {border: " + border
                + "px solid #" + rgb + ";border-collapse: collapse;}"
                + "td { border: " + border + "px solid #" + rgb + "; }"
                + "th { border: " + border + "px solid #" + rgb + "; }"
                + "-->"
                + "</STYLE></head>");
        if (getPrefix() != null) {
            out.write("<P><H2>" + getPrefix() + "</H2></P><BR>");
        }
        out.write("<TABLE style='empty-cells:show' WIDTH = 100%   CELLPADDING=1px CELLSPACING=0px BORDERCOLOR = #" + rgb + " " + bo + ">\n");
        if (getHeader() != null) {
            out.write("<THEAD>\n<TR VALIGN=TOP>\n");
            for (int k = 0; k < getHeader().length; k++) {
                out.write("<TH WIDTH=*>	<P>" + getHeader()[k] + "</P></TH>\n");
            }
            out.write("</TR></THEAD>\n");
        }
        out.write("	<TBODY>\n");
        for (int k = 0; k < getModel().length; k++) {
            out.write("<TR VALIGN=TOP>\n");
            for (int l = 0; l < getModel()[k].length; l++) {
                out.write("<TD WIDTH=*><P>");
                if (getModel()[k][l] != null && !String.valueOf(getModel()[k][l]).equals("null")) {
                    out.write(StringEscapeUtils.escapeHtml(String.valueOf(getModel()[k][l])).replaceAll("<html>|<pre>|&nbsp;", "") + "&nbsp;");
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

    /**
     * @return the showHorizontalLines
     */
    public boolean isShowHorizontalLines() {
        return showHorizontalLines;
    }

    /**
     * @param showHorizontalLines the showHorizontalLines to set
     */
    public void setShowHorizontalLines(boolean showHorizontalLines) {
        this.showHorizontalLines = showHorizontalLines;
    }

    /**
     * @return the showVerticalLines
     */
    public boolean isShowVerticalLines() {
        return showVerticalLines;
    }

    /**
     * @param showVerticalLines the showVerticalLines to set
     */
    public void setShowVerticalLines(boolean showVerticalLines) {
        this.showVerticalLines = showVerticalLines;
    }

    /**
     * @return the borderColor
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * @param borderColor the borderColor to set
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

///////////////////////////////////// xhtml-writer start
    /**
     * This method creates the HTML file
     *
     * @param border The border thickness of the created HTML table
     * @param bordercolor The bordercolor of the created HTML table
     * @return The HTML file
     */
    public File createHtml2(Integer border, Color bordercolor) {

        if (getModel() != null) {
            try {
                write2(border, bordercolor);
            } catch (IOException ex) {
                mpv5.logging.Log.Debug(ex);//Logger.getLogger(TableHtmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Log.Print("No datamodel given.");
        }

        return getFile();
    }

    private void write2(Integer border, Color borderc) throws IOException {

        String bo = "";
        if (!showHorizontalLines || !showVerticalLines) {
            if (!showHorizontalLines && !showVerticalLines) {
                border = 0;
            } else if (showHorizontalLines) {
                bo = "RULES=COLS";
            } else if (showHorizontalLines) {
                bo = " RULES=ROWS";
            }
        }


        BufferedWriter out = new BufferedWriter(new FileWriter(getFile()));
        String rgb = Integer.toHexString(borderc.getRGB());
        rgb = rgb.substring(2, rgb.length());
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        out.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n<meta http-equiv='Content-Type' content='text/html;charset=UTF-8' />\n<title />\n");
        out.write("<style type='text/css'>\ntd{font-family: Arial; font-size:10pt;}\n"
                + "table {border:" + border + "px solid #" + rgb + "; border-collapse:collapse;}\n"
                + "td {border:" + border + "px solid #" + rgb + "; padding:7pt 5pt;}\n"
                + "th {border:" + border + "px solid #" + rgb + "; padding:10pt;}\n"
                + "h2 {margin:10pt 0 20pt;}"
                + "</style>\n</head><body>\n");
        if (getPrefix() != null) {
            out.write("<h2>" + getPrefix() + "</h2>\n");
        }
        out.write("<table style='empty-cells:show;border-color:#" + rgb + " " + bo
                + ";' \nwidth = '100%' cellpadding = '1' cellspacing = '0'>\n");
        if (getHeader() != null) {
            out.write("<thead>\n<tr valign='top'>\n");
            for (int k = 0; k < getHeader().length; k++) {
                out.write("<th>" + getHeader()[k] + "</th>\n");
            }
            out.write("</tr></thead>\n");
        }
        out.write("<tbody>\n");
        for (int k = 0; k < getModel().length; k++) {
            out.write("<tr valign = 'top'>\n");
            for (int l = 0; l < getModel()[k].length; l++) {
                out.write("<td>");
                if (getModel()[k][l] != null && !String.valueOf(getModel()[k][l]).equals("null")) {
                    out.write(String.valueOf(getModel()[k][l]).replaceAll("<html>|<pre>|&nbsp;", "") + "&nbsp;");
                }
                out.write("</td>\n");
            }
            out.write("</tr>\n");
        }
        out.write("</tbody></table>\n");
        out.write("<p style='margin-top:20pt;font-size:small;'>");
        out.write(DateConverter.getDefDateString(new Date()));
        out.write("</p>\n</body></html>");
        out.close();
        Log.Debug(this, getFile());
    }
//////////////////////////////// xhtml-writer end
}
