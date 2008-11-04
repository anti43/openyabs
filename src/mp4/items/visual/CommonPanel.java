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
package mp4.items.visual;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import mp4.interfaces.Lockable;

/**
 *
 * @author Andreas
 */
public abstract class CommonPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private boolean edited;
    private Lockable lockable;

    public void close() {

        if (isEdited()) {
            if (mp4.items.visual.Popup.Y_N_dialog("Wenn Sie jetzt schließen, gehen Ihre Änderungen verloren.\nTrotzdem schließen?")) {
                ((JTabbedPane) this.getParent()).remove(this);
                this.setVisible(false);
                if (getLockable() != null) {
                    getLockable().unlock();
                }
            }
        } else {
            ((JTabbedPane) this.getParent()).remove(this);
            this.setVisible(false);
            if (getLockable() != null) {
                getLockable().unlock();
            }
        }
    }

    public void undo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void redo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void changeTabText(String text) {
        ((JTabbedPane) this.getParent()).setTitleAt(((JTabbedPane) this.getParent()).getSelectedIndex(), text);
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edit) {
        edited = edit;
    }
    
    public void setLockable(Lockable lockable) {
       this.lockable = lockable;
    }

    public Lockable getLockable() {
        return lockable;
    }
}
