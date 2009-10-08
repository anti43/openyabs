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
package mpv5.handler;

/**
 *  This interface represents simplified DatabaseObjects which do only contain getter/setter Methods
 */
public interface SimpleDatabaseObject {

    /**
     * Persist the DatabaseObject 
     * @return true if no error occoured
     * @throws Exception 
     */
    public boolean persist() throws Exception;
    public String getContext();
    public boolean fetch(int id) throws Exception;
    public boolean fetch(String cname) throws Exception;
}
