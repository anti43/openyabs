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
import javax.swing.JDialog;
import javax.swing.JPanel;
import mpv5.YabsViewProxy;
import mpv5.globals.Messages;
import mpv5.ui.misc.Position;

/**
 *
 *  
 */
public class BigPopup {

    private static HashMap<JPanel, JDialog> contents = new HashMap<JPanel, JDialog>();

    /**
     * Creates a new popup FRAME
     * @param content
     */
    public static void showPopup(JPanel content) {
        if (!contents.containsKey(content)) {
            final JDialog window = new JDialog(YabsViewProxy.instance().getIdentifierFrame(), Messages.YABS.getValue());
            window.getContentPane().setLayout(new BorderLayout());
            window.getContentPane().add(content, BorderLayout.CENTER);
            window.pack();
            window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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

            window.setLocationRelativeTo(mpv5.YabsViewProxy.instance().getIdentifierFrame());
            window.setVisible(true);
            contents.put(content, window);
        } else {
            try {
                show(content);
                setOnTop(content);
            } catch (Exception exception) {
            }
        }
    }

    /**
     * Creates a new popup FRAME
     * @param content
     * @param title
     * @param locationOnScreen
     */
    public static void showPopup(JPanel content, String title, Point locationOnScreen) {
        if (!contents.containsKey(content)) {
            final JDialog window = new JDialog(YabsViewProxy.instance().getIdentifierFrame(), Messages.YABS.getValue());
            window.getContentPane().setLayout(new BorderLayout());
            window.getContentPane().add(content, BorderLayout.CENTER);
            window.pack();
            window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
        } else {
            try {
                show(content);
                setOnTop(content);
            } catch (Exception exception) {
            }
        }
    }

    /**
     * Creates a new popup FRAME
     * @param parent
     * @param content
     * @param title
     * @param state
     * @param width optional
     */
    public static void showPopup(JComponent parent, JPanel content, String title, Integer height, Integer width) {
        if (!contents.containsKey(content)) {
            final JDialog window = new JDialog(YabsViewProxy.instance().getIdentifierFrame(), Messages.YABS.getValue());
            window.getContentPane().setLayout(new BorderLayout());
            window.getContentPane().add(content, BorderLayout.CENTER);
            window.pack();
            window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
                window.setPreferredSize(new Dimension(width, height));
                window.setSize(new Dimension(width, height));
            }
//            window.setExtendedState(state);
            new Position(window);
            window.setVisible(true);
            contents.put(content, window);
        } else {
            try {
                show(content);
                setOnTop(content);
            } catch (Exception exception) {
            }
        }
    }

    /**
     * Creates a new popup FRAME
     * @param parent
     * @param content
     * @param title
     * @param alwaysOnTop make the popup modal
     */
    public static void showPopup(JComponent parent, JPanel content, String title, boolean alwaysOnTop) {

        if (!contents.containsKey(content)) {
            final JDialog window = new JDialog(YabsViewProxy.instance().getIdentifierFrame(), Messages.YABS.getValue());
            window.getContentPane().setLayout(new BorderLayout());
            window.getContentPane().add(content, BorderLayout.CENTER);
            window.pack();
            window.setAlwaysOnTop(alwaysOnTop);
            window.setModal(alwaysOnTop);
//            if (alwaysOnTop) {
//                window.setModal(alwaysOnTop);
//            }
            window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
        } else {
            try {
                show(content);
                if (alwaysOnTop) {
                    setOnTop(content);
                }
            } catch (Exception exception) {
            }
        }
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
     *   @throws Exception If the panel is not known
     */
    public static void close(JPanel panel) throws Exception {
        if (contents.containsKey(panel)) {
            contents.get(panel).dispose();
            contents.remove(panel);
        } else {
            throw new Exception("Can not use a panel which has never been shown before.");
        }
    }

    /**
     * Pack the popup with the given content
     * @param panel
     *  @throws Exception If the panel is not known
     */
    public static void pack(JPanel panel) throws Exception {
        if (contents.containsKey(panel)) {
            contents.get(panel).pack();
        } else {
            throw new Exception("Can not use a panel which has never been shown before.");
        }
    }

    /**
     * Sets the popup with the given content on top
     * @param panel
     *  @throws Exception If the panel is not known
     */
    public static void setOnTop(JPanel panel) throws Exception {

        if (contents.containsKey(panel)) {
            //show(panel);
            contents.get(panel).setAlwaysOnTop(true);
            contents.get(panel).validate();
        } else {
            throw new Exception("Can not use a panel which has never been shown before.");
        }
    }

    /**
     * Hides the popup with the given content
     * @param panel
     *  @throws Exception If the panel is not known
     */
    public static void hide(JPanel panel) throws Exception {
        if (contents.containsKey(panel)) {
            contents.get(panel).setVisible(false);
        } else {
            throw new Exception("Can not use a panel which has never been shown before.");
        }
    }

    /**
     * Shows the popup with the given content
     * @param panel
     * @throws Exception If the panel is not known
     */
    public static void show(JPanel panel) throws Exception {
        if (contents.containsKey(panel)) {
            contents.get(panel).setVisible(true);
            contents.get(panel).validate();
        } else {
            throw new Exception("Can not use a panel which has never been shown before.");
        }
    }

    /**
     * Creates the popup with the given content, without showing it
     * @param content
     * @param title 
     */
    public static void create(JPanel content, String title) {
        final JDialog window = new JDialog(YabsViewProxy.instance().getIdentifierFrame(), Messages.YABS.getValue());
        window.getContentPane().setLayout(new BorderLayout());
        window.getContentPane().add(content, BorderLayout.CENTER);
        window.pack();

        window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
        Position position = new Position(window);
        contents.put(content, window);
    }

    public static void setLocation(JPanel panel, Point location) {
        if (contents.containsKey(panel)) {
            contents.get(panel).setLocation(location);
        }
    }
    
    public static void setLocationBottomLeft(JPanel panel) {
       if (contents.containsKey(panel)) {
           Position position = new Position(contents.get(panel));
           position.bottomLeft();
        } 
    }

    public static void setLocationBottomRight(JPanel panel) {
        if (contents.containsKey(panel)) {
           Position position = new Position(contents.get(panel));
           position.bottomRight();
        } 
    }
}
