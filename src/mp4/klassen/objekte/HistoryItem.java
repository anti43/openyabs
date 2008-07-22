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


import mp4.einstellungen.Einstellungen;
import java.util.Date;
import mp3.classes.interfaces.Strings;
import mp4.datenbank.verbindung.Query;

import mp3.classes.layer.Popup;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp3.classes.visual.main.mainframe;
import mp4.utils.datum.DateConverter;
import mp4.utils.tabellen.DataOrder;

/**
 *
 * @author anti43
 */
public class HistoryItem extends mp3.classes.layer.Things implements mp4.datenbank.struktur.Tabellen{

    

    private String aktion = "";
    private String text = "";
    private Date datum = new Date();
    private User user = Einstellungen.instanceOf().getUser();
           public Integer id = 0;
    

   
    public Integer getId() {
        return id;
    }
    public void destroy() {
        this.delete(this.id);
        this.id = 0;
    }  
    public HistoryItem(Query query) {
        super(query.clone(TABLE_HISTORY));
        
        this.setDatum(new Date());
    }
    
    /**
     * 
     * @param aktion 
     * @param text
     * @param user
     */
    public HistoryItem(String aktion, String text, User user) {
        super(ConnectionHandler.instanceOf().clone(TABLE_HISTORY));
      
        this.setAktion(aktion);
        this.setText(text);
        this.setDatum(new Date());
        this.setUser(user);
        
        this.save();
    }
    
    
    /**
     * 
     * @param query
     * @param aktion
     * @param text
     */
    public HistoryItem(Query query, String aktion, String text) {
        super(query.clone(TABLE_HISTORY));
        
                
        this.setAktion(aktion);
        this.setText(text);
        this.setDatum(new Date());
        
        this.save();
    }

    /**
     * 
     * @param query
     * @param id 
     */
    public HistoryItem(Query query, String id) {
        super(query.clone(TABLE_HISTORY));
        this.id = Integer.valueOf(id);
        this.explode(this.selectLast("*", "id", id, true));
        
       
    }

    public HistoryItem(String aktion, String text) {
       super(ConnectionHandler.instanceOf().clone(TABLE_HISTORY));

        this.setAktion(aktion);
        this.setText(text);
        this.setDatum(new Date());
        
        this.save();
        
        mainframe.nachricht.setText(text);
    }


    private void explode(String[] select) {
        
        this.setAktion(select[1]);
        this.setText(select[2]);
        this.setDatum(DateConverter.getDate(select[3]));  
        
    }

        private String collect() {
        String str = "";
        str = str + "(;;2#4#1#1#8#0#;;)" +this.getAktion() + "(;;2#4#1#1#8#0#;;)"  + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)"  + this.getText()  + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)"  + DateConverter.getSQLDateString(this.getDatum()) + "(;;2#4#1#1#8#0#;;)"  + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)"  + this.getUser()  + "(;;2#4#1#1#8#0#;;)";
        return str;
    }
    
    public void save() {

        if (id > 0) {
            this.update(TABLE_HISTORY_FIELDS, this.collect(), id.toString());
            isSaved = true;
        } else if (id == 0) {
            this.insert(TABLE_HISTORY_FIELDS, this.collect());
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

    public Date getDatum() {
        return datum;
    }

    public void setDatum(Date datum) {
        this.datum = datum;
    }

    private String getUser() {
        return user.toString();
    }
    
    private void setUser(User user) {
       this.user = user;
    }
    
    public String[][] getHistory() {   

        String[][] str = this.select("aktion,text,datum,benutzer", null, Strings.NOTNULL, false);
     
        str = DataOrder.reverseArray(str);
        
        return str;
    }
}