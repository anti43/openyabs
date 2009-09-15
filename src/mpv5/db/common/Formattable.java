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
package mpv5.db.common;

import mpv5.handler.FormatHandler;

/**
 * Implement this interface if your DO shall have an automatic id format
 */
public interface Formattable {
    /**
     * The FormatHandler for this do
     * @return
     */
    public FormatHandler getFormatHandler();

    /**
     *
     * @param handler
     */
    public void defineFormatHandler(FormatHandler handler);
}
