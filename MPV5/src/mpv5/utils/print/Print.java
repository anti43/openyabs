/*
 *  This file is part of MP by anti43 /GPL.
 *
 *  MP is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */

package mpv5.utils.print;

import java.util.ArrayList;
import mpv5.db.common.DatabaseObject;
import mpv5.utils.xml.XMLWriter;

/**
 *
 * @author Administrator
 */
public class Print {
    private XMLWriter xmlw;
    private DatabaseObject dbobj;

    public Print(DatabaseObject dataOwner) {
       this.dbobj = dataOwner;
    }

    public void print() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void toCSV() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void toVCF() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void toXML() {
       xmlw = new XMLWriter();
       xmlw.newDoc(dbobj.getDbID());
        
       ArrayList<String[]> data = dbobj.getValues();

        for (int i = 0; i < data.size(); i++) {
            xmlw.addElement(data.get(i)[0], data.get(i)[1]);
        }

       xmlw.createFile();

    }

}
