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
package mpv5.utils.export;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.ValueProperty;
import mpv5.logging.Log;
import mpv5.utils.text.Iban;

/**
 * A bank account of the company. The first account is the one of the company
 * info (fields IBAN, BIC, bank name), further accounts are stored as JSON in
 * the company setting {@value #KEY_ACCOUNTS}. Each contact can be assigned
 * the account its payments go to (value property {@value #KEY_CONTACT}).
 */
public class BankAccount {

    /**
     * Company setting (companyinfo.bankaccounts): further accounts as JSON
     */
    public static final String KEY_ACCOUNTS = "bankaccounts";
    /**
     * Value property of a contact: IBAN of the account it pays to
     */
    public static final String KEY_CONTACT = "einvoice.bankaccount";
    private static final ObjectMapper JSON = new ObjectMapper();
    private final String name;
    private final String iban;
    private final String bic;
    private final String bank;

    public BankAccount(String name, String iban, String bic, String bank) {
        this.name = ZugpferdFile.nvl(name).trim();
        this.iban = Iban.normalize(iban);
        this.bic = Iban.normalize(bic);
        this.bank = ZugpferdFile.nvl(bank).trim();
    }

    public String getName() {
        return name;
    }

    /**
     * @return the IBAN without spaces
     */
    public String getIban() {
        return iban;
    }

    public String getBic() {
        return bic;
    }

    public String getBank() {
        return bank;
    }

    @Override
    public String toString() {
        String label = name.isEmpty() ? bank : name;
        return (label.isEmpty() ? "" : label + " - ") + Iban.format(iban);
    }

    /**
     * @param company lookup for companyinfo.* keys (without prefix)
     * @return all accounts of the company, the account of the company info
     * first; empty if none is configured
     */
    public static List<BankAccount> companyAccounts(Function<String, String> company) {
        List<BankAccount> list = new ArrayList<BankAccount>();
        BankAccount main = mainAccount(company);
        if (main != null) {
            list.add(main);
        }
        for (BankAccount a : fromJson(company.apply(KEY_ACCOUNTS))) {
            if (!a.getIban().isEmpty() && find(list, a.getIban()) == null) {
                list.add(a);
            }
        }
        return list;
    }

    /**
     * The account of the company info. "account" is labelled IBAN and
     * "banknumber" BIC; older (German) versions used "banknumber" for the IBAN,
     * so whatever field holds a valid IBAN / BIC is taken.
     */
    static BankAccount mainAccount(Function<String, String> company) {
        String[] fields = {company.apply("account"), company.apply("iban"), company.apply("banknumber"), company.apply("bic")};
        String iban = null;
        for (String v : fields) {
            if (Iban.isValid(v)) {
                iban = v;
                break;
            }
        }
        if (iban == null) {
            //keep an invalid value, the e-invoice check reports it
            iban = ZugpferdFile.firstNonEmpty(company.apply("account"), company.apply("iban"));
        }
        if (ZugpferdFile.isEmpty(iban)) {
            return null;
        }
        String bic = "";
        for (String v : new String[]{company.apply("banknumber"), company.apply("bic"), company.apply("account")}) {
            if (Iban.isValidBic(v)) {
                bic = v;
                break;
            }
        }
        return new BankAccount("", iban, bic, company.apply("bankname"));
    }

    /**
     * @param accounts
     * @param iban with or without spaces
     * @return the account with this IBAN, null if none
     */
    public static BankAccount find(List<BankAccount> accounts, String iban) {
        String n = Iban.normalize(iban);
        for (BankAccount a : accounts) {
            if (a.getIban().equals(n)) {
                return a;
            }
        }
        return null;
    }

    /**
     * @param contact
     * @param company
     * @return the account assigned to the contact, otherwise the account of
     * the company info; null if the company has no account
     */
    public static BankAccount forContact(DatabaseObject contact, Function<String, String> company) {
        List<BankAccount> accounts = companyAccounts(company);
        BankAccount a = find(accounts, contactIban(contact));
        return a != null ? a : accounts.isEmpty() ? null : accounts.get(0);
    }

    /**
     * @return the IBAN assigned to the contact, "" if none
     */
    public static String contactIban(DatabaseObject contact) {
        return Iban.normalize(ContactProperty.get(contact, KEY_CONTACT));
    }

    /**
     * @param contact the (saved) contact
     * @param iban the account to use, null or empty for the account of the
     * company info
     */
    public static void saveForContact(DatabaseObject contact, String iban) {
        ContactProperty.set(contact, KEY_CONTACT, Iban.normalize(iban));
    }

    /**
     * @param accounts
     * @return JSON for the company setting {@value #KEY_ACCOUNTS}
     */
    public static String toJson(List<BankAccount> accounts) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (BankAccount a : accounts) {
            Map<String, String> m = new LinkedHashMap<String, String>();
            m.put("name", a.getName());
            m.put("iban", a.getIban());
            m.put("bic", a.getBic());
            m.put("bank", a.getBank());
            list.add(m);
        }
        try {
            return JSON.writeValueAsString(list);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param json the company setting {@value #KEY_ACCOUNTS}, may be null
     * @return the accounts, empty if none or unreadable
     */
    public static List<BankAccount> fromJson(String json) {
        List<BankAccount> list = new ArrayList<BankAccount>();
        if (ZugpferdFile.isEmpty(json)) {
            return list;
        }
        try {
            List<Map<String, String>> raw = JSON.readValue(json, new TypeReference<List<Map<String, String>>>() {
            });
            for (Map<String, String> m : raw) {
                list.add(new BankAccount(m.get("name"), m.get("iban"), m.get("bic"), m.get("bank")));
            }
        } catch (Exception e) {
            Log.Debug(BankAccount.class, "Invalid bank accounts: " + e.getMessage());
        }
        return list;
    }
}
