/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.pos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Tax;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.logging.Log;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.numberformat.InvoiceMath;

/**
 * The cash register: products with gross prices, sign-in of YaBS users and
 * sales. A sale is a paid invoice for the cash customer, so it is in YaBS like
 * every other invoice (journal, revenue, reports).
 */
public final class PosService {

    /**
     * A product as the register shows it
     */
    public static final class Article {

        public final int id;
        public final String name;
        public final String number;
        public final String group;
        public final BigDecimal net;
        public final BigDecimal taxPercent;
        public final BigDecimal gross;
        public final String measure;

        public Article(int id, String name, String number, String group, BigDecimal net, BigDecimal taxPercent, String measure) {
            this.id = id;
            this.name = name;
            this.number = number;
            this.group = group;
            this.net = net;
            this.taxPercent = taxPercent;
            this.gross = InvoiceMath.round2(net.multiply(BigDecimal.ONE.add(taxPercent.movePointLeft(2))));
            this.measure = measure;
        }
    }

    /**
     * A line of a sale
     */
    public static final class Line {

        public final Article article;
        public final BigDecimal quantity;

        public Line(Article article, BigDecimal quantity) {
            this.article = article;
            this.quantity = quantity;
        }
    }

    /**
     * A finished sale
     */
    public static final class Receipt {

        public final String number;
        public final BigDecimal net;
        public final BigDecimal tax;
        public final BigDecimal gross;
        public final BigDecimal given;
        public final BigDecimal change;
        public final int invoiceId;

        Receipt(String number, InvoiceMath.Totals t, BigDecimal given, int invoiceId) {
            this.number = number;
            this.net = t.net;
            this.tax = t.tax;
            this.gross = t.gross;
            this.given = given;
            this.change = given == null ? BigDecimal.ZERO : given.subtract(t.gross).max(BigDecimal.ZERO);
            this.invoiceId = invoiceId;
        }
    }

    private final Map<String, User> sessions = new ConcurrentHashMap<String, User>();
    private final SecureRandom random = new SecureRandom();
    private final String cashCustomerName;
    private volatile java.util.Set<Integer> groups = java.util.Collections.emptySet();

    /**
     * @param ids the product groups sold at the register, empty: all products
     */
    public void setGroups(java.util.Set<Integer> ids) {
        this.groups = ids == null ? java.util.Collections.<Integer>emptySet() : new java.util.HashSet<Integer>(ids);
    }

    /**
     * @param csv e.g. "3,5" (as stored in the settings)
     * @return the ids
     */
    public static java.util.Set<Integer> parseGroups(String csv) {
        java.util.Set<Integer> s = new java.util.LinkedHashSet<Integer>();
        if (csv != null) {
            for (String x : csv.split(",")) {
                try {
                    s.add(Integer.valueOf(x.trim()));
                } catch (NumberFormatException ignore) {
                    //skip
                }
            }
        }
        return s;
    }

    /**
     * @param cashCustomerName the name of the contact all sales go to
     */
    public PosService(String cashCustomerName) {
        this.cashCustomerName = cashCustomerName;
    }

    // ------------------------------------------------------------------ sign in
    /**
     * @param user
     * @param password
     * @return a token for the following requests, null if wrong
     */
    public String login(String user, String password) {
        User u = null;
        try {
            u = MPSecurityManager.checkAuth(user, password);
        } catch (Exception e) {
            Log.Debug(PosService.class, e);
        }
        if (u == null || !u.__getIsenabled()) {
            return null;
        }
        byte[] b = new byte[24];
        random.nextBytes(b);
        StringBuilder t = new StringBuilder();
        for (byte x : b) {
            t.append(String.format("%02x", x));
        }
        sessions.put(t.toString(), u);
        return t.toString();
    }

    /**
     * @param token
     * @return the signed in user or null
     */
    public User user(String token) {
        return token == null ? null : sessions.get(token);
    }

