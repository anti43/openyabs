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
import mp4.items.Rechnung;
import mp4.items.Customer;

import java.util.ArrayList;
import java.util.Date;

import java.util.Locale;
import mp4.interfaces.Printable;
import mp4.utils.ui.inputfields.InputVerifiers;
import mp4.einstellungen.Einstellungen;
import mp4.einstellungen.Programmdaten;
import mp4.utils.datum.DateConverter;
import mp4.utils.listen.ListenDataUtils;
import mp4.utils.zahlen.FormatNumber;

/**
 *
 * @author anti43
 */
public class PDF_Mahnung implements Printable{

    private Einstellungen l;
    private Rechnung rechnung;
    private Customer k;
    private Object[][] products;
    private Double netto = 0d;
    private Double brutto = 0d;
    private ArrayList fields = new ArrayList();
    private Double betrag;
    private int nummer;
    private String text;



    /**
     * 
     * @param bill
     * @param text 
     * @param betrag
     * @param nummer 
     */
    public PDF_Mahnung(Rechnung bill, String text,Double betrag, int nummer) {
        l = Einstellungen.instanceOf();
        this.rechnung = bill;
        k = new Customer(bill.getKundenId());
        products = rechnung.getProductlistAsArray();
        this.betrag = betrag;
        this.nummer = nummer;
        this.text = text;
 
        Locale.setDefault(Einstellungen.instanceOf().getLocale());
    }


    @SuppressWarnings("unchecked")
    private String[][] buildFieldList(){
        
        fields.add(new String[]{"company", k.getFirma()});
        fields.add(new String[]{"name", k.getAnrede() + " " + k.getVorname() + " " + k.getName()});
        fields.add(new String[]{"street", k.getStr()});
        fields.add(new String[]{"city", k.getPLZ() + " " + k.getOrt()});
        fields.add(new String[]{"originaldate", rechnung.getFDatum()});
        fields.add(new String[]{"date", DateConverter.getDefDateString(new Date())});
        fields.add(new String[]{"rnumber", rechnung.getRechnungnummer()});
        fields.add(new String[]{"number", String.valueOf(nummer)});
        fields.add(new String[]{"knumber", k.getKundennummer()});
        fields.add(new String[]{"text", text});

//id,Anzahl,Posten,Mehrwertsteuer,Nettopreis,Bruttopreis
        for (int i = 0; i < products.length; i++) {
            try {
                netto = netto + ((Double) products[i][4] * (Double) products[i][1]);
                brutto = brutto + ((Double) products[i][5] * (Double) products[i][1]);
            } catch (Exception exception) {
            }
        }

        fields.add(new String[]{"originalprice", FormatNumber.formatLokalCurrency(brutto)});
        fields.add(new String[]{"arrearsfee", FormatNumber.formatLokalCurrency(betrag)});
        fields.add(new String[]{"totalprice", FormatNumber.formatLokalCurrency(brutto)});

        return ListenDataUtils.StringListToTableArray(fields);
    }

    public String getPath() {
        return rechnung.getRechnungnummer().replaceAll(" ", "_") + "_" + k.getFirma().replaceAll(" ", "_") + k.getName().replaceAll(" ", "_").trim();
    }

    public String[][] getFields() {
        return buildFieldList();
    }

    public Image getImage() {
        return null;
    }

    public String getTemplate() {
       return l.getRechnungtemp();
    }
}
