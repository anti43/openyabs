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
package mp4.klassen.objekte;


import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import mp3.database.util.Query;

import mp3.classes.interfaces.Strings;
import mp3.classes.utils.Log;
import mp3.classes.layer.PostenTableModel;

import mp4.utils.datum.DateConverter;


/**
 *
 * @author anti43
 */
public class Rechnung extends mp3.classes.layer.Things implements mp3.classes.interfaces.Structure {

    private String Rechnungnummer = "";
    private Integer KundenId = 0;
    
    private Date Datum = new Date();
    private Double Gesamtpreis = 0.0;
    private Double Gesamttax = 0.0;
    private boolean storno = false;
    private boolean bezahlt = false;
    private boolean verzug = false;
    private RechnungPosten[] bp;
    private Query query;
    private String[][] prods;
    private List labelsOfGetAllWithD;

    public Rechnung(Query query) {
        super(query.clone(TABLE_BILLS));
        this.query = query;
    }

    /**
     * 
     * @param query
     * @param id
     */
    public Rechnung(Query query, Integer id) {
        super(query.clone(TABLE_BILLS));
        this.id = Integer.valueOf(id);
        this.explode(this.selectLast(ALL, ID, id.toString(), true));
        this.query = query;
        bp = getProducts(query);
    }

    public Rechnung(Query query, String text, boolean b) {
        super(query.clone(TABLE_BILLS));

        try {
            this.id = Integer.valueOf(id);
            this.explode(this.selectLast(ALL, "rechnungnummer", text, false));
            this.query = query;
            bp = getProducts(query);

        } catch (Exception exception) {
        }



    }

    public Rechnung expose() {

        System.out.println(collect());
        return this;
    }

    public String getFDatum() {
      return DateConverter.getDefDateString(getDatum());
    }

    public String[][] getUnpaid() {
        Query q = query.clone(TABLE_BILLS);

        prods = q.select("id, rechnungnummer, gesamtpreis, datum", new String[]{"bezahlt", "0", "","storno", "0", ""}, "datum", false, true);

        return prods;
    }

    public String[][] getPaid() {
        Query q = query.clone(TABLE_BILLS);

        prods = q.select("id, rechnungnummer, gesamtpreis, datum", new String[]{"bezahlt", "1", "","storno", "0", ""}, "datum", false, true);

        return prods;
    }
         
    public String[][] getPaidNaked() {
//        kontenid, preis, tax, datum
        Query q = query.clone(TABLE_BILLS);

        prods = q.select("gesamtpreis, gesamttax, datum", new String[]{"bezahlt", "1", "","storno", "0", ""}, "datum", false, true);


        
        return prods;
    }
    public String[][] getPaidEUR() {
//        kontenid, preis, tax, datum
        Query q = query.clone(TABLE_BILLS);

        prods = q.select("gesamtpreis, gesamttax, datum", new String[]{"bezahlt", "1", "","storno", "0", ""}, "datum", false, true);

        String[][] tzh = new String[prods.length][4];
        
        
        for(int g=0;g<prods.length;g++){
        
            tzh[g][0] = MyData.instanceOf().getEinnahmeDefKonto().getId().toString();
            
            for(int l=0;l<prods[g].length;l++){
         
                tzh[g][l+1] = prods[g][l];
                }
               }
        
        return tzh;
    }