    /**
     * @param token signs out
     */
    public void logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    // ------------------------------------------------------------------ products
    /**
     * @return the products to sell (with a name), by group and name
     */
    public List<Article> articles() {
        List<Article> l = new ArrayList<Article>();
        Map<Integer, String> groups = new LinkedHashMap<Integer, String>();
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getProduct())) {
                Product p = (Product) o;
                if (p.__getCname() == null || p.__getCname().trim().isEmpty()) {
                    continue;
                }
                java.util.Set<Integer> only = this.groups;
                if (!only.isEmpty() && !only.contains(p.__getProductgroupsids())) {
                    continue;
                }
                BigDecimal tax;
                try {
                    tax = Tax.getTaxValue(p.__getTaxids());
                } catch (Exception e) {
                    tax = BigDecimal.ZERO;
                }
                String group = groups.computeIfAbsent(p.__getProductgroupsids(), PosService::groupName);
                l.add(new Article(p.__getIDS(), p.__getCname(), s(p.__getCnumber()), group,
                        p.__getExternalnetvalue() == null ? BigDecimal.ZERO : p.__getExternalnetvalue(),
                        tax == null ? BigDecimal.ZERO : tax, s(p.__getMeasure())));
            }
        } catch (NodataFoundException e) {
            Log.Debug(PosService.class, e.getMessage());
        }
        l.sort((a, b) -> {
            int c = a.group.compareToIgnoreCase(b.group);
            return c != 0 ? c : a.name.compareToIgnoreCase(b.name);
        });
        return l;
    }

    private static String groupName(Integer id) {
        //the main group (all products) is no group of its own on the register
        if (id == null || id <= 1) {
            return "";
        }
        try {
            return DatabaseObject.getObject(Context.getProductGroup(), id).__getCname();
        } catch (Exception e) {
            return "";
        }
    }

    // ------------------------------------------------------------------ sale
    /**
     * @param lines
     * @return net, tax and gross of the lines (as the invoice computes them)
     */
    public static InvoiceMath.Totals totals(List<Line> lines) {
        List<InvoiceMath.Line> l = new ArrayList<InvoiceMath.Line>();
        for (Line x : lines) {
            l.add(new InvoiceMath.Line(x.quantity, x.article.net, BigDecimal.ZERO, x.article.taxPercent));
        }
        return InvoiceMath.totals(l);
    }

    /**
     * Books a sale: a paid invoice for the cash customer with the lines
     *
     * @param token the signed in user
     * @param lines
     * @param given the money given, may be null
     * @return the receipt
     * @throws IllegalArgumentException if not signed in, empty or not enough
     * money
     * @throws Exception if it can not be saved
     */
    public synchronized Receipt sell(String token, List<Line> lines, BigDecimal given) throws Exception {
        //a request of the cash register must never wait for a message box on the desktop
        return mpv5.ui.dialogs.Popup.silently(() -> book(token, lines, given));
    }

    private Receipt book(String token, List<Line> lines, BigDecimal given) throws Exception {
        User seller = user(token);
        if (seller == null) {
            throw new IllegalArgumentException("not signed in");
        }
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("empty");
        }
        InvoiceMath.Totals t = totals(lines);
        if (given != null && given.compareTo(t.gross) < 0) {
            throw new IllegalArgumentException("not enough");
        }
        Contact cash = cashCustomer();
        Item inv = (Item) DatabaseObject.getObject(Context.getInvoice());
        inv.setInttype(Constants.TYPE_INVOICE);
        inv.setContactsids(cash.__getIDS());
        inv.setIntstatus(Item.STATUS_PAID);
        inv.setGroupsids(1);
        //a booking account is required: the default of the seller, else the main account
        int account = seller.__getIntdefaultaccount();
        inv.setAccountsids(account > 0 ? account : 1);
        Date now = new Date();
        inv.setDatetodo(now);
        inv.setDateend(now);
        inv.setDescription("POS · " + seller.__getCname());
        if (!inv.save()) {
            throw new IllegalStateException("The invoice could not be saved");
        }
        int n = 1;
        for (Line x : lines) {
            SubItem s = new SubItem();
            s.setItemsids(inv.__getIDS());
            s.setCname(x.article.name);
            s.setDescription(x.article.name);
            s.setOriginalproductsids(x.article.id);
            s.setQuantityvalue(x.quantity);
            s.setExternalvalue(x.article.net);
            s.setInternalvalue(x.article.net);
            s.setTaxpercentvalue(x.article.taxPercent);
            s.setMeasure(x.article.measure);
            s.setOrdernr(n++);
            s.setDateadded(now);
            s.setGroupsids(1);
            s.save(true);
        }
        mpv5.handler.ItemService.updateTotals(inv);
        return new Receipt(inv.__getCnumber(), t, given, inv.__getIDS());
    }

    /**
     * @return the contact of the cash sales (made once)
     * @throws Exception
     */
    public Contact cashCustomer() throws Exception {
        try {
            return (Contact) DatabaseObject.getObject(Context.getContact(), cashCustomerName);
        } catch (NodataFoundException e) {
            Contact c = (Contact) DatabaseObject.getObject(Context.getContact());
            c.setCname(cashCustomerName);
            c.setCompany(cashCustomerName);
            c.setisCustomer(true);
            c.setGroupsids(1);
            c.save(true);
            return c;
        }
    }

    /**
     * @param s like "12.50" or "12,50"
     * @return the amount or null
     */
    public static BigDecimal amount(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(s.trim().replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String s(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
