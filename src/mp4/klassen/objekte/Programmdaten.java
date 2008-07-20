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
package mp4.klassen.objekte;

import mp3.classes.layer.QueryClass;

/**
 *
 * @author Andreas
 */
public class Programmdaten extends mp3.classes.layer.Things implements mp4.datenbank.struktur.Tabellen {

    private int id = 0;
    
    private boolean BILLPANEL_CHECKBOX_MITLIEFERSCHEIN = false;
    
    private static Programmdaten dat;

    public static Programmdaten instanceOf() {
        if (dat == null) {
            dat = new Programmdaten();
            return dat;
        }
        return dat;
    }

    public Programmdaten() {
        super(QueryClass.instanceOf().clone(TABLE_PROG_DATA));

        this.id = 1;

    }

 
    public void save(String key, String value) {
//         this.update("wert", "(;;2#4#1#1#8#0#;;)" + orig_valuearray[i][1] + "(;;2#4#1#1#8#0#;;)", String.valueOf(i + 1));

    }

    public boolean isBILLPANEL_CHECKBOX_MITLIEFERSCHEIN() {  
        
        return getBoolean("BILLPANELCHECKBOXMITLIEFERSCHEIN");
    }

    public void setBILLPANEL_CHECKBOX_MITLIEFERSCHEIN(boolean BILLPANEL_CHECKBOX_MITLIEFERSCHEIN) {
        this.BILLPANEL_CHECKBOX_MITLIEFERSCHEIN = BILLPANEL_CHECKBOX_MITLIEFERSCHEIN;
    }

    private boolean getBoolean(String string) {
        String[][] values = this.select("wert", "name", string, false);
        if(Integer.valueOf(values[0][0]).intValue() == 1 ) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    public void save() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

 

}
