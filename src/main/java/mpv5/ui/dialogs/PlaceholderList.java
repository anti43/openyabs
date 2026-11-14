/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Messages;
import mpv5.handler.SearchService;
import mpv5.ui.modern.ModernTheme;

/**
 * All placeholders of a document for templates, e.g. &lt;item.paymentiban&gt;,
 * with their value in that document: searchable, a double click copies the
 * placeholder
 */
public final class PlaceholderList {

    private PlaceholderList() {
    }

    /**
     * @param fields the form fields of a document (name → value)
     * @return [placeholder, value], sorted by name; lists (the position
     * table) left out
     */
    public static List<String[]> placeholders(Map<String, Object> fields) {
        Map<String, String> sorted = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String, Object> e : fields.entrySet()) {
            Object v = e.getValue();
            if (e.getKey() == null || v instanceof Collection || v instanceof Map || v instanceof Object[]) {
                continue;
            }
            String s = v == null || "null".equals(String.valueOf(v)) ? "" : String.valueOf(v).trim();
            sorted.put("<" + e.getKey().toLowerCase(java.util.Locale.ROOT) + ">", s);
        }
        List<String[]> l = new ArrayList<String[]>();
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            l.add(new String[]{e.getKey(), e.getValue()});
        }
        return l;
    }

    /**
     * @param row [placeholder, value]
     * @param needle every word must occur in the placeholder or the value
     * @param onlyFilled only rows with a value
     * @return true if the row is shown
     */
    public static boolean shown(String[] row, String needle, boolean onlyFilled) {
        if (onlyFilled && row[1].isEmpty()) {
            return false;
        }
        return SearchService.matches(row[0] + " " + row[1], needle == null ? "" : needle);
    }

    /**
     * Shows the placeholders of the document
     *
     * @param parent
     * @param document
     */
    public static void show(Component parent, DatabaseObject document) {
        final List<String[]> all = placeholders(document.getFormFields());
        Window w = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog d = new JDialog(w, Messages.PLACEHOLDERS_TITLE.toString(), java.awt.Dialog.ModalityType.MODELESS);

        final JTextField search = new JTextField();
        search.putClientProperty("JTextField.placeholderText", Messages.PLACEHOLDERS_SEARCH.toString());
        search.putClientProperty("JTextField.showClearButton", true);
        ModernTheme.roomy(search);
        final JCheckBox onlyFilled = new JCheckBox(Messages.PLACEHOLDERS_ONLY_FILLED.toString());
        onlyFilled.setOpaque(false);

        final DefaultTableModel model = new DefaultTableModel(new Object[]{
            Messages.PLACEHOLDERS_NAME.toString(), Messages.PLACEHOLDERS_VALUE.toString()}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        final JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, table.getFont().getSize()));
        ModernTheme.style(table);
        final JLabel count = new JLabel();
        count.setForeground(ModernTheme.muted());

        final Runnable filter = () -> {
            model.setRowCount(0);
            for (String[] r : all) {
                if (shown(r, search.getText(), onlyFilled.isSelected())) {
                    model.addRow(new Object[]{r[0], r[1]});
                }
            }
            count.setText(String.format(Messages.PLACEHOLDERS_COUNT.toString(), model.getRowCount(), all.size()));
            if (model.getRowCount() > 0) {
                table.setRowSelectionInterval(0, 0);
            }
        };
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter.run();
            }
        });
        onlyFilled.addActionListener(e -> filter.run());

        final JLabel copied = new JLabel(" ");
        copied.setForeground(ModernTheme.accent());
        final Runnable copy = () -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                String p = String.valueOf(model.getValueAt(table.convertRowIndexToModel(r), 0));
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(p), null);
                copied.setText(String.format(Messages.PLACEHOLDERS_COPIED.toString(), p));
            }
        };
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    copy.run();
                }
            }
        });
        //Enter in the search field or the table copies the selected placeholder, arrows move in the table
        search.addActionListener(e -> copy.run());
        search.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int r = table.getSelectedRow();
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN && r < table.getRowCount() - 1) {
                    table.setRowSelectionInterval(r + 1, r + 1);
                    table.scrollRectToVisible(table.getCellRect(r + 1, 0, true));
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP && r > 0) {
                    table.setRowSelectionInterval(r - 1, r - 1);
                    table.scrollRectToVisible(table.getCellRect(r - 1, 0, true));
                }
            }
        });

        JLabel info = new JLabel("<html>" + String.format(Messages.PLACEHOLDERS_INFO.toString(),
                escape(document.isExisting() ? document.toString() : Messages.TEMPLATES_PREVIEW_SAMPLE.toString())) + "</html>");
        info.setFont(ModernTheme.font(-1, Font.PLAIN));
        info.setForeground(ModernTheme.muted());

        JPanel top = new JPanel(new BorderLayout(ModernTheme.px(8), ModernTheme.px(6)));
        top.setOpaque(false);
        top.add(info, BorderLayout.NORTH);
        top.add(search, BorderLayout.CENTER);
        top.add(onlyFilled, BorderLayout.EAST);

        JButton copyButton = new JButton(Messages.PLACEHOLDERS_COPY.toString());
        copyButton.addActionListener(e -> copy.run());
        JButton close = new JButton(Messages.PLACEHOLDERS_CLOSE.toString());
        close.addActionListener(e -> d.dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0));
        buttons.setOpaque(false);
        buttons.add(copyButton);
        buttons.add(close);
        JPanel status = new JPanel(new BorderLayout());
        status.setOpaque(false);
        status.add(count, BorderLayout.NORTH);
        status.add(copied, BorderLayout.SOUTH);
        JPanel bottom = new JPanel(new BorderLayout(ModernTheme.px(8), 0));
        bottom.setOpaque(false);
        bottom.add(status, BorderLayout.CENTER);
        bottom.add(buttons, BorderLayout.EAST);

        JScrollPane sp = new JScrollPane(table);
        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        p.setBackground(ModernTheme.background());
        p.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(12), ModernTheme.px(12), ModernTheme.px(12), ModernTheme.px(12)));
        p.add(top, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);
        d.setContentPane(p);
        d.getRootPane().registerKeyboardAction(e -> d.dispose(),
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        filter.run();
        table.getColumnModel().getColumn(0).setPreferredWidth(ModernTheme.px(320));
        table.getColumnModel().getColumn(1).setPreferredWidth(ModernTheme.px(380));
        d.setSize(new Dimension(ModernTheme.px(760), ModernTheme.px(620)));
        d.setLocationRelativeTo(w);
        d.setVisible(true);
        search.requestFocusInWindow();
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
