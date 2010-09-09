
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
package plugins.sample;

//~--- non-JDK imports --------------------------------------------------------

import mpv5.logging.Log;

import mpv5.pluginhandling.MP5Plugin;

import mpv5.ui.frames.MPView;

//~--- JDK imports ------------------------------------------------------------

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;

import java.io.File;

import java.util.Date;

import javax.imageio.ImageIO;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;

/**
 * This is the sample of a plugin compatible with the MP5 Plugin system.<br/>
 * <b>In real plugins, this needs to go to a package named "plugin"</b> to be recognized as a valid plugin.
 *
 */
public class Main extends JPanel implements MP5Plugin, Runnable {
    private static final long serialVersionUID = -2334458558298502643L;
    private JLabel            clock            = new JLabel();
    private boolean           loaded           = false;
    private JMenu             cmenu;
    private MPView            frame;

    @Override
    public MP5Plugin load(MPView frame) {
        this.frame = frame;
        cmenu      = new JMenu("what a crazy menu");
        frame.getMenuBar().add(cmenu);
        MPView.addMessage("added a crazy menu");
        setLayout(new BorderLayout());
        clock.setFont(new Font("Courier", Font.BOLD, 16));
        add(clock, BorderLayout.CENTER);
        loaded = true;

        return this;
    }

    @Override
    public void unload() {
        frame.getMenuBar().remove(cmenu);
        loaded = false;
    }

    @Override
    public String getName() {
        return "Sample Plugin for MP5";
    }

    @Override
    public String getVendor() {
        return "";
    }

    @Override
    public Long getUID() {
        return serialVersionUID;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isComponent() {
        return false;
    }

    @Override
    public boolean isRunnable() {
        return true;
    }

    @Override
    public void run() {
        while (loaded) {
            clock.setText(new Date().toString());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public Image getIcon() {
        BufferedImage img = null;

        try {
            img = ImageIO.read(new File("/mpv5/resources/images/48/blockdevice.png"));
        } catch (Exception e) {
            Log.Debug(e);
        }

        return img;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
