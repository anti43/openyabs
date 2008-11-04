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

package mp4.interfaces;

import mp4.items.Dienstleistung;
import mp4.items.People;
import mp4.items.Product;
import mp4.items.Steuersatz;


/**
 *
 * @author anti43
 */
public interface DataPanel {

    public void setProduct(Dienstleistung dienstleistung);

    public void setProduct(Product product);
    
    public void setContact(People contact);

    public void setTax(Steuersatz steuersatz);

    public void update();
}
