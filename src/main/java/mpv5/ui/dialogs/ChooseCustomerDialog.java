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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.objects.Contact;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;

/**
 * Asks for the customer of a document which has none yet: choose an existing
 * contact (searchable) or create a new one with a few fields. Used when a
 * document is saved without customer.
 */
public final class ChooseCustomerDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final int LIMIT = 200;

    /**
     * The fields of the new contact, as known from the document
     */
    public static final class Prefill {

        public String search = "";
        public String cname = "";
        public String prename = "";
        public String company = "";
        public String street = "";
        public String zip = "";
        public String city = "";
        public String mail = "";
    }

    /**
     * A contact in the list
     */
    static final class Row {

        final int id;
        final String title;
        final String detail;

        Row(int id, String title, String detail) {
            this.id = id;
            this.title = title;
            this.detail = detail;
        }

        @Override
        public String toString() {
            return detail.isEmpty() ? title : title + "  ·  " + detail;
        }
    }

    private Contact result;
    private final JToggleButton existingButton = new JToggleButton(Messages.CHOOSE_CUSTOMER_EXISTING.toString());
    private final JToggleButton newButton = new JToggleButton(Messages.CHOOSE_CUSTOMER_NEW.toString());
    private final CardLayout cards = new CardLayout();
    private final JPanel center = new JPanel(cards);
    private final JTextField search = new JTextField(24);
    private final DefaultListModel<Row> model = new DefaultListModel<Row>();
    private final JList<Row> list = new JList<Row>(model);
    private final JLabel listStatus = new JLabel(" ");
    private final JTextField cname = new JTextField(16);
    private final JTextField prename = new JTextField(16);
    private final JTextField company = new JTextField(16);
    private final JTextField street = new JTextField(16);
    private final JTextField zip = new JTextField(6);
    private final JTextField city = new JTextField(16);
    private final JTextField mail = new JTextField(16);
    private final JLabel error = new JLabel(" ");
    private final Timer delay;

    /**
     * Shows the dialog (modal)
     *
     * @param parent
     * @param prefill what is known (typed name, address of the document)
     * @return the chosen or new (saved) contact, null if cancelled
     */
    public static Contact choose(Component parent, Prefill prefill) {
        Window w = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        ChooseCustomerDialog d = new ChooseCustomerDialog(w, prefill == null ? new Prefill() : prefill);
        d.setLocationRelativeTo(w);
        d.setVisible(true);
        return d.result;
    }

    private ChooseCustomerDialog(Window owner, Prefill p) {
        super(owner, Messages.CHOOSE_CUSTOMER_TITLE.toString(), ModalityType.APPLICATION_MODAL);
        setName("choosecustomer");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(0, ModernTheme.px(10)));
        root.setBackground(ModernTheme.cardBackground());
        root.setBorder(ModernTheme.padding(14, 16));

        JLabel info = new JLabel("<html><div style='width:" + ModernTheme.px(420) + "px'>"
                + escape(Messages.CHOOSE_CUSTOMER_INFO.toString()) + "</div></html>");
        info.setFont(ModernTheme.font(1, Font.PLAIN));
        JPanel switcher = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        switcher.setOpaque(false);
        ButtonGroup g = new ButtonGroup();
        g.add(existingButton);
        g.add(newButton);
        for (JToggleButton b : new JToggleButton[]{existingButton, newButton}) {
            b.setFont(ModernTheme.font(1, Font.PLAIN));
            b.setFocusable(false);
            switcher.add(b);
        }
        existingButton.addActionListener(e -> showPage(true));
        newButton.addActionListener(e -> showPage(false));
        JPanel top = new JPanel(new BorderLayout(0, ModernTheme.px(10)));
        top.setOpaque(false);
        top.add(info, BorderLayout.NORTH);
        top.add(switcher, BorderLayout.SOUTH);
        root.add(top, BorderLayout.NORTH);

        center.setOpaque(false);
        center.add(createExisting(), "existing");
        center.add(createNew(), "new");
        root.add(center, BorderLayout.CENTER);

        error.setForeground(new Color(190, 40, 40));
        PrimaryButton ok = new PrimaryButton(Messages.CHOOSE_CUSTOMER_OK.toString(), null);
        ok.addActionListener(e -> confirm());
        JButton cancel = new JButton(Messages.CHOOSE_CUSTOMER_CANCEL.toString());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(ok);
        JPanel bottom = new JPanel(new BorderLayout(ModernTheme.px(8), 0));
        bottom.setOpaque(false);
        bottom.add(error, BorderLayout.CENTER);
        bottom.add(buttons, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);
        getRootPane().setDefaultButton(ok);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        delay = new Timer(200, e -> load());
        delay.setRepeats(false);
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                delay.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                delay.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                delay.restart();
            }
        });

        //what is known about the customer
        String typed = trim(p.search);
        search.setText(typed);
        cname.setText(trim(p.cname).isEmpty() ? typed : trim(p.cname));
        prename.setText(trim(p.prename));
        company.setText(trim(p.company));
        street.setText(trim(p.street));
        zip.setText(trim(p.zip));
        city.setText(trim(p.city));
        mail.setText(trim(p.mail));
        load();
        //a typed name nobody has: most likely a new customer
        boolean anyContact = !model.isEmpty() || (!typed.isEmpty() && hasContacts());
        showPage(model.isEmpty() && (!typed.isEmpty() || !anyContact) ? false : true);
        pack();
        setMinimumSize(getSize());
    }

    private JComponent createExisting() {
        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(6)));
        p.setOpaque(false);
        search.setFont(ModernTheme.font(2, Font.PLAIN));
        search.putClientProperty("JTextField.placeholderText", Messages.CHOOSE_CUSTOMER_SEARCH.toString());
        JPanel s = new JPanel(new BorderLayout(0, ModernTheme.px(2)));
        s.setOpaque(false);
        s.add(ModernTheme.fieldLabel(Messages.CHOOSE_CUSTOMER_SEARCH.toString()), BorderLayout.NORTH);
        s.add(search, BorderLayout.CENTER);
        p.add(s, BorderLayout.NORTH);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(ModernTheme.font(1, Font.PLAIN));
        list.setVisibleRowCount(9);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && list.getSelectedValue() != null) {
                    confirm();
                }
            }
        });
        //up / down in the search field move in the list
        search.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "yabs.down");
        search.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "yabs.up");
        search.getActionMap().put("yabs.down", action(() -> move(1)));
        search.getActionMap().put("yabs.up", action(() -> move(-1)));
        JScrollPane sp = new JScrollPane(list);
        sp.setPreferredSize(new java.awt.Dimension(ModernTheme.px(440), ModernTheme.px(220)));
        p.add(sp, BorderLayout.CENTER);
        listStatus.setFont(ModernTheme.font(-1, Font.PLAIN));
        listStatus.setForeground(ModernTheme.muted());
        p.add(listStatus, BorderLayout.SOUTH);
        return p;
    }

    private JComponent createNew() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        field(p, 0, 0, 1, Messages.CHOOSE_CUSTOMER_PRENAME.toString(), prename);
        field(p, 1, 0, 1, Messages.CHOOSE_CUSTOMER_NAME.toString(), cname);
        field(p, 0, 1, 2, Messages.CHOOSE_CUSTOMER_COMPANY.toString(), company);
        field(p, 0, 2, 2, Messages.CHOOSE_CUSTOMER_STREET.toString(), street);
        field(p, 0, 3, 1, Messages.CHOOSE_CUSTOMER_ZIP.toString(), zip);
        field(p, 1, 3, 1, Messages.CHOOSE_CUSTOMER_CITY.toString(), city);
        field(p, 0, 4, 2, Messages.CHOOSE_CUSTOMER_MAIL.toString(), mail);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 5;
        c.weighty = 1;
        p.add(new JLabel(), c);
        return p;
    }

    private static void field(JPanel p, int x, int y, int width, String label, JTextField f) {
        JPanel cell = new JPanel(new BorderLayout(0, ModernTheme.px(2)));
        cell.setOpaque(false);
        cell.add(ModernTheme.fieldLabel(label), BorderLayout.NORTH);
        f.setFont(ModernTheme.font(1, Font.PLAIN));
        ModernTheme.roomy(f);
        cell.add(f, BorderLayout.CENTER);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, x == 0 ? 0 : ModernTheme.px(8), ModernTheme.px(8), 0);
        p.add(cell, c);
    }

    private void showPage(boolean existing) {
        existingButton.setSelected(existing);
        newButton.setSelected(!existing);
        cards.show(center, existing ? "existing" : "new");
        error.setText(" ");
        SwingUtilities.invokeLater(() -> (existing ? search : (cname.getText().isEmpty() && !company.getText().isEmpty() ? company : cname)).requestFocusInWindow());
    }

    /**
     * Fills the list with the contacts matching the search
     */
    private void load() {
        String needle = search.getText().trim();
        Context c = Context.getContact();
        String t = c.getDbIdentity() + ".";
        String fields = t + "ids," + t + "cname," + t + "prename," + t + "company," + t + "cnumber," + t + "city";
        Object[][] rows;
        try {
            DatabaseSearch s = new DatabaseSearch(c, LIMIT);
            rows = needle.isEmpty() ? s.getValuesFor(fields) : s.getValuesFor2(fields, needle, null, true, true);
        } catch (Exception e) {
            Log.Debug(this, e);
            rows = new Object[0][];
        }
        model.clear();
        for (Object[] r : rows) {
            try {
                String name = join(" ", s(r[2]), s(r[1]));
                String comp = s(r[3]);
                String title = comp.isEmpty() || comp.equals(s(r[1])) ? name : (name.isEmpty() ? comp : comp + " (" + name + ")");
                model.addElement(new Row(Integer.parseInt(s(r[0])), title, join(" · ", s(r[4]), s(r[5]))));
            } catch (Exception e) {
                Log.Debug(this, e);
            }
        }
        if (!model.isEmpty()) {
            list.setSelectedIndex(0);
        }
        listStatus.setText(model.isEmpty() ? Messages.CHOOSE_CUSTOMER_NOTHING.toString() : " ");
    }

    private boolean hasContacts() {
        try {
            return new DatabaseSearch(Context.getContact(), 1).getValuesFor(Context.getContact().getDbIdentity() + ".ids").length > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void move(int by) {
        if (model.isEmpty()) {
            return;
        }
        int i = Math.max(0, Math.min(model.size() - 1, list.getSelectedIndex() + by));
        list.setSelectedIndex(i);
        list.ensureIndexIsVisible(i);
    }

    private void confirm() {
        if (existingButton.isSelected()) {
            Row r = list.getSelectedValue();
            if (r == null) {
                error.setText(Messages.CHOOSE_CUSTOMER_PICK.toString());
                return;
            }
            try {
                result = (Contact) DatabaseObject.getObject(Context.getContact(), r.id, true);
                dispose();
            } catch (Exception e) {
                Log.Debug(this, e);
                error.setText(Messages.CHOOSE_CUSTOMER_PICK.toString());
            }
            return;
        }
        String n = cname.getText().trim();
        String co = company.getText().trim();
        if (n.isEmpty() && co.isEmpty()) {
            error.setText(Messages.CHOOSE_CUSTOMER_REQUIRED.toString());
            cname.requestFocusInWindow();
            return;
        }
        try {
            Contact c = createContact(n, prename.getText().trim(), co, street.getText().trim(),
                    zip.getText().trim(), city.getText().trim(), mail.getText().trim());
            if (c == null) {
                error.setText(Messages.CHOOSE_CUSTOMER_FAILED.toString());
                return;
            }
            result = c;
            dispose();
        } catch (Exception e) {
            Log.Debug(this, e);
            error.setText(Messages.CHOOSE_CUSTOMER_FAILED.toString() + " " + e.getMessage());
        }
    }

    /**
     * Creates and saves a new customer
     *
     * @param cname the (sur)name; may be empty when a company is given
     * @param prename
     * @param company
     * @param street
     * @param zip
     * @param city
     * @param mail
     * @return the saved contact, null if it was not saved
     */
    public static Contact createContact(String cname, String prename, String company, String street, String zip, String city, String mail) {
        Contact c = (Contact) DatabaseObject.getObject(Context.getCustomer());
        c.setisCustomer(true);
        boolean onlyCompany = cname.isEmpty() && !company.isEmpty();
        c.setCname(onlyCompany ? company : cname);
        c.setPrename(prename);
        c.setCompany(company);
        c.setisCompany(onlyCompany);
        c.setStreet(street);
        c.setZip(zip);
        c.setCity(city);
        c.setMailaddress(mail);
        try {
            c.setCountry(User.getCurrentUser().__getDefcountry());
        } catch (Exception e) {
            Log.Debug(ChooseCustomerDialog.class, e);
        }
        if (!c.save(true) || !c.isExisting()) {
            return null;
        }
        try {
            return (Contact) DatabaseObject.getObject(Context.getContact(), c.__getIDS(), true);
        } catch (Exception e) {
            return c;
        }
    }

    private static javax.swing.Action action(Runnable r) {
        return new javax.swing.AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                r.run();
            }
        };
    }

    private static String s(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String join(String sep, String... parts) {
        StringBuilder b = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isEmpty()) {
                if (b.length() > 0) {
                    b.append(sep);
                }
                b.append(p);
            }
        }
        return b.toString();
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
