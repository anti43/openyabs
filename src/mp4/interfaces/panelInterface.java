/*
 * 
This file is part of MP by anti43 /GPL.

MP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with MP.  If not, see <http://www.gnu.org/licenses/>.

 * 
 */
package mp4.interfaces;

import mp4.items.People;
import mp4.items.Product;

/**
 *
 * @author anti43
 */
public interface panelInterface {

    /**
     *
     * To be called after a database change in the panels view scope
     */
    public abstract void update();

    /**
     *
     * To change the panels' contact
     * @param contact 
     */
    public abstract void setContact(People contact);

    /**
     * Yes, i know it's dirty..
     * To add a Product (if possible :-)
     * @param product 
     */
    public abstract void addProduct(Product product);
    /**
     * Yes, i know it's dirty.. do it better
     * 
     * @param steuersatz 
     */
    public abstract void setTax(mp4.items.Steuersatz steuersatz);

}
