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
package mpv5.db.sample;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import javax.swing.SwingUtilities;
import mpv5.db.common.Context;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.SubItem;
import mpv5.globals.Constants;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.RandomDate;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.numberformat.FormatNumber;

/**
 *
 *  
 */
public class SampleData {

    private int multiplier;

    /**
     *
     * @param factor
     * @param defAccountID
     * @param withItems
     * @param withProducts
     */
    public SampleData(final int factor, final int defAccountID, final boolean withItems, final boolean withProducts) {
        this.multiplier = factor;

        Group g = new Group();
        g.setDescription("This group hold sample data");
        g.setCname("Sample Group" + new Random().nextInt(factor * 43) + 1);
        g.save();
        final int group = g.__getIDS();


        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < factor * 6; i++) {
                    int seed = new Random().nextInt(factor * 43) + 1;

                    Contact c = new Contact();
                    c.setCname("Contact " + seed);
                    c.setCNumber("SC" + seed);
                    c.setCity("Sample City");
                    c.setCompany("Sample Company");
                    c.setCountry("Germany");
                    c.setDepartment("Billing");
                    c.setFax("00 ");
                    c.setMailaddress(i + "@www.com");
                    c.setMainphone("00 ");
                    c.setMobilephone("00 ");
                    c.setNotes("No notes at all.");
                    c.setPrename("Michael");
                    c.setStreet("Sample Street Nr. " + seed);
                    c.setTitle("Mr.");
                    c.setWebsite("www.internet.de");
                    c.setWorkphone("00 ");
                    c.setZip("" + new Random().nextInt(10000));
                    c.setisEnabled(true);
                    c.setisMale(new Random().nextBoolean());
                    c.setisCustomer(true);
                    c.setisManufacturer(new Random().nextBoolean());
                    c.setisSupplier(new Random().nextBoolean());
                    c.settaxnumber("123-" + seed);
                    c.setGroupsids(group);
                    c.setIntaddedby(mpv5.db.objects.User.getCurrentUser().__getIDS());
                    try {
                        c.save();
                    } catch (Exception e) {
                    }

                    if (withItems) {
                        for (int k = 0; k < i; k++) {

                            Date date = new RandomDate(new vTimeframe(DateConverter.getDate("01.01.2007"), new Date()));

                            Item it = (Item) Context.getInvoice().getNewObject();
                            it.setCname("SI" + seed + "-19-" + k);
                            it.setContactsids(c.__getIDS());
                            it.setDateend(date);
                            it.setDatetodo(date);
                            it.setAccountsids(defAccountID);
                            it.setGroupsids(group);
                            it.setIntreminders(0);
                            it.setInttype(Item.TYPE_INVOICE);
                            it.setIntstatus(Item.STATUS_PAID);
                      

                            double value = 0;
                            double tvalue1;
                            List<SubItem> l= new Vector<SubItem>();
                            List<Item> lm= new Vector<Item>();
                            for (int j = 0; j < 10; j++) {
                                SubItem p = new SubItem();
                                p.setCname("SPI" + seed + "-19-" + j);
                                p.setCountvalue(new BigDecimal(1));
                                p.setDateadded(date);
                                p.setDatedelivery(date);
                                p.setDescription("no description!");
                                p.setExternalvalue(Constants.BD100);value+=100d;
                                p.setGroupsids(1);
                                p.setIntaddedby(mpv5.db.objects.User.getCurrentUser().__getIDS());
                                p.setInternalvalue(Constants.BD100);
                                p.setMeasure("h");
                                p.setQuantityvalue(BigDecimal.ONE);
                                p.setTaxpercentvalue(new BigDecimal("19"));
                                l.add(p);
                            }

                            it.setTaxvalue(FormatNumber.round((value * 1.19) - value));
                            it.setNetvalue(FormatNumber.round(value));
                            it.setIntaddedby(mpv5.db.objects.User.getCurrentUser().__getIDS());
                            try {
                                it.save();
                            } catch (Exception e) {
                            }

                            for (int j = 0; j < l.size(); j++) {
                                SubItem subItem = l.get(j);
                                subItem.setItemsids(it.__getIDS());
                                try {
                                    subItem.save();
                                } catch (Exception e) {
                                }
                            }

                        }
                    }

                    if (withProducts) {
                        for (int k = 0; k < i; k++) {

                            double value = new Random().nextInt(100 * factor) + 1l;
                            Date date = new RandomDate(new vTimeframe(DateConverter.getDate("01.01.2009"), new Date()));

                            Product it = new Product();
                            it.setCname("P" + seed + "-19-" + k);
                            it.setCnumber("P" + seed + "-19-" + k);
                            it.setDateadded(date);
                            it.setDescription("sample product");
                            it.setEan("1234567891011");
                            it.setGroupsids(group);
                            it.setExternalnetvalue(new BigDecimal(value * 1.3));
                            it.setInternalnetvalue(new BigDecimal(value));
                            it.setProductgroupsids(1);
                            it.setInttype(Product.TYPE_PRODUCT);
                            it.setTaxids(2);
                            it.setManufacturersids(c.__getIDS());
                            it.setMeasure("pcs.");
                            it.setReference("swqsd1221");
                            it.setSuppliersids(c.__getIDS());
                            it.setUrl("www.test.com");
                            it.setIntaddedby(mpv5.db.objects.User.getCurrentUser().__getIDS());
                            try {
                                it.save();
                            } catch (Exception e) {
                            }

                            value = new Random().nextInt(100 * factor) + 1l;
                            date = new RandomDate(new vTimeframe(DateConverter.getDate("01.01.2007"), new Date()));

                            Product it1 = new Product();
                            it1.setCname("PS" + seed + "-19-" + k);
                            it1.setCnumber("PS" + seed + "-19-" + k);
                            it1.setDateadded(date);
                            it1.setProductgroupsids(1);
                            it1.setDescription("sample product");
                            it1.setEan("1234567891011");
                            it1.setGroupsids(group);
                            it1.setExternalnetvalue(new BigDecimal(value * 1.3));
                            it1.setInternalnetvalue(new BigDecimal(value));
                            it1.setInttype(Product.TYPE_SERVICE);
                            it1.setTaxids(1);
                            it1.setManufacturersids(c.__getIDS());
                            it1.setMeasure("pcs.");
                            it1.setReference("swqsd1221");
                            it1.setSuppliersids(c.__getIDS());
                            it1.setUrl("www.test.com");
                            it1.setIntaddedby(mpv5.db.objects.User.getCurrentUser().__getIDS());
                            try {
                                it1.save();
                            } catch (Exception e) {
                            }
                        }
                    }
                }
                Popup.notice("Sample data generated!");
            }
        };
        new Thread(runnable).start();
    }
}
