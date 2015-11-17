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
/*
 * SubmitForm.java
 *
 * Created on 05.08.2009, 11:28:47
 */
package mpv5.bugtracker;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.globals.Constants;
import mpv5.logging.Log;
import mpv5.ui.dialogs.BigPopup;
import mpv5.ui.dialogs.Popup;

/**
 *
 */
public class SubmitForm extends javax.swing.JPanel {

    /** Creates new form SubmitForm
     * @param exceptions
     */
    public SubmitForm(List<Exception> exceptions) {
        initComponents();

        String exc = "";
        for (int i = exceptions.size()-1; i >=0 ; i--) {
            Exception exception = exceptions.get(i);
            exc += "Exception " + i + ":\n";
            exc += Log.getStackTrace(exception);
            try {
                exc += "\nCaused by:\n";
                exc += Log.getStackTrace(exception.getCause());
            } catch (Exception e) {
            }
            exc += "\n\n";
        }

        trace.setText(exc);
        trace.setSelectionStart(0);
        trace.setSelectionEnd(0);
        validate();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        description = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        steps = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        trace = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        optional = new javax.swing.JTextArea();
        google = new javax.swing.JCheckBox();

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("SubmitForm.border.title"))); // NOI18N
        setName("Form"); // NOI18N

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

//$2java.awt.Color(238, 238, 238));
        description.setColumns(20);
        description.setEditable(false);
        description.setLineWrap(true);
        description.setRows(5);
        description.setText(bundle.getString("SubmitForm.description.text")); // NOI18N
        description.setWrapStyleWord(true);
        description.setName("description"); // NOI18N
        jScrollPane1.setViewportView(description);

        jLabel1.setText(bundle.getString("SubmitForm.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        steps.setColumns(20);
        steps.setLineWrap(true);
        steps.setRows(5);
        steps.setWrapStyleWord(true);
        steps.setName("steps"); // NOI18N
        jScrollPane2.setViewportView(steps);

        jLabel2.setText(bundle.getString("SubmitForm.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        trace.setColumns(20);
        trace.setEditable(false);
        trace.setLineWrap(true);
        trace.setRows(5);
        trace.setWrapStyleWord(true);
        trace.setName("trace"); // NOI18N
        jScrollPane3.setViewportView(trace);

        jButton1.setText(bundle.getString("SubmitForm.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jScrollPane4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jScrollPane4.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane4.setName("jScrollPane4"); // NOI18N

//$2java.awt.Color(238, 238, 238));
        optional.setColumns(20);
        optional.setEditable(false);
        optional.setLineWrap(true);
        optional.setRows(5);
        optional.setText(bundle.getString("SubmitForm.optional.text")); // NOI18N
        optional.setWrapStyleWord(true);
        optional.setName("optional"); // NOI18N
        jScrollPane4.setViewportView(optional);

        google.setSelected(true);
        google.setText(bundle.getString("SubmitForm.google.text")); // NOI18N
        google.setName("google"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(google, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(google)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        if (google.isSelected()) {
            try {
                String message = "This is a generated issue report for Yabs Version " + Constants.VERSION;
                message += "\n\n" + steps.getText() + "\n\n" + trace.getText();
                message = URLEncoder.encode(message, "UTF-8");
                if (message.length() > 1000) {
                    message = message.substring(0, 999);
                    message+="CUTOFF";
                }

                String url = "http://code.google.com/p/mp-rechnungs-und-kundenverwaltung/issues/entry?" +
                        "labels=Type-Defect,Priority-Medium,Milestone-Release_" + Constants.VERSION +
                        "&template=User+defect+report&summary=Unexpected+error+in+YaBS" +
                        "+Version+" + Constants.VERSION + "&" +
                        "comment=" + message +
                        "";

                Desktop.getDesktop().browse(new URI(url));
                BigPopup.close(this);
            } catch (Exception x) {
                Log.Debug(x);
            }
        } else {
            String message = "This is a generated issue report for Yabs Version " + Constants.VERSION;
            message += "\n\n" + steps.getText() + "\n\n" + trace.getText();
            Popup.notice("Please send this message to openyabs@googlegroups.com: \n\nThank you!\n\n" +
                    message, 500, 500);
            try {
                BigPopup.close(this);
            } catch (Exception ex) {}
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea description;
    private javax.swing.JCheckBox google;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextArea optional;
    private javax.swing.JTextArea steps;
    private javax.swing.JTextArea trace;
    // End of variables declaration//GEN-END:variables
}
