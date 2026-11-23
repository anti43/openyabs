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
package mpv5.ui.dialogs.belegwizard;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.globals.GlobalSettings;
import mpv5.globals.Messages;
import mpv5.handler.FormatHandler;
import mpv5.handler.ItemService;
import mpv5.handler.TemplateHandler;
import mpv5.ui.panels.ItemPositionsTable;
import mpv5.utils.date.DateConverter;
import mpv5.utils.export.Export;
import mpv5.utils.export.ODTFile2;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;

/**
 * Non-visual business logic for the Beleg-Wizard: position extraction,
 * unsaved draft PDF/HTML preview rendering, and persisting the final
 * {@link Item} with all {@link SubItem}s.
 */
public final class BelegWizardService {

    private BelegWizardService() {
    }

    /**
     * Extracts all non-blank position rows from the positions table model as
     * calculated {@link SubItem} instances (matching {@link SubItem#saveModel}).
     */
    public static List<SubItem> extractPositions(MPTableModel model) {
        List<SubItem> result = new ArrayList<SubItem>();
        if (model == null) {
            return result;
        }
        List<Object[]> valid = model.getValidRows(new int[]{4});
        int order = 0;
        for (Object[] raw : valid) {
            if (raw == null || raw.length <= 4 || raw[4] == null || raw[4].toString().trim().isEmpty()) {
                continue;
            }
            Object[] row = new Object[Math.max(raw.length, 18)];
            for (int j = 0; j < row.length; j++) {
                row[j] = (j < raw.length && raw[j] != null) ? raw[j] : "";
            }
            SubItem it = new SubItem();
            it.setOrdernr(order);
            it.setCountvalue(BigDecimal.valueOf(order + 1));
            it.setCname(String.valueOf(row[14]));
            it.setDescription(String.valueOf(row[4]));
            it.setMeasure(String.valueOf(row[3]));
            it.setQuantityvalue(FormatNumber.getBigDecimal(row[2]));
            it.setExternalvalue(FormatNumber.getBigDecimal(row[5]));
            it.setInternalvalue(FormatNumber.getBigDecimal(row[5]));
            it.setTaxpercentvalue(FormatNumber.getBigDecimal(row[6]));
            it.setOriginalproductsids(SubItem.productIdOf(row[10]));
            it.setLinkurl(String.valueOf(row[13]));
            if (!String.valueOf(row[15]).trim().isEmpty()) {
                it.setDiscount(FormatNumber.getBigDecimal(row[15]));
            }
            SubItem.calculate(it);
            result.add(it);
            order++;
        }
        return result;
    }

    /**
     * Builds a transient (unsaved) {@link Item} populated with the wizard's
     * state and calculated totals for preview rendering.
     */
    public static Item buildDraftItem(BelegWizardState state, BigDecimal[] totals) {
        Item item = (Item) ItemService.contextOf(state.getItemType()).getNewObject();
        item.avoidNulls();
        item.setInttype(state.getItemType());
        if (state.getReceiver() != null && state.getReceiver().isExisting()) {
            item.setContactsids(state.getReceiver().__getIDS());
        }
        item.setGroupsids(state.getGroupId());
        item.setAccountsids(state.getAccountId());
        item.setIntstatus(state.getStatusId());
        User u = User.getCurrentUser();
        item.setIntaddedby(u != null ? u.__getIDS() : 1);
        Date docDate = state.getDocDate();
        item.setDateadded(docDate);
        item.setDatetodo(state.getServiceDate() != null ? state.getServiceDate() : docDate);
        item.setDateend(state.getDueDate() != null ? state.getDueDate() : docDate);
        item.setCname(state.getTitle().trim());
        item.setDescription(state.getNotes());
        item.setReforderids(-1);
        item.setRefinvoiceids(0);
        BigDecimal net = totals != null && totals.length > 0 && totals[0] != null ? totals[0] : BigDecimal.ZERO;
        BigDecimal tax = totals != null && totals.length > 1 && totals[1] != null ? totals[1] : BigDecimal.ZERO;
        BigDecimal disc = totals != null && totals.length > 2 && totals[2] != null ? totals[2] : BigDecimal.ZERO;
        BigDecimal discGros = totals != null && totals.length > 3 && totals[3] != null ? totals[3] : BigDecimal.ZERO;
        item.setNetvalue(net);
        item.setTaxvalue(tax);
        item.setDiscountvalue(disc);
        item.setDiscountGrosvalue(discGros);
        item.defineFormatHandler(new FormatHandler(item));
        return item;
    }

    /**
     * Looks up the template assigned for the state's group and document type.
     */
    public static Template findTemplate(BelegWizardState state) {
        if (state == null) {
            return null;
        }
        return TemplateHandler.loadTemplate(Long.valueOf(state.getGroupId()), state.getItemType());
    }

