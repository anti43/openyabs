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
package mpv5.ui.dialogs.belegwizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Group;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ChooseCustomerDialog;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.utils.numberformat.FormatNumber;

/**
 * Step 2 of the Beleg-Wizard: search & choose an existing receiver (customer)
 * or create a new customer via {@link ChooseCustomerDialog}.
 */
public class BelegReceiverStep extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int SEARCH_LIMIT = 200;

    static final class ContactRow {
        final int id;
        final String title;
        final String detail;

        ContactRow(int id, String title, String detail) {
            this.id = id;
            this.title = title;
            this.detail = detail;
        }

        @Override
        public String toString() {
            return detail.isEmpty() ? title : title + " \u00b7 " + detail;
        }
    }

    private final BelegWizardState state;
    private final Runnable onReceiverChanged;
    private final Runnable onAdvance;
    private final JTextField searchField = new JTextField();
    private final DefaultListModel<ContactRow> listModel = new DefaultListModel<ContactRow>();
    private final JList<ContactRow> contactList = new JList<ContactRow>(listModel);
    private final JLabel statusLabel = new JLabel(" ");
    private final JLabel detailLabel = new JLabel();
    private final Timer filterTimer;
    private boolean updatingSelection;

    public BelegReceiverStep(BelegWizardState state, Runnable onReceiverChanged, Runnable onAdvance) {
        super(new BorderLayout(0, ModernTheme.px(12)));
        this.state = state;
        this.onReceiverChanged = onReceiverChanged;
        this.onAdvance = onAdvance;
        setOpaque(false);
        setBorder(ModernTheme.padding(8, 12));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.PAGE_AXIS));
        JLabel heading = new JLabel(Messages.BELEG_WIZARD_RECEIVER_HEADING.toString());
        heading.setFont(ModernTheme.font(6, Font.BOLD));
        heading.setForeground(ModernTheme.foreground());
        JLabel subtitle = new JLabel(Messages.BELEG_WIZARD_RECEIVER_SUBTITLE.toString());
        subtitle.setFont(ModernTheme.font(0, Font.PLAIN));
        subtitle.setForeground(ModernTheme.muted());
        header.add(heading);
        header.add(Box.createVerticalStrut(ModernTheme.px(4)));
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        // Left: search bar + new customer button + customer list
        searchField.setName("belegReceiverSearch");
        searchField.putClientProperty("JTextField.placeholderText", Messages.CHOOSE_CUSTOMER_SEARCH.toString());
        JButton newCustomerButton = new JButton(Messages.BELEG_WIZARD_RECEIVER_NEW.toString());
        newCustomerButton.setName("belegReceiverNew");
        newCustomerButton.addActionListener(e -> createNewCustomer());

        JPanel searchBar = new JPanel(new BorderLayout(ModernTheme.px(8), 0));
        searchBar.setOpaque(false);
        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(newCustomerButton, BorderLayout.EAST);

        contactList.setName("belegReceiverList");
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactList.setFixedCellHeight(ModernTheme.px(32));
        contactList.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(ModernTheme.padding(4, 8));
                if (value instanceof ContactRow) {
                    ContactRow r = (ContactRow) value;
                    if (!isSelected) {
                        setText("<html><b>" + esc(r.title) + "</b>"
                                + (r.detail.isEmpty() ? "" : " <font color='#64748b'>\u00b7 " + esc(r.detail) + "</font>")
                                + "</html>");
                    } else {
                        setText(r.toString());
                    }
                }
                return this;
            }
        });
        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !updatingSelection) {
                ContactRow row = contactList.getSelectedValue();
                if (row != null) {
                    loadAndSelectContact(row.id);
                }
            }
        });
        contactList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && contactList.getSelectedValue() != null && onAdvance != null) {
                    onAdvance.run();
                }
            }
        });

        statusLabel.setFont(ModernTheme.font(-1, Font.PLAIN));
        statusLabel.setForeground(ModernTheme.muted());

        JPanel left = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        left.setOpaque(false);
        left.add(searchBar, BorderLayout.NORTH);
        left.add(new JScrollPane(contactList), BorderLayout.CENTER);
        left.add(statusLabel, BorderLayout.SOUTH);

        // Right: selected receiver details card
        detailLabel.setVerticalAlignment(JLabel.TOP);
        detailLabel.setFont(ModernTheme.font(0, Font.PLAIN));
        Card detailCard = new Card(Messages.BELEG_WIZARD_RECEIVER_SELECTED.toString()).addFull(detailLabel, 1);
        detailCard.setPreferredSize(new Dimension(ModernTheme.px(290), ModernTheme.px(260)));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, detailCard);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setOpaque(false);
        split.setResizeWeight(0.62);
        split.setDividerLocation(ModernTheme.px(470));
        add(split, BorderLayout.CENTER);

        filterTimer = new Timer(180, e -> loadContacts());
        filterTimer.setRepeats(false);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTimer.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTimer.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTimer.restart();
            }
        });

        updateDetailCard();
    }

    /**
     * Reloads the contact list matching the current search term.
     */
    public void loadContacts() {
        String term = searchField.getText() == null ? "" : searchField.getText().trim();
        List<ContactRow> rows = queryContacts(term);
        updatingSelection = true;
        try {
            listModel.clear();
            int selectIdx = -1;
            int wantedId = state.getReceiver() != null && state.getReceiver().isExisting()
                    ? state.getReceiver().__getIDS() : -1;
            for (int i = 0; i < rows.size(); i++) {
                ContactRow r = rows.get(i);
                listModel.addElement(r);
                if (r.id == wantedId) {
                    selectIdx = i;
                }
            }
            if (selectIdx >= 0) {
                contactList.setSelectedIndex(selectIdx);
                contactList.ensureIndexIsVisible(selectIdx);
            }
        } finally {
            updatingSelection = false;
        }
        statusLabel.setText(rows.isEmpty()
                ? Messages.CHOOSE_CUSTOMER_NOTHING.toString()
                : rows.size() + " " + Messages.SEARCH_KIND_CONTACTS.toString());
    }

    private List<ContactRow> queryContacts(String term) {
        List<ContactRow> out = new ArrayList<ContactRow>();
        try {
            DatabaseSearch ds = new DatabaseSearch(Context.getContact(), SEARCH_LIMIT);
            Object[][] data = ds.getValuesFor(
                    "ids, cname, prename, company, cnumber, zip, city",
                    new String[]{"cname", "prename", "company", "cnumber", "city"},
                    term, true);
            if (data != null) {
                for (Object[] row : data) {
                    if (row == null || row.length < 7 || row[0] == null) {
                        continue;
                    }
                    int id = Integer.parseInt(String.valueOf(row[0]).trim());
                    String cname = str(row[1]);
                    String prename = str(row[2]);
                    String company = str(row[3]);
                    String number = str(row[4]);
                    String zip = str(row[5]);
                    String city = str(row[6]);

                    String title = !company.isEmpty() ? company
                            : (!prename.isEmpty() ? (prename + " " + cname).trim() : cname);
                    if (title.isEmpty()) {
                        title = number;
                    }
                    List<String> parts = new ArrayList<String>();
                    if (!company.isEmpty() && !cname.isEmpty() && !company.equals(cname)) {
                        parts.add((prename + " " + cname).trim());
                    }
                    if (!number.isEmpty()) {
                        parts.add(number);
                    }
                    String loc = (zip + " " + city).trim();
                    if (!loc.isEmpty()) {
                        parts.add(loc);
                    }
                    out.add(new ContactRow(id, title, String.join(", ", parts)));
                }
            }
        } catch (Exception ex) {
            Log.Debug(this, ex);
        }
        return out;
    }

    private void loadAndSelectContact(int contactId) {
        try {
            Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), contactId, true);
            selectReceiver(c);
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex);
        }
    }

    public void selectReceiver(Contact contact) {
        state.setReceiver(contact);
        updateDetailCard();
        if (onReceiverChanged != null) {
            onReceiverChanged.run();
        }
    }

    private void createNewCustomer() {
        ChooseCustomerDialog.Prefill prefill = new ChooseCustomerDialog.Prefill();
        prefill.search = searchField.getText() == null ? "" : searchField.getText().trim();
        Contact chosen = ChooseCustomerDialog.choose(this, prefill);
        if (chosen != null) {
            selectReceiver(chosen);
            loadContacts();
        }
    }

    public void updateDetailCard() {
        Contact c = state.getReceiver();
        if (c == null) {
            detailLabel.setText("<html><div style='color:#64748b;padding:4px;'>"
                    + esc(Messages.BELEG_WIZARD_RECEIVER_EMPTY.toString()) + "</div></html>");
            return;
        }
        StringBuilder sb = new StringBuilder("<html><div style='padding:4px;'>");
        if (c.__getCompany() != null && !c.__getCompany().trim().isEmpty()) {
            sb.append("<div style='font-size:12pt;font-weight:bold;'>").append(esc(c.__getCompany())).append("</div>");
        }
        String full = ((c.__getPrename() != null ? c.__getPrename().trim() + " " : "")
                + (c.__getCname() != null ? c.__getCname().trim() : "")).trim();
        if (!full.isEmpty() && !full.equals(c.__getCompany())) {
            sb.append("<div style='font-weight:bold;'>").append(esc(full)).append("</div>");
        }
        if (c.__getCNumber() != null && !c.__getCNumber().trim().isEmpty()) {
            sb.append("<div style='color:#64748b;margin-top:2px;'>").append(esc(c.__getCNumber())).append("</div>");
        }
        sb.append("<div style='margin-top:8px;'>");
        if (c.__getStreet() != null && !c.__getStreet().trim().isEmpty()) {
            sb.append(esc(c.__getStreet())).append("<br>");
        }
        String loc = ((c.__getZip() != null ? c.__getZip().trim() + " " : "")
                + (c.__getCity() != null ? c.__getCity().trim() : "")).trim();
        if (!loc.isEmpty()) {
            sb.append(esc(loc)).append("<br>");
        }
        if (c.__getMailaddress() != null && !c.__getMailaddress().trim().isEmpty()) {
            sb.append("<br>").append(esc(c.__getMailaddress()));
        }
        sb.append("</div>");

        // Adopted group & customer tax rate
        String groupName = "";
        try {
            if (state.getGroupId() > 0) {
                groupName = String.valueOf(DatabaseObject.getObject(Context.getGroup(), state.getGroupId()));
            }
        } catch (Exception ignored) {
        }
        if (!groupName.isEmpty() || state.getCustomerTaxRate() != null) {
            sb.append("<div style='margin-top:10px;padding-top:8px;border-top:1px solid #e2e8f0;color:#475569;'>");
            if (!groupName.isEmpty()) {
                sb.append(esc(Messages.HISTORY_GROUP.toString())).append(": <b>").append(esc(groupName)).append("</b><br>");
            }
            BigDecimal rate = state.getCustomerTaxRate();
            if (rate != null) {
                sb.append(esc(Messages.TAXES_RATE.toString())).append(": <b>").append(esc(FormatNumber.formatPercent(rate))).append("</b>");
            }
            sb.append("</div>");
        }
        sb.append("</div></html>");
        detailLabel.setText(sb.toString());
    }

    public void focusSearch() {
        searchField.requestFocusInWindow();
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
