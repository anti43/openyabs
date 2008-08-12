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
package mp4.utils.files;

import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import mp3.classes.utils.Log;

/**
 *
 * @author Andreas
 */
public class DialogOpenFile extends JFileChooser {

    private static final long serialVersionUID = 1L;

    public DialogOpenFile(int mode) {
        super();
        this.setFileSelectionMode(mode);
        this.setSelectedFile(new File(""));
    }

    public boolean getFile(JTextField field) {
        if (this.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                field.setText(this.getSelectedFile().getCanonicalPath());
                return true;
            } catch (IOException ex) {
                Log.Debug(ex);
            }
        }
        return false;
    }
}
