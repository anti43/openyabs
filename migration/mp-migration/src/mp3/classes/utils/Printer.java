/*
 * 
 *  *  This file is part of MP by anti43 /GPL.
 *  *  
 *  *      MP is free software: you can redistribute it and/or modify
 *  *      it under the terms of the GNU General Public License as published by
 *  *      the Free Software Foundation, either version 3 of the License, or
 *  *      (at your option) any later version.
 *  *  
 *  *      MP is distributed in the hope that it will be useful,
 *  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 * *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *      GNU General Public License for more details.
 *  *  
 *  *      You should have received a copy of the GNU General Public License
 *  *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package mp3.classes.utils;

/**
 *
 * @author anti43
 */
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import java.awt.*;
import java.io.*;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;

public class Printer {

    public static void main(String[] aargs) {

   

    }

    /**
     * 
     * @param file
     * @throws java.lang.Exception
     */
    public void print(File file) throws Exception {

     
            // Open the image file
            InputStream is = new BufferedInputStream(
                    new FileInputStream(file));

            // Find the default service
            DocFlavor flavor = DocFlavor.INPUT_STREAM.PDF;
            PrintService service = PrintServiceLookup.lookupDefaultPrintService();

            // Create the print job
            DocPrintJob job = service.createPrintJob();
            Doc doc = new SimpleDoc(is, flavor, null);

            // Monitor print job events; for the implementation of PrintJobWatcher,
            // see e702 Determining When a Print Job Has Finished
            PrintJobWatcher pjDone = new PrintJobWatcher(job);

            // Print it
            job.print(doc, null);

            // Wait for the print job to be done
            pjDone.waitForDone();
            System.out.println("Printed: "+file.getName());
            // It is now safe to close the input stream
            is.close();
      


    }

    public void printerTest(String filename) {


        PrintRequestAttributeSet pras =
                new HashPrintRequestAttributeSet();
        pras.add(new Copies(1));
        pras.add(MediaName.ISO_A4_WHITE);

        DocFlavor flavor = null;
        PrintService printServices[] = null;
        flavor = DocFlavor.INPUT_STREAM.PDF;
        printServices = PrintServiceLookup.lookupPrintServices(flavor, pras);
        System.out.println("PDF Print Service:");
        for (int i = 0; i < printServices.length; i++) {
            PrintService[] svc = printServices;
            System.out.println((i + 1) + ": " + svc[0].toString());
        }
        flavor = DocFlavor.INPUT_STREAM.PNG;
        printServices = PrintServiceLookup.lookupPrintServices(flavor, pras);
        System.out.println("PNG Print Service:");
        for (int i = 0; i < printServices.length; i++) {
            PrintService[] svc = printServices;
            System.out.println((i + 1) + ": " + svc[0].toString());
        }
        flavor = DocFlavor.INPUT_STREAM.JPEG;
        printServices = PrintServiceLookup.lookupPrintServices(flavor, pras);
        System.out.println("JPEG Print Service:");
        for (int i = 0; i < printServices.length; i++) {
            PrintService[] svc = printServices;
            System.out.println((i + 1) + ": " + svc[0].toString());
        }
    }
    
    
    class PrintJobWatcher {
        // true iff it is safe to close the print job's input stream
        boolean done = false;
    
        PrintJobWatcher(DocPrintJob job) {
            // Add a listener to the print job
            job.addPrintJobListener(new PrintJobAdapter() {
                @Override
                public void printJobCanceled(PrintJobEvent pje) {
                    allDone();
                }
                @Override
                public void printJobCompleted(PrintJobEvent pje) {
                    allDone();
                }
                @Override
                public void printJobFailed(PrintJobEvent pje) {
                    allDone();
                }
                @Override
                public void printJobNoMoreEvents(PrintJobEvent pje) {
                    allDone();
                }
                void allDone() {
                    synchronized (PrintJobWatcher.this) {
                        done = true;
                        
                                
                        PrintJobWatcher.this.notify();
                    }
                }
            });
        }
        public synchronized void waitForDone() {
            try {
                while (!done) {
                    wait();
                }
            } catch (InterruptedException e) {
            }
        }
    }
}

