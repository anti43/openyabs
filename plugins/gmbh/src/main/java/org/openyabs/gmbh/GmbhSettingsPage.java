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

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.NORTHWEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        JTextArea intro = new JTextArea(yabs.tr("settings.intro"));
        intro.setEditable(false);
        intro.setOpaque(false);
        intro.setLineWrap(true);
        intro.setWrapStyleWord(true);
        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        g.weightx = 1;
        form.add(intro, g);
        g.gridwidth = 1;
        row = add(form, g, row, yabs.tr("field." + GmbhData.K_LEGALFORM), legalForm);
        row = add(form, g, row, yabs.tr("field." + GmbhData.K_SEAT), seat);
        row = add(form, g, row, yabs.tr("field." + GmbhData.K_COURT), court);
        row = add(form, g, row, yabs.tr("field." + GmbhData.K_NUMBER), number);
        row = add(form, g, row, yabs.tr("field." + GmbhData.K_DIRECTORS), new JScrollPane(directors));
        row = add(form, g, row, yabs.tr("field." + GmbhData.K_BOARD), board);
        row = add(form, g, row, yabs.tr("field." + GmbhData.K_CAPITAL), capital);
        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        form.add(warnMail, g);
        g.gridy = row++;
        form.add(warnTemplate, g);

        final JLabel preview = new JLabel(" ");
        g.gridy = row++;
        form.add(preview, g);
        Runnable updatePreview = () -> {
            GmbhData p = read(legalForm, seat, court, number, directors, board, capital);
            preview.setText("<html><b>" + yabs.tr("settings.preview") + "</b> " + esc(plugin.legalLine(p)) + "</html>");
        };
        updatePreview.run();

        JButton save = new JButton(yabs.tr("settings.save"));
        save.addActionListener(e -> {
            GmbhData p = read(legalForm, seat, court, number, directors, board, capital);
            p.save();
            plugin.setWarnOnMail(warnMail.isSelected());
            plugin.setWarnOnTemplate(warnTemplate.isSelected());
            updatePreview.run();
            java.util.List<String> missing = p.missing();
            yabs.notify(missing.isEmpty() ? yabs.tr("settings.saved") : yabs.tr("settings.savedMissing", plugin.names(missing)));
        });
        JButton template = new JButton(yabs.tr("cmd.template.title"));
        template.addActionListener(e -> plugin.importTemplate());
        JButton check = new JButton(yabs.tr("cmd.check.title"));
        check.addActionListener(e -> plugin.checkCurrent());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(save);
        buttons.add(template);
        buttons.add(check);
        g.gridy = row++;
        form.add(buttons, g);

        g.gridy = row;
        g.weighty = 1;
        form.add(new JPanel(), g);

        JPanel root = new JPanel(new BorderLayout());
        root.add(form, BorderLayout.NORTH);
        return new JScrollPane(root);
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
