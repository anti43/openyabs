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
package mpv5.handler;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.utils.date.DateConverter;

/**
 * "Search anything": finds contacts, documents, products, positions,
 * conversations and appointments by any text, without Swing
 */
public final class SearchService {

    /**
     * Hits per kind of record
     */
    public static final int PER_KIND = 8;

    private SearchService() {
    }

    /**
     * A record found
     */
    public static final class Hit {

        public final String kind;
        public final String title;
        public final String detail;
        /**
         * the context and id to open (for a position: its document)
         */
        public final Context context;
        public final int id;

        public Hit(String kind, String title, String detail, Context context, int id) {
            this.kind = kind;
            this.title = title;
            this.detail = detail;
            this.context = context;
            this.id = id;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    /**
     * Searches all kinds of records; every word of the needle must occur in
     * one of the text columns
     *
     * @param needle
     * @return the hits, grouped by kind (contacts, documents, products,
     * positions, appointments)
     */
    public static List<Hit> search(String needle) {
        List<Hit> hits = new ArrayList<Hit>();
        String n = needle == null ? "" : needle.trim();
        if (n.length() < 2) {
            return hits;
        }
        contacts(n, hits);
        Context[] docs = {Context.getInvoice(), Context.getOffer(), Context.getOrder(),
            Context.getDeposit(), Context.getPartPayment(), Context.getCredit()};
        int[] types = {Item.TYPE_INVOICE, Item.TYPE_OFFER, Item.TYPE_ORDER,
            Item.TYPE_DEPOSIT, Item.TYPE_PART_PAYMENT, Item.TYPE_CREDIT};
        for (int i = 0; i < docs.length; i++) {
            documents(docs[i], types[i], n, hits);
        }
        products(n, hits);
        positions(n, hits);
        //conversations are hidden for now
        simple(Context.getSchedule(), Messages.SEARCH_KIND_SCHEDULE.toString(), n, hits);
        return hits;
    }

    private static Object[][] find(Context c, String fields, String needle) {
        try {
            return new DatabaseSearch(c, PER_KIND).getValuesFor2(fields, needle, null, true, true);
        } catch (Exception e) {
            Log.Debug(SearchService.class, e);
            return new Object[0][];
        }
    }

    private static void contacts(String n, List<Hit> hits) {
        Context c = Context.getContact();
        String t = c.getDbIdentity() + ".";
        for (Object[] r : find(c, t + "ids," + t + "cname," + t + "prename," + t + "company," + t + "cnumber," + t + "city", n)) {
            String name = join(" ", s(r[2]), s(r[1]));
            String company = s(r[3]);
            String title = company.isEmpty() || company.equals(s(r[1])) ? name : (name.isEmpty() ? company : company + " (" + name + ")");
            hits.add(new Hit(Messages.SEARCH_KIND_CONTACTS.toString(), title, join(" · ", s(r[4]), s(r[5])), c, id(r[0])));
        }
    }

    private static void documents(Context c, int itemType, String n, List<Hit> hits) {
        String t = c.getDbIdentity() + ".";
        String fields = t + "ids," + t + "cnumber," + t + "cname," + t + "description," + t + "dateadded," + t + "inttype," + t + "contactsids";
        Object[][] rows = find(c, fields, n);
        if (rows.length == 0) {
            rows = byLabel(c, fields, n);
        }
        String typeName = Item.getTypeString(itemType);
        if (rows.length == 0 && !typeName.isEmpty()
                && (matches(typeName, n) || (!n.contains(" ") && matches(n, typeName)))) {
            //"Rechnung" lists the latest invoices
            try {
                rows = new DatabaseSearch(c, PER_KIND).getValuesFor(fields);
            } catch (Exception e) {
                Log.Debug(SearchService.class, e);
            }
        }
        Map<Integer, String> names = new HashMap<Integer, String>();
        for (Object[] r : rows) {
            String type;
            try {
                type = Item.getTypeString(Integer.valueOf(s(r[5])));
            } catch (Exception e) {
                type = "";
            }
            String number = s(r[1]).isEmpty() ? s(r[2]) : s(r[1]);
            //as in the old search: number, customer, date and the matching part of the description
            hits.add(new Hit(Messages.SEARCH_KIND_DOCUMENTS.toString(), join(" ", type, number),
                    join(" · ", date(r[4]), contactName(r.length > 6 ? id(r[6]) : -1, names), snippet(s(r[3]), n)), c, id(r[0])));
        }
    }

    /**
     * Documents are shown as "contact-number" (e.g. "Schmitt-000012", see
     * Item#toString) although no field holds that: the number, then only the
     * documents of that contact
     *
     * @return the rows, empty if the needle is no such label
     */
    static Object[][] byLabel(Context c, String fields, String n) {
        int dash = n.lastIndexOf('-');
        if (dash <= 0 || dash == n.length() - 1) {
            return new Object[0][];
        }
        String contact = n.substring(0, dash).trim();
        String number = n.substring(dash + 1).trim();
        List<Object[]> out = new ArrayList<Object[]>();
        for (Object[] r : find(c, fields, number)) {
            try {
                Item i = (Item) mpv5.db.common.DatabaseObject.getObject(c, id(r[0]));
                if (number.equalsIgnoreCase(i.__getCnumber()) && contact.equalsIgnoreCase(i.getContact().__getCname())) {
                    out.add(r);
                }
            } catch (Exception e) {
                Log.Debug(SearchService.class, e.getMessage());
            }
        }
        return out.toArray(new Object[0][]);
    }

    private static void products(String n, List<Hit> hits) {
        Context c = Context.getProduct();
        String t = c.getDbIdentity() + ".";
        for (Object[] r : find(c, t + "ids," + t + "cname," + t + "cnumber," + t + "description", n)) {
            hits.add(new Hit(Messages.SEARCH_KIND_PRODUCTS.toString(), s(r[1]), join(" · ", s(r[2]), shorten(s(r[3]))), c, id(r[0])));
        }
    }

    private static void positions(String n, List<Hit> hits) {
        Context c = Context.getSubItem();
        String t = c.getDbIdentity() + ".";
        Map<Integer, String> names = new HashMap<Integer, String>();
        for (Object[] r : find(c, t + "ids," + t + "itemsids," + t + "description," + t + "cname", n)) {
            String text = snippet(s(r[2]).isEmpty() ? s(r[3]) : s(r[2]), n);
            //opens the document of the position; shows which document it is
            hits.add(new Hit(Messages.SEARCH_KIND_POSITIONS.toString(), text, documentLabel(id(r[1]), names),
                    Context.getInvoice(), id(r[1])));
        }
    }

    /**
     * @return "Auftrag 000012 · Christ · 01.02.2026" for the document of a
     * position, or "opens the document" if it cannot be read
     */
    private static String documentLabel(int itemId, Map<Integer, String> names) {
        try {
            Item i = (Item) DatabaseObject.getObject(Context.getInvoice(), itemId);
            String label = join(" · ", join(" ", Item.getTypeString(i.__getInttype()), i.__getCnumber()),
                    contactName(i.__getContactsids(), names), date(i.__getDateadded()));
            if (!label.isEmpty()) {
                return label;
            }
        } catch (Exception e) {
            Log.Debug(SearchService.class, e.getMessage());
        }
        return Messages.SEARCH_IN_DOCUMENT.toString();
    }

    private static String contactName(int contactId, Map<Integer, String> names) {
        if (contactId <= 0) {
            return "";
        }
        String name = names.get(contactId);
        if (name == null) {
            try {
                Contact co = (Contact) DatabaseObject.getObject(Context.getContact(), contactId);
                String company = s(co.__getCompany());
                name = company.isEmpty() ? join(" ", s(co.__getPrename()), s(co.__getCname())) : company;
            } catch (Exception e) {
                name = "";
            }
            names.put(contactId, name);
        }
        return name;
    }

    /**
     * @return the text without markup, shortened around the first word of the
     * needle it contains, so the matching part (e.g. an order number) is
     * visible
     */
    static String snippet(String s, String needle) {
        String p = plain(s);
        if (p.length() <= 90) {
            return p;
        }
        String lower = p.toLowerCase(Locale.ROOT);
        int at = -1;
        for (String w : (needle == null ? "" : needle.trim().toLowerCase(Locale.ROOT)).split("\\s+")) {
            if (!w.isEmpty()) {
                int k = lower.indexOf(w);
                if (k >= 0 && (at < 0 || k < at)) {
                    at = k;
                }
            }
        }
        if (at < 60) {
            return p.substring(0, 87) + "...";
        }
        int from = Math.max(0, at - 30);
        int to = Math.min(p.length(), from + 84);
        return "..." + p.substring(from, to).trim() + (to < p.length() ? "..." : "");
    }

    private static void simple(Context c, String kind, String n, List<Hit> hits) {
        String t = c.getDbIdentity() + ".";
        for (Object[] r : find(c, t + "ids," + t + "cname," + t + "dateadded", n)) {
            hits.add(new Hit(kind, s(r[1]), date(r[2]), c, id(r[0])));
        }
    }

    /**
     * @return true if every word of the needle occurs in the text, ignoring
     * case and accents ("Rechnung neu" matches "Neue Rechnung")
     */
    public static boolean matches(String text, String needle) {
        String t = fold(text);
        for (String w : fold(needle).split("\\s+")) {
            if (!w.isEmpty() && !t.contains(w)) {
                return false;
            }
        }
        return true;
    }

    private static String fold(String s) {
        String n = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.toLowerCase(Locale.ROOT).replace("ß", "ss");
    }

    private static String plain(String s) {
        return s.replaceAll("<[^>]*>", " ").replace("&nbsp;", " ").replace("&amp;", "&").replaceAll("\\s+", " ").trim();
    }

    private static String shorten(String s) {
        String p = plain(s);
        return p.length() > 90 ? p.substring(0, 87) + "..." : p;
    }

    private static String date(Object o) {
        try {
            return DateConverter.getDefDateString(DateConverter.getDate(o));
        } catch (Exception e) {
            return s(o);
        }
    }

    private static String join(String sep, String... parts) {
        StringBuilder b = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.trim().isEmpty()) {
                if (b.length() > 0) {
                    b.append(sep);
                }
                b.append(p.trim());
            }
        }
        return b.toString();
    }

    private static String s(Object o) {
        return o == null || "null".equals(String.valueOf(o)) ? "" : String.valueOf(o).trim();
    }

    private static int id(Object o) {
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (Exception e) {
            return -1;
        }
    }
}
