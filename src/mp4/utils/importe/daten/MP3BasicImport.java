/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mp4.utils.importe.daten;

import compat.mp3.Bill;
import compat.mp3.Customer;
import java.awt.Cursor;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import mp4.items.Rechnung;
import mp4.items.RechnungPosten;
import mp4.utils.datum.DateConverter;

/**
 *
 * @author anti43
 */
public class MP3BasicImport extends SwingWorker {

    private static Customer cust;
    private static Bill bills;
    private JProgressBar bar;
    private JTextArea textarea;

    public MP3BasicImport(JProgressBar bar, JTextArea jTextArea1) {
        this.bar = bar;
        this.textarea = jTextArea1;
    }

    public void importBasicData() {

        textarea.append("Importiere Kundendaten, Rechnung und Rechnungsposten:");
        cust = new compat.mp3.Customer(compat.mp3.QueryClass.instanceOf(compat.mp3.ImportHelper.dbURL));
        bills = new compat.mp3.Bill(compat.mp3.QueryClass.instanceOf(compat.mp3.ImportHelper.dbURL));

        String[][] data = cust.select("*", null, null, false);
        bar.setMaximum(data.length);
        bar.setMinimum(0);
        bar.setStringPainted(true);
        bar.setString(null);

        for (int i = 0; i < data.length; i++) {

            String[] singleVals = data[i];

            mp4.items.Customer kunde = new mp4.items.Customer();
            String[][] bdata = bills.select("id", "kundenid", singleVals[0], true);


            try {
                kunde.LEGACYexplode(singleVals);
                kunde.save();
                textarea.append("\nErfolgreich: (Kunde " + kunde.getKundennummer() + ") ");
            } catch (Exception e) {
                textarea.append("\nFehler: (Kunde " + kunde.getKundennummer() + ") " + e.getMessage());
            }


            for (int j = 0; j < bdata.length; j++) {
                String[] bsingleVals = bdata[j];
                compat.mp3.Bill bill = new compat.mp3.Bill(compat.mp3.QueryClass.instanceOf(compat.mp3.ImportHelper.dbURL), bsingleVals[0]);

                Rechnung rechnung = null;
                try {
                    rechnung = new Rechnung();
                    rechnung.setKundenId(kunde.getId());
                    rechnung.setRechnungnummer(bill.getRechnungnummer());
                    rechnung.setDatum(DateConverter.getDate(bill.getDatum()));
                    rechnung.setBezahlt(bill.isBezahlt());
                    rechnung.setStorno(bill.isStorno());
                    rechnung.setGesamtpreis(Double.valueOf(bill.getGesamtpreis()));
                    rechnung.setGesamttax(Double.valueOf(bill.getGesamttax()));
                    rechnung.save();

                } catch (Exception xception) {
                    textarea.append("\nFehler: (Rechnung " + rechnung.getRechnungnummer() + ") " + xception.getMessage());
                }

                if (rechnung.getId() > 0) {
                    /*** 
                     * @return id,Anzahl,Posten,Mehrwertsteuer,Nettopreis,Bruttopreis
                     *java.lang.Integer.class, java.lang.Double.class, java.lang.String.class,
                    java.lang.Double.class, java.lang.Double.class, java.lang.Double.class};*/
                    Object[][] postendta = bill.getProductlistAsArray();
                    try {
                        for (int k = 0; k < postendta.length; k++) {
                            Object[] objects = postendta[k];
                            RechnungPosten p = new RechnungPosten();


                            p.setRechnungid(rechnung.getId());
                            p.setAnzahl(Double.valueOf(objects[1].toString()));
                            p.setPosten(objects[2].toString());
                            p.setSteuersatz(Double.valueOf(objects[3].toString()));
                            p.setPreis(Double.valueOf(objects[4].toString()));
                            p.save();

                        }
                        textarea.append("\nErfolgreich: (Rechnung " + rechnung.getRechnungnummer() + ") ");
                    } catch (Exception xception) {
                        textarea.append("\nFehler: (Rechnungsposten " + rechnung.getRechnungnummer() + " ) " + xception.getMessage());
                    }
                } else {
                    textarea.append("\nFehler: (Rechnung " + rechnung.getRechnungnummer() + " ) ");
                }
            }
            bar.setValue(i + 1);
        }
    }

    @Override
    protected Object doInBackground() throws Exception {
        bar.getParent().setCursor(new Cursor(Cursor.WAIT_CURSOR));
        importBasicData();
        bar.getParent().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        return null;
    }
}
