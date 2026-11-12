package mpv5.db;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import mpv5.db.common.ConnectionTypeHandler;
import org.junit.jupiter.api.Test;

/**
 * A new installation on Postgres with the real application code; runs when
 * YABS_TEST_POSTGRES contains a JDBC URL (server, user and password are used, a new
 * database yabs_it_&lt;time&gt; is created)
 */
class AppOnPostgresTest extends AppOnDatabase {

    @Test
    void postgres() throws Exception {
        String url = System.getenv("YABS_TEST_POSTGRES");
        assumeTrue(url != null && !url.isEmpty(), "YABS_TEST_POSTGRES not set");
        URI u = URI.create(url.substring(5));
        Map<String, String> q = new HashMap<String, String>();
        for (String p : (u.getRawQuery() == null ? "" : u.getRawQuery()).split("&")) {
            int i = p.indexOf('=');
            if (i > 0) {
                q.put(p.substring(0, i), URLDecoder.decode(p.substring(i + 1), StandardCharsets.UTF_8));
            }
        }
        newInstallationSaveAndLoad(ConnectionTypeHandler.POSTGRES_DRIVER, u.getHost() + ":" + u.getPort(), "yabs_it_" + System.currentTimeMillis(),
                q.getOrDefault("user", ""), q.getOrDefault("password", ""));
    }
}
