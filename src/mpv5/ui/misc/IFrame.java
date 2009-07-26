
package mpv5.ui.misc;

import java.awt.BorderLayout;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 *
 * 
 */
public class IFrame extends JInternalFrame {

    public IFrame() {
       super("", true, true, true, true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        addInternalFrameListener(new InternalFrameListener() {

            public void internalFrameOpened(InternalFrameEvent e) {

            }

            public void internalFrameClosing(InternalFrameEvent e) {
               System.out.println("Closing " + getTitle());
               dispose();
            }

            public void internalFrameClosed(InternalFrameEvent e) {

            }

            public void internalFrameIconified(InternalFrameEvent e) {

            }

            public void internalFrameDeiconified(InternalFrameEvent e) {

            }

            public void internalFrameActivated(InternalFrameEvent e) {

            }

            public void internalFrameDeactivated(InternalFrameEvent e) {

            }
        });
    }

    public IFrame(String title) {
        super(title, true, true, true, true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        addInternalFrameListener(new InternalFrameListener() {

            public void internalFrameOpened(InternalFrameEvent e) {

            }

            public void internalFrameClosing(InternalFrameEvent e) {
               System.out.println("Closing " + getTitle());
               dispose();
            }

            public void internalFrameClosed(InternalFrameEvent e) {

            }

            public void internalFrameIconified(InternalFrameEvent e) {

            }

            public void internalFrameDeiconified(InternalFrameEvent e) {

            }

            public void internalFrameActivated(InternalFrameEvent e) {

            }

            public void internalFrameDeactivated(InternalFrameEvent e) {

            }
        });

    }



    public void setContent(JPanel panel){
        this.add(panel, BorderLayout.CENTER);
        this.pack();
        this.setVisible(true);
    }
}
