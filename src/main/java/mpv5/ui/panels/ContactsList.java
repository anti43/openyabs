package mpv5.ui.panels;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.table.TableCellRenderer;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Contact;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.misc.MPTable;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.tables.Selection;
import mpv5.ui.misc.TableViewPersistenceHandler;
import mpv5.utils.export.EInvoiceContactCheck;
import mpv5.utils.export.Export;

/**
 *
 *
 */
public class ContactsList extends javax.swing.JPanel implements ListPanel, DataPanel {

   private static final long serialVersionUID = 1L;
   private Context context;
   private java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();

   /**
    * Creates new form ListPanel
    */
   public ContactsList() {
      initComponents();
      setName("contactlist");
      installTable();
      prinitingComboBox1.init(listTable);
      this.context = Context.getContact();
      ((MPTable) listTable).setDefaultColumns(new Integer[]{0, 100, 100, 100, 100, 100, 100}, new Boolean[]{});
      ((MPTable) listTable).setPersistanceHandler(new TableViewPersistenceHandler((MPTable) listTable, this));
      modernLayout();
   }

   /**
    * The modern layout: title, count, printing and the "add" buttons; the
    * filters in a card ("Filter" is the blue main action); the table. Logic
    * unchanged.
    */
   private void modernLayout() {
      java.util.ResourceBundle b = mpv5.i18n.LanguageManager.getBundle();
      removeAll();
      setLayout(new java.awt.BorderLayout(mpv5.ui.modern.ModernTheme.px(10), mpv5.ui.modern.ModernTheme.px(10)));
      setBorder(mpv5.ui.modern.ModernTheme.padding(12, 14));
      setBackground(mpv5.ui.modern.ModernTheme.background());

      javax.swing.JLabel title = new javax.swing.JLabel(b.getString("ContactsList.jPanel1.border.title"));
      title.setFont(mpv5.ui.modern.ModernTheme.font(9, java.awt.Font.BOLD));
      count.setForeground(mpv5.ui.modern.ModernTheme.muted());
      count.setFont(mpv5.ui.modern.ModernTheme.font(0, java.awt.Font.PLAIN));
      javax.swing.JPanel left = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, mpv5.ui.modern.ModernTheme.px(10), 0));
      left.setOpaque(false);
      left.add(title);
      left.add(count);
      //the switch for the edit mode and its legend
      editToggle.setToolTipText(Messages.CONTACTLIST_EDITMODE_TIP.toString());
      editToggle.addActionListener(e -> setEditMode(editToggle.isSelected()));
      editLegend.setForeground(mpv5.ui.modern.ModernTheme.muted());
      editLegend.setVisible(false);
      editLegend.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.BorderLayout());
      header.setOpaque(false);
      header.add(left, java.awt.BorderLayout.WEST);

      //export and "new ..." live in the search card, on the far right
      javax.swing.JPanel actions = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, mpv5.ui.modern.ModernTheme.px(6), 0));
      actions.setOpaque(false);
      prinitingComboBox1.setOpaque(false);
      //all controls of the bar have the height of the main button, so they sit on one line
      final int height = jButton2.getPreferredSize().height;
      jButton3.setPreferredSize(new java.awt.Dimension(jButton3.getPreferredSize().width, height));
      for (Component c : prinitingComboBox1.getComponents()) {
         if (c instanceof javax.swing.JLabel) {
            c.setPreferredSize(null);
            ((javax.swing.JLabel) c).setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, mpv5.ui.modern.ModernTheme.px(6)));
         } else {
            c.setPreferredSize(new java.awt.Dimension(mpv5.ui.modern.ModernTheme.px(100), height));
         }
      }
      prinitingComboBox1.setPreferredSize(null);
      actions.add(new javax.swing.JLabel(jLabel4.getText()));
      //"new ...": green, each one runs the action of the button of the form
      for (final javax.swing.JButton form : new javax.swing.JButton[]{jButton1, jButton5, jButton4}) {
         mpv5.ui.modern.PrimaryButton add = new mpv5.ui.modern.PrimaryButton(form.getText(), null);
         add.setColor(new java.awt.Color(22, 163, 74));
         add.setToolTipText(jLabel4.getText().replace(":", "") + ": " + form.getText());
         add.setPreferredSize(new java.awt.Dimension(add.getPreferredSize().width, height));
         add.addActionListener(e -> form.doClick(0));
         actions.add(add);
      }

      mpv5.ui.modern.ModernTheme.roomy(jTextField1);
      jTextField1.putClientProperty("JTextField.placeholderText", b.getString("ContactsList.jLabel3.text").replace(":", ""));
      javax.swing.JPanel show = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, mpv5.ui.modern.ModernTheme.px(4), 0));
      show.setOpaque(false);
      for (javax.swing.JCheckBox c : new javax.swing.JCheckBox[]{jCheckBox1, jCheckBox2, jCheckBox3, jCheckBox4, excButton}) {
         c.setOpaque(false);
         c.setMinimumSize(null);
         c.setPreferredSize(null);
         c.setPreferredSize(new java.awt.Dimension(c.getPreferredSize().width, jButton2.getPreferredSize().height));
         show.add(c);
      }
      javax.swing.JPanel buttons = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, mpv5.ui.modern.ModernTheme.px(6), 0));
      buttons.setOpaque(false);
      buttons.add(jButton2);
      buttons.add(jButton3);
      jTextField1.setPreferredSize(new java.awt.Dimension(mpv5.ui.modern.ModernTheme.px(150), jButton2.getPreferredSize().height));
      //one line if there is room, otherwise the groups wrap; the last group (export, new) always sits on the right
      final int gap = mpv5.ui.modern.ModernTheme.px(10);
      javax.swing.JPanel bar = new javax.swing.JPanel(new mpv5.ui.modern.WrapLayout(java.awt.FlowLayout.LEFT, gap, mpv5.ui.modern.ModernTheme.px(4)) {
         private static final long serialVersionUID = 1L;

         @Override
         public void layoutContainer(java.awt.Container target) {
            super.layoutContainer(target);
            if (target.getComponentCount() > 0) {
               Component last = target.getComponent(target.getComponentCount() - 1);
               int x = target.getWidth() - target.getInsets().right - last.getWidth();
               if (x > last.getX()) {
                  last.setLocation(x, last.getY());
               }
            }
         }
      });
      bar.setOpaque(false);
      bar.add(group(jLabel3.getText().replace(":", ""), jTextField1));
      bar.add(group(jLabel1.getText().replace(":", ""), show));
      bar.add(group(" ", buttons));
      bar.add(group(" ", actions));
      mpv5.ui.modern.Card filters = new mpv5.ui.modern.Card(null).addFull(bar);

      jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(mpv5.ui.modern.ModernTheme.border()));
      jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      //below the table: export on the left, the edit mode on the right
      javax.swing.JPanel bottom = new javax.swing.JPanel(new java.awt.BorderLayout(mpv5.ui.modern.ModernTheme.px(10), 0));
      bottom.setOpaque(false);
      bottom.add(prinitingComboBox1, java.awt.BorderLayout.WEST);
      bottom.add(editLegend, java.awt.BorderLayout.CENTER);
      bottom.add(editToggle, java.awt.BorderLayout.EAST);
      mpv5.ui.modern.Card list = new mpv5.ui.modern.Card(null).addFull(jScrollPane1, 1).addFull(bottom);

      javax.swing.JPanel top = new javax.swing.JPanel(new java.awt.BorderLayout(0, mpv5.ui.modern.ModernTheme.px(10)));
      top.setOpaque(false);
      top.add(header, java.awt.BorderLayout.NORTH);
      top.add(filters, java.awt.BorderLayout.CENTER);
      add(top, java.awt.BorderLayout.NORTH);
      add(list, java.awt.BorderLayout.CENTER);
   }

   /**
    * A component with its label above
    */
   private static javax.swing.JPanel group(String label, Component c) {
      javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout(0, mpv5.ui.modern.ModernTheme.px(2)));
      p.setOpaque(false);
      p.add(mpv5.ui.modern.ModernTheme.fieldLabel(label), java.awt.BorderLayout.NORTH);
      p.add(c, java.awt.BorderLayout.CENTER);
      return p;
   }

   public ContactsList(Context context) {
      this();

      this.context = context;
      context.setSearchFields(Context.DETAILS_CONTACTS);
//        fill(true, true, true, true, false);
   }

   private void fill(boolean customer, boolean supplier, boolean manufacturer, boolean company, boolean filtered) {

      context.setContactConditions(customer, supplier, manufacturer, company);
      context.setExclusiveContactConditions(customer, supplier, manufacturer, company);
      context.useExclusiveConditions(filtered);
      Object[][] data;
      if (jTextField1.getText() != null && jTextField1.getText().length() > 0) {
         data = new DatabaseSearch(context).getValuesFor(Context.DETAILS_CONTACTS, Context.DETAIL_CONTACT_SEARCH.split(","), jTextField1.getText(), true);
      } else {
         data = new DatabaseSearch(context).getValuesFor(Context.DETAILS_CONTACTS);
      }
      setData(data);
      count.setText(String.format(mpv5.globals.Messages.CONTACTS_COUNT.toString(), listTable.getModel().getRowCount()));
   }

   //<editor-fold defaultstate="collapsed" desc="table: full width, hidden id, edit mode, e-invoice check">
   /**
    * The columns of the model, see {@link Context#DETAILS_CONTACTS}
    */
   private static final int COL_ID = 0, COL_CNUMBER = 1, COL_TITLE = 2, COL_PRENAME = 3, COL_NAME = 4, COL_STREET = 5,
         COL_ZIP = 6, COL_CITY = 7, COL_COUNTRY = 8, COL_MAINPHONE = 9, COL_FAX = 10, COL_MOBILEPHONE = 11,
         COL_WORKPHONE = 12, COL_COMPANY = 13, COL_MAIL = 14, COL_WEBSITE = 15, COL_NOTES = 16, COL_TAXNUMBER = 17,
         COL_IBAN = 18,
         /**
          * not a column of the contacts table: the own account the contact pays to (value property)
          */
         COL_ACCOUNT = 19,
         /**
          * the tax rate of the contact (value property)
          */
         COL_TAX = 20,
         /**
          * the result of the e-invoice check, to sort by
          */
         COL_VALID = 21, COLUMNS = 22;
   /**
    * The id of the tax rate by contact id
    */
   private final java.util.Map<String, String> taxes = new java.util.HashMap<String, String>();
   private java.util.List<mpv5.handler.ContactService.TaxOption> taxOptions = new java.util.ArrayList<mpv5.handler.ContactService.TaxOption>();
   /**
    * The own account the contact pays to (IBAN) by contact id, none: the
    * default account
    */
   private final java.util.Map<String, String> accounts = new java.util.HashMap<String, String>();
   private final mpv5.ui.modern.ChipToggle editToggle = new mpv5.ui.modern.ChipToggle(Messages.CONTACTLIST_EDITMODE.toString()) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void paintComponent(java.awt.Graphics g) {
         super.paintComponent(g);
         if (!isSelected()) {
            //light blue while off (the chip is white otherwise), blue while on
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight() - 1;
            g2.setColor(mpv5.ui.modern.ModernTheme.blend(mpv5.ui.modern.ModernTheme.accent(), mpv5.ui.modern.ModernTheme.cardBackground(), getModel().isRollover() ? 0.6f : 0.75f));
            g2.fillRoundRect(0, 0, getWidth() - 1, h, h, h);
            g2.dispose();
            getUI().paint(g, this);
         }
      }
   };
   private final javax.swing.JLabel editLegend = new javax.swing.JLabel(Messages.CONTACTLIST_EDITMODE_LEGEND.toString());
   private boolean editMode;
   /**
    * The country as stored (a code like 276) by contact id, the table shows
    * the name
    */
   private final java.util.Map<String, String> countries = new java.util.HashMap<String, String>();
   /**
    * The e-invoice check by row of the model
    */
   private final java.util.Map<Integer, EInvoiceContactCheck.Result> checks = new java.util.HashMap<Integer, EInvoiceContactCheck.Result>();
   private String homeCountry;

   /**
    * Replaces the table of the form: the columns use the full width as long
    * as they fit, otherwise the table scrolls; in the edit mode rows with
    * missing e-invoice data are colored.
    */
   private void installTable() {
      java.awt.event.MouseListener[] listeners = listTable.getMouseListeners();
      MPTable table = new MPTable(this) {
         private static final long serialVersionUID = 1L;

         @Override
         public boolean getScrollableTracksViewportWidth() {
            return getParent() != null && getPreferredSize().width < getParent().getWidth();
         }

         @Override
         public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
            Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
            if (c instanceof JComponent) {
               Object value = getValueAt(rowIndex, vColIndex);
               String tip = value == null || String.valueOf(value).isEmpty() ? null : String.valueOf(value);
               //the renderer keeps a background once set: always set it
               java.awt.Color background = getBackground();
               if (editMode || convertColumnIndexToModel(vColIndex) == COL_VALID) {
                  EInvoiceContactCheck.Result check = check(convertRowIndexToModel(rowIndex));
                  if (!check.isOk()) {
                     tip = tooltip(check);
                     if (editMode) {
                        background = color(check, convertColumnIndexToModel(vColIndex), background);
                     }
                  }
               }
               if (!isRowSelected(rowIndex)) {
                  c.setBackground(background);
               }
               ((JComponent) c).setToolTipText(tip);
            }
            return c;
         }
      };
      table.setName("listTable");
      table.setAutoCreateRowSorter(true);
      table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
      table.setDragEnabled(true);
      table.setFillsViewportHeight(true);
      table.setShowVerticalLines(false);
      table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
      for (java.awt.event.MouseListener l : listeners) {
         if (l.getClass().getName().startsWith(ContactsList.class.getName())) {
            table.addMouseListener(l);//open the contact on double click
         }
      }
      listTable = table;
      jScrollPane1.setViewportView(listTable);
   }

   /**
    * Show the contacts
    *
    * @param data rows as defined by {@link Context#DETAILS_CONTACTS}
    */
   private void setData(Object[][] data) {
      if (listTable.isEditing()) {
         listTable.getCellEditor().cancelCellEditing();
      }
      countries.clear();
      checks.clear();
      mpv5.i18n.LanguageManager.getCountriesAsComboBoxModel();//loads the country names
      homeCountry = mpv5.utils.export.ZugpferdFile.toCountryCode(mpv5.utils.export.ZugpferdFile.companySettings().apply("country"));
      accounts.clear();
      for (java.util.Map.Entry<Integer, String> e : mpv5.utils.export.ContactProperty.all(mpv5.utils.export.BankAccount.KEY_CONTACT).entrySet()) {
         accounts.put(String.valueOf(e.getKey()), mpv5.utils.text.Iban.normalize(e.getValue()));
      }
      taxes.clear();
      for (java.util.Map.Entry<Integer, String> e : mpv5.utils.export.ContactProperty.all(mpv5.handler.ContactService.KEY_TAX).entrySet()) {
         taxes.put(String.valueOf(e.getKey()), e.getValue().trim());
      }
      taxOptions = mpv5.handler.ContactService.taxOptions();
      java.util.List<mpv5.utils.export.BankAccount> own = mpv5.utils.export.BankAccount.companyAccounts(mpv5.utils.export.ZugpferdFile.companySettings());
      for (int i = 0; i < data.length; i++) {
         Object[] row = java.util.Arrays.copyOf(data[i], COLUMNS);
         String country = row[COL_COUNTRY] == null ? "" : String.valueOf(row[COL_COUNTRY]);
         countries.put(String.valueOf(row[COL_ID]), country);
         row[COL_COUNTRY] = mpv5.db.objects.Address.countryName(country);
         row[COL_ACCOUNT] = accountLabel(own, accounts.get(String.valueOf(row[COL_ID])));
         row[COL_TAX] = taxLabel(taxes.get(String.valueOf(row[COL_ID])));
         data[i] = row;
      }
      String[] header = java.util.Arrays.copyOf(Headers.CONTACT_DETAILS.getValue(), COLUMNS);
      header[COL_ACCOUNT] = Messages.EINVOICE_ACCOUNT.toString();
      header[COL_TAX] = Messages.CONTACT_TAXRATE.toString();
      header[COL_VALID] = Messages.CONTACTLIST_EINVOICE.toString();
      Class[] types = new Class[COLUMNS];
      java.util.Arrays.fill(types, Object.class);
      MPTableModel model = new MPTableModel(types, editable(editMode), data, header) {
         private static final long serialVersionUID = 1L;

         @Override
         public void setValueAt(Object value, int row, int column) {
            if (!editMode) {
               super.setValueAt(value, row, column);
               return;
            }
            Object shown = saveCell(row, column, value);
            if (shown != null) {
               super.setValueAt(shown, row, column);
               checks.remove(row);
               super.setValueAt(validLabel(check(row)), row, COL_VALID);
               listTable.repaint();
            }
         }
      };
      listTable.setModel(model);
      for (int row = 0; row < model.getRowCount(); row++) {
         model.setValueAt(validLabel(check(row)), row, COL_VALID, true);
      }
      setupColumns();
   }

   /**
    * @param own the accounts of the company info
    * @param iban the account assigned to the contact, null or empty for the
    * default
    * @return the text for the table
    */
   private static String accountLabel(java.util.List<mpv5.utils.export.BankAccount> own, String iban) {
      if (iban == null || iban.isEmpty() || !own.isEmpty() && own.get(0).getIban().equals(iban)) {
         return Messages.EINVOICE_ACCOUNT_DEFAULT.toString();
      }
      mpv5.utils.export.BankAccount a = mpv5.utils.export.BankAccount.find(own, iban);
      return a != null ? a.toString() : mpv5.utils.text.Iban.format(iban) + " - " + Messages.EINVOICE_ACCOUNT_MISSING;
   }

   private String taxLabel(String taxId) {
      mpv5.handler.ContactService.TaxOption o = mpv5.handler.ContactService.taxOption(taxOptions, taxId);
      return o.id.isEmpty() ? "" : o.label;
   }

   private static boolean[] editable(boolean edit) {
      boolean[] b = new boolean[COLUMNS];
      java.util.Arrays.fill(b, edit);
      //the customer number identifies the contact in documents and bookkeeping: only in the contact itself
      b[COL_ID] = false;
      b[COL_CNUMBER] = false;
      b[COL_VALID] = false;
      return b;
   }

   /**
    * Hides the internal id (it stays in the model, for the selection and the
    * exports) and sets the editor for the country
    */
   private void setupColumns() {
      java.util.Enumeration<javax.swing.table.TableColumn> columns = listTable.getColumnModel().getColumns();
      while (columns.hasMoreElements()) {
         javax.swing.table.TableColumn column = columns.nextElement();
         if (column.getModelIndex() == COL_ID) {
            column.setMinWidth(0);
            column.setMaxWidth(0);
            column.setPreferredWidth(0);
            column.setResizable(false);
         } else if (column.getModelIndex() == COL_COUNTRY) {
            final javax.swing.JComboBox<Object> box = new javax.swing.JComboBox<Object>();
            box.setModel(mpv5.i18n.LanguageManager.getCountriesAsComboBoxModel());
            javax.swing.DefaultCellEditor editor = new javax.swing.DefaultCellEditor(box) {
               private static final long serialVersionUID = 1L;

               @Override
               public Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int col) {
                  //select the stored country, not the first one of the list
                  String id = String.valueOf(table.getModel().getValueAt(table.convertRowIndexToModel(row), COL_ID));
                  String code = countries.get(id);
                  int index = -1;
                  for (int i = 0; i < box.getItemCount(); i++) {
                     if (box.getItemAt(i) instanceof mpv5.utils.models.MPComboBoxModelItem
                           && ((mpv5.utils.models.MPComboBoxModelItem) box.getItemAt(i)).getId().equals(code)) {
                        index = i;
                        break;
                     }
                  }
                  box.setSelectedIndex(index);
                  return box;
               }
            };
            editor.setClickCountToStart(2);
            column.setCellEditor(editor);
         } else if (column.getModelIndex() == COL_TAX) {
            final javax.swing.JComboBox<Object> box = new javax.swing.JComboBox<Object>(taxOptions.toArray());
            javax.swing.DefaultCellEditor editor = new javax.swing.DefaultCellEditor(box) {
               private static final long serialVersionUID = 1L;

               @Override
               public Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int col) {
                  String id = String.valueOf(table.getModel().getValueAt(table.convertRowIndexToModel(row), COL_ID));
                  box.setSelectedItem(mpv5.handler.ContactService.taxOption(taxOptions, taxes.get(id)));
                  return box;
               }
            };
            editor.setClickCountToStart(2);
            column.setCellEditor(editor);
         } else if (column.getModelIndex() == COL_ACCOUNT) {
            final javax.swing.JComboBox<Object> box = new javax.swing.JComboBox<Object>();
            javax.swing.DefaultCellEditor editor = new javax.swing.DefaultCellEditor(box) {
               private static final long serialVersionUID = 1L;

               @Override
               public Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int col) {
                  //the accounts of the company info, the one of the contact selected
                  String id = String.valueOf(table.getModel().getValueAt(table.convertRowIndexToModel(row), COL_ID));
                  java.util.List<mpv5.handler.ContactService.AccountOption> options = new java.util.ArrayList<mpv5.handler.ContactService.AccountOption>();
                  int index = -1;
                  try {
                     Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), Integer.parseInt(id));
                     options = mpv5.handler.ContactService.accountOptions(c);
                     index = mpv5.handler.ContactService.selectedAccount(options, mpv5.utils.export.BankAccount.contactIban(c));
                  } catch (Exception ex) {
                     Log.Debug(ex);
                  }
                  box.setModel(new javax.swing.DefaultComboBoxModel<Object>(options.toArray()));
                  box.setSelectedIndex(index);
                  return box;
               }
            };
            editor.setClickCountToStart(2);
            column.setCellEditor(editor);
         }
      }
   }

   private void setEditMode(boolean edit) {
      if (listTable.isEditing()) {
         listTable.getCellEditor().stopCellEditing();
      }
      editMode = edit;
      editLegend.setVisible(edit);
      checks.clear();
      if (listTable.getModel() instanceof MPTableModel) {
         ((MPTableModel) listTable.getModel()).setCanEdits(editable(edit));
      }
      listTable.repaint();
   }

   /**
    * Write the changed value of a cell to the contact
    *
    * @return the value to show in the table, null if nothing was saved
    */
   private Object saveCell(int row, int column, Object value) {
      javax.swing.table.TableModel model = listTable.getModel();
      Object old = model.getValueAt(row, column);
      String id = String.valueOf(model.getValueAt(row, COL_ID));
      String text = value == null ? "" : String.valueOf(value).trim();
      String country = null;
      if (column == COL_ACCOUNT) {
         if (!(value instanceof mpv5.handler.ContactService.AccountOption)) {
            return null;//nothing selected
         }
         String iban = ((mpv5.handler.ContactService.AccountOption) value).iban;
         String before = accounts.get(id) == null ? "" : accounts.get(id);
         if (iban.equals(before)) {
            return null;
         }
         try {
            Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), Integer.parseInt(id));
            mpv5.utils.export.BankAccount.saveForContact(c, iban);
            saved(row);
            accounts.put(id, iban);
            return accountLabel(mpv5.utils.export.BankAccount.companyAccounts(mpv5.utils.export.ZugpferdFile.companySettings()), iban);
         } catch (Exception ex) {
            Log.Debug(ex);
            return null;
         }
      }
      if (column == COL_TAX) {
         if (!(value instanceof mpv5.handler.ContactService.TaxOption)) {
            return null;//nothing selected
         }
         String tax = ((mpv5.handler.ContactService.TaxOption) value).id;
         if (tax.equals(mpv5.handler.ContactService.taxOption(taxOptions, taxes.get(id)).id)) {
            return null;
         }
         try {
            Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), Integer.parseInt(id));
            mpv5.handler.ContactService.saveContactTax(c, tax);
            saved(row);
            taxes.put(id, tax);
            return taxLabel(tax);
         } catch (Exception ex) {
            Log.Debug(ex);
            return null;
         }
      }
      if (column == COL_COUNTRY) {
         if (!(value instanceof mpv5.utils.models.MPComboBoxModelItem)) {
            return null;//nothing selected
         }
         country = ((mpv5.utils.models.MPComboBoxModelItem) value).getId();
         if (country.equals(countries.get(id))) {
            return null;
         }
      } else if (text.equals(old == null ? "" : String.valueOf(old))) {
         return null;
      }
      try {
         Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), Integer.parseInt(id));
         switch (column) {
            case COL_TITLE:
               c.setTitle(text);
               break;
            case COL_PRENAME:
               c.setPrename(text);
               break;
            case COL_NAME:
               c.setCname(text);
               break;
            case COL_STREET:
               c.setStreet(text);
               break;
            case COL_ZIP:
               c.setZip(text);
               break;
            case COL_CITY:
               c.setCity(text);
               break;
            case COL_COUNTRY:
               c.setCountry(country);
               break;
            case COL_MAINPHONE:
               c.setMainphone(text);
               break;
            case COL_FAX:
               c.setFax(text);
               break;
            case COL_MOBILEPHONE:
               c.setMobilephone(text);
               break;
            case COL_WORKPHONE:
               c.setWorkphone(text);
               break;
            case COL_COMPANY:
               c.setCompany(text);
               break;
            case COL_MAIL:
               c.setMailaddress(text);
               break;
            case COL_WEBSITE:
               c.setWebsite(text);
               break;
            case COL_NOTES:
               c.setNotes(text);
               break;
            case COL_TAXNUMBER:
               c.settaxnumber(text);
               break;
            case COL_IBAN:
               c.setBankaccount(text);
               break;
            default:
               return null;
         }
         if (!c.save()) {
            return null;
         }
         mpv5.YabsViewProxy.instance().addMessage(Messages.CONTACTLIST_SAVED + " " + c.__getCNumber() + " " + c.__getCname());
         if (country != null) {
            countries.put(id, country);
            return String.valueOf(value);
         }
         return text;
      } catch (Exception ex) {
         Log.Debug(ex);
         return null;
      }
   }

   /**
    * Tell the user that the change has been saved
    */
   private void saved(int row) {
      javax.swing.table.TableModel m = listTable.getModel();
      mpv5.YabsViewProxy.instance().addMessage(Messages.CONTACTLIST_SAVED + " " + text(m, row, COL_CNUMBER) + " " + text(m, row, COL_NAME));
   }

   private EInvoiceContactCheck.Result check(int row) {
      EInvoiceContactCheck.Result r = checks.get(row);
      if (r == null) {
         javax.swing.table.TableModel m = listTable.getModel();
         r = EInvoiceContactCheck.check(
               text(m, row, COL_PRENAME) + text(m, row, COL_NAME), text(m, row, COL_COMPANY),
               text(m, row, COL_STREET), text(m, row, COL_ZIP), text(m, row, COL_CITY),
               countries.get(text(m, row, COL_ID)), text(m, row, COL_MAIL), text(m, row, COL_TAXNUMBER),
               text(m, row, COL_IBAN), homeCountry);
         checks.put(row, r);
      }
      return r;
   }

   private static String validLabel(EInvoiceContactCheck.Result check) {
      return (check.isOk() ? Messages.CONTACTLIST_EINVOICE_YES : check.hasErrors()
            ? Messages.CONTACTLIST_EINVOICE_NO : Messages.CONTACTLIST_EINVOICE_CHECK).toString();
   }

   private static String text(javax.swing.table.TableModel m, int row, int column) {
      Object v = m.getValueAt(row, column);
      return v == null ? "" : String.valueOf(v);
   }

   private static String tooltip(EInvoiceContactCheck.Result check) {
      StringBuilder b = new StringBuilder("<html>");
      for (String message : check.getMessages()) {
         b.append(message.replace("&", "&amp;").replace("<", "&lt;")).append("<br>");
      }
      return b.append("</html>").toString();
   }

   /**
    * Red: an e-invoice is not possible, yellow: to be checked. The row is
    * tinted, the cells to fix are stronger.
    */
   private static java.awt.Color color(EInvoiceContactCheck.Result check, int column, java.awt.Color background) {
      java.awt.Color red = new java.awt.Color(220, 60, 60);
      java.awt.Color yellow = new java.awt.Color(235, 170, 0);
      for (EInvoiceContactCheck.Field f : check.getErrors()) {
         if (isColumnOf(f, column)) {
            return mpv5.ui.modern.ModernTheme.blend(red, background, 0.55f);
         }
      }
      for (EInvoiceContactCheck.Field f : check.getHints()) {
         if (isColumnOf(f, column)) {
            return mpv5.ui.modern.ModernTheme.blend(yellow, background, 0.5f);
         }
      }
      return mpv5.ui.modern.ModernTheme.blend(check.hasErrors() ? red : yellow, background, 0.87f);
   }

   private static boolean isColumnOf(EInvoiceContactCheck.Field field, int column) {
      switch (field) {
         case NAME:
            return column == COL_NAME || column == COL_COMPANY;
         case STREET:
            return column == COL_STREET;
         case ZIP:
            return column == COL_ZIP;
         case CITY:
            return column == COL_CITY;
         case COUNTRY:
            return column == COL_COUNTRY;
         case EMAIL:
            return column == COL_MAIL;
         case VATID:
            return column == COL_TAXNUMBER;
         case IBAN:
            return column == COL_IBAN;
         default:
            return false;
      }
   }
   //</editor-fold>

   /**
    * This me4thod is called from within the constructor to initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is always
    * regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listTable = new  mpv5.ui.misc.MPTable(this) {
            public Component prepareRenderer(TableCellRenderer renderer,
                int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent)c;
                    jc.setToolTipText(String.valueOf(getValueAt(rowIndex, vColIndex)));
                }
                return c;
            }
        };
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton2 = new mpv5.ui.modern.PrimaryButton("", null); //the main action: blue, Shift+Enter
        jButton3 = new javax.swing.JButton();
        jToolBar2 = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        excButton = new javax.swing.JCheckBox();
        prinitingComboBox1 = new mpv5.ui.beans.PrinitingComboBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        count = new javax.swing.JLabel();

        setName("Form"); // NOI18N

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ContactsList.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        listTable.setAutoCreateRowSorter(true);
        listTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        listTable.setDragEnabled(true);
        listTable.setFillsViewportHeight(true);
        listTable.setName("listTable"); // NOI18N
        listTable.setShowVerticalLines(false);
        listTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(listTable);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ContactsList.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel3.setFont(jLabel3.getFont());
        jLabel3.setText(bundle.getString("ContactsList.jLabel3.text")); // NOI18N
        jLabel3.setMaximumSize(new java.awt.Dimension(100, 14));
        jLabel3.setMinimumSize(new java.awt.Dimension(100, 14));
        jLabel3.setName("jLabel3"); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(100, 30));

        jTextField1.setFont(jTextField1.getFont());
        jTextField1.setText(bundle.getString("ContactsList.jTextField1.text")); // NOI18N
        jTextField1.setName("jTextField1"); // NOI18N
        jTextField1.setPreferredSize(new java.awt.Dimension(200, 30));
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jButton2.setFont(jButton2.getFont());
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/22/mail_find.png"))); // NOI18N
        jButton2.setText(bundle.getString("ContactsList.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setFont(jButton3.getFont());
        jButton3.setText(bundle.getString("ContactsList.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.setPreferredSize(null);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jToolBar2.setFloatable(false);
        jToolBar2.setMaximumSize(new java.awt.Dimension(457, 33));
        jToolBar2.setName("jToolBar2"); // NOI18N
        jToolBar2.setPreferredSize(new java.awt.Dimension(670, 23));

        jLabel1.setFont(jLabel1.getFont());
        jLabel1.setText(bundle.getString("ContactsList.jLabel1.text")); // NOI18N
        jLabel1.setMaximumSize(new java.awt.Dimension(100, 14));
        jLabel1.setMinimumSize(new java.awt.Dimension(100, 14));
        jLabel1.setName("jLabel1"); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 14));
        jToolBar2.add(jLabel1);

        jCheckBox1.setFont(jCheckBox1.getFont());
        jCheckBox1.setText(bundle.getString("ContactsList.jCheckBox1.text")); // NOI18N
        jCheckBox1.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jCheckBox1.setMaximumSize(new java.awt.Dimension(333, 33));
        jCheckBox1.setMinimumSize(new java.awt.Dimension(100, 20));
        jCheckBox1.setName("jCheckBox1"); // NOI18N
        jCheckBox1.setPreferredSize(new java.awt.Dimension(100, 20));
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ItemStateChanged(evt);
            }
        });
        jToolBar2.add(jCheckBox1);

        jCheckBox2.setFont(jCheckBox2.getFont());
        jCheckBox2.setText(bundle.getString("ContactsList.jCheckBox2.text")); // NOI18N
        jCheckBox2.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jCheckBox2.setMaximumSize(new java.awt.Dimension(333, 33));
        jCheckBox2.setMinimumSize(new java.awt.Dimension(100, 20));
        jCheckBox2.setName("jCheckBox2"); // NOI18N
        jCheckBox2.setPreferredSize(new java.awt.Dimension(100, 20));
        jCheckBox2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ItemStateChanged(evt);
            }
        });
        jToolBar2.add(jCheckBox2);

        jCheckBox3.setFont(jCheckBox3.getFont());
        jCheckBox3.setText(bundle.getString("ContactsList.jCheckBox3.text")); // NOI18N
        jCheckBox3.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jCheckBox3.setMaximumSize(new java.awt.Dimension(333, 33));
        jCheckBox3.setMinimumSize(new java.awt.Dimension(100, 20));
        jCheckBox3.setName("jCheckBox3"); // NOI18N
        jCheckBox3.setPreferredSize(new java.awt.Dimension(100, 20));
        jCheckBox3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ItemStateChanged(evt);
            }
        });
        jToolBar2.add(jCheckBox3);

        jCheckBox4.setFont(jCheckBox4.getFont());
        jCheckBox4.setText(bundle.getString("ContactsList.jCheckBox4.text")); // NOI18N
        jCheckBox4.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jCheckBox4.setMaximumSize(new java.awt.Dimension(333, 33));
        jCheckBox4.setMinimumSize(new java.awt.Dimension(100, 20));
        jCheckBox4.setName("jCheckBox4"); // NOI18N
        jCheckBox4.setPreferredSize(new java.awt.Dimension(100, 20));
        jCheckBox4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ItemStateChanged(evt);
            }
        });
        jToolBar2.add(jCheckBox4);

        excButton.setFont(excButton.getFont().deriveFont(excButton.getFont().getStyle() | java.awt.Font.BOLD));
        excButton.setText(bundle.getString("ContactsList.excButton.text")); // NOI18N
        excButton.setToolTipText(bundle.getString("ContactsList.excButton.toolTipText")); // NOI18N
        excButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        excButton.setMaximumSize(new java.awt.Dimension(333, 33));
        excButton.setMinimumSize(new java.awt.Dimension(100, 20));
        excButton.setName("excButton"); // NOI18N
        excButton.setPreferredSize(new java.awt.Dimension(100, 20));
        excButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                excButtonItemStateChanged(evt);
            }
        });
        jToolBar2.add(excButton);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        prinitingComboBox1.setName("prinitingComboBox1"); // NOI18N
        prinitingComboBox1.setPreferredSize(new java.awt.Dimension(200, 20));

        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(232, 227, 227), 1, true));
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel4.setFont(jLabel4.getFont().deriveFont(jLabel4.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel4.setText(bundle.getString("ContactsList.jLabel4.text")); // NOI18N
        jLabel4.setMaximumSize(new java.awt.Dimension(200, 20));
        jLabel4.setMinimumSize(new java.awt.Dimension(50, 20));
        jLabel4.setName("jLabel4"); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(150, 20));
        jPanel3.add(jLabel4);

        jButton5.setFont(jButton5.getFont());
        jButton5.setText(bundle.getString("ContactsList.jButton5.text")); // NOI18N
        jButton5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        jButton5.setContentAreaFilled(false);
        jButton5.setName("jButton5"); // NOI18N
        jButton5.setPreferredSize(new java.awt.Dimension(120, 30));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton5);

        jButton1.setFont(jButton1.getFont());
        jButton1.setText(bundle.getString("ContactsList.jButton1.text")); // NOI18N
        jButton1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        jButton1.setContentAreaFilled(false);
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(120, 30));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton1);

        jButton4.setFont(jButton4.getFont());
        jButton4.setText(bundle.getString("ContactsList.jButton4.text")); // NOI18N
        jButton4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        jButton4.setContentAreaFilled(false);
        jButton4.setName("jButton4"); // NOI18N
        jButton4.setPreferredSize(new java.awt.Dimension(120, 30));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton4);

        count.setFont(count.getFont().deriveFont(count.getFont().getStyle() | java.awt.Font.BOLD));
        count.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        count.setText(bundle.getString("ContactsList.count.text")); // NOI18N
        count.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        count.setName("count"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(183, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(prinitingComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(count, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(prinitingComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(count, javax.swing.GroupLayout.Alignment.LEADING)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ItemStateChanged
//        fill(jCheckBox1.isSelected(), jCheckBox2.isSelected(), jCheckBox3.isSelected(), jCheckBox4.isSelected(), excButton.isSelected());
}//GEN-LAST:event_ItemStateChanged

    private void excButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_excButtonItemStateChanged

       fill(jCheckBox1.isSelected(), jCheckBox2.isSelected(), jCheckBox3.isSelected(), jCheckBox4.isSelected(), excButton.isSelected());
    }//GEN-LAST:event_excButtonItemStateChanged

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
       fill(jCheckBox1.isSelected(), jCheckBox2.isSelected(), jCheckBox3.isSelected(), jCheckBox4.isSelected(), excButton.isSelected());
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void listTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listTableMouseClicked

       if (evt.getClickCount() > 1 && !editMode) {
          Selection sel = new Selection(listTable);
          if (sel.checkID()) {
             try {
                mpv5.YabsViewProxy.instance().getIdentifierView().addTab(DatabaseObject.getObject(context, sel.getId()));
             } catch (NodataFoundException ex) {
                Log.Debug(ex);
             }
          }
       }
    }//GEN-LAST:event_listTableMouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

       DatabaseObject d = DatabaseObject.getObject(Context.getCustomer());
       ((mpv5.db.objects.Contact) d).setisCustomer(true);
       mpv5.YabsViewProxy.instance().getIdentifierView().addTab(d, Messages.NEW_CUSTOMER);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

       jTextField1ActionPerformed(evt);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
       jTextField1.setText("");
       jCheckBox1.setSelected(false);
       jCheckBox2.setSelected(false);
       jCheckBox3.setSelected(false);
       jCheckBox4.setSelected(false);
       excButton.setSelected(false);
       jTextField1ActionPerformed(evt);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

       DatabaseObject d = DatabaseObject.getObject(Context.getManufacturer());
       ((mpv5.db.objects.Contact) d).setisManufacturer(true);
       mpv5.YabsViewProxy.instance().getIdentifierView().addTab(d, Messages.NEW_MANUFACTURER);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed

       DatabaseObject d = DatabaseObject.getObject(Context.getSupplier());
       ((mpv5.db.objects.Contact) d).setisSupplier(true);
       mpv5.YabsViewProxy.instance().getIdentifierView().addTab(d, Messages.NEW_SUPPLIER);

    }//GEN-LAST:event_jButton5ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel count;
    private javax.swing.JCheckBox excButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JTable listTable;
    private mpv5.ui.beans.PrinitingComboBox prinitingComboBox1;
    // End of variables declaration//GEN-END:variables

   /**
    * @return the context
    */
   public Context getContext() {
      return context;
   }

   /**
    * @param context the context to set
    */
   public void setContext(Context context) {
      this.context = context;
   }

   public void refresh() {
      String t = jTextField1.getText();
      jTextField1.setText("");
      jTextField1ActionPerformed(null);
      jTextField1.setText(t);
   }

   public void flush() {
      listTable.setModel(null);
   }

   public void showType(Contact d) {
      jCheckBox1.setSelected(d.__getIscustomer());
      jCheckBox2.setSelected(d.__getIssupplier());
      jCheckBox3.setSelected(d.__getIsmanufacturer());
      jCheckBox4.setSelected(d.__getisCompany());
      jTextField1ActionPerformed(null);
   }

   public boolean collectData() {
      return false;
   }

   public DatabaseObject getDataOwner() {
      return null;
   }

   public void setDataOwner(DatabaseObject object, boolean populateData) {
   }

   public void exposeData() {
   }

   public void paste(DatabaseObject... dbo) {
   }

   public void showRequiredFields() {
   }

   public void showSearchBar(boolean show) {
   }

   public void actionAfterSave() {
   }

   public void actionAfterCreate() {
   }

   public void actionBeforeCreate() {
   }

   public void actionBeforeSave() throws ChangeNotApprovedException {
      //Ctrl+S while a cell is edited: take over the value (which saves it)
      if (listTable.isEditing()) {
         listTable.getCellEditor().stopCellEditing();
      }
   }

   public void mail() {
   }

   public void print() {
      Export.print(listTable);
   }
}
