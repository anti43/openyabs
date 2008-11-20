/*
 * MPV5App.java
 */

package mpv5;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class MPV5App extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        show(new MPV5View(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of MPV5App
     */
    public static MPV5App getApplication() {
        return Application.getInstance(MPV5App.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(MPV5App.class, args);
    }
}
