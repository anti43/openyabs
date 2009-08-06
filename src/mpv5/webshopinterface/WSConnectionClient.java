/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.webshopinterface;

import java.io.IOException;
import java.net.URL;
import mpv5.logging.Log;
import mpv5.utils.http.HttpClient;
import org.apache.http.*;

/**
 *This class acts as connector to webshops using the WebShopAPI specified at
 * http://code.google.com/p/mp-rechnungs-und-kundenverwaltung/wiki/WebShopInterfaceSpecs
 */
public class WSConnectionClient {

    private HttpClient client;
    private final String toHost;
    public static String APPLICATION_FILE = "yabswsi.php";

    /**
     * Create a new connection to the specified url
     * @param toHost
     */
    public WSConnectionClient(final String toHost) {
        try {
            connect(toHost);
        } catch (Exception ex) {
            Log.Debug(ex);
        }
        this.toHost = toHost;
    }

    private void connect(String host) throws IOException, HttpException {
        client = new HttpClient(host, 80);
        HttpResponse req = client.request(APPLICATION_FILE + "?user=mpadmin&token=md5hash&keep-alive");



    }
}
