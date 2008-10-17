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

import java.util.ArrayList;
import java.util.Date;
import mp4.interfaces.Countable;
import mp4.datenbank.verbindung.Query;
import mp4.items.handler.NumberFormatHandler;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.datenbank.verbindung.PrepareData;
import mp4.logs.*;

/**
 *
 * @author anti
 */
public class Customer extends mp4.items.People implements mp4.datenbank.installation.Tabellen, Countable {

    private String Kundensteuernummer = "";
    private Query query;
    private boolean deleted = false;
    private NumberFormatHandler nfh;

    public Customer() {
        super(ConnectionHandler.instanceOf().clone(TABLE_CUSTOMERS));
        this.query = ConnectionHandler.instanceOf();
        nfh = new NumberFormatHandler(this, new Date());
    }

    public Customer(Query query) {
        super(query.clone(TABLE_CUSTOMERS));
        this.query = query;
        nfh = new NumberFormatHandler(this, new Date());

    }

    public Customer(Integer id) {
        super(ConnectionHandler.instanceOf().clone(TABLE_CUSTOMERS));
        this.id = id;
        readonly = !lock();
        try {
            this.explode(this.selectLast("*", "id", id.toString(), true));
        } catch (Exception ex) {
            Log.Debug(this,ex);
        }
        this.query = ConnectionHandler.instanceOf();
        nfh = new NumberFormatHandler(this, new Date());
    }

    public Customer(String nummer) throws Exception {
        super(ConnectionHandler.instanceOf().clone(TABLE_CUSTOMERS));
        String[] vals = this.selectLast("*", "nummer", nummer, false);
        if (vals != null && vals.length > 0) {
            this.explode(vals);
            //We can only lock it after explode() here, as ID is not set before
            readonly = !lock();
        } else {
            throw new Exception("Datensatz nicht vorhanden");
        }
        this.query = ConnectionHandler.instanceOf();
        nfh = new NumberFormatHandler(this, new Date());
    }

    @SuppressWarnings("unchecked")
    public ArrayList getAllCustomers() {
        ArrayList arr = new ArrayList();

        Query q = query.clone(TABLE_CUSTOMERS);
        String[][] str = q.select("id", null);

        for (int i = 0; i < str.length; i++) {
            arr.add(new Customer(Integer.valueOf(str[i][0])));
        }

        return arr;
    }

    public Query getConnectionHandler() {
        return query;
    }

    public String getid() {
        return id.toString();
    }

    public boolean isValid() {
        if (this.id > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String collect() {
        return PrepareData.finalize(super.collect() + PrepareData.prepareString(this.getKundensteuernummer()));
    }

    @Override
    public void explode(String[] str) {
        try {
            super.explode(str);
            this.setKundensteuernummer(str[12 + 3]);
            if (str[12 + 4].equals("1")) {
                this.setDeleted(true);
            }
        } catch (Exception exception) {
            Log.Debug(this,exception);
        }
    }

    public void LEGACYexplode(String[] str) {
        try {

//            this.id = Integer.valueOf(str[0]);
            this.setNummer(str[1]);
            this.setFirma(str[2]);
            this.setAnrede(str[3]);
            this.setVorname(str[4]);
            this.setName(str[5]);
            this.setStr(str[6]);
            this.setPLZ(str[7]);
            this.setOrt(str[8]);
            this.setTel(str[9]);
            this.setFax(str[10]);
            this.setMobil(str[11]);
            this.setMail(str[12]);
            this.setWebseite(str[12 + 1]);
            this.setNotizen(str[12 + 2]);
            if (str[12 + 3].equals("1")) {
                this.setDeleted(true);
            }

        } catch (Exception exception) {
            Log.Debug(this,exception);
        }

    }

    public boolean save() throws Exception {
        
            if (id > 0) {
                if (!readonly) {
                this.update(TABLE_CUSTOMER_FIELDS, this.collect(), id);
                isSaved = true;
                return true;
                } 
            } else if (id == 0) {
                this.id = this.insert(TABLE_CUSTOMER_FIELDS, this.collect());
                lock();
                return true;
            }
        return false;
    }

    public String[][] getBills() {

        Query q = query.clone(TABLE_BILLS);

        String[] wher = {"kundenid", this.getId().toString(), ""};

        String[][] str = q.select("id,rechnungnummer,datum", wher);

        return str;

    }

    public String[][] getPrintModel() {

        Query q = query.clone(TABLE_CUSTOMERS);

        String[][] str = q.select(TABLE_CUSTOMER_PRINT_FIELDS, null);

        return str;
    }

    public String[][] getAll(boolean withDeleted) {

        Query q = query.clone(TABLE_CUSTOMERS);


        String[][] str = q.select("*", null, withDeleted);

        return str;
    }

    public void export() {
    }

    private void setDeleted(boolean b) {
        this.deleted = b;
    }

    /**
     * 
     * @return the satus of this customer
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    public Object[][] countBills() {

        Query q = query.clone(TABLE_CUSTOMERS);



        String[][] str = q.select("firma,", null, TABLE_CUSTOMERS, "kundenid", "datum");


        return str;

    }

    public String getKundensteuernummer() {
        return Kundensteuernummer;
    }

    public void setKundensteuernummer(String Kundensteuernummer) {
        this.Kundensteuernummer = Kundensteuernummer;
    }

    @Override
    public Date getDatum() {
        return new Date();
    }

    @Override
    public String getTable() {
        return TABLE_CUSTOMERS;
    }

    @Override
    public String getCountColumn() {
        return "nummer";
    }

    public NumberFormatHandler getNfh() {
        return nfh;
    }

}
