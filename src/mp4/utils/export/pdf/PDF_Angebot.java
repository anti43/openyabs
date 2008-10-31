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

import java.awt.Image;
import mp4.items.Kunde;
import mp4.items.Angebot;
import java.io.*;
import java.util.ArrayList;
import java.util.Locale;
import mp4.interfaces.Template;
import mp4.logs.*;
import mp4.einstellungen.Einstellungen;
import mp4.einstellungen.Programmdaten;
import mp4.utils.files.PDFFile;
import mp4.utils.listen.ListenDataUtils;
import mp4.utils.zahlen.FormatNumber;

/**
 *
 * @author anti43
 */
public class PDF_Angebot implements Template {

    private Einstellungen l;
    private Angebot r;
    private Kunde k;
    private Object[][] products;
    private Double netto = 0d;
    private Double brutto = 0d;
    private ArrayList fields = new ArrayList();
    private String path;


    public PDF_Angebot(Angebot b, boolean persistent) {
      
            l = Einstellungen.instanceOf();
            this.r = b;
            k = new Kunde(b.getKundenId());
            products = r.getProductlistAsArray();
            Locale.setDefault(Einstellungen.instanceOf().getLocale());
        
        
        if (persistent) {
            path = getTargetFile().getPath();
        } else {
            path = PDFFile.getTempFilename() + ".pdf".trim();
        }
    }

    @SuppressWarnings("unchecked")
    private String[][] buildFieldList() {

        if (Programmdaten.instanceOf().getBILLPANEL_CHECKBOX_MITFIRMENNAME_state()) {
            fields.add(new String[]{"company", k.getFirma()});
        }
        fields.add(new String[]{"name", k.getAnrede() + " " + k.getVorname() + " " + k.getName()});
        fields.add(new String[]{"street", k.getStr()});
        fields.add(new String[]{"city", k.getPLZ() + " " + k.getOrt()});
        fields.add(new String[]{"date", r.getFDatum()});
        fields.add(new String[]{"validdate", r.getBisFDatum()});
        fields.add(new String[]{"number", r.getAngebotnummer()});
        fields.add(new String[]{"knumber", k.getNummer()});

        for (int i = 0; i < products.length; i++) {
            int t = i + 1;
            try {

                if (products[i][2] != null && String.valueOf(products[i][2]).length() > 0) {
                    fields.add(new String[]{"quantity" + t, FormatNumber.formatDezimal((Double) products[i][1])});
                    fields.add(new String[]{"product" + t, String.valueOf(products[i][2])});
                    fields.add(new String[]{"price" + t, FormatNumber.formatLokalCurrency((Double) products[i][5])});
                    fields.add(new String[]{"price_net" + t, FormatNumber.formatLokalCurrency((Double) products[i][4])});
                    fields.add(new String[]{"price_tax" + t, FormatNumber.formatLokalCurrency((Double) products[i][3])});
                    fields.add(new String[]{"multipliedprice" + t, FormatNumber.formatLokalCurrency((Double) products[i][5] * (Double) products[i][1])});
                    netto = netto + ((Double) products[i][4] * (Double) products[i][1]);
                    brutto = brutto + ((Double) products[i][5] * (Double) products[i][1]);
                }
            } catch (Exception exception) {
                Log.Debug(this, exception);
            }
        }
        Double tax = brutto - netto;
        fields.add(new String[]{"taxrate", l.getHauptsteuersatz().toString()});
        fields.add(new String[]{"tax", FormatNumber.formatLokalCurrency(tax)});
        fields.add(new String[]{"totalprice", FormatNumber.formatLokalCurrency(brutto)});

        return ListenDataUtils.StringListToTableArray(fields);
    }

    @Override
    public String getPath() {
        return path;
    }

    public String[][] getFields() {
        return buildFieldList();
    }

    public Image getImage() {
        return null;
    }

    public String getTemplate() {
        return l.getAngebot_Template();
    }

    @Override
    public File getTargetFile() {
        return new File(l.getAngebot_Verzeichnis() + File.separator + r.getAngebotnummer().replaceAll(" ", "_") + "_" + k.getFirma().replaceAll(" ", "_") +
                "_" + k.getName().replaceAll(" ", "_") + ".pdf".trim());
    }
}
