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
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Product;
import mpv5.globals.Messages;
import mpv5.handler.StockService;
import mpv5.logging.Log;
import mpv5.ui.dialogs.CsvImportDialog;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.utils.csv.CsvImport;
import mpv5.utils.date.DateConverter;

/**
 * Stock management: scan, type, paste or import (CSV wizard) codes; the
 * product is found by number, EAN, reference or name. Unknown products can be
 * created. Modes: stock in, stock out and stocktaking (Inventur: expected and
 * counted stock, exported as CSV or HTML).
 */
public class Stockmanager extends JPanel {

    private static final long serialVersionUID = 3L;
    private static Stockmanager me;

    public static Stockmanager instanceOf() {
        if (me == null) {
            me = new Stockmanager();
        }
        return me;
    }

    private static final class Line {

        String code;
        BigDecimal quantity = BigDecimal.ONE;
        //stocktaking: false until the product was scanned or the count typed
        boolean counted = true;
        Product product;
        //the stock when the product was found (stocktaking: the expected stock)
        BigDecimal expected = BigDecimal.ZERO;
        boolean create;
        //a row of a CSV file: the values of a product to create
        String[] row;
        CsvImport.Field[] mapping;
        String state = "";
        boolean booked;
    }

    private static final Color RED = new Color(190, 40, 40);
    private static final Color GREEN = new Color(30, 130, 60);

    private final List<Line> lines = new ArrayList<Line>();
    private final JTextField scan = new JTextField();
    private final JRadioButton in = new JRadioButton();
    private final JRadioButton out = new JRadioButton();
    private final JRadioButton inventory = new JRadioButton();
    private final JCheckBox autoCreate = new JCheckBox();
    private final JCheckBox ignoreCase = new JCheckBox();
    private final JTextArea modeHint = (JTextArea) Applets.plainHint(" ");
    private final JLabel counter = new JLabel(" ");
    private PrimaryButton book;
    private static final String IGNORE_CASE_PROPERTY = "stock.ignorecase";
    private final JLabel status = new JLabel(" ");
    private final Model model = new Model();
    private final JTable table = new JTable(model);
    private final JButton loadAll = ModernTheme.actionButton(new JButton(), "/images/16/kwikdisk.png");
    private final JButton exportCsv = ModernTheme.actionButton(new JButton(), "/images/16/fileexport.png");
    private final JButton exportHtml = ModernTheme.actionButton(new JButton(), "/images/16/fileexport.png");

