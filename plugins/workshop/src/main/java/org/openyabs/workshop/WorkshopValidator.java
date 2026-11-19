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
package org.openyabs.workshop;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mpv5.plugin.api.ExtensionContext;
import org.openyabs.workshop.WorkshopStore.JobData;
import org.openyabs.workshop.WorkshopStore.Settings;
import org.openyabs.workshop.WorkshopStore.TireStorage;
import org.openyabs.workshop.WorkshopStore.Vehicle;

/**
 * Pure validation, unit conversion, inspection-date parsing, and template
 * placeholder logic for the Car Repair Shop / Kfz-Werkstatt plugin.
 */
public final class WorkshopValidator {

    private static final Pattern VIN_ISO3779 = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");
    private static final Pattern YM_ISO = Pattern.compile("^(\\d{4})-(\\d{1,2})$");
    private static final Pattern MY_SLASH = Pattern.compile("^(\\d{1,2})[./](\\d{4})$");
    private static final Pattern YMD_ISO = Pattern.compile("^(\\d{4})-(\\d{1,2})-(\\d{1,2})$");
    private static final Pattern DMY_DOT = Pattern.compile("^(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4})$");
    private static final double KM_PER_MILE = 1.609344;

    private WorkshopValidator() {
    }

    /**
     * Validates a 17-character VIN according to ISO 3779 (alphanumeric,
     * excluding {@code I}, {@code O}, {@code Q}). Does not require the North
     * American 9th-digit checksum so European VINs are accepted without false
     * positives.
     */
    public static boolean isValidVin(String vin) {
        if (vin == null) {
            return false;
        }
        String cleaned = vin.trim().toUpperCase(Locale.ROOT);
        return VIN_ISO3779.matcher(cleaned).matches();
    }

    /**
     * Converts a mileage reading to kilometers for unit-aware comparisons.
     */
    public static long toKilometers(long mileage, String unit) {
        if (mileage <= 0) {
            return 0L;
        }
        if ("mi".equalsIgnoreCase(unit == null ? "" : unit.trim())) {
            return Math.round(mileage * KM_PER_MILE);
        }
        return mileage;
    }

