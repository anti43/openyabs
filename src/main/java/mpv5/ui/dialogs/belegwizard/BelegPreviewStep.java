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
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Account;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.beans.DateChooser;
import mpv5.ui.dialogs.PdfPreview;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.panels.ItemPositionsTable;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.numberformat.FormatNumber;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Step 4 of the Beleg-Wizard: review summary, adjust optional metadata
 * (group, account, dates, notes), and inspect a live rendered PDF preview (with
 * structured HTML fallback if no template or LibreOffice is available).
 */
public class BelegPreviewStep extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final String CARD_PDF = "pdf";
    private static final String CARD_HTML = "html";

    private final BelegWizardState state;
    private final ItemPositionsTable positionsTable;

    private final JLabel summaryHeader = new JLabel();
    private final JLabel totalsLabel = new JLabel();
    private final JComboBox<MPComboBoxModelItem> groupCombo = new JComboBox<MPComboBoxModelItem>();
    private final JComboBox<MPComboBoxModelItem> accountCombo = new JComboBox<MPComboBoxModelItem>();
    private final DateChooser docDateChooser = new DateChooser();
    private final DateChooser serviceDateChooser = new DateChooser();
    private final DateChooser dueDateChooser = new DateChooser();
    private final JTextArea notesArea = new JTextArea(4, 24);

    private final CardLayout previewCards = new CardLayout();
    private final JPanel previewStack = new JPanel(previewCards);
    private final JLabel pdfImageLabel = new JLabel("", SwingConstants.CENTER);
    private final JEditorPane htmlPreviewPane = new JEditorPane("text/html", "");
    private final JLabel previewCaption = new JLabel(" ");
    private final JButton largerButton = new JButton(Messages.TEMPLATES_PREVIEW_LARGER.toString());

    private boolean populating;
    private int previewRun;
    private File currentPreviewPdf;

    public BelegPreviewStep(BelegWizardState state, ItemPositionsTable positionsTable) {
        super(new BorderLayout(0, ModernTheme.px(10)));
        this.state = state;
        this.positionsTable = positionsTable;
        setOpaque(false);
        setBorder(ModernTheme.padding(8, 12));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.PAGE_AXIS));
        JLabel heading = new JLabel(Messages.BELEG_WIZARD_PREVIEW_HEADING.toString());
        heading.setFont(ModernTheme.font(6, Font.BOLD));
        heading.setForeground(ModernTheme.foreground());
        JLabel subtitle = new JLabel(Messages.BELEG_WIZARD_PREVIEW_SUBTITLE.toString());
        subtitle.setFont(ModernTheme.font(0, Font.PLAIN));
        subtitle.setForeground(ModernTheme.muted());
        header.add(heading);
        header.add(Box.createVerticalStrut(ModernTheme.px(4)));
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        // Left: summary + editable metadata
        JPanel leftBody = buildLeftPanel();
        JScrollPane leftScroll = new JScrollPane(leftBody);
        leftScroll.setBorder(BorderFactory.createEmptyBorder());
        leftScroll.setOpaque(false);
        leftScroll.getViewport().setOpaque(false);

        // Right: PDF / HTML preview
        JPanel rightCard = buildRightPreviewPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightCard);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setOpaque(false);
        split.setResizeWeight(0.42);
        split.setDividerLocation(ModernTheme.px(350));
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildLeftPanel() {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.PAGE_AXIS));

        summaryHeader.setVerticalAlignment(JLabel.TOP);
        totalsLabel.setVerticalAlignment(JLabel.TOP);

        JPanel sumContent = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        sumContent.setOpaque(false);
        sumContent.add(summaryHeader, BorderLayout.NORTH);
        sumContent.add(totalsLabel, BorderLayout.SOUTH);
        Card summaryCard = new Card(Messages.BELEG_WIZARD_SUMMARY.toString()).addFull(sumContent);
        summaryCard.setAlignmentX(LEFT_ALIGNMENT);
        col.add(summaryCard);
        col.add(Box.createVerticalStrut(ModernTheme.px(10)));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(ModernTheme.px(4), 0, ModernTheme.px(4), ModernTheme.px(8));
        g.anchor = GridBagConstraints.WEST;
        int row = 0;

        addRow(form, g, row++, Messages.HISTORY_GROUP.toString(), groupCombo);
        addRow(form, g, row++, Messages.USERS_ACCOUNT.toString(), accountCombo);
        addRow(form, g, row++, Messages.ITEM_DATE_DOCUMENT.toString(), docDateChooser);
        addRow(form, g, row++, Messages.ITEM_DATE_SERVICE.toString(), serviceDateChooser);
        addRow(form, g, row++, BelegWizardService.bundleText("ItemPanel2.date3._Label", "Due"), dueDateChooser);

        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(ModernTheme.font(0, Font.PLAIN));
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.NORTHWEST;
        form.add(ModernTheme.fieldLabel(Messages.CSV_F_NOTES.toString()), g);
        g.gridx = 1;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.BOTH;
        form.add(new JScrollPane(notesArea), g);

        groupCombo.addActionListener(e -> {
            if (!populating) {
                commitToState();
                refreshPreview();
            }
        });
        accountCombo.addActionListener(e -> {
            if (!populating) {
                commitToState();
            }
        });

        Card detailsCard = new Card(Messages.ITEM_SECTION_DOCUMENT.toString()).addFull(form);
        detailsCard.setAlignmentX(LEFT_ALIGNMENT);
        col.add(detailsCard);
        return col;
    }

    private static void addRow(JPanel form, GridBagConstraints g, int row, String label, javax.swing.JComponent comp) {
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        form.add(ModernTheme.fieldLabel(label), g);
        g.gridx = 1;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        form.add(comp, g);
    }

    private JPanel buildRightPreviewPanel() {
        JButton refreshBtn = new JButton(Messages.BELEG_WIZARD_PREVIEW_REFRESH.toString());
        refreshBtn.addActionListener(e -> {
            commitToState();
            refreshPreview();
        });
        largerButton.setEnabled(false);
        largerButton.addActionListener(e -> {
            if (currentPreviewPdf != null && currentPreviewPdf.exists()) {
                PdfPreview.show(currentPreviewPdf);
            }
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0));
        toolbar.setOpaque(false);
        toolbar.add(refreshBtn);
        toolbar.add(largerButton);

        pdfImageLabel.setOpaque(true);
        pdfImageLabel.setBackground(ModernTheme.cardBackground());
        JScrollPane pdfScroll = new JScrollPane(pdfImageLabel);
        pdfScroll.getViewport().setBackground(ModernTheme.background());

        htmlPreviewPane.setEditable(false);
        htmlPreviewPane.setName("belegHtmlPreview");
        JScrollPane htmlScroll = new JScrollPane(htmlPreviewPane);

        previewStack.setOpaque(false);
        previewStack.add(pdfScroll, CARD_PDF);
        previewStack.add(htmlScroll, CARD_HTML);

        previewCaption.setFont(ModernTheme.font(-1, Font.PLAIN));
        previewCaption.setForeground(ModernTheme.muted());

        JPanel content = new JPanel(new BorderLayout(0, ModernTheme.px(6)));
        content.setOpaque(false);
        content.add(toolbar, BorderLayout.NORTH);
        content.add(previewStack, BorderLayout.CENTER);
        content.add(previewCaption, BorderLayout.SOUTH);

        Card card = new Card(Messages.TEMPLATES_PREVIEW.toString()).addFull(content, 1);
        card.setPreferredSize(new Dimension(ModernTheme.px(440), ModernTheme.px(420)));
        return card;
    }

    /**
     * Populates the summary, metadata combos, and kicks off the preview render
     * when Step 4 becomes active.
     */
    public void onEnter() {
        populating = true;
        try {
            loadGroups();
            loadAccounts();
            selectById(groupCombo, state.getGroupId());
            selectById(accountCombo, state.getAccountId());
            docDateChooser.setDate(state.getDocDate());
            serviceDateChooser.setDate(state.getServiceDate());
            dueDateChooser.setDate(state.getDueDate());
            notesArea.setText(state.getNotes());
            updateSummaryLabels();
        } finally {
            populating = false;
        }
        refreshPreview();
    }

    /**
     * Writes any edited metadata on Step 4 back to {@link BelegWizardState}.
     */
    public void commitToState() {
        MPComboBoxModelItem gItem = (MPComboBoxModelItem) groupCombo.getSelectedItem();
        if (gItem != null && gItem.isValid()) {
            try {
                state.setGroupId(Integer.parseInt(String.valueOf(gItem.getId())));
            } catch (NumberFormatException ignored) {
            }
        }
        MPComboBoxModelItem aItem = (MPComboBoxModelItem) accountCombo.getSelectedItem();
        if (aItem != null && aItem.isValid()) {
            try {
                state.setAccountId(Integer.parseInt(String.valueOf(aItem.getId())));
            } catch (NumberFormatException ignored) {
            }
        }
        if (docDateChooser.getDate() != null) {
            state.setDocDate(docDateChooser.getDate());
        }
        state.setServiceDate(serviceDateChooser.getDate());
        state.setDueDate(dueDateChooser.getDate());
        state.setNotes(notesArea.getText());
    }

    private void loadGroups() {
        DefaultComboBoxModel<MPComboBoxModelItem> m = new DefaultComboBoxModel<MPComboBoxModelItem>();
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getGroup())) {
                Group grp = (Group) o;
                m.addElement(new MPComboBoxModelItem(grp.__getIDS(), grp.__getCname()));
            }
        } catch (Exception ex) {
            Log.Debug(this, ex);
        }
        groupCombo.setModel(m);
    }

    private void loadAccounts() {
        DefaultComboBoxModel<MPComboBoxModelItem> m = new DefaultComboBoxModel<MPComboBoxModelItem>();
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getAccounts())) {
                Account acc = (Account) o;
                m.addElement(new MPComboBoxModelItem(acc.__getIDS(), acc.__getCname()));
            }
        } catch (Exception ex) {
            Log.Debug(this, ex);
        }
        accountCombo.setModel(m);
    }

    private static void selectById(JComboBox<MPComboBoxModelItem> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            MPComboBoxModelItem item = combo.getItemAt(i);
            if (item != null && String.valueOf(id).equals(String.valueOf(item.getId()))) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void updateSummaryLabels() {
        positionsTable.stopEditing();
        positionsTable.recalculate();
        List<SubItem> subs = BelegWizardService.extractPositions(positionsTable.getModel());
        BigDecimal[] totals = positionsTable.getTotals();
        BigDecimal net = totals[0] != null ? totals[0] : BigDecimal.ZERO;
        BigDecimal tax = totals[1] != null ? totals[1] : BigDecimal.ZERO;
        BigDecimal disc = totals[2] != null ? totals[2] : BigDecimal.ZERO;
        BigDecimal gross = net.subtract(disc).add(tax);

        Contact c = state.getReceiver();
        String receiverText = "";
        if (c != null) {
            String company = c.__getCompany() != null ? c.__getCompany().trim() : "";
            String person = ((c.__getPrename() != null ? c.__getPrename().trim() + " " : "")
                    + (c.__getCname() != null ? c.__getCname().trim() : "")).trim();
            receiverText = !company.isEmpty() ? company : person;
            if (!company.isEmpty() && !person.isEmpty() && !company.equals(person)) {
                receiverText += " (" + person + ")";
            }
        }

        StringBuilder hdr = new StringBuilder("<html>");
        hdr.append("<div style='font-size:12pt;font-weight:bold;'>")
           .append(esc(Item.getTypeString(state.getItemType())))
           .append("</div>");
        if (!state.getTitle().trim().isEmpty()) {
            hdr.append("<div style='color:#475569;'>").append(esc(state.getTitle().trim())).append("</div>");
        }
        hdr.append("<div style='margin-top:6px;'><b>").append(esc(receiverText)).append("</b></div>");
        hdr.append("<div style='color:#64748b;margin-top:2px;'>")
           .append(esc(String.format(Messages.BELEG_WIZARD_POSITIONS_COUNT.toString(), subs.size())))
           .append("</div></html>");
        summaryHeader.setText(hdr.toString());

        StringBuilder tot = new StringBuilder("<html><table cellspacing='2' cellpadding='0' style='width:100%;'>");
        tot.append("<tr><td style='color:#64748b;'>").append(esc(BelegWizardService.bundleText("ItemPanel2.jLabel1.text", "Net")))
           .append("</td><td align='right'>").append(esc(FormatNumber.formatLokalCurrency(net))).append("</td></tr>");
        if (disc.signum() != 0) {
            tot.append("<tr><td style='color:#64748b;'>").append(esc(BelegWizardService.bundleText("ItemPanel2.jLabel5.text_1", "Discount")))
               .append("</td><td align='right'>-").append(esc(FormatNumber.formatLokalCurrency(disc))).append("</td></tr>");
        }
        tot.append("<tr><td style='color:#64748b;'>").append(esc(BelegWizardService.bundleText("ItemPanel2.jLabel2.text", "Tax")))
           .append("</td><td align='right'>").append(esc(FormatNumber.formatLokalCurrency(tax))).append("</td></tr>");
        tot.append("<tr><td style='font-weight:bold;padding-top:4px;'>").append(esc(BelegWizardService.bundleText("ItemPanel2.jLabel3.text", "Total")))
           .append("</td><td align='right' style='font-weight:bold;color:#1d4ed8;padding-top:4px;'>")
           .append(esc(FormatNumber.formatLokalCurrency(gross))).append("</td></tr></table></html>");
        totalsLabel.setText(tot.toString());
    }

    /**
     * Generates the PDF preview asynchronously; if no template is configured
     * or LibreOffice cannot render it, shows the structured HTML preview.
     */
    public void refreshPreview() {
        final int run = ++previewRun;
        currentPreviewPdf = null;
        largerButton.setEnabled(false);
        positionsTable.stopEditing();
        positionsTable.recalculate();
        final List<SubItem> subs = BelegWizardService.extractPositions(positionsTable.getModel());
        final BigDecimal[] totals = positionsTable.getTotals();

        if (BelegWizardService.findTemplate(state) == null) {
            showHtmlFallback(subs, totals, Messages.BELEG_WIZARD_PREVIEW_NO_TEMPLATE.toString());
            return;
        }

        previewCards.show(previewStack, CARD_PDF);
        pdfImageLabel.setIcon(null);
        pdfImageLabel.setText(Messages.TEMPLATES_PREVIEW_LOADING.toString());
        previewCaption.setText(" ");

        final int targetWidth = Math.max(ModernTheme.px(320), Math.min(ModernTheme.px(560),
                previewStack.getWidth() > 0 ? previewStack.getWidth() - ModernTheme.px(24) : ModernTheme.px(420)));

        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                File pdf = BelegWizardService.renderPreviewPdf(state, subs, totals);
                if (pdf == null) {
                    return null;
                }
                try (PDDocument doc = Loader.loadPDF(pdf)) {
                    float pageWidth = doc.getPage(0).getMediaBox().getWidth();
                    BufferedImage img = new PDFRenderer(doc).renderImage(0, targetWidth / pageWidth);
                    return new Object[]{pdf, img, doc.getNumberOfPages()};
                }
            }

            @Override
            protected void done() {
                if (run != previewRun) {
                    return;
                }
                try {
                    Object[] res = get();
                    if (res == null) {
                        showHtmlFallback(subs, totals, Messages.BELEG_WIZARD_PREVIEW_NO_TEMPLATE.toString());
                        return;
                    }
                    currentPreviewPdf = (File) res[0];
                    BufferedImage img = (BufferedImage) res[1];
                    pdfImageLabel.setText(null);
                    pdfImageLabel.setIcon(new ImageIcon(img));
                    largerButton.setEnabled(true);
                    previewCaption.setText(String.format(Messages.TEMPLATES_PREVIEW_CAPTION.toString(),
                            res[2], Item.getTypeString(state.getItemType())));
                    previewCards.show(previewStack, CARD_PDF);
                } catch (Exception ex) {
                    Log.Debug(BelegPreviewStep.this, ex);
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String notice = Messages.TEMPLATES_PREVIEW_FAILED + " " + String.valueOf(cause.getMessage());
                    showHtmlFallback(subs, totals, notice);
                }
            }
        }.execute();
    }

    private void showHtmlFallback(List<SubItem> subs, BigDecimal[] totals, String notice) {
        htmlPreviewPane.setText(BelegWizardService.buildHtmlPreview(state, subs, totals, notice));
        htmlPreviewPane.setCaretPosition(0);
        previewCaption.setText(Messages.BELEG_WIZARD_SUMMARY.toString());
        previewCards.show(previewStack, CARD_HTML);
    }

    public String getHtmlPreviewText() {
        return htmlPreviewPane.getText();
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
