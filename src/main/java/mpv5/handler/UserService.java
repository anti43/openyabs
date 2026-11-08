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
package mpv5.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mpv5.globals.Messages;
import mpv5.usermanagement.MPSecurityManager;

/**
 * Rules of the user administration, without Swing: required fields, the
 * password and who may be deleted
 */
public final class UserService {

    /**
     * Minimal length of a new password
     */
    public static final int MIN_PASSWORD = 4;

    private UserService() {
    }

    /**
     * A user as the rules need it
     */
    public static final class Account {

        final int id;
        final String login;
        final int role;
        final boolean enabled;

        /**
         * @param id database id, &lt;= 0 for a new user
         * @param login
         * @param role MPSecurityManager.RIGHT_TO_*
         * @param enabled
         */
        public Account(int id, String login, int role, boolean enabled) {
            this.id = id;
            this.login = login;
            this.role = role;
            this.enabled = enabled;
        }

        boolean isEnabledAdmin() {
            return enabled && role <= MPSecurityManager.RIGHT_TO_ADMINISTRATE;
        }
    }

    /**
     * Checks the form before saving
     *
     * @param user the user as entered
     * @param password the new password, empty: keep the current one (only for
     * existing users)
     * @param repeat the repetition of the new password
     * @param others the other users (for the unique login and the last
     * administrator)
     * @return the problems, empty if it can be saved
     */
    public static List<String> check(Account user, char[] password, char[] repeat, List<Account> others) {
        List<String> problems = new ArrayList<String>();
        String login = user.login == null ? "" : user.login.trim();
        if (login.isEmpty()) {
            problems.add(Messages.USERS_LOGIN_MISSING.toString());
        } else if (!login.matches("[^\\s'\"]+")) {
            problems.add(Messages.USERS_LOGIN_INVALID.toString());
        }
        for (Account o : others) {
            if (o.id != user.id && o.login != null && o.login.trim().equalsIgnoreCase(login)) {
                problems.add(String.format(Messages.USERS_LOGIN_TAKEN.toString(), login));
            }
        }
        boolean newPassword = password != null && password.length > 0;
        if (user.id <= 0 && !newPassword) {
            problems.add(Messages.USERS_PASSWORD_MISSING.toString());
        }
        if (newPassword) {
            if (password.length < MIN_PASSWORD) {
                problems.add(String.format(Messages.USERS_PASSWORD_SHORT.toString(), MIN_PASSWORD));
            }
            if (repeat == null || !Arrays.equals(password, repeat)) {
                problems.add(Messages.USERS_PASSWORD_MISMATCH.toString());
            }
        }
        //at least one administrator who can log in must remain
        if (!user.isEnabledAdmin() && user.id > 0) {
            boolean otherAdmin = false;
            for (Account o : others) {
                if (o.id != user.id && o.isEnabledAdmin()) {
                    otherAdmin = true;
                }
            }
            if (!otherAdmin) {
                problems.add(Messages.USERS_LAST_ADMIN.toString());
            }
        }
        return problems;
    }

    /**
     * @param user the user to delete
     * @param currentUserId the user who is logged in
     * @param others the other users
     * @return why the user can not be deleted, null if it can
     */
    public static String whyNotDelete(Account user, int currentUserId, List<Account> others) {
        if (user.id <= 0) {
            return Messages.USERS_NOT_SAVED.toString();
        }
        if (user.id == currentUserId) {
            return Messages.USERS_DELETE_SELF.toString();
        }
        if (user.isEnabledAdmin()) {
            for (Account o : others) {
                if (o.id != user.id && o.isEnabledAdmin()) {
                    return null;
                }
            }
            return Messages.USERS_LAST_ADMIN.toString();
        }
        return null;
    }
}
