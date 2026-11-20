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

import java.math.BigDecimal;
import mpv5.globals.Messages;

/**
 * The VAT category of a tax rate (EN 16931 BT-151 / BT-118, code list UNCL5305),
 * with the default exemption reason code (VATEX, BT-121) and the default note
 * printed on the invoice. The wording of the notes is in the language files.
 */
public enum VatCategory {

    /**
     * standard rate
     */
    S(Messages.VATCAT_S, null, null),
    /**
     * zero rated goods
     */
    Z(Messages.VATCAT_Z, null, null),
    /**
     * exempt from VAT
     */
    E(Messages.VATCAT_E, null, Messages.VATCAT_NOTE_E),
    /**
     * reverse charge (the recipient owes the VAT, e.g. services to EU
     * businesses, § 13b UStG)
     */
    AE(Messages.VATCAT_AE, "VATEX-EU-AE", Messages.VATCAT_NOTE_AE),
    /**
     * intra-community supply of goods (§ 4 Nr. 1b UStG)
     */
    K(Messages.VATCAT_K, "VATEX-EU-IC", Messages.VATCAT_NOTE_K),
    /**
     * export outside the EU (§ 4 Nr. 1a UStG)
     */
    G(Messages.VATCAT_G, "VATEX-EU-G", Messages.VATCAT_NOTE_G),
    /**
     * not subject to VAT (outside the scope)
     */
    O(Messages.VATCAT_O, "VATEX-EU-O", Messages.VATCAT_NOTE_O),
    /**
     * Canary Islands general indirect tax (IGIC)
     */
    L(Messages.VATCAT_L, null, null),
    /**
     * tax for production, services and importation in Ceuta and Melilla
     * (IPSI)
     */
    M(Messages.VATCAT_M, null, null);

    private final Messages label;
    private final String reasonCode;
    private final Messages note;

    VatCategory(Messages label, String reasonCode, Messages note) {
        this.label = label;
        this.reasonCode = reasonCode;
        this.note = note;
    }

    /**
     * @return the code (UNCL5305)
     */
    public String code() {
        return name();
    }

    /**
     * @return the default exemption reason code (VATEX), null if none
     */
    public String defaultReasonCode() {
        return reasonCode;
    }

    /**
     * @return the default note on the invoice (the language of the user), ""
     * if none
     */
    public String defaultNote() {
        return note == null ? "" : note.toString();
    }

    /**
     * @return true if the e-invoice needs an exemption reason (text or code)
     * for this category (BR-E-10, BR-AE-10, BR-IC-10, BR-G-10, BR-O-10)
     */
    public boolean needsReason() {
        return this == E || this == AE || this == K || this == G || this == O;
    }

    /**
     * @return true if the rate of this category is always 0 %
     */
    public boolean isZeroRate() {
        return this != S && this != L && this != M;
    }

    /**
     * @return the label and the code, e.g. "Reverse charge (AE)"
     */
    @Override
    public String toString() {
        return label + " (" + name() + ")";
    }

    /**
     * The category of a tax rate: the stored code if it fits the rate, else S
     * for a rate above 0 % and E for 0 % (as before the categories existed)
     *
     * @param code the stored code, may be null or empty
     * @param rate the rate in percent, may be null (0 %)
     * @return the category
     */
    public static VatCategory of(String code, BigDecimal rate) {
        boolean zero = rate == null || rate.signum() == 0;
        VatCategory c = null;
        if (code != null && !code.trim().isEmpty()) {
            try {
                c = valueOf(code.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                c = null;
            }
        }
        if (c == null || c.isZeroRate() != zero) {
            //no code, an unknown code or one that does not fit the rate (e.g. S with 0 %)
            return zero ? E : (c == L || c == M ? c : S);
        }
        return c;
    }
}
