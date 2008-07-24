/*
 * 
This file is part of MP by anti43 /GPL.

MP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with MP.  If not, see <http://www.gnu.org/licenses/>.

 * 
 */
package mp4.panels;

import javax.swing.Icon;

/**
 *
 * @author anti43
 */
public interface panelInterface {

    /**
     *
     * To be called after a database change in the panels view scope
     */
    public abstract void updateTables();

    /**
     *
     * To be called to save the current data 
     */
    public abstract void save();

    /**
     *
     * To be called on closing the tab
     */
    public abstract void close();

    /**
     *
     * To be called for Undo action
     */
    public abstract void undo();

    /**
     *
     * To be called for Redo action
     */
    public abstract void redo();

    /**
     *
     * To change the tab icon
     */
    public abstract void changeTabIcon(Icon icon);

    /**
     *
     * To check whether any changes have been done to the Tabs initial data
     */
    public abstract boolean isEdited();
}
