package mpv5.utils.print;

import java.io.File;

/**
 * Package access for tests in other packages
 */
public final class PrintJob2Access {

    private PrintJob2Access() {
    }

    public static String jobName(File f) {
        return PrintJob2.jobName(f);
    }
}
