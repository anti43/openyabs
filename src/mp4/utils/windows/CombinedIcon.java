/*
 * 
This file is part of MP by anti43 /GPL.

MP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with MP.  If not, see <http://www.gnu.org/licenses/>.

 * 
 */
package mp4.utils.windows;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class CombinedIcon extends ImageIcon {

    public static final int POS_BOTTOM_LEFT = 0;
    public static final int POS_BOTTOM_RIGHT = 1;
    public static final int POS_TOP_LEFT = 2;
    public static final int POS_TOP_RIGHT = 3;
    private final Icon icon1;
    private final Icon icon2;
    private int secondIconPos = 0;

    public CombinedIcon(Icon icon1, Icon icon2) {
        this.icon1 = icon1;
        this.icon2 = icon2;
    }

    public CombinedIcon(Icon icon1, Icon icon2, int secondIconPos) {
        this(icon1, icon2);
        this.secondIconPos = secondIconPos;
    }

    @Override
    public int getIconWidth() {
        return Math.max(icon1.getIconWidth(), icon2.getIconWidth());
    }

    @Override
    public int getIconHeight() {
        return Math.max(icon1.getIconHeight(), icon2.getIconHeight());
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        icon1.paintIcon(c, g, x, y);
        if (secondIconPos == POS_BOTTOM_RIGHT) {
            icon2.paintIcon(c, g, icon1.getIconWidth() - icon2.getIconWidth(), icon1.getIconHeight() - icon2.getIconHeight());
        } else if (secondIconPos == POS_TOP_LEFT) {
            icon2.paintIcon(c, g, x, y);
        } else if (secondIconPos == POS_TOP_RIGHT) {
            icon2.paintIcon(c, g, icon1.getIconWidth() - icon2.getIconWidth(), y);
        } else {
            icon2.paintIcon(c, g, x, icon1.getIconHeight() - icon2.getIconHeight());
        }
    }
}
