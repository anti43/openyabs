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
package mp4.items;

import mp4.items.handler.NumberFormatHandler;
import java.util.Date;

import mp3.classes.interfaces.Countable;
import mp3.classes.interfaces.Strings;
import mp3.classes.layer.Popup;
import mp4.datenbank.verbindung.Query;
import mp3.classes.utils.Log;
import mp4.utils.tabellen.models.PostenTableModel;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.datenbank.verbindung.PrepareData;
import mp4.utils.datum.DateConverter;

/**
 *
 * @author anti43
 */
public class Angebot extends mp3.classes.layer.Things implements mp4.datenbank.installation.Tabellen, Countable {

    private String Angebotnummer = "";
    private Integer KundenId = 0;
    private Integer RechnungId = 0;
    
    private boolean isAuftrag = false;
    private Date anfrageVom = new Date();
    private Date Datum = new Date();
    private Date validVon = new Date();
    private Date validBis = new Date();
    private Date auftragdatum = null;
    private Query query;
    private String[][] products;
    public Integer id = 0;
    private PostenTableModel postendata = new PostenTableModel(new Object[0][0]);

    public void add(PostenTableModel m) {
        this.postendata = m;
    }

    public Integer getId() {
        return id;
    }

    public void destroy() {
        this.delete(this.id);
        this.id = 0;
    }

    public Angebot() {
        super(ConnectionHandler.instanceOf().clone(TABLE_OFFERS));
        this.query = ConnectionHandler.instanceOf();

        nfh = new NumberFormatHandler(this, getDatum());

    }

    public Angebot(Query query) {
        super(query.clone(TABLE_OFFERS));
        this.query = query;

        nfh = new NumberFormatHandler(this, getDatum());
    }

    /**
     * 
     * @param id
     */
    public Angebot(Integer id) {
        super(ConnectionHandler.instanceOf().clone(TABLE_OFFERS));
        this.id = Integer.valueOf(id);
        this.explode(this.selectLast(Strings.ALL, Strings.ID, id.toString(), true));
        this.query = ConnectionHandler.instanceOf();
        nfh = new NumberFormatHandler(this, getDatum());

        Query q = query.clone(TABLE_OFFERS_DATA);
        String[] wher = {"angebotid", this.getId().toString(), ""};
        products = q.select(Strings.ALL, wher);
    }

    public Angebot expose() {
        System.out.println(collect());
        return this;
    }

    public String getBisFDatum() {
        return DateConverter.getDefDateString(getBisDatum());
    }

    public String getFDatum() {
        return DateConverter.getDefDateString(getDatum());
    }


