package mp4.utils.export.druck;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.swing.RepaintManager;
import mp4.interfaces.Printable;
import mp4.interfaces.Waiter;
import mp4.logs.*;
import mp4.main.Main;


/**
 *
 * @author anti43
 */
public class DruckJob implements Waiter {

    private PrintService prservDflt;
    private PrintService[] prservices;
    int idxPrintService = -1;
    private HashPrintRequestAttributeSet aset;
    private DocFlavor flavor;
    public static boolean FORCE_WIN_PRINT = false;

    public DruckJob() {
        aset = new HashPrintRequestAttributeSet();
        aset.add(MediaSizeName.ISO_A4);
        this.flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        prservDflt = PrintServiceLookup.lookupDefaultPrintService();
        prservices = PrintServiceLookup.lookupPrintServices(flavor, aset);
    }

    public DruckJob(DocFlavor flavor) {
        aset = new HashPrintRequestAttributeSet();
        aset.add(MediaSizeName.ISO_A4);
        this.flavor = flavor;
        prservDflt = PrintServiceLookup.lookupDefaultPrintService();
        prservices = PrintServiceLookup.lookupPrintServices(flavor, aset);

    }

    public void print(ArrayList<File> filelist) {
        if (null == prservices || 0 >= prservices.length) {
            if (null != prservDflt) {
                System.err.println("Nur Default-Printer, da lookupPrintServices fehlgeschlagen.");
                prservices = new PrintService[]{prservDflt};
            }
        }
        Log.Debug(this,"Print-Services:");
        int i;
        for (i = 0; i < prservices.length; i++) {
            Log.Debug(this,"  " + i + ":  " + prservices[i] + ((prservDflt != prservices[i]) ? "" : " (Default)"));
        }
        PrintService prserv = null;
        if (0 <= idxPrintService && idxPrintService < prservices.length) {
            prserv = prservices[idxPrintService];
        } else {
            if (!Arrays.asList(prservices).contains(prservDflt)) {
                prservDflt = null;
            }
            if(prservices == null)prservices = new PrintService[]{PrintServiceLookup.lookupDefaultPrintService()};
            prserv = ServiceUI.printDialog(null, 50, 50, prservices, prservDflt, null, aset);
        }
        if (null != prserv) {
            Log.Debug(this,"Ausgewaehlter Print-Service:");
            Log.Debug(this,"      " + prserv);
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

    /*
     * Prints a File
     */
    public void print(File file) throws FileNotFoundException, PrintException {

        if (null == prservices || 0 >= prservices.length) {
            if (null != prservDflt) {
                System.err.println("Nur Default-Printer, da lookupPrintServices fehlgeschlagen.");
                prservices = new PrintService[]{prservDflt};
            }
        }
        Log.Debug(this,"Print-Services:");
        int i;
        for (i = 0; i < prservices.length; i++) {
            Log.Debug(this,"  " + i + ":  " + prservices[i] + ((prservDflt != prservices[i]) ? "" : " (Default)"));
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
            Log.Debug(this,"Ausgewaehlter Print-Service:");
            Log.Debug(this,"      " + prserv);
            printPrintServiceAttributesAndDocFlavors(prserv);
            DocPrintJob pj = prserv.createPrintJob();
            FileInputStream fis = new FileInputStream(file);
            Doc doc = new SimpleDoc(fis, flavor, null);
            pj.print(doc, aset);
        }

    }

    /*
     * Prints a mp4.interfaces.Printable Object
     */
    private void print(Printable printable) {
        this.flavor = printable.getFlavor();
        try {
            if ((flavor == DocFlavor.INPUT_STREAM.PDF && Main.IS_WINDOWS)) {
                printWIN(printable.getFile());
            } else {
                print(printable.getFile());
            }
        } catch (FileNotFoundException fileNotFoundException) {
            Log.Debug(this,fileNotFoundException);
        } catch (PrintException printException) {
            Log.Debug(this,printException);
        }

    }

    public void printWIN(File file) {
        try {
            Log.Debug("Forcing WINPrint through Adobe Reader, because Java is not able to print a PDF directly in Windows :-(");
            if (file.getName().contains("pdf")) {
                Runtime.getRuntime().exec("cmd.exe /C start acrord32 /P /h" + file.getPath());
            } else {
                print(file);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void printPrintServiceAttributesAndDocFlavors(PrintService prserv) {
        String s1 = null, s2;
        Attribute[] prattr = prserv.getAttributes().toArray();
        DocFlavor[] prdfl = prserv.getSupportedDocFlavors();
        if (null != prattr && 0 < prattr.length) {
            for (int i = 0; i < prattr.length; i++) {
                Log.Debug(this,"      PrintService-Attribute[" + i + "]: " + prattr[i].getName() + " = " + prattr[i]);
            }
        }
        if (null != prdfl && 0 < prdfl.length) {
            for (int i = 0; i < prdfl.length; i++) {
                s2 = prdfl[i].getMimeType();
                if (null != s2 && !s2.equals(s1)) {
                    Log.Debug(this,"      PrintService-DocFlavor-Mime[" + i + "]: " + s2);
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
                if (FORCE_WIN_PRINT) {
                    printWIN((File) object);
                } else {
                    print((File) object);
                }
            }
        } catch (FileNotFoundException fileNotFoundException) {
            Log.Debug(this,fileNotFoundException);
        } catch (PrintException printException) {
            Log.Debug(this,printException);
        }
    }
    
    public void printPDF(File file){
        try {

            RandomAccessFile raf = new RandomAccessFile(file, "r");
            FileChannel channel = raf.getChannel();
            ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            PDFFile pdffile = new PDFFile(buf);

            // get the first page
            PDFPage page = pdffile.getPage(0);


            // create and configure a graphics object
            BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // do the actual drawing
            PDFRenderer renderer = new PDFRenderer(page, g2, new Rectangle(0, 0, 500, 500), null, Color.RED);
            page.waitForFinish();
            renderer.run();
          
            
        } catch (Exception ex) {
            Log.Debug(ex);
        }

    }
    
    
}

class PrintUtilities implements java.awt.print.Printable {
  private Component componentToBePrinted;

  public static void printComponent(Component c) {
    new PrintUtilities(c).print();
  }
  
  public PrintUtilities(Component componentToBePrinted) {
    this.componentToBePrinted = componentToBePrinted;
  }
  
  public void print() {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    printJob.setPrintable((java.awt.print.Printable) this);
    if (printJob.printDialog()) {
            try {
                printJob.print();
            } catch (PrinterException pe) {
                System.out.println("Error printing: " + pe);
            }
        }
  }

  public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
    if (pageIndex > 0) {
      return(1);
    } else {
      Graphics2D g2d = (Graphics2D)g;
      g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
      disableDoubleBuffering(componentToBePrinted);
      componentToBePrinted.paint(g2d);
      enableDoubleBuffering(componentToBePrinted);
      return(0);
    }
  }

  public static void disableDoubleBuffering(Component c) {
    RepaintManager currentManager = RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(false);
  }

  public static void enableDoubleBuffering(Component c) {
    RepaintManager currentManager = RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(true);
  }

}

