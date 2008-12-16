/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.data;

import javax.swing.table.DefaultTableModel;
import mpv5.db.common.Context;
import javax.swing.table.TableModel;
import mpv5.db.common.DatabaseSearch;
import mpv5.logging.Log;
import mpv5.utils.models.MPTableModel;

/**
 *
 * @author Administrator
 */
public class Search {

    public static final int NAMESEARCH = 0;
    public static final int NUMBERSEARCH = 1;
    public static final int DETAILSSEARCH = 2;
    public static final int CONTEXTSEARCH = 3;
    private Context context;
    private int searchtype;
    private Object[][] data;

    public Search(Context context, int searchtype) {
        this.context = context;
        this.searchtype = searchtype;
    }

    public Object[][] searchFor(String needle) {
        boolean like = false;


        if(needle == null || needle.length()<1){like = true;}

        switch (searchtype) {

            case NAMESEARCH:
                data = new DatabaseSearch(context).getValuesFor(context.getDefaultSearchFields(), Context.SEARCH_NAME, needle, like);
                break;

            case NUMBERSEARCH:
                data = new DatabaseSearch(context).getValuesFor(context.getDefaultSearchFields(), Context.SEARCH_NUMBER, needle, like);
                break;

            case DETAILSSEARCH:
                data = new DatabaseSearch(context).getValuesFor(context.getDefaultSearchFields(), Context.SEARCH_DETAILS, needle, like);
                break;

            case CONTEXTSEARCH:
                data = new DatabaseSearch(context).getValuesFor(context.getDefaultSearchFields(), Context.SEARCH_NAME, needle, like);
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
        Log.PrintArray(data);
        return new MPTableModel(data, context.getDefaultSearchHeaders());
    }
}
