package mpv5.ui.dialogs.subcomponents;

import mpv5.handler.FutureCallback;
import mpv5.handler.TemplateHandler;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import mpv5.Main;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryData;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.Wizard;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;
import mpv5.utils.files.FileDirectoryHandler;

/**
 *
 *
 */
public class wizard_FirstSettings1 extends javax.swing.JPanel implements Wizardable {

    private static final long serialVersionUID = -8347532498124147821L;

    public static void build(JFrame f) {

        Wizard w = new Wizard(false);
        w.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        w.setAlwaysOnTop(false);
        w.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        w.setLocationRelativeTo(f);
        w.addPanel(new wizard_FirstSettings1(w));
        w.addPanel(new wizard_FirstSettings2(w));
        //w.addPanel(new wizard_FirstSettings3(w));
        //w.addPanel(new wizard_FirstSettings4(w, dir));
        w.addPanel(new wizard_FirstSettings5(w));
        w.showWizAsChild();
        
    }
    private WizardMaster master;
    private PropertyStore nactions;
    private List<JToolBar> nbars;

    public wizard_FirstSettings1(WizardMaster w) {
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
        jCheckBox1 = new javax.swing.JCheckBox();

        setBackground(new java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_FirstSettings1.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextPane1.setText(bundle.getString("wizard_FirstSettings1.jTextPane1.text")); // NOI18N
        jTextPane1.setName("jTextPane1"); // NOI18N
        jScrollPane1.setViewportView(jTextPane1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 16, Short.MAX_VALUE))
        );

        jCheckBox1.setText(bundle.getString("wizard_FirstSettings1.jCheckBox1.text")); // NOI18N
        jCheckBox1.setName("jCheckBox1"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 40, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables

    public boolean next() {
   
        try {
            importCountries();
        } catch (Exception ex) {   
            Log.Debug(wizard_FirstSettings1.class, ex);
        }
        
        if (jCheckBox1.isSelected()) {
            try {
                importInitialLanguage("DE");
            } catch (Exception ex) {
                Log.Debug(wizard_FirstSettings1.class, ex);
            }
             try {
                importInitialTemplate();
            } catch (Exception ex) {
                Log.Debug(wizard_FirstSettings1.class, ex);
            }
        }
     
        return true;
    }

    public boolean back() {
        return false;
    }

    public void load() {
        jCheckBox1.setSelected(true);
    }

    private void setModels() {
    }

    public void importInitialLanguage(String langid) throws IOException, Exception {
        LanguageManager.importFromJar(langid);
    }

    public void importInitialTemplate() throws Exception {

        InputStream s = Main.class.getResourceAsStream("/extra/DE-" + Constants.VERSION + ".odt");
        final File f = FileDirectoryHandler.getTempFile("template-de", "odt");
        FileDirectoryHandler.copyFile(s, f);
        Log.Debug(Main.class, "Importing language from: " + f.getCanonicalPath());

        TemplateHandler.importTemplateAndAssign(f, new FutureCallback<Template>() {
            @Override
            public void call(Template t) {
                Log.Debug(this, "Template callback");
                t.setGroupsids(1);
                t.setCname(f.getName());
                t.setMimetype(String.valueOf(Constants.TYPE_INVOICE));
                t.setFormat("1,2,4,5,7,9");
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
        });
    }

    public void importCountries() throws Exception {
        InputStream s = Main.class.getResourceAsStream("/extra/countries.xml");
        final File f = FileDirectoryHandler.getTempFile("countries", "xml");
        FileDirectoryHandler.copyFile(s, f);
        Log.Debug(Main.class, "Importing language from: " + f.getCanonicalPath());
        LanguageManager.importCountries(f);
    }

    public void assignOneTemplate() {
        try {
            QueryHandler xc = QueryHandler.instanceOf().clone(Context.getTemplatesToUsers(), 1);
            if (xc.getCount() > 0) {
                return;
            }
            try {
                QueryHandler xx = QueryHandler.instanceOf().clone(Context.getTemplate(), 1);
                ReturnValue data = xx.freeSelect("ids,cname");
                if (data.hasData()) {
                    int id = Integer.valueOf(data.getData()[0][0].toString());
                    Template t = (Template) DatabaseObject.getObject(Context.getTemplate(), id);

                    t.setGroupsids(1);
                    t.setCname(data.getData()[0][1].toString());
                    t.setMimetype(String.valueOf(Constants.TYPE_INVOICE));
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
                } else {
                    Popup.notice("Please restart the aplication or assign a template manually in the Templatemanager");
                }
                TemplateHandler.clearCache();
            } catch (Exception e) {
                Log.Debug(e);
            }

        } catch (Exception ex) {
            Log.Debug(ex);
            Popup.error(ex);
        }
    }
}
