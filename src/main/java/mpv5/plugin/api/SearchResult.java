/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.plugin.api;

/**
 * A result of a plugin in Search anything
 */
public final class SearchResult {

    public final String title;
    public final String detail;
    public final Runnable open;

    /**
     * @param title
     * @param detail a gray line, may be empty
     * @param open what happens on Enter
     */
    public SearchResult(String title, String detail, Runnable open) {
        this.title = title;
        this.detail = detail == null ? "" : detail;
        this.open = open;
    }
}
