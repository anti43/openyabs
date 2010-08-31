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

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPrintPage;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.*;
import java.util.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.files.FileDirectoryHandler;

/**
 *
 * @author Torsten Horn, Andreas Weber
 */
public class PrintJob2 {
    /* PrintWithJ2SE14Document.java: Drucken eines Dokuments mit J2SE 1.4 */

    /**
     * Set up a new printjob and print
     * @param file
     * @param fileType
     * @throws Exception
     */
    public PrintJob2(File file, String fileType) throws Exception {
        if (fileType.toUpperCase().equals("PDF")) {
            printPdf(file);
        } else {
            print(new FileInputStream(file), fileType);
        }
    }

    /**
     * Send an InputStream to a printer
     * @param resourceAsStream
     * @param fileType
     * @throws Exception
     */
    public static void print(InputStream resourceAsStream, String fileType) throws Exception {

        final String[] ssFileExtensionsAccepted = {"JPEG", "JPG", "PNG", "GIF", "TXT", "HTM", "HTML", "PS"};
        final DocFlavor[] docFlavorsAccepted = {
            DocFlavor.INPUT_STREAM.JPEG,
            DocFlavor.INPUT_STREAM.JPEG,
            DocFlavor.INPUT_STREAM.PNG,
            DocFlavor.INPUT_STREAM.GIF,
            DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST,
            DocFlavor.INPUT_STREAM.TEXT_HTML_HOST,
            DocFlavor.INPUT_STREAM.TEXT_HTML_HOST,
            DocFlavor.INPUT_STREAM.POSTSCRIPT};
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
                mpv5.ui.dialogs.Notificator.raiseNotification(Messages.PRINT_NOT_POSSIBLE + " " + fileType);
            }
        }
        Log.Debug(PrintJob2.class, "Print-Services:");
        for (i = 0; i < prservices.length; i++) {
            Log.Debug(PrintJob2.class, "  " + i + ":  " + prservices[i]
                    + ((prservDflt != prservices[i]) ? "" : " (Default)"));
        }
        PrintService prserv = null;
        if (0 <= idxPrintService && idxPrintService < prservices.length) {
            prserv = prservices[idxPrintService];
        } else {
            if (!Arrays.asList(prservices).contains(prservDflt)) {
                prservDflt = null;
            }
            try {
                prserv = ServiceUI.printDialog(null, 50, 50, prservices, prservDflt, null, aset);
            } catch (Exception exception) {
                Log.Debug(exception);
            }
        }
        if (null != prserv) {
            Log.Debug(PrintJob2.class, "Choosen Print-Service:");
            Log.Debug(PrintJob2.class, "      " + prserv);
            printPrintServiceAttributesAndDocFlavors(prserv);
            DocPrintJob pj = prserv.createPrintJob();
            Doc doc = new SimpleDoc(resourceAsStream, flavor, null);
            pj.print(doc, aset);
            Log.Debug(PrintJob2.class, "Document '" + resourceAsStream + "' printed.");
        }
    }

    private static void printPrintServiceAttributesAndDocFlavors(PrintService prserv) {
        String s1 = null, s2;
        Attribute[] prattr = prserv.getAttributes().toArray();
        DocFlavor[] prdfl = prserv.getSupportedDocFlavors();
        if (null != prattr && 0 < prattr.length) {
            for (int i = 0; i < prattr.length; i++) {
                Log.Debug(PrintJob2.class, "      PrintService-Attribute[" + i + "]: "
                        + prattr[i].getName() + " = " + prattr[i]);
            }
        }
        if (null != prdfl && 0 < prdfl.length) {
            for (int i = 0; i < prdfl.length; i++) {
                s2 = prdfl[i].getMimeType();
                if (null != s2 && !s2.equals(s1)) {
                    Log.Debug(PrintJob2.class, "      PrintService-DocFlavor-Mime[" + i + "]: " + s2);
                }
                s1 = s2;
            }
        }
    }

    /**
     * Print a Component
     * @param c
     */
    public PrintJob2(Component c) {
        printComponent(c);
    }

    private void printPdf(File file) throws Exception {

        // Copy the file to a temp location to avoid issues with RandomAccessFile and spaces in path names
        File tempFile = FileDirectoryHandler.copyFile2(file, FileDirectoryHandler.getTempFile(".pdf"), true);
        // set up the PDF reading
        RandomAccessFile raf = new RandomAccessFile(tempFile, "r");
        FileChannel channel = raf.getChannel();
        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        PDFFile pdfFile = new PDFFile(buf); // Create PDF Print Page

        // Create Print Job
        PrinterJob pjob = PrinterJob.getPrinterJob();
        PageFormat pf = PrinterJob.getPrinterJob().defaultPage();

        Paper paper = new Paper();
        paper.setSize(594.936, 841.536);
        paper.setImageableArea(0, 0, 594.936, 841.536);
        pf.setPaper(paper);

        Book book = new Book();
        PDFPrintPage pages = new PDFPrintPage(pdfFile);

        book.append(pages, pf, pdfFile.getNumPages());
        pjob.setPageable(book);

        // Set print attributes:
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add(MediaSizeName.ISO_A4);

        try {
            // Send print job to printer
            if (pjob.printDialog(aset)) {
                pjob.print(aset);
                Log.Debug(PrintJob2.class, "Document '" + file + "' printed.");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            raf.close();
        }
    }

    private void printComponent(final Component componentToPrint) {
        PrinterJob job = PrinterJob.getPrinterJob();

        PageFormat pf = PrinterJob.getPrinterJob().defaultPage();

        Paper paper = new Paper();
        paper.setSize(594.936, 841.536);
        paper.setImageableArea(0, 0, 594.936, 841.536);
        pf.setPaper(paper);
        pf.setOrientation(PageFormat.LANDSCAPE);

        Book book = new Book();
        book.append(new java.awt.print.Printable() {

            public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
                if (page > 0) {
                    return NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) g;
                g2d.translate(pf.getImageableX(), pf.getImageableY());
                componentToPrint.printAll(g);

                return PAGE_EXISTS;
            }
        }, pf);

        job.setPageable(book);
        boolean ok = job.printDialog();
        if (ok) {
            try {
                job.print();
            } catch (PrinterException ex) {
                Popup.error(ex);
                Log.Debug(ex);
            }
        }
    }
}

