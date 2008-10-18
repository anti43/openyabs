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
package mp4.utils.files;

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

import javax.print.DocFlavor;
import mp4.interfaces.Template;
import mp4.interfaces.Waitable;
import mp4.items.visual.Popup;

import mp4.logs.*;
import mp4.einstellungen.Einstellungen;
import mp4.einstellungen.Programmdaten;
import mp4.interfaces.Printable;
import mp4.main.Main;

/**
 *
 * @author anti43
 */
public class PDFFile extends File implements Waitable, Printable {

    private static final long serialVersionUID = 7455276510000739261L;
    private AcroFields acroFields;
    private Set fieldNameKeys;
    private Einstellungen settings;
    private PdfStamper pdfStamper;
    private boolean scale = false;
    private int width,  height;
    private Template object;
    private String[][] fields;

    public PDFFile(Template object) {
        super(object.getPath());
        this.object = object;
        this.fields = object.getFields();
        settings = Einstellungen.instanceOf();
    }

    public void start() {

        if (this.exists()) {
            if (Popup.Y_N_dialog(this.getPath() + "\nexistiert bereits. Überschreiben?")) {
                if (!this.delete()) {
                    Popup.error("Datei konnte nicht gelöscht werden.", "Es ist ein Fehler aufgetreten.");
                }
                work();
            }
        } else {
            work();
        }
    }

    public void printOutFieldNames() {
        String fieldnames = "";
        for (Iterator fieldNames = fieldNameKeys.iterator(); fieldNames.hasNext();) {
            fieldnames += ", " + (String) fieldNames.next();
        }
        Log.Debug(this, "Template: " + fieldnames);
        Log.Debug(this, "Objekt: ");
        for (int i = 0; i < fields.length; i++) {
            Log.Debug(this, fields[i][0] + "= " + fields[i][1]);
        }
    }

    public void open() {
        try {
            if (Main.IS_WINDOWS) {
                Process proc = Runtime.getRuntime().exec(settings.getPDF_Programm() + "  \"" + this.getPath() + "\"");
            } else {
                Process proc = Runtime.getRuntime().exec(settings.getPDF_Programm() + "  " + this.getPath());
            }
        } catch (IOException ex) {
            Log.Debug(this, "Es ist ein Fehler aufgetreten: " + "\n" + ex);
        }
    }

    private Image parseImage(java.awt.Image image) {
        try {
            return Image.getInstance(image, null);
        } catch (BadElementException ex) {
            Log.Debug(this, ex);
        } catch (IOException ex) {
            Log.Debug(this, ex);
        }

        return null;
    }

    private void setFields() throws IOException, DocumentException {

        if (Log.getLoglevel() == Log.LOGLEVEL_HIGH) {
            printOutFieldNames();
        }

        for (int i = 0; i < fields.length; i++) {
            try {
                acroFields.setField(fields[i][0], fields[i][1]);
            } catch (IOException e) {
                Log.Debug(this, e);
            } catch (DocumentException ex) {
                Log.Debug(this, ex);
            }
        }
    }

    private void setImage() throws DocumentException {
        if (object.getImage() != null) {
            Log.Debug(this, "Write Image..");
            float[] photograph = acroFields.getFieldPositions("abbildung");
            Rectangle rect = new Rectangle(photograph[1], photograph[2], photograph[3], photograph[4]);
            Image img = parseImage(object.getImage());
            if (scale) {
                img.scaleToFit(width, height);
            } else {
//                img.scaleToFit(rect.width(), rect.height());
            }
            img.setAbsolutePosition(photograph[1] + (rect.getWidth() - img.getScaledWidth()) / 2, photograph[2] + (rect.getHeight() - img.getScaledHeight()) / 2);
            PdfContentByte cb = pdfStamper.getOverContent((int) photograph[0]);
            cb.addImage(img);
        }
    }

    @Override
    public void waitFor() {
        start();
    }

    public void setScale(int width, int height) {
        this.scale = true;
        this.width = width;
        this.height = height;
    }

    public static String getTempFilename() {
        File file = null;
        try {
            file = File.createTempFile("mpTempPdf", ".pdf", Programmdaten.instanceOf().getDIR_CACHE());
            file.deleteOnExit();
        } catch (IOException ex) {
            Log.Debug(PDFFile.class, ex);
        }
        return file.getPath();
    }

    @Override
    public DocFlavor getFlavor() {
        return DocFlavor.INPUT_STREAM.PDF;
    }

    @Override
    public File getFile() {
        return this;
    }

    private void work() {
        if (new File(object.getTemplate()).exists()) {
            try {
                PdfReader template = new PdfReader(object.getTemplate());
                Log.Debug(this, "Creating PDF File: " + this.getPath());
                pdfStamper = new PdfStamper(template, new FileOutputStream(this.getPath()));
                acroFields = pdfStamper.getAcroFields();
                HashMap PDFFields = acroFields.getFields();
                fieldNameKeys = PDFFields.keySet();
                Log.Debug(this, "Set field values..");
                setFields();
                Log.Debug(this, "Set image (if exists)..");
                setImage();
            } catch (DocumentException ex) {
                Log.Debug(this, ex);
            } catch (IOException e) {
                Log.Debug(this, e);
            } finally {
                Log.Debug(this, "Finishing..");
                pdfStamper.setFormFlattening(true);
                try {
                    pdfStamper.close();
                } catch (DocumentException ex) {
                    Log.Debug(this, ex);
                } catch (IOException ex) {
                    Log.Debug(this, ex);
                }
            }
        } else {
            Popup.error("Bitte geben Sie unter \nBearbeiten-> Einstellungen ein PDF-Template an." +
                    "\nTemplate: " + object.getTemplate() + "\nexistiert nicht.", Popup.ERROR);
        }
    }
}

