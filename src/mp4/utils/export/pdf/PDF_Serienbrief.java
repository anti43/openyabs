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
package mp4.utils.export.pdf;

import java.awt.Image;
import mp4.items.Customer;
import mp4.einstellungen.Einstellungen;



import java.util.ArrayList;
import java.util.Date;



import mp4.interfaces.Printable;
import mp3.classes.utils.Formater;
import mp4.utils.datum.DateConverter;
/**
 *
 * @author anti43
 */
public class PDF_Serienbrief implements Printable{


    private Customer kontakt;
    private String text = "";
    private String pretext;
    private Einstellungen l;
    private ArrayList fields = new ArrayList();
    private boolean named = false;
    private String template;

    /**
     * 
     * @param contact 
     * @param pretext 
     * @param text
     * @param named 
     */
    public PDF_Serienbrief(Customer contact, String pretext, String text, boolean named) {
            l = Einstellungen.instanceOf();
            kontakt = contact;
            this.text = text;
            this.pretext = pretext;
            this.template = l.getSerienbrieftemp();
            this.named = named;
    }



    @SuppressWarnings("unchecked")
    private String[][] buildFieldList(){

        fields.add(new String[]{"company", kontakt.getFirma()});
        fields.add(new String[]{"name", kontakt.getAnrede() + " " + kontakt.getVorname() + " " + kontakt.getName()});
        fields.add(new String[]{"street", kontakt.getStr()});
        fields.add(new String[]{"city", kontakt.getPLZ() + " " + kontakt.getOrt()});      
        fields.add(new String[]{"knumber", kontakt.getKundennummer()});
        fields.add(new String[]{"date", DateConverter.getDefDateString(new Date())});
        if (named) {
            fields.add(new String[]{"pretext", pretext + " " + kontakt.getAnrede() + " " + kontakt.getName()});
        } else {
            fields.add(new String[]{"pretext", pretext});
        }
        fields.add(new String[]{"text", text});
        
        return Formater.StringListToTableArray(fields);
    }

    public String getPath() {
       return PDFFile.getTempFilename();
    }

    public String[][] getFields() {
       return buildFieldList();
    }

    public Image getImage() {
       return null;
    }

    public String getTemplate() {
       return l.getSerienbrieftemp();
    }
}
