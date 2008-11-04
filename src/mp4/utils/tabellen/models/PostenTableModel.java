package mp4.utils.tabellen.models;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import mp4.items.visual.Popup;
import mp4.logs.*;
import mp4.einstellungen.Einstellungen;
import mp4.einstellungen.Programmdaten;
import mp4.items.Dienstleistung;
import mp4.items.Product;
import mp4.utils.tabellen.DataModelUtils;
import mp4.utils.text.*;

/**
 * Tablemodel for bills and orders
 * @author anti43
 */
public class PostenTableModel extends MPTableModel {

    private static final long serialVersionUID = -2651695142723361873L;
    private double stst =  Einstellungen.instanceOf().getHauptsteuersatz();

    public PostenTableModel() {
        super(new Class[]{java.lang.Integer.class, java.lang.Double.class,
                    java.lang.String.class, java.lang.Double.class, java.lang.Double.class,
                    java.lang.Double.class
                },
                new boolean[]{
                    true, true, true, true, true, true
                },
                new Object[][]{
                    {null, 1.0, null, Einstellungen.instanceOf().getHauptsteuersatz(), null, null},
                    {null, 1.0, null, Einstellungen.cachedHauptsteuersatz, null, null},
                    {null, 1.0, null, Einstellungen.cachedHauptsteuersatz, null, null},
                    {null, 1.0, null, Einstellungen.cachedHauptsteuersatz, null, null},
                    {null, 1.0, null, Einstellungen.cachedHauptsteuersatz, null, null},
                    {null, 1.0, null, Einstellungen.cachedHauptsteuersatz, null, null},
                    {null, 1.0, null, Einstellungen.cachedHauptsteuersatz, null, null},
                    {null, 1.0, null, Einstellungen.cachedHauptsteuersatz, null, null},
                    {null, 1.0, null, Einstellungen.cachedHauptsteuersatz, null, null},
                    {null, 1.0, null, Einstellungen.cachedHauptsteuersatz, null, null}
                },
                new Object[]{"id", "Anzahl", "Bezeichnung", "Steuersatz", "Nettopreis", "Bruttopreis"});
        
        

    }

    public PostenTableModel(Object[][] data) {
        super(new Class[]{java.lang.Integer.class, java.lang.Double.class,
                    java.lang.String.class, java.lang.Double.class, java.lang.Double.class,
                    java.lang.Double.class
                }, new boolean[]{true, true, true, true, true, true},
                data, new Object[]{"id", "Anzahl", "Bezeichnung", "Steuersatz", "Nettopreis", "Bruttopreis"});
    }

    public void addProduct(JTable table, Product product) {
        TableModel m = this;
        int end = 0;
        DataModelUtils.addRowToTable(table);
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            selectedRow = this.getLastRow(table);
        }

        try {
            try {
                end = TextFormat.verifyTextMaxLength(product.getText(), 50);
                String str = "";
                str = product.getName() + " ";
                if(Programmdaten.instanceOf().getPRODUCTPICKER_NUMMER())str = str + product.getNummer() + " ";
                if(Programmdaten.instanceOf().getPRODUCTPICKER_TEXT())str = str + product.getText().substring(0, end) + " ";
                if(Programmdaten.instanceOf().getPRODUCTPICKER_EAN())str = str + product.getEan();
                m.setValueAt(str, selectedRow, 2);
            } catch (Exception exception) {
                Log.Debug(this,exception);
            }
            m.setValueAt(new Double(1), selectedRow, 1);
            try {
                m.setValueAt(product.getTaxValue(), selectedRow, 3);
            } catch (NumberFormatException numberFormatException) {
                Popup.notice("Wert 'Steuer' unzulässig: Produkt " + product.getProduktNummer());
                m.setValueAt(Einstellungen.instanceOf().getHauptsteuersatz(), selectedRow, 3);
            }
            try {
                m.setValueAt(new Double(product.getVK()), selectedRow, 4);
            } catch (NumberFormatException numberFormatException) {
                Popup.notice("Wert 'Preis' unzulässig: Produkt " + product.getProduktNummer());
                m.setValueAt(product.getVK(), selectedRow, 4);
            }
        } catch (Exception exception) {
            Log.Debug(this,exception);
        }
    }
    
     public void addService(JTable table, Dienstleistung product) {
        TableModel m = this;
        int end = 0;
        DataModelUtils.addRowToTable(table);
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            selectedRow = this.getLastRow(table);
        }

        try {
            try {
                end = TextFormat.verifyTextMaxLength(product.getBeschreibung(), 60);
                String str = "";
                if(Programmdaten.instanceOf().getPRODUCTPICKER_NAME())str = product.getName() + " ";
                if(Programmdaten.instanceOf().getPRODUCTPICKER_TEXT())str = str + product.getBeschreibung().substring(0, end) + " ";
                if(Programmdaten.instanceOf().getPRODUCTPICKER_NUMMER())str = str + product.getProduktNummer();
                m.setValueAt(str, selectedRow, 2);
            } catch (Exception exception) {
                Log.Debug(this,exception);
            }
            m.setValueAt(new Double(1), selectedRow, 1);
            try {
                m.setValueAt(product.getTaxValue(), selectedRow, 3);
            } catch (NumberFormatException numberFormatException) {
                Popup.notice("Wert 'Steuer' unzulässig: Produkt " + product.getProduktNummer());
                m.setValueAt(Einstellungen.instanceOf().getHauptsteuersatz(), selectedRow, 3);
            }
            try {
                m.setValueAt(new Double(product.getPreis()), selectedRow, 4);
            } catch (NumberFormatException numberFormatException) {
                Popup.notice("Wert 'Preis' unzulässig: Produkt " + product.getProduktNummer());
                m.setValueAt(product.getPreis(), selectedRow, 4);
            }
        } catch (Exception exception) {
            Log.Debug(this,exception);
        }
    }


    private int getLastRow(JTable table) {
        int rowID = -1;
        int i;

        for (i = 0; i < table.getRowCount(); i++) {
            if (table.getValueAt(i, 2) == null || table.getValueAt(i, 2).equals("")) {
                rowID = i;
                break;
            }
        }
        if (rowID == -1) {
            Object[][] o = new Object[][]{null, null, null, null, null};
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            m.addRow(o);
            rowID = i + 1;
        }
        return rowID;
    }
}

