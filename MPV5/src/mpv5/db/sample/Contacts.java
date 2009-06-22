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
package mpv5.db.sample;

import java.text.DateFormat;
import java.util.Date;
import java.util.Random;
import javax.swing.SwingUtilities;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPV5View;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.RandomDate;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.numberformat.FormatNumber;

/**
 *
 *  anti
 */
public class Contacts {

    private int multiplier;

    /**
     *
     * @param factor
     * @param defAccountID
     * @param withItems
     */
    public Contacts(final int factor, final int defAccountID, final boolean withItems) {
        this.multiplier = factor;

        Group g = new Group();
        g.setDescription("This group hold sample data");
        g.setCName("Sample Group" + new Random().nextInt(factor * 43) + 1);
        g.save();
        final int group = g.__getIDS();


        Runnable runnable = new Runnable() {

            public void run() {
                for (int i = 0; i < factor * 6; i++) {
                    int seed = new Random().nextInt(factor * 43) + 1;

                    Contact c = new Contact();
                    c.setCName("Contact " + seed);
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
                    c.setIntaddedby(MPV5View.getUser().__getIDS());
                    c.save();

                    if (withItems) {
                        for (int k = 0; k < i; k++) {

                            double value = new Random().nextInt(100 * factor) + 1l;
                            Date date = new RandomDate(new vTimeframe(DateConverter.getDate("01.01.2007"), new Date()));

                            Item it = new Item();
                            it.setCName("SI" + seed + "-19-" + k);
                            it.setContactsids(c.__getIDS());
                            it.setDateend(date);
                            it.setDatetodo(date);
                            it.setDefaultaccountsids(defAccountID);
                            it.setGroupsids(group);
                            it.setIntreminders(0);
                            it.setInttype(Item.TYPE_BILL);
                            it.setIntstatus(Item.STATUS_PAID);
                            it.setTaxvalue(FormatNumber.round((value * 1.19) - value));
                            it.setNetvalue(FormatNumber.round(value));
                            it.setIntaddedby(MPV5View.getUser().__getIDS());
                            it.save();

                            value = new Random().nextInt(100 * factor) + 1l;
                            date = new RandomDate(new vTimeframe(DateConverter.getDate("01.01.2007"), new Date()));

                            Item it1 = new Item();
                            it1.setCName("SI" + seed + "-7-" + k);
                            it1.setContactsids(c.__getIDS());
                            it1.setDateend(date);
                            it1.setDatetodo(date);
                            it1.setDefaultaccountsids(defAccountID);
                            it1.setGroupsids(group);
                            it1.setIntreminders(0);
                            it1.setInttype(Item.TYPE_BILL);
                            it1.setIntstatus(Item.STATUS_PAID);
                            it1.setTaxvalue(FormatNumber.round((value * 1.07) - value));
                            it1.setNetvalue(FormatNumber.round(value));
                            it1.setIntaddedby(MPV5View.getUser().__getIDS());
                            it1.save();

                            value = new Random().nextInt(100 * factor) + 1l;
                            date = new RandomDate(new vTimeframe(DateConverter.getDate("01.01.2007"), new Date()));

                            Item it2 = new Item();
                            it2.setCName("SI" + seed + "-0-" + k);
                            it2.setContactsids(c.__getIDS());
                            it2.setDateend(date);
                            it2.setDatetodo(date);
                            it2.setDefaultaccountsids(defAccountID);
                            it2.setGroupsids(group);
                            it2.setIntreminders(0);
                            it2.setInttype(Item.TYPE_BILL);
                            it2.setIntstatus(Item.STATUS_PAID);
                            it2.setTaxvalue(FormatNumber.round(0));
                            it2.setNetvalue(FormatNumber.round(value));
                            it2.setIntaddedby(MPV5View.getUser().__getIDS());
                            it2.save();
                        }
                    }
                }
                Popup.notice("Sample data generated!");
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

}
