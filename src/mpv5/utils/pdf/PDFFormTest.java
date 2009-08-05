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
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.utils.text.RandomText;

/**
 *
 *  
 */
public class PDFFormTest {

  private AcroFields acroFields;
  private PdfReader template;
  private File pdf;

  /**
   *
   * @param pdf
   * @throws java.io.IOException
   * @throws com.lowagie.text.DocumentException
   */
  public PDFFormTest(File pdf) throws IOException, DocumentException {

    this.pdf = pdf;
    template = new PdfReader(pdf.getPath());
    System.out.println("Checking PDF File: " + pdf.getPath());

    acroFields = template.getAcroFields();

  }

  public void printFields() {
    HashMap PDFFields = acroFields.getFields();

    for (Iterator it = PDFFields.keySet().iterator(); it.hasNext();) {
      Object object = it.next();
      System.out.println("Field: " + object);
    }
  }

  public void fillFields() {
    try {
      File f = new File(pdf.getParent() + File.separator + RandomText.getText() + ".pdf");
      System.out.println("Creating PDF File: " + f.getPath());
      PdfStamper pdfStamper = new PdfStamper(template, new FileOutputStream(f.getPath()));
      pdfStamper.setFormFlattening(true);
      acroFields = pdfStamper.getAcroFields();
      HashMap PDFFields = acroFields.getFields();
      for (Iterator it = PDFFields.keySet().iterator(); it.hasNext();) {
        Object object = it.next();
        System.out.println("Filling Field: " + object);
        try {
          acroFields.setField(object.toString(), "test_" + RandomText.getText());
        } catch (IOException ex) {
          mpv5.logging.Log.Debug(ex);//Logger.getLogger(PDFFormTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DocumentException ex) {
          mpv5.logging.Log.Debug(ex);//Logger.getLogger(PDFFormTest.class.getName()).log(Level.SEVERE, null, ex);
        }
      }

      pdfStamper.close();
    } catch (DocumentException ex) {
      mpv5.logging.Log.Debug(ex);//Logger.getLogger(PDFFormTest.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      mpv5.logging.Log.Debug(ex);//Logger.getLogger(PDFFormTest.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  public void fillFields(HashMap map) {
    try {
      File f = new File(pdf.getParent() + File.separator + RandomText.getText() + ".pdf");
      f = new File(RandomText.getText() + ".pdf");
      System.out.println("Creating PDF File: " + f.getPath());
      PdfStamper pdfStamper = new PdfStamper(template, new FileOutputStream(f.getPath()));
      pdfStamper.setFormFlattening(true);
      acroFields = pdfStamper.getAcroFields();
      HashMap PDFFields = acroFields.getFields();
      for (Iterator it = PDFFields.keySet().iterator(); it.hasNext();) {
        Object object = it.next();
        System.out.println("Filling Field: " + object);
        try {
          acroFields.setField(object.toString(),(String) map.get(object.toString()));
        } catch (IOException ex) {
          mpv5.logging.Log.Debug(ex);//Logger.getLogger(PDFFormTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DocumentException ex) {
          mpv5.logging.Log.Debug(ex);//Logger.getLogger(PDFFormTest.class.getName()).log(Level.SEVERE, null, ex);
        }
      }

      pdfStamper.close();
    } catch (DocumentException ex) {
      mpv5.logging.Log.Debug(ex);//Logger.getLogger(PDFFormTest.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      mpv5.logging.Log.Debug(ex);//Logger.getLogger(PDFFormTest.class.getName()).log(Level.SEVERE, null, ex);
    }

  }
}
