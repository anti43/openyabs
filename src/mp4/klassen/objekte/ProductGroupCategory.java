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

import mp4.datenbank.verbindung.Query;


 /**
 * @author anti43         
 */
public class ProductGroupCategory extends mp3.classes.layer.Things implements mp4.datenbank.struktur.Tabellen {

    private String kategorienummer = "0";
    private String name = "";
      
    private String[][] data;
    private Query query;
   public Integer id = 0;
    public Integer getId() {
        return id;
    }
    public void destroy() {
        this.delete(this.id);
        this.id = 0;
    }
    public ProductGroupCategory(Query query) {
        super(query.clone(TABLE_PRODUCTS_GROUPS_CATEGORIES));

    }
    
    public ProductGroupCategory(Query query, String id) {
        super(query.clone(TABLE_PRODUCTS_GROUPS_CATEGORIES));
        
        this.id=Integer.valueOf(id);
        this.explode(this.selectLast("*", "id", id, true , true, false));
        this.query=query;
    }

    

    private void explode(String[] data) {
         this.id=Integer.valueOf(data[0]);
        this.setKategorienummer(data[1]);
        this.setName(data[2]);
        
    }


    private String collect() {
        String str = "";
        str = str +"(;;2#4#1#1#8#0#;;)"+getKategorienummer()+"(;;2#4#1#1#8#0#;;)"+"(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)"+getName()+"(;;2#4#1#1#8#0#;;)";
    
        return str;
    }

    public void save() {

        if (id > 0) {
            this.update(TABLE_PRODUCTS_GROUPS_CATEGORIES_FIELDS, this.collect(), id.toString());
            isSaved = true;
        } else if (id == 0) {
            if(this.getKategorienummer().equals("0")){
            
                this.setKategorienummer(this.getNextIndex("kategorienummer").toString());
            }
            id = this.insert(TABLE_PRODUCTS_GROUPS_CATEGORIES_FIELDS, this.collect());
        } else {

        }
    }

    public String getKategorienummer() {
        return kategorienummer;
    }

    public void setKategorienummer(String kategorienummer) {
        this.kategorienummer = kategorienummer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     * @return
     */
    public Integer getID(){
    
       return this.id;
    }

    @Override
  public String toString(){
  return this.name;
  
  }
  
}
