/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JWindow;
import mpv5.ui.frames.MPV5View;
import mpv5.ui.parents.Position;

/**
 *
 * @author anti
 */
public class BigPopup {

    public static void showPopup(JComponent parent, JPanel content) {
        final JWindow window = new JWindow(MPV5View.identifierFrame);
        window.getContentPane().setLayout(new BorderLayout());
        window.getContentPane().add(content, BorderLayout.CENTER);
        window.pack();

        window.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    window.dispose();
                }
            }
        });

        new Position(window);

        window.setVisible(true);

    }
}
