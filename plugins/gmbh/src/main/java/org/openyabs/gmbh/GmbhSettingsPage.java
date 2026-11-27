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
package org.openyabs.gmbh;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import mpv5.plugin.api.ExtensionContext;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.ui.modern.SetupPage;

/**
 * Settings › Plugins › GmbH: the mandatory details, the checks, the demo
 * template
 */
final class GmbhSettingsPage {

    static final String[] LEGAL_FORMS = {"GmbH", "UG (haftungsbeschränkt)", "gGmbH", "GmbH & Co. KG",
        "UG (haftungsbeschränkt) & Co. KG"};

    private GmbhSettingsPage() {
    }

    static JComponent create(final ExtensionContext yabs, final GmbhPlugin plugin) {
        GmbhData d = GmbhData.load();
        final JComboBox<String> legalForm = new JComboBox<String>(LEGAL_FORMS);
        legalForm.setEditable(true);
        legalForm.setSelectedItem(d.legalForm);
        final JTextField seat = new JTextField(d.seat, 30);
        final JTextField court = new JTextField(d.registerCourt, 30);
        final JTextField number = new JTextField(d.registerNumber, 30);
        final JTextArea directors = new JTextArea(d.managingDirectors, 3, 30);
        directors.setLineWrap(true);
        final JTextField board = new JTextField(d.supervisoryChair, 30);
        final JTextField capital = new JTextField(d.shareCapital, 30);
        final JCheckBox warnMail = new JCheckBox(yabs.tr("settings.warnMail"), plugin.warnOnMail());
        final JCheckBox warnTemplate = new JCheckBox(yabs.tr("settings.warnTemplate"), plugin.warnOnTemplate());

        final JLabel preview = new JLabel(" ");
        Runnable updatePreview = () -> {
            GmbhData p = read(legalForm, seat, court, number, directors, board, capital);
            preview.setText("<html><b>" + yabs.tr("settings.preview") + "</b> " + esc(plugin.legalLine(p)) + "</html>");
        };
        updatePreview.run();

        SetupPage page = new SetupPage(yabs.tr("setup.title"), yabs.tr("settings.intro"));
        String quick = yabs.tr("setup.card.quick");
        JButton template = new PrimaryButton(yabs.tr("setup.btn.template"), null);
        template.addActionListener(e -> plugin.importTemplate());
        page.action(quick, template, yabs.tr("cmd.template.title"), yabs.tr("cmd.template.desc"));
        JButton check = new JButton(yabs.tr("setup.btn.check"));
        check.addActionListener(e -> plugin.checkCurrent());
        page.action(quick, check, yabs.tr("cmd.check.title"), yabs.tr("cmd.check.desc"));

        JPanel previewBox = new JPanel(new java.awt.BorderLayout());
        previewBox.setBackground(ModernTheme.accentSoft());
        previewBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.border()), ModernTheme.padding(8, 10)));
        previewBox.add(preview, java.awt.BorderLayout.CENTER);
        page.card(new Card(yabs.tr("setup.card.details"))
                .row(Card.field(yabs.tr("field." + GmbhData.K_LEGALFORM), ModernTheme.roomyCombo(legalForm), 1),
                        Card.field(yabs.tr("field." + GmbhData.K_SEAT), ModernTheme.roomy(seat), 1))
                .row(Card.field(yabs.tr("field." + GmbhData.K_COURT), ModernTheme.roomy(court), 1),
                        Card.field(yabs.tr("field." + GmbhData.K_NUMBER), ModernTheme.roomy(number), 1))
                .row(Card.field(yabs.tr("field." + GmbhData.K_DIRECTORS), new JScrollPane(directors)))
                .row(Card.field(yabs.tr("field." + GmbhData.K_BOARD), ModernTheme.roomy(board), 1),
                        Card.field(yabs.tr("field." + GmbhData.K_CAPITAL), ModernTheme.roomy(capital), 1))
                .section(yabs.tr("settings.preview"))
                .addFull(previewBox));
        page.card(new Card(yabs.tr("setup.card.checks"))
                .addFull(SetupPage.plain(warnMail))
                .addFull(SetupPage.plain(warnTemplate)));

        PrimaryButton save = SetupPage.saveButton(yabs.tr("settings.save"));
        save.addActionListener(e -> {
            GmbhData p = read(legalForm, seat, court, number, directors, board, capital);
            p.save();
            plugin.setWarnOnMail(warnMail.isSelected());
            plugin.setWarnOnTemplate(warnTemplate.isSelected());
            updatePreview.run();
            java.util.List<String> missing = p.missing();
            yabs.notify(missing.isEmpty() ? yabs.tr("settings.saved") : yabs.tr("settings.savedMissing", plugin.names(missing)));
        });
        return page.build(save);
    }

    private static GmbhData read(JComboBox<String> legalForm, JTextField seat, JTextField court, JTextField number,
            JTextArea directors, JTextField board, JTextField capital) {
        GmbhData p = GmbhData.load();
        Object lf = legalForm.getEditor().getItem();
        p.legalForm = lf == null ? "" : String.valueOf(lf).trim();
        p.seat = seat.getText().trim();
        p.registerCourt = court.getText().trim();
        p.registerNumber = number.getText().trim();
        p.managingDirectors = directors.getText().trim();
        p.supervisoryChair = board.getText().trim();
        p.shareCapital = capital.getText().trim();
        return p;
    }

    private static int add(JPanel form, GridBagConstraints g, int row, String label, JComponent c) {
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 0;
        form.add(new JLabel(label), g);
        g.gridx = 1;
        g.weightx = 1;
        form.add(c, g);
        return row + 1;
    }

    static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
