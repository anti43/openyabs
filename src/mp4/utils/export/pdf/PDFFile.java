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

/**
 *
 * @author Andreas
 */

import com.lowagie.text.BadElementException;
import com.lowagie.text.pdf.*;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;

import java.io.*;
import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import mp3.classes.interfaces.Printable;
import mp3.classes.interfaces.Waitable;
import mp3.classes.layer.Popup;

import mp3.classes.utils.Log;
import mp4.einstellungen.Einstellungen;
import mp4.einstellungen.Programmdaten;
import mp4.main.Main;

/**
 *
 * @author anti43
 */
public class PDFFile extends File implements Waitable {
    private static final long serialVersionUID = 7455276510000739261L;

    private AcroFields acroFields;
    private Set fieldNameKeys;
    private Einstellungen settings;
    private PdfStamper pdfStamper;
    private boolean scale = false;
    private int width,  height;
    private Printable object;

    public PDFFile(Printable object) {
        super(object.getPath());
        this.object = object;
        settings = Einstellungen.instanceOf();
    }

    public void start() {
        try {
            PdfReader template = new PdfReader(object.getTemplate());
            Log.Debug("Creating PDF File: " + this.getPath());
            pdfStamper = new PdfStamper(template, new FileOutputStream(this.getPath()));
            acroFields = pdfStamper.getAcroFields();
            HashMap PDFFields = acroFields.getFields();
            fieldNameKeys = PDFFields.keySet();
            setFields();
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
    }

    public void printOutFieldNames() {
        for (Iterator fieldNames = fieldNameKeys.iterator(); fieldNames.hasNext();) {
            String fieldName = (String) fieldNames.next();
            Log.Debug("Field: " + fieldName);
        }
    }

    public void open() {
        try {
            if (Main.IS_WINDOWS) {
                Process proc = Runtime.getRuntime().exec(settings.getPdfviewer() + "  \"" + this.getPath() + "\"");
            } else {
                Process proc = Runtime.getRuntime().exec(settings.getPdfviewer() + "  " + this.getPath());
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

    private void setFields() throws IOException, DocumentException {

        if (Log.getLoglevel() == Log.LOGLEVEL_HIGH) {
            printOutFieldNames();
        }
      
        for (int i = 0; i < object.getFields().length; i++) {
            acroFields.setField(object.getFields()[i][0], object.getFields()[i][1]);
        }
    }

    private void setImage() throws DocumentException {
        if (object.getImage()!= null) {
            Log.Debug("Write Image..");
            float[] photograph = acroFields.getFieldPositions("abbildung");
            Rectangle rect = new Rectangle(photograph[1], photograph[2], photograph[3], photograph[4]);
            Image img = parseImage(object.getImage());
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
    
    public static String getTempFilename(){
        File file = null;
        try {
            file = File.createTempFile("mpTempPdf", ".pdf", Programmdaten.instanceOf().getDIR_CACHE());
            file.deleteOnExit();
        } catch (IOException ex) {
            Log.Debug(ex);
        }
        return file.getPath();
    }
}

