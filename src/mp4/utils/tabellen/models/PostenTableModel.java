package mp4.utils.tabellen.models;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import mp3.classes.layer.Popup;
import mp3.classes.utils.Log;
import mp4.einstellungen.Einstellungen;
import mp4.klassen.objekte.Product;
import mp4.utils.tabellen.DataModelUtils;
import mp4.utils.zahlen.FormatNumber;
import mp4.utils.text.*;

/**
 * Tablemodel for bills and orders
 * @author anti43
 */
public class PostenTableModel extends MPTableModel {

    private static final long serialVersionUID = -2651695142723361873L;

    public PostenTableModel() {
        super(new Class[]{java.lang.Integer.class, java.lang.Double.class,
                    java.lang.String.class, java.lang.Double.class, java.lang.Double.class,
                    java.lang.Double.class
                },
                new boolean[]{
                    true, true, true, true, true, true
                },
                new Object[][]{
                    {null, 1.0, null, Einstellungen.instanceOf().getGlobaltax(), null, null},
                    {null, 1.0, null, Einstellungen.instanceOf().getGlobaltax(), null, null},
                    {null, 1.0, null, Einstellungen.instanceOf().getGlobaltax(), null, null},
                    {null, 1.0, null, Einstellungen.instanceOf().getGlobaltax(), null, null},
                    {null, 1.0, null, Einstellungen.instanceOf().getGlobaltax(), null, null},
                    {null, 1.0, null, Einstellungen.instanceOf().getGlobaltax(), null, null},
                    {null, 1.0, null, Einstellungen.instanceOf().getGlobaltax(), null, null},
                    {null, 1.0, null, Einstellungen.instanceOf().getGlobaltax(), null, null},
                    {null, 1.0, null, Einstellungen.instanceOf().getGlobaltax(), null, null},
                    {null, 1.0, null, Einstellungen.instanceOf().getGlobaltax(), null, null}
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
                end = TextFormat.verifyTextMaxLength(product.getText(), 60);
                String str = product.getName() + " ";
                str = str + product.getText().substring(0, end) + " ";
                str = str + product.getEan();
                m.setValueAt(str, selectedRow, 2);
            } catch (Exception exception) {
                Log.Debug(exception);
            }
            m.setValueAt(new Double(1), selectedRow, 1);
            try {
                m.setValueAt(product.getTAX(), selectedRow, 3);
            } catch (NumberFormatException numberFormatException) {
                Popup.notice("Wert 'Steuer' unzulässig: Produkt " + product.getNummer());
                m.setValueAt(Einstellungen.instanceOf().getGlobaltax(), selectedRow, 3);
            }
            try {
                m.setValueAt(new Double(product.getVK()), selectedRow, 4);
            } catch (NumberFormatException numberFormatException) {
                Popup.notice("Wert 'Preis' unzulässig: Produkt " + product.getNummer());
                m.setValueAt(product.getVK(), selectedRow, 4);
            }
        } catch (Exception exception) {
            Log.Debug(exception);
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

