
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
 * This interface describes a WS Daemon Job
 */
public interface WSDaemonJob {

    /**
     * Returns true if the job shall be run only once
     * @return
     */
    public boolean isOneTimeJob();

    /**
     * Returns true if the job is done
     * @return
     */
    public boolean isDone();

    /**
     * Do the actual work
     * @param client The client to use
     */
    public void work(WSConnectionClient client);
}


//~ Formatted by Jindent --- http://www.jindent.com
