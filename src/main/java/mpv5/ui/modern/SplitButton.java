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
package mpv5.ui.modern;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * A button with a main action and a small arrow next to it which opens
 * further actions (e.g. "New invoice" with deposit and part payment)
 */
public class SplitButton extends JPanel {

    private static final long serialVersionUID = 1L;
    private final JButton main;
    private final JButton arrow = new JButton("\u25BE");
    private final JPopupMenu menu = new JPopupMenu();

    /**
     * @param label
     * @param icon resource path, may be null
     */
    public SplitButton(String label, String icon) {
        super(new BorderLayout(0, 0));
        setOpaque(false);
        main = ModernTheme.actionButton(new JButton(label), icon);
        ModernTheme.actionButton(arrow, null);
        arrow.setMargin(new java.awt.Insets(ModernTheme.px(6), ModernTheme.px(6), ModernTheme.px(6), ModernTheme.px(6)));
        arrow.addActionListener(e -> menu.show(this, 0, getHeight()));
        add(main, BorderLayout.CENTER);
        add(arrow, BorderLayout.EAST);
    }

    /**
     * @param l the main action
     */
    public void addActionListener(ActionListener l) {
        main.addActionListener(l);
    }

    /**
     * Adds a further action to the menu of the arrow
     *
     * @param label
     * @param l
     */
    public void add(String label, ActionListener l) {
        JMenuItem i = new JMenuItem(label);
        i.setFont(ModernTheme.font(1, java.awt.Font.PLAIN));
        i.addActionListener(l);
        menu.add(i);
    }

    @Override
    public void setToolTipText(String text) {
        main.setToolTipText(text);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        main.setEnabled(enabled);
        arrow.setEnabled(enabled);
    }
}
