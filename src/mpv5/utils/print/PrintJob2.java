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
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;
import java.awt.*;
import java.awt.print.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.PrinterName;
import mpv5.globals.GlobalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Notificator;
import mpv5.utils.files.FileDirectoryHandler;

/**
 *
 * @author Torsten Horn, Andreas Weber
 */
public class PrintJob2 {

    /**
     * a paper fitting DIN-A4
     */
    public static Paper DINA4;

    /**
     * Creates a paper fitting DIN-A4
     */
    static {
        Paper paper = new java.awt.print.Paper();
        paper.setSize(595, 842);
        paper.setImageableArea(0, 0, 595, 842);
        DINA4 = paper;
    }

    /**
     * Send an File to a printer
     *
     * @param file
     * @param printer
     * @throws FileNotFoundException
     * @throws Exception
     */
    public static void print(File file, String printer) throws FileNotFoundException, Exception {
        new PrintJob2((file), file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length()), printer);
    }
    /*
     * PrintWithJ2SE14Document.java: Drucken eines Dokuments mit J2SE 1.4
     */

    /**
     * Set up a new printjob and print
     *
     * @param file
     * @param fileType
     * @param printer
     * @throws Exception
     */
    public PrintJob2(File file, String fileType, String printer) throws Exception {
        if (fileType.toUpperCase().equals("PDF")) {
            printPdf(file, printer);
        } else {
            print(new FileInputStream(file), fileType, printer);
        }
    }

    /**
     * Set up a new printjob and print
     *
     * @param file
     * @param fileType
     * @throws Exception
     */
    public PrintJob2(File file, String fileType) throws Exception {
        if (fileType.toUpperCase().equals("PDF")) {
            printPdf(file, null);
        } else {
            print(new FileInputStream(file), fileType, null);
        }
    }

    /**
     * Send an InputStream to a printer
     *
     * @param resourceAsStream
     * @param fileType
     * @param printername (optional)
     * @throws Exception
     */
    public static void print(InputStream resourceAsStream, String fileType, String printername) throws Exception {

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
        AttributeSet aset = new HashAttributeSet();

        aset.add(MediaSizeName.ISO_A4);
        if (printername != null && printername.length() > 0) {
            aset.add(new PrinterName(printername, null));
        }
        HashPrintRequestAttributeSet aset2 = new HashPrintRequestAttributeSet();

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
                prserv = ServiceUI.printDialog(null, 50, 50, prservices, prservDflt, null, aset2);
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
            pj.print(doc, aset2);
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
     *
     * @param c
     */
    public PrintJob2(Component c) {
        printComponent(c);
    }

    public PrintJob2() {
    }

    /**
     *
     * @param file
     * @param printername not used yet
     * @throws Exception
     */
    private void printPdf(File file, String printername) throws Exception {

        // Copy the file to a temp location to avoid issues with RandomAccessFile and spaces in path names
        File tempFile = FileDirectoryHandler.copyFile2(file, FileDirectoryHandler.getTempFile("pdf"), true);
        // set up the PDF reading
        RandomAccessFile raf = new RandomAccessFile(tempFile, "r");
        FileChannel channel = raf.getChannel();
        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        final PDFFile pdfFile = new PDFFile(buf); // Create PDF Print Page

        // Create Print Job
        PrinterJob pjob = PrinterJob.getPrinterJob();
        PageFormat pf = PrinterJob.getPrinterJob().defaultPage();

        Paper paper = DINA4;
        pf.setOrientation(PageFormat.PORTRAIT);
        pf.setPaper(paper);

        Book book = new Book();

        book.append(new java.awt.print.Printable() {

            @Override
            public int print(Graphics g, PageFormat format, int index) throws PrinterException {
                int pagenum = index + 1;

                if ((pagenum >= 1) && (pagenum <= pdfFile.getNumPages())) {
                    try {
                        Graphics2D g2 = (Graphics2D) g;
                        PDFPage page = pdfFile.getPage(pagenum);
                        double pwidth = format.getImageableWidth();
                        double pheight = format.getImageableHeight();
                        double aspect = page.getAspectRatio();
                        double paperaspect = pwidth / pheight;
                        Rectangle imgbounds;
                        int width;
                        int height;
                        if (aspect > paperaspect) {
                            //fit paper
                            height = (int) (pwidth / aspect);
                            width = (int) pwidth;
                        } else {
                            width = (int) (pheight * aspect);
                            height = (int) pheight;
                        }
                        imgbounds = new Rectangle((int) format.getImageableX(), (int) format.getImageableY(), width, height);
                        PDFRenderer pgs = new PDFRenderer(page, g2, imgbounds, null, null);
                        try {
                            page.waitForFinish();
                            pgs.run();
                        } catch (InterruptedException ie) {
                        }
                        return PAGE_EXISTS;
                    } catch (Exception ex) {
                        Log.Debug(ex);
                        return NO_SUCH_PAGE;
                    }
                } else {
                    return NO_SUCH_PAGE;
                }
            }
        }, pf, pdfFile.getNumPages());
        pjob.setPageable(book);
        try {
            print0(pjob, printername);
            Notificator.raiseNotification(Messages.PRINTED + " .pdf " + " [" + pjob.getPrintService().getName() + "]", false);
            Log.Debug(PrintJob2.class, "Document '" + file + "' printed.");
        } catch (Exception e) {
            throw e;
        } finally {
            raf.close();
        }
    }

    private void printComponent(final Component componentToPrint) {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf = job.defaultPage();

        Paper paper = DINA4;
        double leftMargin = 0.78;
        double rightMargin = 0.78;
        double topMargin = 0.78;
        double bottomMargin = 0.78;
        paper.setImageableArea(leftMargin * 72.0, topMargin * 72.0,
                (paper.getWidth() - leftMargin - rightMargin) * 72.0,
                (paper.getHeight() - topMargin - bottomMargin) * 72.0);
        pf.setPaper(paper);
        pf.setOrientation(PageFormat.LANDSCAPE);

        Book book = new Book();
        book.append(new java.awt.print.Printable() {

            public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
                if (page > 0) {
                    return NO_SUCH_PAGE;
                }

//                Graphics2D g2d = (Graphics2D) g;
//                g2d.translate(pf.getImageableX(), pf.getImageableY());
//                componentToPrint.printAll(g);
//                return PAGE_EXISTS;
                return PrintJob2.print(componentToPrint, g, pf, page);
            }
        }, pf);

        job.setPageable(book);
        try {
            print0(job, null);
        } catch (PrinterException ex) {
            Log.Debug(ex);
        }

//        if (job.printDialog()) {
//            try {
//                job.print();
//            } catch (PrinterException ex) {
//                Popup.error(ex);
//                Log.Debug(ex);
//            }
//        }
    }

    /**
     * From http://www.coderanch.com/t/340021/GUI/java/Printing-JPanel-printer
     */
    private static int print(Component componentToBePrinted, Graphics g, PageFormat pageFormat, int pageIndex) {
        synchronized (componentToBePrinted) {
            if (pageIndex > 0) {
                return java.awt.print.Printable.NO_SUCH_PAGE;
            } else {
                try {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
// scale to fill the page        
                    double dw = pageFormat.getImageableWidth();
                    double dh = pageFormat.getImageableHeight();
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

                    double xScale = dw / screenSize.width;
                    double yScale = dh / screenSize.height;
                    double scale = Math.min(xScale, yScale);

// center the chart on the page
                    double tx = 0.0;
                    double ty = 0.0;
                    if (xScale > scale) {
                        tx = 0.5 * (xScale - scale) * screenSize.width;
                    } else {
                        ty = 0.5 * (yScale - scale) * screenSize.height;
                    }
                    g2d.translate(tx, ty);
                    g2d.scale(scale, scale);

                    componentToBePrinted.printAll(g2d);
                } catch (Exception exception) {
                    Log.Debug(exception);
                }
                return java.awt.print.Printable.PAGE_EXISTS;
            }
        }
    }

    private void print0(PrinterJob pjob, String printername) throws PrinterException {
        // Set print attributes:
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add(MediaSizeName.ISO_A4);

//        AttributeSet aset = new HashAttributeSet();
//aset.add(new PrinterName("\\\\122.166.97.138//Canon iP1300", null));

        HashAttributeSet aset2 = new HashAttributeSet();
        aset2.add(MediaSizeName.ISO_A4);
        if (printername != null && printername.length() > 0 && !printername.contains("undefined")) {
            aset2.add(new PrinterName(printername, null));
            PrintService[] services3 = PrintServiceLookup.lookupPrintServices(null, aset2);
            for (int i = 0; i < services3.length; i++) {
                PrintService printService = services3[i];
                Log.Debug(this, printService.getName());
            }
            if (services3.length > 0) {
                pjob.setPrintService(services3[0]);
            } else {
                Notificator.raiseNotification(Messages.NO_PRINTER_FOUND + " [" + printername + "]", false);
            }
        }
        // Send print job to printer
        if (GlobalSettings.getBooleanProperty("org.openyabs.printproperty.printdirect", false)
                || pjob.printDialog(aset)) {
            pjob.print(aset);
        }
    }
}
