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
package org.openyabs.gmbh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Item;
import mpv5.db.objects.Template;
import mpv5.globals.Constants;
import mpv5.handler.TemplateHandler;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.YabsExtension;
import mpv5.ui.panels.DataPanel;

/**
 * GmbH: the mandatory details of § 35a GmbHG on all documents (template
 * fields, a demo invoice template, a check before sending) and a check of
 * the advance VAT return as tab of the journal. (The DATEV document package is
 * part of the DATEV plugin.)
 */
public final class GmbhPlugin implements YabsExtension {

    public static final String ID = "org.openyabs.gmbh";
    static final String I18N_EN = "/org/openyabs/gmbh/i18n/messages_en.properties";
    static final String I18N_DE = "/org/openyabs/gmbh/i18n/messages_de.properties";

    private ExtensionContext ctx;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return ctx != null ? ctx.tr("plugin.name") : "GmbH";
    }

    @Override
    public String description() {
        return ctx != null ? ctx.tr("plugin.description")
                : "Mandatory details of a GmbH (§ 35a GmbHG) on all documents, demo invoice template, advance VAT return check.";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String author() {
        return "OpenYaBS Community";
    }

    @Override
    public String minYabsVersion() {
        return "2.0.10";
    }

    @Override
    public void start(final ExtensionContext yabs) throws Exception {
        this.ctx = yabs;
        ClassLoader cl = GmbhPlugin.class.getClassLoader();
        yabs.addTranslationBundle("en", I18N_EN, cl);
        yabs.addTranslationBundle("de", I18N_DE, cl);

        yabs.addSetup(yabs.tr("cmd.setup.title"), yabs.tr("settings.title"), yabs.tr("settings.description"),
                () -> GmbhSettingsPage.create(yabs, this));

        yabs.addTemplateFields(obj -> templateFields());

        yabs.addMailWarningProvider(this::mailWarning);

        yabs.addCommand(yabs.tr("cmd.check.title"), yabs.tr("cmd.check.desc"), this::checkCurrent);
        yabs.addCommand(yabs.tr("cmd.ustva.title"), yabs.tr("cmd.ustva.desc"),
                () -> yabs.openTab(yabs.tr("ustva.tab"), UstvaPanel.create(yabs)));

        yabs.addJournalTab(yabs.tr("ustva.tab"), () -> UstvaPanel.create(yabs));
    }

    // ------------------------------------------------------------- details
    boolean warnOnMail() {
        return ctx.settings().getBoolean("warnOnMail", true);
    }

    void setWarnOnMail(boolean b) {
        ctx.settings().setBoolean("warnOnMail", b);
    }

    boolean warnOnTemplate() {
        return ctx.settings().getBoolean("warnOnTemplate", true);
    }

    void setWarnOnTemplate(boolean b) {
        ctx.settings().setBoolean("warnOnTemplate", b);
    }

    String legalLine(GmbhData d) {
        return d.legalLine(ctx.tr("label.seat"), ctx.tr("label.register"), ctx.tr("label.directors"), ctx.tr("label.board"));
    }

    private Map<String, Object> templateFields() {
        try {
            return GmbhData.load().templateFields(ctx.tr("label.seat"), ctx.tr("label.register"),
                    ctx.tr("label.directors"), ctx.tr("label.board"));
        } catch (Exception e) {
            Log.Debug(GmbhPlugin.class, e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * @param keys keys of {@link GmbhData#KEYS}
     * @return their names, separated by ", "
     */
    String names(List<String> keys) {
        List<String> l = new ArrayList<String>();
        for (String k : keys) {
            l.add(ctx.tr("field." + k));
        }
        return String.join(", ", l);
    }

    /**
     * @param item
     * @return true for the documents sent to customers (business letters)
     */
    static boolean isBusinessLetter(Item item) {
        switch (item.__getInttype()) {
            case Constants.TYPE_INVOICE:
            case Constants.TYPE_CREDIT:
            case Constants.TYPE_DEPOSIT:
            case Constants.TYPE_PART_PAYMENT:
            case Constants.TYPE_OFFER:
            case Constants.TYPE_ORDER:
            case Constants.TYPE_ORDER_CONFIRMATION:
            case Constants.TYPE_DELIVERY_NOTE:
            case Constants.TYPE_REMINDER:
                return true;
            default:
                return false;
        }
    }

    /**
     * @param item
     * @return what is missing for § 35a GmbHG on the document, empty if
     * nothing
     */
    List<String> problems(Item item) {
        List<String> p = new ArrayList<String>();
        List<String> missing = GmbhData.load().missing();
        if (!missing.isEmpty()) {
            p.add(ctx.tr("check.missing", names(missing)));
        }
        if (item != null && warnOnTemplate()) {
            try {
                Template t = TemplateHandler.loadTemplate(item);
                if (t != null && !GmbhTemplate.printsDetails(t)) {
                    p.add(ctx.tr("check.template", t.__getCname()));
                }
            } catch (Exception e) {
                Log.Debug(GmbhPlugin.class, e.getMessage());
            }
        }
        return p;
    }

    private String mailWarning(DatabaseObject obj) {
        if (!warnOnMail() || !(obj instanceof Item) || !isBusinessLetter((Item) obj)) {
            return null;
        }
        List<String> p = problems((Item) obj);
        return p.isEmpty() ? null : ctx.tr("check.mail") + "\n• " + String.join("\n• ", p);
    }

    /**
     * Shows the check of the mandatory details for the document of the
     * selected tab (or only the company details)
     */
    void checkCurrent() {
        DataPanel v = ctx.currentView();
        Item item = v != null && v.getDataOwner() instanceof Item ? (Item) v.getDataOwner() : null;
        ctx.background(() -> problems(item), p -> {
            String text = p.isEmpty()
                    ? ctx.tr(item == null ? "check.okCompany" : "check.ok") + "\n\n" + legalLine(GmbhData.load())
                    : String.join("\n", p);
            JOptionPane.showMessageDialog(null, text, ctx.tr("cmd.check.title"),
                    p.isEmpty() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        });
    }

    /**
     * Imports the demo invoice template of a GmbH (once)
     */
    void importTemplate() {
        final String name = ctx.tr("template.name");
        ctx.background(() -> {
            try {
                if (GmbhTemplate.exists(name)) {
                    return ctx.tr("template.exists", name);
                }
                Template t = GmbhTemplate.importDemo(name);
                return t == null ? ctx.tr("template.failed", "-") : ctx.tr("template.done", name);
            } catch (Exception e) {
                Log.Debug(GmbhPlugin.class, e);
                return ctx.tr("template.failed", e.getMessage());
            }
        }, msg -> ctx.popup(msg));
    }

    @Override
    public void stop() {
        this.ctx = null;
    }
}
