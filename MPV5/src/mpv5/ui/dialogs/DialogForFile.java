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
package mpv5.ui.dialogs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.frames.MPV5View;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.jobs.Waiter;

/**
 *  This class is useful for selecting files and directories
 * @author Andreas
 */
public class DialogForFile extends JFileChooser implements Waiter {

    private static final long serialVersionUID = 1L;
    private File file = null;

    /**
     * Create a new dialog for files and dirs
     */
    public DialogForFile() {
        super();
        this.setFileSelectionMode(DialogForFile.FILES_AND_DIRECTORIES);

//        this.setSelectedFile(new File(""));
    }

    /**
     * Create a new dialog for the given selection mode
     * @param mode DialogForFile.MODE
     */
    public DialogForFile(int mode) {
        super();
        this.setFileSelectionMode(mode);
//        this.setSelectedFile(new File(""));
    }

    /**
     * Create a new dialog for files and dirs with the given file seleced
     * @param file
     */
    public DialogForFile(File file) {
        super();
        this.setFileSelectionMode(DialogForFile.FILES_AND_DIRECTORIES);
        this.setSelectedFile(file);
    }

    /**
     * Create a new dialog for the given selection mode with the given file seleced
     * @param mode
     * @param filename
     */
    public DialogForFile(int mode, String filename) {
        super();
        this.setFileSelectionMode(mode);
        this.setSelectedFile(new File(filename));
    }

    /**
     * Create a new dialog for the given selection mode with the given file seleced
     * @param mode
     * @param file
     */
    public DialogForFile(int mode, File file) {
        super();
        this.setFileSelectionMode(mode);
        this.setSelectedFile(file);
    }

    /**
     * Show a file selection dialog
     * @return true if a file/dir was selected
     */
    public boolean chooseFile() {
        try {
            if (this.showOpenDialog(MPV5View.identifierFrame) == JFileChooser.APPROVE_OPTION) {
                try {
                    this.file = this.getSelectedFile();
                    return true;
                } catch (Exception ex) {
                    Log.Debug(this, ex);
                }
            }
        } catch (Exception n) {
            Log.Debug(this, n.getMessage());
        }
        return false;
    }

     /**
     * Show a file save dialog
     * @return true if a file/dir was selected
     */
    public boolean saveFile() {
        try {
            if (this.showSaveDialog(MPV5View.identifierFrame) == JFileChooser.APPROVE_OPTION) {
                try {
                    if (!this.getSelectedFile().exists()) {
                        this.file = this.getSelectedFile();
                        return true;
                    } else {
                        if (!Popup.Y_N_dialog(Messages.FILE_EXISTS + "\n" + getSelectedFile())) {
                            saveFile();
                        } else {
                            this.file = this.getSelectedFile();
                            return true;
                        }
                    }
                } catch (Exception ex) {
                    Log.Debug(this, ex);
                }
            }
        } catch (Exception n) {
            Log.Debug(this, n.getMessage());
        }
        return false;
    }

    /**
     * Show a file save dialog
     * @param fileToSave
     */
    public void saveFile(File fileToSave) {

        if (this.showSaveDialog(MPV5View.identifierFrame) == JFileChooser.APPROVE_OPTION) {
            this.file = this.getSelectedFile();
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(DialogForFile.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                if (!Popup.Y_N_dialog(Messages.FILE_EXISTS + "\n" + file)) {
                    saveFile(file);
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
                MPV5View.addMessage(Messages.FILE_SAVED + file.getCanonicalPath());
            } catch (IOException iOException) {
                Popup.error(Messages.FILE_NOT_SAVED + file.getName(), iOException);

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

    /**
     *
     * @return The selected file
     */
    public File getFile() {
        return file;
    }

    /**
     * Show a file selection dialog
     * @param field This gets the selected files' canonical path
     * @return True if a file was selected
     */
    public boolean getFilePath(JTextField field) {
        if (this.showOpenDialog(MPV5View.identifierFrame ) == JFileChooser.APPROVE_OPTION) {
            try {
                field.setText(this.getSelectedFile().getCanonicalPath());
                this.file = this.getSelectedFile();
                return true;
            } catch (IOException ex) {
                Log.Debug(this, ex);
            }
        }
        return false;
    }

    /**
     * Calls saveFile((File) object)
     * @param object
     */
    @Override
    public void set(Object object) {
        saveFile((File) object);
    }

    public void writeFile(File file) {
        if (saveFile()) {
            try {
                FileDirectoryHandler.copyFile(file, this.file);
                MPV5View.addMessage(Messages.FILE_SAVED + this.file);
            } catch (Exception ex) {
                Popup.error(Messages.FILE_NOT_SAVED + file.getName(), ex);
                Log.Debug(this, ex);
            }
        }
    }
}
