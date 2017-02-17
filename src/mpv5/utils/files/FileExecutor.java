package mpv5.utils.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import mpv5.Main;
import mpv5.db.objects.User;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Notificator;

/**
 *
 * @author andreas.weber
 */
public class FileExecutor {

    /**
     * Runs a command,non- blocking, in the yabs home directory, with sudo if
     * root password is available and os is unix
     *
     * @param command
     */
    public static synchronized void run(String command) {
        if (Main.osIsWindows) {
            runWin(command);
        } else if (Main.osIsMacOsX || Main.osIsLinux || Main.osIsSolaris) {
            runUnixWPassword(command);
        }
    }

    /**
     * Runs a command,non- blocking, in the yabs home directory, with sudo if
     * root password is available and os is unix
     *
     * @param commandArgs
     * @param processHolder
     */
    public static synchronized void run(String[] commandArgs, List<Process> processHolder) {
        runAlternate(commandArgs, processHolder);
    }

    /**
     * Runs a command,non- blocking, in the yabs home directory, with sudo if
     * root password is available and os is unix
     *
     * @param commandArgs
     */
    public static synchronized void run(String[] commandArgs) {
        runAlternate(commandArgs, new ArrayList<Process>());
    }

    private static void runAlternate(String[] commandArrq, final List<Process> processHolder) {

        final ProcessBuilder builder = new ProcessBuilder(commandArrq);

        Map<String, String> environment = builder.environment();
        environment.put("path", ";"); // Clearing the path variable;
        environment.put("path", commandArrq[0] + File.pathSeparator);
        builder.directory(FileDirectoryHandler.getnewTemporaryDirectory());

        Log.Debug(FileExecutor.class, "runAlternate" + Arrays.asList(commandArrq));
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Log.Print(builder.command());
                    Log.Print(builder.directory());
                    Log.Print(builder.environment());
                    Process oos = builder.start();
                    processHolder.add(oos);
                    Main.addProcessToClose(oos);
                    InputStream is = oos.getErrorStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String line;

                    while ((line = br.readLine()) != null) {
                        mpv5.logging.Log.Print(line);
                    }
                } catch (IOException ex) {
                    Notificator.raiseNotification(ex, true);
                }
            }
        };
        new Thread(runnable).start();
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

    private static void runUnixWPassword(String command) {

        String pw = null;
        if (LocalSettings.hasProperty(LocalSettings.CMD_PASSWORD)
                && LocalSettings.getProperty(LocalSettings.CMD_PASSWORD).length() > 0) {
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
