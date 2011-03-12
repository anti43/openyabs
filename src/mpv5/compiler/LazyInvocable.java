/**
 *  This file is part of YaBS-UI.
 *
 *      YaBS-UI is free software: you can redistribute it and/or modify
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
 *      along with YaBS-UI.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.compiler;

import mpv5.db.common.DatabaseObject;

/**
 *
 * @author anti
 */
public interface LazyInvocable {
    public String getScript();
    public void setScript(String script);
    public String doIt(DatabaseObject dbo);
}
