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
 /*
 * ContactPanel.java
 *
 * Created on Nov 20, 2008, 8:17:28 AM
 */
package mpv5.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import mpv5.db.common.*;
import static mpv5.db.common.DatabaseObject.getObjects;
import mpv5.db.objects.Account;
import mpv5.db.objects.Expense;
import mpv5.globals.Messages;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.Group;
import mpv5.db.objects.Tax;
import mpv5.db.objects.Template;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.toolbars.DataPanelTB;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.Headers;
import mpv5.ui.misc.MPTable;
import mpv5.utils.export.Exportable;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.ui.misc.TableViewPersistenceHandler;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 *
 */
public class ExpensePanel extends javax.swing.JPanel implements DataPanel {

    private static final long serialVersionUID = 1L;
    private static ExpensePanel me;
    private ArrayList<DatabaseObject> accmod;
    private BigDecimal tax = BigDecimal.ZERO;
    private final java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();

    /**
     * Singleton
     *
     * @return
     */
    public static ExpensePanel instanceOf() {
        if (me == null) {
            me = new ExpensePanel();
        }
        me.refresh();
        return me;
    }
    private Expense dataOwner;
    private final DataPanelTB tb;
    private boolean filter;

    /**
     * Creates new form ContactPanel
     */
    public ExpensePanel() {
        initComponents();
        setName("expensepanel");
        tb = new mpv5.ui.toolbars.DataPanelTB(this);
        tb.disableButton(1);
        tb.disableButton(8);
        tb.disableButton(9);
        toolbarpane.add(tb, BorderLayout.CENTER);
        dataOwner = new Expense();

        addedby.setText(mpv5.db.objects.User.getCurrentUser().getName());

        groupnameselect.setSearchEnabled(true);
        groupnameselect.setContext(Context.getGroup());
        taxrate.setSearchEnabled(true);
        taxrate.setContext(Context.getTaxes());

        value.getTextField().addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                calculate();
            }

            @Override
            public void keyPressed(KeyEvent ek) {
                calculate();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                calculate();
            }
        });

        taxrate.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                getTaxRate();
            }
        });
        getTaxRate();

        try {
            DefaultListModel al = new DefaultListModel();
            ArrayList<DatabaseObject> accounts = DatabaseObject.getObjects(Context.getAccounts());
            for (int i = 0; i < accounts.size(); i++) {
                DatabaseObject databaseObject = accounts.get(i);
                al.addElement((Account) databaseObject);
            }
            jList1.setModel(al);
            jList1.setSelectionModel(new DefaultListSelectionModel() {
                private static final long serialVersionUID = -2473659851927728063L;

                @Override
                public void setSelectionInterval(int index0, int index1) {
                    if (jList1.isSelectedIndex(index0)) {
                        jList1.removeSelectionInterval(index0, index1);
                    } else {
                        jList1.addSelectionInterval(index0, index1);
                    }
                }
            });
        } catch (NodataFoundException nodataFoundException) {
            Log.Debug(this, nodataFoundException.getMessage());
        }

        taxrate.getComboBox().setEditable(false);
