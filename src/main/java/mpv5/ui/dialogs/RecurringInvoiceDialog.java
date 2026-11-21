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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import mpv5.db.objects.Item;
import mpv5.db.objects.Schedule;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.beans.LabeledDateChooser;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.utils.date.DateConverter;

/**
 * Recurring invoice: the invoice is due again every n months, from the first
 * date to the end (shown as due on the start page and in the calendar). One
 * invoice can have several schedules; replaces the classic "Terminplaner"
 * window for invoices.
 */
public final class RecurringInvoiceDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final Item invoice;
    private final List<Schedule> schedules = new ArrayList<Schedule>();
    private final DefaultTableModel model = new DefaultTableModel(new Object[0][], new String[]{
        Messages.RECURRING_INTERVAL.toString(), Messages.RECURRING_START.toString(),
        Messages.RECURRING_NEXT.toString(), Messages.RECURRING_END.toString()}) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JSpinner interval = new JSpinner(new SpinnerNumberModel(1, 1, 120, 1));
    private final LabeledDateChooser start = ModernTheme.plain(new LabeledDateChooser());
    private final LabeledDateChooser end = ModernTheme.plain(new LabeledDateChooser());
    private final JButton delete = new JButton(Messages.RECURRING_DELETE.toString());
    private final PrimaryButton save = new PrimaryButton(Messages.RECURRING_SAVE.toString(), null);
    //what the form does: add a new schedule, or change the one selected in the list
    private final JLabel formTitle = ModernTheme.heading("");
    private final JButton cancelEdit = new JButton(Messages.RECURRING_CANCEL_EDIT.toString());
    private Schedule selected;

    /**
     * Shows the recurring invoices of the invoice
     *
     * @param invoice a saved invoice
     */
    public static void show(Item invoice) {
        if (invoice == null || !invoice.isExisting()) {
            return;
        }
        new RecurringInvoiceDialog(invoice).setVisible(true);
    }

    private RecurringInvoiceDialog(Item invoice) {
        super(mpv5.YabsViewProxy.instance().getIdentifierFrame(), Messages.RECURRING_TITLE.toString(), ModalityType.MODELESS);
        this.invoice = invoice;

        JPanel content = new JPanel(new BorderLayout(0, ModernTheme.px(10)));
        content.setBackground(ModernTheme.background());
        content.setBorder(ModernTheme.padding(14, 16));

        JPanel top = new JPanel(new BorderLayout(0, ModernTheme.px(4)));
        top.setOpaque(false);
        top.add(ModernTheme.heading(Messages.RECURRING_TITLE + ": " + invoice.__getCname()), BorderLayout.NORTH);
        JLabel info = new JLabel("<html><div style='width:" + ModernTheme.px(420) + "px'>"
                + Messages.RECURRING_INFO.toString().replace("&", "&amp;").replace("<", "&lt;") + "</div></html>");
        info.setForeground(ModernTheme.muted());
        top.add(info, BorderLayout.CENTER);
        content.add(top, BorderLayout.NORTH);

        //the schedules of the invoice
        ModernTheme.style(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                select(table.getSelectedRow());
            }
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(ModernTheme.px(460), ModernTheme.px(110)));

        //the form: new or selected schedule
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, ModernTheme.px(2), ModernTheme.px(12));
        g.gridy = 0;
        g.gridx = 0;
        form.add(ModernTheme.fieldLabel(Messages.RECURRING_INTERVAL.toString()), g);
        g.gridx = 1;
        form.add(ModernTheme.fieldLabel(Messages.RECURRING_START.toString()), g);
        g.gridx = 2;
        form.add(ModernTheme.fieldLabel(Messages.RECURRING_END.toString()), g);
        g.gridy = 1;
        g.gridx = 0;
        g.weightx = 0.2;
        form.add(interval, g);
        g.gridx = 1;
        g.weightx = 0.4;
        form.add(start, g);
        g.gridx = 2;
        g.weightx = 0.4;
        g.insets = new Insets(0, 0, ModernTheme.px(2), 0);
        form.add(end, g);

        JPanel formCard = new JPanel(new BorderLayout(0, ModernTheme.px(6)));
        formCard.setOpaque(false);
        formCard.add(formTitle, BorderLayout.NORTH);
        formCard.add(form, BorderLayout.CENTER);
        JPanel center = new JPanel(new BorderLayout(0, ModernTheme.px(12)));
        center.setOpaque(false);
        center.add(scroll, BorderLayout.CENTER);
        center.add(formCard, BorderLayout.SOUTH);
        content.add(center, BorderLayout.CENTER);

        //back to adding a new one (the selection in the list is the one being changed)
        cancelEdit.addActionListener(e -> table.clearSelection());
        delete.addActionListener(e -> deleteSelected());
        save.addActionListener(e -> save());
        JButton close = new JButton(Messages.RECURRING_CLOSE.toString());
        close.addActionListener(e -> dispose());
        content.add(Applets.actionBar(new JComponent[]{delete}, new JComponent[]{close, cancelEdit, save}), BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().setDefaultButton(save);
        getRootPane().registerKeyboardAction(e -> dispose(), javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        refresh();
        select(-1);
        pack();
        setLocationRelativeTo(getOwner());
    }

    /**
     * Loads the schedules of the invoice
     */
    private void refresh() {
        schedules.clear();
        schedules.addAll(Schedule.getEvents(invoice));
        model.setRowCount(0);
        for (Schedule s : schedules) {
            model.addRow(new Object[]{
                String.format(Messages.RECURRING_EVERY.toString(), s.__getIntervalmonth()),
                date(s.__getStartdate()), date(s.__getNextdate()), date(s.__getStopdate())});
        }
    }

    private static String date(Date d) {
        return d == null ? "–" : DateConverter.getDefDateString(d);
    }

    /**
     * Shows the schedule of the row in the form; -1: a new one
     */
    private void select(int row) {
        selected = row >= 0 && row < schedules.size() ? schedules.get(row) : null;
        if (selected != null) {
            interval.setValue(Math.max(1, selected.__getIntervalmonth()));
            start.setDate(selected.__getStartdate());
            end.setDate(selected.__getStopdate());
        } else {
            interval.setValue(1);
            start.setDate(new Date());
            end.setDate(DateConverter.addYear(new Date()));
        }
        //delete and cancel only for the selected schedule
        delete.setVisible(selected != null);
        cancelEdit.setVisible(selected != null);
        formTitle.setText(selected != null ? Messages.RECURRING_FORM_EDIT.toString() : Messages.RECURRING_FORM_NEW.toString());
        save.setText(selected != null ? Messages.RECURRING_SAVE.toString() : Messages.RECURRING_ADD.toString());
    }

    private void save() {
        Date from = start.getDate();
        Date to = end.getDate();
        if (from == null || to == null || to.before(from)) {
            Popup.notice(this, Messages.RECURRING_DATES);
            return;
        }
        Schedule s = selected != null ? selected : new Schedule();
        try {
            int months = ((Number) interval.getValue()).intValue();
            s.setCname("(" + Messages.SCHEDULE + ") " + invoice.toString());
            s.setItemsids(invoice.__getIDS());
            s.setGroupsids(invoice.__getGroupsids());
            s.setIntervalmonth(months);
            s.setStartdate(from);
            s.setStopdate(to);
            s.setUsersids(mpv5.db.objects.User.getCurrentUser().__getIDS());
            s.setNextdate(DateConverter.addMonths(from, months));
            s.setContactsids(invoice.__getContactsids());
            s.setEventtype(1);
            if (s.save()) {
                mpv5.YabsViewProxy.instance().addMessage(Messages.SCHEDULE_NEXT + DateConverter.getDefDateString(s.__getNextdate()));
                refresh();
                int row = schedules.indexOf(s);
                if (row < 0) {
                    for (int i = 0; i < schedules.size(); i++) {
                        if (schedules.get(i).__getIDS().equals(s.__getIDS())) {
                            row = i;
                        }
                    }
                }
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                } else {
                    select(-1);
                }
            }
        } catch (Exception ex) {
            Log.Debug(this, ex);
            Popup.error(ex);
        }
    }

    private void deleteSelected() {
        if (selected == null || !selected.isExisting()) {
            return;
        }
        if (!Popup.Y_N_dialog(this, Messages.RECURRING_DELETE_ASK, Messages.RECURRING_TITLE)) {
            return;
        }
        selected.delete();
        refresh();
        table.clearSelection();
        select(-1);
    }
}
