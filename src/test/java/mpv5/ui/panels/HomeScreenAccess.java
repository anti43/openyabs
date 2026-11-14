package mpv5.ui.panels;

/**
 * Package access to the dashboard's helpers for tests in other packages
 */
public final class HomeScreenAccess {

    private HomeScreenAccess() {
    }

    public static long daysOpen(java.util.Date from, java.util.Date today) {
        return HomeScreen.daysOpen(from, today);
    }
}
