/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import mpv5.ui.misc.Position;

/**
 *
 *  anti
 */
public class BigPopup {

    public static void showPopup(JComponent parent, JPanel content) {
        final JFrame window = new JFrame();
        window.getContentPane().setLayout(new BorderLayout());
        window.getContentPane().add(content, BorderLayout.CENTER);
        window.pack();
        window.setAlwaysOnTop(true);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        window.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
              if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    window.dispose();
                }
            }
            public void keyPressed(KeyEvent e) {
               if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    window.dispose();
                }}
            public void keyReleased(KeyEvent e) {
              
            }
        });

        new Position(window);

        window.setVisible(true);

    }
}
