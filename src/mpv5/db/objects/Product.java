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

import java.io.File;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.Formattable;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.Templateable;
import mpv5.globals.Messages;
import mpv5.handler.FormatHandler;
import mpv5.handler.MPEnum;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.panels.ProductPanel;
import mpv5.utils.images.MPIcon;
import mpv5.utils.numberformat.FormatNumber;

/**
 *
 *
 */
public class Product extends DatabaseObject implements Formattable, Templateable {

   /**
    * Returns a localized string represenation of the given product type
    *
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

   /**
    * Returns all possible Types
    *
    * @return
    */
   public static MPEnum[] getTypes() {
      MPEnum[] en = new MPEnum[2];
      en[0] = new MPEnum() {
         @Override
         public Integer getId() {
            return TYPE_PRODUCT;
         }

         @Override
         public String getName() {
            return getTypeString(TYPE_PRODUCT);
         }
      };
      en[1] = new MPEnum() {
         @Override
         public Integer getId() {
            return TYPE_SERVICE;
         }

         @Override
         public String getName() {
            return getTypeString(TYPE_SERVICE);
         }
      };

      return en;

   }
   private int taxids = 1;
   private int inttype = 0;
   private int manufacturersids = 0;
   private int suppliersids = 0;
   private int productgroupsids = 1;
   private int productlistsids = 0;
   private BigDecimal externalnetvalue = BigDecimal.ZERO;
   private BigDecimal internalnetvalue = BigDecimal.ZERO;
   private String description = "";
   private String cnumber = "";
   private String measure = "";
   private String url = "";
   private String ean = "";
   private String reference = "";//herstellernummer
   private String defaultimage = "";
   private BigDecimal stockvalue = BigDecimal.ZERO;
   private BigDecimal thresholdvalue = BigDecimal.ZERO;
   private int intinventorytype = 0;
   private FormatHandler formatHandler;

   public Product() {
      setContext(Context.getProduct());
   }

   @Override
   public mpv5.utils.images.MPIcon getIcon() {
      MPIcon mpi = null;
      if (defaultimage != null && defaultimage.length() > 0) {
         File file = QueryHandler.instanceOf().clone(Context.getFiles()).retrieveFile(defaultimage);
         if (file != null) {
            try {
               mpi = new MPIcon(file.toURI().toURL());
               return mpi.getScaledIcon(100, 100);
            } catch (Exception ex) {
               Log.Debug(ex);
            }
         }
      }
      return mpi;
   }