//        itemtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        itemtable.setFillsViewportHeight(true);
        ((MPTable) itemtable).setDefaultColumns(new Integer[]{100, 333, 100}, new Boolean[]{});
        ((MPTable) itemtable).setPersistanceHandler(new TableViewPersistenceHandler((MPTable) itemtable, this));
        dateFrame.setTime(new vTimeframe(DateConverter.getDate(DateConverter.getYear()), new Date()));
    }

    private void calculate() {
        try {
            netvalue.setText(FormatNumber.formatLokalCurrency(FormatNumber.parseDezimal(value.getText()).divide((tax.divide(Constants.BD100, 9, RoundingMode.HALF_UP)).add(BigDecimal.ONE), 9, RoundingMode.HALF_UP)));
        } catch (Exception e) {
            Log.Debug(e);
            netvalue.setText(FormatNumber.formatLokalCurrency(0d));
        }
    }

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean populate) {
        dataOwner = (Expense) object;
        if (populate) {
            dataOwner.setPanelData(this);

            this.exposeData();
            setTitle();

            tb.setFavourite(Favourite.isFavourite(object));
            tb.setEditable(!object.isReadOnly());

            formatTable();
            if (object.isReadOnly()) {
                Popup.notice(Messages.LOCKED_BY);
            }

            validate();
        }
    }
    Exportable preloadedExportFile;
    Template preloadedTemplate;

    private void setTitle() {
        if (this.getParent() instanceof JViewport || this.getParent() instanceof JTabbedPane) {
            JTabbedPane jTabbedPane = null;
            String title1 = cname_;
            //this->viewport->scrollpane->tabbedpane
            if (this.getParent().getParent().getParent() instanceof JTabbedPane) {
                jTabbedPane = (JTabbedPane) this.getParent().getParent().getParent();
            } else {
                try {
                    jTabbedPane = (JTabbedPane) this.getParent();
                } catch (Exception e) {
                    //Free floating window
                    ((JFrame) this.getRootPane().getParent()).setTitle(title1);
                }
            }
            if (jTabbedPane != null) {
                jTabbedPane.setTitleAt(jTabbedPane.getSelectedIndex(), title1);
            }
        }
    }

    @Override
    public void showRequiredFields() {
        TextFieldUtils.blinkerRed(value);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form. WARNING: Do NOT modify this
     * code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        rightpane = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        itemtable = new  mpv5.ui.misc.MPTable(this) {
            private static final long serialVersionUID = 1L;
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
        jPanel3 = new javax.swing.JPanel();
        search = new javax.swing.JTextField();
        dateFrame = new mpv5.ui.beans.TimeframeChooser();
        jButton1 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        value = new mpv5.ui.beans.LabeledTextField();
        netvalue = new mpv5.ui.beans.LabeledTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        notes = new javax.swing.JEditorPane();
        taxrate = new mpv5.ui.beans.LabeledCombobox();
        path = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        number = new mpv5.ui.beans.LabeledTextField();
        addedby = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        accountselect = new mpv5.ui.beans.LabeledCombobox();
        groupnameselect = new mpv5.ui.beans.MPCombobox();
        labeledDateChooser1 = new mpv5.ui.beans.LabeledDateChooser();
        jCheckBox1 = new javax.swing.JCheckBox();
        paydate = new mpv5.ui.beans.LabeledDateChooser();
        toolbarpane = new javax.swing.JPanel();

        setBackground(new java.awt.Color(176, 158, 158));
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ExpensePanel.border.title_1"))); // NOI18N
        setName("Form"); // NOI18N
        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        rightpane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        rightpane.setName("rightpane"); // NOI18N

        jPanel4.setBackground(new java.awt.Color(204, 204, 204));
        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setName("jPanel4"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N
        jScrollPane3.setPreferredSize(new java.awt.Dimension(453, 220));

        itemtable.setAutoCreateRowSorter(true);
        itemtable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        itemtable.setAutoscrolls(false);
        itemtable.setGridColor(new java.awt.Color(204, 204, 204));
        itemtable.setName("itemtable"); // NOI18N
        itemtable.setShowGrid(true);
        itemtable.setShowVerticalLines(false);
        itemtable.setSurrendersFocusOnKeystroke(true);
        itemtable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                itemtableMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(itemtable);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ExpensePanel.jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setPreferredSize(new java.awt.Dimension(300, 300));
        jPanel3.setRequestFocusEnabled(false);
        jPanel3.setVerifyInputWhenFocusTarget(false);

        search.setText(bundle.getString("ExpensePanel.search.text")); // NOI18N
        search.setName("search"); // NOI18N
        search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchActionPerformed(evt);
            }
        });
        search.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchKeyReleased(evt);
            }
        });

        dateFrame.setBackground(new java.awt.Color(255, 255, 255));
        dateFrame.setFont(dateFrame.getFont());
        dateFrame.setName("dateFrame"); // NOI18N
        dateFrame.setPreferredSize(new java.awt.Dimension(332, 24));

        jButton1.setFont(jButton1.getFont());
        jButton1.setText(bundle.getString("ExpensePanel.jButton1.text")); // NOI18N
        jButton1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jList1.setFont(jList1.getFont().deriveFont(jList1.getFont().getStyle() | java.awt.Font.BOLD));
        jList1.setName("jList1"); // NOI18N
        jList1.setSelectionBackground(new java.awt.Color(51, 51, 255));
        jScrollPane4.setViewportView(jList1);

        jButton2.setFont(jButton2.getFont());
        jButton2.setText(bundle.getString("ExpensePanel.jButton2.text")); // NOI18N
        jButton2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setFont(jButton3.getFont());
        jButton3.setText(bundle.getString("ExpensePanel.jButton3.text")); // NOI18N
        jButton3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setFont(jButton4.getFont());
        jButton4.setText(bundle.getString("ExpensePanel.jButton4.text")); // NOI18N
        jButton4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setFont(jButton5.getFont());
        jButton5.setText(bundle.getString("ExpensePanel.jButton5.text")); // NOI18N
        jButton5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton5.setName("jButton5"); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setFont(jButton6.getFont());
        jButton6.setText(bundle.getString("ExpensePanel.jButton6.text")); // NOI18N
        jButton6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton6.setName("jButton6"); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setFont(jButton7.getFont());
        jButton7.setText(bundle.getString("ExpensePanel.jButton7.text")); // NOI18N
        jButton7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton7.setName("jButton7"); // NOI18N
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setFont(jButton8.getFont());
        jButton8.setText(bundle.getString("ExpensePanel.jButton8.text")); // NOI18N
        jButton8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton8.setName("jButton8"); // NOI18N
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setFont(jButton9.getFont());
        jButton9.setText(bundle.getString("ExpensePanel.jButton9.text")); // NOI18N
        jButton9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton9.setName("jButton9"); // NOI18N
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dateFrame, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                    .addComponent(search, javax.swing.GroupLayout.Alignment.LEADING))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dateFrame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(search, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setName("jPanel2"); // NOI18N

        value.set_Label(bundle.getString("ExpensePanel.value._Label")); // NOI18N
        value.set_Text(bundle.getString("ExpensePanel.value._Text")); // NOI18N
        value.setName("value"); // NOI18N

        netvalue.set_Label(bundle.getString("ExpensePanel.netvalue._Label")); // NOI18N
        netvalue.set_Text(bundle.getString("ExpensePanel.netvalue._Text")); // NOI18N
        netvalue.setEnabled(false);
        netvalue.setName("netvalue"); // NOI18N

        jLabel1.setText(bundle.getString("ExpensePanel.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        notes.setBackground(new java.awt.Color(254, 254, 254));
        notes.setName("notes"); // NOI18N
        jScrollPane1.setViewportView(notes);

        taxrate.set_Label(bundle.getString("ExpensePanel.taxrate._Label")); // NOI18N
        taxrate.setName("taxrate"); // NOI18N
        taxrate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                taxrateMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                taxrateMouseExited(evt);
            }
        });

        path.setText(bundle.getString("ExpensePanel.path.text")); // NOI18N
        path.setName("path"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 713, Short.MAX_VALUE)
                    .addComponent(path, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(value, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(taxrate, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(netvalue, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(7, 7, 7))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(value, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(taxrate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(netvalue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(path, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel5.setName("jPanel5"); // NOI18N

        number.set_Label(bundle.getString("ExpensePanel.number._Label")); // NOI18N
        number.setEnabled(false);
        number.setFocusable(false);
        number.setFont(number.getFont());
        number.setName("number"); // NOI18N

        addedby.setFont(addedby.getFont());
        addedby.setText(bundle.getString("ExpensePanel.addedby.text")); // NOI18N
        addedby.setToolTipText(bundle.getString("ExpensePanel.addedby.toolTipText")); // NOI18N
        addedby.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        addedby.setEnabled(false);
        addedby.setName("addedby"); // NOI18N

        jLabel4.setFont(jLabel4.getFont());
        jLabel4.setText(bundle.getString("ExpensePanel.jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        accountselect.set_Label(bundle.getString("ExpensePanel.accountselect._Label")); // NOI18N
        accountselect.setName("accountselect"); // NOI18N
        accountselect.setSearchOnEnterEnabled(false);

        groupnameselect.setName("groupnameselect"); // NOI18N

        labeledDateChooser1.set_Label(bundle.getString("ExpensePanel.labeledDateChooser1._Label")); // NOI18N
        labeledDateChooser1.setName("labeledDateChooser1"); // NOI18N

        jCheckBox1.setText(bundle.getString("ExpensePanel.jCheckBox1.text")); // NOI18N
        jCheckBox1.setMargin(new java.awt.Insets(2, 0, 2, 2));
        jCheckBox1.setName("jCheckBox1"); // NOI18N
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox1ItemStateChanged(evt);
            }
        });

        paydate.set_Label(bundle.getString("ExpensePanel.paydate._Label")); // NOI18N
        paydate.setName("paydate"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(number, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(paydate, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                            .addComponent(labeledDateChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(accountselect, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addedby, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(groupnameselect, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(accountselect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addedby, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(7, 7, 7)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(groupnameselect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labeledDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(number, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(paydate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout rightpaneLayout = new javax.swing.GroupLayout(rightpane);
        rightpane.setLayout(rightpaneLayout);
        rightpaneLayout.setHorizontalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightpaneLayout.createSequentialGroup()
                .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(rightpaneLayout.createSequentialGroup()
                        .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 537, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        rightpaneLayout.setVerticalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightpaneLayout.createSequentialGroup()
                .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(rightpaneLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(rightpane, java.awt.BorderLayout.CENTER);

        toolbarpane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        toolbarpane.setName("toolbarpane"); // NOI18N
        toolbarpane.setLayout(new java.awt.BorderLayout());
        add(toolbarpane, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void itemtableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_itemtableMouseClicked
        try {
            setDataOwner((DatabaseObject) itemtable.getModel().getValueAt(itemtable.convertRowIndexToModel(itemtable.getSelectedRow()), 0), true);
        } catch (Exception e) {
            Log.Debug(this, e.getMessage());
        }
    }//GEN-LAST:event_itemtableMouseClicked

    private void jCheckBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged
        if (jCheckBox1.isSelected()) {
            paydate.setDate(new Date());
        }
    }//GEN-LAST:event_jCheckBox1ItemStateChanged

    private void taxrateMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_taxrateMouseExited
        calculate();
    }//GEN-LAST:event_taxrateMouseExited

    private void taxrateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_taxrateMouseClicked
        calculate();
    }//GEN-LAST:event_taxrateMouseClicked

    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
        filter(search.getText());
        filter = true;
    }//GEN-LAST:event_searchActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        filter(search.getText());
        filter = true;
    }//GEN-LAST:event_jButton1ActionPerformed

    private void searchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchKeyReleased
        filter(search.getText());
        filter = true;
    }//GEN-LAST:event_searchKeyReleased

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        dateFrame.setTime(new vTimeframe(DateConverter.getDate(DateConverter.getYear()), new Date()));
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        dateFrame.setTime(new vTimeframe(DateConverter.getStartOfYear(DateConverter.addYears(new Date(), -1)),
                DateConverter.getEndOfYear(DateConverter.addYears(new Date(), -1))));
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        dateFrame.setTime(new vTimeframe(new Date(0),
                DateConverter.getEndOfYear(new Date())));
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        dateFrame.setTime(new vTimeframe(new Date(), new Date()));
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        dateFrame.setTime(new vTimeframe(DateConverter.getStartOfWeek(new Date()),
                DateConverter.getEndOfWeek(new Date())));
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        dateFrame.setTime(new vTimeframe(DateConverter.getStartOfMonth(new Date()),
                DateConverter.getEndOfMonth(new Date())));
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        dateFrame.setTime(new vTimeframe(DateConverter.getStartOfQuarter(new Date()),
                DateConverter.getEndOfQuarter(new Date())));
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        this.filter = false;
        this.search.setText("");
        this.dateFrame.setTime(new vTimeframe(new Date(), new Date()));
        this.jList1.clearSelection();
        this.actionAfterSave();
    }//GEN-LAST:event_jButton9ActionPerformed
    MPTableModel omodel = null;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.beans.LabeledCombobox accountselect;
    private javax.swing.JLabel addedby;
    private javax.swing.ButtonGroup buttonGroup1;
    private mpv5.ui.beans.TimeframeChooser dateFrame;
    private mpv5.ui.beans.MPCombobox groupnameselect;
    private javax.swing.JTable itemtable;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private mpv5.ui.beans.LabeledDateChooser labeledDateChooser1;
    private mpv5.ui.beans.LabeledTextField netvalue;
    private javax.swing.JEditorPane notes;
    private mpv5.ui.beans.LabeledTextField number;
    private javax.swing.JLabel path;
    private mpv5.ui.beans.LabeledDateChooser paydate;
    private javax.swing.JPanel rightpane;
    private javax.swing.JTextField search;
    private mpv5.ui.beans.LabeledCombobox taxrate;
    private javax.swing.JPanel toolbarpane;
    private mpv5.ui.beans.LabeledTextField value;
    // End of variables declaration//GEN-END:variables
    public String cname_;
    public String description_;
    public String cnumber_;
    public int intaddedby_;
    public int ids_;
    public Date dateadded_;
    public Group group_;
    public BigDecimal netvalue_;
    public BigDecimal taxpercentvalue_;
    public BigDecimal brutvalue_;
    public int accountsids_;
    public boolean ispaid_;
    public Date dateend_;

    @Override
    public synchronized boolean collectData() {
        calculate();

        try {
            accountsids_ = Integer.valueOf(accountselect.getSelectedItem().getId());
            mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty(me, "accountselect", accountsids_);
        } catch (NumberFormatException e) {
            accountsids_ = 1;
        }

        if (groupnameselect.getSelectedItem() != null) {
            try {
                int group = Integer.valueOf(groupnameselect.getSelectedItem().getId());
                group_ = (Group) DatabaseObject.getObject(Context.getGroup(), group);
                Log.Debug(this, groupnameselect.getSelectedItem().getId());
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex);
            }
        }

        dateadded_ = labeledDateChooser1.getDate();
        dateend_ = paydate.getDate();
        ispaid_ = jCheckBox1.isSelected();

        intaddedby_ = User.getUserId(addedby.getText());
        description_ = notes.getText();
        try {
            netvalue_ = FormatNumber.parseDezimal(netvalue.getText());
        } catch (Exception e) {
            netvalue_ = BigDecimal.ZERO;
        }
        try {
            brutvalue_ = FormatNumber.parseDezimal(value.getText());
        } catch (Exception e) {
            brutvalue_ = BigDecimal.ZERO;
        }
        try {
            taxpercentvalue_ = Tax.getTaxValue(Integer.valueOf(taxrate.getSelectedItem().getId()));
        } catch (NumberFormatException numberFormatException) {
            taxpercentvalue_ = BigDecimal.ZERO;
        }
        return true;
    }

    @Override
    public void exposeData() {
        number.setText(cname_);
        notes.setText(description_);
        value.setText(FormatNumber.formatDezimal(brutvalue_));
        netvalue.setText(FormatNumber.formatDezimal(netvalue_));
        try {
            Account a = (Account) DatabaseObject.getObject(Context.getAccounts(), accountsids_);
            accountselect.setSelectedItem(a.__getIDS());
            path.setText(a.__getHierarchypath());
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        try {
            groupnameselect.setModel(group_);
        } catch (Exception ex) {
            Log.Debug(this, ex.getMessage());
        }
        addedby.setText(User.getUsername(intaddedby_));
        labeledDateChooser1.setDate(dateadded_);
        taxrate.setSelectedItem(Tax.getTaxId(taxpercentvalue_));
        paydate.setDate(dateend_);
        jCheckBox1.setSelected(ispaid_);
        getTaxRate();
    }

    @Override
    public void refresh() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {

                groupnameselect.triggerSearch();
                taxrate.triggerSearch();
                try {
                    MPTableModel x = new MPTableModel(Expense.getExpenses(), Headers.EXPENSE);
                    x.setTypes(Expense.class, String.class, Account.class, String.class, String.class, Boolean.class, Date.class, Date.class);
                    itemtable.setModel(x);
                } catch (NodataFoundException e) {
                    Log.Debug(this, e.getMessage());
                }
                try {
                    accmod = DatabaseObject.getObjects(Context.getAccounts(), new QueryCriteria("intaccounttype", Account.EXPENSE));
                    accountselect.setModel(accmod);
                } catch (NodataFoundException e) {
                    Log.Debug(this, e.getMessage());
                }
                try {
                    accountselect.setSelectedItem(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(me, "accountselect", 1));
                    formatTable();
                } catch (Exception e) {
                    Log.Debug(this, e.getMessage());
                }
                getTaxRate();
            }
        };

        SwingUtilities.invokeLater(runnable);
    }

    /**
     *
     */
    public void formatTable() {
//        TableFormat.resizeCols(itemtable, new Integer[]{100, 333, 100}, false);
    }

    @Override
    public void paste(DatabaseObject... dbo) {
        mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE.toString(), Color.RED);
    }

    @Override
    public void showSearchBar(boolean show) {
    }

    @Override
    public void actionAfterSave() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    List<? extends RowSorter.SortKey> sk = itemtable.getRowSorter().getSortKeys();
                    itemtable.setModel(new MPTableModel(Expense.getExpenses(), Headers.EXPENSE));
                    itemtable.getRowSorter().setSortKeys(sk);
                    if (filter) {
                        filter(search.getText());
                    }
                    formatTable();

                } catch (NodataFoundException ex) {
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    @Override
    public void actionAfterCreate() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {

                    itemtable.setModel(new MPTableModel(Expense.getExpenses(), Headers.EXPENSE));
                    formatTable();

                } catch (NodataFoundException ex) {
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    @Override
    public void actionBeforeCreate() {
    }

    @Override
    public void actionBeforeSave() {
    }

    @Override
    public void mail() {
    }

    @Override
    public void print() {
        mpv5.utils.export.Export.print(this);
    }

    private void getTaxRate() {
        try {
            MPComboBoxModelItem t = taxrate.getValue();
            if (t == null) {
                tax = BigDecimal.ZERO;
                taxrate.setSelectedIndex(-1);
            } else {
                tax = Tax.getTaxValue(Integer.valueOf(t.getId()));
            }
            Log.Debug(this, "Selected Taxrate: " + tax);
        } catch (NumberFormatException ex) {
            Log.Debug(this, "Reading Taxrate failed: Assuming Zero");
            tax = BigDecimal.ZERO;
        }
        calculate();
    }
//    class calc extends Thread {
//
//        @Override
//        public void run() {
//            while (true) {
//                while (isShowing()) {
//                    BigDecimal tax = BigDecimal.ZERO;
//                    try {
//                        MPComboBoxModelItem t = taxrate.getValue();
//                        tax = Tax.getTaxValue(Integer.valueOf(t.getId()));
//                    } catch (Exception e) {
//                        try {
//                            tax = new BigDecimal(taxrate.getText());
//                        } catch (NumberFormatException numberFormatException) {
//                            tax = BigDecimal.ZERO;
//                        }
//                    }
//
//                    try {
//                        netvalue.setText(FormatNumber.formatLokalCurrency(FormatNumber.parseDezimal(value.getText()).divide((tax.divide(Constants.BD100, 9, BigDecimal.ROUND_HALF_UP)).add(BigDecimal.ONE))));
//                    } catch (Exception e) {
//                        netvalue.setText(FormatNumber.formatLokalCurrency(0d));
//                    }
//                    try {
//                        sleep(333);
//                    } catch (InterruptedException ex) {
//                    }
//                }
//            }
//        }
//    }

    private void filter(String text) {
        QueryCriteria2 itemsParams = new QueryCriteria2();
        Object[][] obj = new Object[0][0];
        ArrayList<QueryParameter> l1 = new ArrayList<QueryParameter>();
        for (int i = 0; i < jList1.getSelectedValues().length; i++) {
            l1.add(new QueryParameter(Context.getExpense(), "accountsids", ((Account) jList1.getSelectedValues()[i]).__getIDS(), QueryParameter.EQUALS)); //NOI18N
        }
        if (l1.size() > 1) {
            itemsParams.or(l1);
        } else {
            itemsParams.and(l1.toArray(new QueryParameter[0]));
        }
        if (!text.isEmpty()) {
            itemsParams.and(new QueryParameter(Context.getExpense(), "description", text, QueryParameter.LIKE));
        }

        itemsParams.and(new QueryParameter(Context.getExpense(), "dateend", DateConverter.getSQLDateString(dateFrame.getTime().getStart()), QueryParameter.GREATEREQUALS));
        itemsParams.and(new QueryParameter(Context.getExpense(), "dateend", DateConverter.getSQLDateString(dateFrame.getTime().getEnd()), QueryParameter.LOWEREQUALS));

        try {
            ArrayList<DatabaseObject> data = getObjects(Context.getExpense(), itemsParams, true);
            obj = new Object[data.size()][];
            for (int i = 0; i < data.size(); i++) {
                Expense e = (Expense) data.get(i);
                obj[i] = e.toArray();
            }
        } catch (NodataFoundException e) {
            Log.Debug(this, e.getMessage());
        }

        List<? extends RowSorter.SortKey> sk = itemtable.getRowSorter().getSortKeys();
        MPTableModel x = new MPTableModel(obj, Headers.EXPENSE);
        x.setTypes(Expense.class, String.class, Account.class, String.class, String.class, Boolean.class, Date.class, Date.class);
        itemtable.setModel(x);
        itemtable.getRowSorter().setSortKeys(sk);
    }
}
