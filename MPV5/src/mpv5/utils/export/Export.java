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
package mpv5.utils.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import mpv5.db.common.NodataFoundException;

/**
 * This is a dummy class for exporting data to template documents
 * @author anti
 */
public class Export extends HashMap<String, String> {

    private static final long serialVersionUID = 1L;
    private File file;

    /**
     * Add the data to be exported, as key - value pairs
     * @param data 
     */
    public void setData(String[][] data) {
        for (int i = 0; i < data.length; i++) {
            String[] strings = data[i];
            super.put(strings[0], strings[1]);
        }
    }

    /**
     *  Set the file to be filled
     * @param <T>
     * @param pdfFile
     */
    public <T extends File> void setFile(T pdfFile) {
        this.file = pdfFile;
    }

    /**
     * Exports the given data to a newly created temp file using the given template file
     * @return true if no error was encountered during processing the data
     * @throws NodataFoundException If no data has been previously added
     * @throws FileNotFoundException If no file was given as template
     */
    public boolean processData() throws NodataFoundException, FileNotFoundException {
        if (this.isEmpty()) {
            throw new NodataFoundException();
        }
        if (!this.file.exists()) {
            throw new FileNotFoundException(file.getPath());
        }
        return false;
    }

    /**
     * After processing the data, the created file is a temporary file
     * @return The created file or NULL if no file has been created yet.
     */
    public File getFile() {
        return null;
    }
}
