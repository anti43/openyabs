/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mp4.installation;

import java.io.File;
import java.io.IOException;
import mp3.classes.interfaces.ProtectedStrings;
import mp3.classes.interfaces.Strings;
import mp3.classes.layer.Popup;
import mp3.classes.utils.JarFinder;
import mp3.classes.utils.Log;
import mp4.main.Main;
import mp4.utils.files.FileDirectoryHandler;

/**
 *
 * @author anti43
 */
public class Verzeichnisse  implements ProtectedStrings, Strings {
    
    private static String url;
    private static String workdir;
    private static File pdf_root_dir;
    private static File backup_dir;
    private static File public_dir;
  
    private static File lib_dir;
    private static File install_lib_dir;
    private static File install_templates_dir;
    
    private static File pdf_offer_dir;
    private static File pdf_bill_dir;
    private static File pdf_mahnung_dir;
    private static File pdf_produkt_dir;
    
    private static File templates_dir;
    private static File cache_dir;
    
   private static String backuppathtftext = System.getProperty("user.home");
   private static String pdfpathtftext = System.getProperty("user.home");;
    
 public  static void buildPath() throws IOException {
        try {
            workdir = JarFinder.getPathOfJar(JAR_NAME);
        } catch (Exception ex) {
            Log.Debug(ex);
        }
        System.out.println("Workdir: " + workdir);

        public_dir = new File(USER_HOME + SEP + PROG_NAME);

        lib_dir = new File(getPublic_dir().getCanonicalPath() + File.separator + LIB_DIR);
        install_lib_dir = new File(workdir + SEP + LIB_DIR);
        install_templates_dir = new File(workdir + SEP + TEMPLATES_DIR);
        cache_dir = new File(DBROOTDIR + SEP + CACHE_DIR);

        if (Main.BACKUP_DIR == null) {
            backup_dir = new File(backuppathtftext);
        } else {
            backup_dir = new File(Main.BACKUP_DIR);
        }


        if (Main.TEMPLATEDIR == null) {
            templates_dir = new File(getPublic_dir().getCanonicalPath() + File.separator + TEMPLATES_DIR);
        } else {
            templates_dir = new File(Main.TEMPLATEDIR);
        }


        if (Main.PDFDIR == null) {
            pdf_root_dir = new File(pdfpathtftext);
        } else {
            pdf_root_dir = new File(Main.PDFDIR);
        }

        pdf_offer_dir = new File(getPdf_root_dir().getCanonicalPath() + File.separator + OFFER_SAVE_DIR);
        pdf_bill_dir = new File(getPdf_root_dir().getCanonicalPath() + File.separator + BILL_SAVE_DIR);
        pdf_mahnung_dir = new File(getPdf_root_dir().getCanonicalPath() + File.separator + ARREAR_SAVE_DIR);
        pdf_produkt_dir = new File(getPdf_root_dir().getCanonicalPath() + File.separator + PRODUCT_SAVE_DIR);
    }

    public static void createDirs() {
        try {
            Log.Debug("Verzeichnisse anlegen..", true);
            if (
                    getPublic_dir().mkdirs() &
                    getTemplates_dir().mkdirs() &
                    getBackup_dir().mkdirs() &
                    getPdf_root_dir().mkdirs() &
                    getPdf_bill_dir().mkdirs() &
                    getPdf_offer_dir().mkdirs() &
                    getPdf_mahnung_dir().mkdirs() &
                    getCache_dir().mkdirs()) {
                
                Log.Debug("Erfolgreich!", true);
            }
        } catch (Exception e) {
             Log.Debug("Fehler!: " + e.getMessage(), true);
        }
    }
 public static void copyFiles() throws Exception {

        if (!public_dir.exists() && getInstall_lib_dir().exists()) {
            if (getPublic_dir().mkdirs()) {
                try {

                    if (!Main.FORCE_NO_FILE_COPY) {
                        Log.Debug("Libraries kopieren..", true);
                        FileDirectoryHandler.copyDirectory(getInstall_lib_dir(), getLib_dir());
                        Log.Debug("MP Jar kopieren..", true);
                        FileDirectoryHandler.copyDirectory(new File(workdir + File.separator + ProtectedStrings.JAR_NAME), new File(getPublic_dir().getAbsolutePath() + File.separator + ProtectedStrings.JAR_NAME));
                    }

                    Log.Debug("Templates kopieren..", true);
                    FileDirectoryHandler.copyDirectory(getInstall_templates_dir(), getTemplates_dir());
                    Log.Debug("Installation abgeschlossen.", true);

                } catch (IOException ex) {
                    Popup.error(ex.getMessage(), "Es ist ein Fehler aufgetreten:");
                    Log.Debug("Es ist ein Fehler aufgetreten: " + ex.getMessage(), true);
                }
            } else {

                Log.Debug("Es ist ein Fehler aufgetreten,\nüberprüfen Sie Ihre Berechtigungen!", true);
            }
        }

    }
    public  static void deleteFiles() {
        try {
            File fil = new File(workdir);
            FileDirectoryHandler.deleteTree(fil);
        } catch (IOException ex) {
        }
    }
    
      public static File getPdf_root_dir() {
        return pdf_root_dir;
    }

    public static File getBackup_dir() {
        return backup_dir;
    }

    public static File getPublic_dir() {
        return public_dir;
    }

    public static File getLib_dir() {
        return lib_dir;
    }

    public static File getInstall_lib_dir() {
        return install_lib_dir;
    }

    public static File getInstall_templates_dir() {
        return install_templates_dir;
    }

    public static File getPdf_offer_dir() {
        return pdf_offer_dir;
    }

    public static File getPdf_bill_dir() {
        return pdf_bill_dir;
    }

    public static File getPdf_mahnung_dir() {
        return pdf_mahnung_dir;
    }

    public static File getTemplates_dir() {
        return templates_dir;
    }

    public static File getCache_dir() {
        return cache_dir;
    }

    public static String getPathpdf_root_dir() {
        return pdf_root_dir.getPath();
    }

    public static String getPathbackup_dir() {
        return backup_dir.getPath();
    }

    public static String getPathpublic_dir() {
        return public_dir.getPath();
    }

    public static String getPathlib_dir() {
        return lib_dir.getPath();
    }

    public static String getPathinstall_lib_dir() {
        return install_lib_dir.getPath();
    }

    public static String getPathinstall_templates_dir() {
        return install_templates_dir.getPath();
    }

    public static String getPathpdf_offer_dir() {
        return pdf_offer_dir.getPath();
    }

    public static String getPathpdf_bill_dir() {
        return pdf_bill_dir.getPath();
    }

    public static String getPathpdf_mahnung_dir() {
        return pdf_mahnung_dir.getPath();
    }

    public static String getPathtemplates_dir() {
        return templates_dir.getPath();
    }

    public static String getPathcache_dir() {
        return cache_dir.getPath();
    }
    
    public static String getPathpdf_produkt_dir() {
       return pdf_produkt_dir.getPath();
    }

    public static void setBackuppathtftext(String aBackuppathtftext) {
        backuppathtftext = aBackuppathtftext;
    }

    public static void setPdfpathtftext(String aPdfpathtftext) {
        pdfpathtftext = aPdfpathtftext;
    }
   

}
