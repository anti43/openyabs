/*
 *  This file is part of MP.
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
package mpv5.utils.export;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import mpv5.logging.Log;

/**
 *
 *  
 */
public class PDFFile extends Exportable {

    private PdfReader template;
    private AcroFields acroFields;

    public PDFFile(String pathToFile) {
        super(pathToFile);
    }

    @Override
    public void run() {
        try {
            try {
                template = new PdfReader(getPath());
            } catch (Exception iOException) {
                Log.Debug(iOException);
            }
            Log.Debug(this, "Checking PDF File: " + getPath());
            acroFields = template.getAcroFields();
            Log.Debug(this, "Creating PDF File: " + getTarget().getPath());
            PdfStamper pdfStamper = new PdfStamper(template, new FileOutputStream(getTarget().getPath()));
            pdfStamper.setFormFlattening(true);
            acroFields = pdfStamper.getAcroFields();
            HashMap PDFFields = acroFields.getFields();
            for (Iterator it = PDFFields.keySet().iterator(); it.hasNext();) {
                Object object = it.next();
                Log.Debug(this, "Filling Field: " + object);
                acroFields.setField(object.toString(), getData().get(object.toString()));
            }
            pdfStamper.close();
            Log.Debug(this, "Done file: " + getTarget().getPath());
        } catch (Exception ex) {
            Log.Debug(ex);
        }

    }
}
