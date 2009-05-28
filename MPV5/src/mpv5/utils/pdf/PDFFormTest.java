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
package mpv5.utils.pdf;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import mpv5.logging.Log;

/**
 *
 * @author anti
 */
public class PDFFormTest {


    private AcroFields acroFields;

    public PDFFormTest(File pdf) throws IOException, DocumentException {

        PdfReader template = new PdfReader(pdf.getPath());
        System.out.println( "Checking PDF File: " + pdf.getPath());

        acroFields = template.getAcroFields();

    }

    public void printFields() {
        HashMap PDFFields = acroFields.getFields();

        for (Iterator it = PDFFields.keySet().iterator(); it.hasNext();) {
            Object object = it.next();
            System.out.println("Field: "  + object);
        }
    }
}
