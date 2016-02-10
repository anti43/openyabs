package mpv5.ui.dialogs.subcomponents;

import enoa.handler.TemplateHandler;
import java.awt.Cursor;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JToolBar;
import mpv5.Main;
import mpv5.YabsViewProxy;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryCriteria2;
import mpv5.db.common.QueryData;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.QueryParameter;
import mpv5.db.common.ReturnValue;
import mpv5.db.common.SaveString;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;
import mpv5.ui.frames.MPView;
import mpv5.ui.panels.ChangeNotApprovedException;
import mpv5.ui.panels.DataPanel;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.models.MPComboBoxModelItem;

/**
 *
 *
 */
public class wizard_FirstSettings4 extends javax.swing.JPanel implements Wizardable, DataPanel {

    private static final long serialVersionUID = -8347532498124147821L;
    private WizardMaster master;
    private PropertyStore nactions;
    private List<JToolBar> nbars;

    public wizard_FirstSettings4(WizardMaster w) {
        this.master = w;
        initComponents();
        setModels();

    }

   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jPanel1 = new javax.swing.JPanel();
      jPanel2 = new javax.swing.JPanel();
      jScrollPane1 = new javax.swing.JScrollPane();
      jTextPane1 = new javax.swing.JTextPane();
      labeledTextChooser2 = new mpv5.ui.beans.LabeledTextChooser();

      //\$2java.awt.Color(255, 255, 255));
      setName("Form"); // NOI18N
      setLayout(new java.awt.BorderLayout());

//$2java.awt.Color(255, 255, 255));
      java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();// NOI18N
      jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_FirstSettings4.jPanel1.border.title"))); // NOI18N
      jPanel1.setName("jPanel1"); // NOI18N

      jPanel2.setName("jPanel2"); // NOI18N

      jScrollPane1.setName("jScrollPane1"); // NOI18N

      jTextPane1.setText(bundle.getString("wizard_FirstSettings4.jTextPane1.text")); // NOI18N
      jTextPane1.setName("jTextPane1"); // NOI18N
      jScrollPane1.setViewportView(jTextPane1);

      labeledTextChooser2.set_Label(bundle.getString("wizard_FirstSettings4.labeledTextChooser2._Label")); // NOI18N
      labeledTextChooser2.setName("labeledTextChooser2"); // NOI18N

      javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
      jPanel2.setLayout(jPanel2Layout);
      jPanel2Layout.setHorizontalGroup(
         jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
         .addComponent(labeledTextChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      );
      jPanel2Layout.setVerticalGroup(
         jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel2Layout.createSequentialGroup()
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(labeledTextChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE))
      );

      javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(40, 40, 40))
      );

      add(jPanel1, java.awt.BorderLayout.CENTER);
   }// </editor-fold>//GEN-END:initComponents
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JTextPane jTextPane1;
   private mpv5.ui.beans.LabeledTextChooser labeledTextChooser2;
   // End of variables declaration//GEN-END:variables

    public boolean next() {
        File a = new File(labeledTextChooser2.get_Text(false));
        if (a != null && a.exists()) {
            File f = a;
            try {
                String m = String.valueOf(new Date().getTime());
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                YabsViewProxy.instance().setWaiting(true);
                String lang = LanguageManager.importLanguage(f.getName() + " - " + m, f, true);
                User.getCurrentUser().setLanguage(lang);
                User.getCurrentUser().save();
            } catch (Exception ex) {
                Logger.getLogger(MPView.class.getName()).log(Level.SEVERE, null, ex);
                Popup.error(ex);
            } finally {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                YabsViewProxy.instance().setWaiting(false);
            }
        } 
        try {

            File f = new File(this.getClass().getResource("/mpv5/resources/extra/").toURI());
            Log.Debug(this, "Importing coutries from: " + f.getCanonicalPath());
            File[] langfiles = f.listFiles();
            for (int i = 0; i < langfiles.length; i++) {
                File file = langfiles[i];
                if (file.getName().endsWith(".odt")) {
                    String m = String.valueOf(System.nanoTime());
                    File fi = file;
                    QueryHandler.instanceOf().clone(Context.getFiles(), this).insertFile(fi,
                            DatabaseObject.getObject(Context.getTemplate()),
                            new SaveString(fi.getName() + "_" + m, true));
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(wizard_FirstSettings4.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    public void refresh() {
        try {
            QueryHandler xx = QueryHandler.instanceOf().clone(Context.getTemplate(), 1);
            ReturnValue data = xx.freeQuery("select ids,cname from templates order by dateadded desc ", MPSecurityManager.VIEW, null);
            for (int i = 0; i < data.size(); i++) {
                int id = Integer.valueOf(data.getData()[i][0].toString());
                Template t = (Template) DatabaseObject.getObject(Context.getTemplate(), id);

                t.setGroupsids(1);
                t.setCname(data.getData()[i][1].toString());
                t.setMimetype(String.valueOf(Constants.TYPE_BILL));
                t.setFormat("1,2,4,5,6");
                t.setDescription("Wizard insert");
                t.save(true);

                User object = User.getCurrentUser();
                QueryData c = new QueryData();
                c.add("usersids", object.__getIDS());
                c.add("templatesids", t.__getIDS());
                c.add("groupsids", 1);
                c.add("cname", t.__getIDS() + "@" + object.__getIDS() + "@" + 1);
                QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).insert(c, null);
            }
            TemplateHandler.clearCache();
        } catch (Exception ex) {
            Logger.getLogger(wizard_FirstSettings4.class.getName()).log(Level.SEVERE, null, ex);
            Popup.error(ex);
        }
    }

    public boolean back() {
        return true;
    }

    public void load() {
    }

    private void setModels() {
    }

    //just faking
    public boolean collectData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public DatabaseObject getDataOwner() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setDataOwner(DatabaseObject object, boolean populateData) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void exposeData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void paste(DatabaseObject... dbo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void showRequiredFields() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void showSearchBar(boolean show) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void actionAfterSave() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void actionAfterCreate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void actionBeforeCreate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void actionBeforeSave() throws ChangeNotApprovedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void mail() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void print() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
