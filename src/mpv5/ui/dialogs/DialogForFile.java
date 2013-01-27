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
package mpv5.ui.dialogs;

import java.awt.Container;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import mpv5.Main;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
import mpv5.utils.export.Export;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.jobs.Waiter;

/**
 * This class is useful for selecting files and directories
 *
 */
public class DialogForFile extends JFileChooser implements Waiter {

   private static final long serialVersionUID = 1L;
   public static FileFilter DIRECTORIES = new FileFilter() {
      public boolean accept(File f) {
         return f.isDirectory();
      }

      public String getDescription() {
         return "Directories";
      }
   };
   public static FileFilter TEMPLATE_FILES = new FileFilter() {
      public boolean accept(File f) {
         return f.getName().toLowerCase().endsWith(".odt") || f.getName().toLowerCase().endsWith(".pdf") || f.isDirectory();
      }

      public String getDescription() {
         return "Templates";
      }
   };
   public static FileFilter HTML_FILES = new FileFilter() {
      public boolean accept(File f) {
         return f.getName().toLowerCase().endsWith(".html") || f.getName().toLowerCase().endsWith(".htm") || f.isDirectory();
      }

      public String getDescription() {
         return "HTML Format(*.htm[l])";
      }
   };
   public static FileFilter XML_FILES = new FileFilter() {
      public boolean accept(File f) {
         return f.getName().toLowerCase().endsWith(".xml") || f.isDirectory();
      }

      public String getDescription() {
         return "XML Format(*.xml)";
      }
   };
   public static FileFilter CSV_FILES = new FileFilter() {
      public boolean accept(File f) {
         return f.getName().toLowerCase().endsWith(".csv") || f.isDirectory();
      }

      public String getDescription() {
         return "CSV Format(*.csv)";
      }
   };
   public static FileFilter PDF_FILES = new FileFilter() {
      public boolean accept(File f) {
         return f.getName().toLowerCase().endsWith(".pdf") || f.isDirectory();
      }

      public String getDescription() {
         return "PDF Format(*.pdf)";
      }
   };
   public static FileFilter ZIP_FILES = new FileFilter() {
      public boolean accept(File f) {
         return f.getName().toLowerCase().endsWith(".zip") || f.isDirectory();
      }

      public String getDescription() {
         return "Zip Format(*.zip)";
      }
   };
   public static FileFilter SQL_FILES = new FileFilter() {
      public boolean accept(File f) {
         return f.getName().toLowerCase().endsWith(".sql") || f.isDirectory();
      }

      public String getDescription() {
         return "SQL Format(*.sql)";
      }
   };
   public static FileFilter OOO_FILES = new FileFilter() {
      public boolean accept(File f) {
         return f.getName().toLowerCase().matches(".*sxw$|.*doc$|.*xls$|.*odt$|.*ods$|.*pps$|.*odt$|.*ppt$|.*odp$") || f.isDirectory();
      }

      public String getDescription() {
         return "Open Office Formats(*.odt etc.)";
      }
   };
   private File file = null;
   public static File CURRENT_DIR = new File(Main.USER_HOME);
   private Container mparent;

   /**
    * Create a new dialog for files and dirs
    */
   public DialogForFile() {
      super();
      this.setFileSelectionMode(DialogForFile.FILES_AND_DIRECTORIES);
      this.setSelectedFile(CURRENT_DIR);
   }

   /**
    * Create a new dialog for the given selection mode
    *
    * @param selectionMode DialogForFile.MODE
    */
   public DialogForFile(int selectionMode) {
      super();
      this.setFileSelectionMode(selectionMode);
      this.setSelectedFile(CURRENT_DIR);
   }

   /**
    * Create a new dialog for files and dirs with the given file selected
    *
    * @param file
    */
   public DialogForFile(File file) {
      super();
      if (!file.isDirectory()) {
         this.setFileSelectionMode(DialogForFile.FILES_AND_DIRECTORIES);
         this.setSelectedFile(file);
      } else {
         this.setFileSelectionMode(DialogForFile.FILES_ONLY);
         this.setSelectedFile(new File(file.getPath() + File.separator + "file"));
      }
   }

   /**
    * Create a new dialog for files and dirs with the given file selected
    *
    * @param file
    */
   public DialogForFile(File dir, String filename) {
      super();
      if (!dir.isDirectory()) {
         this.setFileSelectionMode(DialogForFile.FILES_AND_DIRECTORIES);
         this.setSelectedFile(dir);
      } else {
         setCurrentDir(file);
         this.setFileSelectionMode(DialogForFile.FILES_ONLY);
         this.setSelectedFile(new File(dir.getPath(), filename));
      }
   }

   /**
    * Create a new dialog for the given selection mode with the given file
    * seleced
    *
    * @param mode
    * @param filename
    */
   public DialogForFile(int mode, String filename) {
      super();
      this.setFileSelectionMode(mode);
      this.setSelectedFile(new File(filename));
   }

