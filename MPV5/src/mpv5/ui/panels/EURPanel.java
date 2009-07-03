/*
 * EURPanel.java
 *
 * Created on 21. Februar 2008, 21:50
 */
package mpv5.ui.panels;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import mpv5.logging.Log;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.utils.date.DateConverter;
import mpv5.utils.files.TableHtmlWriter;
import mpv5.utils.models.DateSelectorModel;
import mpv5.utils.models.EURModel;
import mpv5.utils.tables.TableFormat;

/**
 *
 * @author  anti43
 */
public class EURPanel extends JPanel {

  private DateSelectorModel dateModel = new DateSelectorModel();
  private EURModel eurModel;
  private String html;
  private JEditorPane formPane = new JEditorPane();

  public EURPanel() {
    initComponents();

    eurModel = new EURModel(dateModel);
    if (eurModel.isSkr() != true) {
      jButton2.setVisible(false);
    }
    jTable1.setModel(eurModel);
  }

  private String[] getYears() {
    String[] ys = new String[7];
    int actYear = Integer.parseInt(DateConverter.getYear());
    for (int i = 0; i < ys.length; i++) {
      ys[i] = (actYear - 3 + i) + "";
    }
    return ys;
  }

  private void showPane() {
    formPane.setContentType("text/html");
    formPane.setEditable(false);
    jScrollPane1.setViewportView(formPane);
    formPane.setBackground(java.awt.Color.WHITE);
    formPane.setText(html.substring(38));
    jTable1.setVisible(false);
    formPane.setVisible(true);
  }

  private void showTable() {
    jScrollPane1.setViewportView(jTable1);
    formPane.setVisible(false);
    jTable1.setVisible(true);
    eurModel.getTableHtml();
jTextField1.setText(dateModel.getStartDay() + "   -   " + dateModel.getEndDay());
    try {
      jTable1.setModel(eurModel.getModel());
      TableFormat.format(jTable1, 0, 380);
      jTable1.doLayout();

      jButton2.setEnabled(true);
      jButton3.setEnabled(true);

    } catch (Exception ex) {
      ex.printStackTrace();
      Log.Debug(ex);
    }
  }

  private void printToFile() {
    String filename = "";

    try {
//          TableHtmlWriter writ = new TableHtmlWriter((DefaultTableModel) jTable1.getModel(), new File(month + "_" + year + ".html"), "Beschreibung,Daten".split(","), "Einnahmen / Ausgaben " + month + "/" + year);
//
//          dialog = new DialogForFile(new File(Main.DESKTOP + File.separator + writ.getFile().getName()));
//          dialog.saveFile(writ.createHtml(1, Color.LIGHT_GRAY));
//          new Browser(dialog.getFile());
      if (eurModel.isSkr()) {
        eurModel.getPdf();
      } else {

        TableHtmlWriter thw = new TableHtmlWriter(eurModel);
        thw.setHeader(eurModel.getHeader());
        File tempfile = thw.createHtml();
        DialogForFile d = new DialogForFile();
        d.setFileFilter(DialogForFile.PDF_FILES);
        d.saveFile(tempfile);

//                OutputStream os = new FileOutputStream(tempfile);
//          ITextRenderer renderer = new ITextRenderer();
//          renderer.setDocument(url);
//          renderer.layout();
//          renderer.createPDF(os);
//          os.close();
      }

    } catch (Exception ex) {
      Log.Debug(this, ex);
    }
  }

