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

import com.lowagie.text.BadElementException;
import com.lowagie.text.pdf.*;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;

import java.io.*;
import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import mp3.classes.interfaces.Waitable;
import mp3.classes.layer.Popup;



import mp3.classes.utils.Formater;
import mp3.classes.utils.Log;
import mp4.einstellungen.Einstellungen;
import mp4.items.Hersteller;
import mp4.items.Lieferant;
import mp4.items.Product;
import mp4.main.Main;
import mp4.utils.datum.DateConverter;
import mp4.utils.zahlen.FormatNumber;
import mp4.utils.zahlen.FormatTax;

/**
 *
 * @author anti43
 */
public class PDF_Produkt implements Waitable {

    private AcroFields acroFields;
    private Set fieldNameKeys;
    private String[] myData;
    private String filename;
    private String separator;
    private Einstellungen settings;
    private Product produkt;
    private Lieferant lieferant;
    private Hersteller hersteller;
    private Double netto = 0d;
    private Double brutto = 0d;
    private Image image;
    private PdfStamper pdfStamper;
    private boolean scale = false;
    private int width,  height;

    public PDF_Produkt(Product produkt, java.awt.Image image) {

        separator = File.separator;
        settings = Einstellungen.instanceOf();

        this.produkt = produkt;
        this.lieferant = new Lieferant(produkt.getLieferantenId());
        this.hersteller = new Hersteller(produkt.getHerstellerId());
        if (image != null) {
            this.image = parseImage(image);//        this.start();
        }
    }

    public File getFile() {
        return new File(filename);
    }

    public void start() {

        try {
            PdfReader template = new PdfReader(settings.getProdukttemp());
//                filename = settings.getProduktverz() + separator + produkt.getProduktNummer().replaceAll(" ", "_") + ".pdf";
//                filename = filename.trim();


            File updatedPDF = File.createTempFile("temppdf", ".pdf");
            filename = updatedPDF.getCanonicalPath();

//                if(updatedPDF.exists())updatedPDF.delete();

            Log.Debug("Creating PDF: " + updatedPDF.getPath());

            pdfStamper = new PdfStamper(template, new FileOutputStream(updatedPDF.getAbsolutePath()));
            acroFields = pdfStamper.getAcroFields();
            HashMap PDFFields = acroFields.getFields();
            fieldNameKeys = PDFFields.keySet();

            Log.Debug("Write text..");
            setAllFields();

            setImage();
        } catch (Exception e) {

            Popup.error("Bitte geben Sie unter \nBearbeiten-> Einstellungen ein PDF-Template an.\n" + e.getMessage(), Popup.ERROR);

            Log.Debug(e);
        } finally {

            Log.Debug("Finishing..");
            pdfStamper.setFormFlattening(true);
            try {
                pdfStamper.close();
            } catch (DocumentException ex) {
                Log.Debug(ex);
            } catch (IOException ex) {
                Log.Debug(ex);
            }
        }
//            Log.Debug("Try to launch PDF Viewer: " + settings.getPdfviewer());
//            open();


    }

    public void printOutFieldNames() {

        for (Iterator fieldNames = fieldNameKeys.iterator(); fieldNames.hasNext();) {
//            try {
            String fieldName = (String) fieldNames.next();
            Log.Debug("Field: " + fieldName);
//                acroFields.setField(fieldName, fieldName);
//            } catch (IOException ex) {
//                Log.Debug(ex);
//            } catch (DocumentException ex) {
//                Log.Debug(ex);
//            }
        }
    }

    private void open() {
        try {
            if (Main.IS_WINDOWS) {
                Process proc = Runtime.getRuntime().exec(settings.getPdfviewer() + "  \"" + filename + "\"");
            } else {
                Process proc = Runtime.getRuntime().exec(settings.getPdfviewer() + "  " + filename);
            }
        } catch (IOException ex) {
            Log.Debug("Es ist ein Fehler aufgetreten: " + "\n" + ex);
        }
    }

    private Image parseImage(java.awt.Image image) {
        try {
            return Image.getInstance(image, null);
        } catch (BadElementException ex) {
            Log.Debug(ex);
        } catch (IOException ex) {
            Log.Debug(ex);
        }

        return null;
    }

    private void setAllFields() throws IOException, DocumentException {

        if (Log.getLoglevel() == Log.LOGLEVEL_HIGH) {
            printOutFieldNames();
        }
        if (lieferant.isValid()) {
            acroFields.setField("lieferantfirma", lieferant.getFirma());
            acroFields.setField("lieferantname", lieferant.getAnrede() + " " + lieferant.getVorname() + " " + lieferant.getName());
            acroFields.setField("lieferantstrasse", lieferant.getStr());
            acroFields.setField("lieferantort", lieferant.getPLZ() + " " + lieferant.getOrt());
        }

        if (hersteller.isValid()) {
            acroFields.setField("herstellerfirma", hersteller.getFirma());
            acroFields.setField("herstellername", hersteller.getAnrede() + " " + hersteller.getVorname() + " " + hersteller.getName());
            acroFields.setField("herstellerstrasse", hersteller.getStr());
            acroFields.setField("herstellerort", hersteller.getPLZ() + " " + hersteller.getOrt());
        }

        acroFields.setField("datum", DateConverter.getDefDateString(produkt.getDatum()));
        acroFields.setField("name", produkt.getName());
        acroFields.setField("ean", produkt.getEan());
        acroFields.setField("nummer", produkt.getProduktNummer());
        acroFields.setField("warengruppe", produkt.getProductgroupPath());
        acroFields.setField("langtext", produkt.getText());
        acroFields.setField("url", produkt.getUrl());
        acroFields.setField("vk", FormatNumber.formatLokalCurrency(produkt.getVK()));
        acroFields.setField("steuersatz", FormatTax.formatLokal(produkt.getTaxValue()));

    }

    private void setImage() throws DocumentException {
        if (image != null) {
            Log.Debug("Write Image..");
            float[] photograph = acroFields.getFieldPositions("abbildung");
            Rectangle rect = new Rectangle(photograph[1], photograph[2], photograph[3], photograph[4]);
            Image img = image;
            if (scale) {
                img.scaleToFit(width, height);
            } else {
//                img.scaleToFit(rect.width(), rect.height());
            }
            img.setAbsolutePosition(photograph[1] + (rect.width() - img.scaledWidth()) / 2, photograph[2] + (rect.height() - img.scaledHeight()) / 2);
            PdfContentByte cb = pdfStamper.getOverContent((int) photograph[0]);
            cb.addImage(img);
        }
    }

    public void waitFor() {
        start();
    }

    public void setScale(int width, int height) {
        this.scale = true;
        this.width = width;
        this.height = height;
    }
}
