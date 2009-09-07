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
package mpv5.utils.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import javax.swing.SwingUtilities;
import mpv5.db.common.NodataFoundException;
import mpv5.logging.Log;
import mpv5.utils.jobs.Waitable;

/**
 * The Export class handles the export of data using templatefiles to PDF
 *  
 */
public class Export extends HashMap<String, Object> implements Waitable {

    private static final long serialVersionUID = 1L;
    private Exportable file;
//    private Thread t;
    private File toFile;
    private String targetName;

    /**
     * Add the data, must be key - value pairs
     * @param data 
     */
    public void addData(String[][] data) {
        for (int i = 0; i < data.length; i++) {
            String[] strings = data[i];
            super.put(strings[0], strings[1]);
        }
    }

    /**
     *  Set the file to be filled
     * @param <T>
     * @param templateFile 
     */
    public <T extends Exportable> void setTemplate(T templateFile) {
        this.file = templateFile;
    }

    /**
     * Exports the given data to the target file using the given template file.
     * <br/>
     * @param toFile The target file. If the file exists, will be overwritten if possible.
     * @throws NodataFoundException If no data has been previously added
     * @throws FileNotFoundException If no file was given as template
     */
    public void processData(File toFile) throws NodataFoundException, FileNotFoundException {
        if (this.isEmpty()) {
            throw new NodataFoundException();
        }

        if (file == null) {
            throw new FileNotFoundException("You must call setFile(Exportable file) first!");
        } else if (!this.file.exists()) {
            throw new FileNotFoundException(file.getPath());
        }

        if (toFile != null) {
            if (toFile.exists()) {
                toFile.delete();
                Log.Debug(this, "File exists, will be replaced: " + toFile);
            }

            toFile.getParentFile().mkdirs();
            file.setTarget(toFile);
        }
        file.setData(this);

        try {
            SwingUtilities.invokeAndWait(file);// we need to wait for OO to perform the task
        } catch (Exception e) {
            Log.Debug(e);
        }
    }

    public Exception waitFor() {
        try {
            processData(getTargetFile());
        } catch (Exception ex) {
            return ex;
        }
        return null;
    }

    /**
     * The target file. If the file exists, will be overwritten if possible.
     * @param toFile the toFile to set
     */
    public void setTargetFile(File toFile) {
        this.toFile = toFile;
    }

    /**
     * @return the toFile
     */
    public File getTargetFile() {
        return toFile;
    }

}
