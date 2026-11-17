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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import mpv5.globals.Messages;
import mpv5.utils.text.Iban;

/**
 * Checks the data of a contact for its use as buyer of an electronic invoice
 * (XRechnung / ZUGFeRD) - before an invoice is written. Uses the same rules as
 * {@link ZugpferdFile#validate(org.mustangproject.Invoice)}.
 */
public final class EInvoiceContactCheck {

    /**
     * The fields of a contact which are checked
     */
    public enum Field {

        NAME, STREET, ZIP, CITY, COUNTRY, EMAIL, VATID, IBAN
    }

    /**
     * The result of a check
     */
    public static final class Result {

        private final Set<Field> errors = EnumSet.noneOf(Field.class);
        private final Set<Field> hints = EnumSet.noneOf(Field.class);
        private final List<String> messages = new ArrayList<String>();

        /**
         * @return true if there is nothing to complain about
         */
        public boolean isOk() {
            return errors.isEmpty() && hints.isEmpty();
        }

        /**
         * @return true if an e-invoice for this contact would be rejected
         */
        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        /**
         * @return the fields with missing or invalid data
         */
        public Set<Field> getErrors() {
            return Collections.unmodifiableSet(errors);
        }

        /**
         * @return the fields which should be checked by the user
         */
        public Set<Field> getHints() {
            return Collections.unmodifiableSet(hints);
        }

        /**
         * @return the reasons, human readable
         */
        public List<String> getMessages() {
            return Collections.unmodifiableList(messages);
        }

        private void error(Field field, String message) {
            errors.add(field);
            messages.add(message);
        }

        private void hint(Field field, String message) {
            hints.add(field);
            messages.add(message);
        }
    }

    private EInvoiceContactCheck() {
    }

    /**
     * Check the data of a contact
     *
     * @param name name of the person
     * @param company
     * @param street
     * @param zip
     * @param city
     * @param country as stored in YaBS (numeric code, ISO code or name)
     * @param email
     * @param vatId the content of the VAT ID / tax number field
     * @param iban the IBAN of the contact, may be empty
     * @param homeCountry ISO 3166 alpha 2 code of the own company
     * @return the result
     */
    public static Result check(String name, String company, String street, String zip, String city, String country,
            String email, String vatId, String iban, String homeCountry) {
        Result r = new Result();
        if (ZugpferdFile.isEmpty(name) && ZugpferdFile.isEmpty(company)) {
            r.error(Field.NAME, Messages.EINVOICE_CHECK_MISSING + " " + Messages.ZUGFERD_FIELD_NAME);
        }
        if (ZugpferdFile.isEmpty(street)) {
            r.error(Field.STREET, Messages.EINVOICE_CHECK_MISSING + " " + Messages.ZUGFERD_FIELD_STREET);
        }
        if (ZugpferdFile.isEmpty(zip)) {
            r.error(Field.ZIP, Messages.EINVOICE_CHECK_MISSING + " " + Messages.ZUGFERD_FIELD_ZIP);
        }
        if (ZugpferdFile.isEmpty(city)) {
            r.error(Field.CITY, Messages.EINVOICE_CHECK_MISSING + " " + Messages.ZUGFERD_FIELD_CITY);
        }
        String code = ZugpferdFile.toCountryCode(country);
        if (code.isEmpty()) {
            r.error(Field.COUNTRY, Messages.EINVOICE_CHECK_MISSING + " " + Messages.ZUGFERD_FIELD_COUNTRY);
        }
        if (ZugpferdFile.isEmpty(email)) {
            r.error(Field.EMAIL, Messages.EINVOICE_CHECK_MISSING + " " + Messages.ZUGFERD_FIELD_EMAIL);
        }
        if (!ZugpferdFile.isEmpty(iban) && !Iban.isValid(iban)) {
            r.error(Field.IBAN, Messages.ZUGFERD_INVALID_IBAN + ": " + iban.trim());
        }

        boolean validVatId = ZugpferdFile.isValidVatId(vatId);
        if (!ZugpferdFile.isEmpty(vatId) && !validVatId) {
            r.hint(Field.VATID, Messages.EINVOICE_CHECK_VATID.toString());
        }
        if (!code.isEmpty() && !ZugpferdFile.isEmpty(homeCountry) && !code.equalsIgnoreCase(homeCountry.trim())
                && !validVatId) {
            r.hint(Field.VATID, Messages.EINVOICE_CHECK_FOREIGN.toString());
        }
        return r;
    }
}
