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
package mpv5.plugin.api;

import java.io.File;
import java.util.Collections;
import java.util.List;
import mpv5.db.common.DatabaseObject;

/**
 * Lets a plugin tell YaBS that a record is locked with an archived original
 * file (e.g. a finalized invoice and the PDF or e-invoice that was sent).
 * <p>
 * YaBS asks the providers when the user prints, creates a PDF, a preview, an
 * ODT or an e-invoice of a record: for a locked record no new file is created,
 * the archived original is opened. This does not depend on the view having
 * been refreshed after the record was locked.
 * <p>
 * If the archived file is missing on disk, YaBS offers a duplicate: a PDF
 * created from the stored data and stamped as a copy on every page. It never
 * replaces the original. The provider may refuse it (e.g. the data no longer
 * matches what was locked), name differences the user has to confirm, and
 * record the duplicate (e.g. in an audit log).
 */
public interface LockedDocumentProvider {

    /**
     * @param record the record of the view
     * @return the archived original if the record is locked (also if the file
     * is missing on disk), null if this provider does not lock it
     */
    File lockedDocument(DatabaseObject record);

    /**
     * @param record a locked record whose archived file is missing
     * @return the reason why no duplicate may be created, null to allow it
     */
    default String duplicateRefused(DatabaseObject record) {
        return null;
    }

    /**
     * @param record a locked record whose archived file is missing
     * @return differences between the stored data and the locked original
     * that the duplicate would show (e.g. a changed address of the customer);
     * the user has to confirm them; never null
     */
    default List<String> duplicateWarnings(DatabaseObject record) {
        return Collections.emptyList();
    }

    /**
     * Called after a duplicate was created
     *
     * @param record the locked record
     * @param missing the archived original that is missing
     * @param duplicate the stamped duplicate
     */
    default void duplicateCreated(DatabaseObject record, File missing, File duplicate) {
    }
}
