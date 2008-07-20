/*
 * 
 *  *  This file is part of MP by anti43 /GPL.
 *  *  
 *  *      MP is free software: you can redistribute it and/or modify
 *  *      it under the terms of the GNU General Public License as published by
 *  *      the Free Software Foundation, either version 3 of the License, or
 *  *      (at your option) any later version.
 *  *  
 *  *      MP is distributed in the hope that it will be useful,
 *  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 * *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *      GNU General Public License for more details.
 *  *  
 *  *      You should have received a copy of the GNU General Public License
 *  *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package mp4.klassen.objekte;

import mp4.datenbank.verbindung.Query;
import mp3.classes.layer.QueryClass;

/**
 *
 * @author anti43
 */
public class ChartData extends mp3.classes.layer.Things implements mp4.datenbank.struktur.Tabellen {


    public ChartData(Query query) {
        super(query.clone(TABLE_BILLS));
    }


    @Override
    public void save() {
    }

    public Object[][] fetchUmsatzInYears(String year) {

        Data l = new Data(QueryClass.instanceOf());

        Object[][] stri = new Object[12][2];

       
                    stri[0][0] = new String(l.fetchUmsatzInMonth2("01", year) + "€");
                    stri[0][1] = Double.valueOf(l.fetchUmsatzInMonth2("01", year));

                    stri[1][0] = new String(l.fetchUmsatzInMonth2("02", year) + "€");
                    stri[1][1] = Double.valueOf(l.fetchUmsatzInMonth2("02", year));
                    stri[2][0] = new String(l.fetchUmsatzInMonth2("03", year) + "€");
                    stri[2][1] = Double.valueOf(l.fetchUmsatzInMonth2("03", year));
                    stri[3][0] = new String(l.fetchUmsatzInMonth2("04", year) + "€");
                    stri[3][1] = Double.valueOf(l.fetchUmsatzInMonth2("04", year));
                    stri[4][0] = new String(l.fetchUmsatzInMonth2("05", year) + "€");
                    stri[4][1] = Double.valueOf(l.fetchUmsatzInMonth2("05", year));
                    stri[5][0] = new String(l.fetchUmsatzInMonth2("06", year) + "€");
                    stri[5][1] = Double.valueOf(l.fetchUmsatzInMonth2("06", year));
                    stri[6][0] = new String(l.fetchUmsatzInMonth2("07", year) + "€");
                    stri[6][1] = Double.valueOf(l.fetchUmsatzInMonth2("07", year));
                    stri[7][0] = new String(l.fetchUmsatzInMonth2("08", year) + "€");
                    stri[7][1] = Double.valueOf(l.fetchUmsatzInMonth2("08", year));
                    stri[8][0] = new String(l.fetchUmsatzInMonth2("09", year) + "€");
                    stri[8][1] = Double.valueOf(l.fetchUmsatzInMonth2("09", year));
                    stri[9][0] = new String(l.fetchUmsatzInMonth2("10", year) + "€");
                    stri[9][1] = Double.valueOf(l.fetchUmsatzInMonth2("10", year));
                    stri[10][0] = new String(l.fetchUmsatzInMonth2("11", year) + "€");
                    stri[10][1] = Double.valueOf(l.fetchUmsatzInMonth2("11", year));
                    stri[11][0] = new String(l.fetchUmsatzInMonth2("12", year) + "€");
                    stri[11][1] = Double.valueOf(l.fetchUmsatzInMonth2("12", year));
                   
         
        
        return stri;
    }
}
