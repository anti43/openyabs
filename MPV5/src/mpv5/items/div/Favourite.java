/*
 *  This file is part of MP by anti43 /GPL.
 *
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.items.div;

import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Messages;
import mpv5.ui.frames.MPV5View;
import mpv5.usermanagement.User;

/**
 *
 * @author anti
 */
public class Favourite extends DatabaseObject {


    private int userid;
    private int itemid;

    public Favourite() {
        context.setDbIdentity(Context.SMALLIDENTITY_FAVS);
    }

    public Favourite(DatabaseObject dato) {
        context.setDbIdentity(Context.SMALLIDENTITY_FAVS);
        this.setUserid(MPV5View.getUser().__getIDS());
        setCName(dato.getDbIdentity());
        setItemid(dato.__getIDS());
    }

    /**
     * @return the name
     */
    public String __getCName() {
        return cname;
    }


    /**
     * @param name the name to set
     */
    public void setCName(String name) {
        this.cname = name;
    }

    /**
     * @return the userid
     */
    public int __getUserid() {
        return userid;
    }

    /**
     * @param userid the userid to set
     */
    public void setUserid(int userid) {
        this.userid = userid;
    }

    /**
     * @return the itemid
     */
    public int __getItemid() {
        return itemid;
    }

    /**
     * @param itemid the itemid to set
     */
    public void setItemid(int itemid) {
        this.itemid = itemid;
    }

    /**
     * Checks if the given do is in the favourites list of the current logged in user
     * @param dato
     * @return
     */
    public static boolean isFavourite(DatabaseObject dato) {
        if (!mpv5.db.common.QueryHandler.instanceOf().clone(Context.getFavourites()).
                checkConstraint(new String[]{"cname", "usersids", "itemsids"},
                    new Object[]{dato.getDbIdentity(), MPV5View.getUser().__getIDS(),dato.__getIDS()})) {
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
        
            mpv5.db.common.QueryHandler.instanceOf().clone(Context.getFavourites()).
                    delete(new String[]{"cname", "usersids", "itemsids"},
                    new Object[]{dato.getDbIdentity(), MPV5View.getUser().__getIDS(),dato.__getIDS()}, Messages.DONE);
        
    }

    /**
     * 
     * @return The favourites list of the current logged in user
     */
    public static Favourite[] getUserFavourites() {

        Object[][] data = mpv5.db.common.QueryHandler.instanceOf().clone(Context.getFavourites()).select("cname, usersids, itemsids", new String[]{"usersids", MPV5View.getUser().__getIDS().toString(), ""});
        Favourite[] favs = new Favourite[data.length];

        for (int i = 0; i < favs.length; i++) {
            Favourite favi = new Favourite();
            favi.setCName(String.valueOf(data[i][0]));
            favi.setUserid(Integer.valueOf(data[i][1].toString()));
            favi.setItemid(Integer.valueOf(data[i][2].toString()));
            favs[i] = favi;
        }

        return favs;
    }


    /**
     * Returns the context of the do represented by this fav
     * @return
     */
    public Context getFavContext() {
       return Context.getMatchingContext(__getCName());
    }


    /**
     * Flushes the users favourites
     * @param user
     */
    public static void flush(User user) {
       mpv5.db.common.QueryHandler.instanceOf().clone(Context.getFavourites()).delete(new String[][]{{"userid",user.__getIDS().toString(),""}},
               Messages.DONE);
    }
}
