/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.ui.panels;

import mpv5.db.common.DatabaseObject;

/**
 *Represents a view for {@link DatabaseObject}s
 *  
 */
public interface DataPanel {

    /**
     * Collect the view data
     * @return 
     */
    public boolean collectData();

    /**
     * Get the database objec twhich owns the view
     * @return
     */
    public DatabaseObject getDataOwner();

    /**
     * Assign this view to a database object
     * @param object
     * @param populateData If true, the DOs data is populated into the view.
     */
    public void setDataOwner(DatabaseObject object, boolean populateData);

    /**
     * Reload the view from database, in background
     */
    public void refresh();

    /**
     * Populate the data to the view
     */
    public void exposeData();

    /**
     * Paste a dbo into this panel and let the panel decide what to do with it
     * @param dbo
     */
    public void paste(DatabaseObject... dbo);

    /**
     * Show the user the fields which are mandatory to fill in
     */
    public void showRequiredFields();

    /**
     * Show/hide the search panel for this view (if any)
     * @param show
     */
    public void showSearchBar(boolean show);

    /**
     * Call this after saving the dataowner of the panel
     */
    public void actionAfterSave();

    /**
     * Call this after creating a new dataowner on the panel
     */
    public void actionAfterCreate();

    /**
     * Call this before creating a new dataowner on the panel
     */
    public void actionBeforeCreate();

    /**
     * Call this before saving the dataowner of the panel
     * @throws ChangeNotApprovedException thrown if the user did not approve the change
     */
    public void actionBeforeSave() throws ChangeNotApprovedException;

    /**
     * Mail the current data
     */
    public void mail();

    /**
     * Print the current data
     */
    public void print();

    /**
     * Client property key for a banner component shown at the top of the view
     */
    public static final String PROPERTY_PLUGIN_BANNER = "yabs.pluginBanner";

    /**
     * Client property key for a badge component shown in the header of the view
     */
    public static final String PROPERTY_PLUGIN_BADGE = "yabs.pluginBadge";

    /**
     * Client property key indicating that the main content fields and deletion
     * of the record are locked by a plugin
     */
    public static final String PROPERTY_CONTENT_LOCKED = "yabs.contentLocked";

    /**
     * Client property key for the archived document file (e.g. the e-invoice)
     * of a record locked by a plugin
     */
    public static final String PROPERTY_LOCKED_DOCUMENT = "yabs.lockedDocument";

    /**
     * Shows or clears a banner component provided by a plugin at the top of
     * this view
     *
     * @param banner the banner component, or null to clear
     */
    public default void setPluginBanner(javax.swing.JComponent banner) {
        if (this instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) this).putClientProperty(PROPERTY_PLUGIN_BANNER, banner);
        }
    }

    /**
     * Shows or clears a status badge component provided by a plugin in the
     * header of this view
     *
     * @param badge the badge component, or null to clear
     */
    public default void setPluginBadge(javax.swing.JComponent badge) {
        if (this instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) this).putClientProperty(PROPERTY_PLUGIN_BADGE, badge);
        }
    }

    /**
     * Locks or unlocks the main content fields and positions of this view
     * (e.g. when a plugin marks a finalized document as immutable while still
     * allowing status updates)
     *
     * @param locked true to lock content fields and deletion
     */
    public default void setContentLocked(boolean locked) {
        if (this instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) this).putClientProperty(PROPERTY_CONTENT_LOCKED, Boolean.valueOf(locked));
        }
    }

    /**
     * @return true if a plugin locked the content of this view
     */
    public default boolean isContentLocked() {
        return this instanceof javax.swing.JComponent
                && Boolean.TRUE.equals(((javax.swing.JComponent) this).getClientProperty(PROPERTY_CONTENT_LOCKED));
    }

    /**
     * Sets the archived document file of a locked record (e.g. the e-invoice
     * a plugin archived when it locked the invoice). While the content is
     * locked, print, PDF, preview and e-invoice open this file instead of
     * creating a new one.
     *
     * @param file the archived file, or null to clear
     */
    public default void setLockedDocument(java.io.File file) {
        if (this instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) this).putClientProperty(PROPERTY_LOCKED_DOCUMENT, file);
        }
    }

    /**
     * @return the archived document file of the locked record, or null if the
     * content is not locked or no file was set
     */
    public default java.io.File getLockedDocument() {
        if (!isContentLocked() || !(this instanceof javax.swing.JComponent)) {
            return null;
        }
        Object f = ((javax.swing.JComponent) this).getClientProperty(PROPERTY_LOCKED_DOCUMENT);
        return f instanceof java.io.File ? (java.io.File) f : null;
    }

}
