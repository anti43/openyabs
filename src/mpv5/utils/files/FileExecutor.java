package mpv5.utils.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import mpv5.Main;
import mpv5.db.objects.User;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;

/**
 *
 * @author andreas.weber
 */
public class FileExecutor {

    /**
     * Runs a command, blocking, in the yabs home directory, with sudo if root password is available and os is unix
     * @param command
     */
    public static synchronized void run(String command) {
        Log.Debug(FileExecutor.class, "Running command: " + command);
        if (Main.osIsWindows) {
            runWin(command);
        } else if (Main.osIsMacOsX || Main.osIsLinux || Main.osIsSolaris) {
            runUnix(command);
        }
    }

    private static void runWin(String command) {

        String output = null;
        File workDir = new File(Main.MPPATH);

        try {
            Process p = Runtime.getRuntime().exec(command, null, workDir);
            Main.addProcessToClose(p);
            int exitval = p.waitFor();
            if (exitval == 0) {
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((output = stdInput.readLine()) != null) {
                    Log.Print(output);
                }
            } else {
                BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((output = stdErr.readLine()) != null) {
                    Log.Print(output);
                }
            }
        } catch (Exception e) {
            Log.Debug(e);
        }
    }

    private static void runUnix(String command) {

        String pw = null;
        if (LocalSettings.hasProperty(LocalSettings.CMD_PASSWORD) &&
                LocalSettings.getProperty(LocalSettings.CMD_PASSWORD).length() > 0) {
            pw = LocalSettings.getProperty("cmdpassword");
        } else if (User.getCurrentUser().getProperties().hasProperty("cmdpassword")) {
            pw = User.getCurrentUser().getProperties().getProperty("cmdpassword");
        }
        if (pw != null) {
            String script = "#!/usr/bin/expect -d" + "\n"
                    + "set password " + pw + "\n"
                    + "set timeout -1" + "\n"
                    + "spawn ssh root@localhost \"" + command + "\"" + "\n"
                    + "match_max 100000" + "\n"
                    + "expect \"*?assword:*\"" + "\n"
                    + "send -- \"$password\\r\"" + "\n"
                    + "send -- \"\\r\"" + "\n"
                    + "expect eof";

            File rw = FileDirectoryHandler.getTempFile("sh");
            FileReaderWriter frw = new FileReaderWriter(rw);
            frw.writeOnce(script);
            rw.setExecutable(true);
            command = rw.getPath();
            Log.Debug(FileExecutor.class, "Running script: " + rw);
        }

        String output = null;
        File workDir = new File(Main.MPPATH);
        Log.Debug(FileExecutor.class, command);
        try {
            Process p = Runtime.getRuntime().exec(command, null, workDir);
            Main.addProcessToClose(p);
            int exitval = p.waitFor();
            if (exitval == 0) {
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((output = stdInput.readLine()) != null) {
                    Log.Print(output);
                }
            } else {
                BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((output = stdErr.readLine()) != null) {
                    Log.Print(output);
                }
            }
        } catch (Exception e) {
            Log.Debug(e);
        }
    }
}
