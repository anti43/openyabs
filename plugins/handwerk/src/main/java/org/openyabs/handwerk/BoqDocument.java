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
package org.openyabs.handwerk;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Format-neutral Bill of Quantities (BoQ / Leistungsverzeichnis) domain model.
 * Used as the common internal representation across GAEB DA XML (3.1–3.3),
 * legacy GAEB 90, generic CSV BoQs, and future European formats such as ÖNORM A
 * 2063 (Austria), SIA 451 (Switzerland), or FIEBDC-3 (Spain).
 */
public final class BoqDocument {

    public enum PositionType {
        NORMAL,
        OPTIONAL,
        ALTERNATIVE,
        BASE,
        TEXT
    }

    public static final class BoqPosition {

        public int id;
        public int boqId;
        public int subItemId;
        public String ordinalNo = "";
        public String sourceItemId = "";
        public String categoryPath = "";
        public PositionType positionType = PositionType.NORMAL;
        public BigDecimal quantity = BigDecimal.ZERO;
        public String unit = "";
        public BigDecimal unitPrice = BigDecimal.ZERO;
        public String shortText = "";
        public String longText = "";
        public String bidderText = "";

        public BigDecimal lineTotal() {
            if (positionType == PositionType.TEXT || quantity == null || unitPrice == null) {
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
            return quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        }

        public boolean isCountedInTotal() {
            return positionType == PositionType.NORMAL || positionType == PositionType.BASE;
        }
    }

    public int id;
    public int itemId;
    public String formatCode = "GAEB_XML_33";
    public String exchangePhase = "83";
    public String projectName = "";
    public String boqLabel = "";
    public String currency = "EUR";
    public String rawSource = "";
    public String importedAt = "";
    public final List<BoqPosition> positions = new ArrayList<BoqPosition>();

    public BigDecimal totalNet() {
        BigDecimal sum = BigDecimal.ZERO;
        for (BoqPosition p : positions) {
            if (p.isCountedInTotal()) {
                sum = sum.add(p.lineTotal());
            }
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    public int countMandatoryUnpriced() {
        int count = 0;
        for (BoqPosition p : positions) {
            if (p.isCountedInTotal()
                    && p.quantity != null && p.quantity.signum() > 0
                    && (p.unitPrice == null || p.unitPrice.signum() <= 0)) {
                count++;
            }
        }
        return count;
    }
}
