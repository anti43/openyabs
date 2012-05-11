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
package mpv5.db.objects;

import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Messages;
import mpv5.ui.frames.MPView;
import mpv5.db.objects.User;

/**
 *
 *  
 */
public class Favourite extends DatabaseObject {


    private int usersids;
    private int itemsids;

    public Favourite() {
        setContext(Context.getFavourite());
    }

    public Favourite(DatabaseObject dato) {
        setContext(Context.getFavourite());
        this.setUsersids(mpv5.db.objects.User.getCurrentUser().__getIDS());
        setCname(dato.getDbIdentity());
        setItemsids(dato.__getIDS());
    }



    /**
     * @return the userid
     */
    public int __getUsersids() {
        return usersids;
    }

    /**
     * @param userid the userid to set
     */
    public void setUsersids(int userid) {
        this.usersids = userid;
    }

    /**
     * @return the itemid
     */
    public int __getItemsids() {
        return itemsids;
    }

    /**
     * @param itemid the itemid to set
     */
    public void setItemsids(int itemid) {
        this.itemsids = itemid;
    }

    /**
     * Checks if the given do is in the favourites list of the current logged in user
     * @param dato
     * @return
     */
    public static boolean isFavourite(DatabaseObject dato) {
        if (!mpv5.db.common.QueryHandler.instanceOf().clone(Context.getFavourite()).
                checkConstraint(new String[]{"cname", "usersids", "itemsids"},
                    new Object[]{dato.getDbIdentity(), mpv5.db.objects.User.getCurrentUser().__getIDS(),dato.__getIDS()})) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if the given do is in the favourites list of the current logged in user
     * and removes it
     * @param dato
     */
    public static void removeFavourite(DatabaseObject dato) {
        
            mpv5.db.common.QueryHandler.instanceOf().clone(Context.getFavourite()).
                    delete(new String[]{"cname", "usersids", "itemsids"},
                    new Object[]{dato.getDbIdentity(), mpv5.db.objects.User.getCurrentUser().__getIDS(),dato.__getIDS()}, Messages.DONE.toString());
        
    }

    /**
     * 
     * @return The favourites list of the current logged in user
     */
    public static Favourite[] getUserFavourites() {

        Object[][] data = mpv5.db.common.QueryHandler.instanceOf().clone(Context.getFavourite()).select("cname, usersids, itemsids", new String[]{"usersids", mpv5.db.objects.User.getCurrentUser().__getIDS().toString(), ""});
        Favourite[] favs = new Favourite[data.length];

        for (int i = 0; i < favs.length; i++) {
            Favourite favi = new Favourite();
            favi.setCname(String.valueOf(data[i][0]));
            favi.setUsersids(Integer.valueOf(data[i][1].toString()));
            favi.setItemsids(Integer.valueOf(data[i][2].toString()));
            favs[i] = favi;
        }

        return favs;
    }


    /**
     * Returns the context of the do represented by this fav
     * @return
     */
    public Context getFavContext() {
       return Context.getMatchingContext(__getCname());
    }


    /**
     * Flushes the users favourites
     * @param user
     */
    public static void flush(User user) {
       mpv5.db.common.QueryHandler.instanceOf().clone(Context.getFavourite()).delete(new String[][]{{"usersids",user.__getIDS().toString(),""}},
               Messages.DONE.toString());
    }

    @Override
    public JComponent getView() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
     @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
    }
}
