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
package noa.connection;

import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.application.OfficeApplicationRuntime;
import ag.ion.bion.officelayer.desktop.IDesktopService;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.internal.document.DocumentService;
import ag.ion.noa.NOAException;
import com.sun.star.auth.InvalidArgumentException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class NoaConnection {

    public static final int TYPE_LOCAL = 0;
    public static final int TYPE_REMOTE = 1;
    private IOfficeApplication officeAplication;
    private int type = -1;
    private IDocumentService documentService;
    private IDesktopService desktopService;

    public NoaConnection() {
    }

    public NoaConnection(int type, String connectionString) throws Exception {
        switch (type) {
            case TYPE_LOCAL:
                createLocalConnection(connectionString);
                break;
            case TYPE_REMOTE:
                createServerConnection(connectionString.split(":")[0], Integer.valueOf(connectionString.split(":")[1]));
                break;
            default:
                throw new Exception("Connection not possible with the given parameters: " + type + " [" + connectionString + "]");
        }
    }

    public boolean createServerConnection(String host, int port) throws OfficeApplicationException, NOAException, InvalidArgumentException {
        if (host != null && port > 0) {
            Map<String, String> configuration = new HashMap<String, String>();
            configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY,
                    IOfficeApplication.REMOTE_APPLICATION);
            configuration.put(IOfficeApplication.APPLICATION_HOST_KEY, host);
            configuration.put(IOfficeApplication.APPLICATION_PORT_KEY, String.valueOf(port));

            officeAplication = OfficeApplicationRuntime.getApplication(configuration);
            officeAplication.setConfiguration(configuration);
            officeAplication.activate();
            documentService = officeAplication.getDocumentService();
            desktopService = officeAplication.getDesktopService();
            setType(TYPE_REMOTE);
        } else {
            throw new InvalidArgumentException("Host cannot be null and port must be > 0: " + host + ":" + port);
        }

        return true;
    }

    public boolean createLocalConnection(String OOOPath) throws OfficeApplicationException, NOAException, InvalidArgumentException {
        if (OOOPath != null) {
            Map<String, String> configuration = new HashMap<String, String>();
            configuration.put(IOfficeApplication.APPLICATION_HOME_KEY, OOOPath);
            configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY,
                    IOfficeApplication.LOCAL_APPLICATION);

            officeAplication = OfficeApplicationRuntime.getApplication(configuration);
            officeAplication.setConfiguration(configuration);
            officeAplication.activate();
            setType(TYPE_LOCAL);
        } else {
            throw new InvalidArgumentException("Path to OO cannot be null: " + OOOPath);
        }

        return true;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the documentService
     */
    public IDocumentService getDocumentService() {
        return documentService;
    }

    /**
     * @return the desktopService
     */
    public IDesktopService getDesktopService() {
        return desktopService;
    }
}
