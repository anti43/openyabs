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
package mp4.klassen.pdf;

import mp4.klassen.objekte.MyData;
import mp4.klassen.objekte.*;
import mp4.klassen.objekte.Customer;
import mp4.klassen.objekte.Angebot;
import com.lowagie.text.pdf.*;
import com.lowagie.text.DocumentException;
import java.io.*;
import java.util.*;

import mp3.Main;
import mp3.classes.layer.Popup;
import mp3.classes.layer.QueryClass;

import mp3.classes.objects.ungrouped.*;
import mp3.classes.utils.Formater;
import mp3.classes.utils.Log;

/**
 *
 * @author anti43
 */
public class PDF_Angebot {

    private AcroFields acroFields;
    private Set fieldNameKeys;
    private String[] myData;
    private String filename;
    private String separator;
    private MyData l;
    private Angebot r;
    private Customer k;
    private Object[][] products;
    private Double netto = 0d;
    private Double brutto = 0d;
//Insert the following into a servlet or adapt it to a standard java class
    //get a handle to the file location
    /**
     * 
     * @param b
     */
    public PDF_Angebot(Angebot b) {


        l = MyData.instanceOf();
        this.r = b;

        k = new Customer(QueryClass.instanceOf(), b.getKundenId());

        products = r.getProductlistAsArray();

//          Log.Debug(products);

        this.start();
    }

    public void start() {
        try {


            Properties prop = System.getProperties();
            separator = prop.getProperty("file.separator");
            //new out(separator);

            PdfReader template = new PdfReader(l.getAngebottemp());
            filename = l.getAngebotverz() + separator + r.getOrdernummer().replaceAll(" ", "_") + "_" + k.getFirma().replaceAll(" ", "_") + "_" + k.getName().replaceAll(" ", "_") + ".pdf";

//           r.getRechnungnummer().replaceAll(" ", "_")+ "_" + k.getFirma().replaceAll(" ", "_") + k.getName().replaceAll(" ", "_")+".pdf";
//            filename = filename.replaceAll(" ", "_");
            filename = filename.trim();

            File updatedPDF = new File(filename);

            Log.Debug("Creating PDF: " + updatedPDF.getPath());
            PdfStamper pdfStamper = new PdfStamper(template, new FileOutputStream(updatedPDF.getAbsolutePath()));
            acroFields = pdfStamper.getAcroFields();
            HashMap PDFFields = acroFields.getFields();

            fieldNameKeys = PDFFields.keySet();

            setAllFields();

//            float[] photograph = acroFields.getFieldPositions("image");
//            Rectangle rect = new Rectangle(photograph[1], photograph[2], photograph[3], photograph[4]);
//            Image img = Image.getInstance(myData[3]);
//            img.scaleToFit(rect.width(), rect.height());
//            img.setAbsolutePosition(photograph[1] + (rect.width() - img.scaledWidth()) / 2, photograph[2] + (rect.height() - img.scaledHeight()) / 2);
//            PdfContentByte cb = pdfStamper.getOverContent((int) photograph[0]);
//            cb.addImage(img);


            pdfStamper.setFormFlattening(true);

            pdfStamper.close();

            open();
        } catch (Exception e) {
            Popup.error("Bitte geben Sie unter \nBearbeiten-> Einstellungen ein PDF-Template an.\n" + e.getMessage(), Popup.ERROR);

        }
    }

    public void printOutFieldNames() {

        for (Iterator fieldNames = fieldNameKeys.iterator(); fieldNames.hasNext();) {
            try {
                String fieldName = (String) fieldNames.next();
                Log.Debug(fieldName);

                acroFields.setField(fieldName, fieldName);
            } catch (IOException ex) {
//                Logger.getLogger(OrderPDF.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DocumentException ex) {
//                Logger.getLogger(OrderPDF.class.getName()).log(Level.SEVERE, null, ex);
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

        }
    }

    private void setAllFields() throws IOException, DocumentException {

        acroFields.setField("company", k.getFirma());
        acroFields.setField("name", k.getAnrede() + " " + k.getVorname() + " " + k.getName());
        acroFields.setField("street", k.getStr());
        acroFields.setField("city", k.getPLZ() + " " + k.getOrt());

        acroFields.setField("date", r.getFDatum());
        acroFields.setField("validdate", r.getBisFDatum());

        acroFields.setField("number", r.getOrdernummer());
        acroFields.setField("knumber", k.getKundennummer());



//    private String auftragid= "";
//    private String anzahl = "";
//    private String posten = "";
//    private String preis = "";
//    private String steuersatz = "";
        for (int i = 0; i < products.length; i++) {
            int t = i + 1;
            try {

                if (products[i][2] != null && String.valueOf(products[i][2]).length() > 0) {
                    acroFields.setField("quantity" + t, Formater.formatDecimal((Double) products[i][1]));
                    acroFields.setField("product" + t, String.valueOf(products[i][2]));
                    acroFields.setField("price" + t, Formater.formatMoney((Double) products[i][5]));
                    
                    acroFields.setField("price_net" + t, Formater.formatMoney((Double) products[i][4]));
                    acroFields.setField("price_tax" + t, Formater.formatMoney((Double) products[i][3]));

                    acroFields.setField("multipliedprice" + t, Formater.formatMoney((Double) products[i][5] * (Double) products[i][1]));

                    netto = netto + ((Double) products[i][4] * (Double) products[i][1]);
                    brutto = brutto + ((Double) products[i][5] * (Double) products[i][1]);
                }
            } catch (Exception exception) {
//                Log.Debug("Nicht genug Produktfelder im Dokument? Produkt: "+i+"\n"+exception.getMessage(),true);
//                Popup.notice("Nicht genug Produktfelder im Dokument? Produkt: "+i+"\n"+exception.getMessage());
            }



        }

        Double tax = brutto - netto;

        acroFields.setField("taxrate", l.getGlobaltax());
        acroFields.setField("tax", Formater.formatMoney(tax));
        acroFields.setField("totalprice", Formater.formatMoney(brutto));

    }
}