    /**
     * Parses inspection due dates in {@code YYYY-MM}, {@code MM/YYYY},
     * {@code MM.YYYY}, {@code YYYY-MM-DD}, or {@code DD.MM.YYYY}. Month-only
     * inputs resolve to the last day of that month.
     */
    public static LocalDate parseInspectionDue(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String s = raw.trim();
        try {
            Matcher m = YMD_ISO.matcher(s);
            if (m.matches()) {
                return LocalDate.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
            }
            m = DMY_DOT.matcher(s);
            if (m.matches()) {
                return LocalDate.of(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(1)));
            }
            m = YM_ISO.matcher(s);
            if (m.matches()) {
                return YearMonth.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))).atEndOfMonth();
            }
            m = MY_SLASH.matcher(s);
            if (m.matches()) {
                return YearMonth.of(Integer.parseInt(m.group(2)), Integer.parseInt(m.group(1))).atEndOfMonth();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static boolean isInspectionDueSoon(String raw, LocalDate today, int leadDays) {
        LocalDate due = parseInspectionDue(raw);
        if (due == null || today == null) {
            return false;
        }
        return !due.isAfter(today.plusDays(Math.max(0, leadDays)));
    }

    /**
     * Computes percentage overrun over a quote total, or {@code null} if
     * {@code quoteTotal <= 0} or {@code currentTotal <= quoteTotal}.
     */
    public static BigDecimal quoteOverrunPercent(BigDecimal currentTotal, BigDecimal quoteTotal) {
        if (currentTotal == null || quoteTotal == null || quoteTotal.signum() <= 0) {
            return null;
        }
        if (currentTotal.compareTo(quoteTotal) <= 0) {
            return BigDecimal.ZERO;
        }
        return currentTotal.subtract(quoteTotal)
                .multiply(new BigDecimal("100"))
                .divide(quoteTotal, 1, RoundingMode.HALF_UP);
    }

    /**
     * Validates a workshop order or invoice and returns localized warning
     * messages via {@link ExtensionContext#tr(String, Object...)}.
     */
    public static List<String> validate(
            JobData job,
            Vehicle vehicle,
            BigDecimal currentTotal,
            Long previousMaxMileageKm,
            Settings settings,
            ExtensionContext yabs) {
        List<String> warnings = new ArrayList<String>();
        Settings s = settings == null ? new Settings() : settings;

        if (job == null || (job.vehicleId <= 0 && safe(job.plateSnapshot).isEmpty())) {
            warnings.add(tr(yabs, "val.missingVehicle"));
        }
        if (job == null || job.mileage <= 0) {
            warnings.add(tr(yabs, "val.missingMileage"));
        } else if (previousMaxMileageKm != null && previousMaxMileageKm > 0) {
            long currentKm = toKilometers(job.mileage, job.mileageUnit);
            if (currentKm < previousMaxMileageKm) {
                warnings.add(tr(yabs, "val.mileageDrop", job.mileage, job.mileageUnit, previousMaxMileageKm));
            }
        }

        if (vehicle != null && !safe(vehicle.vin).isEmpty() && !isValidVin(vehicle.vin)) {
            warnings.add(tr(yabs, "val.invalidVin", vehicle.vin));
        }

        if (job != null && !job.overrunApproved && !safe(job.quoteTotalSnapshot).isEmpty() && currentTotal != null) {
            BigDecimal quoteTotal = parseMoney(job.quoteTotalSnapshot);
            if (quoteTotal != null && quoteTotal.signum() > 0) {
                BigDecimal pct = quoteOverrunPercent(currentTotal, quoteTotal);
                if (pct != null && pct.compareTo(BigDecimal.valueOf(s.quoteOverrunPercent)) > 0) {
                    String qNum = safe(job.quoteNumberSnapshot).isEmpty() ? "" : "#" + safe(job.quoteNumberSnapshot);
                    warnings.add(tr(yabs, "val.quoteOverrun",
                            currentTotal.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                            qNum,
                            quoteTotal.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                            pct.toPlainString(),
                            s.quoteOverrunPercent));
                }
            }
        }
        return warnings;
    }

    /**
     * Builds all {@code [ws.*]} template placeholders for ODT/FODT/PDF export.
     * Uses the job's point-in-time {@code plateSnapshot} and {@code mileage}
     * when present so historical documents remain immutable.
     */
    public static Map<String, Object> templateFields(
            Vehicle vehicle,
            JobData job,
            TireStorage tire,
            Settings settings,
            ExtensionContext yabs) {
        Settings s = settings == null ? new Settings() : settings;
        Map<String, Object> m = new LinkedHashMap<String, Object>();

        String plate = job != null && !safe(job.plateSnapshot).isEmpty()
                ? safe(job.plateSnapshot)
                : (vehicle == null ? "" : safe(vehicle.licensePlate));
        long mileageVal = job != null && job.mileage > 0
                ? job.mileage
                : (vehicle == null ? 0L : vehicle.mileage);
        String mileageUnit = job != null && !safe(job.mileageUnit).isEmpty()
                ? safe(job.mileageUnit)
                : (vehicle == null ? s.distanceUnit : safe(vehicle.mileageUnit));

        m.put("ws.plate", plate);
        m.put("ws.vin", vehicle == null ? "" : safe(vehicle.vin));
        m.put("ws.makeModel", vehicle == null ? "" : safe(vehicle.makeModel));
        m.put("ws.hsnTsn", vehicle == null ? "" : safe(vehicle.hsnTsn));
        m.put("ws.firstRegistration", vehicle == null ? "" : safe(vehicle.firstRegistration));
        m.put("ws.mileage", mileageVal > 0 ? String.valueOf(mileageVal) : "");
        m.put("ws.mileageUnit", mileageUnit);
        m.put("ws.inspectionDue", vehicle == null ? "" : safe(vehicle.inspectionDue));
        m.put("ws.dropoffDate", job == null ? "" : safe(job.dropoffDate));
        m.put("ws.pickupDate", job == null ? "" : safe(job.pickupDate));
        m.put("ws.complaint", job == null ? "" : safe(job.complaint));
        m.put("ws.workSummary", job == null ? "" : safe(job.workSummary));
        m.put("ws.oldParts", job == null || safe(job.oldPartsHandling).isEmpty()
                ? "" : tr(yabs, "job.oldParts." + safe(job.oldPartsHandling)));
        m.put("ws.awDefinition", formatAwDefinition(s, yabs));
        m.put("ws.quoteNumber", job == null ? "" : safe(job.quoteNumberSnapshot));
        m.put("ws.quoteTotal", job == null ? "" : safe(job.quoteTotalSnapshot));

        if (tire != null) {
            String seasonLabel = tr(yabs, "tire.season." + safe(tire.season));
            String rimLabel = tr(yabs, "tire.rim." + safe(tire.rimType));
            StringBuilder info = new StringBuilder();
            if (!seasonLabel.isEmpty()) {
                info.append(seasonLabel);
            }
            if (vehicle != null && !safe(vehicle.tireSize).isEmpty()) {
                if (info.length() > 0) {
                    info.append(" ");
                }
                info.append(safe(vehicle.tireSize));
            }
            if (!rimLabel.isEmpty()) {
                if (info.length() > 0) {
                    info.append(" (").append(rimLabel).append(")");
                } else {
                    info.append(rimLabel);
                }
            }
            if (!safe(tire.dotCodes).isEmpty()) {
                info.append(", DOT: ").append(safe(tire.dotCodes));
            }
            m.put("ws.tireInfo", info.toString());
            m.put("ws.tireStorageLocation", safe(tire.storageLocation));
            m.put("ws.tireTreadDepth", tire.treadSummary());
            m.put("ws.tireCondition", safe(tire.conditionNotes));
        } else {
            m.put("ws.tireInfo", vehicle == null ? "" : safe(vehicle.tireSize));
            m.put("ws.tireStorageLocation", "");
            m.put("ws.tireTreadDepth", "");
            m.put("ws.tireCondition", "");
        }
        return m;
    }

    public static String formatAwDefinition(Settings s, ExtensionContext yabs) {
        if (s == null || s.awPerHour <= 0) {
            return tr(yabs, "aw.hourly");
        }
        BigDecimal minutes = new BigDecimal("60")
                .divide(BigDecimal.valueOf(s.awPerHour), 1, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return tr(yabs, "aw.definition", s.awPerHour, minutes.toPlainString());
    }

    static BigDecimal parseMoney(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String cleaned = raw.trim().replaceAll("[^0-9.,-]", "");
        if (cleaned.isEmpty()) {
            return null;
        }
        if (cleaned.contains(",") && cleaned.contains(".")) {
            if (cleaned.lastIndexOf(',') > cleaned.lastIndexOf('.')) {
                cleaned = cleaned.replace(".", "").replace(',', '.');
            } else {
                cleaned = cleaned.replace(",", "");
            }
        } else if (cleaned.contains(",")) {
            cleaned = cleaned.replace(',', '.');
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String tr(ExtensionContext yabs, String key, Object... args) {
        return yabs == null ? key : yabs.tr(key, args);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
