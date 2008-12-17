/*
 *  This file is part of MP by anti43 /GPL.
 *
 *  MP is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.print;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.SimpleDoc;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import mpv5.db.common.DatabaseObject;
import mpv5.logging.Log;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileReaderWriter;
import mpv5.utils.jobs.Waiter;

/**
 *
 * @author anti43
 */
public class PrintJob implements Waiter {

    private PrintService prservDflt;
    private PrintService[] prservices;
    int idxPrintService = -1;
    private HashPrintRequestAttributeSet aset;
    private DocFlavor flavor;

    public PrintJob() {
        aset = new HashPrintRequestAttributeSet();
        aset.add(MediaSizeName.ISO_A4);
        this.flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        prservDflt = PrintServiceLookup.lookupDefaultPrintService();
        prservices = PrintServiceLookup.lookupPrintServices(flavor, aset);
    }

    public PrintJob(DocFlavor flavor) {
        aset = new HashPrintRequestAttributeSet();
        aset.add(MediaSizeName.ISO_A4);
        this.flavor = flavor;
        prservDflt = PrintServiceLookup.lookupDefaultPrintService();
        prservices = PrintServiceLookup.lookupPrintServices(flavor, aset);

    }

    /**
     *
     * @param filelist
     */
    public void print(ArrayList<File> filelist) {
        if (null == prservices || 0 >= prservices.length) {
            if (null != prservDflt) {
                System.err.println("Nur Default-Printer, da lookupPrintServices fehlgeschlagen.");
                prservices = new PrintService[]{prservDflt};
            }
        }
        Log.Debug(this, "Print-Services:");
        int i;
        for (i = 0; i < prservices.length; i++) {
            Log.Debug(this, "  " + i + ":  " + prservices[i] + ((prservDflt != prservices[i]) ? "" : " (Default)"));
        }
        PrintService prserv = null;
        if (0 <= idxPrintService && idxPrintService < prservices.length) {
            prserv = prservices[idxPrintService];
        } else {
            if (!Arrays.asList(prservices).contains(prservDflt)) {
                prservDflt = null;
            }
            if (prservices == null) {
                prservices = new PrintService[]{PrintServiceLookup.lookupDefaultPrintService()};
            }
            prserv = ServiceUI.printDialog(null, 50, 50, prservices, prservDflt, null, aset);
        }
        if (null != prserv) {
            Log.Debug(this, "Ausgewaehlter Print-Service:");
            Log.Debug(this, "      " + prserv);
            printPrintServiceAttributesAndDocFlavors(prserv);
            DocPrintJob pj = prserv.createPrintJob();


            for (int j = 0; j < filelist.size(); j++) {
                FileInputStream fis = null;
                try {
                    File file = filelist.get(j);
                    fis = new FileInputStream(file);
                    Doc doc = new SimpleDoc(fis, flavor, null);
                    try {
                        pj.print(doc, aset);
                    } catch (PrintException ex) {
                        Log.Debug(ex);
                    }
                } catch (FileNotFoundException ex) {
                    Log.Debug(ex);
                } finally {
                    try {
                        fis.close();
                    } catch (IOException ex) {
                        Log.Debug(ex);
                    }
                }
            }
        }

    }

    public void printl(List<DatabaseObject> dbobjarr) {

        File file = FileDirectoryHandler.getTempFile();
        if (dbobjarr != null && dbobjarr.size() > 0) {
            FileReaderWriter rw = new FileReaderWriter(file);

            rw.writeOnce(dbobjarr.get(0).getDbID());
            rw.write("");
            rw.write(dbobjarr.get(0).__getCName());
            rw.write("");

            for (int i = 0; i < dbobjarr.size(); i++) {
                DatabaseObject databaseObject = dbobjarr.get(i);
                ArrayList<String[]> data = databaseObject.getValues();

                for (int h = 0; h < data.size(); h++) {
                    rw.write(data.get(h)[0] + ": " + data.get(h)[1]);
                }
            }
        }
        try {
            print(file);
        } catch (FileNotFoundException fileNotFoundException) {
            Log.Debug(this, fileNotFoundException);
        } catch (PrintException printException) {
            Log.Debug(this, printException);
        }
    }

    @SuppressWarnings("unchecked")
    public void print(DatabaseObject dbobj) {
        ArrayList list = new ArrayList();
        list.add(dbobj);
        printl(list);
    }

    /*
     * Prints a File
     */
    public void print(File file) throws FileNotFoundException, PrintException {
        ArrayList<File> list = new ArrayList<File>();
        list.add(file);
        print(list);
    }

    /*
     * Prints a mp4.interfaces.Printable Object
     */
    private void print(Printable printable) {
        this.flavor = printable.getFlavor();
        try {
            print(printable.getFile());
        } catch (FileNotFoundException fileNotFoundException) {
            Log.Debug(this, fileNotFoundException);
        } catch (PrintException printException) {
            Log.Debug(this, printException);
        }

    }

    private void printPrintServiceAttributesAndDocFlavors(PrintService prserv) {
        String s1 = null, s2;
        Attribute[] prattr = prserv.getAttributes().toArray();
        DocFlavor[] prdfl = prserv.getSupportedDocFlavors();
        if (null != prattr && 0 < prattr.length) {
            for (int i = 0; i < prattr.length; i++) {
                Log.Debug(this, "      PrintService-Attribute[" + i + "]: " + prattr[i].getName() + " = " + prattr[i]);
            }
        }
        if (null != prdfl && 0 < prdfl.length) {
            for (int i = 0; i < prdfl.length; i++) {
                s2 = prdfl[i].getMimeType();
                if (null != s2 && !s2.equals(s1)) {
                    Log.Debug(this, "      PrintService-DocFlavor-Mime[" + i + "]: " + s2);
                }
                s1 = s2;
            }
        }
    }


    /*
     * Prints a File or mp4.interfaces.Printable Object
     */
    @Override
    public void set(Object object) {
        try {
            try {
                print((Printable) object);
            } catch (ClassCastException ex) {
                print((File) object);
            }
        } catch (FileNotFoundException fileNotFoundException) {
            Log.Debug(this, fileNotFoundException);
        } catch (PrintException printException) {
            Log.Debug(this, printException);
        }
    }
}