   /**
    * Create a new dialog for the given selection mode with the given file
    * seleced
    *
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
    *
    * @return true if a file/dir was selected
    */
   public boolean chooseFile() {
      try {
         if (this.showOpenDialog(mparent == null ? mpv5.YabsViewProxy.instance().getIdentifierFrame() : mparent) == JFileChooser.APPROVE_OPTION) {
            try {
               this.file = this.getSelectedFile();
               setCurrentDir(file);
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
    *
    * @return true if a file/dir was selected
    */
   public boolean saveFile() {
      try {
         if (this.showSaveDialog(mparent == null ? mpv5.YabsViewProxy.instance().getIdentifierFrame() : mparent) == JFileChooser.APPROVE_OPTION) {
            try {
               if (!this.getSelectedFile().exists()) {
                  this.file = this.getSelectedFile();
                  setCurrentDir(file);
                  return true;
               } else {
                  if (!Popup.Y_N_dialog(Messages.FILE_EXISTS + "\n" + getSelectedFile())) {
                     saveFile();
                  } else {
                     this.file = this.getSelectedFile();
                     CURRENT_DIR = file.getParentFile();
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
    *
    * @param fileToSave
    */
   public File saveFile(File fileToSave) {
      setSelectedFile(new File(fileToSave.getName()));
      if (this.showSaveDialog(mparent == null ? mpv5.YabsViewProxy.instance().getIdentifierFrame() : mparent) == JFileChooser.APPROVE_OPTION) {
         try {
            this.file = this.getSelectedFile();
            if (!file.exists()) {
               try {
                  file.createNewFile();
               } catch (IOException ex) {
                  mpv5.logging.Log.Debug(ex); //Logger.getLogger(DialogForFile.class.getName()).log(Level.SEVERE, null, ex);
               }
            } else {
               if (!Popup.Y_N_dialog(Messages.FILE_EXISTS + "\n" + file)) {
                  saveFile(file);
               }
            }
            FileDirectoryHandler.copyFile2(fileToSave, file);
         } catch (FileNotFoundException ex) {
            Log.Debug(ex);
         } catch (IOException ex) {
            Log.Debug(ex);
         }
         return file;
      }

      return null;
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
    *
    * @param field This gets the selected files' canonical path
    * @return True if a file was selected
    */
   public boolean getFilePath(JTextField field) {
      if (this.showOpenDialog(mparent == null ? mpv5.YabsViewProxy.instance().getIdentifierFrame() : mparent) == JFileChooser.APPROVE_OPTION) {
         try {
            field.setText(this.getSelectedFile().getCanonicalPath());
            this.file = this.getSelectedFile();
            setCurrentDir(file);
            return true;
         } catch (IOException ex) {
            Log.Debug(this, ex);
         }
      }
      return false;
   }

   /**
    * Calls saveFile((File) object)
    *
    * @param object
    * @param e
    * @throws Exception
    */
   @Override
   @SuppressWarnings("unchecked")
   public void set(Object object, Exception e) throws Exception {

      if (e == null) {
         if (object instanceof List) {

            if (!((List) object).isEmpty()) {
               if (((List) object).get(0) instanceof File) {
                  saveFiles((List) object);
               } else if (((List) object).get(0) instanceof Export) {
                  saveFiles2((List) object);
               }
            }
         } else if (object instanceof File) {
            saveFile((File) object);
         } else if (object instanceof Export) {
            if(getSelectedFile() == null)
                setSelectedFile(((Export) object).getTargetFile());
            if (saveFile()) {
               FileDirectoryHandler.copyFile2(((Export) object).getTargetFile(), file, false);
               ((Export) object).getTargetFile().delete();
            }
         }
      } else {
         Popup.error(e);
      }
   }

   /**
    * Shows a choose directory dialog and saves a files in the list to it.
    *
    * @param <T>
    * @param list
    */
   public <T extends Export> void saveFiles2(List<T> list) {
      try {
         this.setFileSelectionMode(DialogForFile.DIRECTORIES_ONLY);
         this.setSelectedFile(CURRENT_DIR);


         if (this.showSaveDialog(mparent == null ? mpv5.YabsViewProxy.instance().getIdentifierFrame() : mparent) == JFileChooser.APPROVE_OPTION) {
            if (!this.getSelectedFile().exists()) {
               this.file = this.getSelectedFile();
               setCurrentDir(file);
               file.mkdirs();
            } else {
               this.file = this.getSelectedFile();
               setCurrentDir(file);
            }

            for (int i = 0; i < list.size(); i++) {
               File f3 = list.get(i).getTargetFile();
               FileDirectoryHandler.copyFile2(f3, new File(CURRENT_DIR.getPath() + File.separator + f3.getName()), true);
            }
         }

      } catch (Exception n) {
         Log.Debug(this, n.getMessage());
         Popup.error(n);
      }
   }

   /**
    * Shows a choose directory dialog and saves a files in the list to it.
    *
    * @param <T>
    * @param list
    */
   public <T extends File> void saveFiles(List<T> list) {
      try {
         this.setFileSelectionMode(DialogForFile.DIRECTORIES_ONLY);
         this.setSelectedFile(CURRENT_DIR);

         if (this.showSaveDialog(mparent == null ? mpv5.YabsViewProxy.instance().getIdentifierFrame() : mparent) == JFileChooser.APPROVE_OPTION) {
            if (!this.getSelectedFile().exists()) {
               this.file = this.getSelectedFile();
               setCurrentDir(file);
               file.mkdirs();
            } else {
               this.file = this.getSelectedFile();
               setCurrentDir(file);
            }

            for (int i = 0; i
                    < list.size(); i++) {
               File f3 = list.get(i);
               FileDirectoryHandler.copyFile2(f3, new File(CURRENT_DIR.getPath() + File.separator + f3.getName()), true);
            }
         }

      } catch (Exception n) {
         Log.Debug(this, n.getMessage());
         Popup.error(n);
      }
   }

   /**
    * Set the modality parent
    *
    * @param mparent
    */
   public void setModalityParent(JComponent mparent) {
      this.mparent = mparent;
   }

   private void setCurrentDir(File file) {
      if (file != null) {
         if (file.isDirectory()) {
            CURRENT_DIR = file;
         } else {
            CURRENT_DIR = file.getParentFile();
         }
      }
   }
}