    public boolean hasId() {
        if (id > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 
     * @return id,Anzahl,Posten,Mehrwertsteuer,Nettopreis,Bruttopreis
     */
    public Object[][] getProductlistAsArray() {

        Object[][] nstr = new Object[products.length][6];

        try {
            for (int i = 0; i < nstr.length; i++) {
                try {
                    nstr[i][0] = Integer.valueOf(products[i][0]);
                    nstr[i][1] = Double.valueOf(products[i][2]);
                    nstr[i][2] = String.valueOf(products[i][3]);
                    nstr[i][3] = Double.valueOf(products[i][5]);
                    nstr[i][4] = Double.valueOf(products[i][4]);
                    nstr[i][5] = Double.valueOf(
                            (Double.valueOf(products[i][4]) *
                            (((Double.valueOf(products[i][5])) / 100) + 1)));
                } catch (NumberFormatException numberFormatException) {
                    nstr[i][4] = Double.valueOf("0");
                    nstr[i][5] = Double.valueOf("0");
                }
            }
        } catch (Exception exception) {
            Log.Debug(exception);
        }
        return nstr;
    }

    public int search(Integer angebotid) {
        String[] in = this.selectLast("id", "id", angebotid.toString(), true);
        if (in != null && in.length > 0) {
            return Integer.valueOf(in[0]);
        } else {
            return 0;
        }
    }

    private void explode(PostenTableModel m) {
        for (int i = 0; i < m.getRowCount(); i++) {
            if (m.getValueAt(i, 4) != null || m.getValueAt(i, 2) != null) {
                AngebotPosten b = new AngebotPosten();
                b.setAngebotId(this.getId());
                b.setPosten((String) m.getValueAt(i, 2));

                try {
                    
                    b.setAnzahl(Double.valueOf((Double) m.getValueAt(i, 1)));
                } catch (Exception e) {
                     b.setAnzahl(0d);
                }

                try {
                    
                    b.setSteuersatz(Double.valueOf((Double) m.getValueAt(i, 3)));
                } catch (Exception e) {
                    b.setSteuersatz(0d);
                }

                try {
                    
                    b.setPreis(Double.valueOf((Double) m.getValueAt(i, 4)));
                } catch (Exception e) {
                    b.setPreis(0d);
                }

                b.save();
            }
        }
    }

    private void clearPostenData() {
        new AngebotPosten().deleteExistingOf(this);
    }

    private void explode(String[] select) {

        this.id = Integer.valueOf(select[0]);
        this.setAngebotnummer(select[1]);
        this.setKundenId(Integer.valueOf(select[2]));
        this.setDatum(DateConverter.getDate(select[3]));
        this.setAuftrag(select[4]);
        this.setAnfrageVom(DateConverter.getDate(select[5]));
        this.setValidVon(DateConverter.getDate(select[6]));
        this.setBisDatum(DateConverter.getDate(select[7]));
        this.setRechnungId(Integer.valueOf(select[8]));
        hasRechnung();

    }

    private String collect() {
        String str = PrepareData.prepareString(this.getAngebotnummer());
        str = str + PrepareData.prepareNumber(this.getKundenId());
        str = str + PrepareData.prepareString(DateConverter.getSQLDateString(this.getDatum()));
        if (this.getAuftragdatum() != null) {
            str = str + PrepareData.prepareString(DateConverter.getSQLDateString(this.getAuftragdatum()));
        } else {
            str = str + PrepareData.prepareNumber(null);
        }
        str = str + PrepareData.prepareString(DateConverter.getSQLDateString(this.getAnfrageVom()));
        str = str + PrepareData.prepareString(DateConverter.getSQLDateString(this.getValidVon()));
        str = str + PrepareData.prepareString(DateConverter.getSQLDateString(this.getBisDatum()));
        str = str + PrepareData.prepareNumber(this.getRechnungId());
        return PrepareData.finalize(str);
    }

    public boolean save() {
        int result = -1;
        if (id > 0 && !isSaved) {
            result = this.update(TABLE_OFFERS_FIELDS, this.collect(), id.toString());
            if (postendata != null) {
                clearPostenData();
                explode(postendata);
            }

            isSaved = true;
        } else if (id == 0 && !isSaved) {
            result = this.insert(TABLE_OFFERS_FIELDS, this.collect());
            this.id = this.getMyId();
            if (postendata != null) {
                explode(postendata);
            }

            isSaved = true;
        }
        if (result > 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getid() {
        return id.toString();
    }

    public String getAngebotnummer() {
        return Angebotnummer;
    }

    public void setAngebotnummer(String Ordernummer) {
        this.Angebotnummer = Ordernummer;
        this.isSaved = false;
    }

    public Integer getKundenId() {
        return KundenId;
    }

    public void setKundenId(Integer KundenId) {
        this.KundenId = KundenId;
        this.isSaved = false;
    }

    public Date getDatum() {
        return Datum;
    }

    public void setDatum(Date Datum) {
        this.Datum = Datum;
        this.isSaved = false;
    }

    public PostenTableModel getProductlistAsTableModel() {

        if (products != null) {

            Object[][] nstr = new Object[products.length][6];

            try {
                for (int i = 0; i < nstr.length; i++) {
                    nstr[i][0] = Integer.valueOf(products[i][0]);
                    nstr[i][1] = Double.valueOf(products[i][2]);
                    nstr[i][2] = String.valueOf(products[i][3]);
                    nstr[i][3] = Double.valueOf(products[i][5]);
                    try {
                        nstr[i][4] = Double.valueOf(products[i][4]);
                        nstr[i][5] = Double.valueOf(
                                (Double.valueOf(products[i][4]) *
                                (((Double.valueOf(products[i][5])) / 100) + 1)));
                    } catch (NumberFormatException numberFormatException) {
                        nstr[i][4] = Double.valueOf("0");
                        nstr[i][5] = Double.valueOf("0");
                        Popup.notice(Popup.GENERAL_ERROR);
                    }
                }
            } catch (Exception exception) {
                Log.Debug(exception);
            }
            return new PostenTableModel(nstr);
        } else {
            return postendata;
        }
    }

    private Integer getMyId() {
        String[] str = this.selectLast("id", "Angebotnummer", this.getAngebotnummer(), false);
        return Integer.valueOf(str[0]);
    }

//    private AngebotPosten[] getProducts(Query query) {
//
//        Query q = query.clone(TABLE_OFFERS_DATA);
//        String[] wher = {"auftragid", this.getId().toString(), ""};
//
//        products = q.select(Strings.ALL, wher);
//        AngebotPosten[] prof = null;
//
//        for (int t = 0; t < products.length; t++) {
//            prof[t] = new AngebotPosten(query, products[0][t]);
//        }
//        return prof;
//    }
    public String getNextNumber() {
       return  getNfh().getNextNumber();
//        return query.getNextIndexOfIntCol("angebotnummer");
    }

    /**
     * 
     * @return
     */
    public String[][] getAllWithDepencies() {
        Query q = query.clone(TABLE_OFFERS);
        String[][] prods = q.select("angebote.id,angebotnummer,angebote.datum,name,firma, auftragdatum", null, TABLE_CUSTOMERS, "kundenid");
        return prods;
    }

    /**
     * 
     * @param table1fields
     * @return 
     */
    public String[][] getWithDepencies(String table1fields) {
        Query q = query.clone(TABLE_OFFERS);
        String[][] prods = q.select(table1fields, null, TABLE_CUSTOMERS, "kundenid");
        return prods;
    }

    public String[][] getAll() {
        Query q = query.clone(TABLE_OFFERS);
        String[][] prods = q.select(Strings.ALL, null);
        return prods;
    }

    public boolean isAuftrag() {
        return isAuftrag;
    }

    public void setAuftrag(boolean auftrag) {
        this.isAuftrag = auftrag;
    }

    public Date getBisDatum() {
        return validBis;
    }

    public void setBisDatum(Date bisDatum) {
        this.validBis = bisDatum;
    }

//    public void setRechnung(boolean rechnung) {
//        this.hasRechnung = rechnung;
//    }

    public boolean hasRechnung() {
        if (getRechnungId() > 0) {
            
            return true;
        } else {
            return false;
        }
    }

    public int delete(String id) {
        return delete(Integer.valueOf(id));
    }

    public Date getAnfrageVom() {
        return anfrageVom;
    }

    public void setAnfrageVom(Date anfrageVom) {
        this.anfrageVom = anfrageVom;
    }

    public Date getValidVon() {
        return validVon;
    }

    public void setValidVon(Date validVon) {
        this.validVon = validVon;
    }

    public Integer getRechnungId() {
        return RechnungId;
    }

    public void setRechnungId(Integer RechnungId) {
        this.RechnungId = RechnungId;
        
    }

    private void setAuftrag(String datum) {
        if (DateConverter.getDate(datum) != null) {
            this.setAuftrag(true);
            this.setAuftragdatum(DateConverter.getDate(datum));
        } else {
            this.setAuftrag(false);
        }
    }

    public Date getAuftragdatum() {
        return auftragdatum;
    }

    public void setAuftragdatum(Date auftragdatum) {
        this.auftragdatum = auftragdatum;
    }

    public String getTable() {
        return TABLE_OFFERS;
    }

    public String getCountColumn() {
        return "angebotnummer";
    }
}

