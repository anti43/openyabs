/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.misc;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import mpv5.logging.Log;

/**
 * This is a helper class for visual component positioning
 * 
 */
public class Position {

    private Component comp;

    /**
     * Centers the given Component
     * @param comp
     */
    public Position(Component comp) {
        this.comp = comp;
        center();
    }

    /**
     *
     * @param comp
     * @param doNotCenter
     */
    public Position(Component comp, boolean doNotCenter) {
        this.comp = comp;
        if (!doNotCenter) {
            center();
        }
    }

    /**
     * Moves the component to the bottom left corner
     */
    public void bottomLeft() {
        Dimension frameSize = new Dimension(comp.getSize());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        comp.setLocation(0, (int) (screenSize.getHeight() - frameSize.getHeight()));
    }

    /**
     * Centers the given Component
     */
    public void center() {

//        Dimension frameSize = new Dimension(comp.getSize());
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//
//        int top = (screenSize.height - frameSize.height) / 2;
//        int left = (screenSize.width - frameSize.width) / 2;
//
//        comp.setSize(frameSize);
//        comp.setLocation(left, top);

        try {
            Dimension w = comp.getSize();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle r = ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
            Dimension d = new Dimension(r.width, r.height);
            comp.setLocation((d.width - w.width) / 2, (d.height - w.height) / 2);
        } catch (Exception e) {
            Log.Debug(e);
        }
    }

    /**
     * Returns TRUE if the component is not as big as the screen
     * @return
     */
    public boolean isNotMaximized() {
        Dimension frameSize = new Dimension(comp.getSize());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        if (frameSize.width < screenSize.width) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Moves the component to the upper left corner
     */
    public void topLeft() {
        comp.setLocation(0, 0);
    }

    public void bottomRight() {
        Dimension frameSize = new Dimension(comp.getSize());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        comp.setLocation((int) (screenSize.getWidth() - frameSize.getWidth()), (int) (screenSize.getHeight() - frameSize.getHeight()));
    }
}

