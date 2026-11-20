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
package org.openyabs.heilpraktiker;

import mpv5.db.objects.User;
import mpv5.usermanagement.MPSecurityManager;
import org.openyabs.heilpraktiker.HeilpraktikerStore.Settings;

/**
 * Access control for sensitive treatment documentation (SOAP notes under
 * § 630f BGB, Art. 9 GDPR and § 203 StGB professional secrecy).
 * <p>
 * When {@link Settings#restrictSoapAccess} is enabled (default), only
 * Administrators ({@link MPSecurityManager#RIGHT_TO_ADMINISTRATE}), users with
 * the user property {@value #KEY_USER_SOAP_ROLE} set to {@code true}, or
 * usernames explicitly listed in {@link Settings#soapAllowedUsers} may view or
 * edit SOAP notes.
 */
public final class HeilpraktikerSecurity {

    /**
     * Per-user property key granting access to SOAP treatment notes
     */
    public static final String KEY_USER_SOAP_ROLE = "hp.role.soap";

    private HeilpraktikerSecurity() {
    }

    /**
     * @return {@code true} if the currently logged-in YaBS user may view and
     * edit SOAP treatment notes
     */
    public static boolean canAccessSoapNotes(Settings settings) {
        try {
            return canAccessSoapNotes(User.getCurrentUser(), settings);
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * @return {@code true} if the given YaBS user may view and edit SOAP
     * treatment notes
     */
    public static boolean canAccessSoapNotes(User user, Settings settings) {
        if (settings == null || !settings.restrictSoapAccess) {
            return true;
        }
        if (user == null) {
            return false;
        }
        boolean hasRole = false;
        try {
            String prop = user.getProperty(KEY_USER_SOAP_ROLE);
            hasRole = Boolean.parseBoolean(prop);
        } catch (Throwable ignored) {
        }
        return canAccessSoapNotes(user.__getCname(), user.__getInthighestright(), hasRole, settings);
    }

    /**
     * Pure, testable access-control check for SOAP notes.
     *
     * @param username the user's login name
     * @param highestRight the user's {@link MPSecurityManager} right level
     * (0 = Administrator, higher = less privileged)
     * @param hasUserSoapRole whether the user has {@value #KEY_USER_SOAP_ROLE}
     * @param settings current Heilpraktiker plugin settings
     * @return {@code true} if SOAP notes access is permitted
     */
    public static boolean canAccessSoapNotes(String username, int highestRight, boolean hasUserSoapRole, Settings settings) {
        if (settings == null || !settings.restrictSoapAccess) {
            return true;
        }
        if (highestRight <= MPSecurityManager.RIGHT_TO_ADMINISTRATE) {
            return true;
        }
        if (hasUserSoapRole) {
            return true;
        }
        if (username != null && !username.trim().isEmpty() && settings.soapAllowedUsers != null) {
            String target = username.trim();
            for (String token : settings.soapAllowedUsers.split("[,;\\s]+")) {
                if (!token.isEmpty() && token.equalsIgnoreCase(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return {@code true} if the current YaBS user is an Administrator
     */
    public static boolean isCurrentUserAdmin() {
        try {
            User u = User.getCurrentUser();
            return u != null && u.__getInthighestright() <= MPSecurityManager.RIGHT_TO_ADMINISTRATE;
        } catch (Throwable t) {
            return false;
        }
    }
}