  /**
   * Gets the final HTML form from the XML Parser and opens it in the
   * JEditorPane. The JEditorPane needs some older HTML code, so the
   * XML input stream has to be modified.
   * The PDF parser needs valid XHTML as it comes from the XML
   * input stream.
   */
  private void showHtmlForm() {
    ByteArrayOutputStream os = eurModel.getFormHtml();
    InputStream in = new ByteArrayInputStream(os.toByteArray());
    try {
      html = streamToString(in);
      showPane();
      in.close();
      os.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    formPane.setCaretPosition(0);
  }

  private String streamToString(InputStream in) throws IOException {
    StringBuilder sb = new StringBuilder();
    byte[] b = new byte[4096];
    for (int n; (n = in.read(b)) != -1;) {
      sb.append(new String(b, 0, n));
    }
    return sb.toString();
  }


  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jPanel1 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    jComboBox1 = new javax.swing.JComboBox();
    jButton1 = new javax.swing.JButton();
    jButton2 = new javax.swing.JButton();
    jButton3 = new javax.swing.JButton();
    jLabel2 = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    jTable1 = new javax.swing.JTable();
    jComboBox2 = new javax.swing.JComboBox();
    jTextField1 = new javax.swing.JTextField();

    jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Einnahmen-Überschuss Rechnung: Alle Angaben ohne Gewähr."));

    jLabel1.setText("Zeitraum:");

    jComboBox1.setModel(dateModel.getModel());

    jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/kspread.png"))); // NOI18N
    jButton1.setToolTipText("Show Data");
    jButton1.setContentAreaFilled(false);
    jButton1.setFocusable(false);
    jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    jButton1.setMaximumSize(new java.awt.Dimension(44, 44));
    jButton1.setMinimumSize(new java.awt.Dimension(44, 44));
    jButton1.setPreferredSize(new java.awt.Dimension(44, 44));
    jButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton1ActionPerformed(evt);
      }
    });

    jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/knode.png"))); // NOI18N
    jButton2.setToolTipText("Form Preview");
    jButton2.setContentAreaFilled(false);
    jButton2.setEnabled(false);
    jButton2.setFocusable(false);
    jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    jButton2.setMaximumSize(new java.awt.Dimension(44, 44));
    jButton2.setMinimumSize(new java.awt.Dimension(44, 44));
    jButton2.setPreferredSize(new java.awt.Dimension(44, 44));
    jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    jButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton2ActionPerformed(evt);
      }
    });

    jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/3floppy_unmount.png"))); // NOI18N
    jButton3.setMnemonic('P');
    jButton3.setToolTipText("Save (PDF)");
    jButton3.setContentAreaFilled(false);
    jButton3.setEnabled(false);
    jButton3.setFocusable(false);
    jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    jButton3.setMaximumSize(new java.awt.Dimension(44, 44));
    jButton3.setMinimumSize(new java.awt.Dimension(44, 44));
    jButton3.setPreferredSize(new java.awt.Dimension(44, 44));
    jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    jButton3.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton3ActionPerformed(evt);
      }
    });

    jLabel2.setText("Zahlen:");

    jLabel3.setText("Achtung! Überprüfen Sie die Daten in jedem Fall auf Richtigkeit, Vollständigkeit und Plausibilität!");

    jTable1.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "Gruppe", "Betrag"
      }
    ) {
      boolean[] canEdit = new boolean [] {
        false, false
      };

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    jTable1.setAutoscrolls(false);
    jTable1.setEnabled(false);
    jTable1.setRowSelectionAllowed(false);
    jScrollPane1.setViewportView(jTable1);

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
    );
    jPanel2Layout.setVerticalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
        .addContainerGap())
    );

    jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(getYears()));
    jComboBox2.setSelectedIndex(3);
    jComboBox2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jComboBox2ActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel3)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel1)
              .addComponent(jLabel2))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 104, Short.MAX_VALUE))
              .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE))))
        .addContainerGap())
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(jButton3, 0, 0, Short.MAX_VALUE)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(jLabel1)
              .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
          .addComponent(jButton2, 0, 0, Short.MAX_VALUE)
          .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, Short.MAX_VALUE))
        .addGap(14, 14, 14)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel3))
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
  }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
      showTable();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
      showHtmlForm();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
      printToFile();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
      dateModel.setYear((String) jComboBox2.getSelectedItem());
    }//GEN-LAST:event_jComboBox2ActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton jButton1;
  private javax.swing.JButton jButton2;
  private javax.swing.JButton jButton3;
  private javax.swing.JComboBox jComboBox1;
  private javax.swing.JComboBox jComboBox2;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JTable jTable1;
  private javax.swing.JTextField jTextField1;
  // End of variables declaration//GEN-END:variables
}
