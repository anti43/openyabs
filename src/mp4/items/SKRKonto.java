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

package mp4.items;

import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.globals.Strings;
import mp4.datenbank.verbindung.Query;


/**
 *
 * @author anti43
 */
public class SKRKonto extends mp4.items.Things implements mp4.datenbank.installation.Tabellen{


    private String Nummer = "0";
    private String Klasse = "";
    private String Gruppe = "";
    private String Art = "";
    public Integer id = 0;

    public SKRKonto(String nummer) {
       super(ConnectionHandler.instanceOf().clone(TABLE_KONTEN));
       this.explode(this.selectLast("*", "nummer", nummer, false));
    }
   
    public Integer getId() {
        return id;
    }
    public void destroy() {
        this.delete(this.id);
        this.id = 0;
    }
    public SKRKonto() {
       super(ConnectionHandler.instanceOf().clone(TABLE_KONTEN));
    }
          
    public SKRKonto(Query query) {
        super(query.clone(TABLE_KONTEN));
       
    }

    public SKRKonto(ConnectionHandler query, String string, boolean bool) {
        super(query.clone(TABLE_KONTEN));
        this.explode(this.selectLast("*", "nummer", string, false));
    }
 
    /**
     * 
     * @param Nummer
     * @param Klasse
     * @param Gruppe
     * @param Art
     */
    public SKRKonto(String Nummer, String Klasse, String Gruppe, String Art) {
        super(ConnectionHandler.instanceOf().clone(TABLE_KONTEN));
        
           
        this.setNummer(Nummer);
        this.setKlasse(Klasse);
        this.setGruppe(Gruppe);
        this.setArt(Art);
        
        this.save();
    }

    /**
     * 
     * @param id 
     */
    public SKRKonto(Integer id) {
        super(ConnectionHandler.instanceOf().clone(TABLE_KONTEN));
        this.id = Integer.valueOf(id);
        this.explode(this.selectLast("*", "id", id.toString(), true));
    }
    
     public SKRKonto(Query q, Integer id) {
        super(q.clone(TABLE_KONTEN));
        this.id = Integer.valueOf(id);
        this.explode(this.selectLast("*", "id", id.toString(), true));
    }


    private void explode(String[] select) {
       
            this.id = Integer.valueOf(select[0]);
            this.setNummer(select[1]);
            this.setKlasse(select[2]);
            this.setGruppe(select[3]);
            this.setArt(select[4]);

    }

        private String collect() {
        String str = "";
        str = str + "(;;2#4#1#1#8#0#;;)" +this.getNummer() + "(;;2#4#1#1#8#0#;;)"  + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)"  + this.getKlasse()  + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)"  + this.getGruppe()  + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)"  + this.getArt() + "(;;2#4#1#1#8#0#;;)" ;
        return str;
    }
    
    public void save() {

        if (id > 0) {
            this.update(TABLE_KONTEN_FIELDS, this.collect(), id.toString());
            isSaved = true;
        } else if (id == 0) {
            this.insert(TABLE_KONTEN_FIELDS, this.collect(),null);
        } 
    }

    public String[][] getAll() {

        Query q = ConnectionHandler.instanceOf().clone(TABLE_KONTEN);

        String[][] prods = q.select(Strings.ALL, null,"nummer",false);

        return prods;
    }
    
    public String getNummer() {
        return Nummer;
    }

    public void setNummer(String Nummer) {
        this.Nummer = Nummer;
    }

    public String getKlasse() {
        return Klasse;
    }

    public void setKlasse(String Klasse) {
        this.Klasse = Klasse;
    }

    public String getGruppe() {
        return Gruppe;
    }

    public void setGruppe(String Gruppe) {
        this.Gruppe = Gruppe;
    }

    public String getArt() {
        return Art;
    }

    public void setArt(String Art) {
        this.Art = Art;
    }






}