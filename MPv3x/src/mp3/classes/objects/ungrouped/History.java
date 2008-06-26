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

package mp3.classes.objects.ungrouped;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import mp3.database.util.Query;
import mp3.classes.interfaces.Structure;
import mp3.classes.layer.Popup;

/**
 *
 * @author anti43
 */
public class History extends mp3.classes.layer.Things implements mp3.classes.interfaces.Structure{


    private String aktion="";
    private String text="";
    private String datum="";
    private DateFormat df;
          
    public History(Query query) {
        super(query.clone(TABLE_HISTORY));
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");
        this.setDatum(format.format(new Date()));
    }
    /**
     * 
     * @param query
     * @param aktion
     * @param text
     */
    public History(Query query, String aktion, String text) {
        super(query.clone(TABLE_HISTORY));
        
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");
        
        this.setAktion(aktion);
        this.setText(text);
        this.setDatum(format.format(new Date()));
        
        this.save();
    }

    /**
     * 
     * @param query
     * @param id 
     */
    public History(Query query, String id) {
        super(query.clone(Structure.TABLE_HISTORY));
        this.id = Integer.valueOf(id);
        this.explode(this.selectLast("*", "id", id, true));
        
       
    }


    private void explode(String[] select) {
        
        this.setAktion(select[1]);
        this.setText(select[2]);
        this.setDatum(select[3]);  
        
    }

        private String collect() {
        String str = "";
        str = str + "(;;2#4#1#1#8#0#;;)" +this.getAktion() + "(;;2#4#1#1#8#0#;;)"  + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)"  + this.getText()  + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)"  + this.getDatum() + "(;;2#4#1#1#8#0#;;)" ;
        return str;
    }
    
    public void save() {

        if (id > 0) {
            this.update(TABLE_HISTORY_FIELDS, this.collect(), id.toString());
            isSaved = true;
        } else if (id == 0) {
            this.insert(TABLE_HISTORY_FIELDS, this.collect());
        } else {

            mp3.classes.layer.Popup.warn(java.util.ResourceBundle.getBundle("languages/Bundle").getString("no_data_to_save"), Popup.WARN);

        }
    }

    public String getAktion() {
        return aktion;
    }

    public void setAktion(String aktion) {
        this.aktion = aktion;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String[][] getHistory() {   

        String[][] str = this.select("aktion,text,datum", null, NOTNULL, false);
        
        int i = 0, j = str.length - 1;
		while (i < j) {
			String[] h = str[i]; str[i] = str[j]; str[j] = h;
			i++;
			j--;
		}
        
        return str;
    }


}