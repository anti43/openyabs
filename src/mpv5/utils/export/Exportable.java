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
import java.util.HashMap;

/**
 *This interface specifies export specific methods
 */
public abstract class Exportable extends File implements Runnable {

    private File target;
    private HashMap<String, String> data;

    public Exportable(String pathToFile) {
        super(pathToFile);
    }

    /**
     * Define the target file
     * @param target
     */
    public void setTarget(File target) {
        this.target = target;
    }

    /**
     * @return the target
     */
    public File getTarget() {
        return target;
    }

    /**
     * 
     * @param data
     */
    public void setData(final HashMap<String, String> data) {
        this.data = data;
    }

    /**
     * @return the data
     */
    public HashMap<String, String> getData() {
        return data;
    }

}

