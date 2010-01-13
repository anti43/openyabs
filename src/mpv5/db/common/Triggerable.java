

package mpv5.db.common;

/**
 * Use this interface to trigger events after a create/update/delete on a {@link DatabaseObject}
 * @author anti
 */
public interface Triggerable {

    /**
     * This method is triggered after the creation of the object implementing this interface
     */
    public void triggerOnCreate();
    /**
     * This method is triggered after an update of the object implementing this interface
     */
    public void triggerOnUpdate();
    /**
     * This method is triggered after the deletion of the object implementing this interface
     */
    public void triggerOnDelete();
}
