/*
 * 
 * 
 */
package mp4.items;

import java.util.ArrayList;

/**
 *
 * @author anti43
 */
public class KontenImporteur {

 
 

    private String Nummer = "0";
    private String Klasse = "";
    private String Gruppe = "";
    private String Art = "";
   

    public String[][] getData(KontenImporteur[] imp) {
    
        String[][] str = new String[imp.length][12];
        
        for (int i = 0; i < imp.length; i++) {

            str[i][0] = imp[i].getNummer();
            str[i][1] = imp[i].getKlasse();
            str[i][2] = imp[i].getGruppe();
            str[i][3] = imp[i].getArt();
    
        }

        return str;
    }

    public static KontenImporteur[] listToImporteurArray(ArrayList list) {

        KontenImporteur[] str = new KontenImporteur[list.size()];
        KontenImporteur imp = null;
        for (int i = 0; i < list.size(); i++) {

            imp = (KontenImporteur) list.get(i);
            
            if(imp.getNummer().equals(""))imp.setNummer(" ");
            if(imp.getKlasse().equals(""))imp.setKlasse("0");
            if(imp.getGruppe().equals(""))imp.setGruppe("0");
            if(imp.getArt().equals(""))imp.setArt(" ");
   
            str[i] = imp;
        }
        return str;
    }

    public String getArt() {
       return Art;
    }

   public String getGruppe() {
       return Gruppe;
    }

    public String getKlasse() {
       return Klasse;
    }

    public String getNummer() {
       return Nummer;
               
    }

    public void setArt(String string) {
       this.Art =string;
    }

    public void setGruppe(String string) {
       this.Gruppe =string;
    }

    public void setKlasse(String string) {
      this.Klasse =string;
    }

    public void setNummer(String string) {
     this.Nummer =string;
    }
 
}
 
