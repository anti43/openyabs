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
package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryData;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.SaveString;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.handler.TemplateHandler;
import mpv5.handler.VariablesHandler;
import mpv5.logging.Log;
import mpv5.ui.dialogs.BigPopup;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.Search2;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.ui.modern.ResponsiveGrid;
import mpv5.ui.panels.DataPanel;
import mpv5.ui.panels.PreviewWaiter;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.export.Export;
import mpv5.utils.export.ODTFile2;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileExecutor;
import mpv5.utils.files.FileMonitor;
import mpv5.utils.jobs.Job;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.ui.TextFieldUtils;

/**
 * Template manager: the templates on the left (name, document type, group,
 * users), the selected one in cards on the right: what it is for, who uses
 * it, printing and the template file.
 */
public final class ControlPanel_Templates extends JPanel implements ControlApplet, DataPanel {

    private static final long serialVersionUID = 2L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "templates";
    private Template dataOwner;
    private File lastImportedFile;
    private long lastmodified;

    private final JTable templates = new JTable();
    private final List<Template> rows = new ArrayList<Template>();
    private final JLabel title = new JLabel();
    private final JTextField fullname = new JTextField();
    private final JComboBox<MPComboBoxModelItem> type = new JComboBox<MPComboBoxModelItem>();
    private final JComboBox groupname = new JComboBox();
    private final JTextArea descr = new JTextArea(3, 20);
    private final JPanel userBoxes = new JPanel(new GridLayout(0, 2, ModernTheme.px(8), ModernTheme.px(2)));
    private final List<JCheckBox> userChecks = new ArrayList<JCheckBox>();
    private final List<User> allUsers = new ArrayList<User>();
    private final JTextField printern = new JTextField();
    private final JTextField format = new JTextField();
    private final JTextField einvoicePayterm = new JTextField();
    private final JLabel file = new JLabel();
    private final JCheckBox updateService = new JCheckBox(Messages.TEMPLATES_WATCH.toString());
    private final JPanel actions = new JPanel(new BorderLayout());
    private final List<JComponent> needsTemplate = new ArrayList<JComponent>();
    private FileMonitor.FileChangeListener watcher;
    private String watchedFile;
    private boolean loading;