    public boolean hasId() {
        if (id > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void explode(String[] select) {


        try {
            this.setRechnungnummer(select[1]);
            this.setKundenId(Integer.valueOf(select[2]));

            this.setDatum(DateConverter.getDate(select[3]));
            if (select[4].equals("1")) {
                setStorno(true);
            }
            if (select[5].equals("1")) {
                setBezahlt(true);
            }

            this.setGesamtpreis(Double.valueOf(select[6]));
            this.setGesamttax(Double.valueOf(select[7]));
        } catch (Exception exception) {
             Log.Debug(exception.getMessage());
        }


        if (!isBezahlt()) {
            try {

                Date date = new Date();

                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                DecimalFormat dec = new DecimalFormat("00");

                String[] str = DateConverter.getDefDateString(getDatum()).split("\\.");
                int inte = Integer.valueOf(str[1]) + 1;
                if (inte == 13) {
                    inte = 1;
                }
                String u = dec.format(inte);

                String stri = new String(str[0] + "." + u + "." + str[2]);
               

                Date dates = df.parse(stri);

                if (date.after(dates)) {
                    this.setVerzug(true);
                    Log.Debug("Rechnung in Verzug!");
                }

            } catch (Exception ex) {
                Log.Debug(ex.getMessage());
            }

        }


    }

    private String collect() {



        String str = "";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getRechnungnummer() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + this.getKundenId() + "(;;,;;)";

        str = str + "(;;2#4#1#1#8#0#;;)" + this.getDatum() + "(;;2#4#1#1#8#0#;;)"+ "(;;,;;)";

        if (isStorno()) {
            str = str + "1" + "(;;,;;)";
        } else {
            str = str + "0" + "(;;,;;)";

        }
        if (isBezahlt()) {
            str = str + "1" + "(;;,;;)";
        } else {
            str = str + "0" + "(;;,;;)";
        }

        str = str +  this.getGesamtpreis()  + "(;;,;;)";
        str = str +  this.getGesamttax();

       
        return str;

    }

    public void save() {


        if (id > 0 && !isSaved) {
            this.update(TABLE_BILLS_FIELDS, this.collect(), id.toString());
            isSaved = true;
        } else if (id == 0 && !isSaved) {
            this.insert(TABLE_BILLS_FIELDS, this.collect());

            this.id = this.getMyId();

            isSaved = true;
        } 
    }

    public int getid() {
        return id;
    }

    public String getRechnungnummer() {
        return Rechnungnummer;
    }

    public void setRechnungnummer(String Rechnungnummer) {
        this.Rechnungnummer = Rechnungnummer;
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

    /**
     * 
     * @return id,Anzahl,Posten,Mehrwertsteuer,Nettopreis,Bruttopreis
     */
    public Object[][] getProductlistAsArray() {

        DecimalFormat n = new DecimalFormat("0.00");

        String lab = "id,Anzahl,Posten,Mehrwertsteuer,Nettopreis,Bruttopreis";
        Object[][] nstr = new Object[prods.length][6];
//Class[] types = new Class[]{java.lang.Integer.class, java.lang.Double.class, java.lang.String.class,
//        java.lang.Double.class, java.lang.Double.class, java.lang.Double.class};

        try {

            for (int i = 0; i < nstr.length; i++) {

                nstr[i][0] = Integer.valueOf(prods[i][0]);
                nstr[i][1] = Double.valueOf(prods[i][2]);
                nstr[i][2] = String.valueOf(prods[i][3]);

                nstr[i][3] = Double.valueOf(prods[i][5]);

                try {
                    nstr[i][4] = Double.valueOf(prods[i][4]);

                    nstr[i][5] = Double.valueOf(
                            (Double.valueOf(prods[i][4]) *
                            (((Double.valueOf(prods[i][5])) / 100) + 1)));

                } catch (NumberFormatException numberFormatException) {
                    nstr[i][4] = Double.valueOf("0");
                    nstr[i][5] = Double.valueOf("0");

//                    Popup.error(numberFormatException.getMessage(), Popup.ERROR);
                }
            }


        } catch (Exception exception) {
            Log.Debug(exception);
//               Popup.error(exception.getMessage(), Popup.ERROR);
        }

        return nstr;

    }

    public PostenTableModel getProductlistAsTableModel() {

        DecimalFormat n = new DecimalFormat("0.00");

        String lab = "id,Anzahl,Posten,Mehrwertsteuer,Nettopreis,Bruttopreis";
        Object[][] nstr = new Object[prods.length][6];
//Class[] types = new Class[]{java.lang.Integer.class, java.lang.Double.class, java.lang.String.class,
//        java.lang.Double.class, java.lang.Double.class, java.lang.Double.class};

        try {

            for (int i = 0; i < nstr.length; i++) {

                nstr[i][0] = Integer.valueOf(prods[i][0]);
                nstr[i][1] = Double.valueOf(prods[i][2]);
                nstr[i][2] = String.valueOf(prods[i][3]);

                nstr[i][3] = Double.valueOf(prods[i][5]);

                try {
                    nstr[i][4] = Double.valueOf(prods[i][4]);

                    nstr[i][5] = Double.valueOf(
                            (Double.valueOf(prods[i][4]) *
                            (((Double.valueOf(prods[i][5])) / 100) + 1)));

                } catch (NumberFormatException numberFormatException) {
                    nstr[i][4] = Double.valueOf("0");
                    nstr[i][5] = Double.valueOf("0");

//                    Popup.error(numberFormatException.getMessage(), Popup.ERROR);
                }
            }


        } catch (Exception exception) {
            Log.Debug(exception);
//               Popup.error(exception.getMessage(), Popup.ERROR);
        }

        return new PostenTableModel(nstr, lab.split(","));

    }

    private Integer getMyId() {

        String[] str = this.selectLast("id", "rechnungnummer", this.getRechnungnummer(), false);

        return Integer.valueOf(str[0]);
    }

    private RechnungPosten[] getProducts(Query query) {

        Query q = query.clone(TABLE_BILLS_DATA);

        String[] wher = {"rechnungid", this.getId().toString(), ""};

        prods = q.select(ALL, wher);
        RechnungPosten[] prof = null;
//
//        for (int t = 0; t < str.length; t++) {
//
//            prof[t] = new BillProducts(query, str[0][t]);
//        }
        return prof;


    }

    public Integer getNextBillNumber() {
        return query.getNextIndex("rechnungnummer");
    }

    public RechnungPosten[] getBp() {
        return bp;
    }

    /**
     * 
     * @return
     */
    public String[][] getAllWithDepencies() {

        Query q = query.clone(TABLE_BILLS);

        prods = q.select(ALL, null, TABLE_CUSTOMERS, "kundenid");

        setLabelsOfAllWithDepencies(q);
        return prods;

    }

    /**
     * 
     * @param table1fields
     * @param table2fields
     * @return
     */
    public String[][] getWithDepencies(String table1fields) {
        Query q = query.clone(TABLE_BILLS);

        prods = q.select(table1fields, null, TABLE_CUSTOMERS, "kundenid", "datum");

        setLabelsOfAllWithDepencies(q);
        return prods;
    }

    public void setLabelsOfAllWithDepencies(Query q) {

        labelsOfGetAllWithD = q.getLabels();

    }

    /**
     * 
     * @return 
     */
    public String[] getLabelsOfAllWithDepencies() {

        String[] stray = new String[labelsOfGetAllWithD.size()];

        for (int i = 0; i < labelsOfGetAllWithD.size(); i++) {

            stray[i] = (String) labelsOfGetAllWithD.get(i);

        }

        return stray;

    }

    public String[][] getAll() {

        Query q = query.clone(TABLE_BILLS);

        prods = q.select(ALL, null);

        return prods;
    }

    public boolean isStorno() {
        return storno;
    }

    public void setStorno(boolean storno) {
        this.storno = storno;
    }

    public boolean isBezahlt() {
        return bezahlt;
    }

    public void setBezahlt(boolean bezahlt) {
        this.bezahlt = bezahlt;
    }

    public boolean isVerzug() {
        return verzug;
    }

    public void setVerzug(boolean verzug) {
        this.verzug = verzug;
    }

    /**
     * 
     * @param tableo 
     * @param columns 
     * @param verzug
     * @return
     */
    public static String[][] formatTableArray(String[][] tableo, int[] columns, int verzug) {

        String[][] table = new String[tableo.length][tableo[0].length + 1];

        for (int h = 0; h < tableo.length; h++) {

            for (int j = 0; j < tableo[h].length; j++) {

                table[h][j] = tableo[h][j];
            }


            try {

                Date date = new Date();

                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                DecimalFormat dec = new DecimalFormat("00");

                String[] str = table[h][2].split("\\.");
                int inte = Integer.valueOf(str[1]) + 1;
                if (inte == 13) {
                    inte = 1;
                }
                String u = dec.format(inte);

                String stri = new String(str[0] + "." + u + "." + str[2]);
               

                Date dates = df.parse(stri);

                if (date.after(dates) && table[h][5].equals("0") && table[h][6].equals("0")) {
                    table[h][tableo[h].length] = "Ja";

                } else {

                    table[h][tableo[h].length] = "Nein";
                }

            } catch (ParseException ex) {
                Log.Debug(ex.getMessage());
            }





        }


        for (int i = 0; i < table.length; i++) {

            for (int z = 0; z < table[i].length; z++) {


                for (int h = 0; h < columns.length; h++) {

                    if (z == columns[h]) {

                        if (table[i][z].equals("0")) {
                            table[i][z] = Strings.NO;
                        } else {
                            table[i][z] = Strings.YES;
                        }
                    }


                }


            }

        }


        return table;

    }

    public Double getGesamtpreis() {
        return Gesamtpreis;
    }

    public void setGesamtpreis(Double Gesamtpreis) {
        this.Gesamtpreis = Gesamtpreis;
    }
    
      public String[][] inserType(String[][] prods) {
        String[][] pro = null;
      if(prods.length>0){
          pro =  new String[prods.length][prods[0].length +1];
         
          for (int i = 0; i < pro.length; i++) {
           int m=0;
              for (int j=0; j < pro[i].length; j++,m++) {
                  
                  
                  if(j==2) {
                        pro[i][2] = "Rechnung";
                        m--;
                    }else {
                      
                        pro[i][j] = prods[i][m];
                    }

              }
          }
      } 
      return pro;
    }

    public Double getGesamttax() {
        return Gesamttax;
    }

    public void setGesamttax(Double Gesamttax) {
        this.Gesamttax = Gesamttax;
    }
}
