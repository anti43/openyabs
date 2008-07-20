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

import mp4.einstellungen.Einstellungen;
import mp4.klassen.objekte.*;
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


import mp3.classes.utils.Formater;
import mp3.classes.utils.Log;
import mp3.classes.utils.Printer;
/**
 *
 * @author anti43
 */
public class PDF_Serienbrief {

    private AcroFields acroFields;
    private Set fieldNameKeys;
    private Customer k;
    private String text = "";
    private File template;
    private File updatedPDF;
    private String pretext;
    private Einstellungen l;
    private String separator;
    private boolean named = false;

    /**
     * 
     * @param c 
     * @param template 
     * @param pretext 
     * @param text
     * @param named 
     */
    public PDF_Serienbrief(Customer c, File template, String pretext, String text, boolean named) {
        try {

            l = Einstellungen.instanceOf();

            k = c;

            this.text = text;
            this.pretext = pretext;
            this.template = template;
            this.named = named;

            this.start();
        } catch (Exception ex) {
          
        }
    }

    public void start() throws Exception {
        try {

//        try {

            separator = File.separator;

            Double d = Double.valueOf(Math.random() * 100);

            PdfReader pdftemplate = new PdfReader(template.getAbsolutePath());



            updatedPDF = File.createTempFile(d.toString(), ".pdf");
            updatedPDF.deleteOnExit();

            Log.Debug("Creating PDF: " + updatedPDF.getPath());

            PdfStamper pdfStamper = new PdfStamper(pdftemplate, new FileOutputStream(updatedPDF.getAbsolutePath()));
            acroFields = pdfStamper.getAcroFields();
            HashMap PDFFields = acroFields.getFields();

            fieldNameKeys = PDFFields.keySet();

            setAllFields();

            pdfStamper.setFormFlattening(true);

            pdfStamper.close();

           
        } catch (DocumentException ex) {
         
        } catch (IOException ex) {
           
        }
      
    }

    public void printOutFieldNames() {

        for (Iterator fieldNames = fieldNameKeys.iterator(); fieldNames.hasNext();) {
            try {
                String fieldName = (String) fieldNames.next();
                Log.Debug(fieldName);

                acroFields.setField(fieldName, fieldName);
            } catch (IOException ex) {
                Logger.getLogger(PDF_Serienbrief.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DocumentException ex) {
                Logger.getLogger(PDF_Serienbrief.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * 
     * @throws java.lang.Exception
     */
    public void print() throws Exception {
        

            Log.Debug("Printing: " + updatedPDF.getAbsolutePath(),true);
            String cmd[] = new String[]{"konsole","lp " + updatedPDF.getAbsolutePath()};

            
            new Printer().print(new File(updatedPDF.getAbsolutePath()));

       

    }

    private void setAllFields() throws IOException, DocumentException {


        acroFields.setField("company", k.getFirma());
        acroFields.setField("name", k.getAnrede() + " " + k.getVorname() + " " + k.getName());
        acroFields.setField("street", k.getStr());
        acroFields.setField("city", k.getPLZ() + " " + k.getOrt());
        
        acroFields.setField("knumber", k.getKundennummer());


        acroFields.setField("date", Formater.formatDate(new Date()));

        if (named) {
            acroFields.setField("pretext", pretext + " " + k.getAnrede() + " " + k.getName());
        } else {
            acroFields.setField("pretext", pretext);
        }
        acroFields.setField("text", text);

    }
}