    public ControlPanel_Templates() {
        if (MPSecurityManager.checkAdminAccess()) {
            build();
            type.setModel(new DefaultComboBoxModel<MPComboBoxModelItem>(MPComboBoxModelItem.toItems(TemplateHandler.getTypes())));
            groupname.setModel(new DefaultComboBoxModel(
                    MPComboBoxModelItem.toItems(new DatabaseSearch(Context.getGroup()).getValuesFor(Context.getGroup().getSubID()))));
            format.setText(Template.DEFAULT_FORMAT);
            refresh();
            setVisible(true);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Layout">
    private void build() {
        setLayout(new BorderLayout(ModernTheme.px(12), 0));
        setBackground(ModernTheme.background());
        setBorder(ModernTheme.padding(10, 10));

        //left: the templates
        templates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templates.getSelectionModel().addListSelectionListener(e -> {
            int r = templates.getSelectedRow();
            if (!e.getValueIsAdjusting() && !loading && r >= 0) {
                setDataOwner(rows.get(templates.convertRowIndexToModel(r)), true);
            }
        });
        ModernTheme.style(templates);
        JButton add = new JButton("+ " + Messages.TEMPLATES_IMPORT);
        ModernTheme.actionButton(add, null);
        add.setToolTipText(Messages.TEMPLATES_IMPORT_TIP.toString());
        add.addActionListener(e -> importTemplate());
        Card left = new Card(Messages.TEMPLATES_LIST.toString()).headerAction(add);
        JScrollPane st = new JScrollPane(templates);
        st.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
        left.addFull(st, 1);
        left.setPreferredSize(new Dimension(ModernTheme.px(430), ModernTheme.px(420)));
        add(left, BorderLayout.WEST);

        //right: the template
        title.setFont(ModernTheme.font(8, Font.BOLD));
        title.setBorder(BorderFactory.createEmptyBorder(0, ModernTheme.px(4), ModernTheme.px(6), 0));
        for (JTextField t : new JTextField[]{fullname, printern, format, einvoicePayterm}) {
            ModernTheme.roomy(t);
        }
        descr.setLineWrap(true);
        descr.setWrapStyleWord(true);
        JScrollPane sd = new JScrollPane(descr);
        sd.setPreferredSize(new Dimension(ModernTheme.px(200), ModernTheme.px(64)));

        ResponsiveGrid grid = new ResponsiveGrid(ModernTheme.px(360), 2);
        grid.add(new Card(Messages.TEMPLATES_WHAT.toString())
                .row(Card.field(Messages.TEMPLATES_NAME.toString(), fullname))
                .row(Card.field(Messages.TEMPLATES_TYPE.toString(), type), Card.field(Messages.TEMPLATES_GROUP.toString(), groupname))
                .addFull(hint(Messages.TEMPLATES_GROUP_HINT.toString()))
                .row(Card.field(Messages.TEMPLATES_DESCRIPTION.toString(), sd)));

        userBoxes.setOpaque(false);
        JButton all = link(Messages.TEMPLATES_ALL.toString(), () -> setAllUsers(true));
        JButton none = link(Messages.TEMPLATES_NONE.toString(), () -> setAllUsers(false));
        JPanel allNone = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(4), 0));
        allNone.setOpaque(false);
        allNone.add(all);
        allNone.add(none);
        grid.add(new Card(Messages.TEMPLATES_USERS.toString()).headerAction(allNone)
                .addFull(hint(Messages.TEMPLATES_USERS_HINT.toString()))
                .addFull(userBoxes));

        JButton printers = new JButton(Messages.TEMPLATES_PRINTERS.toString());
        printers.addActionListener(e -> showPrinters());
        JButton editFormat = new JButton(Messages.TEMPLATES_EDIT_FORMAT.toString());
        editFormat.addActionListener(e -> BigPopup.showPopup(this, TemplateFormatEditor.instanceOf(format), "", true));
        grid.add(new Card(Messages.TEMPLATES_OUTPUT.toString())
                .row(Card.field(Messages.TEMPLATES_PRINTER.toString(), printern, 3), Card.field(null, printers, 1))
                .addFull(hint(Messages.TEMPLATES_PRINTER_HINT.toString()))
                .row(Card.field(Messages.TEMPLATES_FORMAT.toString(), format, 3), Card.field(null, editFormat, 1))
                .row(Card.field(Messages.TEMPLATE_EINVOICE_PAYTERM.toString(), einvoicePayterm))
                .addFull(hint(Messages.TEMPLATES_PAYTERM_HINT.toString())));

        file.setForeground(ModernTheme.muted());
        JButton open = new JButton(Messages.TEMPLATES_OPEN.toString());
        open.addActionListener(e -> openFile());
        JButton replace = new JButton(Messages.TEMPLATES_REPLACE.toString());
        replace.addActionListener(e -> replaceFile());
        JButton saveAs = new JButton(Messages.TEMPLATES_SAVE_AS.toString());
        saveAs.addActionListener(e -> saveFileAs());
        JButton preview = new JButton(Messages.TEMPLATES_PREVIEW.toString());
        preview.addActionListener(e -> preview());
        JButton vars = new JButton(Messages.TEMPLATES_VARIABLES.toString());
        vars.addActionListener(e -> showVariables());
        JPanel fileButtons = new JPanel(new mpv5.ui.modern.WrapLayout(FlowLayout.LEFT, ModernTheme.px(6), ModernTheme.px(4)));
        fileButtons.setOpaque(false);
        for (JButton b : new JButton[]{open, replace, saveAs, preview, vars}) {
            fileButtons.add(b);
            needsTemplate.add(b);
        }
        updateService.setOpaque(false);
        updateService.addItemListener(e -> watch(updateService.isSelected()));
        needsTemplate.add(updateService);
        grid.add(new Card(Messages.TEMPLATES_FILE.toString())
                .addFull(file)
                .addFull(fileButtons)
                .addFull(updateService)
                .addFull(hint(Messages.TEMPLATES_WATCH_HINT.toString())));

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(title, BorderLayout.NORTH);
        right.add(grid, BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);

        JButton delete = new JButton(Messages.TEMPLATES_DELETE.toString());
        delete.setForeground(new Color(185, 28, 28));
        delete.addActionListener(e -> delete());
        needsTemplate.add(delete);
        JButton reset = new JButton(Messages.USERS_RESET.toString());
        reset.addActionListener(e -> reset());
        PrimaryButton save = new PrimaryButton(Messages.USERS_SAVE.toString(), "/images/16/filesave.png");
        save.addActionListener(e -> save());
        needsTemplate.add(save);
        JPanel l = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), ModernTheme.px(6)));
        l.setOpaque(false);
        l.add(delete);
        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), ModernTheme.px(6)));
        r.setOpaque(false);
        r.add(reset);
        r.add(save);
        actions.setOpaque(false);
        actions.add(l, BorderLayout.WEST);
        actions.add(r, BorderLayout.EAST);
    }

    private static JLabel hint(String text) {
        JLabel l = new JLabel("<html>" + text + "</html>");
        l.setFont(ModernTheme.font(-1, Font.PLAIN));
        l.setForeground(ModernTheme.muted());
        return l;
    }

    private static JButton link(String text, Runnable r) {
        JButton b = new JButton(text);
        b.putClientProperty("JButton.buttonType", "toolBarButton");
        b.setForeground(ModernTheme.accent());
        b.addActionListener(e -> r.run());
        return b;
    }

    private void setAllUsers(boolean on) {
        for (JCheckBox c : userChecks) {
            c.setSelected(on);
        }
    }

    private static String typeName(String mimetype) {
        try {
            int t = Integer.parseInt(mimetype);
            for (mpv5.handler.MPEnum e : TemplateHandler.getTypes()) {
                if (e != null && e.getId() != null && e.getId() == t) {
                    return e.getName();
                }
            }
        } catch (Exception e) {
            //unknown type, e.g. "odt" right after the import
        }
        return Messages.TEMPLATES_TYPE_UNSET.toString();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Actions">
    private void importTemplate() {
        DialogForFile di = new DialogForFile(DialogForFile.FILES_ONLY);
        di.setFileFilter(DialogForFile.TEMPLATE_FILES);
        if (di.chooseFile()) {
            final File fi = di.getFile();
            Template t = new Template();
            //when the file is stored: select the new template, the user assigns it
            QueryHandler.instanceOf().clone(Context.getFiles(), this).insertFile(fi, t, new SaveString(fi.getName(), true),
                    new mpv5.handler.FutureCallback<Object>() {
                @Override
                public void call(Object created) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        refresh();
                        if (created instanceof Template) {
                            select((Template) created);
                        }
                        configureUpdateService(fi);
                        Popup.notice(Messages.TEMPLATES_IMPORTED.toString());
                    });
                }
            });
        }
    }

    private void save() {
        if (dataOwner == null || !collectData()) {
            showRequiredFields();
            return;
        }
        if (selectedUsers().isEmpty() && !Popup.Y_N_dialog(Messages.TEMPLATES_NO_USERS.toString())) {
            return;
        }
        dataOwner.getPanelData(this);
        if (dataOwner.save()) {
            saveEInvoicePayterm(dataOwner);
            actionAfterSave();
            Template saved = dataOwner;
            refresh();
            select(saved);
            mpv5.YabsViewProxy.instance().addMessage(Messages.TEMPLATES_SAVED + ": " + saved.__getCname());
        } else {
            showRequiredFields();
        }
    }

    private void delete() {
        if (dataOwner != null && dataOwner.isExisting()
                && Popup.Y_N_dialog(String.format(Messages.TEMPLATES_DELETE_CONFIRM.toString(), dataOwner.__getCname()))) {
            watch(false);
            TemplateHandler.clearCache();
            dataOwner.delete();
            dataOwner = null;
            refresh();
        }
    }

    private void showVariables() {
        try {
            DatabaseObject t = searchDocument();
            if (t != null) {
                List<String[]> vars = VariablesHandler.resolveVarsFor(t);
                List<List<String>> varsl = new ArrayList<List<String>>();
                for (String[] strings : vars) {
                    varsl.add(Arrays.asList(strings));
                }
                Popup.notice(varsl, Messages.VARIABLES + "\n\n" + t.toString());
            }
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }

    /**
     * @return a document of the type of the template, chosen by the user
     */
    private DatabaseObject searchDocument() {
        MPComboBoxModelItem i = (MPComboBoxModelItem) type.getSelectedItem();
        if (i == null) {
            return null;
        }
        switch (Integer.parseInt(String.valueOf(i.getId()))) {
            case Constants.TYPE_OFFER:
                return Search2.showSearchFor(Context.getOffer());
            case Constants.TYPE_ORDER:
                return Search2.showSearchFor(Context.getOrder());
            case Constants.TYPE_INVOICE:
                return Search2.showSearchFor(Context.getInvoice());
            case Constants.TYPE_DEPOSIT:
                return Search2.showSearchFor(Context.getDeposit());
            case Constants.TYPE_PART_PAYMENT:
                return Search2.showSearchFor(Context.getPartPayment());
            case Constants.TYPE_CREDIT:
                return Search2.showSearchFor(Context.getCredit());
            default:
                return null;
        }
    }

    private void saveFileAs() {
        if (dataOwner != null && dataOwner.isExisting()) {
            try {
                DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY, new File(dataOwner.getFile().getName()));
                File nFile = d.saveFile(dataOwner.getFile());
                if (nFile != null) {
                    configureUpdateService(nFile);
                    updateService.setSelected(true);
                }
            } catch (Exception e) {
                Log.Debug(e);
                Popup.error(e);
            }
        }
    }

    private void replaceFile() {
        if (dataOwner != null && dataOwner.isExisting()
                && Popup.Y_N_dialog(Messages.REALLY_CHANGE.toString(), dataOwner.__getCname())) {
            DialogForFile di = new DialogForFile(DialogForFile.FILES_ONLY);
            di.setFileFilter(DialogForFile.TEMPLATE_FILES);
            if (di.chooseFile()) {
                File fi = di.getFile();
                storeFile(dataOwner, fi);
                configureUpdateService(fi);
            }
        }
    }

    /**
     * Stores a new version of the template file
     */
    private void storeFile(Template tpl, File fi) {
        QueryHandler.instanceOf().clone(Context.getFiles(), this).updateFile(fi, tpl.__getFilename());
        tpl.setDescription(tpl.__getDescription() + "\n - Updated: " + new Date());
        tpl.save(true);
        TemplateHandler.clearCache();
        mpv5.YabsViewProxy.instance().addMessage(Messages.TEMPLATES_UPDATED + ": " + tpl.__getCname());
    }

    /**
     * Watches the template file: changes are stored automatically. One
     * watcher at a time (switching it off really stops it).
     */
    private void watch(boolean on) {
        if (loading) {
            return;
        }
        if (watcher != null) {
            FileMonitor.getInstance().removeFileChangeListener(watcher, watchedFile);
            watcher = null;
            watchedFile = null;
        }
        String path = sourceFile();
        if (on && path != null && dataOwner != null) {
            final Template tpl = dataOwner;
            watchedFile = path;
            watcher = fileName -> javax.swing.SwingUtilities.invokeLater(() -> storeFile(tpl, new File(fileName)));
            FileMonitor.getInstance().addFileChangeListener(watcher, path, 1000L);
        }
    }

    private void openFile() {
        if (dataOwner != null && dataOwner.isExisting()) {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(dataOwner.getFile());
                } else if (LocalSettings.getBooleanProperty(LocalSettings.OFFICE_USE)) {
                    FileExecutor.run(LocalSettings.getProperty(LocalSettings.OFFICE_HOME) + File.separator + dataOwner.getFile().getPath());
                } else {
                    Popup.notice(Messages.TEMPLATES_CANNOT_OPEN.toString());
                }
            } catch (Exception e) {
                Log.Debug(e);
                Popup.error(e);
            }
        }
    }

    private void showPrinters() {
        List<String> l = new ArrayList<String>();
        for (PrintService s : PrintServiceLookup.lookupPrintServices(null, null)) {
            l.add(s.getName());
        }
        Popup.notice(l, Messages.PRINTER_NAMES);
    }

    /**
     * A PDF of the template with a chosen document (or sample data) and 20
     * sample positions
     */
    private void preview() {
        if (dataOwner == null) {
            return;
        }
        DatabaseObject t = searchDocument();
        if (t == null) {
            try {
                t = DatabaseObject.getObject(Context.getInvoice(), 1);
            } catch (NodataFoundException ex) {
                t = (Item) Context.getInvoice().getNewObject();
                Contact k = new Contact();
                k.avoidNulls();
                k.fillSampleData();
                t.avoidNulls();
                t.fillSampleData();
                ((Item) t).setContactsids(k.__getIDS());
            }
        }
        try {
            Map<String, Object> hm1 = t.getFormFields();
            File f = dataOwner.getFile();
            File f2 = FileDirectoryHandler.getTempFile("pdf");
            Export ex = new Export(dataOwner);
            ex.putAll(hm1);
            ArrayList<String[]> l = new ArrayList<String[]>();
            for (int i = 0; i < 20; i++) {
                l.add(SubItem.getRandomItem().toStringArray());
            }
            ex.put(ODTFile2.KEY_TABLE + "1", l);
            ex.setTemplate(new ODTFile2(f.getPath()));
            ex.setTargetFile(f2);
            new Job(ex, new PreviewWaiter()).execute();
        } catch (Exception ex1) {
            Log.Debug(ex1);
            Popup.error(ex1);
        }
    }

    private void select(Template t) {
        for (int i = 0; i < rows.size(); i++) {
            if (t != null && rows.get(i).__getIDS().intValue() == t.__getIDS().intValue()) {
                int v = templates.convertRowIndexToView(i);
                templates.setRowSelectionInterval(v, v);
                templates.scrollRectToVisible(templates.getCellRect(v, 0, true));
                return;
            }
        }
        if (!rows.isEmpty()) {
            templates.setRowSelectionInterval(0, 0);
        } else {
            empty();
        }
    }

    /**
     * No template yet: explain what to do
     */
    private void empty() {
        dataOwner = null;
        title.setText(Messages.TEMPLATES_EMPTY.toString());
        fullname.setText("");
        descr.setText("");
        file.setText(" ");
        for (JComponent c : needsTemplate) {
            c.setEnabled(false);
        }
    }
    // </editor-fold>

    @Override
    public void setValues(PropertyStore values) {
    }

    @Override
    public String getUname() {
        return UNAME;
    }

    @Override
    public void reset() {
        if (dataOwner != null && dataOwner.isExisting()) {
            try {
                setDataOwner(DatabaseObject.getObject(Context.getTemplate(), dataOwner.__getIDS()), true);
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex);
            }
        }
    }
    public String description_ = "";
    public String filename_ = "";
    public String cname_;
    public String format_;
    public int intsize_;
    public String mimetype_;
    public int intaddedby_ = 4343;
    public int ids_;
    public Group group_;
    public int compsids_;
    public String printer_;
    public java.util.Date dateadded_ = new java.util.Date();
    public boolean isupdateenabled_ = false;
    public String pathtofile_ = "";
    public long lastmodified_;

    @Override
    public boolean collectData() {
        if (fullname.getText().trim().isEmpty()) {
            return false;
        }
        if (groupname.getSelectedItem() != null) {
            try {
                group_ = (Group) DatabaseObject.getObject(Context.getGroup(),
                        Integer.parseInt(((MPComboBoxModelItem) groupname.getSelectedItem()).getId()));
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex);
                group_ = Group.getDefault();
            }
        }
        description_ = descr.getText();
        cname_ = fullname.getText().trim();
        MPComboBoxModelItem t = (MPComboBoxModelItem) type.getSelectedItem();
        mimetype_ = t == null ? mimetype_ : String.valueOf(t.getId());
        format_ = format.getText();
        printer_ = printern.getText().trim().isEmpty() ? Template.PRINTER_UNDEFINED : printern.getText().trim();
        lastmodified_ = lastmodified;
        isupdateenabled_ = updateService.isSelected();
        return true;
    }

    @Override
    public void exposeData() {
        loading = true;
        try {
            for (JComponent c : needsTemplate) {
                c.setEnabled(true);
            }
            title.setText(cname_);
            fullname.setText(cname_);
            descr.setText(description_);
            descr.setCaretPosition(0);
            for (int i = 0; i < type.getItemCount(); i++) {
                if (String.valueOf(type.getItemAt(i).getId()).equals(mimetype_)) {
                    type.setSelectedIndex(i);
                }
            }
            if (group_ != null) {
                groupname.setSelectedIndex(MPComboBoxModelItem.getItemID(String.valueOf(group_.__getIDS()), groupname.getModel()));
            }
            format.setText(format_);
            printern.setText(Template.PRINTER_UNDEFINED.equals(printer_) ? "" : printer_);

            //the users, checked if the template is assigned to them
            Set<Integer> assigned = new HashSet<Integer>();
            try {
                Object[][] data = QueryHandler.instanceOf().clone(Context.getTemplatesToUsers())
                        .select("usersids", new QueryCriteria("templatesids", dataOwner.__getIDS()));
                for (Object[] d : data) {
                    assigned.add(Integer.valueOf(String.valueOf(d[0])));
                }
            } catch (Exception e) {
                Log.Debug(this, e);
            }
            userBoxes.removeAll();
            userChecks.clear();
            allUsers.clear();
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getUser())) {
                User u = (User) o;
                String name = u.__getFullname() == null || u.__getFullname().trim().isEmpty() ? u.__getCname() : u.__getFullname();
                JCheckBox c = new JCheckBox(name, assigned.contains(u.__getIDS()));
                c.setOpaque(false);
                c.setToolTipText(u.__getCname());
                userChecks.add(c);
                allUsers.add(u);
                userBoxes.add(c);
            }
            userBoxes.revalidate();

            int days = mpv5.utils.export.ZugpferdFile.paytermOf(dataOwner);
            einvoicePayterm.setText(days >= 0 ? String.valueOf(days) : "");
            lastImportedFile = null;
            lastmodified = lastmodified_;
            try {
                File f = dataOwner.getFile();
                //stored in the database; where it came from, if known
                file.setText("<html>" + Messages.TEMPLATES_FILE_SIZE + ": <b>" + Math.max(1, f.length() / 1024) + " KB</b>"
                        + (pathtofile_ != null && !pathtofile_.isEmpty() ? "<br>" + Messages.TEMPLATES_FILE_ORIGIN + ": " + pathtofile_ : "")
                        + "</html>");
            } catch (Exception e) {
                //the file could not be read: say so, do not offer to delete the template
                Log.Debug(this, e);
                file.setText("<html><font color='#b91c1c'>" + Messages.TEMPLATES_FILE_MISSING + "</font></html>");
            }
            updateService.setSelected(isupdateenabled_);
            updateService.setEnabled(sourceFile() != null);
        } catch (Exception e) {
            Log.Debug(this, e);
        } finally {
            loading = false;
        }
        watch(updateService.isSelected());
    }

    private List<User> selectedUsers() {
        List<User> l = new ArrayList<User>();
        for (int i = 0; i < userChecks.size(); i++) {
            if (userChecks.get(i).isSelected()) {
                l.add(allUsers.get(i));
            }
        }
        return l;
    }

    /**
     * Store the payment term (days) of the template, 0 = payable immediately,
     * empty removes it
     */
    private void saveEInvoicePayterm(DatabaseObject template) {
        String v = einvoicePayterm.getText() == null ? "" : einvoicePayterm.getText().trim();
        int days;
        try {
            days = v.isEmpty() ? mpv5.utils.export.ZugpferdFile.PAYTERM_UNSET : Integer.parseInt(v);
            //empty is allowed (no own payment term); only a typed negative number is wrong
            if (!v.isEmpty() && days < 0) {
                throw new NumberFormatException(v);
            }
        } catch (NumberFormatException e) {
            Popup.warn(Messages.TEMPLATE_EINVOICE_PAYTERM + ": " + v);
            return;
        }
        mpv5.utils.export.ZugpferdFile.savePayterm(template, days);
    }

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean p) {
        dataOwner = (Template) object;
        if (p) {
            dataOwner.setPanelData(this);
            this.exposeData();
        }
    }

    @Override
    public void refresh() {
        loading = true;
        rows.clear();
        DefaultTableModel m = new DefaultTableModel(new Object[]{
            Messages.TEMPLATES_NAME.toString(), Messages.TEMPLATES_TYPE.toString(), Messages.TEMPLATES_GROUP.toString(),
            Messages.TEMPLATES_USERS_COUNT.toString()}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getTemplate())) {
                Template t = (Template) o;
                rows.add(t);
                String group;
                try {
                    group = String.valueOf(Group.getObject(Context.getGroup(), t.__getGroupsids()));
                } catch (Exception e) {
                    group = "";
                }
                int count = 0;
                try {
                    count = QueryHandler.instanceOf().clone(Context.getTemplatesToUsers())
                            .select("usersids", new QueryCriteria("templatesids", t.__getIDS())).length;
                } catch (Exception e) {
                    //none
                }
                m.addRow(new Object[]{t.__getCname(), typeName(t.__getMimetype()), group, count});
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        templates.setModel(m);
        templates.setAutoCreateRowSorter(true);
        templates.getColumnModel().getColumn(0).setPreferredWidth(ModernTheme.px(170));
        templates.getColumnModel().getColumn(3).setPreferredWidth(ModernTheme.px(60));
        loading = false;
        select(dataOwner);
    }

    @Override
    public void paste(DatabaseObject... dbos) {
        for (DatabaseObject dbo : dbos) {
            if (dbo.getContext().equals(Context.getTemplate())) {
                select((Template) dbo);
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE, Color.RED);
            }
        }
    }

    @Override
    public void showRequiredFields() {
        TextFieldUtils.blinkerRed(fullname);
        fullname.requestFocusInWindow();
    }

    @Override
    public void showSearchBar(boolean show) {
    }

    @Override
    public Component getAndRemoveActionPanel() {
        return actions;
    }

    /**
     * Stores who uses the template: checked users get it, unchecked users no
     * longer
     */
    @Override
    public void actionAfterSave() {
        int group = 1;
        if (groupname.getSelectedItem() != null) {
            group = Integer.parseInt(((MPComboBoxModelItem) groupname.getSelectedItem()).getId());
        }
        Set<Integer> wanted = new HashSet<Integer>();
        for (User u : selectedUsers()) {
            wanted.add(u.__getIDS());
        }
        Set<Integer> present = new HashSet<Integer>();
        try {
            Object[][] data = QueryHandler.instanceOf().clone(Context.getTemplatesToUsers())
                    .select("usersids, cname", new QueryCriteria("templatesids", dataOwner.__getIDS()));
            for (Object[] d : data) {
                int user = Integer.parseInt(String.valueOf(d[0]));
                if (wanted.contains(user)) {
                    present.add(user);
                } else {
                    QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).delete(new QueryCriteria("cname", String.valueOf(d[1])));
                }
            }
        } catch (Exception ex) {
            Log.Debug(this, ex);
        }
        for (Integer user : wanted) {
            if (!present.contains(user)) {
                QueryData c = new QueryData();
                c.add("usersids", user);
                c.add("templatesids", dataOwner.__getIDS());
                c.add("groupsids", group);
                c.add("cname", dataOwner.__getIDS() + "@" + user + "@" + group);
                QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).insert(c, null);
            }
        }
        TemplateHandler.clearCache();
    }

    @Override
    public void actionAfterCreate() {
    }

    @Override
    public void actionBeforeCreate() {
    }

    @Override
    public void actionBeforeSave() {
    }

    @Override
    public void mail() {
    }

    @Override
    public void print() {
    }

    /**
     * @return the file on this computer the template was imported from (to be
     * watched), null if unknown
     */
    private String sourceFile() {
        if (lastImportedFile != null) {
            return lastImportedFile.getPath();
        }
        return pathtofile_ != null && !pathtofile_.isEmpty() && new File(pathtofile_).canRead() ? pathtofile_ : null;
    }

    private void configureUpdateService(File fi) {
        lastImportedFile = fi;
        updateService.setEnabled(true);
        try {
            pathtofile_ = fi.getCanonicalPath();
        } catch (IOException ex) {
            Log.Debug(this, ex);
        }
        lastmodified = fi.lastModified();
    }
}
