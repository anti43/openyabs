/*
 *  This file is part of MP.
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
package mpv5.handler;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.logging.Log;
import mpv5.ui.frames.MPV5View;

/**
 *
 */
public class FormatHandler {

    public static final int TYPE_BILL = 0;
    public static final int TYPE_OFFER = 1;
    public static final int TYPE_ORDER = 2;
    public static final int TYPE_CONTACT = 3;
    public static final int TYPE_CUSTOMER = 4;
    public static final int TYPE_MANUFACTURER = 5;
    public static final int TYPE_SUPPLIER = 6;
    public static final int TYPE_PRODUCT = 7;
    public static final int TYPE_SERVICE = 8;
    private int type;
    private java.text.NumberFormat format;

    /**
     * Determines the format type of the given {@link DatabaseObject}
     * @param obj
     * @return An int value representing the format type
     */
    public static int determineType(DatabaseObject obj) {
        if (obj instanceof Item) {
            if (((Item) obj).__getInttype() == Item.TYPE_BILL) {
                return TYPE_BILL;
            } else if (((Item) obj).__getInttype() == Item.TYPE_OFFER) {
                return TYPE_OFFER;
            } else if (((Item) obj).__getInttype() == Item.TYPE_ORDER) {
                return TYPE_ORDER;
            }
        } else if (obj instanceof Contact) {
            if (((Contact) obj).__getIscustomer()) {
                return TYPE_CUSTOMER;
            } else if (((Contact) obj).__getIsmanufacturer()) {
                return TYPE_MANUFACTURER;
            } else if (((Contact) obj).__getIssupplier()) {
                return TYPE_SUPPLIER;
            } else {
                return TYPE_CONTACT;
            }
        } else if (obj instanceof Product) {
            if (((Product) obj).__getInttype() == Product.TYPE_PRODUCT) {
                return TYPE_PRODUCT;
            } else {
                return TYPE_SERVICE;
            }
        }
        return -1;
    }

    /**
     *
     * @param forObject
     */
    public FormatHandler(DatabaseObject forObject) {
        this.type = determineType(forObject);
    }

    /**
     *
     * @return
     */
    public NumberFormat getFormat() {
        QueryCriteria c = new QueryCriteria();
        c.add("usersids", MPV5View.getUser().__getIDS());
        c.add("inttype", this.type);
        try {
            Object[][] frm = QueryHandler.instanceOf().clone(Context.getFormats()).select("cname", c);
            return new DecimalFormat(frm[frm.length][0].toString());
        } catch (Exception ex) {
            Log.Debug(this, "Format not found, using default format instead!");
            return NumberFormat.getInstance();
        }
    }

    /**
     *
     * @param number
     * @return
     */
    public String toString(Object number) {
        return format.format(number);
    }
}
