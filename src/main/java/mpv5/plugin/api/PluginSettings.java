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
 * Key/value settings of a plugin, stored on this computer
 */
public interface PluginSettings {

    /**
     * @param key
     * @param fallback returned when the key is not set
     * @return the value
     */
    String get(String key, String fallback);

    /**
     * Sets and stores a value
     *
     * @param key
     * @param value null removes it
     */
    void set(String key, String value);

    default boolean getBoolean(String key, boolean fallback) {
        return Boolean.parseBoolean(get(key, String.valueOf(fallback)));
    }

    default void setBoolean(String key, boolean value) {
        set(key, String.valueOf(value));
    }
}
