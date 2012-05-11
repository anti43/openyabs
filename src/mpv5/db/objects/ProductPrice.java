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

import java.math.BigDecimal;
import javax.swing.JComponent;

import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.ui.panels.ProductPanel;

/**
 *
 * 
 */
public class ProductPrice extends DatabaseObject {

    private static final long serialVersionUID = 1L;
    private Product product;
    private BigDecimal externalnetvalue;
    private BigDecimal internalnetvalue;
    private BigDecimal mincountvalue;

    public ProductPrice() {
        setContext(Context.getProductPrice());
        setCname("ProductPrice@" + IDENTITY);
    }

    @Override
    public JComponent getView() {
        return new ProductPanel(getProduct());
    }

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
    }

    /**
     * @return the product
     */
    @Persistable(true)
    public Product getProduct() {
        return product;
    }

    /**
     * @param product the product to set
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * @return the externalnetvalue
     */
    @Persistable(true)
    public BigDecimal getExternalnetvalue() {
        return externalnetvalue;
    }

    /**
     * @param externalnetvalue the externalnetvalue to set
     */
    public void setExternalnetvalue(BigDecimal externalnetvalue) {
        this.externalnetvalue = externalnetvalue;
    }

    /**
     * @return the internalnetvalue
     */
    @Persistable(true)
    public BigDecimal getInternalnetvalue() {
        return internalnetvalue;
    }

    /**
     * @param internalnetvalue the internalnetvalue to set
     */
    public void setInternalnetvalue(BigDecimal internalnetvalue) {
        this.internalnetvalue = internalnetvalue;
    }

    /**
     * @return the mincountvalue
     */
    @Persistable(true)
    public BigDecimal getMincountvalue() {
        return mincountvalue;
    }

    /**
     * @param mincountvalue the mincountvalue to set
     */
    public void setMincountvalue(BigDecimal mincountvalue) {
        this.mincountvalue = mincountvalue;
    }

    @Override
    public boolean save(boolean silent) {
        setCname("Productprice for " + getProduct() + "@" + mincountvalue);
        return super.save(silent);
    }
}