    /**
     * Renders a temporary PDF preview from the assigned ODT template using the
     * unsaved draft data and in-memory positions. Returns {@code null} if no
     * template is configured for the selected type and group.
     */
    public static File renderPreviewPdf(BelegWizardState state, List<SubItem> positions, BigDecimal[] totals) throws Exception {
        Template tpl = findTemplate(state);
        if (tpl == null) {
            return null;
        }
        Item draft = buildDraftItem(state, totals);
        Map<String, Object> fields = draft.getFormFields();
        ArrayList<String[]> tableRows = new ArrayList<String[]>();
        boolean hideCountForText = GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.hidecountfortext", true);
        int skipCount = 0;
        for (int i = 0; i < positions.size(); i++) {
            SubItem s = positions.get(i);
            String[] arr = s.toStringArray();
            if (hideCountForText) {
                if (s.getInttype() == SubItem.TYPE_TEXT) {
                    skipCount--;
                } else {
                    arr[0] = String.valueOf((i + 1) + skipCount);
                }
            } else {
                arr[0] = String.valueOf(i + 1);
            }
            tableRows.add(arr);
        }

        File templateFile = tpl.getFile();
        File targetPdf = FileDirectoryHandler.getTempFile("pdf");
        Export ex = new Export(tpl);
        ex.putAll(fields);
        ex.put(ODTFile2.KEY_TABLE + "1", tableRows);
        ex.setTemplate(new ODTFile2(templateFile.getPath()));
        ex.setTargetFile(targetPdf);
        Exception failure = ex.waitFor();
        if (failure != null) {
            throw failure;
        }
        return targetPdf;
    }

    /**
     * Builds a clean HTML preview of the draft document, used as fallback when
     * no print template is configured or LibreOffice rendering is unavailable.
     */
    public static String buildHtmlPreview(BelegWizardState state, List<SubItem> positions, BigDecimal[] totals, String notice) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:sans-serif;font-size:11pt;color:#1e293b;margin:14px;background:#ffffff;'>");
        if (notice != null && !notice.trim().isEmpty()) {
            sb.append("<div style='background:#fef3c7;border:1px solid #f59e0b;padding:8px 10px;margin-bottom:12px;color:#92400e;font-size:10pt;'>")
              .append(esc(notice))
              .append("</div>");
        }

        sb.append("<div style='border-bottom:2px solid #2563eb;padding-bottom:8px;margin-bottom:14px;'>")
          .append("<h2 style='margin:0;color:#0f172a;'>").append(esc(Item.getTypeString(state.getItemType()))).append("</h2>");
        if (!state.getTitle().trim().isEmpty()) {
            sb.append("<div style='color:#475569;margin-top:2px;'>").append(esc(state.getTitle().trim())).append("</div>");
        }
        sb.append("<div style='color:#64748b;font-size:9.5pt;margin-top:4px;'>")
          .append(esc(Messages.ITEM_DATE_DOCUMENT.toString())).append(": ").append(esc(DateConverter.getDefDateString(state.getDocDate())));
        if (state.getDueDate() != null) {
            sb.append(" &nbsp;&middot;&nbsp; ").append(esc(bundleText("ItemPanel2.date3._Label", "Due"))).append(": ")
              .append(esc(DateConverter.getDefDateString(state.getDueDate())));
        }
        sb.append("</div></div>");

        Contact c = state.getReceiver();
        if (c != null) {
            sb.append("<div style='margin-bottom:14px;padding:10px;background:#f8fafc;border:1px solid #e2e8f0;'>");
            if (c.__getCompany() != null && !c.__getCompany().trim().isEmpty()) {
                sb.append("<b>").append(esc(c.__getCompany())).append("</b><br>");
            }
            String fullName = ((c.__getPrename() != null ? c.__getPrename().trim() + " " : "")
                    + (c.__getCname() != null ? c.__getCname().trim() : "")).trim();
            if (!fullName.isEmpty() && !fullName.equals(c.__getCompany())) {
                sb.append(esc(fullName)).append("<br>");
            }
            if (c.__getStreet() != null && !c.__getStreet().trim().isEmpty()) {
                sb.append(esc(c.__getStreet())).append("<br>");
            }
            String cityLine = ((c.__getZip() != null ? c.__getZip().trim() + " " : "")
                    + (c.__getCity() != null ? c.__getCity().trim() : "")).trim();
            if (!cityLine.isEmpty()) {
                sb.append(esc(cityLine));
            }
            sb.append("</div>");
        }

        String[] colNames = Messages.TEMPLATES_COLUMNS.toString().split("\\|");
        String colQty = colNames.length > 1 ? colNames[1] : "Qty";
        String colDesc = colNames.length > 3 ? colNames[3] : Messages.CSV_F_DESCRIPTION.toString();
        String colPrice = colNames.length > 4 ? colNames[4] : "Price";
        String colTax = colNames.length > 6 ? colNames[6] : Messages.TAXES_RATE.toString();
        String colGross = colNames.length > 8 ? colNames[8] : bundleText("ItemPanel2.jLabel3.text", "Total");

