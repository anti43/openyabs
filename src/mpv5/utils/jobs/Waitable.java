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

package mpv5.utils.jobs;

/**
 *
 *  This interfaces marks a long running tasks which shall be run in background
 */
public interface Waitable {

    /**
     * The long running task shall be triggered by this method
     * null indicates no error
     * @return the return code (An <code>Exception</code> if an error has occured, or null if the task has finished successfully)
     */
    public abstract Exception waitFor();

}
