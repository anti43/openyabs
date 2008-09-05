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
import mp4.items.Things;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.utils.combobox.CheckComboItem;

/**
 *
 * @author Andreas
 */
public class RechnungBetreffZZR extends Things {

    private int id = 0;
    private String[][] values;
    private Integer rechnungid = 0;
    private ArrayList liste = new ArrayList();
    private int vls = 0;

    public RechnungBetreffZZR(Integer rechnungid) {
        super(ConnectionHandler.instanceOf().clone(TABLE_BILL_TEXTS_TO_BILLS));
        this.rechnungid = rechnungid;
        values = this.select("betreffzid", "rechnungid", rechnungid.toString(), true);
        explode(values);
    }

    public RechnungBetreffZZR() {
        super(ConnectionHandler.instanceOf().clone(TABLE_BILL_TEXTS_TO_BILLS));
    }

    public void add(ArrayList list) {
       liste = explode(liste, list);
    }

    public ArrayList getList() {
       return liste;
    }

    public Object[] getListVorlagen() {

        String[][] data = new RechnungBetreffzeile().getVorlagen();
        CheckComboItem[] models = new CheckComboItem[data.length];
        RechnungBetreffzeile zeile;

        for (int i = 0; i < data.length; i++) {
            zeile = new RechnungBetreffzeile(Integer.valueOf(data[i][0]));
            if (zeile != null) {
                models[i] = new CheckComboItem(zeile.getId(), zeile.getName(), true);
            }
        }
        return models;
    }

    public Object[] getDisplayListData() {

        ArrayList list = explode(new ArrayList(), liste);
        list = explode(list, new RechnungBetreffzeile().getVorlagenIds());

        CheckComboItem[] models = new CheckComboItem[list.size()];
        RechnungBetreffzeile zeile;

        for (int i = 0; i < list.size(); i++) {
            zeile = (RechnungBetreffzeile) list.get(i);
            if (zeile != null) {
                models[i] = new CheckComboItem(zeile.getId(), zeile.getName(), true);
            }
        }
        return models;
    }

    @SuppressWarnings({"unchecked", "unchecked"})
    private void explode(String[][] data) {

        if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[i].length; j++) {
                    liste.add(new RechnungBetreffzeile(Integer.valueOf(data[i][0])));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void add(RechnungBetreffzeile zeile) {
        zeile.setHandler(this);
        liste.add(zeile);
    }

    public void removeAll() {
        liste.clear();
        liste.trimToSize();
    }

    public Integer getRechnungId() {
        return this.rechnungid;
    }

    public void setRechnungId(Integer id) {
        this.rechnungid = id;
    }

    public void save() {
        RechnungBetreffzeile zeile;

        this.freeQuery("DELETE FROM " + TABLE_BILL_TEXTS_TO_BILLS + " WHERE rechnungid = " + rechnungid);
        for (int i = 0; i < liste.size() - vls; i++) {
            zeile = (RechnungBetreffzeile) liste.get(i);
            this.insert(TABLE_BILL_TEXTS_TO_BILLS_FIELDS, rechnungid + "," + zeile.getId());
        }
    }

    public Object[][] getOriginalListData() {

        Object[][] data = new Object[liste.size()][3];
        RechnungBetreffzeile zeile;

        for (int i = 0; i < liste.size(); i++) {
            zeile = (RechnungBetreffzeile) liste.get(i);
            data[i][0] = zeile.getId();
            data[i][1] = zeile.getName();
            data[i][2] = zeile.getText();
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    private ArrayList explode(ArrayList arrayList, String[][] data) {
        if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[i].length; j++) {
                    arrayList.add(new RechnungBetreffzeile(Integer.valueOf(data[i][0])));
                }
            }
        }
        return arrayList;
    }

    @SuppressWarnings("unchecked")
    private ArrayList explode(ArrayList arrayList, ArrayList liste) {
        for (int i = 0; i < liste.size(); i++) {
            arrayList.add(liste.get(i));
        }
        return arrayList;
    }

 
}
