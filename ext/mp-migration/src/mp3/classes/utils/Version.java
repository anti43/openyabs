/*
 * 
 * 
 */
package mp3.classes.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

/**
 *
 * @author Andreas
 */
public class Version {

    public void getVersion(String url, JLabel panel) {
        SwingWorker t = new Task(url, panel);
        t.execute();

    }

    class Task extends SwingWorker<String, Void> {

        private String address;
        private String localFileName = "version.txt";
        URLConnection conn = null;
        InputStream in = null;
        byte[] buffer = new byte[1024];
        int numRead;
        Long numWritten = 0l;
        private JLabel panel;
        private StringWriter out;

        /*
         * Main task. Executed in background thread.
         */
        public Task() {
        }

        Task(String address, JLabel panel) {

            this.address = address;
            this.panel = panel;

        }

        public String doInBackground() {
            {

                BufferedReader ins = null;
                try {
                    URL url = new URL(address);
                    StringWriter content = new StringWriter();
                    ins = new BufferedReader(new InputStreamReader(url.openStream()));

                    String line;
                    while ((line = ins.readLine()) != null) {
                        content.write(line);
                    }

                    ins.close();
                    content.close();

                    return content.toString();
                } catch (IOException ex) {
                    Logger.getLogger(Version.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        ins.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Version.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            return null;
        }

        @Override
        public void done() {
            try {


                panel.setText(get());
            } catch (InterruptedException ex) {
                Logger.getLogger(Version.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(Version.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}
