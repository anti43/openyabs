/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.export;

import com.lowagie.text.BadElementException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import enoa.handler.TableHandler;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import mpv5.logging.Log;
import mpv5.utils.images.MPIcon;

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
            Log.Debug(this, "Running export for template file: " + getPath() + "  to file: " + getTarget());
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
                String keyt = String.valueOf(object);
                if (getData().get(keyt) != null) {
                    Log.Debug(this, "Filling Field: " + object + " [" + getData().get(keyt) + "]");
                    if (!keyt.startsWith("image")) {
                        acroFields.setField(keyt, String.valueOf(getData().get(keyt)));
                    } else {
                        setImage(pdfStamper, keyt, ((MPIcon) getData().get(keyt)).getImage());
                    }
                }
                if (getData().get(keyt.replace("_", ".")) != null) {
                    Log.Debug(this, "Filling Field: " + object + " [" + getData().get(keyt) + "]");
                    if (!keyt.startsWith("image")) {
                       acroFields.setField(keyt, String.valueOf(getData().get(keyt.replace("_", "."))));
                    } else {
                        setImage(pdfStamper, keyt, ((MPIcon) getData().get(keyt)).getImage());
                    }
                }
            }

            Log.Debug(this, "Looking for tables in: " + getName());
            for (Iterator<String> it = getData().keySet().iterator(); it.hasNext();) {
                String key = it.next();
                if (key.startsWith(TableHandler.KEY_TABLE)) {//Table found
                    @SuppressWarnings("unchecked")
                    List<String[]> value = (List<String[]>) getData().get(key);
                    for (int i = 0; i < value.size(); i++) {
                        String[] strings = value.get(i);
                        for (int j = 0; j < strings.length; j++) {
                            String cellValue = strings[j];
                            String fieldname = "col" + j + "row" + i;
                            Log.Debug(this, "Filling Field: " + fieldname + " [" + getData().get(cellValue) + "]");
                            acroFields.setField(fieldname, cellValue);
                        }
                    }
                }
            }

            pdfStamper.close();
            Log.Debug(this, "Done file: " + getTarget().getPath());
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }

    private void setImage(PdfStamper stamper, String key, java.awt.Image oimg) {
        try {
            Log.Debug(this, "Write Image.." + key);
            float[] photograph = acroFields.getFieldPositions(key);
            Rectangle rect = new Rectangle(photograph[1], photograph[2], photograph[3], photograph[4]);
            Image img = Image.getInstance(oimg, null);

            img.setAbsolutePosition(photograph[1] + (rect.getWidth() - img.getScaledWidth()) / 2, photograph[2] + (rect.getHeight() - img.getScaledHeight()) / 2);
            PdfContentByte cb = stamper.getOverContent((int) photograph[0]);
            cb.addImage(img);
        } catch (Exception iOException) {
            Log.Debug(iOException);
        }
    }
}
