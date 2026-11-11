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
package mpv5.webshop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.globals.GlobalSettings;
import mpv5.globals.Messages;
import mpv5.handler.ItemService;
import mpv5.logging.Log;

/**
 * Imports the new orders of a Shopify shop (Admin REST API): each order
 * becomes an order (or invoice) in YaBS with its positions and shipping; the
 * customer becomes a contact (found again by e-mail). The id of the last
 * imported order is remembered, so no order is imported twice.
 */
public final class ShopifyImport {

    public static final String KEY_SHOP = "org.openyabs.shopify.shop";
    public static final String KEY_TOKEN = "org.openyabs.shopify.token";
    public static final String KEY_VERSION = "org.openyabs.shopify.apiversion";
    public static final String KEY_LAST = "org.openyabs.shopify.lastorderid";
    public static final String KEY_DOCTYPE = "org.openyabs.shopify.doctype";
    public static final String DEFAULT_VERSION = "2024-10";
    private static final ObjectMapper JSON = new ObjectMapper();

    /**
     * What an import did
     */
    public static final class Result {

        public int orders;
        public int contacts;
        public int positions;
        public final List<String> warnings = new ArrayList<String>();
    }

    private final String shop;
    private final String token;
    private final String version;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();

    /**
     * @param shop e.g. my-shop.myshopify.com (with or without https://)
     * @param token the Admin API access token of a custom app
     * @param version the API version, e.g. 2024-10; null: default
     */
    public ShopifyImport(String shop, String token, String version) {
        this.shop = normalizeShop(shop);
        this.token = token == null ? "" : token.trim();
        this.version = version == null || version.trim().isEmpty() ? DEFAULT_VERSION : version.trim();
    }

    /**
     * @return the importer with the saved settings
     */
    public static ShopifyImport fromSettings() {
        return new ShopifyImport(GlobalSettings.getProperty(KEY_SHOP), GlobalSettings.getProperty(KEY_TOKEN),
                GlobalSettings.getProperty(KEY_VERSION));
    }

    /**
     * @param shop
     * @return the host name only: "my-shop.myshopify.com"
     */
    public static String normalizeShop(String shop) {
        String s = shop == null ? "" : shop.trim().toLowerCase();
        s = s.replaceFirst("^https?://", "");
        int slash = s.indexOf('/');
        if (slash >= 0) {
            s = s.substring(0, slash);
        }
        if (!s.isEmpty() && !s.contains(".")) {
            s = s + ".myshopify.com";
        }
        return s;
    }

    private JsonNode get(String path) throws IOException, InterruptedException {
        if (shop.isEmpty() || token.isEmpty()) {
            throw new IOException(Messages.SHOPIFY_NOT_SET.toString());
        }
        HttpRequest r = HttpRequest.newBuilder(URI.create("https://" + shop + "/admin/api/" + version + "/" + path))
                .timeout(Duration.ofSeconds(60))
                .header("X-Shopify-Access-Token", token)
                .header("Accept", "application/json")
                .GET().build();
        HttpResponse<String> res = http.send(r, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            String body = res.body() == null ? "" : res.body();
            throw new IOException("Shopify: HTTP " + res.statusCode() + " " + (body.length() > 300 ? body.substring(0, 300) : body));
        }
        return JSON.readTree(res.body());
    }

    /**
     * @return the name of the shop
     * @throws Exception if the shop cannot be reached or the token is wrong
     */
    public String test() throws Exception {
        return get("shop.json").path("shop").path("name").asText();
    }

    /**
     * Imports all orders after the last imported one
     *
     * @return what was done
     * @throws Exception if the shop cannot be reached
     */
    public Result importNew() throws Exception {
        Result result = new Result();
        long since = 0;
        try {
            since = Long.parseLong(GlobalSettings.getProperty(KEY_LAST, "0"));
        } catch (NumberFormatException e) {
            //from the start
        }
        while (true) {
            JsonNode orders = get("orders.json?status=any&limit=250&since_id=" + since).path("orders");
            if (!orders.isArray() || orders.size() == 0) {
                break;
            }
            for (JsonNode o : orders) {
                long id = o.path("id").asLong();
                try {
                    importOrder(o, result);
                } catch (Exception e) {
                    Log.Debug(this, e);
                    result.warnings.add(o.path("name").asText() + ": " + e.getMessage());
                }
                since = Math.max(since, id);
                //after each order: an interrupted import goes on from there
                GlobalSettings.setProperty(KEY_LAST, String.valueOf(since));
                GlobalSettings.save();
            }
            if (orders.size() < 250) {
                break;
            }
        }
        return result;
    }

    private static String text(JsonNode n, String field) {
        String s = n == null ? null : n.path(field).asText(null);
        return s == null || s.trim().isEmpty() || "null".equals(s) ? null : s.trim();
    }

    private static String cut(String s, int max) {
        return s == null ? null : s.length() > max ? s.substring(0, max) : s;
    }