    private Stockmanager() {
        super(new BorderLayout(0, ModernTheme.px(8)));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(12), ModernTheme.px(14), ModernTheme.px(12), ModernTheme.px(14)));

        in.setText(Messages.STOCK_IN.toString());
        out.setText(Messages.STOCK_OUT.toString());
        inventory.setText(Messages.STOCK_MODE_INVENTORY.toString());
        ButtonGroup g = new ButtonGroup();
        for (JRadioButton b : new JRadioButton[]{in, out, inventory}) {
            b.setOpaque(false);
            g.add(b);
            b.addActionListener(e -> modeChanged());
        }
        in.setSelected(true);
        ignoreCase.setText(Messages.STOCK_IGNORE_CASE.toString());
        ignoreCase.setToolTipText(Messages.STOCK_IGNORE_CASE_INFO.toString());
        ignoreCase.setFont(ModernTheme.font(1, java.awt.Font.PLAIN));
        ignoreCase.setOpaque(false);
        ignoreCase.setSelected(ignoreCaseSetting());
        ignoreCase.addActionListener(e -> {
            try {
                mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty(IGNORE_CASE_PROPERTY, String.valueOf(ignoreCase.isSelected()));
            } catch (Exception ex) {
                Log.Debug(this, ex.getMessage());
            }
            //look the codes not found up again
            for (Line l : lines) {
                if (!l.booked && l.product == null) {
                    Product p = StockService.find(l.code, ignoreCase.isSelected());
                    if (p != null) {
                        setProduct(l, p);
                    }
                }
            }
            refresh();
            start();
        });
        autoCreate.setText(Messages.STOCK_AUTOCREATE.toString());
        autoCreate.setToolTipText(Messages.STOCK_AUTOCREATE_INFO.toString());
        autoCreate.setFont(ModernTheme.font(1, java.awt.Font.PLAIN));
        autoCreate.setOpaque(false);
        autoCreate.addActionListener(e -> {
            for (Line l : lines) {
                if (l.product == null && !l.booked) {
                    l.create = autoCreate.isSelected();
                }
            }
            model.fireTableDataChanged();
        });

        scan.setColumns(26);
        scan.setFont(ModernTheme.font(5, java.awt.Font.PLAIN));
        ModernTheme.roomy(scan);
        scan.setToolTipText(Messages.STOCK_SCAN_TOOLTIP.toString());
        scan.addActionListener(e -> {
            String t = scan.getText();
            scan.setText("");
            //a pasted list in the field: every code
            if (t != null && t.matches("(?s).*[\\r\\n\\t;].*")) {
                addEntries(StockService.fromText(t, StockService.guessSeparator(t), ignoreCase.isSelected()));
            } else {
                add(t, BigDecimal.ONE, null, null);
            }
            refresh();
        });

        JButton importData = ModernTheme.actionButton(new JButton(Messages.STOCK_IMPORT_DATA.toString()), "/images/16/editpaste.png");
        importData.setToolTipText(Messages.STOCK_WIZ_SOURCE_INFO.toString());
        importData.addActionListener(e -> importData());
        loadAll.setText(Messages.STOCK_LOAD_ALL.toString());
        loadAll.addActionListener(e -> loadAll());
        exportCsv.setText(Messages.STOCK_EXPORT_CSV.toString());
        exportCsv.addActionListener(e -> export(true));
        exportHtml.setText(Messages.STOCK_EXPORT_HTML.toString());
        exportHtml.addActionListener(e -> export(false));
        for (JRadioButton b : new JRadioButton[]{in, out, inventory}) {
            b.setFont(ModernTheme.font(1, java.awt.Font.PLAIN));
        }

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new javax.swing.BoxLayout(top, javax.swing.BoxLayout.PAGE_AXIS));
        JComponent hint = Applets.plainHint(Messages.STOCK_INFO.toString());
        //a first width: the text wraps already in the first layout
        hint.setSize(ModernTheme.px(900), 1);
        //import, scan field and "create unknown products" in one line
        JPanel row = new JPanel(new mpv5.ui.modern.WrapLayout(FlowLayout.LEFT, ModernTheme.px(8), ModernTheme.px(4)));
        row.setOpaque(false);
        row.add(importData);
        JLabel scanLabel = new JLabel(Messages.STOCK_SCAN + ":");
        scanLabel.setFont(ModernTheme.font(1, java.awt.Font.BOLD));
        row.add(scanLabel);
        row.add(scan);
        row.add(ignoreCase);
        row.add(autoCreate);
        JComponent createInfo = Applets.plainHint(Messages.STOCK_AUTOCREATE_SHORT.toString());
        createInfo.setToolTipText(Messages.STOCK_AUTOCREATE_INFO.toString());
        JPanel row2 = new JPanel(new mpv5.ui.modern.WrapLayout(FlowLayout.LEFT, ModernTheme.px(8), ModernTheme.px(4)));
        row2.setOpaque(false);
        JLabel modeLabel = new JLabel(Messages.STOCK_MODE + ":");
        modeLabel.setFont(ModernTheme.font(1, java.awt.Font.BOLD));
        row2.add(modeLabel);
        row2.add(in);
        row2.add(out);
        row2.add(inventory);
        row2.add(javax.swing.Box.createHorizontalStrut(ModernTheme.px(16)));
        row2.add(loadAll);
        row2.add(exportCsv);
        row2.add(exportHtml);
        //what the mode does (stocktaking: the steps)
        modeHint.setForeground(ModernTheme.foreground());
        modeHint.setFont(ModernTheme.font(0, java.awt.Font.PLAIN));
        modeHint.setSize(ModernTheme.px(900), 1);
        JPanel modeBox = new JPanel(new BorderLayout(0, ModernTheme.px(4)));
        modeBox.setBackground(new Color(0xEEF4FF));
        modeBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xC7D7F5)),
                BorderFactory.createEmptyBorder(ModernTheme.px(6), ModernTheme.px(10), ModernTheme.px(6), ModernTheme.px(10))));
        modeBox.add(modeHint, BorderLayout.CENTER);
        counter.setFont(ModernTheme.font(1, java.awt.Font.BOLD));
        modeBox.add(counter, BorderLayout.SOUTH);
        for (JComponent c : new JComponent[]{hint, row, createInfo, row2, modeBox}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(c);
            top.add(javax.swing.Box.createVerticalStrut(ModernTheme.px(6)));
        }

        table.setRowHeight(ModernTheme.px(26));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(false);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                Line l = lines.get(t.convertRowIndexToModel(r));
                if (!sel) {
                    Color fg = l.product == null ? (l.create ? GREEN : RED)
                            : l.booked ? ModernTheme.muted() : ModernTheme.foreground();
                    if (c == 5 && inventory.isSelected() && l.product != null && l.counted) {
                        BigDecimal d = l.quantity.subtract(l.expected);
                        fg = d.signum() == 0 ? fg : RED;
                    }
                    setForeground(fg);
                }
                setHorizontalAlignment(c == 1 || c == 4 || c == 5 ? RIGHT : LEFT);
                return this;
            }
        });
        ModernTheme.style(table);
        int[] widths = {150, 70, 300, 90, 80, 80, 140};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(ModernTheme.px(widths[i]));
        }
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                if (e.getClickCount() == 2 && r >= 0 && table.columnAtPoint(e.getPoint()) != 1
                        && table.columnAtPoint(e.getPoint()) != 3) {
                    open(lines.get(table.convertRowIndexToModel(r)));
                }
            }
        });
        table.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "stockPaste");
        table.getActionMap().put("stockPaste", new javax.swing.AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                importData();
            }
        });
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(ModernTheme.cardBackground());

        book = new PrimaryButton(Messages.STOCK_BOOK.toString(), "/images/16/filesave.png");
        book.addActionListener(e -> book());
        JButton remove = ModernTheme.actionButton(new JButton(Messages.ACTION_REMOVE.toString()), "/images/16/remove.png");
        remove.addActionListener(e -> {
            if (table.isEditing()) {
                table.getCellEditor().cancelCellEditing();
            }
            int[] rows = table.getSelectedRows();
            for (int i = rows.length - 1; i >= 0; i--) {
                lines.remove(table.convertRowIndexToModel(rows[i]));
            }
            refresh();
        });
        JButton clear = Applets.danger(ModernTheme.actionButton(new JButton(Messages.STOCK_CLEAR.toString()), null));
        clear.addActionListener(e -> {
            if (table.isEditing()) {
                table.getCellEditor().cancelCellEditing();
            }
            lines.clear();
            refresh();
            status.setText(" ");
            start();
        });
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(status, BorderLayout.CENTER);
        bottom.add(Applets.actionBar(new JComponent[]{clear}, new JComponent[]{remove, book}), BorderLayout.SOUTH);

        model.addTableModelListener(e -> updateCounter());
        add(top, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        modeChanged();
    }

    public void start() {
        scan.requestFocusInWindow();
    }

    private void modeChanged() {
        boolean inv = inventory.isSelected();
        loadAll.setVisible(inv);
        exportCsv.setVisible(inv);
        exportHtml.setVisible(inv);
        modeHint.setText((inv ? Messages.STOCK_HINT_INVENTORY : out.isSelected() ? Messages.STOCK_HINT_OUT : Messages.STOCK_HINT_IN).toString());
        book.setText((inv ? Messages.STOCK_INVENTORY_BOOK : Messages.STOCK_BOOK).toString());
        model.fireTableStructureChanged();
        int[] widths = {150, 70, 300, 90, 80, 80, 140};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(ModernTheme.px(widths[i]));
        }
        start();
    }

    private static boolean ignoreCaseSetting() {
        try {
            String v = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(IGNORE_CASE_PROPERTY);
            return v == null || v.trim().isEmpty() || !"false".equalsIgnoreCase(v.trim());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Stocktaking: counted / not counted / differences; else lines and
     * quantity
     */
    private void updateCounter() {
        int counted = 0;
        int open = 0;
        int diff = 0;
        int unknown = 0;
        for (Line l : lines) {
            if (l.product == null) {
                unknown++;
            } else if (!l.counted) {
                open++;
            } else {
                counted++;
                diff += l.quantity.compareTo(l.expected) != 0 ? 1 : 0;
            }
        }
        counter.setText(inventory.isSelected()
                ? String.format(Messages.STOCK_COUNTER_INVENTORY.toString(), counted, open, diff, unknown)
                : String.format(Messages.STOCK_COUNTER.toString(), lines.size(), unknown));
    }

    private void refresh() {
        model.fireTableDataChanged();
        if (!lines.isEmpty()) {
            table.scrollRectToVisible(table.getCellRect(lines.size() - 1, 0, true));
        }
    }

    // ------------------------------------------------------------ adding
    /**
     * Adds a code: the same product (or the same unknown code) again tops up
     * its quantity
     */
    private Line add(String text, BigDecimal quantity, String[] row, CsvImport.Field[] mapping) {
        String code = text == null ? "" : text.trim();
        if (code.isEmpty()) {
            return null;
        }
        Product p = StockService.find(code, ignoreCase.isSelected());
        for (Line l : lines) {
            if (l.booked) {
                continue;
            }
            boolean same = p != null ? l.product != null && l.product.__getIDS().equals(p.__getIDS())
                    : l.product == null && StockService.sameCode(l.code, code, ignoreCase.isSelected());
            if (same) {
                l.quantity = l.counted ? l.quantity.add(quantity) : quantity;
                l.counted = true;
                if (l.row == null && row != null) {
                    l.row = row;
                    l.mapping = mapping;
                }
                return l;
            }
        }
        Line l = new Line();
        l.code = code;
        l.quantity = quantity;
        l.row = row;
        l.mapping = mapping;
        setProduct(l, p);
        lines.add(l);
        return l;
    }

    private void setProduct(Line l, Product p) {
        l.product = p;
        l.expected = stock(p);
        l.create = p == null && autoCreate.isSelected();
        l.state = p == null ? Messages.STOCK_NOT_FOUND.toString() : "";
    }

    /**
     * The import wizard: a pasted list or a CSV file
     */
    private void importData() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        mpv5.ui.dialogs.StockImportWizard.open(autoCreate, ignoreCase, this::addEntries);
        start();
    }

    /**
     * The entries of the wizard: found products are topped up, the others are
     * created when booking if marked
     */
    private void addEntries(List<StockService.Entry> entries) {
        for (StockService.Entry e : entries) {
            Line l = add(e.code, e.quantity, e.row, e.mapping);
            if (l != null && l.product == null) {
                l.create = e.create;
            }
        }
        refresh();
        status.setText(String.format(Messages.STOCK_ADDED.toString(), entries.size()));
    }

    /**
     * Stocktaking: every product (not the services) with its expected stock,
     * not counted yet
     */
    private void loadAll() {
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getProduct())) {
                Product p = (Product) o;
                if (p.__getInttype() == Product.TYPE_SERVICE) {
                    continue;
                }
                boolean there = false;
                for (Line l : lines) {
                    there |= !l.booked && l.product != null && l.product.__getIDS().equals(p.__getIDS());
                }
                if (!there) {
                    Line l = new Line();
                    l.code = p.__getCnumber();
                    l.quantity = BigDecimal.ZERO;
                    l.counted = false;
                    setProduct(l, p);
                    lines.add(l);
                }
            }
        } catch (Exception e) {
            Log.Debug(this, e.getMessage());
        }
        refresh();
        start();
    }

    // ------------------------------------------------------------ products
    private static BigDecimal stock(Product p) {
        return p == null || p.__getStockvalue() == null ? BigDecimal.ZERO : p.__getStockvalue();
    }

    /**
     * Creates the product of an unknown line (with the values of its CSV
     * row), stock 0
     *
     * @return true if created
     */
    private boolean create(Line l) {
        try {
            Product p = StockService.newProduct(l.row, l.mapping, l.code);
            if (p.saveImport() && p.isExisting()) {
                setProduct(l, p);
                l.state = Messages.STOCK_CREATED.toString();
                return true;
            }
        } catch (Exception e) {
            Log.Debug(this, e);
        }
        l.state = Messages.STOCK_CREATE_FAILED.toString();
        return false;
    }

    /**
     * Double click: opens the product; an unknown one is created first
     */
    private void open(Line l) {
        if (l.product == null) {
            if (l.booked || !create(l)) {
                model.fireTableDataChanged();
                return;
            }
            model.fireTableDataChanged();
        }
        try {
            mpv5.YabsViewProxy.instance().addTab(l.product);
        } catch (Exception e) {
            Log.Debug(this, e);
        }
    }

    private BigDecimal change(Line l) {
        return out.isSelected() ? l.quantity.negate() : l.quantity;
    }

    private void book() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        boolean inv = inventory.isSelected();
        if (inv) {
            int counted = 0;
            for (Line l : lines) {
                counted += !l.booked && l.counted && (l.product != null || l.create) ? 1 : 0;
            }
            if (counted == 0 || !Popup.Y_N_dialog(String.format(Messages.STOCK_INVENTORY_CONFIRM.toString(), counted))) {
                return;
            }
        }
        int done = 0;
        int created = 0;
        int missing = 0;
        for (Line l : lines) {
            if (l.booked || (inv && !l.counted)) {
                continue;
            }
            if (l.product == null) {
                if (!l.create) {
                    missing++;
                    continue;
                }
                if (!create(l)) {
                    missing++;
                    continue;
                }
                created++;
            }
            try {
                //fresh from the database: the stock may have changed since the scan
                Product p = (Product) DatabaseObject.getObject(Context.getProduct(), l.product.__getIDS());
                p.setStockvalue(inv ? l.quantity : stock(p).add(change(l)));
                if (p.save(true)) {
                    if (!inv) {
                        l.expected = stock(l.product);
                    }
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
        String result = String.format(Messages.STOCK_RESULT_FULL.toString(), done, created, missing);
        status.setText(result);
        if (missing > 0) {
            Popup.notice(result);
        }
        start();
    }

    // ------------------------------------------------------------ stocktaking export
    private List<StockService.Count> counts() {
        List<StockService.Count> c = new ArrayList<StockService.Count>();
        for (Line l : lines) {
            if (l.product != null) {
                c.add(new StockService.Count(l.product.__getCnumber(), l.product.__getCname(), l.product.__getEan(),
                        l.product.__getMeasure(), l.expected, l.counted ? l.quantity : null));
            } else {
                c.add(new StockService.Count(l.code, Messages.STOCK_NOT_FOUND.toString(), "", "", BigDecimal.ZERO,
                        l.counted ? l.quantity : null));
            }
        }
        return c;
    }

    private void export(boolean csv) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        String ext = csv ? "csv" : "html"; //NOI18N
        DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY, "inventur-" + DateConverter.getTodayFileDate() + "." + ext); //NOI18N
        if (!d.saveFile()) {
            return;
        }
        File f = d.getFile();
        String text = csv ? "﻿" + StockService.inventoryCsv(counts(), ';')
                : StockService.inventoryHtml(counts(), String.format(Messages.STOCK_INVENTORY_TITLE.toString(),
                        DateConverter.getDefDateString(new java.util.Date())));
        try {
            java.nio.file.Files.write(f.toPath(), text.getBytes(StandardCharsets.UTF_8));
            status.setText(f.getAbsolutePath());
        } catch (Exception e) {
            Log.Debug(this, e);
            Popup.error(e);
        }
    }

    // ------------------------------------------------------------ table
    private final class Model extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return lines.size();
        }

        @Override
        public int getColumnCount() {
            return 7;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 3 ? Boolean.class : Object.class;
        }

        @Override
        public String getColumnName(int c) {
            boolean inv = inventory.isSelected();
            switch (c) {
                case 0:
                    return Messages.STOCK_COL_CODE.toString();
                case 1:
                    return (inv ? Messages.STOCK_COL_COUNTED : Messages.STOCK_COL_QUANTITY).toString();
                case 2:
                    return Messages.STOCK_COL_PRODUCT.toString();
                case 3:
                    return Messages.STOCK_COL_CREATE.toString();
                case 4:
                    return (inv ? Messages.STOCK_COL_EXPECTED : Messages.STOCK_COL_NOW).toString();
                case 5:
                    return (inv ? Messages.STOCK_COL_DIFF : Messages.STOCK_COL_NEW).toString();
                default:
                    return Messages.STOCK_COL_STATE.toString();
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            Line l = lines.get(r);
            return !l.booked && (c == 1 || (c == 3 && l.product == null));
        }

        @Override
        public Object getValueAt(int r, int c) {
            Line l = lines.get(r);
            boolean inv = inventory.isSelected();
            switch (c) {
                case 0:
                    return l.code;
                case 1:
                    return l.counted ? plain(l.quantity) : "";
                case 2:
                    return l.product == null ? "" : l.product.__getCname() + " (" + l.product.__getCnumber() + ")";
                case 3:
                    return l.product == null && l.create;
                case 4:
                    if (inv) {
                        return l.product == null ? "" : plain(l.expected);
                    }
                    return l.product == null || l.booked ? "" : plain(stock(l.product));
                case 5:
                    if (inv) {
                        return l.product == null || !l.counted ? "" : signed(l.quantity.subtract(l.expected));
                    }
                    if (l.product == null) {
                        return l.create ? plain(change(l)) : "";
                    }
                    return l.booked ? plain(stock(l.product)) : plain(stock(l.product).add(change(l)));
                default:
                    return l.state;
            }
        }

        @Override
        public void setValueAt(Object v, int r, int c) {
            Line l = lines.get(r);
            if (c == 3) {
                l.create = Boolean.TRUE.equals(v);
            } else {
                try {
                    String s = String.valueOf(v).trim();
                    if (s.isEmpty() && inventory.isSelected()) {
                        l.counted = false;
                        l.quantity = BigDecimal.ZERO;
                    } else {
                        BigDecimal q = CsvImport.number(s);
                        //stocktaking: 0 is a count; stock in/out: more than 0
                        if (q.signum() > 0 || (q.signum() == 0 && inventory.isSelected())) {
                            l.quantity = q;
                            l.counted = true;
                        }
                    }
                } catch (Exception e) {
                    //keep the quantity
                }
            }
            fireTableRowsUpdated(r, r);
        }
    }

    private static String plain(BigDecimal v) {
        return v == null ? "" : v.stripTrailingZeros().toPlainString();
    }

    private static String signed(BigDecimal v) {
        return v.signum() > 0 ? "+" + plain(v) : plain(v);
    }
}
