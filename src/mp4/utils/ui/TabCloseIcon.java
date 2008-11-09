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
package mp4.utils.ui;

/*
 * TabCloseIcon.java
 */

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import mp4.logs.Log;
import mp4.items.visual.CommonPanel;
import mp4.plugin.mpplugin;

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
        this(new ImageIcon(TabCloseIcon.class.getResource("/bilder/small/close.png")));
    }

    /**
     * when painting, remember last position painted.
     */
    @Override
    public void paintIcon(Component c, Graphics g, final int x, final int y) {
        if (null == mTabbedPane) {
            final int xw = x + 14;
            final int yw = y + 14;
            
            mTabbedPane = (JTabbedPane) c;
            mTabbedPane.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    
                    // asking for isConsumed is *very* important, otherwise more than one tab might get closed!
                  
//                    if (!e.isConsumed() && mPosition.contains(e.getX(), e.getY())) {
                        if (!e.isConsumed() && e.getX()>x&&e.getX()<xw && e.getY()>y&&e.getY()<yw) {
                        Log.Debug(this,"Initiate unload event..");
                        Log.Debug(this,"Click x:" +e.getX() +" y:"+e.getY());
                        Log.Debug(this,"Icon x:" +xw +" y:"+yw + " width: " +getIconWidth());
                        final int index = mTabbedPane.getSelectedIndex();
          
                        Log.Debug(this,"Unload Tab at Index " + index);
                        try {
                            ((mpplugin) mTabbedPane.getComponentAt(index)).unload();
                            Log.Debug(this,"Unloaded a Plugin: " + ((mpplugin) mTabbedPane.getComponentAt(index)).getName());
                        } catch (Exception ej) {
                        }
                        
                        ((CommonPanel) mTabbedPane.getComponentAt(index)).close();
                        Log.Debug(this,"Unload finished: " + index);
                        e.consume();
                        mTabbedPane.removeMouseListener(this);
                    } 
//                        else Log.Debug(this,"Unload event already consumed.");
                }
            });
        }

        mPosition = new Rectangle(x, y, 14, 14);
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

