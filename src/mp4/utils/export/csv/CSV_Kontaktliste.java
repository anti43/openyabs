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
package mp4.utils.export.csv;

import mp4.interfaces.TableData;
import mp4.utils.datum.DateConverter;

/**
 *
 * @author anti43
 */
public class CSV_Kontaktliste implements TableData {

    public CSV_Kontaktliste(Class clazz) {
    }

    public CSV_Kontaktliste(Class clazz, Integer id) {
    }

    public Object[][] getData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getTitle() {
        return "Kontakte " + DateConverter.getTodayDefDate();
    }
}
