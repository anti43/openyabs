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
package mpv5.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Product;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;

/**
 * Stock booking with a barcode scanner or by hand: scan (or type) a code and
 * press Enter; the product is looked up right away, every further scan of the
 * same code counts up. "Book" adds the quantities to the stock (or takes them
 * off).
 */
public class Stockmanager extends JPanel {

    private static final long serialVersionUID = 2L;
    private static Stockmanager me;

    public static Stockmanager instanceOf() {
        if (me == null) {
            me = new Stockmanager();
        }
        return me;
    }

    /**
     * One scanned code
     */
    private static final class Line {

        String code;
        BigDecimal quantity = BigDecimal.ONE;
        Product product;
        String state = "";
        boolean booked;
    }

    /**
     * The product fields a code can be (field name, label)
     */
    private static final String[][] FIELDS = {{"ean", "EAN"}, {"cnumber", null}, {"cname", null}};

    private final List<Line> lines = new ArrayList<Line>();
    private final JTextField scan = new JTextField();
    private final JComboBox<String> field = new JComboBox<String>();
    private final JRadioButton in = new JRadioButton();
    private final JRadioButton out = new JRadioButton();
    private final JLabel status = new JLabel(" ");
    private final Model model = new Model();
    private final JTable table = new JTable(model);

    private Stockmanager() {
        super(new BorderLayout(0, ModernTheme.px(8)));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(12), ModernTheme.px(14), ModernTheme.px(12), ModernTheme.px(14)));

        FIELDS[1][1] = Messages.POS_FIELD_NUMBER.toString();
        FIELDS[2][1] = Messages.POS_FIELD_NAME.toString();
        for (String[] f : FIELDS) {
            field.addItem(f[1]);
        }
        in.setText(Messages.STOCK_IN.toString());
        out.setText(Messages.STOCK_OUT.toString());
        in.setOpaque(false);
        out.setOpaque(false);
        ButtonGroup g = new ButtonGroup();
        g.add(in);
        g.add(out);
        in.setSelected(true);
        in.addActionListener(e -> model.fireTableDataChanged());
        out.addActionListener(e -> model.fireTableDataChanged());

        scan.setColumns(24);
        scan.setFont(ModernTheme.font(3, java.awt.Font.PLAIN));
        scan.addActionListener(e -> add(scan.getText()));
        field.addActionListener(e -> {
            for (Line l : lines) {
                lookup(l);
            }
            model.fireTableDataChanged();
        });

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new javax.swing.BoxLayout(top, javax.swing.BoxLayout.PAGE_AXIS));
        JComponent hint = Applets.hint(Messages.STOCK_INFO.toString());
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), 0));
        row.setOpaque(false);
        row.add(new JLabel(Messages.STOCK_SCAN + ":"));
        row.add(scan);
        row.add(new JLabel(Messages.STOCK_CODE_IS.toString()));
        row.add(field);
        row.add(in);
        row.add(out);
        for (JComponent c : new JComponent[]{hint, row}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(c);
            top.add(javax.swing.Box.createVerticalStrut(ModernTheme.px(6)));
        }

        table.setRowHeight(ModernTheme.px(26));
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(ModernTheme.px(160));
        table.getColumnModel().getColumn(1).setPreferredWidth(ModernTheme.px(70));
        table.getColumnModel().getColumn(2).setPreferredWidth(ModernTheme.px(320));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                Line l = lines.get(t.convertRowIndexToModel(r));
                if (!sel) {
                    setForeground(l.product == null ? new Color(190, 40, 40)
                            : l.booked ? ModernTheme.muted() : ModernTheme.foreground());
                }
                setHorizontalAlignment(c == 1 || c >= 3 ? RIGHT : LEFT);
                return this;
            }
        });
        ModernTheme.style(table);
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(ModernTheme.cardBackground());

        PrimaryButton book = new PrimaryButton(Messages.STOCK_BOOK.toString(), "/images/16/filesave.png");
        book.addActionListener(e -> book());
        JButton remove = new JButton(Messages.ACTION_REMOVE.toString());
        remove.addActionListener(e -> {
            int[] rows = table.getSelectedRows();
            for (int i = rows.length - 1; i >= 0; i--) {
                lines.remove(table.convertRowIndexToModel(rows[i]));
            }
            model.fireTableDataChanged();
        });
        JButton clear = Applets.danger(new JButton(Messages.STOCK_CLEAR.toString()));
        clear.addActionListener(e -> {
            lines.clear();
            model.fireTableDataChanged();
            status.setText(" ");
            start();
        });
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(status, BorderLayout.CENTER);
        bottom.add(Applets.actionBar(new JComponent[]{clear}, new JComponent[]{remove, book}), BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Focus in the scan field
     */
    public void start() {
        scan.requestFocusInWindow();
    }

    private void add(String text) {
        String code = text == null ? "" : text.trim();
        scan.setText("");
        if (code.isEmpty()) {
            return;
        }
        for (Line l : lines) {
            if (l.code.equals(code) && !l.booked) {
                l.quantity = l.quantity.add(BigDecimal.ONE);
                model.fireTableDataChanged();
                return;
            }
        }
        Line l = new Line();
        l.code = code;
        lookup(l);
        lines.add(l);
        model.fireTableDataChanged();
        table.scrollRectToVisible(table.getCellRect(lines.size() - 1, 0, true));
    }

    private void lookup(Line l) {
        if (l.booked) {
            return;
        }
        try {
            l.product = (Product) DatabaseObject.getObject(Context.getProduct(), FIELDS[Math.max(0, field.getSelectedIndex())][0], l.code);
            l.state = "";
        } catch (Exception e) {
            l.product = null;
            l.state = Messages.STOCK_NOT_FOUND.toString();
        }
    }

    private static BigDecimal stock(Product p) {
        return p == null || p.__getStockvalue() == null ? BigDecimal.ZERO : p.__getStockvalue();
    }

    private BigDecimal change(Line l) {
        return out.isSelected() ? l.quantity.negate() : l.quantity;
    }

    /**
     * Adds (or takes off) the quantities of all found, not yet booked lines
     */
    private void book() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        int done = 0;
        int missing = 0;
        for (Line l : lines) {
            if (l.booked) {
                continue;
            }
            if (l.product == null) {
                missing++;
                continue;
            }
            try {
                //fresh from the database: the stock may have changed since the scan
                Product p = (Product) DatabaseObject.getObject(Context.getProduct(), l.product.__getIDS());
                p.setStockvalue(stock(p).add(change(l)));
                if (p.save(true)) {
                    l.product = p;
                    l.booked = true;
                    l.state = Messages.STOCK_BOOKED.toString();
                    done++;
                }
            } catch (Exception e) {
                Log.Debug(this, e);
                l.state = e.getMessage();
            }
        }
        model.fireTableDataChanged();
        status.setText(String.format(Messages.STOCK_RESULT.toString(), done, missing));
        if (missing > 0) {
            Popup.notice(String.format(Messages.STOCK_RESULT.toString(), done, missing));
        }
        start();
    }

    private final class Model extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return lines.size();
        }

        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public String getColumnName(int c) {
            switch (c) {
                case 0:
                    return Messages.STOCK_COL_CODE.toString();
                case 1:
                    return Messages.STOCK_COL_QUANTITY.toString();
                case 2:
                    return Messages.STOCK_COL_PRODUCT.toString();
                case 3:
                    return Messages.STOCK_COL_NOW.toString();
                case 4:
                    return Messages.STOCK_COL_NEW.toString();
                default:
                    return Messages.STOCK_COL_STATE.toString();
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 1 && !lines.get(r).booked;
        }

        @Override
        public Object getValueAt(int r, int c) {
            Line l = lines.get(r);
            switch (c) {
                case 0:
                    return l.code;
                case 1:
                    return l.quantity.stripTrailingZeros().toPlainString();
                case 2:
                    return l.product == null ? "" : l.product.__getCname() + " (" + l.product.__getCnumber() + ")";
                case 3:
                    return l.product == null || l.booked ? "" : stock(l.product).stripTrailingZeros().toPlainString();
                case 4:
                    return l.product == null ? "" : l.booked ? stock(l.product).stripTrailingZeros().toPlainString()
                            : stock(l.product).add(change(l)).stripTrailingZeros().toPlainString();
                default:
                    return l.state;
            }
        }

        @Override
        public void setValueAt(Object v, int r, int c) {
            try {
                BigDecimal q = new BigDecimal(String.valueOf(v).trim().replace(',', '.'));
                if (q.signum() > 0) {
                    lines.get(r).quantity = q;
                }
            } catch (Exception e) {
                //keep the quantity
            }
            fireTableRowsUpdated(r, r);
        }
    }
}
