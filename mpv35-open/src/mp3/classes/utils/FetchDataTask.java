/*
 * 
 *  *  This file is part of MP by anti43 /GPL.
 *  *  
 *  *      MP is free software: you can redistribute it and/or modify
 *  *      it under the terms of the GNU General Public License as published by
 *  *      the Free Software Foundation, either version 3 of the License, or
 *  *      (at your option) any later version.
 *  *  
 *  *      MP is distributed in the hope that it will be useful,
 *  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 * *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *      GNU General Public License for more details.
 *  *  
 *  *      You should have received a copy of the GNU General Public License
 *  *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package mp3.classes.utils;

import java.awt.Cursor;
import java.awt.Toolkit;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import mp3.classes.interfaces.Daemonable;

/**
 *
 * @author anti43
 */
public class FetchDataTask extends SwingWorker<String[][], Void> {

    private JComponent comp;
    private JProgressBar bar;
    private String[][] result;
    private Daemonable daemon;


    /*
     * Main task. Executed in background thread.
     */
    /**
     * 
     * @param comp
     * @param bar (can be null)
  
     * @param d
     */
    public FetchDataTask(JComponent comp, JProgressBar bar, Daemonable d) {

        this.comp = comp;
        this.bar = bar;
        this.daemon = d;



    }

    public String[][] doInBackground() {
        int h = 0;


        comp.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        try {
            bar.setIndeterminate(true);

        } catch (Exception exception) {
        }



        result = daemon.getAll();

        comp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));


        try {
            bar.setIndeterminate(false);

        } catch (Exception exception) {
        }


        return result;
    }

    /*
     * Executed in event dispatching thread
     */
    @Override
    public void done() {
//        Toolkit.getDefaultToolkit().beep();
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
        try {
            bar.setIndeterminate(false);

        } catch (Exception exception) {
        }


    }
    }
