/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import mpv5.ui.frames.MPView;
import mpv5.ui.misc.Position;

/**
 *
 *  
 */
public class BigPopup {

    static HashMap<JPanel, JFrame> contents = new HashMap<JPanel, JFrame>();

    /**
     *  Creates a new popup FRAME
     * @param content
     * @param title
     * @param locationOnScreen
     */
    public static void showPopup(JPanel content, String title, Point locationOnScreen) {
        final JFrame window = new JFrame();
        window.getContentPane().setLayout(new BorderLayout());
        window.getContentPane().add(content, BorderLayout.CENTER);
        window.pack();
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setTitle(title);
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
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        window.setLocation(locationOnScreen);
        window.setVisible(true);
        contents.put(content, window);
    }

    /**
     * Creates a new popup FRAME
     * @param parent
     * @param content
     * @param title
     * @param state
     * @param width optional
     */
    public static void showPopup(JComponent parent, JPanel content, String title, int state, Integer width) {

        final JFrame window = new JFrame();
        window.getContentPane().setLayout(new BorderLayout());
        window.getContentPane().add(content, BorderLayout.CENTER);
        window.pack();
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setTitle(title);
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
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        if (width != null) {
            window.setPreferredSize(new Dimension(width, MPView.identifierFrame.getHeight()));
            window.setSize(new Dimension(width, MPView.identifierFrame.getHeight()));
        }
        window.setExtendedState(state);
        new Position(window);
        window.setVisible(true);
        contents.put(content, window);
    }

    /**
     * Creates a new popup FRAME
     * @param parent
     * @param content
     * @param title
     * @param alwaysOnTop
     */
    public static void showPopup(JComponent parent, JPanel content, String title, boolean alwaysOnTop) {

        final JFrame window = new JFrame();
        window.getContentPane().setLayout(new BorderLayout());
        window.getContentPane().add(content, BorderLayout.CENTER);
        window.pack();
        window.setAlwaysOnTop(alwaysOnTop);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setTitle(title);
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
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        new Position(window);
        window.setVisible(true);
        contents.put(content, window);
    }

    /**
     * Creates a new popup FRAME
     * @param parent
     * @param content
     * @param title
     */
    public static void showPopup(JComponent parent, JPanel content, String title) {
        showPopup(parent, content, title, false);
    }

    /**
     * Closes the popup with the given content
     * @param panel
     */
    public static void close(JPanel panel) {
        contents.get(panel).dispose();
        contents.remove(panel);
    }

    /**
     * 
     * @param panel
     */
    public static void pack(JPanel panel) {
        contents.get(panel).pack();
    }

    public static void setOnTop(JPanel panel) {
        contents.get(panel).setAlwaysOnTop(true);
    }
}
