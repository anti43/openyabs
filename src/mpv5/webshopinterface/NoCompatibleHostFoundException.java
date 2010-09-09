
/*
*  This file is part of MP.
*
*      MP is free software: you can redistribute it and/or modify
*      it under the terms of the GNU General Public License as published by
*      the Free Software Foundation, either version 3 of the License, or
*      (at your option) any later version.
*
*      MP is distributed in the hope that it will be useful,
*      but WITHOUT ANY WARRANTY; without even the implied warranty of
*      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*      GNU General Public License for more details.
*
*      You should have received a copy of the GNU General Public License
*      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.webshopinterface;

//~--- JDK imports ------------------------------------------------------------

import java.net.URL;

/**
 * This is thrown if the host is not reachable or does not reply with the expected version
 */
public class NoCompatibleHostFoundException extends Exception {

    /**
     * Throw this exception
     * @param host
     */
    public NoCompatibleHostFoundException(URL host) {
        super(host.toString());
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
