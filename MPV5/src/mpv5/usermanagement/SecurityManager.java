/*
 *  This file is part of MP by anti43 /GPL.
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
package mpv5.usermanagement;

import java.util.ArrayList;
import mpv5.db.common.Context;
import mpv5.ui.frames.MPV5View;

/**
 *
 * @author Andreas
 */
public class SecurityManager {

    public static final int RIGHT_TO_VIEW = 3;
    public static final int RIGHT_TO_EXPORT = 2;
    public static final int RIGHT_TO_EDIT = 1;
    public static final int RIGHT_TO_CREATE = 0;
    public static final int VIEW = 3;
    public static final int EXPORT = 2;
    public static final int EDIT = 1;
    public static final int CREATE = 0;
    public static ArrayList<Context> securedContexts = new ArrayList<Context>();

    public static Boolean check(Context context, int action) {
        for (Context item : securedContexts) {
            if (item.getDbIdentity().equals(context.getDbIdentity())) {

                if (MPV5View.getUser().getHighestRight() >= action) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }
        return null;
    }
}
