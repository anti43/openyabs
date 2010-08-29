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
 *  This interface marks a class which is able to wait for a long running task to finish
 */
public interface Waiter {
    /**
     * Called by the {@link Job} after the {@link Waitable} has done its job
     * @param object The resulting object (maybe a file, or anything else) from the Waitable
     * @param exceptions
     * @throws Exception
     */
    public abstract void set(Object object, Exception exceptions) throws Exception;
}
