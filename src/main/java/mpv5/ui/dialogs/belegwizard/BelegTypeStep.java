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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import mpv5.db.objects.Item;
import mpv5.globals.Messages;
import mpv5.ui.modern.ModernTheme;

/**
 * Step 1 of the Beleg-Wizard: choose the document type (invoice, offer, order,
 * progress invoice, deposit, credit note) via large, clear selectable tiles.
 */
public class BelegTypeStep extends JPanel {

    private static final long serialVersionUID = 1L;

    private final BelegWizardState state;
    private final Runnable onAdvance;
    private final Map<Integer, JPanel> cards = new LinkedHashMap<Integer, JPanel>();

    public BelegTypeStep(BelegWizardState state, Runnable onAdvance) {
        super(new BorderLayout(0, ModernTheme.px(16)));
        this.state = state;
        this.onAdvance = onAdvance;
        setOpaque(false);
        setBorder(ModernTheme.padding(8, 12));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.PAGE_AXIS));
        JLabel heading = new JLabel(Messages.BELEG_WIZARD_TYPE_HEADING.toString());
        heading.setFont(ModernTheme.font(6, Font.BOLD));
        heading.setForeground(ModernTheme.foreground());
        JLabel subtitle = new JLabel(Messages.BELEG_WIZARD_TYPE_SUBTITLE.toString());
        subtitle.setFont(ModernTheme.font(0, Font.PLAIN));
        subtitle.setForeground(ModernTheme.muted());
        header.add(heading);
        header.add(Box.createVerticalStrut(ModernTheme.px(4)));
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 3, ModernTheme.px(12), ModernTheme.px(12)));
        grid.setOpaque(false);
        grid.add(createCard(Item.TYPE_INVOICE, "RE", new Color(37, 99, 235), Messages.BELEG_WIZARD_DESC_INVOICE.toString()));
        grid.add(createCard(Item.TYPE_OFFER, "AN", new Color(13, 148, 136), Messages.BELEG_WIZARD_DESC_OFFER.toString()));
        grid.add(createCard(Item.TYPE_ORDER, "AU", new Color(22, 163, 74), Messages.BELEG_WIZARD_DESC_ORDER.toString()));
        grid.add(createCard(Item.TYPE_PART_PAYMENT, "AR", new Color(124, 58, 237), Messages.BELEG_WIZARD_DESC_PART_PAYMENT.toString()));
        grid.add(createCard(Item.TYPE_DEPOSIT, "AZ", new Color(217, 119, 6), Messages.BELEG_WIZARD_DESC_DEPOSIT.toString()));
        grid.add(createCard(Item.TYPE_CREDIT, "GS", new Color(220, 38, 38), Messages.BELEG_WIZARD_DESC_CREDIT.toString()));

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(grid, BorderLayout.NORTH);
        add(centerWrap, BorderLayout.CENTER);

        selectType(state.getItemType());
    }

    private JPanel createCard(final int itemType, String code, Color badgeColor, String description) {
        final JPanel card = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        card.setName("belegType_" + itemType);
        card.setOpaque(true);
        card.setBackground(ModernTheme.cardBackground());
        card.setPreferredSize(new Dimension(ModernTheme.px(210), ModernTheme.px(136)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel badge = new JLabel(code, JLabel.CENTER);
        badge.setOpaque(true);
        badge.setBackground(badgeColor);
        badge.setForeground(Color.WHITE);
        badge.setFont(ModernTheme.font(1, Font.BOLD));
        badge.setPreferredSize(new Dimension(ModernTheme.px(36), ModernTheme.px(28)));

        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);

        JLabel title = new JLabel(Item.getTypeString(itemType));
        title.setFont(ModernTheme.font(3, Font.BOLD));
        title.setForeground(ModernTheme.foreground());

        JLabel desc = new JLabel("<html><div style='width:" + ModernTheme.px(180) + "px'>" + description + "</div></html>");
        desc.setFont(ModernTheme.font(-1, Font.PLAIN));
        desc.setForeground(ModernTheme.muted());

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.PAGE_AXIS));
        body.add(title);
        body.add(Box.createVerticalStrut(ModernTheme.px(4)));
        body.add(desc);

        card.add(badgeWrap, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectType(itemType);
                if (e.getClickCount() >= 2 && onAdvance != null) {
                    onAdvance.run();
                }
            }
        });

        cards.put(itemType, card);
        return card;
    }

    public void selectType(int itemType) {
        state.setItemType(itemType);
        Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.border(), 1),
                ModernTheme.padding(14, 14));
        Border active = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.accent(), 2),
                ModernTheme.padding(13, 13));
        Color activeBg = ModernTheme.accentSoft();
        Color normalBg = ModernTheme.cardBackground();
        for (Map.Entry<Integer, JPanel> entry : cards.entrySet()) {
            boolean selected = entry.getKey().intValue() == itemType;
            JPanel card = entry.getValue();
            card.setBorder(selected ? active : normal);
            card.setBackground(selected ? activeBg : normalBg);
        }
        repaint();
    }

    public int getSelectedType() {
        return state.getItemType();
    }
}
