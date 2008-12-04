/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.handling;

import javax.swing.JPanel;

/**
 *
 * @author Administrator
 */
public class DataInjection {

    public DataInjection(javax.swing.JPanel panel, DatabaseObject data) {
        inject(panel, data);
    }

    private void inject(JPanel panel, DatabaseObject data) {
        for(int i=0;i<data.getVars().length;i++){

        }
    }
}
