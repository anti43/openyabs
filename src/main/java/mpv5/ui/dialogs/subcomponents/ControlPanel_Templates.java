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
import java.awt.CardLayout;
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
import javax.swing.KeyStroke;
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
import mpv5.handler.TemplateImportService;
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
 * users), the selected one in cards on the right (or bulk edit controls when
 * multiple templates are selected): what it is for, who uses it, printing and
 * the template file.
 */
public final class ControlPanel_Templates extends JPanel implements ControlApplet, DataPanel {

    private static final long serialVersionUID = 3L;
    private static final String CARD_SINGLE = "single";
    private static final String CARD_BULK = "bulk";
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "templates";
    private Template dataOwner;
    private long lastmodified;

    private final JTable templates = new JTable();
    private final List<Template> rows = new ArrayList<Template>();
    private final CardLayout rightCards = new CardLayout();
    private final JPanel rightPanel = new JPanel(rightCards);
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
    private volatile int previewRun;
    private final JPanel actions = new JPanel(new BorderLayout());
    private JButton deleteButton;
    private PrimaryButton saveButton;
    private final List<JComponent> needsTemplate = new ArrayList<JComponent>();

    // Bulk edit controls (shown when 2+ templates are selected)
    private final JLabel bulkTitle = new JLabel();
    private final JLabel bulkNames = new JLabel();
    private final JCheckBox bulkApplyType = new JCheckBox(Messages.TEMPLATES_BULK_CHANGE.toString());
    private final JComboBox<MPComboBoxModelItem> bulkType = new JComboBox<MPComboBoxModelItem>();
    private final JCheckBox bulkApplyGroup = new JCheckBox(Messages.TEMPLATES_BULK_CHANGE.toString());
    private final JComboBox bulkGroup = new JComboBox();
    private final JCheckBox bulkApplyUsers = new JCheckBox(Messages.TEMPLATES_BULK_CHANGE.toString());
    private final JPanel bulkUserBoxes = new JPanel(new GridLayout(0, 2, ModernTheme.px(8), ModernTheme.px(2)));
    private final List<JCheckBox> bulkUserChecks = new ArrayList<JCheckBox>();
    private final List<User> bulkAllUsers = new ArrayList<User>();
    private final JCheckBox bulkApplyFormat = new JCheckBox(Messages.TEMPLATES_BULK_CHANGE.toString());
    private String bulkFormatValue = Template.DEFAULT_FORMAT;
    private final JLabel bulkFormatLabel = new JLabel();
    private final JCheckBox bulkApplyPayterm = new JCheckBox(Messages.TEMPLATES_BULK_CHANGE.toString());
    private final JTextField bulkPayterm = new JTextField();
    private final JCheckBox bulkApplyPrinter = new JCheckBox(Messages.TEMPLATES_BULK_CHANGE.toString());
    private final JComboBox<String> bulkPrinter = new JComboBox<String>();
    private boolean bulkMode;

    private final mpv5.handler.TemplateEditing.Listener editListener = (tpl, when) -> javax.swing.SwingUtilities.invokeLater(() -> {
        mpv5.YabsViewProxy.instance().addMessage(String.format(Messages.TEMPLATES_FILE_CHANGED.toString(), tpl.__getCname()));
        if (!bulkMode && dataOwner != null && dataOwner.isExisting() && dataOwner.__getIDS().intValue() == tpl.__getIDS().intValue()) {
            dataOwner.forgetFile();
            showFileState();
            refreshPreview();
        }
    });
    private boolean loading;

