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

/**
 * Jobs for the WSDaemon
 */
public class WSDaemonJob {

    /**
     * Defines a getter job
     */
    public static final int TYPE_GET = 0;
    /**
     * Defines a setter job
     */
    public static final int TYPE_SET = 1;
    private final int type;
    private final String commandName;
    private final Object[] params;

    /**
     * Create a new job
     * @param type
     * @param commandName
     * @param params
     */
    public WSDaemonJob(int type, String commandName, Object[] params) {
        this.type = type;
        this.commandName = commandName;
        this.params = params;
    }

    public void work(WSConnectionClient client) {
        switch (type) {
            case TYPE_GET:

//                client.getClient().invokeGetCommand(commandName, params);

                break;
            case TYPE_SET:
                break;
        }
    }
}
