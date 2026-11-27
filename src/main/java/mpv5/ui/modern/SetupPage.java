package mpv5.ui.modern;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

/**
 * Builds the modern look of a plugin's "Setup" page: a header, a card with
 * the one-click setup actions, further settings cards and a sticky save bar.
 * The page is used in the control panel and in its own tab.
 */
public final class SetupPage {

    private static final int CONTENT_WIDTH = 820;

    private final JPanel stack = new Stack();
    private int row;
    private final JPanel root = new JPanel(new BorderLayout());
    private Card actions;

    /**
     * @param title the plugin's title
     * @param subtitle a short description
     */
    public SetupPage(String title, String subtitle) {
        stack.setOpaque(false);
        stack.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(14), ModernTheme.px(16), ModernTheme.px(14), ModernTheme.px(16)));
        JPanel head = new JPanel(new BorderLayout(0, ModernTheme.px(4)));
        head.setOpaque(false);
        head.setBorder(BorderFactory.createEmptyBorder(0, ModernTheme.px(2), ModernTheme.px(6), 0));
        JLabel t = new JLabel(title);
        t.setFont(ModernTheme.font(6, Font.BOLD));
        head.add(t, BorderLayout.NORTH);
        if (subtitle != null && !subtitle.isEmpty()) {
            head.add(Applets.plainHint(subtitle), BorderLayout.CENTER);
        }
        add(head);
    }

    private void add(JComponent c) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = row++;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTH;
        g.insets = new Insets(0, 0, ModernTheme.px(12), 0);
        stack.add(c, g);
    }

    /**
     * Adds a one-click action to the "quick setup" card
     *
     * @param cardTitle title of the card (used once, on the first action)
     * @param button the action button
     * @param title the action's title
     * @param description what it does
     * @return this
     */
    public SetupPage action(String cardTitle, JButton button, String title, String description) {
        if (actions == null) {
            actions = new Card(cardTitle);
            add(actions);
        }
        JPanel r = new JPanel(new BorderLayout(ModernTheme.px(12), 0));
        r.setOpaque(false);
        r.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.border()),
                BorderFactory.createEmptyBorder(ModernTheme.px(6), 0, ModernTheme.px(8), 0)));
        JPanel text = new JPanel(new BorderLayout(0, ModernTheme.px(2)));
        text.setOpaque(false);
        JLabel tl = new JLabel(title);
        tl.setFont(ModernTheme.font(0, Font.BOLD));
        text.add(tl, BorderLayout.NORTH);
        text.add(Applets.plainHint(description), BorderLayout.CENTER);
        r.add(text, BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bp.setOpaque(false);
        button.setText(button.getText());
        bp.add(button);
        r.add(bp, BorderLayout.EAST);
        actions.addFull(r);
        return this;
    }

    /**
     * @param card a settings card
     * @return this
     */
    public SetupPage card(Card card) {
        add(card);
        return this;
    }

    /**
     * @param text a muted note
     * @return this
     */
    public SetupPage note(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, ModernTheme.px(4), 0, ModernTheme.px(4)));
        p.add(Applets.plainHint(text), BorderLayout.CENTER);
        add(p);
        return this;
    }

    /**
     * @param box a checkbox, made transparent
     * @return the same checkbox
     */
    public static JCheckBox plain(JCheckBox box) {
        box.setOpaque(false);
        return box;
    }

    /**
     * @param save the save button (shown in the sticky bar), null for none
     * @return the page
     */
    public JComponent build(JButton save) {
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row++;
        g.weighty = 1;
        g.fill = GridBagConstraints.VERTICAL;
        stack.add(filler, g);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(ModernTheme.background());
        holder.add(stack, BorderLayout.CENTER);
        JScrollPane sp = new JScrollPane(holder, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(ModernTheme.px(16));
        sp.getViewport().setBackground(ModernTheme.background());

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), ModernTheme.px(8)));
        bar.setBackground(ModernTheme.cardBackground());
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.border()));
        if (save != null) {
            bar.add(save);
        }

        root.setBackground(ModernTheme.background());
        root.add(sp, BorderLayout.CENTER);
        if (save != null) {
            root.add(bar, BorderLayout.SOUTH);
        }
        return root;
    }

    /**
     * @param label button text
     * @return the primary save button
     */
    public static PrimaryButton saveButton(String label) {
        return new PrimaryButton(label, null);
    }

    private static final class Stack extends JPanel implements Scrollable {

        private static final long serialVersionUID = 1L;

        Stack() {
            super(new GridBagLayout());
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(ModernTheme.px(CONTENT_WIDTH), ModernTheme.px(560));
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return ModernTheme.px(16);
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return Math.max(ModernTheme.px(80), r.height - ModernTheme.px(24));
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
