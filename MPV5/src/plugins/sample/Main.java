/*
 *  This file is part of MP by anti43 /GPL.
 *
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package plugins.sample;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import mpv5.pluginhandling.MP5Plugin;
import mpv5.ui.frames.MPV5View;

/**
 * Needs to go to a package named "plugin"
 * @author anti
 */
public class Main extends JPanel implements MP5Plugin, Runnable {
    private static final long serialVersionUID = -2334458558298502643L;
    private MPV5View frame;
    private JMenu cmenu;
    private JLabel clock =  new JLabel();
    private boolean loaded = false;

    public MP5Plugin load(MPV5View frame) {
        this.frame = frame;
        cmenu = new JMenu("what a crazy menu");
        frame.getMenuBar().add(cmenu);
        MPV5View.addMessage("added a crazy menu");

        setLayout(new BorderLayout());
        clock.setFont(new Font("Courier", Font.BOLD, 16));
        add(clock, BorderLayout.CENTER);

        loaded = true;
        return this;
    }

    public void unload() {
        frame.getMenuBar().remove(cmenu);
        loaded = false;
    }

    @Override
    public String getName() {
        return "Sample Plugin for MP5";
    }

    public String getVendor() {
        return "anti";
    }

    public Long getUID() {
        return serialVersionUID;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isComponent() {
        return false;
    }

    public boolean isRunnable() {
        return true;
    }

    public void run() {
       while(loaded){
           clock.setText(new Date().toString());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
       }
    }

    public boolean isLoaded() {
       return loaded;
    }
}
