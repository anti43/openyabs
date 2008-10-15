/*
 * 
 * 
 */
package mp4.utils.importe.daten;

import compat.mp3.Product;
import compat.mp3.Supplier;
import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import mp4.items.Lieferant;
import mp4.items.visual.csvProductImporter;
import mp4.logs.Log;
import mp4.utils.files.FileReaderWriter;

/**
 *
 * @author anti43
 */
public class MP3ProductImport extends SwingWorker {

    private static Supplier lief;
    private JProgressBar bar;
    private JTextArea textarea;
    private Product prod;
    private Supplier[] supls;

    public MP3ProductImport(JProgressBar bar, JTextArea jTextArea1) {
        this.bar = bar;
        this.textarea = jTextArea1;
    }

    public void importBasicData() {

        textarea.append("Importiere Lieferanten:");
        lief = new compat.mp3.Supplier(compat.mp3.QueryClass.instanceOf(compat.mp3.ImportHelper.dbURL));

        supls = lief.getAll();

        bar.setMaximum(supls.length);
        bar.setMinimum(0);

        for (int i = 0; i < supls.length; i++) {
            Supplier lief2 = supls[i];
            try {
                Lieferant nlief = new Lieferant();
                nlief.setFax(lief2.getFax());
                nlief.setFirma(lief2.getFirma());
                nlief.setNummer(lief2.getLieferantennummer());
                nlief.setMail(lief2.getMail());
                nlief.setMobil(lief2.getMobil());
                nlief.setName(lief2.getFirma());
                nlief.setNotizen(lief2.getNotizen());
                nlief.setOrt(lief2.getOrt());
                nlief.setPLZ(lief2.getPLZ());
                nlief.setStr(lief2.getStr());
                nlief.setTel(lief2.getTel());
                nlief.setWebseite(lief2.getWebseite());
                nlief.save();

                bar.setValue(i + 1);
                textarea.append("\nErfolgreich: (Lieferant " + lief2.getLieferantennummer() + ") ");
            } catch (Exception e) {
                textarea.append("\nFehler: (Lieferant " + lief2.getLieferantennummer() + ") " + e.getMessage());
            }
        }
    }

    @Override
    protected Object doInBackground() throws Exception {
        bar.getParent().setCursor(new Cursor(Cursor.WAIT_CURSOR));
        importBasicData();
        extractProducts();
        bar.getParent().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        return null;
    }

    private void extractProducts() {
       textarea.append("Extrahiere Produkte:");
        File file = null;
        try {
            file = File.createTempFile("MP3EXP", ".txt");
            FileReaderWriter rw = new FileReaderWriter(file.getPath());
            rw.write(new compat.mp3.Product(compat.mp3.QueryClass.instanceOf(compat.mp3.ImportHelper.dbURL)).getAll());
        } catch (IOException ex) {
            Log.Debug(ex);            
        }
       textarea.append("Starte Importmodul..");
       new csvProductImporter(file);

    }
}
