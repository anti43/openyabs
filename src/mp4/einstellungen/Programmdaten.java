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
package mp4.einstellungen;

import mp3.classes.layer.QueryClass;

/**
 *
 * @author Andreas
 */
public class Programmdaten implements mp4.datenbank.struktur.Tabellen {
    
    private boolean BILLPANEL_CHECKBOX_MITLIEFERSCHEIN = false;
    
    
    
    private DataHandler handler;
    private static Programmdaten dat;

    public static Programmdaten instanceOf() {
        if (dat == null) {
            dat = new Programmdaten();
            return dat;
        }
        return dat;
    }
    

    public Programmdaten() {
       handler = new DataHandler(TABLE_PROG_DATA);

    }



    public boolean isBILLPANEL_CHECKBOX_MITLIEFERSCHEIN() {       
        return handler.getBoolean("BILLPANELCHECKBOXMITLIEFERSCHEIN");
    }

    public void setBILLPANEL_CHECKBOX_MITLIEFERSCHEIN(boolean BILLPANEL_CHECKBOX_MITLIEFERSCHEIN) {
        handler.setBoolean("BILLPANELCHECKBOXMITLIEFERSCHEIN",BILLPANEL_CHECKBOX_MITLIEFERSCHEIN);
    }

}
