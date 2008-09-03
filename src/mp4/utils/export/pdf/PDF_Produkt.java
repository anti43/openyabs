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
import mp4.interfaces.Printable;
import mp3.classes.utils.Formater;
import mp4.einstellungen.Einstellungen;
import mp4.items.Dienstleistung;
import mp4.items.Hersteller;
import mp4.items.Lieferant;
import mp4.items.Product;
import mp4.utils.datum.DateConverter;
import mp4.utils.zahlen.FormatNumber;
import mp4.utils.zahlen.FormatTax;

/**
 *
 * @author anti43
 */
public class PDF_Produkt implements Printable {

    private Einstellungen settings;
    private Product produkt;
    private Lieferant lieferant;
    private Hersteller hersteller;
    private Dienstleistung service;
    private Image bild;
    private ArrayList fields = new ArrayList();

    public PDF_Produkt(Dienstleistung produkt) {
        settings = Einstellungen.instanceOf();
        this.service = produkt;
    }

    public PDF_Produkt(Product produkt, java.awt.Image image) {
        settings = Einstellungen.instanceOf();
        this.produkt = produkt;
        this.lieferant = new Lieferant(produkt.getLieferantenId());
        this.hersteller = new Hersteller(produkt.getHerstellerId());
        this.bild = image;
    }


    @SuppressWarnings("unchecked")
    private String[][] buildFieldsList(){

        if (lieferant.isValid()) {
            fields.add(new String[]{"lieferantfirma", lieferant.getFirma()});
            fields.add(new String[]{"lieferantname", lieferant.getAnrede() + " " + lieferant.getVorname() + " " + lieferant.getName()});
            fields.add(new String[]{"lieferantstrasse", lieferant.getStr()});
            fields.add(new String[]{"lieferantort", lieferant.getPLZ() + " " + lieferant.getOrt()});
        }

        if (hersteller.isValid()) {
            fields.add(new String[]{"herstellerfirma", hersteller.getFirma()});
            fields.add(new String[]{"herstellername", hersteller.getAnrede() + " " + hersteller.getVorname() + " " + hersteller.getName()});
            fields.add(new String[]{"herstellerstrasse", hersteller.getStr()});
            fields.add(new String[]{"herstellerort", hersteller.getPLZ() + " " + hersteller.getOrt()});
        }

        fields.add(new String[]{"datum", DateConverter.getDefDateString(produkt.getDatum())});
        fields.add(new String[]{"name", produkt.getName()});
        fields.add(new String[]{"ean", produkt.getEan()});
        fields.add(new String[]{"nummer", produkt.getProduktNummer()});
        fields.add(new String[]{"warengruppe", produkt.getProductgroupPath()});
        fields.add(new String[]{"langtext", produkt.getText()});
        fields.add(new String[]{"url", produkt.getUrl()});
        fields.add(new String[]{"vk", FormatNumber.formatLokalCurrency(produkt.getVK())});
        fields.add(new String[]{"steuersatz", FormatTax.formatLokal(produkt.getTaxValue())});
        
        return Formater.StringListToTableArray(fields);

    }

    public String getPath() {
        return settings.getProduktverz() + File.separator + produkt.getProduktNummer().replaceAll(" ", "_") + ".pdf".trim();
    }

    public String[][] getFields() {
      return  buildFieldsList();
    }

    public Image getImage() {
        return bild;
    }

    public String getTemplate() {
        return settings.getProdukttemp();
    }
}
