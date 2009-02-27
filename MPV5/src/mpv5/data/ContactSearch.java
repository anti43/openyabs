/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.data;

import mpv5.db.common.Context;
import javax.swing.table.TableModel;
import mpv5.db.common.DatabaseSearch;
import mpv5.utils.models.MPTableModel;

/**
 *
 * @author anti43
 */
public class ContactSearch {

    public static final int NAMESEARCH = 0;
    public static final int NUMBERSEARCH = 1;
    public static final int DETAILSSEARCH = 2;
    public static final int CONTEXTSEARCH = 3;
    private Context context;
    private Integer searchtype = null;
    private Object[][] data;

    public ContactSearch(Context context, int searchtype) {
        this.context = context;
        this.searchtype = searchtype;
    }

    public ContactSearch(Context context) {
        this.context = context;
    }

    public Object[][] searchFor(String needle) {
        boolean like = false;


        if (needle == null || needle.length() < 1) {
            like = true;
        }

        switch (searchtype) {

            case NAMESEARCH:
                data = new DatabaseSearch(context).getValuesFor(context.getSearchFields(), Context.SEARCH_NAME, needle, like);
                break;

            case NUMBERSEARCH:
                data = new DatabaseSearch(context).getValuesFor(context.getSearchFields(), Context.SEARCH_CONTACT_NUMBER, needle, like);
                break;

            case DETAILSSEARCH:
                data = new DatabaseSearch(context).getValuesFor(context.getSearchFields(), Context.SEARCH_CONTACT_CITY, needle, like);
                break;

            case CONTEXTSEARCH:
                data = new DatabaseSearch(context).getValuesFor(context.getSearchFields(), Context.SEARCH_NAME, needle, like);
                break;

        }

        return data;
    }

    /**
     * 
     * @param needle
     * @return
     */
    public TableModel getTableModelFor(String needle) {
        if (data == null) {
            searchFor(needle);
        }
        return new MPTableModel(data, context.getSearchHeaders());
    }
}
