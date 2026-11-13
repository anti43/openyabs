package mpv5.db;

import mpv5.db.common.ConnectionTypeHandler;
import org.junit.jupiter.api.Test;

/**
 * A new installation on (embedded) Derby with the real application code
 */
class AppOnDerbyTest extends AppOnDatabase {

    @Test
    void derby() throws Exception {
        newInstallationSaveAndLoad(ConnectionTypeHandler.DERBY_DRIVER, tmp.resolve("derby").toString(), "yabs", "", "");
    }
}
