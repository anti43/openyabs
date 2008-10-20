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
package mp4.items;

import mp4.frames.mainframe;
import mp4.interfaces.ActionHandler;
import mp4.interfaces.ContactPanel;
import mp4.interfaces.DataPanel;
import mp4.items.visual.MenuItem;
import mp4.items.visual.Popup;

/**
 *
 * @author anti43
 */
public class ClipBoard implements ActionHandler {

    private mainframe frame;

    public ClipBoard(mainframe frame) {
        this.frame = frame;
    }

    @Override
    public void doAction(MenuItem item) {
        try {
            ((DataPanel) frame.mainTabPane.getSelectedComponent()).setProduct(((Product) item.getItem()));
        } catch (ClassCastException e) {
            try {
                ((DataPanel) frame.mainTabPane.getSelectedComponent()).setContact(((People) item.getItem()));
            } catch (Exception ex) {
                try {
                    ((ContactPanel) frame.mainTabPane.getSelectedComponent()).setContact(((People) item.getItem()));
                } catch (Exception elf) {
                    item.removeFromParent();
                    Popup.error("Einfuegen nicht moeglich.", "Es ist ein Fehler aufgetreten.");
                }
            }
        }
    }

    public void add(Things data) {
        frame.clipboard.add(new MenuItem(this, "Datensatz: " + data.getNummer(), data, frame.clipboard));
    }
   
    public void add(People data){
        frame.clipboard.add(new MenuItem(this,"Kontakt: " + data.getNummer(), data, frame.clipboard));
    }
}
