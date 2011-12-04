/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.ui.menus;

import javax.swing.JMenuItem;

/**
 *
 * @author Jan Hahnisch
 */
public class ListMenuItem extends JMenuItem {

    private static final long serialVersionUID = 1L;
    private int ID = 0;

    public ListMenuItem(String string) {
        super(string);
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
}
