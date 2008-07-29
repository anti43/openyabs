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

import java.util.ArrayList;
import javax.swing.ComboBoxModel;
import mp3.classes.layer.Things;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.utils.tabellen.models.MPJComboboxModelItem;


/**
 *
 * @author Andreas
 */
public class RechnungBetreffZZR extends Things {

    private int id = 0;
    private String[][] values;
    private Integer rechnungid = 0;
    private ArrayList liste = new ArrayList();

    public RechnungBetreffZZR(Integer rechnungid) {
        super(ConnectionHandler.instanceOf().clone(TABLE_BILL_TEXTS_TO_BILLS));
        this.rechnungid = rechnungid;
        values = this.select("betreffzid", "rechnungid", rechnungid.toString(), true);
        explode(values);
    }

    public RechnungBetreffZZR() {
        super(ConnectionHandler.instanceOf().clone(TABLE_BILL_TEXTS_TO_BILLS));
    }

    public Object[] getListModel() {
        
        MPJComboboxModelItem[] models = new MPJComboboxModelItem[liste.size()];
        RechnungBetreffzeile zeile;

        for (int i = 0; i < liste.size(); i++) {
            zeile = (RechnungBetreffzeile) liste.get(i);
            models[i] = new MPJComboboxModelItem(zeile.getId(),zeile.getName(),zeile.getText());
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
    }

    public Integer getRechnungId() {
        return this.rechnungid;
    }

    public void setRechnungId(Integer id) {
        this.rechnungid = id;
    }

    public void save() {
        RechnungBetreffzeile zeile;

        for (int i = 0; i < liste.size(); i++) {
            zeile = (RechnungBetreffzeile) liste.get(i);
            this.freeQuery("DELETE FROM " + TABLE_BILL_TEXTS_TO_BILLS + " WHERE rechnungid = " + rechnungid);
            this.insert(TABLE_BILL_TEXTS_TO_BILLS_FIELDS, rechnungid + "," + zeile.getId());
        }
    }

    public Object[][] getListData() {

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
}
