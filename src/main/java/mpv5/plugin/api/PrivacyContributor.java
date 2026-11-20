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

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import mpv5.db.objects.Contact;

/**
 * Allows a plugin to participate in GDPR retention checks, Art. 15/20 data
 * exports and final deletion after retention periods expire
 * ({@link mpv5.handler.Privacy}).
 */
public interface PrivacyContributor {

    /**
     * Returns an additional retention deadline for the given contact (for
     * example, 10 years for medical treatment records under § 630f Abs. 3 BGB),
     * or {@code null} if this plugin imposes no retention requirement.
     *
     * @param contact the contact being checked
     * @return retention end date, or {@code null}
     */
    default Date retentionUntil(Contact contact) {
        return null;
    }

    /**
     * Returns additional personal data files (entry name -> UTF-8 JSON/text
     * content) to include in the Art. 15/20 GDPR export ZIP for the contact.
     *
     * @param contact the contact being exported
     * @return map of ZIP entry name to content (never null)
     */
    default Map<String, String> exportData(Contact contact) {
        return Collections.emptyMap();
    }

    /**
     * Called when a contact is permanently deleted after all retention periods
     * have expired ({@link mpv5.handler.Privacy#deleteFinally(Contact)}), so
     * the plugin can delete its own records for this contact.
     *
     * @param contact the contact being permanently deleted
     */
    default void onDeleteFinally(Contact contact) {
    }
}
