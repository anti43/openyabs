/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.ui.panels;

import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Item;

/**
 *
 * @author anti
 */
public class ChangeNotApprovedException extends Exception {

    private final DatabaseObject dataOwner;

    /**
     * This exception is thrown if the change of the given dataset is not confirmed by the user
     * @param dataOwner
     */
    public ChangeNotApprovedException(DatabaseObject dataOwner) {
        super("Change of " + dataOwner + " not approved by the user/ configuration.");
        this.dataOwner = dataOwner;
    }
}
