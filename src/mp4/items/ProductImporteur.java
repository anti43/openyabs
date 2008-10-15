/*
 * 
 * 
 */
package mp4.items;

import java.util.ArrayList;
import mp4.utils.text.RandomText;

/**
 *
 * @author anti43
 */
public class ProductImporteur {

 

    private String Ean = "N/A";
    private String Produktnummer = "";
    private String Name = "";
    private String Datum = "";
    private String url = "";
    private String Text = "";
    private String Vk = "";
    private String Ek = "";
    private String Tax = "";
    private String Hersteller = "";
    private Integer LieferantenId = 1;
    private String Warengruppenkategorie = "";
    private String Warengruppenfamilie = "";
    private String Warengruppe = "";

    public String getEan() {
        return Ean;
    }

    public void setEan(String Ean) {
        this.Ean = Ean;
    }

    public String getProduktnummer() {
        return Produktnummer;
    }

    public void setProduktnummer(String Produktnummer) {
        this.Produktnummer = Produktnummer;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getDatum() {
        return Datum;
    }

    public void setDatum(String Datum) {
        this.Datum = Datum;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return Text;
    }

    public void setText(String Text) {
        this.Text = Text;
    }

    public String getVk() {
        return Vk;
    }

    public void setVk(String Vk) {
        this.Vk = Vk;
    }

    public String getEk() {
        return Ek;
    }

    public void setEk(String Ek) {
        this.Ek = Ek;
    }

    public String getTax() {
        return Tax;
    }

    public void setTax(String Tax) {
        this.Tax = Tax;
    }

    public String getHersteller() {
        return Hersteller;
    }

    public void setHersteller(String Hersteller) {
        this.Hersteller = Hersteller;
    }

    public String getLieferantenid() {
        return LieferantenId.toString();
    }

    public void setLieferantenid(String LieferantenId) {
        this.LieferantenId = Integer.valueOf(LieferantenId);
    }



    public String[][] getData(ProductImporteur[] imp) {
        /**
         *  ]{"produktnummer", "name", "text", "vk", 
        "ek","tax", "hersteller", "warengruppenkategorie", "warengruppenfamilie", 
        "warengruppe", "url", "ean"
         */
        String[][] str = new String[imp.length][12+1];

  
        
        for (int i = 0; i < imp.length; i++) {

            str[i][0] = imp[i].getProduktnummer();
            str[i][1] = imp[i].getName();
            str[i][2] = imp[i].getText();
            str[i][3] = imp[i].getVk();
            str[i][4] = imp[i].getEk();
            str[i][5] = imp[i].getTax();
            str[i][6] = imp[i].getHersteller();
       
            str[i][7] = imp[i].getWarengruppenkategorie();
            str[i][8] = imp[i].getWarengruppenfamilie();
            str[i][9] = imp[i].getWarengruppe();
         
            str[i][10] = imp[i].getUrl();
            str[i][11] = imp[i].getEan();
            str[i][12] = imp[i].getLieferantenid();

        }

        return str;
    }

    public static ProductImporteur[] listToImporteurArray(ArrayList list, Lieferant sup) {

        ProductImporteur[] str = new ProductImporteur[list.size()];
        ProductImporteur imp = null;
        for (int i = 0; i < list.size(); i++) {


            imp = (ProductImporteur) list.get(i);
            
            if(imp.getDatum().equals(""))imp.setDatum(" ");
            if(imp.getEan().equals(""))imp.setEan("0");
            if(imp.getEk().equals(""))imp.setEk("0");
            if(imp.getHersteller().equals(""))imp.setHersteller(" ");
            if(imp.getLieferantenid().equals(""))imp.setLieferantenid("1");
            if(imp.getName().equals(""))imp.setName(" ");
            if(imp.getProduktnummer().equals(""))imp.setProduktnummer(new RandomText().getString());
            if(imp.getTax().equals(""))imp.setTax("0");
            if(imp.getText().equals(""))imp.setText(" ");
            if(imp.getUrl().equals(""))imp.setUrl(" ");
            if(imp.getVk().equals(""))imp.setVk("0");
            if(imp.getWarengruppenkategorie().equals(""))imp.setWarengruppenkategorie("1");
            if(imp.getWarengruppenfamilie().equals(""))imp.setWarengruppenfamilie("1");
            if(imp.getWarengruppe().equals(""))imp.setWarengruppe("1");
            
            if(sup!=null)imp.setLieferantenid(sup.getId().toString());
            
            
            str[i] = imp;
        }
        return str;
    }

    public String getWarengruppenkategorie() {
        return Warengruppenkategorie;
    }

    public void setWarengruppenkategorie(String Warengruppenkategorie) {
        this.Warengruppenkategorie = Warengruppenkategorie;
    }

    public String getWarengruppenfamilie() {
        return Warengruppenfamilie;
    }

    public void setWarengruppenfamilie(String Warengruppenfamilie) {
        this.Warengruppenfamilie = Warengruppenfamilie;
    }

    public String getWarengruppe() {
        return Warengruppe;
    }

    public void setWarengruppe(String Warengruppe) {
        this.Warengruppe = Warengruppe;
    }
}
