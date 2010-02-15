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
package mpv5.db.objects;

import java.io.File;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.table.TableModel;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.logging.Log;
import mpv5.utils.export.Exportable;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.images.MPIcon;
import mpv5.utils.text.RandomText;

/**
 * This class represents Yabs Templates
 *  
 */
public class Template extends DatabaseObject {


    /**
     * Indicates no printer association
     */
    public static final String PRINTER_UNDEFINED = "printer_undefined";

  
    private String description = "";
    private String filename = "";
    private String printer = "";
    private int intsize;
    private String mimetype;
    private File file;
    private String format = DEFAULT_FORMAT;
    private Exportable exFile;
    /**
     * Represents the default column order
     */
    public static String DEFAULT_FORMAT = "1,2,3,4,5,6,7,8,9,10,11";

    public Template() {
        context = Context.getTemplate();
    }

    @Override
    public String __getCName() {
        return cname;
    }

    @Override
    public JComponent getView() {
        return null;
    }

    @Override
    public void setCName(String name) {
        cname = name;
    }

    /**
     * @return the description
     */
    public String __getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the mimetype
     */
    public String __getMimetype() {
        return mimetype;
    }

    /**
     * @param mimetype the mimetype to set
     */
    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    /**
     * @return the intsize
     */
    public int __getIntsize() {
        return intsize;
    }

    /**
     * @param intsize the intsize to set
     */
    public void setIntsize(int intsize) {
        this.intsize = intsize;
    }
    MPIcon icon;

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        if (icon == null) {
            try {
                Log.Debug(this, "Determining Icon for " + __getCName());
                icon = new MPIcon(MPIcon.DIRECTORY_DEFAULT_ICONS + __getCName().substring(__getCName().lastIndexOf(".") + 1, __getCName().length()) + ".png");
                return icon;
            } catch (Exception e) {
                Log.Debug(this, "Icon file not existing in " + MPIcon.DIRECTORY_DEFAULT_ICONS);
                try {
                    JFileChooser chooser = new JFileChooser();
                    icon = new MPIcon(chooser.getIcon(new File(filename)));
                    return icon;
                } catch (Exception ez) {
                    Log.Debug(this, ez);
                    icon = new MPIcon(MPIcon.DIRECTORY_DEFAULT_ICONS + "folder_tar.png");
                    return icon;
                }
            }
        } else {
            return icon;
        }
    }

    /**
     * Fetches the physical file from db
     * @return
     */
    public synchronized File getFile() {
        if (file == null) {
            try {
                file = QueryHandler.instanceOf().clone(Context.getFiles()).
                        retrieveFile(filename,
                        new File(FileDirectoryHandler.getTempDir() + "~" + RandomText.getText() + "_" + cname));
                file.deleteOnExit();
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
        return file;
    }

    /**
     * @return the filename
     */
    public String __getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public boolean delete() {
        QueryCriteria c = new QueryCriteria();
        c.addAndCondition("filename", filename);
        QueryHandler.instanceOf().clone(Context.getFiles()).delete(c);
        return super.delete();
    }

    /**
     * @return the format
     */
    public String __getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }

   

    /**
     * @return the exFile
     */
    public Exportable getExFile() {
        return exFile;
    }

    /**
     * Injects a table resource
     * @param key
     * @param model
     */
    public void injectTable(String key, TableModel model) {
        getTables().put(key, model);
    }
    private HashMap<String, TableModel> tables = new HashMap<String, TableModel>();

    /**
     * @return the tables
     */
    public HashMap<String, TableModel> getTables() {
        return tables;
    }

    /**
     * @return the printer
     */
    public String __getPrinter() {
        return printer;
    }

    /**
     * @param printer the printer to set
     */
    public void setPrinter(String printer) {
        this.printer = printer;
    }

    public void defineExFile(Exportable ex){
       this.exFile = ex;
    }
}
