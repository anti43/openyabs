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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import mpv5.db.common.Context;
import mpv5.db.objects.Product;
import mpv5.db.objects.SubItem;
import mpv5.globals.Messages;
import mpv5.ui.beans.DateChooser;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.panels.ItemPositionsTable;

/**
 * Step 3 of the Beleg-Wizard: enter document title/date and add positions using
 * {@link ItemPositionsTable} with live EN 16931 totals.
 */
public class BelegItemsStep extends JPanel {

    private static final long serialVersionUID = 1L;

    private final BelegWizardState state;
    private final JTextField titleField = new JTextField();
    private final DateChooser docDateChooser = new DateChooser();
    private final ItemPositionsTable positionsTable = new ItemPositionsTable();
    private BigDecimal appliedTaxRate;

    public BelegItemsStep(BelegWizardState state) {
        super(new BorderLayout(0, ModernTheme.px(10)));
        this.state = state;
        setOpaque(false);
        setBorder(ModernTheme.padding(8, 12));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.PAGE_AXIS));
        JLabel heading = new JLabel(Messages.BELEG_WIZARD_ITEMS_HEADING.toString());
        heading.setFont(ModernTheme.font(6, Font.BOLD));
        heading.setForeground(ModernTheme.foreground());
        JLabel subtitle = new JLabel(Messages.BELEG_WIZARD_ITEMS_SUBTITLE.toString());
        subtitle.setFont(ModernTheme.font(0, Font.PLAIN));
        subtitle.setForeground(ModernTheme.muted());
        header.add(heading);
        header.add(Box.createVerticalStrut(ModernTheme.px(4)));
        header.add(subtitle);

        // Compact metadata strip (Title / Subject + Document date)
        JPanel meta = new JPanel(new GridBagLayout());
        meta.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.insets = new Insets(ModernTheme.px(6), 0, 0, ModernTheme.px(10));
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        meta.add(ModernTheme.fieldLabel(Messages.CSV_F_DESCRIPTION.toString()), g);

        titleField.setName("belegTitle");
        g.gridx = 1;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        meta.add(titleField, g);

        g.gridx = 2;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        meta.add(ModernTheme.fieldLabel(Messages.ITEM_DATE_DOCUMENT.toString()), g);

        docDateChooser.setDate(state.getDocDate());
        docDateChooser.setPreferredSize(new Dimension(ModernTheme.px(140), docDateChooser.getPreferredSize().height));
        g.gridx = 3;
        g.insets = new Insets(ModernTheme.px(6), 0, 0, 0);
        meta.add(docDateChooser, g);

        JPanel north = new JPanel(new BorderLayout(0, ModernTheme.px(4)));
        north.setOpaque(false);
        north.add(header, BorderLayout.NORTH);
        north.add(meta, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        JButton addProduct = new JButton(Messages.BELEG_WIZARD_ITEMS_ADD_PRODUCT.toString());
        addProduct.setName("belegAddProduct");
        addProduct.addActionListener(e -> addProductFromCatalog());
        positionsTable.addAction(addProduct);
        add(positionsTable, BorderLayout.CENTER);
    }

    private void addProductFromCatalog() {
        Product product = (Product) Popup.SelectValue(Context.getProduct());
        if (product != null) {
            SubItem sub = new SubItem(product, state.getCustomerTaxRate());
            addPosition(sub);
        }
    }

    /**
     * Inserts a position into the first free row (or appends a new row) and
     * recalculates totals.
     */
    public void addPosition(SubItem sub) {
        if (sub == null) {
            return;
        }
        positionsTable.stopEditing();
        BigDecimal rate = state.getCustomerTaxRate();
        if (rate != null) {
            sub.setTaxpercentvalue(rate);
            SubItem.calculate(sub);
        }
        mpv5.utils.models.MPTableModel model = positionsTable.getModel();
        int targetRow = -1;
        for (int r = 0; r < model.getRowCount(); r++) {
            Object desc = model.getValueAt(r, 4);
            if (desc == null || desc.toString().trim().isEmpty()) {
                targetRow = r;
                break;
            }
        }
        if (targetRow >= 0) {
            model.setRowAt(sub.getRowData(targetRow + 1), targetRow, 1);
            if (model.getEmptyRows(new int[]{4}) < 1) {
                model.addRow(1);
            }
        } else {
            positionsTable.addPosition(sub);
        }
        positionsTable.recalculate();
    }

    /**
     * Synchronizes step controls from {@link BelegWizardState} when entering
     * this step (e.g. applying a newly selected customer's tax rate).
     */
    public void onEnter() {
        titleField.setText(state.getTitle());
        docDateChooser.setDate(state.getDocDate());
        BigDecimal rate = state.getCustomerTaxRate();
        positionsTable.setCustomerTaxRate(rate);
        if (rate != null && (appliedTaxRate == null || rate.compareTo(appliedTaxRate) != 0)) {
            positionsTable.applyCustomerTaxRate(rate);
            // Also preset empty rows so typing a new position inherits customer's tax rate
            mpv5.utils.models.MPTableModel model = positionsTable.getModel();
            for (int r = 0; r < model.getRowCount(); r++) {
                model.setValueAt(rate, r, 6);
            }
            appliedTaxRate = rate;
        } else if (rate == null) {
            appliedTaxRate = null;
        }
        positionsTable.recalculate();
    }

    /**
     * Commits active cell edits and writes metadata fields back into
     * {@link BelegWizardState}.
     */
    public void commitToState() {
        positionsTable.stopEditing();
        positionsTable.recalculate();
        state.setTitle(titleField.getText());
        if (docDateChooser.getDate() != null) {
            state.setDocDate(docDateChooser.getDate());
        }
    }

    public boolean hasPositions() {
        positionsTable.stopEditing();
        List<SubItem> list = BelegWizardService.extractPositions(positionsTable.getModel());
        return !list.isEmpty();
    }

    public ItemPositionsTable getPositionsTable() {
        return positionsTable;
    }

    public JTextField getTitleField() {
        return titleField;
    }
}
