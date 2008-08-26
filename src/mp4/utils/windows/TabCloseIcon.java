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

/*
 * TabCloseIcon.java
 */
import mp3.classes.interfaces.panelInterface;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;

/**
 *
 * @author Herkules
 */
public class TabCloseIcon implements Icon {

    private final Icon mIcon;
    private JTabbedPane mTabbedPane = null;
    private transient Rectangle mPosition = null;

    /**
     * Creates a new instance of TabCloseIcon.
     */
    public TabCloseIcon(Icon icon) {
        mIcon = icon;
    }

    /**
     * Creates a new instance of TabCloseIcon.
     */
    public TabCloseIcon() {
        this(new ImageIcon(TabCloseIcon.class.getResource("/bilder/small/but_closeWindow.gif")));
    }

    /**
     * when painting, remember last position painted.
     */
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (null == mTabbedPane) {
            mTabbedPane = (JTabbedPane) c;
            mTabbedPane.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    // asking for isConsumed is *very* important, otherwise more than one tab might get closed!
                    if (!e.isConsumed() && mPosition.contains(e.getX(), e.getY())) {
                        final int index = mTabbedPane.getSelectedIndex();
//						mTabbedPane.remove( index );                                           
                        ((panelInterface) mTabbedPane.getComponentAt(index)).close();
                        e.consume();
                    }
                }
            });
        }

        mPosition = new Rectangle(x, y, getIconWidth(), getIconHeight());
        mIcon.paintIcon(c, g, x, y);
    }

    /**
     * just delegate
     */
    public int getIconWidth() {
        return mIcon.getIconWidth();
    }

    /**
     * just delegate
     */
    public int getIconHeight() {
        return mIcon.getIconHeight();
    }
}

