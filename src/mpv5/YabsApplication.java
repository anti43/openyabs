/*
 * YabsApplication.java
 */
package mpv5;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import mpv5.db.objects.User;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 * Just a wrapper class to encapsulate SingleFrameApplication from yabs.Main.
 *
 * Replacing classes must do the same callbacks to main as this class.
 */
public class YabsApplication extends SingleFrameApplication {

    private final mpv5.Main main = new Main();

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        main.startup();
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     * @param root
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
//        getApplication().getMainFrame().setTitle(Main.WINDOW_TITLE);
        root.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                main.shutdown();
            }
        });
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of Main
     */
    public static Application getApplication() {
        return YabsApplication.getInstance(Application.class);
    }

    @Override
    public void ready() {
        Main.setLaF(User.getCurrentUser().__getLaf());
    }
}
