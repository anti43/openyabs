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

}
