/*
 *  This file is part of MP.
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
package mpv5.utils.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import mpv5.db.common.NodataFoundException;
import mpv5.logging.Log;

/**
 * The Export class handles the export of data using templatefiles
 *  
 */
public class Export extends HashMap<String, String> {

    private static final long serialVersionUID = 1L;
    private Exportable file;
    private Thread t;

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
    public <T extends Exportable> void setFile(T templateFile) {
        this.file = templateFile;
    }

    /**
     * Exports the given data to the target file using the given template file
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

        if (toFile!=null) {
            if (toFile.exists()) {
                toFile.delete();
                Log.Debug(this, "File exists, will be replaced: " + toFile);
            }

            toFile.mkdirs();
            file.setTarget(toFile);
        }
        file.setData(this);

        try {
          t = new Thread(file);
                    t.start();
        } catch (Exception e) {
            Log.Debug(e);
        }
    }

}
