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
import mp4.utils.tabellen.DataModelUtils;

/**
 *
 * @author Andreas
 */
public class BillListTableModel extends MPTableModel {
    private static final long serialVersionUID = 5414150884755558696L;
    
    /**
     * 
     */
    public BillListTableModel(){
      super(new Class[]{java.lang.String.class, java.lang.String.class,
                    java.lang.String.class, java.lang.String.class, java.lang.String.class,
                    java.lang.Boolean.class,java.lang.Boolean.class
                }, new boolean[]{false,false,false,false,false,false,false},
                
                new Rechnung().getWithDependencies("rechnungen.id,rechnungnummer,datum,kundennummer,firma, bezahlt, storno"),
                new Object[]{"id","Nummer","Datum","Kunde","Firma", "Bezahlt", "Storniert"});
    
    }

}
 