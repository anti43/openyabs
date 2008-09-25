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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import mp4.interfaces.Waiter;
import mp4.logs.*;

/**
 *
 * @author Andreas
 */
public class DialogForFile extends JFileChooser implements Waiter {

    private static final long serialVersionUID = 1L;
    private File file = null;

    public DialogForFile() {
        super();
        this.setFileSelectionMode(DialogForFile.FILES_AND_DIRECTORIES);
        this.setSelectedFile(new File(""));
    }

    public DialogForFile(int mode) {
        super();
        this.setFileSelectionMode(mode);
        this.setSelectedFile(new File(""));
    }

    public DialogForFile(File file) {
        super();
        this.setFileSelectionMode(DialogForFile.FILES_AND_DIRECTORIES);
        this.setSelectedFile(file);
    }

    public DialogForFile(int mode, String filename) {
        super();
        this.setFileSelectionMode(mode);
        this.setSelectedFile(new File(filename));
    }

    public DialogForFile(int mode, File file) {
        super();
        this.setFileSelectionMode(mode);
        this.setSelectedFile(file);
    }

    public boolean chooseFile() {
        if (this.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                this.file = this.getSelectedFile();
                return true;
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        }
        return false;
    }

    public void saveFile(File fileToSave) {

        if (chooseFile()) {
            if (!fileToSave.exists()) {
                try {
                    fileToSave.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(DialogForFile.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            FileReader in = null;
            FileWriter out = null;
            int c;
            try {
                out = new FileWriter(file);
            } catch (IOException ex) {
                Logger.getLogger(DialogForFile.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                in = new FileReader(fileToSave);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(DialogForFile.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                while ((c = in.read()) != -1) {
                    out.write(c);
                }
                fileToSave.delete();
            } catch (IOException iOException) {
                mp4.items.visual.Popup.error("Konnte Datei " + fileToSave.getName() + " nicht schreiben.", "Es ist ein Fehler aufgetreten.");

            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    Logger.getLogger(DialogForFile.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(DialogForFile.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public File getFile() {
        return file;
    }

    public boolean getFilePath(JTextField field) {
        if (this.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                field.setText(this.getSelectedFile().getCanonicalPath());
                this.file = this.getSelectedFile();
                return true;
            } catch (IOException ex) {
                Log.Debug(ex);
            }
        }
        return false;
    }

    public void set(Object object) {
        saveFile((File) object);
    }
}
