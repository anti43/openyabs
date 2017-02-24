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
import java.util.HashMap;
import mpv5.db.objects.Template;
import mpv5.logging.Log;

/**
 *This class specifies export specific methods
 */
public abstract class Exportable extends File {

    private static final long serialVersionUID = 9214263835002096207L;

    private File target;
    private HashMap<String, Object> data;
    private Template template;

    public Exportable(String pathToFile) {
        super(pathToFile);
    }

    /**
     * Define the target file
     * @param target
     */
    public void setTarget(File target) {
        Log.Debug(System.identityHashCode(this), "target: " + target);
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
    public void setData(final HashMap<String, Object> data) {
        this.data = data;
    }

    /**
     * @return the data
     */
    public HashMap<String, Object> getData() {
        return data;
    }

    /**
     *
     * @param t
     */
    public void setTemplate(Template t) {
        template = t;
    }

    /**
     * @return the template
     */
    public Template getTemplate() {
        return template;
    }

    abstract void run();

    void reset() {
        data = null;
        template = null;
        target = null;
    }
}

