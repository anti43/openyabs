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
package mp4.panels.misc;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author Andreas
 */
public abstract class commonPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private boolean edited;

    public void close() {
//        setPanelValues();
        if (isEdited()) {
            if (mp4.items.visual.Popup.Y_N_dialog("Wenn Sie jetzt schliessen, gehen Ihre Änderungen verloren.\nTrotzdem schliessen?")) {
                ((JTabbedPane) this.getParent()).remove(this);
            }
        } else {
            ((JTabbedPane) this.getParent()).remove(this);
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
//          if (edit && (edit != edited)) {
//            this.changeTabText(((JTabbedPane) this.getParent()).getTitleAt(((JTabbedPane) this.getParent()).getSelectedIndex()) + "*");
//        } else if (!edit) {
//            this.changeTabText(((JTabbedPane) this.getParent()).getTitleAt(((JTabbedPane) this.getParent()).getSelectedIndex()).replaceAll("\\*", ""));
//        }
        edited = edit;
    }
}