   @Override
   public JComponent getView() {
      ProductPanel p = new ProductPanel(getContext());
      return p;
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
   public BigDecimal __getExternalnetvalue() {
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
   public BigDecimal __getInternalnetvalue() {
      return internalnetvalue;
   }

   /**
    * @param internalnetvalue the internalnetvalue to set
    */
   public void setInternalnetvalue(BigDecimal internalnetvalue) {
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
      if ((cnumber == null || cnumber.length() == 0) || !QueryHandler.instanceOf().clone(Context.getProduct()).checkUniqueness("cnumber", cnumber)) {
         setCnumber(getFormatHandler().next());
      }
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
   public java.util.Map<String, Object> resolveReferences(java.util.Map<String, Object> map) {

      if (map.containsKey("productgroupsids")) {
         try {
            try {
               map.put("productgroup", DatabaseObject.getObject(Context.getProductGroup(), Integer.valueOf(map.get("productgroupsids").toString())));
               map.remove("productgroupsids");
            } catch (NodataFoundException ex) {
               map.put("productgroup", null);
               Log.Debug(this, ex.getMessage());
            }
         } catch (NumberFormatException numberFormatException) {
            //already resolved?
         }
      }

      if (map.containsKey("suppliersids")) {
         try {
            try {
               map.put("supplier", DatabaseObject.getObject(Context.getContact(), Integer.valueOf(map.get("suppliersids").toString())));
               map.remove("suppliersids");
            } catch (NodataFoundException ex) {
               map.put("supplier", null);
               Log.Debug(this, ex.getMessage());
            }
         } catch (NumberFormatException numberFormatException) {
            //already resolved?
         }
      }

      if (map.containsKey("manufacturersids")) {
         try {

            //if Integer.valueOf(map.get("manufacturersids")>0
            try {
               map.put("manufacturer", DatabaseObject.getObject(Context.getContact(), Integer.valueOf(map.get("manufacturersids").toString())));
               map.remove("manufacturersids");
            } catch (NodataFoundException ex) {
               map.put("manufacturer", null);
               Log.Debug(this, ex.getMessage());
            }
         } catch (NumberFormatException numberFormatException) {
            //already resolved?
         }
      }

      if (map.containsKey("taxids")) {
         try {
            map.put("tax", FormatNumber.formatPercent(Tax.getTaxValue(Integer.valueOf(map.get("taxids").toString()))));
            map.remove("taxids");
         } catch (NumberFormatException numberFormatException) {
            Log.Debug(numberFormatException);
         }
      }

      try {
         if (map.containsKey("inttype")) {
            map.put("type", getTypeString(Integer.valueOf(map.get("inttype").toString())));
            map.remove("inttype");
         }
      } catch (NumberFormatException numberFormatException) {
         //already resolved?
         Log.Debug(numberFormatException);
      }
      map.put("externalnetvalue", FormatNumber.formatDezimal(__getExternalnetvalue()));
      map.put("externalbrutvalue", FormatNumber.formatDezimal(__getExternalnetvalue().multiply(Tax.getTaxValue(__getTaxids()))));
      map.put("taxpercentvalue", Tax.getTaxValue(__getTaxids()));

      return super.resolveReferences(map);
   }

   @Override
   public void defineFormatHandler(FormatHandler handler) {
      formatHandler = handler;
   }

   /**
    * Creates products from subitems which contain the trackid identifier -
    * "tid#"
    *
    * @param saveModel
    * @param dataowner
    */
   public static void createProducts(List<SubItem> saveModel, Item dataowner) {
      Vector<String> refs = new Vector<String>();
      for (SubItem i : saveModel) {
         if (i.__getDescription().contains(TRACKID_END_IDENTIFIER) && i.__getDescription().contains(TRACKID_START_IDENTIFIER)) {
            try {
               Product p = new Product();
               p.setReference(i.__getDescription().substring(i.__getDescription().indexOf(TRACKID_START_IDENTIFIER) + TRACKID_START_IDENTIFIER.length(), i.__getDescription().indexOf(TRACKID_END_IDENTIFIER)));
               p.setCname(i.__getDescription().substring(i.__getDescription().indexOf(TRACKID_END_IDENTIFIER) + TRACKID_START_IDENTIFIER.length()));
               p.setExternalnetvalue(i.__getExternalvalue());
               p.setDescription(Messages.AUTO_GENERATED_VALUE.getValue());
               p.setInttype(Product.TYPE_SERVICE);
               p.setProductgroupsids(1);
               p.setGroupsids(i.__getGroupsids());
               p.setTaxids(Tax.getTaxId(i.__getTaxpercentvalue()));
               p.setMeasure(i.__getMeasure());
               p.setUrl(i.__getLinkurl());
               p.save(true);
               refs.add(p.__getReference());
               i.setDescription(i.__getDescription().replace(TRACKID_END_IDENTIFIER, " "));
               i.setDescription(i.__getDescription().replace(TRACKID_START_IDENTIFIER, ""));
               i.setCname(i.__getDescription().replace(TRACKID_END_IDENTIFIER, " "));
               i.setCname(i.__getDescription().replace(TRACKID_START_IDENTIFIER, ""));
               i.save(true);
            } catch (Exception e) {
               Log.Debug(e);
            }
         }
      }

      String r = "";
      for (int i = 0; i < refs.size(); i++) {
         String string = refs.get(i);
         if (!dataowner.__getDescription().contains(string)) {
            r += string + ", ";
         }
      }

      if (!r.equals("")) {
         dataowner.setDescription(dataowner.__getDescription() + "\n" + r.substring(0, r.length() - 2));
         dataowner.save(true);
      }
   }
   public static String TRACKID_START_IDENTIFIER = "tid#";
   public static String TRACKID_END_IDENTIFIER = "#tid";

   @Override
   public boolean saveImport() {

      if (__getProductgroupsids() <= 0 || !DatabaseObject.exists(Context.getProductGroup(), __getProductgroupsids())) {
         setProductgroupsids(1);
      }

      if (__getManufacturersids() <= 0 || !DatabaseObject.exists(Context.getManufacturer(), __getManufacturersids())) {
         setManufacturersids(0);
      }

      if (__getSuppliersids() <= 0 || !DatabaseObject.exists(Context.getSupplier(), __getSuppliersids())) {
         setSuppliersids(0);
      }

      return super.saveImport();
   }

   /**
    * @return the stockvalue
    */
   public BigDecimal __getStockvalue() {
      return stockvalue;
   }

   /**
    * @param stockvalue the stockvalue to set
    */
   public void setStockvalue(BigDecimal stockvalue) {
      this.stockvalue = stockvalue;
   }

   /**
    * @return the thresholdvalue
    */
   public BigDecimal __getThresholdvalue() {
      return thresholdvalue;
   }

   /**
    * @param thresholdvalue the thresholdvalue to set
    */
   public void setThresholdvalue(BigDecimal thresholdvalue) {
      this.thresholdvalue = thresholdvalue;
   }

   /**
    * @return the inventorytype
    */
   public int __getIntinventorytype() {
      return intinventorytype;
   }

   /**
    * @param inventorytype the inventorytype to set
    */
   public void setIntinventorytype(int inventorytype) {
      this.intinventorytype = inventorytype;
   }

   /**
    * @return the productlistsids
    */
   public int __getProductlistsids() {
      return productlistsids;
   }

   /**
    * @param productlistsids the productlistsids to set
    */
   public void setProductlistsids(int productlistsids) {
      this.productlistsids = productlistsids;
   }

   @Override
   public boolean save(boolean silent) {

      if (__getInttype() == TYPE_PRODUCT && __getIntinventorytype() == 1
            && __getStockvalue().intValue() <= __getThresholdvalue().intValue()) {
         Notificator.raiseNotification(Messages.INVENTORY_STOCK_TRESHOLD + toString(), true, true, this);
      }
      return super.save(silent);
   }

   @Override
   public int templateType() {
      return __getInttype();
   }

   /**
    *
    * @return
    */
   @Override
   public int templateGroupIds() {
      return __getGroupsids();
   }

   @Override
   public boolean delete() {
      List<ProductPrice> pp = findProductPrices();
      for (int i = 0; i < pp.size(); i++) {
         pp.get(i).delete();
      }
      return super.delete();
   }

   public List<ProductPrice> findProductPrices() {
      try {
         return getReferencedObjects(this, Context.getProductPrice(), new ProductPrice(), false);
      } catch (NodataFoundException ex) {
         Log.Debug(this, ex.getMessage());
      }
      return Collections.emptyList();
   }

   public BigDecimal findPriceFor(double amount) {
      BigDecimal res = BigDecimal.ZERO;
      BigDecimal am = BigDecimal.valueOf(amount);
      List<ProductPrice> pp = findProductPrices();
      Collections.sort(pp);
      for (ProductPrice p : pp) {
         if (am.compareTo(p.getMincountvalue()) > 0) {
            res = p.getExternalnetvalue();
         }
      }
      return res.compareTo(BigDecimal.ZERO) == 0 ? __getExternalnetvalue() : res;
   }

   @Persistable(false)
   public ProductGroup getProductgroup() {
      if (productgroupsids > 0) {
         try {
            return (ProductGroup) Product.getObject(Context.getProductGroup(), productgroupsids);
         } catch (NodataFoundException ex) {
            Logger.getLogger(SubItem.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
      return null;
   }

   @Persistable(false)
   public ProductList getProductlist() {
      if (productgroupsids > 0) {
         try {
            return (ProductList) Product.getObject(Context.getProductList(), productlistsids);
         } catch (NodataFoundException ex) {
            Logger.getLogger(SubItem.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
      return null;
   }
}
