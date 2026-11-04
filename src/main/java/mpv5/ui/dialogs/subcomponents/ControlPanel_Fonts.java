package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import java.awt.Font;
import javax.swing.UIManager;
import mpv5.data.PropertyStore;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;

/**
 *
 * 
 */
public class ControlPanel_Fonts extends javax.swing.JPanel implements ControlApplet {

    private static final long serialVersionUID = -8347532498124147821L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "fonts";
    private PropertyStore oldvalues;
    private static ControlPanel_Fonts ident;

    public ControlPanel_Fonts() {
        initComponents();

        font.setText(LocalSettings.getProperty(LocalSettings.DEFAULT_FONT));
        jFontChooser1.setSelectedFont(Font.decode(LocalSettings.getProperty(LocalSettings.DEFAULT_FONT)));

        validate();
    }

    /**
     * Applies the give font
     * @param font
     */
    public static void applyFont(Font font) {
        UIManager.put("Button.font", font);
        UIManager.put("ToggleButton.font", font);
        UIManager.put("RadioButton.font", font);
        UIManager.put("CheckBox.font", font);
        UIManager.put("ColorChooser.font", font);
        UIManager.put("ComboBox.font", font);
        UIManager.put("Label.font", font);
        UIManager.put("List.font", font);
        UIManager.put("MenuBar.font", font);
        UIManager.put("MenuItem.font", font);
        UIManager.put("RadioButtonMenuItem.font", font);
        UIManager.put("CheckBoxMenuItem.font", font);
        UIManager.put("Menu.font", font);
        UIManager.put("PopupMenu.font", font);
        UIManager.put("OptionPane.font", font);
        UIManager.put("Panel.font", font);
        UIManager.put("ProgressBar.font", font);
        UIManager.put("ScrollPane.font", font);
        UIManager.put("Viewport.font", font);
        UIManager.put("TabbedPane.font", font);
        UIManager.put("Table.font", font);
        UIManager.put("TableHeader.font", font);
        UIManager.put("TextField.font", font);
        UIManager.put("PasswordField.font", font);
        UIManager.put("TextArea.font", font);
        UIManager.put("TextPane.font", font);
        UIManager.put("EditorPane.font", font);
        UIManager.put("TitledBorder.font", font);
        UIManager.put("ToolBar.font", font);
        UIManager.put("ToolTip.font", font);
        UIManager.put("Tree.font", font);

        if (mpv5.YabsViewProxy.instance().getIdentifierFrame() != null) {
            mpv5.YabsViewProxy.instance().getIdentifierFrame().validate();
        }
    }

    private String getFontString(Font font) {
        return font.getFontName() + " " + font.getSize();
    }

    private String getFontString(String string) {
        return Font.decode(string).getFontName() + " " + Font.decode(string).getSize();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        font = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jFontChooser1 = new com.l2fprod.common.swing.JFontChooser();
        jPanel6 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        //\$2java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

//$2java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Fonts.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setText(bundle.getString("ControlPanel_Fonts.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        font.setText(bundle.getString("ControlPanel_Fonts.font.text")); // NOI18N
        font.setName("font"); // NOI18N

        jLabel2.setText(bundle.getString("ControlPanel_Fonts.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jFontChooser1.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        jFontChooser1.setName("jFontChooser1"); // NOI18N
        jFontChooser1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jFontChooser1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(font, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jFontChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(font, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jFontChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);

//$2java.awt.Color(255, 255, 255));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton2.setText(bundle.getString("ControlPanel_Fonts.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton2);

        jButton1.setText(bundle.getString("ControlPanel_Fonts.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton1);

        add(jPanel6, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        if (jFontChooser1.getSelectedFont() != null) {
            font.setText(getFontString(jFontChooser1.getSelectedFont()));
            setSettings();
        }

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setSettings();
        LocalSettings.save();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jFontChooser1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jFontChooser1MouseClicked
        font.setText(getFontString(jFontChooser1.getSelectedFont()));
    }//GEN-LAST:event_jFontChooser1MouseClicked

    public void setValues(PropertyStore values) {
        oldvalues = values;
        font.setText(values.getProperty(LocalSettings.DEFAULT_FONT));
    }

    public String getUname() {
        return UNAME;
    }

    public void reset() {
        setValues(oldvalues);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField font;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private com.l2fprod.common.swing.JFontChooser jFontChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel6;
    // End of variables declaration//GEN-END:variables

    private void setSettings() {

        if (font.getText().equalsIgnoreCase(getFontString(Font.decode(null)))) {
            LocalSettings.setProperty(LocalSettings.DEFAULT_FONT, getFontString(jFontChooser1.getSelectedFont()));

        } else if (font.getText() != null && font.getText().length() > 0 &&
                !(getFontString(font.getText())).equalsIgnoreCase(getFontString(Font.decode(null)))) {
            LocalSettings.setProperty(LocalSettings.DEFAULT_FONT, getFontString(font.getText()));

        } else {
            Popup.notice(Messages.VALUE_NOT_VALID + font.getText());
        }

        jFontChooser1.setSelectedFont(Font.decode(LocalSettings.getProperty(LocalSettings.DEFAULT_FONT)));
        font.setText(getFontString(jFontChooser1.getSelectedFont()));

        applyFont(Font.decode(getFontString(font.getText())));

    }


    public Component getAndRemoveActionPanel() {
         this.remove(jPanel6);validate();
        return jPanel6;
    }
}