        sb.append("<table width='100%' cellspacing='0' cellpadding='5' style='border-collapse:collapse;font-size:10pt;'>")
          .append("<tr style='background:#f1f5f9;border-bottom:1px solid #cbd5e1;'>")
          .append("<th align='left'>#</th>")
          .append("<th align='left'>").append(esc(colDesc)).append("</th>")
          .append("<th align='right'>").append(esc(colQty)).append("</th>")
          .append("<th align='right'>").append(esc(colPrice)).append("</th>")
          .append("<th align='right'>").append(esc(colTax)).append("</th>")
          .append("<th align='right'>").append(esc(colGross)).append("</th>")
          .append("</tr>");
        for (int i = 0; i < positions.size(); i++) {
            SubItem s = positions.get(i);
            sb.append("<tr style='border-bottom:1px solid #e2e8f0;'>")
              .append("<td>").append(i + 1).append("</td>")
              .append("<td>").append(esc(s.__getDescription()).replace("\n", "<br>")).append("</td>")
              .append("<td align='right'>").append(esc(FormatNumber.formatDezimal(s.__getQuantityvalue())))
              .append(s.__getMeasure() != null && !s.__getMeasure().trim().isEmpty() ? " " + esc(s.__getMeasure()) : "").append("</td>")
              .append("<td align='right'>").append(esc(FormatNumber.formatLokalCurrency(s.__getExternalvalue()))).append("</td>")
              .append("<td align='right'>").append(esc(FormatNumber.formatPercent(s.__getTaxpercentvalue()))).append("</td>")
              .append("<td align='right'>").append(esc(FormatNumber.formatLokalCurrency(s.__getTotalbrutvalue()))).append("</td>")
              .append("</tr>");
        }
        sb.append("</table>");

        BigDecimal net = totals != null && totals.length > 0 && totals[0] != null ? totals[0] : BigDecimal.ZERO;
        BigDecimal tax = totals != null && totals.length > 1 && totals[1] != null ? totals[1] : BigDecimal.ZERO;
        BigDecimal disc = totals != null && totals.length > 2 && totals[2] != null ? totals[2] : BigDecimal.ZERO;
        BigDecimal gross = net.subtract(disc).add(tax);

        sb.append("<div style='margin-top:14px;text-align:right;font-size:10.5pt;'>")
          .append("<div>").append(esc(bundleText("ItemPanel2.jLabel1.text", "Net"))).append(": ")
          .append(esc(FormatNumber.formatLokalCurrency(net))).append("</div>");
        if (disc.signum() != 0) {
            sb.append("<div>").append(esc(bundleText("ItemPanel2.jLabel5.text_1", "Discount"))).append(": -")
              .append(esc(FormatNumber.formatLokalCurrency(disc))).append("</div>");
        }
        sb.append("<div>").append(esc(bundleText("ItemPanel2.jLabel2.text", "Tax"))).append(": ")
          .append(esc(FormatNumber.formatLokalCurrency(tax))).append("</div>")
          .append("<div style='font-size:12pt;font-weight:bold;color:#1d4ed8;margin-top:4px;'>")
          .append(esc(bundleText("ItemPanel2.jLabel3.text", "Total"))).append(": ")
          .append(esc(FormatNumber.formatLokalCurrency(gross))).append("</div></div>");

        if (!state.getNotes().trim().isEmpty()) {
            sb.append("<div style='margin-top:14px;padding-top:8px;border-top:1px solid #e2e8f0;color:#475569;font-size:10pt;'>")
              .append(esc(state.getNotes().trim()).replace("\n", "<br>"))
              .append("</div>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    static String bundleText(String key, String fallback) {
        try {
            String v = mpv5.i18n.LanguageManager.getBundle().getString(key);
            return (v != null && !v.trim().isEmpty()) ? v.replaceAll("\\s*:\\s*$", "") : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * Saves the wizard's document and all its positions to the database,
     * auto-generating the document number, and returns the freshly reloaded
     * persisted {@link Item} ready to be opened in the normal document view.
     */
    public static Item saveDocument(BelegWizardState state, ItemPositionsTable positions) throws Exception {
        if (state == null || state.getReceiver() == null || !state.getReceiver().isExisting()) {
            throw new IllegalStateException("Receiver is required");
        }
        if (positions == null) {
            throw new IllegalStateException("Positions table is required");
        }
        positions.prepareSave();
        positions.recalculate();
        List<SubItem> extracted = extractPositions(positions.getModel());
        if (extracted.isEmpty()) {
            throw new IllegalStateException("At least one position is required");
        }

        BigDecimal[] totals = positions.getTotals();
        Item item = buildDraftItem(state, totals);
        if (!item.save()) {
            throw new IllegalStateException("Document could not be saved");
        }
        Date wantedDate = state.getDocDate();
        if (wantedDate != null && item.__getDateadded() != null
                && !DateConverter.getDefDateString(wantedDate).equals(DateConverter.getDefDateString(item.__getDateadded()))) {
            item.setDateadded(wantedDate);
            item.save(true);
        }
        ItemService.savePositions(item, positions.getModel(), false, positions.getUsedOrders(), true);
        Context ctx = ItemService.contextOf(state.getItemType());
        return (Item) DatabaseObject.getObject(ctx, item.__getIDS(), true);
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
