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
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import mp4.interfaces.Template;
import mp4.einstellungen.Einstellungen;
import mp4.items.Dienstleistung;
import mp4.items.Hersteller;
import mp4.items.Lieferant;
import mp4.items.Product;
import mp4.utils.datum.DateConverter;
import mp4.utils.files.PDFFile;
import mp4.utils.listen.ListenDataUtils;
import mp4.utils.zahlen.FormatNumber;
import mp4.utils.zahlen.FormatTax;

/**
 *
 * @author anti43
 */
public class PDF_Produkt implements Template {

    private Einstellungen settings;
    private Product produkt;
    private Lieferant lieferant;
    private Hersteller hersteller;
    private Image bild;
    private ArrayList fields = new ArrayList();
    private String path;


    public PDF_Produkt(Product produkt, java.awt.Image image, boolean persistent) {
        settings = Einstellungen.instanceOf();
        this.produkt = produkt;
        this.lieferant = produkt.getLieferant();
        this.hersteller = produkt.getHersteller();
        this.bild = image;
        Locale.setDefault(Einstellungen.instanceOf().getLocale());
          
        if (persistent) {
            path = getTargetFile().getPath();
        } else {
            path = PDFFile.getTempFilename() + ".pdf".trim();
        }
    }

    @SuppressWarnings("unchecked")
    private String[][] buildFieldsList() {

        if (lieferant != null && lieferant.isValid()) {
            fields.add(new String[]{"scompany", lieferant.getFirma()});
            fields.add(new String[]{"sname", lieferant.getAnrede() + " " + lieferant.getVorname() + " " + lieferant.getName()});
            fields.add(new String[]{"sstreet", lieferant.getStr()});
            fields.add(new String[]{"scity", lieferant.getPLZ() + " " + lieferant.getOrt()});
        }

        if (hersteller!= null && hersteller.isValid()) {
            fields.add(new String[]{"mcompany", hersteller.getFirma()});
            fields.add(new String[]{"mname", hersteller.getAnrede() + " " + hersteller.getVorname() + " " + hersteller.getName()});
            fields.add(new String[]{"mstreet", hersteller.getStr()});
            fields.add(new String[]{"mcity", hersteller.getPLZ() + " " + hersteller.getOrt()});
        }

        fields.add(new String[]{"date", DateConverter.getDefDateString(produkt.getDatum())});
        fields.add(new String[]{"name", produkt.getName()});
        fields.add(new String[]{"ean", produkt.getEan()});
        fields.add(new String[]{"number", produkt.getProduktNummer()});
        fields.add(new String[]{"group", produkt.getProductgroupPath()});
        fields.add(new String[]{"text", produkt.getText()});
        fields.add(new String[]{"url", produkt.getUrl()});
        fields.add(new String[]{"price", FormatNumber.formatLokalCurrency(produkt.getVK())});
        fields.add(new String[]{"taxrate", FormatNumber.formatLokalTax(produkt.getTaxValue())});

        return ListenDataUtils.StringListToTableArray(fields);

    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String[][] getFields() {
        return buildFieldsList();
    }

    @Override
    public Image getImage() {
        return bild;
    }

    @Override
    public String getTemplate() {
        return settings.getProdukt_Template();
    }

    @Override
    public File getTargetFile() {
        return new File(settings.getProdukt_Verzeichnis() + File.separator + produkt.getProduktNummer().replaceAll(" ", "_") + ".pdf".trim());
    }

}
