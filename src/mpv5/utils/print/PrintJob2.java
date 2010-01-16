/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.print;

import java.util.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import mpv5.globals.Messages;
import mpv5.logging.Log;

/**
 *
 * @author Torsten Horn
 */
public class PrintJob2 {
    /* PrintWithJ2SE14Document.java: Drucken eines Dokuments mit J2SE 1.4 */

    public PrintJob2(File file, String fileType) throws Exception {
        
        final String sPrintFile = "PrintFile.ps";
        final String[] ssFileExtensionsAccepted = {"JPEG", "JPG", "PNG", "GIF", "TXT", "HTM", "HTML", "PS", "PDF"};
        final DocFlavor[] docFlavorsAccepted = {DocFlavor.INPUT_STREAM.JPEG, DocFlavor.INPUT_STREAM.JPEG,
            DocFlavor.INPUT_STREAM.PNG, DocFlavor.INPUT_STREAM.GIF,
            DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST,
            DocFlavor.INPUT_STREAM.TEXT_HTML_HOST,
            DocFlavor.INPUT_STREAM.TEXT_HTML_HOST,
            DocFlavor.INPUT_STREAM.POSTSCRIPT,
            DocFlavor.INPUT_STREAM.PDF};
        DocFlavor flavor = null;
        int i, idxPrintService = -1;

        String sInputFilenameExtension = fileType.toUpperCase();
        for (i = 0; i < ssFileExtensionsAccepted.length; i++) {
            if (ssFileExtensionsAccepted[i].equals(sInputFilenameExtension)) {
                flavor = docFlavorsAccepted[i];
                break;
            }
        }

        if (null == flavor) {
            throw new UnsupportedOperationException("Type not printable here: " + fileType);
        }

        // Set print attributes:
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add(MediaSizeName.ISO_A4);

                // Print to PrintService (e.g. to Printer):
                PrintService prservDflt = PrintServiceLookup.lookupDefaultPrintService();
                PrintService[] prservices = PrintServiceLookup.lookupPrintServices(flavor, aset);
                if (null == prservices || 0 >= prservices.length) {
                    if (null != prservDflt) {
                        prservices = new PrintService[]{prservDflt};
                    } else {
                        mpv5.ui.dialogs.Notificator.raiseNotification( Messages.PRINT_NOT_POSSIBLE + " " + fileType);
                    }
                }
                Log.Debug(this, "Print-Services:");
                for (i = 0; i < prservices.length; i++) {
                    Log.Debug(this, "  " + i + ":  " + prservices[i]
                            + ((prservDflt != prservices[i]) ? "" : " (Default)"));
                }
                PrintService prserv = null;
                if (0 <= idxPrintService && idxPrintService < prservices.length) {
                    prserv = prservices[idxPrintService];
                } else {
                    if (!Arrays.asList(prservices).contains(prservDflt)) {
                        prservDflt = null;
                    }
                    prserv = ServiceUI.printDialog(null, 50, 50, prservices, prservDflt, null, aset);
                }
                if (null != prserv) {
                    Log.Debug(this, "Choosen Print-Service:");
                    Log.Debug(this, "      " + prserv);
                    printPrintServiceAttributesAndDocFlavors(prserv);
                    DocPrintJob pj = prserv.createPrintJob();
                    FileInputStream fis = new FileInputStream(file);
                    Doc doc = new SimpleDoc(fis, flavor, null);
                    pj.print(doc, aset);
                    Log.Debug(this, "Document '" + file + "' printed.");
                }
    }

    private void printPrintServiceAttributesAndDocFlavors(PrintService prserv) {
        String s1 = null, s2;
        Attribute[] prattr = prserv.getAttributes().toArray();
        DocFlavor[] prdfl = prserv.getSupportedDocFlavors();
        if (null != prattr && 0 < prattr.length) {
            for (int i = 0; i < prattr.length; i++) {
                Log.Debug(this, "      PrintService-Attribute[" + i + "]: "
                        + prattr[i].getName() + " = " + prattr[i]);
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
}

