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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import mpv5.Main;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
import mpv5.utils.export.Export;
import mpv5.ui.modern.ModernTheme;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.LastDirectory;
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
   /**
    * The folder chosen last (see {@link LastDirectory}, which keeps it across
    * restarts)
    */
   public static File CURRENT_DIR = LastDirectory.get();
   private Container mparent;
   private static Dimension lastSize;

   /**
    * Create a new dialog for files and dirs
    */
   public DialogForFile() {
      this(FILES_AND_DIRECTORIES);
   }

   /**
    * Create a new dialog for the given selection mode, in the folder chosen
    * last
    *
    * @param selectionMode DialogForFile.MODE
    */
   public DialogForFile(int selectionMode) {
      super();
      init();
      this.setFileSelectionMode(selectionMode);
      startIn(null);
   }

   /**
    * Create a new dialog for files and dirs with the given file selected; a
    * file without a folder is put into the folder chosen last
    *
    * @param file
    */
   public DialogForFile(File file) {
      super();
      init();
      if (file != null && file.isDirectory()) {
         this.setFileSelectionMode(DialogForFile.FILES_ONLY);
         setCurrentDirectory(file);
      } else {
         this.setFileSelectionMode(DialogForFile.FILES_AND_DIRECTORIES);
         startIn(file);
      }
   }

   /**
    * Create a new dialog for files with the given file name in the given
    * folder
    *
    * @param dir
    * @param filename
    */
   public DialogForFile(File dir, String filename) {
      super();
      init();
      this.setFileSelectionMode(DialogForFile.FILES_ONLY);
      if (dir != null && dir.isDirectory()) {
         startIn(new File(dir, filename));
      } else {
         startIn(dir);
      }
   }

   /**
    * Create a new dialog for the given selection mode with the given file
    * name selected (in the folder chosen last, unless the name has a folder)
    *
    * @param mode
    * @param filename
    */
   public DialogForFile(int mode, String filename) {
      super();
      init();
      this.setFileSelectionMode(mode);
      startIn(filename == null ? null : new File(filename));
   }

   /**
    * Create a new dialog for the given selection mode with the given file
    * selected (in the folder chosen last, unless the file has a folder)
    *
    * @param mode
    * @param file
    */
   public DialogForFile(int mode, File file) {
      super();
      init();
      this.setFileSelectionMode(mode);
      startIn(file);
   }

   /**
    * Size, places and folder of the dialog
    */
   private void init() {
      setAccessory(new Places(this));
      setPreferredSize(new Dimension(ModernTheme.px(820), ModernTheme.px(520)));
   }

   /**
    * Starts in the folder of the file (a file without a folder: in the folder
    * chosen last) and selects it; null: just the folder chosen last
    */
   private void startIn(File f) {
      File dir;
      if (f == null || f.getPath().length() == 0) {
         dir = LastDirectory.get();
         setCurrentDirectory(dir);
         if (getFileSelectionMode() == DIRECTORIES_ONLY) {
            setSelectedFile(dir);
         }
         return;
      }
      if (!f.isAbsolute() || f.getParentFile() == null || !f.getParentFile().isDirectory()) {
         if (f.isAbsolute() && f.isDirectory()) {
            dir = f;
         } else {
            dir = LastDirectory.get();
            f = new File(dir, f.getName());
         }
      } else {
         dir = f.getParentFile();
      }
      setCurrentDirectory(dir);
      if (f.isDirectory() && getFileSelectionMode() == FILES_ONLY) {
         setCurrentDirectory(f);
      } else {
         setSelectedFile(f);
      }
   }

   @Override
   protected JDialog createDialog(Component parent) throws HeadlessException {
      JDialog d = super.createDialog(parent);
      if (lastSize != null) {
         d.setSize(lastSize);
      } else {
         d.pack();
      }
      d.setLocationRelativeTo(parent);
      d.addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent e) {
            if (d.isShowing()) {
               lastSize = d.getSize();
            }
         }
      });
      return d;
   }

   private Component parentWindow() {
      return mparent == null ? mpv5.YabsViewProxy.instance().getIdentifierFrame() : mparent;
   }

   /**
    * Show a file selection dialog
    *
    * @return true if a file/dir was selected
    */
   public boolean chooseFile() {
      try {
         if (this.showOpenDialog(parentWindow()) == JFileChooser.APPROVE_OPTION) {
            this.file = this.getSelectedFile();
            setCurrentDir(file);
            return true;
         }
      } catch (Exception n) {
         Log.Debug(this, n);
      }
      return false;
   }

   /**
    * Show a file save dialog; asks before an existing file is replaced
    *
    * @return true if a file/dir was selected
    */
   public boolean saveFile() {
      try {
         while (this.showSaveDialog(parentWindow()) == JFileChooser.APPROVE_OPTION) {
            File f = this.getSelectedFile();
            if (f.exists() && !f.isDirectory() && !Popup.Y_N_dialog(Messages.FILE_EXISTS + "\n" + f)) {
               continue;
            }
            this.file = f;
            setCurrentDir(file);
            return true;
         }
      } catch (Exception n) {
         Log.Debug(this, n);
      }
      return false;
   }

   /**
    * Show a file save dialog and copy the file to the chosen place
    *
    * @param fileToSave
    * @return the new file or null if canceled
    */
   public File saveFile(File fileToSave) {
      File sel = getSelectedFile();
      if (sel == null || sel.isDirectory() || !sel.getName().equals(fileToSave.getName())) {
         File dir = sel != null && sel.isDirectory() ? sel : getCurrentDirectory();
         setSelectedFile(new File(dir != null ? dir : LastDirectory.get(), fileToSave.getName()));
      }
      if (saveFile()) {
         try {
            if (!file.exists()) {
               file.createNewFile();
            }
            FileDirectoryHandler.copyFile2(fileToSave, file);
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
      String current = field.getText();
      if (current != null && current.trim().length() > 0 && new File(current.trim()).exists()) {
         startIn(new File(current.trim()));
      }
      if (this.showOpenDialog(parentWindow()) == JFileChooser.APPROVE_OPTION) {
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
            File target = ((Export) object).getTargetFile();
            suggest(target);
            if (saveFile()) {
               FileDirectoryHandler.copyFile2(target, file, false);
               target.delete();
            }
         }
      } else {
         Popup.error(e);
      }
   }

   /**
    * Suggests the name of the created (temporary) file in the chosen folder,
    * unless a file name was given already; adds a missing extension
    */
   private void suggest(File target) {
      File sel = getSelectedFile();
      String ext = extension(target.getName());
      if (sel == null || sel.isDirectory()) {
         File dir = sel != null ? sel : getCurrentDirectory();
         setSelectedFile(new File(dir != null ? dir : LastDirectory.get(), target.getName()));
      } else if (ext.length() > 0 && extension(sel.getName()).length() == 0) {
         setSelectedFile(new File(sel.getParentFile(), sel.getName() + ext));
      }
   }

   private static String extension(String name) {
      int i = name.lastIndexOf('.');
      return i > 0 ? name.substring(i) : "";
   }

   /**
    * Shows a choose directory dialog and saves a files in the list to it.
    *
    * @param <T>
    * @param list
    */
   public <T extends Export> void saveFiles2(List<T> list) {
      List<File> files = new java.util.ArrayList<File>();
      for (T t : list) {
         files.add(t.getTargetFile());
      }
      saveFiles(files);
   }

   /**
    * Shows a choose directory dialog and saves a files in the list to it.
    *
    * @param <T>
    * @param list
    */
   public <T extends File> void saveFiles(List<T> list) {
      try {
         File start = getSelectedFile() != null && getSelectedFile().isDirectory() ? getSelectedFile() : getCurrentDirectory();
         this.setFileSelectionMode(DialogForFile.DIRECTORIES_ONLY);
         if (start != null) {
            this.setSelectedFile(start);
         }

         if (this.showSaveDialog(parentWindow()) == JFileChooser.APPROVE_OPTION) {
            this.file = this.getSelectedFile();
            file.mkdirs();
            setCurrentDir(file);

            for (int i = 0; i < list.size(); i++) {
               File f3 = list.get(i);
               FileDirectoryHandler.copyFile2(f3, new File(file, f3.getName()), true);
            }
         }

      } catch (Exception n) {
         Log.Debug(this, n);
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
         CURRENT_DIR = file.isDirectory() ? file : file.getAbsoluteFile().getParentFile();
         LastDirectory.set(CURRENT_DIR);
      }
   }

   /**
    * Quick access to the usual folders, next to the file list
    */
   private static final class Places extends JPanel {

      private static final long serialVersionUID = 1L;

      Places(final JFileChooser chooser) {
         setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
         setOpaque(false);
         int gap = ModernTheme.px(8);
         //a light panel with a rounded frame next to the file list
         setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createEmptyBorder(0, gap, 0, 0),
                 BorderFactory.createCompoundBorder(
                         new com.formdev.flatlaf.ui.FlatLineBorder(new java.awt.Insets(1, 1, 1, 1), ModernTheme.border(), 1, ModernTheme.px(8)),
                         BorderFactory.createEmptyBorder(gap / 2, gap / 2, gap / 2, gap / 2))));
         JLabel title = new JLabel(Messages.FILE_PLACES.toString());
         title.setFont(title.getFont().deriveFont(Font.BOLD));
         title.setForeground(ModernTheme.muted());
         title.setBorder(BorderFactory.createEmptyBorder(0, ModernTheme.px(6), ModernTheme.px(4), 0));
         title.setAlignmentX(LEFT_ALIGNMENT);
         add(title);

         File home = new File(System.getProperty("user.home"));
         LinkedHashMap<String, File> places = new LinkedHashMap<String, File>();
         places.put(Messages.FILE_PLACE_LAST.toString(), LastDirectory.get());
         File base = LastDirectory.configuredBaseDir();
         if (base != null) {
            places.put(Messages.FILE_PLACE_SAVEDIR.toString(), base);
         }
         places.put(Messages.FILE_PLACE_HOME.toString(), home);
         places.put(Messages.FILE_PLACE_DESKTOP.toString(), new File(home, "Desktop"));
         places.put(Messages.FILE_PLACE_DOCUMENTS.toString(), new File(home, "Documents"));
         Icon icon = UIManager.getIcon("FileView.directoryIcon");
         for (Map.Entry<String, File> p : places.entrySet()) {
            final File dir = p.getValue();
            if (dir == null || !dir.isDirectory()) {
               continue;
            }
            JButton b = new JButton(p.getKey(), icon);
            b.setToolTipText(dir.getPath());
            b.putClientProperty("JButton.buttonType", "toolBarButton");
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setFocusable(false);
            b.setAlignmentX(LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, b.getPreferredSize().height));
            b.addActionListener(e -> {
               File sel = chooser.getSelectedFile();
               String name = sel != null && !sel.isDirectory() ? sel.getName() : null;
               chooser.setCurrentDirectory(dir);
               if (name != null) {
                  chooser.setSelectedFile(new File(dir, name));
               } else if (chooser.getFileSelectionMode() == DIRECTORIES_ONLY) {
                  chooser.setSelectedFile(dir);
               }
            });
            add(b);
         }
         add(Box.createVerticalGlue());
         setPreferredSize(new Dimension(ModernTheme.px(180), getPreferredSize().height));
      }

      @Override
      protected void paintComponent(java.awt.Graphics g) {
         //background inside the frame, with rounded corners
         int left = ModernTheme.px(8);
         int arc = ModernTheme.px(8);
         java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
         g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
         g2.setColor(ModernTheme.cardBackground());
         g2.fillRoundRect(left, 0, getWidth() - left - 1, getHeight() - 1, arc, arc);
         g2.dispose();
         super.paintComponent(g);
      }
   }
}
