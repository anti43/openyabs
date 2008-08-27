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

package mp4.utils.tabellen.models;

import mp4.items.Rechnung;
import mp4.utils.datum.DateConverter;

/**
 *
 * @author Andreas
 */
public class BillSearchListTableModel extends MPTableModel {
    private static final long serialVersionUID = 5414150884755558696L;
    
    /**
     * 
     */
    public BillSearchListTableModel(){
      super(new Class[]{java.lang.String.class, java.lang.String.class,
                    java.lang.String.class
                }, new boolean[]{false,false,false},
                new Rechnung().select("id, rechnungnummer, datum ", "datum", DateConverter.getTodayDefMonth(), "datum,id DESC ", true),
                new Object[]{"id", "Nummer","Datum"});
    }


    public BillSearchListTableModel(String key, String searchtext) {
         super(new Class[]{java.lang.String.class, java.lang.String.class,
                    java.lang.String.class
                }, new boolean[]{false,false,false},
                new Rechnung().select("rechnungen.id, rechnungen.rechnungnummer, rechnungen.datum ", key, searchtext, "rechnungen.datum,rechnungen.id  DESC ", true, "kunden", "kundenid"),
                new Object[]{"id", "Nummer","Datum"});
    }
}
 