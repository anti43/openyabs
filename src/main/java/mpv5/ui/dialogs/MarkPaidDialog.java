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
package mpv5.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Item;
import mpv5.db.objects.Revenue;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.handler.Payments;
import mpv5.logging.Log;
import mpv5.ui.beans.DateChooser;
import mpv5.ui.modern.ModernTheme;
import mpv5.utils.date.DateConverter;
import mpv5.utils.numberformat.FormatNumber;

/**
 * Marks invoices, expenses and income as paid, several at once: a preview of
 * every selected record with what will change (status, payment date; for an
 * invoice with recorded part payments the open amount becomes a payment),
 * records which stay as they are (already paid, cancelled, no invoice) and
 * the sum; nothing is changed before "Mark as paid", "Cancel" changes
 * nothing.
 */
public final class MarkPaidDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    /**
     * What happens to one record
     */
    static final class Plan {

        final DatabaseObject obj;
        final String number;
        final String kind;
        final String name;
        final BigDecimal gross;
        final String now;
        final Date oldPayDate;
        /**
         * true: it will be marked as paid
         */
        final boolean change;
        /**
         * the open amount recorded as payment (invoices with part payments),
         * null otherwise
         */
        final BigDecimal payment;
        final String note;

        Plan(DatabaseObject obj, String number, String kind, String name, BigDecimal gross, String now, Date oldPayDate,
                boolean change, BigDecimal payment, String note) {
            this.obj = obj;
            this.number = number == null ? "" : number;
            this.kind = kind;
            this.name = name == null ? "" : name;
            this.gross = gross == null ? BigDecimal.ZERO : gross;
            this.now = now;
            this.oldPayDate = oldPayDate;
            this.change = change;
            this.payment = payment;
            this.note = note;
        }
    }

    /**
     * @param o an invoice, expense or income (others stay unchanged)
     * @return what marking it as paid does
     */
    static Plan plan(DatabaseObject o) {
        if (o instanceof Item) {
            Item i = (Item) o;
            int t = i.__getInttype();
            String kind = Item.getTypeString(t);
            String now = Item.getStatusString(i.__getIntstatus());
            BigDecimal gross = i.getGrossAmount();
            if (t != Constants.TYPE_INVOICE && t != Constants.TYPE_DEPOSIT && t != Constants.TYPE_PART_PAYMENT && t != Constants.TYPE_CREDIT) {
                return new Plan(o, i.__getCnumber(), kind, i.__getCname(), gross, now, i.__getDateend(), false, null, Messages.MARK_PAID_NOT_INVOICE.toString());
            }
            if (i.__getIntstatus() == Item.STATUS_PAID) {
                return new Plan(o, i.__getCnumber(), kind, i.__getCname(), gross, now, i.__getDateend(), false, null, Messages.MARK_PAID_ALREADY.toString());
            }
            if (i.__getIntstatus() == Item.STATUS_CANCELLED) {
                return new Plan(o, i.__getCnumber(), kind, i.__getCname(), gross, now, i.__getDateend(), false, null, Messages.MARK_PAID_CANCELLED.toString());
            }
            BigDecimal payment = null;
            try {
                if (Payments.hasPayments(i)) {
                    payment = Payments.open(i);
                }
            } catch (Exception e) {
                Log.Debug(MarkPaidDialog.class, e.getMessage());
            }
            String note = payment != null && payment.signum() > 0
                    ? String.format(Messages.MARK_PAID_REST.toString(), FormatNumber.formatLokalCurrency(payment))
                    : Messages.MARK_PAID_SET.toString();
            return new Plan(o, i.__getCnumber(), kind, i.__getCname(), gross, now, i.__getDateend(), true, payment, note);
        } else if (o instanceof Expense) {
            Expense e = (Expense) o;
            String kind = Messages.TYPE_EXPENSE.toString();
            String now = e.__getIspaid() ? Messages.STATUS_PAID.toString() : Messages.STATUS_UNPAID.toString();
            boolean change = !e.__getIspaid();
            return new Plan(o, e.__getCnumber(), kind, e.__getDescription(), e.__getBrutvalue(), now, e.__getDateend(), change, null,
                    change ? Messages.MARK_PAID_SET.toString() : Messages.MARK_PAID_ALREADY.toString());
        } else if (o instanceof Revenue) {
            Revenue r = (Revenue) o;
            String kind = Messages.TYPE_REVENUE.toString();
            String now = Item.getStatusString(r.__getStatus());
            String note;
            boolean change = false;
            if (r.__getStatus() == Item.STATUS_PAID) {
                note = Messages.MARK_PAID_ALREADY.toString();
            } else if (r.__getStatus() == Item.STATUS_CANCELLED) {
                note = Messages.MARK_PAID_CANCELLED.toString();
            } else {
                note = Messages.MARK_PAID_SET.toString();
                change = true;
            }
            return new Plan(o, r.__getCnumber(), kind, r.__getDescription(), r.__getBrutvalue(), now, r.__getDateend(), change, null, note);
        }
        return new Plan(o, "", "", String.valueOf(o), BigDecimal.ZERO, "", null, false, null, Messages.MARK_PAID_NOT_INVOICE.toString());
    }

    /**
     * Marks one record as paid on the given day
     *
     * @param p
     * @param day
     * @return true if saved
     */
    static boolean apply(Plan p, Date day) {
        if (!p.change) {
            return false;
        }
        if (p.obj instanceof Item) {
            Item i = (Item) p.obj;
            if (p.payment != null) {
                //part payments: the rest is a payment as well (sets the status and the payment date)
                return p.payment.signum() <= 0 || Payments.add(i, p.payment, day, Messages.MARK_PAID_PAYMENT_NOTE.toString());
            }
            i.setIntstatus(Item.STATUS_PAID);
            i.setDateend(day);
            return i.save(true);
        } else if (p.obj instanceof Expense) {
            Expense e = (Expense) p.obj;
            e.setIspaid(true);
            e.setDateend(day);
            return e.save(true);
        } else if (p.obj instanceof Revenue) {
            Revenue r = (Revenue) p.obj;
            r.setStatus(Item.STATUS_PAID);
            r.setDateend(day);
            return r.save(true);
        }
        return false;
    }

    private final List<Plan> plans = new ArrayList<Plan>();
    private final DateChooser date = new DateChooser();
    private final JLabel summary = new JLabel(" ");
    private final JButton ok = new mpv5.ui.modern.PrimaryButton(" ", null);
    private final JButton cancel = new JButton(Messages.NUMWIZ_CANCEL.toString());
    private final Model model = new Model();
    private final Runnable after;

    private MarkPaidDialog(Window owner, List<DatabaseObject> objects, Runnable after) {
        super(owner, Messages.MARK_PAID_TITLE.toString(), ModalityType.APPLICATION_MODAL);
        this.after = after;
        Set<DatabaseObject> seen = new LinkedHashSet<DatabaseObject>(objects);
        for (DatabaseObject o : seen) {
            plans.add(plan(o));
        }
        date.setDate(new Date());
        date.getSpinner().addChangeListener(e -> update());

        JPanel content = new JPanel(new BorderLayout(0, ModernTheme.px(10)));
        content.setBorder(ModernTheme.padding(14, 16));
        JPanel top = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        top.setOpaque(false);
        top.add(mpv5.ui.modern.Applets.hint(Messages.MARK_PAID_INFO.toString()), BorderLayout.NORTH);
        JPanel day = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), 0));
        day.setOpaque(false);
        day.add(ModernTheme.fieldLabel(Messages.MARK_PAID_DATE.toString()));
        date.setPreferredSize(new Dimension(ModernTheme.px(150), date.getPreferredSize().height));
        day.add(date);
        top.add(day, BorderLayout.CENTER);
        content.add(top, BorderLayout.NORTH);

        JTable table = new JTable(model);
        ModernTheme.style(table);
        table.setAutoCreateRowSorter(true);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                Plan p = plans.get(t.convertRowIndexToModel(row));
                if (!selected) {
                    c.setForeground(p.change ? t.getForeground() : ModernTheme.muted());
                }
                setHorizontalAlignment(t.convertColumnIndexToModel(column) == 3 ? RIGHT : LEFT);
                return c;
            }
        });
        int[] widths = {90, 110, 220, 90, 100, 230, 170};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(ModernTheme.px(widths[i]));
        }
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(ModernTheme.px(1000), ModernTheme.px(Math.min(420, 60 + plans.size() * 24))));
        scroll.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
        content.add(scroll, BorderLayout.CENTER);

        summary.setFont(ModernTheme.font(0, Font.BOLD));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(ok);
        JPanel bottom = new JPanel(new BorderLayout(ModernTheme.px(10), 0));
        bottom.setOpaque(false);
        bottom.add(summary, BorderLayout.CENTER);
        bottom.add(buttons, BorderLayout.EAST);
        content.add(bottom, BorderLayout.SOUTH);
        setContentPane(content);

        cancel.addActionListener(e -> dispose());
        ok.addActionListener(e -> run());
        getRootPane().setDefaultButton(ok);
        getRootPane().registerKeyboardAction(e -> dispose(), javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        update();
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * @return the chosen payment date (today if none)
     */
    private Date day() {
        Date d = date.hasDate() ? date.getDate() : null;
        return d == null ? new Date() : d;
    }

    private void update() {
        int n = 0;
        BigDecimal sum = BigDecimal.ZERO;
        for (Plan p : plans) {
            if (p.change) {
                n++;
                sum = sum.add(p.payment != null ? p.payment : p.gross);
            }
        }
        summary.setText(n == 0 ? Messages.MARK_PAID_NOTHING.toString()
                : String.format(Messages.MARK_PAID_SUMMARY.toString(), n, plans.size(), FormatNumber.formatLokalCurrency(sum)));
        ok.setText(String.format(Messages.MARK_PAID_APPLY.toString(), n));
        ok.setEnabled(n > 0);
        model.fireTableDataChanged();
    }

    private void run() {
        final Date d = day();
        ok.setEnabled(false);
        cancel.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<List<String>, Void>() {
            int done;

            @Override
            protected List<String> doInBackground() {
                List<String> failed = new ArrayList<String>();
                for (Plan p : plans) {
                    if (!p.change) {
                        continue;
                    }
                    try {
                        if (apply(p, d)) {
                            done++;
                        } else {
                            failed.add(p.number + " " + p.name);
                        }
                    } catch (Exception e) {
                        Log.Debug(MarkPaidDialog.class, e);
                        failed.add(p.number + " " + p.name + ": " + e.getMessage());
                    }
                }
                return failed;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                dispose();
                List<String> failed;
                try {
                    failed = get();
                } catch (Exception e) {
                    failed = new ArrayList<String>();
                    failed.add(String.valueOf(e.getMessage()));
                }
                String msg = String.format(Messages.MARK_PAID_DONE.toString(), done);
                if (!failed.isEmpty()) {
                    Popup.notice(failed, msg);
                } else {
                    mpv5.YabsViewProxy.instance().addMessage(msg, new Color(21, 128, 61));
                }
                if (after != null) {
                    after.run();
                }
            }
        }.execute();
    }

    private final class Model extends AbstractTableModel {

        private static final long serialVersionUID = 1L;
        private final String[] columns = {Messages.MARK_PAID_COL_DOC.toString(), Messages.MARK_PAID_COL_KIND.toString(),
            Messages.MARK_PAID_COL_NAME.toString(), Messages.MARK_PAID_COL_GROSS.toString(), Messages.MARK_PAID_COL_NOW.toString(),
            Messages.MARK_PAID_COL_CHANGE.toString(), Messages.MARK_PAID_DATE.toString()};

        @Override
        public int getRowCount() {
            return plans.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int c) {
            return columns[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            Plan p = plans.get(r);
            switch (c) {
                case 0:
                    return p.number;
                case 1:
                    return p.kind;
                case 2:
                    return p.name;
                case 3:
                    return FormatNumber.formatLokalCurrency(p.gross);
                case 4:
                    return p.now;
                case 5:
                    return p.note;
                default:
                    String old = p.oldPayDate == null ? "-" : DateConverter.getDefDateString(p.oldPayDate);
                    return p.change ? old + "  →  " + DateConverter.getDefDateString(day()) : old;
            }
        }
    }

    /**
     * Shows the preview for the records; marks them as paid only on "Mark as
     * paid"
     *
     * @param parent
     * @param objects invoices, expenses, income (others are shown as
     * unchanged)
     * @param after run after marking (e.g. reload the list), not on cancel
     */
    public static void show(Component parent, List<DatabaseObject> objects, Runnable after) {
        if (objects == null || objects.isEmpty()) {
            Popup.notice(Messages.SELECT_AN_INVOICE);
            return;
        }
        Window w = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        new MarkPaidDialog(w, objects, after).setVisible(true);
    }

    /**
     * @param table a table with the records in the first column of the model
     * @return the records of the selected rows
     */
    public static List<DatabaseObject> selected(JTable table) {
        List<DatabaseObject> l = new ArrayList<DatabaseObject>();
        for (int row : table.getSelectedRows()) {
            Object o = table.getModel().getValueAt(table.convertRowIndexToModel(row), 0);
            if (o instanceof DatabaseObject) {
                l.add((DatabaseObject) o);
            }
        }
        return l;
    }
}
