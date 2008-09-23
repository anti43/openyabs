/*
 * 
 * 
 */
package mp4.installation;

import java.io.File;
import java.io.IOException;
import mp4.globals.Constants;
import mp4.globals.Strings;
import mp4.items.visual.Popup;
import mp4.utils.files.JarFinder;
import mp4.logs.*;
import mp4.main.Main;
import mp4.utils.files.FileDirectoryHandler;

/**
 *
 * @author anti43
 */
public class Verzeichnisse implements Constants, Strings {

    private static String url;
    private static String workdir;
    private static File pdf_root_dir;
    private static File backup_dir;
    private static File public_dir;
    private static File lib_dir;
    private static File install_lib_dir;
    private static File install_plugin_dir;
    private static File install_templates_dir;
    private static File pdf_offer_dir;
    private static File pdf_bill_dir;
    private static File pdf_mahnung_dir;
    private static File pdf_produkt_dir;
    private static File templates_dir;
    private static File cache_dir;
    private static File plugin_dir;
    private static String backuppathtftext = Main.MPPATH;
    private static String pdfpathtftext = Main.MPPATH;
    ;

    public static void buildPath() throws IOException {
        try {
            workdir = JarFinder.getPathOfJar(JAR_NAME);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Log.Debug("Quellverzeichnis: " + workdir, true);

        public_dir = new File(Main.APP_DIR);

        lib_dir = new File(getPublic_dir().getCanonicalPath() + File.separator + LIB_DIR);
        install_lib_dir = new File(workdir + SEP + LIB_DIR);
        install_templates_dir = new File(workdir + SEP + TEMPLATES_DIR);
        install_plugin_dir = new File(workdir + SEP + PLUGIN_DIR);
        cache_dir = new File(Main.MPPATH + SEP + CACHE_DIR);
        plugin_dir = new File(getPublic_dir().getCanonicalPath() + File.separator + PLUGIN_DIR);

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
                    getPlugin_dir().mkdirs() &
                    getCache_dir().mkdirs()) {

                Log.Debug("Erfolgreich!", true);
            }  else {
                Log.Debug("Es ist ein Fehler aufgetreten,\nüberprüfen Sie Ihre Berechtigungen!", true);
            }
        } catch (Exception e) {
            Log.Debug("Fehler!: " + e.getMessage(), true);
        }
    }

    public static void copyFiles() throws Exception {

        Log.Debug("Kopiere von: " + getInstall_lib_dir(), true);
        if (public_dir.exists() && getInstall_lib_dir().exists()) {
           
                try {

                    if (!Main.FORCE_NO_FILE_COPY) {
                        Log.Debug("Libraries kopieren..", true);
                        FileDirectoryHandler.copyDirectory(getInstall_lib_dir(), getLib_dir());
                        Log.Debug("Plugins kopieren..", true);
                        FileDirectoryHandler.copyDirectory(getInstall_plugin_dir(), getPlugin_dir());
                        Log.Debug("MP Jar kopieren..", true);
                        FileDirectoryHandler.copyDirectory(new File(workdir + File.separator + Constants.JAR_NAME), new File(getPublic_dir().getAbsolutePath() + File.separator + Constants.JAR_NAME));
                    }

                    Log.Debug("Templates kopieren..", true);
                    FileDirectoryHandler.copyDirectory(getInstall_templates_dir(), getTemplates_dir());
                    Log.Debug("Installation abgeschlossen.", true);

                } catch (IOException ex) {
                    Popup.error(ex.getMessage(), "Es ist ein Fehler aufgetreten:");
                    Log.Debug("Es ist ein Fehler aufgetreten: " + ex.getMessage(), true);
                }
            
        } else {
            Log.Debug("Es ist ein Fehler aufgetreten,\nexistiert das /lib Verzeichnis?", true);
        }

    }

    public static void deleteFiles() {
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

    public static File getPlugin_dir() {
        return plugin_dir;
    }

    public static File getInstall_plugin_dir() {
        return install_plugin_dir;
    }

    public String getPathplugin_dir() {
       return plugin_dir.getPath();
    }
}