    private static BigDecimal number(JsonNode n, String field) {
        try {
            String s = n.path(field).asText("0");
            return new BigDecimal(s.isEmpty() ? "0" : s);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * @return the VAT rate in percent of the first tax line (0.19 → 19)
     */
    private static BigDecimal rate(JsonNode n) {
        JsonNode t = n.path("tax_lines");
        if (t.isArray() && t.size() > 0) {
            return number(t.get(0), "rate").movePointRight(2).stripTrailingZeros();
        }
        return BigDecimal.ZERO;
    }

    void importOrder(JsonNode o, Result result) throws Exception {
        boolean taxesIncluded = o.path("taxes_included").asBoolean(false);
        Date date = new Date();
        try {
            date = Date.from(OffsetDateTime.parse(o.path("created_at").asText()).toInstant());
        } catch (Exception e) {
            //now
        }
        int contact = contact(o, result);

        boolean invoice = "invoice".equals(GlobalSettings.getProperty(KEY_DOCTYPE, "order"));
        Item item = (Item) DatabaseObject.getObject(invoice ? Context.getInvoice() : Context.getOrder());
        item.setInttype(invoice ? Item.TYPE_INVOICE : Item.TYPE_ORDER);
        item.setContactsids(contact);
        item.setGroupsids(1);
        item.setAccountsids(1);
        item.setDateadded(date);
        item.setDatetodo(date);
        item.setDateend(date);
        boolean paid = "paid".equals(o.path("financial_status").asText());
        item.setIntstatus(paid ? Item.STATUS_PAID : Item.STATUS_IN_PROGRESS);
        StringBuilder d = new StringBuilder("Shopify ").append(o.path("name").asText()).append(" (")
                .append(o.path("financial_status").asText("")).append(")");
        if (text(o, "note") != null) {
            d.append("\n").append(text(o, "note"));
        }
        item.setDescription(cut(d.toString(), 100000));
        if (!item.save(true)) {
            throw new IOException("not saved");
        }

        int pos = 1;
        for (JsonNode l : o.path("line_items")) {
            BigDecimal qty = number(l, "quantity");
            if (qty.signum() == 0) {
                continue;
            }
            BigDecimal price = number(l, "price");
            BigDecimal discount = BigDecimal.ZERO;
            for (JsonNode a : l.path("discount_allocations")) {
                discount = discount.add(number(a, "amount"));
            }
            BigDecimal unit = price.subtract(discount.divide(qty, 10, RoundingMode.HALF_UP));
            BigDecimal rate = rate(l);
            String name = l.path("title").asText("");
            if (text(l, "variant_title") != null) {
                name += " – " + text(l, "variant_title");
            }
            if (text(l, "sku") != null) {
                name = text(l, "sku") + " " + name;
            }
            position(item, name, qty, net(unit, rate, taxesIncluded), rate, pos++).save();
            result.positions++;
        }
        for (JsonNode s : o.path("shipping_lines")) {
            BigDecimal price = number(s, "discounted_price");
            if (price.signum() == 0) {
                price = number(s, "price");
            }
            if (price.signum() == 0) {
                continue;
            }
            BigDecimal rate = rate(s);
            position(item, Messages.SHOPIFY_SHIPPING + " " + s.path("title").asText(""), BigDecimal.ONE,
                    net(price, rate, taxesIncluded), rate, pos++).save();
            result.positions++;
        }
        ItemService.updateTotals(item);
        result.orders++;
    }

    private static BigDecimal net(BigDecimal price, BigDecimal rate, boolean taxesIncluded) {
        return taxesIncluded ? price.divide(BigDecimal.ONE.add(rate.movePointLeft(2)), 5, RoundingMode.HALF_UP)
                : price.setScale(5, RoundingMode.HALF_UP);
    }

    private static SubItem position(Item item, String name, BigDecimal quantity, BigDecimal net, BigDecimal rate, int pos) {
        SubItem s = new SubItem();
        s.setItemsids(item.__getIDS());
        s.setCname(cut(name, 250));
        s.setDescription(cut(name, 10000));
        s.setQuantityvalue(quantity);
        s.setCountvalue(BigDecimal.valueOf(pos));
        s.setExternalvalue(net);
        s.setInternalvalue(net);
        s.setTaxpercentvalue(rate);
        s.setMeasure("");
        s.setOrdernr(pos);
        s.setDateadded(item.__getDateadded());
        s.setGroupsids(1);
        return s;
    }

    /**
     * The customer: found by e-mail, otherwise created from the billing address
     */
    private static int contact(JsonNode o, Result result) throws Exception {
        JsonNode customer = o.path("customer");
        String email = text(o, "email");
        if (email == null) {
            email = text(customer, "email");
        }
        if (email != null) {
            try {
                return DatabaseObject.getObject(Context.getContact(), "mailaddress", email).__getIDS();
            } catch (Exception notFound) {
                //new customer
            }
        }
        JsonNode a = o.path("billing_address");
        if (a.isMissingNode() || a.isNull()) {
            a = customer.path("default_address");
        }
        if (a.isMissingNode() || a.isNull()) {
            a = o.path("shipping_address");
        }
        String last = text(a, "last_name") != null ? text(a, "last_name") : text(customer, "last_name");
        String first = text(a, "first_name") != null ? text(a, "first_name") : text(customer, "first_name");
        String company = text(a, "company");
        Contact c = (Contact) DatabaseObject.getObject(Context.getContact());
        c.setCname(cut(last != null ? last : company != null ? company : email != null ? email : o.path("name").asText(), 250));
        c.setPrename(cut(first, 250));
        c.setCompany(cut(company, 250));
        c.setisCompany(last == null && company != null);
        c.setisCustomer(true);
        String street = text(a, "address1");
        if (text(a, "address2") != null) {
            street = (street == null ? "" : street + ", ") + text(a, "address2");
        }
        c.setStreet(cut(street, 250));
        c.setZip(cut(text(a, "zip"), 50));
        c.setCity(cut(text(a, "city"), 250));
        c.setCountry(cut(text(a, "country_code") != null ? text(a, "country_code") : text(a, "country"), 250));
        c.setMainphone(cut(text(a, "phone") != null ? text(a, "phone") : text(customer, "phone"), 250));
        c.setMailaddress(cut(email, 250));
        c.setNotes("Shopify");
        c.setGroupsids(1);
        if (!c.save(true)) {
            throw new IOException("contact not saved");
        }
        result.contacts++;
        return c.__getIDS();
    }
}
