/*
 * PdfVorschauWindow.java
 *
 * Created on July 30, 2008, 2:23 PM
 */
package mp4.frames;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PagePanel;
import mp4.utils.export.pdf.PDF_Produkt;
import java.awt.BorderLayout;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import mp4.interfaces.Waiter;
import mp4.logs.*;
import mp4.utils.ui.Position;


/**
 *
 * @author  anti43
 */
public class PdfVorschauWindow extends javax.swing.JFrame implements Waiter{

    private File file;
    

    /** Creates new form PdfVorschauWindow
     * @param pdfFile 
     */
    public PdfVorschauWindow(File pdfFile) {

        this.file = pdfFile;
           
        try {
            initComponents();
            PagePanel panel = new PagePanel();
            this.jPanel1.add(panel, BorderLayout.CENTER);

            RandomAccessFile raf = new RandomAccessFile(pdfFile, "r");
            FileChannel channel = raf.getChannel();
            ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY,
                    0, channel.size());
            PDFFile pdffile = new PDFFile(buf);
            
            
            this.pack();
            new Position(this);
            setVisible(true);
            
            
            // show the first page
            PDFPage page = pdffile.getPage(0);
            panel.showPage(page);
            panel.useZoomTool(true);
            
           
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }

    public PdfVorschauWindow() {
     
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PDF Vorschau");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jButton1.setText("Drucken");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Schliessen");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(196, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
  
    file.delete();
    this.dispose();
}//GEN-LAST:event_jButton2ActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
//    try {
//        new DruckJob(DruckJob.PDF).print(file);
//    } catch (FileNotFoundException fileNotFoundException) {
//    } catch (PrintException printException) {
//    }
}//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

public void set(Object object){

        file = ((mp4.utils.export.pdf.PDFFile) object);
           
        try {
            initComponents();
            PagePanel panel = new PagePanel();
            this.jPanel1.add(panel, BorderLayout.CENTER);

            RandomAccessFile raf = new RandomAccessFile(file, "r");
            
            FileChannel channel = raf.getChannel();
            ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY,
                    0, channel.size());
            PDFFile pdffile = new PDFFile(buf);
            
            this.pack();
            new Position(this);
            setVisible(true);
            
            // show the first page
            PDFPage page = pdffile.getPage(0);
            panel.showPage(page);
            panel.useZoomTool(true);
            
           
        } catch (Exception ex) {
            Log.Debug(ex);
        }
}


}
