package mpv5.ui.dialogs.subcomponents;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import mpv5.YabsViewProxy;
import mpv5.data.PropertyStore;
import mpv5.db.common.QueryHandler;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;
import mpv5.ui.popups.CopyPasteMenu;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileReaderWriter;
import org.codehaus.groovy.control.CompilationFailedException;

/**
 *
 * 
 */
public class ControlPanel_Konsole extends javax.swing.JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "console";
    private PropertyStore oldvalues;
    private static ControlPanel_Konsole ident;
    private GroovyShell groovyShell;

    public ControlPanel_Konsole() {
        if (MPSecurityManager.checkAdminAccess()) {
            initComponents();
            new CopyPasteMenu(jTextArea1);
            setVisible(true);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Konsole.border.title"))); // NOI18N
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setName("jTextArea1"); // NOI18N
        jTextArea1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextArea1KeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTextArea1);

        add(jScrollPane1, java.awt.BorderLayout.NORTH);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jTextArea2.setText(bundle.getString("ControlPanel_Konsole.jTextArea2.text")); // NOI18N
        jTextArea2.setName("jTextArea2"); // NOI18N
        jScrollPane2.setViewportView(jTextArea2);

        add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton3.setText(bundle.getString("ControlPanel_Konsole.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton3);

        jButton2.setText(bundle.getString("ControlPanel_Konsole.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton2);

        jButton1.setText(bundle.getString("ControlPanel_Konsole.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1);

        add(jPanel2, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void jTextArea1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            process();
        }
    }//GEN-LAST:event_jTextArea1KeyPressed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        File tf = FileDirectoryHandler.getTempFile("mp_konsole_export", "txt");
        FileReaderWriter f = new FileReaderWriter(tf);
        f.write(jTextArea1.getText());
        mpv5.YabsViewProxy.instance().showFilesaveDialogFor(tf);

}//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        jTextArea1.setText("");
        jTextArea2.setText("");
}//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        process();
}//GEN-LAST:event_jButton1ActionPerformed

    public void setValues(PropertyStore values) {
    }

    public String getUname() {
        return UNAME;
    }

    public void reset() {
        setValues(oldvalues);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    // End of variables declaration//GEN-END:variables

    @Override
    public Component getAndRemoveActionPanel() {
        this.remove(jPanel2);
        validate();
        return jPanel2;
    }

    private void process() {
        if (MPSecurityManager.checkAdminAccess()) {
            if (jTextArea1.getText().startsWith("groovy")) {
                runGroovy();
            } else {
                runSQL();
            }
            jTextArea1.setCaretPosition(0);
        }}
    

    private void runSQL() {
        for (int i = 0; i < jTextArea1.getLineCount(); i++) {
            try {
                String command = jTextArea1.getText().substring(jTextArea1.getLineStartOffset(i), jTextArea1.getLineEndOffset(i));
                if (command.contains(";")) {
                    command = command.substring(0, command.lastIndexOf(";"));
                }
                if (command.length() > 1) {
                    if (command.toLowerCase().contains("update")) {
                        QueryHandler.getConnection().freeUpdateQuery(command, jTextArea2, MPSecurityManager.CREATE_OR_DELETE, Messages.SEE_LOG.getValue());
                    } else {
                        QueryHandler.getConnection().freeQuery(command, jTextArea2, MPSecurityManager.CREATE_OR_DELETE, Messages.SEE_LOG.getValue());
                    }
                }
                jTextArea1.replaceRange(command + ";--ok", jTextArea1.getLineStartOffset(i), jTextArea1.getLineEndOffset(i));
            } catch (Exception ex) {
                Log.Debug(this, ex.getMessage());
            }
        }
    }

    private void runGroovy() {
        try {
            jTextArea2.setText(String.valueOf(
                    getGroovyShell().evaluate(jTextArea1.getText().replace("groovy", ""))));
            jTextArea1.setText(null);
        } catch (Exception exception) {
            Log.Debug(exception);
            jTextArea2.setText(exception.getMessage());
        }
    }

    private GroovyShell getGroovyShell() {
        synchronized (this) {
            if (groovyShell == null) {
                Binding binding = new Binding();
                binding.setVariable("db", QueryHandler.instanceOf());
                groovyShell = new GroovyShell(binding);
            }
            return groovyShell;
        }
    }
}
