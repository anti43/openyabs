/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import mpv5.bugtracker.SubmitForm;
import mpv5.ui.misc.Position;

/**
 *
 *  
 */
public class BigPopup {

    static HashMap<JPanel, JFrame> contents = new HashMap<JPanel, JFrame>();
    /**
     * Creates a new popup FRAME
     * @param parent
     * @param content
     */
    public static void showPopup(JComponent parent, JPanel content) {
       
        final JFrame window = new JFrame();
        window.getContentPane().setLayout(new BorderLayout());
        window.getContentPane().add(content, BorderLayout.CENTER);
        window.pack();
        window.setAlwaysOnTop(true);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        window.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
              if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    window.dispose();
                }
            }
            @Override
            public void keyPressed(KeyEvent e) {
               if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    window.dispose();
                }}
            @Override
            public void keyReleased(KeyEvent e) {
              
            }
        });

        new Position(window);
        window.setVisible(true);
        contents.put(content, window);
    }

    /**
     * Closes the popup with the given content
     * @param panel
     */
    public static void close(JPanel panel) {
        contents.get(panel).dispose();
        contents.remove(panel);
    }
}
