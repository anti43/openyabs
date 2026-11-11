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
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import mpv5.data.PropertyStore;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.models.MPComboBoxModelItem;

/**
 * Translating YaBS inside the Control Panel: all keys YaBS needs (sorted by
 * key), the English text and the translation; filter, import / export of
 * .properties files, and "Save" which keeps exactly one own language per user
 * and uses it right away.
 */
public class ControlPanel_Translate extends JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    /**
     * User property with the id of the user's own language
     */
    public static final String OWN_LANGUAGE = "org.openyabs.languageproperty.own";
    public final String UNAME = "translate";

    private static final class Row {

        final String key;
        final String english;
        String text;

        Row(String key, String english) {
            this.key = key;
            this.english = english;
        }
    }

    private final List<Row> rows = new ArrayList<Row>();
    private final Model model = new Model();
    private final JTable table = new JTable(model);
    private final TableRowSorter<Model> sorter = new TableRowSorter<Model>(model);
    private final JComboBox<MPComboBoxModelItem> base = new JComboBox<MPComboBoxModelItem>();
    private final JTextField filter = new JTextField(22);
    private final JLabel count = new JLabel(" ");
    private final JPanel actions;

    public ControlPanel_Translate() {
        super(new BorderLayout(0, ModernTheme.px(8)));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(12), ModernTheme.px(14), ModernTheme.px(4), ModernTheme.px(14)));

        for (Map.Entry<String, String> e : LanguageManager.requiredKeys().entrySet()) {
            rows.add(new Row(e.getKey(), e.getValue()));
        }

        //"based on": the languages; preselected: the own language, else the current one
        javax.swing.ComboBoxModel<?> langs = LanguageManager.getLanguagesAsComboBoxModel();
        for (int i = 0; i < langs.getSize(); i++) {
            base.addItem((MPComboBoxModelItem) langs.getElementAt(i));
        }
        String own = User.getCurrentUser().getProperties().getProperty(OWN_LANGUAGE);
        String start = own != null && hasLanguage(own) ? own : User.getCurrentUser().__getLanguage();
        for (int i = 0; i < base.getItemCount(); i++) {
            if (base.getItemAt(i).getId().equals(start)) {
                base.setSelectedIndex(i);
            }
        }
        base.addActionListener(e -> fill());

        table.setRowSorter(sorter);
        List<javax.swing.RowSorter.SortKey> keys = new ArrayList<javax.swing.RowSorter.SortKey>();
        keys.add(new javax.swing.RowSorter.SortKey(0, javax.swing.SortOrder.ASCENDING));
        sorter.setSortKeys(keys);
        table.setRowHeight(ModernTheme.px(24));
        table.getColumnModel().getColumn(0).setPreferredWidth(ModernTheme.px(260));
        table.getColumnModel().getColumn(1).setPreferredWidth(ModernTheme.px(380));
        table.getColumnModel().getColumn(2).setPreferredWidth(ModernTheme.px(380));
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        ModernTheme.style(table);
        filter.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter();
            }
        });

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.PAGE_AXIS));
        JComponent hint = Applets.hint(Messages.TRANSLATE_INFO.toString());
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), 0));
        row.setOpaque(false);
        row.add(new JLabel(Messages.TRANSLATE_BASE + ":"));
        row.add(base);
        row.add(Box.createHorizontalStrut(ModernTheme.px(16)));
        row.add(new JLabel(Messages.TRANSLATE_FILTER + ":"));
        row.add(filter);
        row.add(count);
        for (JComponent c : new JComponent[]{hint, row}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(c);
            top.add(Box.createVerticalStrut(ModernTheme.px(6)));
        }
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(ModernTheme.cardBackground());
        add(top, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);

        JButton importFile = new JButton(Messages.TRANSLATE_IMPORT.toString());
        importFile.addActionListener(e -> importFile());
        JButton exportFile = new JButton(Messages.TRANSLATE_EXPORT.toString());
        exportFile.addActionListener(e -> exportFile());
        JButton delete = Applets.danger(new JButton(Messages.TRANSLATE_DELETE.toString()));
        delete.addActionListener(e -> deleteLanguage());
        PrimaryButton save = new PrimaryButton(Messages.TRANSLATE_SAVE.toString(), "/images/16/filesave.png");
        save.addActionListener(e -> save());
        actions = Applets.actionBar(new JComponent[]{importFile, exportFile, delete}, new JComponent[]{save});

        fill();
    }

    private boolean hasLanguage(String id) {
        for (int i = 0; i < base.getItemCount(); i++) {
            if (base.getItemAt(i).getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private String baseId() {
        MPComboBoxModelItem i = (MPComboBoxModelItem) base.getSelectedItem();
        return i == null ? "buildin_en" : i.getId();
    }

    /**
     * The translation column from the chosen language
     */
    private void fill() {
        Map<String, String> texts = LanguageManager.ownKeys(baseId());
        for (Row r : rows) {
            r.text = texts.get(r.key);
        }
        model.fireTableDataChanged();
        showCount();
    }

    private void applyFilter() {
        String f = filter.getText().trim();
        if (f.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(f)));
        }
        showCount();
    }

    private void showCount() {
        int missing = 0;
        for (Row r : rows) {
            if (r.text == null || r.text.trim().isEmpty()) {
                missing++;
            }
        }
        count.setText(String.format(Messages.TRANSLATE_COUNT.toString(), table.getRowCount(), rows.size(), missing));
    }

    private void stopEditing() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }

    /**
     * @return all keys; without translation: the English text (so no key is
     * ever missing)
     */
    private Map<String, String> texts() {
        Map<String, String> m = new LinkedHashMap<String, String>();
        for (Row r : rows) {
            m.put(r.key, r.text == null || r.text.trim().isEmpty() ? r.english : r.text);
        }
        return m;
    }

    private void exportFile() {
        stopEditing();
        DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY, "yabs-language.properties");
        if (d.saveFile()) {
            try {
                File f = d.getFile();
                if (!f.getName().contains(".")) {
                    f = new File(f.getPath() + ".properties");
                }
                LanguageManager.writeLanguageFile(texts(), f);
                mpv5.YabsViewProxy.instance().addMessage(Messages.TRANSLATE_EXPORTED + " " + f);
            } catch (Exception e) {
                Log.Debug(this, e);
                Popup.error(e);
            }
        }
    }

    private void importFile() {
        stopEditing();
        DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY);
        if (d.chooseFile()) {
            try {
                Map<String, String> in = LanguageManager.readLanguageFile(d.getFile());
                int used = 0;
                for (Row r : rows) {
                    String v = in.remove(r.key);
                    if (v != null) {
                        r.text = v;
                        used++;
                    }
                }
                model.fireTableDataChanged();
                showCount();
                Popup.notice(String.format(Messages.TRANSLATE_IMPORTED.toString(), used, in.size()));
            } catch (Exception e) {
                Log.Debug(this, e);
                Popup.error(e);
            }
        }
    }

    /**
     * One own language per user: a new one replaces the previous, and the user
     * uses it right away
     */
    private void save() {
        stopEditing();
        final Map<String, String> texts = texts();
        final User u = User.getCurrentUser();
        final String previous = u.getProperties().getProperty(OWN_LANGUAGE);
        MPComboBoxModelItem b = (MPComboBoxModelItem) base.getSelectedItem();
        final String name = String.format(Messages.TRANSLATE_OWN_NAME.toString(),
                b == null || b.getId().equals(previous) ? u.__getCname() : b.getValue() + ", " + u.__getCname(),
                mpv5.utils.date.DateConverter.getTodayFileDate());
        mpv5.YabsViewProxy.instance().setWaiting(true);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                File f = File.createTempFile("yabs-own-language", ".properties");
                LanguageManager.writeLanguageFile(texts, f);
                return LanguageManager.importLanguageQuietly(name, f);
            }

            @Override
            protected void done() {
                mpv5.YabsViewProxy.instance().setWaiting(false);
                try {
                    String id = get();
                    if (id == null) {
                        Popup.notice(Messages.ERROR_OCCURED);
                        return;
                    }
                    u.setLanguage(id);
                    u.save(true);
                    u.getProperties().changeProperty(OWN_LANGUAGE, id);
                    //chosen by the user: no automatic switch to German any more
                    u.getProperties().changeProperty("org.openyabs.languageproperty", "autoimportde", true);
                    u.saveProperties();
                    if (previous != null && !previous.equals(id)) {
                        try {
                            LanguageManager.removeLanguage(previous);
                        } catch (Exception e) {
                            Log.Debug(this, e);
                        }
                    }
                    LanguageManager.getBundle(id);
                    base.removeAllItems();
                    javax.swing.ComboBoxModel<?> langs = LanguageManager.getLanguagesAsComboBoxModel();
                    for (int i = 0; i < langs.getSize(); i++) {
                        MPComboBoxModelItem it = (MPComboBoxModelItem) langs.getElementAt(i);
                        base.addItem(it);
                        if (it.getId().equals(id)) {
                            base.setSelectedItem(it);
                        }
                    }
                    Popup.notice(Messages.TRANSLATE_SAVED);
                } catch (Exception e) {
                    Log.Debug(this, e);
                    Popup.error(e);
                }
            }
        }.execute();
    }

    private void deleteLanguage() {
        String id = baseId();
        if ("buildin_en".equals(id)) {
            Popup.notice(Messages.NOT_POSSIBLE);
            return;
        }
        String own = User.getCurrentUser().getProperties().getProperty(OWN_LANGUAGE);
        if (!id.equals(own) && !MPSecurityManager.checkAdminAccess()) {
            Popup.notice(Messages.ADMIN_ACCESS);
            return;
        }
        if (Popup.Y_N_dialog(Messages.REALLY_WIPE + ": " + base.getSelectedItem())) {
            try {
                LanguageManager.removeLanguage(id);
                if (id.equals(User.getCurrentUser().__getLanguage())) {
                    User.getCurrentUser().setLanguage("buildin_en");
                    User.getCurrentUser().save(true);
                }
                if (id.equals(own)) {
                    User.getCurrentUser().getProperties().changeProperty(OWN_LANGUAGE, "");
                    User.getCurrentUser().saveProperties();
                }
                base.removeItem(base.getSelectedItem());
            } catch (Exception e) {
                Log.Debug(this, e);
            }
        }
    }

    private final class Model extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int c) {
            return c == 0 ? Messages.TRANSLATE_COL_KEY.toString() : c == 1 ? "English" : Messages.TRANSLATE_COL_TEXT.toString();
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 2;
        }

        @Override
        public Object getValueAt(int r, int c) {
            Row row = rows.get(r);
            return c == 0 ? row.key : c == 1 ? row.english : row.text;
        }

        @Override
        public void setValueAt(Object v, int r, int c) {
            rows.get(r).text = v == null ? null : String.valueOf(v);
            fireTableCellUpdated(r, c);
            showCount();
        }
    }

    @Override
    public void setValues(PropertyStore values) {
    }

    @Override
    public String getUname() {
        return UNAME;
    }

    @Override
    public void reset() {
        fill();
    }

    @Override
    public Component getAndRemoveActionPanel() {
        return actions;
    }
}
