package mpv5.db;

import mpv5.db.common.ConnectionTypeHandler;
import org.junit.jupiter.api.Test;

/**
 * A new installation on H2 with the real application code
 */
class AppOnH2Test extends AppOnDatabase {

    @Test
    void h2() throws Exception {
        newInstallationSaveAndLoad(ConnectionTypeHandler.H2_DRIVER, tmp.resolve("h2").toString(), "yabs", "", "");
    }
}
