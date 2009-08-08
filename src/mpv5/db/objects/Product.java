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

import java.util.Date;
import java.util.HashMap;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.Formattable;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryHandler;
import mpv5.globals.Messages;
import mpv5.handler.FormatHandler;
import mpv5.logging.Log;
import mpv5.utils.numberformat.FormatNumber;

/**
 *
 *  
 */
public class Product extends DatabaseObject implements Formattable {

    /**
     * Returns a localized string represenation of the given product type
     * @param type
     * @return
     */
    public static String getTypeString(int type) {
        switch (type) {
            case (TYPE_PRODUCT):
                return Messages.TYPE_PRODUCT.toString();
            case (TYPE_SERVICE):
                return Messages.TYPE_SERVICE.toString();
        }
        return null;
    }
    private int taxids;
    private int inttype;
    private int manufacturersids;
    private int suppliersids;
    private int productgroupsids;
    private double externalnetvalue;
    private double internalnetvalue;
    private String description = "";
    private String cnumber = "";
    private String measure = "";
    private String url = "";
    private String ean = "";
    private String reference = "";//herstellernummer
    private String defaultimage = "";
    public static final int TYPE_PRODUCT = 0;
    public static final int TYPE_SERVICE = 1;
    private FormatHandler formatHandler;

    public Product() {
        context.setDbIdentity(Context.IDENTITY_PRODUCTS);
        context.setIdentityClass(Product.class);
    }

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
    }

    @Override
    public JComponent getView() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the inttaxids
     */
    public int __getTaxids() {
        return taxids;
    }

    /**
     * @param inttaxids the inttaxids to set
     */
    public void setTaxids(int inttaxids) {
        this.taxids = inttaxids;
    }

    /**
     * @return the inttype
     */
    public int __getInttype() {
        return inttype;
    }

    /**
     * @param inttype the inttype to set
     */
    public void setInttype(int inttype) {
        this.inttype = inttype;
    }

    /**
     * @return the externalnetvalue
     */
    public double __getExternalnetvalue() {
        return externalnetvalue;
    }

    /**
     * @param externalnetvalue the externalnetvalue to set
     */
    public void setExternalnetvalue(double externalnetvalue) {
        this.externalnetvalue = externalnetvalue;
    }

    /**
     * @return the internalnetvalue
     */
    public double __getInternalnetvalue() {
        return internalnetvalue;
    }

    /**
     * @param internalnetvalue the internalnetvalue to set
     */
    public void setInternalnetvalue(double internalnetvalue) {
        this.internalnetvalue = internalnetvalue;
    }

    /**
     * @return the description
     */
    public String __getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the cnumber
     */
    public String __getCnumber() {
        return cnumber;
    }

    /**
     * @param cnumber the cnumber to set
     */
    public void setCnumber(String cnumber) {
        this.cnumber = cnumber;
    }

    /**
     * @return the measure
     */
    public String __getMeasure() {
        return measure;
    }

    /**
     * @param measure the measure to set
     */
    public void setMeasure(String measure) {
        this.measure = measure;
    }

    /**
     * @return the url
     */
    public String __getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the ean
     */
    public String __getEan() {
        return ean;
    }

    /**
     * @param ean the ean to set
     */
    public void setEan(String ean) {
        this.ean = ean;
    }

    /**
     * @return the reference
     */
    public String __getReference() {
        return reference;
    }

    /**
     * @param reference the reference to set
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * @return the formatHandler
     */
    @Override
    public FormatHandler getFormatHandler() {
        if (formatHandler == null) {
            formatHandler = new FormatHandler(this);
        }
        return formatHandler;
    }

    @Override
    public void ensureUniqueness() {
        setCnumber(getFormatHandler().toString(getFormatHandler().getNextNumber()));
    }

    /**
     * @return the manufacturersids
     */
    public int __getManufacturersids() {
        return manufacturersids;
    }

    /**
     * @param manufacturersids the manufacturersids to set
     */
    public void setManufacturersids(int manufacturersids) {
        this.manufacturersids = manufacturersids;
    }

    /**
     * @return the suppliersids
     */
    public int __getSuppliersids() {
        return suppliersids;
    }

    /**
     * @param suppliersids the suppliersids to set
     */
    public void setSuppliersids(int suppliersids) {
        this.suppliersids = suppliersids;
    }

    /**
     * @return the defaultimage
     */
    public String __getDefaultimage() {
        return defaultimage;
    }

    /**
     * @param defaultimage the defaultimage to set
     */
    public void setDefaultimage(String defaultimage) {
        this.defaultimage = defaultimage;
    }

    /**
     * @return the productgroupsids
     */
    public int __getProductgroupsids() {
        return productgroupsids;
    }

    /**
     * @param productgroupsids the productgroupsids to set
     */
    public void setProductgroupsids(int productgroupsids) {
        this.productgroupsids = productgroupsids;
    }

    @Override
    public HashMap<String, Object> resolveReferences(HashMap<String, Object> map) {
        super.resolveReferences(map);

        if (map.containsKey("productgroupsids")) {
            try {
                try {
                    map.put("productgroup", DatabaseObject.getObject(Context.getContact(), Integer.valueOf(map.get("productgroupsids").toString())));
                } catch (NodataFoundException ex) {
                    map.put("productgroup", "N/A");
                    Log.Debug(ex);
                }
            } catch (NumberFormatException numberFormatException) {
                //already resolved?
            }
        }

        if (map.containsKey("suppliersids")) {
            try {
                try {
                    map.put("supplier", DatabaseObject.getObject(Context.getContact(), Integer.valueOf(map.get("suppliersids").toString())));
                } catch (NodataFoundException ex) {
                    map.put("supplier", "N/A");
                    Log.Debug(ex);
                }
            } catch (NumberFormatException numberFormatException) {
                //already resolved?
            }
        }

        if (map.containsKey("manufacturersids")) {
            try {
                try {
                    map.put("manufacturer", DatabaseObject.getObject(Context.getContact(), Integer.valueOf(map.get("manufacturersids").toString())));
                } catch (NodataFoundException ex) {
                    map.put("manufacturer", "N/A");
                    Log.Debug(ex);
                }
            } catch (NumberFormatException numberFormatException) {
                //already resolved?
            }
        }

        if (map.containsKey("taxids")) {
            try {
                map.put("tax", FormatNumber.formatPercent(Item.getTaxValue(Integer.valueOf(map.get("taxids").toString()))));
            } catch (NumberFormatException numberFormatException) {
                Log.Debug(numberFormatException);
            }
        }

        try {
            if (map.containsKey("inttype")) {
                map.put("type", getTypeString(Integer.valueOf(map.get("inttype").toString())));
            }
        } catch (NumberFormatException numberFormatException) {
            //already resolved?
            Log.Debug(numberFormatException);
        }

        return map;
    }
}
