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
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.Search2;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.ui.modern.ResponsiveGrid;
import mpv5.ui.panels.DataPanel;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.export.Export;
import mpv5.utils.export.ODTFile2;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileExecutor;
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
    private final JComboBox<String> printern = new JComboBox<String>();
    /**
     * the columns of the position table, e.g. "1,2,3,4,5,6"
     */
    private String formatValue = Template.DEFAULT_FORMAT;
    private final JLabel formatLabel = new JLabel();
    private final JTextField einvoicePayterm = new JTextField();
    private final JLabel file = new JLabel();
    private final JLabel editState = new JLabel();
    private final JLabel previewImage = new JLabel();
    private final JLabel previewCaption = new JLabel();
    /**
     * the document shown in the preview, null: the default
     */
    private DatabaseObject previewDoc;
    private File previewPdf;
    private int previewRun;
    private final JPanel actions = new JPanel(new BorderLayout());
    private final List<JComponent> needsTemplate = new ArrayList<JComponent>();
    private final mpv5.handler.TemplateEditing.Listener editListener = (tpl, when) -> javax.swing.SwingUtilities.invokeLater(() -> {
        mpv5.YabsViewProxy.instance().addMessage(String.format(Messages.TEMPLATES_FILE_CHANGED.toString(), tpl.__getCname()));
        if (dataOwner != null && dataOwner.isExisting() && dataOwner.__getIDS().intValue() == tpl.__getIDS().intValue()) {
            dataOwner.forgetFile();
            showFileState();
            refreshPreview();
        }
    });
    private boolean loading;

    public ControlPanel_Templates() {
        if (MPSecurityManager.checkAdminAccess()) {
            build();
            type.setModel(new DefaultComboBoxModel<MPComboBoxModelItem>(MPComboBoxModelItem.toItems(TemplateHandler.getTypes())));
            groupname.setModel(new DefaultComboBoxModel(
                    MPComboBoxModelItem.toItems(new DatabaseSearch(Context.getGroup()).getValuesFor(Context.getGroup().getSubID()))));
            mpv5.handler.TemplateEditing.addListener(editListener);
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
        for (JTextField t : new JTextField[]{fullname, einvoicePayterm}) {
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

        //the file: edit it in LibreOffice, every save is taken over
        file.setForeground(ModernTheme.muted());
        editState.setFont(ModernTheme.font(-1, Font.PLAIN));
        JButton edit = new JButton(Messages.TEMPLATES_OPEN.toString());
        ModernTheme.actionButton(edit, null);
        edit.addActionListener(e -> editFile());
        JButton replace = new JButton(Messages.TEMPLATES_REPLACE.toString());
        replace.addActionListener(e -> replaceFile());
        JButton saveAs = new JButton(Messages.TEMPLATES_SAVE_AS.toString());
        saveAs.addActionListener(e -> saveFileAs());
        JPanel fileButtons = new JPanel(new mpv5.ui.modern.WrapLayout(FlowLayout.LEFT, ModernTheme.px(6), ModernTheme.px(4)));
        fileButtons.setOpaque(false);
        for (JButton b : new JButton[]{edit, replace, saveAs}) {
            fileButtons.add(b);
            needsTemplate.add(b);
        }
        grid.add(new Card(Messages.TEMPLATES_FILE.toString())
                .addFull(hint(Messages.TEMPLATES_EDIT_HINT.toString()))
                .addFull(fileButtons)
                .addFull(editState)
                .addFull(file));

        //always a preview: the first page with a document, made again after each save in LibreOffice
        previewImage.setHorizontalAlignment(JLabel.CENTER);
        previewImage.setVerticalAlignment(JLabel.TOP);
        previewImage.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
        previewImage.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        previewImage.setToolTipText(Messages.TEMPLATES_PREVIEW_LARGER.toString());
        previewImage.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showLarger();
            }
        });
        previewCaption.setFont(ModernTheme.font(-1, Font.PLAIN));
        previewCaption.setForeground(ModernTheme.muted());
        JButton larger = new JButton(Messages.TEMPLATES_PREVIEW_LARGER.toString());
        larger.addActionListener(e -> showLarger());
        JButton otherDoc = new JButton(Messages.TEMPLATES_PREVIEW_DOCUMENT.toString());
        otherDoc.addActionListener(e -> choosePreviewDocument());
        JButton vars = new JButton(Messages.TEMPLATES_VARIABLES.toString());
        vars.addActionListener(e -> showVariables());
        JPanel previewButtons = new JPanel(new mpv5.ui.modern.WrapLayout(FlowLayout.LEFT, ModernTheme.px(6), ModernTheme.px(4)));
        previewButtons.setOpaque(false);
        for (JButton b : new JButton[]{larger, otherDoc}) {
            previewButtons.add(b);
            needsTemplate.add(b);
        }
        JPanel varsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, ModernTheme.px(4)));
        varsRow.setOpaque(false);
        varsRow.add(vars);
        needsTemplate.add(vars);

        //the position table
        formatLabel.setFont(ModernTheme.font(0, Font.PLAIN));
        JButton columns = new JButton(Messages.TEMPLATES_CHOOSE_COLUMNS.toString());
        columns.addActionListener(e -> chooseColumns());
        needsTemplate.add(columns);
        JPanel columnsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, ModernTheme.px(4)));
        columnsRow.setOpaque(false);
        columnsRow.add(columns);
        grid.add(new Card(Messages.TEMPLATES_POSITIONS.toString())
                .addFull(hint(Messages.TEMPLATES_FORMAT_HINT.toString()))
                .addFull(formatLabel)
                .addFull(columnsRow));

        //payment term
        grid.add(new Card(Messages.TEMPLATES_PAYTERM_CARD.toString())
                .row(Card.field(Messages.TEMPLATE_EINVOICE_PAYTERM.toString(), einvoicePayterm))
                .addFull(hint(Messages.TEMPLATES_PAYTERM_HINT.toString())));

        //printing
        grid.add(new Card(Messages.TEMPLATES_PRINTING.toString())
                .row(Card.field(Messages.TEMPLATES_PRINTER.toString(), printern))
                .addFull(hint(Messages.TEMPLATES_PRINTER_HINT.toString())));

        //the preview is the tallest card: last
        grid.addFullWidth(new Card(Messages.TEMPLATES_PREVIEW.toString())
                .addFull(hint(Messages.TEMPLATES_PREVIEW_HINT.toString()))
                .addFull(previewImage)
                .addFull(previewCaption)
                .addFull(previewButtons)
                .addFull(hint(Messages.TEMPLATES_VARIABLES_HINT.toString()))
                .addFull(varsRow));

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(title, BorderLayout.NORTH);
        right.add(grid, BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);

        JButton delete = new JButton(Messages.TEMPLATES_DELETE.toString());
        delete.setForeground(new Color(185, 28, 28));
        delete.addActionListener(e -> delete());
        needsTemplate.add(delete);
        PrimaryButton save = new PrimaryButton(Messages.USERS_SAVE.toString(), "/images/16/filesave.png");
        save.addActionListener(e -> save());
        needsTemplate.add(save);
        JPanel l = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), ModernTheme.px(6)));
        l.setOpaque(false);
        l.add(delete);
        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), ModernTheme.px(6)));
        r.setOpaque(false);
        r.add(save);
        actions.setOpaque(false);
        actions.add(l, BorderLayout.WEST);
        actions.add(r, BorderLayout.EAST);
    }

    private static JLabel hint(String text) {
        //plain text: placeholders like <item.paymentiban> are shown, not read as HTML
        JLabel l = new JLabel("<html>" + text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</html>");
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
            mpv5.handler.TemplateEditing.stop(dataOwner);
            TemplateHandler.clearCache();
            dataOwner.delete();
            dataOwner = null;
            refresh();
        }
    }

    /**
     * All placeholders with their value in the document of the preview,
     * searchable
     */
    private void showVariables() {
        DatabaseObject t = previewDocument();
        if (t != null) {
            mpv5.ui.dialogs.PlaceholderList.show(this, t);
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
                d.saveFile(dataOwner.getFile());
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
                //a new file: an edit in LibreOffice starts again from it
                mpv5.handler.TemplateEditing.stop(dataOwner);
                mpv5.handler.TemplateEditing.store(dataOwner, fi, Messages.TEMPLATES_FILE_REPLACED.toString());
                dataOwner.forgetFile();
                mpv5.YabsViewProxy.instance().addMessage(String.format(Messages.TEMPLATES_FILE_REPLACED.toString(), dataOwner.__getCname()));
                showFileState();
                refreshPreview();
            }
        }
    }

    /**
     * Opens the template in LibreOffice (or the program for its file type);
     * every save there is taken over (see TemplateEditing)
     */
    private void editFile() {
        if (dataOwner == null || !dataOwner.isExisting()) {
            return;
        }
        try {
            File work = mpv5.handler.TemplateEditing.start(dataOwner);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(work);
            } else if (LocalSettings.getBooleanProperty(LocalSettings.OFFICE_USE)) {
                FileExecutor.run(LocalSettings.getProperty(LocalSettings.OFFICE_HOME) + File.separator + work.getPath());
            } else {
                Popup.notice(Messages.TEMPLATES_CANNOT_OPEN.toString() + "\n" + work.getPath());
            }
        } catch (Exception e) {
            Log.Debug(e);
            Popup.error(e);
        }
        showFileState();
    }

    /**
     * The size of the file and whether it is edited in LibreOffice
     */
    private void showFileState() {
        if (dataOwner == null || !dataOwner.isExisting()) {
            file.setText(" ");
            editState.setText(" ");
            return;
        }
        try {
            File f = dataOwner.getFile();
            file.setText(Messages.TEMPLATES_FILE_SIZE + ": " + Math.max(1, f.length() / 1024) + " KB");
        } catch (Exception e) {
            //the file could not be read: say so, do not offer to delete the template
            Log.Debug(this, e);
            file.setText("<html><font color='#b91c1c'>" + Messages.TEMPLATES_FILE_MISSING + "</font></html>");
        }
        File work = mpv5.handler.TemplateEditing.workingFile(dataOwner);
        if (work == null) {
            editState.setText(" ");
        } else {
            Date last = mpv5.handler.TemplateEditing.lastStored(dataOwner);
            String state = last == null ? Messages.TEMPLATES_EDIT_WAITING.toString()
                    : String.format(Messages.TEMPLATES_EDIT_LAST.toString(),
                            mpv5.utils.date.DateConverter.getDefDateString(last) + " "
                            + new java.text.SimpleDateFormat("HH:mm:ss").format(last));
            editState.setText("<html><b>" + Messages.TEMPLATES_EDITING + "</b> \u00b7 " + state
                    + "<br><font color='#64748b'>" + work.getPath() + "</font></html>");
        }
    }

    /**
     * @param format e.g. "1,2,4,6"
     * @return the names of the columns, e.g. "No. · Quantity · Description ·
     * Total net"
     */
    static String columnNames(String format) {
        String[] names = Messages.TEMPLATES_COLUMNS.toString().split("\\|");
        List<String> l = new ArrayList<String>();
        for (int c : columns(format)) {
            l.add(c <= names.length ? names[c - 1] : String.valueOf(c));
        }
        return l.isEmpty() ? Messages.TEMPLATES_COLUMNS_NONE.toString() : String.join(" \u00b7 ", l);
    }

    /**
     * @return the column numbers of a format, invalid parts left out
     */
    static List<Integer> columns(String format) {
        List<Integer> l = new ArrayList<Integer>();
        for (String s : (format == null ? "" : format).split(",")) {
            try {
                int c = Integer.parseInt(s.trim());
                if (c > 0 && !l.contains(c)) {
                    l.add(c);
                }
            } catch (NumberFormatException e) {
                //not a column
            }
        }
        return l;
    }

    private void showFormat() {
        formatLabel.setText("<html>" + columnNames(formatValue) + "</html>");
    }

    /**
     * The columns of the position table: tick them and put them in order
     */
    private void chooseColumns() {
        String[] names = Messages.TEMPLATES_COLUMNS.toString().split("\\|");
        final javax.swing.DefaultListModel<JCheckBox> model = new javax.swing.DefaultListModel<JCheckBox>();
        List<Integer> chosen = columns(formatValue);
        List<Integer> order = new ArrayList<Integer>(chosen);
        for (int c = 1; c <= names.length; c++) {
            if (!order.contains(c)) {
                order.add(c);
            }
        }
        for (int c : order) {
            JCheckBox b = new JCheckBox(c <= names.length ? names[c - 1] : String.valueOf(c), chosen.contains(c));
            b.putClientProperty("column", c);
            model.addElement(b);
        }
        final javax.swing.JList<JCheckBox> list = new javax.swing.JList<JCheckBox>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer((l, value, index, selected, focus) -> {
            value.setOpaque(true);
            value.setBackground(selected ? l.getSelectionBackground() : l.getBackground());
            value.setForeground(selected ? l.getSelectionForeground() : l.getForeground());
            return value;
        });
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int i = list.locationToIndex(e.getPoint());
                if (i >= 0 && list.getCellBounds(i, i).contains(e.getPoint())) {
                    model.get(i).setSelected(!model.get(i).isSelected());
                    list.repaint();
                }
            }
        });
        list.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("SPACE"), "toggle");
        list.getActionMap().put("toggle", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int i = list.getSelectedIndex();
                if (i >= 0) {
                    model.get(i).setSelected(!model.get(i).isSelected());
                    list.repaint();
                }
            }
        });
        JButton up = new JButton("\u2191");
        JButton down = new JButton("\u2193");
        up.addActionListener(e -> move(list, model, -1));
        down.addActionListener(e -> move(list, model, 1));
        JPanel arrows = new JPanel(new GridLayout(0, 1, 0, ModernTheme.px(4)));
        arrows.add(up);
        arrows.add(down);
        JPanel arrowsTop = new JPanel(new BorderLayout());
        arrowsTop.add(arrows, BorderLayout.NORTH);
        JScrollPane sl = new JScrollPane(list);
        sl.setPreferredSize(new Dimension(ModernTheme.px(320), ModernTheme.px(300)));
        JPanel p = new JPanel(new BorderLayout(ModernTheme.px(8), ModernTheme.px(8)));
        p.add(hint(Messages.TEMPLATES_COLUMNS_HINT.toString()), BorderLayout.NORTH);
        p.add(sl, BorderLayout.CENTER);
        p.add(arrowsTop, BorderLayout.EAST);
        if (javax.swing.JOptionPane.showConfirmDialog(this, p, Messages.TEMPLATES_POSITIONS.toString(),
                javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE) == javax.swing.JOptionPane.OK_OPTION) {
            List<String> f = new ArrayList<String>();
            for (int i = 0; i < model.size(); i++) {
                if (model.get(i).isSelected()) {
                    f.add(String.valueOf(model.get(i).getClientProperty("column")));
                }
            }
            formatValue = String.join(",", f);
            showFormat();
        }
    }

    private static void move(javax.swing.JList<JCheckBox> list, javax.swing.DefaultListModel<JCheckBox> model, int by) {
        int i = list.getSelectedIndex();
        int j = i + by;
        if (i >= 0 && j >= 0 && j < model.size()) {
            JCheckBox b = model.remove(i);
            model.add(j, b);
            list.setSelectedIndex(j);
            list.ensureIndexIsVisible(j);
        }
    }

    /**
     * The printers of the system, the first entry is the default printer
     */
    private void fillPrinters(String current) {
        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
        m.addElement(Messages.TEMPLATES_PRINTER_DEFAULT.toString());
        try {
            for (PrintService s : PrintServiceLookup.lookupPrintServices(null, null)) {
                m.addElement(s.getName());
            }
        } catch (Exception e) {
            Log.Debug(this, e);
        }
        boolean set = current != null && !current.trim().isEmpty() && !Template.PRINTER_UNDEFINED.equals(current);
        if (set && m.getIndexOf(current) < 0) {
            //a printer not on this computer: keep it
            m.addElement(current);
        }
        printern.setModel(m);
        printern.setSelectedIndex(set ? m.getIndexOf(current) : 0);
    }

    /**
     * @return the document of the preview: the chosen one, else the first
     * invoice, else sample data
     */
    private DatabaseObject previewDocument() {
        if (previewDoc != null) {
            return previewDoc;
        }
        try {
            return DatabaseObject.getObject(Context.getInvoice(), 1);
        } catch (NodataFoundException ex) {
            Item t = (Item) Context.getInvoice().getNewObject();
            Contact k = new Contact();
            k.avoidNulls();
            k.fillSampleData();
            t.avoidNulls();
            t.fillSampleData();
            t.setContactsids(k.__getIDS());
            return t;
        }
    }

    /**
     * A PDF of the template with a document and 20 sample positions
     */
    public static File previewPdf(Template template, DatabaseObject document) throws Exception {
        Map<String, Object> hm1 = document.getFormFields();
        File f = template.getFile();
        File f2 = FileDirectoryHandler.getTempFile("pdf");
        Export ex = new Export(template);
        ex.putAll(hm1);
        ArrayList<String[]> l = new ArrayList<String[]>();
        for (int i = 0; i < 20; i++) {
            l.add(SubItem.getRandomItem().toStringArray());
        }
        ex.put(ODTFile2.KEY_TABLE + "1", l);
        ex.setTemplate(new ODTFile2(f.getPath()));
        ex.setTargetFile(f2);
        Exception e = ex.waitFor();
        if (e != null) {
            throw e;
        }
        return f2;
    }

    /**
     * Makes the preview again in the background (selection, new file)
     */
    private void refreshPreview() {
        final int run = ++previewRun;
        previewPdf = null;
        if (dataOwner == null || !dataOwner.isExisting()) {
            previewImage.setIcon(null);
            previewImage.setText(" ");
            previewCaption.setText(" ");
            return;
        }
        final Template tpl = dataOwner;
        final DatabaseObject doc = previewDocument();
        final int width = Math.max(ModernTheme.px(240), Math.min(ModernTheme.px(620),
                previewImage.getParent() == null ? 0 : previewImage.getParent().getWidth() - ModernTheme.px(24)));
        previewImage.setIcon(null);
        previewImage.setText(Messages.TEMPLATES_PREVIEW_LOADING.toString());
        previewImage.setPreferredSize(new Dimension(width, (int) (width * 1.414)));
        previewCaption.setText(" ");
        new javax.swing.SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                File pdf = previewPdf(tpl, doc);
                try (org.apache.pdfbox.pdmodel.PDDocument d = org.apache.pdfbox.Loader.loadPDF(pdf)) {
                    float pageWidth = d.getPage(0).getMediaBox().getWidth();
                    java.awt.image.BufferedImage img = new org.apache.pdfbox.rendering.PDFRenderer(d)
                            .renderImage(0, width / pageWidth);
                    return new Object[]{pdf, img, d.getNumberOfPages()};
                }
            }

            @Override
            protected void done() {
                if (run != previewRun) {
                    return;
                }
                try {
                    Object[] r = get();
                    previewPdf = (File) r[0];
                    java.awt.image.BufferedImage img = (java.awt.image.BufferedImage) r[1];
                    previewImage.setText(null);
                    previewImage.setIcon(new javax.swing.ImageIcon(img));
                    previewImage.setPreferredSize(new Dimension(img.getWidth() + 2, img.getHeight() + 2));
                    String what = doc.isExisting() ? doc.toString() : Messages.TEMPLATES_PREVIEW_SAMPLE.toString();
                    previewCaption.setText(String.format(Messages.TEMPLATES_PREVIEW_CAPTION.toString(), r[2], what));
                } catch (Exception e) {
                    Log.Debug(ControlPanel_Templates.this, e);
                    Throwable c = e.getCause() != null ? e.getCause() : e;
                    previewImage.setIcon(null);
                    previewImage.setText("<html><div style='padding:8px'>" + Messages.TEMPLATES_PREVIEW_FAILED + "<br><font color='#b91c1c'>"
                            + String.valueOf(c.getMessage()).replace("<", "&lt;") + "</font></div></html>");
                    previewImage.setPreferredSize(null);
                }
                previewImage.revalidate();
            }
        }.execute();
    }

    private void showLarger() {
        if (previewPdf != null && previewPdf.exists()) {
            mpv5.ui.dialogs.PdfPreview.show(previewPdf);
        }
    }

    /**
     * Another document for the preview and the fields
     */
    private void choosePreviewDocument() {
        DatabaseObject d = searchDocument();
        if (d != null) {
            previewDoc = d;
            refreshPreview();
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
        editState.setText(" ");
        refreshPreview();
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
        format_ = formatValue;
        printer_ = printern.getSelectedIndex() <= 0 || printern.getSelectedItem() == null
                ? Template.PRINTER_UNDEFINED : String.valueOf(printern.getSelectedItem());
        lastmodified_ = lastmodified;
        //the old watching of the imported file is replaced by "Edit in LibreOffice"
        isupdateenabled_ = false;
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
            formatValue = format_ == null || format_.trim().isEmpty() ? Template.DEFAULT_FORMAT : format_;
            showFormat();
            fillPrinters(printer_);

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
            lastmodified = lastmodified_;
            showFileState();
            refreshPreview();
        } catch (Exception e) {
            Log.Debug(this, e);
        } finally {
            loading = false;
        }
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
        if (dataOwner == null || object == null || !dataOwner.__getIDS().equals(object.__getIDS())) {
            //another template: the preview starts with the default document again
            previewDoc = null;
        }
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
}
