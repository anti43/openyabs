/*
 * This file is part of YaBS.
 *
 *    YaBS is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    YaBS is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Item;
import mpv5.db.objects.Reminder;
import mpv5.db.objects.Stage;
import mpv5.db.objects.Template;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.handler.TemplateHandler;
import mpv5.logging.Log;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.ui.panels.PreviewWaiter;
import mpv5.utils.date.DateConverter;
import mpv5.utils.export.Export;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.jobs.Job;
import mpv5.utils.numberformat.FormatNumber;

/**
 * The dunning letters (Mahnungen) of an invoice: the list, a form to create
 * or change one (level, fee, text), its preview and PDF with the template
 * "reminder" of the invoice's group. A level (Mahnstufe) is a reusable text
 * and fee. Replaces the classic reminder panel for invoices.
 */
public final class DunningDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final Item invoice;
    private final List<Reminder> reminders = new ArrayList<Reminder>();
    private final DefaultTableModel model = new DefaultTableModel(new Object[0][], new String[]{
        Messages.DUNNING_STAGE.toString(), Messages.DUNNING_FEE.toString(), Messages.DUNNING_DATE.toString()}) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JComboBox<Object> stage = new JComboBox<Object>();
    private final JTextField fee = new JTextField(8);
    private final JTextArea text = new JTextArea(6, 40);
    private final JLabel formTitle = ModernTheme.heading("");
    private final JButton delete = new JButton(Messages.DUNNING_DELETE.toString());
    private final JButton cancelEdit = new JButton(Messages.DUNNING_CANCEL.toString());
    private final JButton preview = new JButton(Messages.DUNNING_PREVIEW.toString());
    private final JButton pdf = new JButton(Messages.DUNNING_PDF.toString());
    private final PrimaryButton save = new PrimaryButton(Messages.DUNNING_ADD.toString(), null);
    private Reminder selected;
    private boolean filling;

    /**
     * Shows the dunning letters of the invoice
     *
     * @param invoice a saved invoice
     */
    public static void show(Item invoice) {
        if (invoice == null || !invoice.isExisting()) {
            return;
        }
        new DunningDialog(invoice).setVisible(true);
    }

    private DunningDialog(Item invoice) {
        super(mpv5.YabsViewProxy.instance().getIdentifierFrame(), Messages.DUNNING_TITLE.toString(), ModalityType.MODELESS);
        this.invoice = invoice;

        JPanel content = new JPanel(new BorderLayout(0, ModernTheme.px(10)));
        content.setBackground(ModernTheme.background());
        content.setBorder(ModernTheme.padding(14, 16));

        JPanel top = new JPanel(new BorderLayout(0, ModernTheme.px(4)));
        top.setOpaque(false);
        top.add(ModernTheme.heading(Messages.DUNNING_TITLE + ": " + invoice.__getCname()), BorderLayout.NORTH);
        JLabel info = new JLabel("<html><div style='width:" + ModernTheme.px(460) + "px'>"
                + Messages.DUNNING_INFO.toString().replace("&", "&amp;").replace("<", "&lt;") + "</div></html>");
        info.setForeground(ModernTheme.muted());
        top.add(info, BorderLayout.CENTER);
        content.add(top, BorderLayout.NORTH);

        //the dunning letters of the invoice
        ModernTheme.style(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                select(table.getSelectedRow());
            }
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(ModernTheme.px(500), ModernTheme.px(110)));

        //the form: level (fills fee and text), fee, text
        stage.addActionListener(e -> {
            if (!filling && stage.getSelectedItem() instanceof Stage) {
                Stage s = (Stage) stage.getSelectedItem();
                text.setText(s.__getDescription());
                fee.setText(FormatNumber.formatDezimal(s.__getExtravalue()));
            }
        });
        JButton stages = new JButton(Messages.DUNNING_STAGES + " ▾");
        JPopupMenu stageMenu = new JPopupMenu();
        JMenuItem saveStage = new JMenuItem(Messages.DUNNING_STAGE_SAVE.toString());
        saveStage.addActionListener(e -> saveAsStage());
        JMenuItem deleteStage = new JMenuItem(Messages.DUNNING_STAGE_DELETE.toString());
        deleteStage.addActionListener(e -> deleteStage());
        stageMenu.add(saveStage);
        stageMenu.add(deleteStage);
        stages.addActionListener(e -> {
            deleteStage.setEnabled(stage.getSelectedItem() instanceof Stage);
            stageMenu.show(stages, 0, stages.getHeight());
        });
        text.setLineWrap(true);
        text.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, ModernTheme.px(2), ModernTheme.px(10));
        g.gridy = 0;
        g.gridx = 0;
        form.add(ModernTheme.fieldLabel(Messages.DUNNING_STAGE.toString()), g);
        g.gridx = 2;
        form.add(ModernTheme.fieldLabel(Messages.DUNNING_FEE.toString()), g);
        g.gridy = 1;
        g.gridx = 0;
        g.weightx = 1;
        form.add(stage, g);
        g.gridx = 1;
        g.weightx = 0;
        form.add(stages, g);
        g.gridx = 2;
        g.insets = new Insets(0, 0, ModernTheme.px(2), 0);
        form.add(fee, g);
        g.gridy = 2;
        g.gridx = 0;
        g.gridwidth = 3;
        g.insets = new Insets(ModernTheme.px(8), 0, ModernTheme.px(2), 0);
        form.add(ModernTheme.fieldLabel(Messages.DUNNING_TEXT.toString()), g);
        g.gridy = 3;
        g.weighty = 1;
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(0, 0, 0, 0);
        form.add(new JScrollPane(text), g);

        JPanel formCard = new JPanel(new BorderLayout(0, ModernTheme.px(6)));
        formCard.setOpaque(false);
        formCard.add(formTitle, BorderLayout.NORTH);
        formCard.add(form, BorderLayout.CENTER);
        JPanel center = new JPanel(new BorderLayout(0, ModernTheme.px(12)));
        center.setOpaque(false);
        center.add(scroll, BorderLayout.NORTH);
        center.add(formCard, BorderLayout.CENTER);
        content.add(center, BorderLayout.CENTER);

        cancelEdit.addActionListener(e -> table.clearSelection());
        delete.addActionListener(e -> deleteSelected());
        preview.addActionListener(e -> export(true));
        pdf.addActionListener(e -> export(false));
        save.addActionListener(e -> save());
        JButton close = new JButton(Messages.DUNNING_CLOSE.toString());
        close.addActionListener(e -> dispose());
        content.add(Applets.actionBar(new JComponent[]{delete, preview, pdf}, new JComponent[]{close, cancelEdit, save}), BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().registerKeyboardAction(e -> dispose(), javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        loadStages(0);
        refresh();
        select(-1);
        pack();
        setLocationRelativeTo(getOwner());
    }

    /**
     * Fills the levels; the given one selected (0: none, the form keeps its
     * text)
     */
    private void loadStages(int selectId) {
        filling = true;
        try {
            DefaultComboBoxModel<Object> m = new DefaultComboBoxModel<Object>();
            m.addElement(" ");
            try {
                for (DatabaseObject o : DatabaseObject.getObjects(Context.getStage())) {
                    m.addElement(o);
                }
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
            stage.setModel(m);
            stage.setSelectedIndex(0);
            for (int i = 1; i < m.getSize(); i++) {
                if (((Stage) m.getElementAt(i)).__getIDS() == selectId) {
                    stage.setSelectedIndex(i);
                }
            }
        } finally {
            filling = false;
        }
    }

    /**
     * Loads the dunning letters of the invoice
     */
    private void refresh() {
        reminders.clear();
        reminders.addAll(Reminder.getRemindersOf(invoice));
        model.setRowCount(0);
        for (Reminder r : reminders) {
            model.addRow(new Object[]{stageName(r.__getStagesids()), FormatNumber.formatDezimal(r.__getExtravalue()),
                r.__getDateadded() == null ? "–" : DateConverter.getDefDateString(r.__getDateadded())});
        }
    }

    private static String stageName(int id) {
        try {
            return DatabaseObject.getObject(Context.getStage(), id).__getCname();
        } catch (Exception e) {
            return "–";
        }
    }

    /**
     * Shows the dunning letter of the row in the form; -1: a new one
     */
    private void select(int row) {
        selected = row >= 0 && row < reminders.size() ? reminders.get(row) : null;
        if (selected != null) {
            loadStages(selected.__getStagesids());
            fee.setText(FormatNumber.formatDezimal(selected.__getExtravalue()));
            text.setText(selected.__getDescription());
        } else {
            loadStages(0);
            //the next level as suggestion: the one after the last letter, else the first
            int next = reminders.isEmpty() ? 1 : indexOfStage(reminders.get(reminders.size() - 1).__getStagesids()) + 1;
            if (next > 0 && next < stage.getItemCount()) {
                stage.setSelectedIndex(next);
            } else {
                fee.setText("");
                text.setText("");
            }
        }
        text.setCaretPosition(0);
        boolean on = selected != null;
        delete.setVisible(on);
        cancelEdit.setVisible(on);
        preview.setEnabled(on);
        pdf.setEnabled(on);
        preview.setToolTipText(on ? null : Messages.DUNNING_SELECT.toString());
        pdf.setToolTipText(on ? null : Messages.DUNNING_SELECT.toString());
        formTitle.setText(on ? Messages.DUNNING_FORM_EDIT.toString() : Messages.DUNNING_FORM_NEW.toString());
        save.setText(on ? Messages.DUNNING_SAVE.toString() : Messages.DUNNING_ADD.toString());
    }

    private int indexOfStage(int id) {
        for (int i = 1; i < stage.getItemCount(); i++) {
            if (((Stage) stage.getItemAt(i)).__getIDS() == id) {
                return i;
            }
        }
        return 0;
    }

    /**
     * @return the fee as typed, in the format of the user's locale ("5,00"),
     * 0 if empty or not a number
     */
    private double fee() {
        BigDecimal v = FormatNumber.parseDezimal(fee.getText());
        return v == null ? 0d : FormatNumber.round(v).doubleValue();
    }

    private void save() {
        if (!(stage.getSelectedItem() instanceof Stage)) {
            Popup.notice(this, Messages.DUNNING_NO_STAGE);
            return;
        }
        Stage s = (Stage) stage.getSelectedItem();
        Reminder r = selected != null ? selected : new Reminder();
        r.setDescription(text.getText());
        r.setExtravalue(fee());
        r.setItemsids(invoice.__getIDS());
        r.setStagesids(s.__getIDS());
        //the template group of the invoice prints it
        r.setGroupsids(invoice.__getGroupsids());
        r.setCname(s + "@" + invoice);
        if (r.save()) {
            refresh();
            for (int i = 0; i < reminders.size(); i++) {
                if (reminders.get(i).__getIDS().equals(r.__getIDS())) {
                    table.setRowSelectionInterval(i, i);
                    return;
                }
            }
            select(-1);
        }
    }

    private void deleteSelected() {
        if (selected == null || !selected.isExisting()
                || !Popup.Y_N_dialog(this, Messages.DUNNING_DELETE_ASK, Messages.DUNNING_TITLE)) {
            return;
        }
        selected.delete();
        refresh();
        table.clearSelection();
        select(-1);
    }

    /**
     * Saves fee and text as a new level (asks for its name)
     */
    private void saveAsStage() {
        String name = Popup.Enter_Value(Messages.ENTER_A_DESCRIPTION);
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        Stage s = new Stage();
        s.setCname(name.trim());
        s.setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
        s.setTemplategroup(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
        s.setDescription(text.getText());
        s.setExtravalue(fee());
        if (s.save()) {
            loadStages(s.__getIDS());
        }
    }

    private void deleteStage() {
        if (!(stage.getSelectedItem() instanceof Stage)) {
            return;
        }
        Stage s = (Stage) stage.getSelectedItem();
        if (Popup.Y_N_dialog(this, String.format(Messages.DUNNING_STAGE_DELETE_ASK.toString(), s.__getCname()), Messages.DUNNING_STAGES)) {
            s.delete();
            loadStages(0);
        }
    }

    /**
     * The selected dunning letter with the template "reminder" of the
     * invoice's group: in the preview, or as PDF (save dialog)
     */
    private void export(boolean inPreview) {
        final Reminder r = selected;
        if (r == null || !r.isExisting()) {
            return;
        }
        final Template t = TemplateHandler.loadTemplate(invoice.templateGroupIds(), Constants.TYPE_REMINDER);
        if (t == null) {
            Popup.notice(this, Messages.NO_TEMPLATE_DEFINDED);
            return;
        }
        final Map<String, Object> extra = new HashMap<String, Object>();
        extra.put("reminder.stage", stageName(r.__getStagesids()));
        extra.put("reminder.count", reminders.size());
        if (inPreview) {
            final PreviewWaiter waiter = new PreviewWaiter();
            waiter.setDataOwner(r);
            mpv5.utils.export.ExportProgress.execute(r.__getCname(), Messages.EXPORT_STEP_FIELDS, () -> {
                Export ex = new Export(t);
                ex.putAll(r.getFormFields());
                ex.putAll(extra);
                ex.setTemplate(t.getExFile());
                ex.setTargetFile(FileDirectoryHandler.getTempFile("pdf"));
                return new Job(ex, waiter);
            }, Messages.EXPORT_STEP_OPEN);
        } else {
            final DialogForFile target = new DialogForFile(mpv5.db.objects.User.getSaveDir(r), r.__getCname());
            final HashMap<String, Object> fields = new HashMap<String, Object>(extra);
            mpv5.utils.export.ExportProgress.execute(r.__getCname(), Messages.EXPORT_STEP_FIELDS,
                    () -> new Job(Export.createFile(r.__getCname(), t, r, fields), target), Messages.EXPORT_STEP_SAVE);
        }
    }
}
