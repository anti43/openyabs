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
package handling.pdf;


import mp4.klassen.objekte.Rechnung;

import mp4.klassen.objekte.Customer;


import com.lowagie.text.DocumentException;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Date;
import java.util.HashMap;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import mp3.Main;
import mp3.classes.interfaces.Strings;
import mp3.classes.layer.Popup;
import mp3.classes.layer.QueryClass;
import mp3.classes.utils.Formater;
import mp3.classes.utils.Log;
import mp4.einstellungen.Einstellungen;

/**
 *
 * @author anti43
 */
public class PDF_Mahnung {

    private AcroFields acroFields;
    private Set fieldNameKeys;
    private String filename;
    private String separator;
    private Einstellungen l;
    private Rechnung r;
    private Customer k;
    private Object[][] products;
    private Double netto = 0d;
    private Double brutto = 0d;
    private String text = "";
    private String number = "";
    private Double money = 0d;

    /**
     * 
     * @param b
     * @param text 
     * @param number
     * @param money 
     */
    public PDF_Mahnung(Rechnung b, String text, String number, Double money) {


        l = Einstellungen.instanceOf();
        this.r = b;

        k = new Customer(b.getKundenId());

        products = r.getProductlistAsArray();

        this.text = text;
        this.number = number;
        this.money = money;

        this.start();
    }

    public void start() {
        try {


            separator = File.separator;
            //new out(separator);

            PdfReader template = new PdfReader(l.getMahnungtemp());
            filename = l.getMahnungverz() + File.separator + r.getRechnungnummer().replaceAll(" ", "_") + "_" + number + "_" + k.getFirma().replaceAll(" ", "_") + k.getName().replaceAll(" ", "_") + ".pdf";

//            filename = filename.replaceAll(" ", "_");
            filename = filename.trim();

            File updatedPDF = new File(filename);


            Log.Debug("Creating PDF: " + updatedPDF.getPath());
            PdfStamper pdfStamper = new PdfStamper(template, new FileOutputStream(updatedPDF.getAbsolutePath()));
            acroFields = pdfStamper.getAcroFields();
            HashMap PDFFields = acroFields.getFields();

            fieldNameKeys = PDFFields.keySet();

            setAllFields();

            pdfStamper.setFormFlattening(true);

            pdfStamper.close();

            open();
        } catch (Exception e) {
            e.printStackTrace();
            Popup.error(Strings.NO_PDF + e.getMessage(), Popup.ERROR);

        }
    }

    public void printOutFieldNames() {

        for (Iterator fieldNames = fieldNameKeys.iterator(); fieldNames.hasNext();) {
            try {
                String fieldName = (String) fieldNames.next();
                Log.Debug(fieldName);

                acroFields.setField(fieldName, fieldName);
            } catch (IOException ex) {
                Logger.getLogger(PDF_Mahnung.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DocumentException ex) {
                Logger.getLogger(PDF_Mahnung.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void open() {
        try {


            if (Main.IS_WINDOWS) {
                Process proc = Runtime.getRuntime().exec(l.getPdfviewer() + "  \"" + filename + "\"");
            } else {
                Process proc = Runtime.getRuntime().exec(l.getPdfviewer() + "  " + filename);
            }
        } catch (IOException ex) {
            Log.Debug("Es ist ein Fehler aufgetreten: " + "\n" + ex);
//            Popup.notice("Kein PDF-Programm angegeben. Wählen Sie Ihren PDF Reader unter 'Programmeinstellungen'.");

        }
    }

    private void setAllFields() throws IOException, DocumentException {


        acroFields.setField("company", k.getFirma());
        acroFields.setField("name", k.getAnrede() + " " + k.getVorname() + " " + k.getName());
        acroFields.setField("street", k.getStr());
        acroFields.setField("city", k.getPLZ() + " " + k.getOrt());

        acroFields.setField("originaldate", r.getFDatum());

        acroFields.setField("date", Formater.formatDate(new Date()));

        acroFields.setField("rnumber", r.getRechnungnummer());
        acroFields.setField("number", number);
        acroFields.setField("knumber", k.getKundennummer());


        acroFields.setField("text", text);



//id,Anzahl,Posten,Mehrwertsteuer,Nettopreis,Bruttopreis
        for (int i = 0; i < products.length; i++) {

            try {


                netto = netto + ((Double) products[i][4] * (Double) products[i][1]);
                brutto = brutto + ((Double) products[i][5] * (Double) products[i][1]);

            } catch (Exception exception) {
            }



        }

        acroFields.setField("originalprice", Formater.formatMoney(brutto));

        brutto = brutto + money;

        acroFields.setField("arrearsfee", Formater.formatMoney(money));
        acroFields.setField("totalprice", Formater.formatMoney(brutto));

    }
}