    public ControlPanel_Templates() {
        putClientProperty(mpv5.ui.misc.Help.PROPERTY_HELP_PAGE, "Vorlagen|Templates-(EN)");
        if (MPSecurityManager.checkAdminAccess()) {
            build();
            MPComboBoxModelItem[] typeItems = MPComboBoxModelItem.toItems(TemplateHandler.getTypes());
            type.setModel(new DefaultComboBoxModel<MPComboBoxModelItem>(typeItems));
            bulkType.setModel(new DefaultComboBoxModel<MPComboBoxModelItem>(typeItems.clone()));
            MPComboBoxModelItem[] groupItems = MPComboBoxModelItem.toItems(
                    new DatabaseSearch(Context.getGroup()).getValuesFor(Context.getGroup().getSubID()));
            groupname.setModel(new DefaultComboBoxModel(groupItems));
            bulkGroup.setModel(new DefaultComboBoxModel(groupItems.clone()));
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

        //left: the templates (supports multi-select for bulk edit and multi-delete)
        templates.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        templates.setName("templatesTable");
        templates.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !loading) {
                onSelectionChanged();
            }
        });
        templates.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0), "deleteTemplates");
        templates.getActionMap().put("deleteTemplates", new javax.swing.AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                delete();
            }
        });
        ModernTheme.style(templates);
        JButton add = new JButton("+ " + Messages.TEMPLATES_IMPORT);
        add.setName("importTemplatesButton");
        ModernTheme.actionButton(add, null);
        add.setToolTipText(Messages.TEMPLATES_IMPORT_MULTI_TIP.toString());
        add.addActionListener(e -> importTemplate());
        Card left = new Card(Messages.TEMPLATES_LIST.toString()).headerAction(add);
        JScrollPane st = new JScrollPane(templates);
        st.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
        left.addFull(st, 1);
        left.addFull(hint(Messages.TEMPLATES_MULTI_HINT.toString()));
        left.setPreferredSize(new Dimension(ModernTheme.px(430), ModernTheme.px(420)));
        add(left, BorderLayout.WEST);

        //right: CardLayout switching between single-template editor and bulk-edit panel
        rightPanel.setOpaque(false);
        rightPanel.add(buildSinglePanel(), CARD_SINGLE);
        rightPanel.add(buildBulkPanel(), CARD_BULK);
        add(rightPanel, BorderLayout.CENTER);

        deleteButton = new JButton(Messages.TEMPLATES_DELETE.toString());
        deleteButton.setName("deleteTemplatesButton");
        deleteButton.setForeground(new Color(185, 28, 28));
        deleteButton.addActionListener(e -> delete());
        needsTemplate.add(deleteButton);
        saveButton = new PrimaryButton(Messages.USERS_SAVE.toString(), "/images/16/filesave.png");
        saveButton.setName("saveTemplatesButton");
        saveButton.addActionListener(e -> saveOrApplyBulk());
        needsTemplate.add(saveButton);
        JPanel l = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), ModernTheme.px(6)));
        l.setOpaque(false);
        l.add(deleteButton);
        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), ModernTheme.px(6)));
        r.setOpaque(false);
        r.add(saveButton);
        actions.setOpaque(false);
        actions.add(l, BorderLayout.WEST);
        actions.add(r, BorderLayout.EAST);
    }

    private JPanel buildSinglePanel() {
        title.setFont(ModernTheme.font(8, Font.BOLD));
        title.setBorder(BorderFactory.createEmptyBorder(0, ModernTheme.px(4), ModernTheme.px(6), 0));
        for (JTextField t : new JTextField[]{fullname, einvoicePayterm, bulkPayterm}) {
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
                .addFull(columnsRow)
                .addFull(hint(Messages.TEMPLATES_VARIABLES_HINT.toString()))
                .addFull(varsRow));

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
                .addFull(previewImage)
                .addFull(previewCaption)
                .addFull(previewButtons));

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(title, BorderLayout.NORTH);
        right.add(grid, BorderLayout.CENTER);
        return right;
    }

    private JPanel buildBulkPanel() {
        bulkTitle.setFont(ModernTheme.font(8, Font.BOLD));
        bulkNames.setFont(ModernTheme.font(-1, Font.PLAIN));
        bulkNames.setForeground(ModernTheme.muted());
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.PAGE_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(0, ModernTheme.px(4), ModernTheme.px(8), 0));
        header.add(bulkTitle);
        header.add(bulkNames);
        header.add(hint(Messages.TEMPLATES_BULK_HINT.toString()));

        for (JCheckBox cb : new JCheckBox[]{bulkApplyType, bulkApplyGroup, bulkApplyUsers, bulkApplyFormat, bulkApplyPayterm, bulkApplyPrinter}) {
            cb.setOpaque(false);
            cb.setFont(ModernTheme.font(0, Font.BOLD));
        }
        bulkApplyType.setName("bulkApplyType");
        bulkApplyGroup.setName("bulkApplyGroup");
        bulkApplyUsers.setName("bulkApplyUsers");
        bulkApplyFormat.setName("bulkApplyFormat");
        bulkApplyPayterm.setName("bulkApplyPayterm");
        bulkApplyPrinter.setName("bulkApplyPrinter");

        // Auto-check the corresponding "Change" box when the user interacts with a bulk control
        bulkType.addActionListener(e -> {
            if (!loading) {
                bulkApplyType.setSelected(true);
            }
        });
        bulkGroup.addActionListener(e -> {
            if (!loading) {
                bulkApplyGroup.setSelected(true);
            }
        });
        bulkPrinter.addActionListener(e -> {
            if (!loading) {
                bulkApplyPrinter.setSelected(true);
            }
        });
        bulkPayterm.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                if (!loading) {
                    bulkApplyPayterm.setSelected(true);
                }
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                if (!loading) {
                    bulkApplyPayterm.setSelected(true);
                }
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
            }
        });

        ResponsiveGrid grid = new ResponsiveGrid(ModernTheme.px(360), 2);

        // Card 1: Document type & group
        JPanel typeRow = new JPanel(new BorderLayout(ModernTheme.px(8), 0));
        typeRow.setOpaque(false);
        typeRow.add(bulkApplyType, BorderLayout.WEST);
        typeRow.add(bulkType, BorderLayout.CENTER);
        JPanel groupRow = new JPanel(new BorderLayout(ModernTheme.px(8), 0));
        groupRow.setOpaque(false);
        groupRow.add(bulkApplyGroup, BorderLayout.WEST);
        groupRow.add(bulkGroup, BorderLayout.CENTER);
        grid.add(new Card(Messages.TEMPLATES_WHAT.toString())
                .row(Card.field(Messages.TEMPLATES_TYPE.toString(), typeRow))
                .row(Card.field(Messages.TEMPLATES_GROUP.toString(), groupRow))
                .addFull(hint(Messages.TEMPLATES_GROUP_HINT.toString())));

        // Card 2: Users
        bulkUserBoxes.setOpaque(false);
        JButton bAll = link(Messages.TEMPLATES_ALL.toString(), () -> {
            bulkApplyUsers.setSelected(true);
            for (JCheckBox c : bulkUserChecks) {
                c.setSelected(true);
            }
        });
        JButton bNone = link(Messages.TEMPLATES_NONE.toString(), () -> {
            bulkApplyUsers.setSelected(true);
            for (JCheckBox c : bulkUserChecks) {
                c.setSelected(false);
            }
        });
        JPanel bHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(4), 0));
        bHeader.setOpaque(false);
        bHeader.add(bulkApplyUsers);
        bHeader.add(bAll);
        bHeader.add(bNone);
        grid.add(new Card(Messages.TEMPLATES_USERS.toString()).headerAction(bHeader)
                .addFull(hint(Messages.TEMPLATES_USERS_HINT.toString()))
                .addFull(bulkUserBoxes));

        // Card 3: Position table columns
        bulkFormatLabel.setFont(ModernTheme.font(0, Font.PLAIN));
        JButton bColumns = new JButton(Messages.TEMPLATES_CHOOSE_COLUMNS.toString());
        bColumns.setName("bulkChooseColumnsButton");
        bColumns.addActionListener(e -> chooseBulkColumns());
        JPanel bColRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, ModernTheme.px(4)));
        bColRow.setOpaque(false);
        bColRow.add(bColumns);
        grid.add(new Card(Messages.TEMPLATES_POSITIONS.toString()).headerAction(bulkApplyFormat)
                .addFull(hint(Messages.TEMPLATES_FORMAT_HINT.toString()))
                .addFull(bulkFormatLabel)
                .addFull(bColRow));

        // Card 4: Payment term
        JPanel payRow = new JPanel(new BorderLayout(ModernTheme.px(8), 0));
        payRow.setOpaque(false);
        payRow.add(bulkApplyPayterm, BorderLayout.WEST);
        payRow.add(bulkPayterm, BorderLayout.CENTER);
        grid.add(new Card(Messages.TEMPLATES_PAYTERM_CARD.toString())
                .row(Card.field(Messages.TEMPLATE_EINVOICE_PAYTERM.toString(), payRow))
                .addFull(hint(Messages.TEMPLATES_PAYTERM_HINT.toString())));

        // Card 5: Printer
        JPanel printRow = new JPanel(new BorderLayout(ModernTheme.px(8), 0));
        printRow.setOpaque(false);
        printRow.add(bulkApplyPrinter, BorderLayout.WEST);
        printRow.add(bulkPrinter, BorderLayout.CENTER);
        grid.add(new Card(Messages.TEMPLATES_PRINTING.toString())
                .row(Card.field(Messages.TEMPLATES_PRINTER.toString(), printRow))
                .addFull(hint(Messages.TEMPLATES_PRINTER_HINT.toString())));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("bulkEditPanel");
        panel.setOpaque(false);
        panel.add(header, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
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
    /**
     * @return all templates currently selected in the table (in view order)
     */
    public List<Template> getSelectedTemplates() {
        List<Template> selected = new ArrayList<Template>();
        for (int viewRow : templates.getSelectedRows()) {
            int modelRow = templates.convertRowIndexToModel(viewRow);
            if (modelRow >= 0 && modelRow < rows.size()) {
                selected.add(rows.get(modelRow));
            }
        }
        return selected;
    }

    private void onSelectionChanged() {
        List<Template> selected = getSelectedTemplates();
        if (selected.isEmpty()) {
            bulkMode = false;
            rightCards.show(rightPanel, CARD_SINGLE);
            updateActionButtons(0);
            empty();
            return;
        }
        if (selected.size() == 1) {
            bulkMode = false;
            rightCards.show(rightPanel, CARD_SINGLE);
            updateActionButtons(1);
            setDataOwner(selected.get(0), true);
        } else {
            bulkMode = true;
            dataOwner = selected.get(0);
            showBulkEditor(selected);
            rightCards.show(rightPanel, CARD_BULK);
            updateActionButtons(selected.size());
        }
    }

    private void updateActionButtons(int selectedCount) {
        if (deleteButton == null || saveButton == null) {
            return;
        }
        boolean any = selectedCount > 0;
        deleteButton.setEnabled(any);
        saveButton.setEnabled(any);
        if (selectedCount > 1) {
            deleteButton.setText(String.format(Messages.TEMPLATES_DELETE_MANY.toString(), selectedCount));
            saveButton.setText(String.format(Messages.TEMPLATES_BULK_APPLY.toString(), selectedCount));
        } else {
            deleteButton.setText(Messages.TEMPLATES_DELETE.toString());
            saveButton.setText(Messages.USERS_SAVE.toString());
        }
    }

    private void showBulkEditor(List<Template> selected) {
        loading = true;
        try {
            bulkTitle.setText(String.format(Messages.TEMPLATES_BULK_TITLE.toString(), selected.size()));
            List<String> names = new ArrayList<String>();
            for (int i = 0; i < Math.min(selected.size(), 5); i++) {
                names.add(selected.get(i).__getCname());
            }
            String summary = String.join(", ", names);
            if (selected.size() > 5) {
                summary += " \u2026 (+" + (selected.size() - 5) + ")";
            }
            bulkNames.setText(summary);

            bulkApplyType.setSelected(false);
            bulkApplyGroup.setSelected(false);
            bulkApplyUsers.setSelected(false);
            bulkApplyFormat.setSelected(false);
            bulkApplyPayterm.setSelected(false);
            bulkApplyPrinter.setSelected(false);

            // Pre-populate controls from the first selected template as a sensible starting point
            Template first = selected.get(0);
            for (int i = 0; i < bulkType.getItemCount(); i++) {
                if (String.valueOf(bulkType.getItemAt(i).getId()).equals(first.__getMimetype())) {
                    bulkType.setSelectedIndex(i);
                    break;
                }
            }
            if (first.__getGroupsids() > 0) {
                int idx = MPComboBoxModelItem.getItemID(String.valueOf(first.__getGroupsids()), bulkGroup.getModel());
                if (idx >= 0) {
                    bulkGroup.setSelectedIndex(idx);
                }
            }
            bulkFormatValue = first.__getFormat() == null || first.__getFormat().trim().isEmpty()
                    ? Template.DEFAULT_FORMAT : first.__getFormat();
            bulkFormatLabel.setText("<html>" + columnNames(bulkFormatValue) + "</html>");
            int days = mpv5.utils.export.ZugpferdFile.paytermOf(first);
            bulkPayterm.setText(days >= 0 ? String.valueOf(days) : "");
            fillPrinters(bulkPrinter, first.__getPrinter());

            // Populate user checkboxes (checked if all selected templates share the user)
            Set<Integer> commonUsers = null;
            for (Template t : selected) {
                Set<Integer> assigned = new HashSet<Integer>();
                for (User u : TemplateImportService.loadAssignedUsers(t)) {
                    assigned.add(u.__getIDS());
                }
                if (commonUsers == null) {
                    commonUsers = assigned;
                } else {
                    commonUsers.retainAll(assigned);
                }
            }
            bulkUserBoxes.removeAll();
            bulkUserChecks.clear();
            bulkAllUsers.clear();
            for (User u : TemplateImportService.loadAllUsers()) {
                String name = u.__getFullname() == null || u.__getFullname().trim().isEmpty() ? u.__getCname() : u.__getFullname();
                JCheckBox c = new JCheckBox(name, commonUsers != null && commonUsers.contains(u.__getIDS()));
                c.setOpaque(false);
                c.setToolTipText(u.__getCname());
                c.addActionListener(e -> {
                    if (!loading) {
                        bulkApplyUsers.setSelected(true);
                    }
                });
                bulkUserChecks.add(c);
                bulkAllUsers.add(u);
                bulkUserBoxes.add(c);
            }
            bulkUserBoxes.revalidate();
            bulkUserBoxes.repaint();
        } finally {
            loading = false;
        }
    }

    private void importTemplate() {
        DialogForFile di = new DialogForFile(DialogForFile.FILES_ONLY);
        di.setFileFilter(DialogForFile.TEMPLATE_IMPORT_FILES);
        if (di.chooseFiles()) {
            importFiles(di.getFiles());
        }
    }

    /**
     * Imports one or more {@code .odt} or {@code .zip} files in the background,
     * guesses each template's document type from its filename, assigns every
     * user by default, and refreshes the table.
     */
    void importFiles(final File[] chosenFiles) {
        if (chosenFiles == null || chosenFiles.length == 0) {
            return;
        }
        final int defaultGroupId = User.getCurrentUser() != null && User.getCurrentUser().__getGroupsids() > 0
                ? User.getCurrentUser().__getGroupsids() : 1;
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
        new javax.swing.SwingWorker<List<Template>, Void>() {
            @Override
            protected List<Template> doInBackground() throws Exception {
                return TemplateImportService.importFiles(chosenFiles, defaultGroupId);
            }

            @Override
            protected void done() {
                setCursor(java.awt.Cursor.getDefaultCursor());
                try {
                    List<Template> imported = get();
                    if (imported.isEmpty()) {
                        Popup.notice(Messages.TEMPLATES_IMPORT_NONE.toString());
                        return;
                    }
                    mpv5.YabsViewProxy.instance().refreshOpenTabsTemplates();
                    refresh();
                    selectMany(imported);
                    if (imported.size() == 1) {
                        mpv5.YabsViewProxy.instance().addMessage(Messages.TEMPLATES_IMPORTED + ": " + imported.get(0).__getCname());
                    } else {
                        Popup.notice(String.format(Messages.TEMPLATES_IMPORTED_MANY.toString(), imported.size()));
                    }
                } catch (Exception ex) {
                    Log.Debug(ControlPanel_Templates.this, ex);
                    Popup.error(ex);
                }
            }
        }.execute();
    }

    private void saveOrApplyBulk() {
        if (bulkMode) {
            applyBulkEdit();
        } else {
            save();
        }
    }

    private void applyBulkEdit() {
        List<Template> selected = getSelectedTemplates();
        if (selected.isEmpty()) {
            return;
        }
        TemplateImportService.BulkEditSpec spec = new TemplateImportService.BulkEditSpec();
        if (bulkApplyType.isSelected() && bulkType.getSelectedItem() != null) {
            spec.applyType = true;
            spec.mimetype = String.valueOf(((MPComboBoxModelItem) bulkType.getSelectedItem()).getId());
        }
        if (bulkApplyGroup.isSelected() && bulkGroup.getSelectedItem() != null) {
            spec.applyGroup = true;
            spec.groupId = Integer.parseInt(((MPComboBoxModelItem) bulkGroup.getSelectedItem()).getId());
        }
        if (bulkApplyUsers.isSelected()) {
            List<User> chosen = new ArrayList<User>();
            for (int i = 0; i < bulkUserChecks.size(); i++) {
                if (bulkUserChecks.get(i).isSelected()) {
                    chosen.add(bulkAllUsers.get(i));
                }
            }
            if (chosen.isEmpty() && !Popup.Y_N_dialog(Messages.TEMPLATES_NO_USERS.toString())) {
                return;
            }
            spec.applyUsers = true;
            spec.users = chosen;
        }
        if (bulkApplyFormat.isSelected()) {
            spec.applyFormat = true;
            spec.format = bulkFormatValue;
        }
        if (bulkApplyPrinter.isSelected()) {
            spec.applyPrinter = true;
            spec.printer = bulkPrinter.getSelectedIndex() <= 0 || bulkPrinter.getSelectedItem() == null
                    ? Template.PRINTER_UNDEFINED : String.valueOf(bulkPrinter.getSelectedItem());
        }
        if (bulkApplyPayterm.isSelected()) {
            String v = bulkPayterm.getText() == null ? "" : bulkPayterm.getText().trim();
            int days;
            try {
                days = v.isEmpty() ? mpv5.utils.export.ZugpferdFile.PAYTERM_UNSET : Integer.parseInt(v);
                if (!v.isEmpty() && days < 0) {
                    throw new NumberFormatException(v);
                }
            } catch (NumberFormatException e) {
                Popup.warn(Messages.TEMPLATE_EINVOICE_PAYTERM + ": " + v);
                return;
            }
            spec.applyPayterm = true;
            spec.paytermDays = days;
        }
        if (!spec.hasAnyChange()) {
            Popup.notice(Messages.TEMPLATES_BULK_NONE_CHECKED.toString());
            return;
        }
        int updated = TemplateImportService.applyBulkEdit(selected, spec);
        mpv5.YabsViewProxy.instance().refreshOpenTabsTemplates();
        refresh();
        selectMany(selected);
        mpv5.YabsViewProxy.instance().addMessage(String.format(Messages.TEMPLATES_BULK_SAVED.toString(), updated));
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
        List<Template> selected = getSelectedTemplates();
        if (selected.isEmpty() && dataOwner != null && dataOwner.isExisting()) {
            selected = java.util.Collections.singletonList(dataOwner);
        }
        if (selected.isEmpty()) {
            return;
        }
        String confirm = selected.size() == 1
                ? String.format(Messages.TEMPLATES_DELETE_CONFIRM.toString(), selected.get(0).__getCname())
                : String.format(Messages.TEMPLATES_DELETE_MANY_CONFIRM.toString(), selected.size());
        if (Popup.Y_N_dialog(confirm)) {
            int count = TemplateImportService.deleteTemplates(selected);
            mpv5.YabsViewProxy.instance().refreshOpenTabsTemplates();
            dataOwner = null;
            refresh();
            if (count > 1) {
                mpv5.YabsViewProxy.instance().addMessage(String.format(Messages.TEMPLATES_DELETED_MANY.toString(), count));
            }
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
        String chosen = showColumnsDialog(formatValue);
        if (chosen != null) {
            formatValue = chosen;
            showFormat();
        }
    }

    private void chooseBulkColumns() {
        String chosen = showColumnsDialog(bulkFormatValue);
        if (chosen != null) {
            bulkFormatValue = chosen;
            bulkFormatLabel.setText("<html>" + columnNames(bulkFormatValue) + "</html>");
            bulkApplyFormat.setSelected(true);
        }
    }

    private String showColumnsDialog(String initialFormat) {
        String[] names = Messages.TEMPLATES_COLUMNS.toString().split("\\|");
        final javax.swing.DefaultListModel<JCheckBox> model = new javax.swing.DefaultListModel<JCheckBox>();
        List<Integer> chosen = columns(initialFormat);
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
            return String.join(",", f);
        }
        return null;
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
        fillPrinters(printern, current);
    }

    private void fillPrinters(JComboBox<String> target, String current) {
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
        target.setModel(m);
        target.setSelectedIndex(set ? m.getIndexOf(current) : 0);
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

    private volatile javax.swing.SwingWorker<Object[], Void> previewWorker;
    private final Object previewLock = new Object();
    private int runningPreviews;

    /**
     * Cancels any pending or in-flight background preview rendering.
     */
    public void cancelPreview() {
        previewRun++;
        javax.swing.SwingWorker<Object[], Void> w = previewWorker;
        if (w != null && !w.isDone()) {
            w.cancel(true);
        }
    }

    /**
     * Waits up to {@code timeoutMs} milliseconds for any already-running
     * background preview worker to finish.
     */
    public void awaitPreviews(long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + Math.max(0L, timeoutMs);
        synchronized (previewLock) {
            while (runningPreviews > 0) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0L) {
                    break;
                }
                previewLock.wait(remaining);
            }
        }
    }

    /**
     * Makes the preview again in the background (selection, new file).
     * Execution is coalesced via {@code SwingUtilities.invokeLater} so rapid
     * selection changes or multi-selection gestures do not spawn redundant
     * export jobs.
     */
    private void refreshPreview() {
        cancelPreview();
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
        javax.swing.SwingWorker<Object[], Void> worker = new javax.swing.SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                synchronized (previewLock) {
                    runningPreviews++;
                }
                try {
                    if (isCancelled() || run != previewRun) {
                        return null;
                    }
                    File pdf = previewPdf(tpl, doc);
                    if (isCancelled() || run != previewRun) {
                        return null;
                    }
                    try (org.apache.pdfbox.pdmodel.PDDocument d = org.apache.pdfbox.Loader.loadPDF(pdf)) {
                        float pageWidth = d.getPage(0).getMediaBox().getWidth();
                        java.awt.image.BufferedImage img = new org.apache.pdfbox.rendering.PDFRenderer(d)
                                .renderImage(0, width / pageWidth);
                        return new Object[]{pdf, img, d.getNumberOfPages()};
                    }
                } finally {
                    synchronized (previewLock) {
                        runningPreviews--;
                        previewLock.notifyAll();
                    }
                }
            }

            @Override
            protected void done() {
                if (run != previewRun || isCancelled()) {
                    return;
                }
                try {
                    Object[] r = get();
                    if (r == null) {
                        return;
                    }
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
        };
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (run != previewRun) {
                return;
            }
            previewWorker = worker;
            worker.execute();
        });
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

    private void selectMany(List<Template> toSelect) {
        if (toSelect == null || toSelect.isEmpty()) {
            select(null);
            return;
        }
        Set<Integer> ids = new HashSet<Integer>();
        for (Template t : toSelect) {
            if (t != null && t.__getIDS() != null) {
                ids.add(t.__getIDS());
            }
        }
        loading = true;
        int firstView = -1;
        try {
            templates.clearSelection();
            for (int i = 0; i < rows.size(); i++) {
                if (ids.contains(rows.get(i).__getIDS())) {
                    int v = templates.convertRowIndexToView(i);
                    if (v >= 0) {
                        templates.addRowSelectionInterval(v, v);
                        if (firstView < 0) {
                            firstView = v;
                        }
                    }
                }
            }
        } finally {
            loading = false;
        }
        if (firstView >= 0) {
            templates.scrollRectToVisible(templates.getCellRect(firstView, 0, true));
            onSelectionChanged();
        } else {
            select(null);
        }
    }

    public boolean isBulkMode() {
        return bulkMode;
    }

    public JTable getTemplatesTable() {
        return templates;
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
        updateActionButtons(0);
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
        try {
            QueryHandler.instanceOf().clone(Context.getTemplatesToUsers())
                    .delete(new QueryCriteria("templatesids", dataOwner.__getIDS()));
        } catch (Exception ex) {
            Log.Debug(this, ex);
        }
        for (Integer user : wanted) {
            QueryData c = new QueryData();
            c.add("usersids", user);
            c.add("templatesids", dataOwner.__getIDS());
            c.add("groupsids", group);
            c.add("cname", dataOwner.__getIDS() + "@" + user + "@" + group);
            QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).insert(c, null);
        }
        DatabaseObject.uncacheObject(Context.getTemplate(), dataOwner.__getIDS());
        TemplateHandler.clearCache();
        mpv5.YabsViewProxy.instance().refreshOpenTabsTemplates();
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
