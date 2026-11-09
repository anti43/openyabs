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
package mpv5.plugin.builtin;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.net.URI;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.YabsExtension;
import mpv5.pos.PosServer;
import mpv5.pos.PosService;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.ModernTheme;

/**
 * Built-in plugin: a cash register in the browser. YaBS serves a touch page
 * (e.g. for a tablet at the counter); the seller signs in with a YaBS user,
 * taps products and books the sale as a paid invoice for the cash customer.
 */
public final class CashRegister implements YabsExtension {

    private ExtensionContext ctx;
    private PosServer server;
    private PosService service;

    @Override
    public String id() {
        return "yabs.cash-register";
    }

    @Override
    public String name() {
        return Messages.POS_NAME.toString();
    }

    @Override
    public String description() {
        return Messages.POS_DESCRIPTION.toString();
    }

    @Override
    public String version() {
        return "0.1";
    }

    @Override
    public String author() {
        return "YaBS";
    }

    @Override
    public void start(ExtensionContext ctx) {
        this.ctx = ctx;
        ctx.addCommand(Messages.POS_OPEN.toString(), Messages.POS_DESCRIPTION.toString(), () -> {
            if (startServer()) {
                browse("http://localhost:" + server.port() + "/");
            }
        });
        ctx.addSettingsPage(Messages.POS_NAME.toString(), Messages.POS_DESCRIPTION.toString(), this::page);
        ensureUser();
        ensureTemplate();
        if (ctx.settings().getBoolean("autostart", false)) {
            startServer();
        }
    }

    @Override
    public void stop() {
        stopServer();
    }

    /**
     * The user of the register ("kasse"), made once with a PIN as password;
     * the PIN is kept in the settings of the plugin to show it
     */
    private void ensureUser() {
        if (!ctx.settings().get("user", "").isEmpty()) {
            return;
        }
        try {
            try {
                mpv5.db.common.DatabaseObject.getObject(mpv5.db.common.Context.getUser(), USER);
                //made elsewhere: its password is not known here
                ctx.settings().set("user", USER);
                return;
            } catch (mpv5.db.common.NodataFoundException e) {
                //make it
            }
            mpv5.db.objects.User me = mpv5.db.objects.User.getCurrentUser();
            mpv5.db.objects.User u = (mpv5.db.objects.User) mpv5.db.common.DatabaseObject.getObject(mpv5.db.common.Context.getUser());
            u.setCname(USER);
            u.setFullname(Messages.POS_NAME.toString());
            u.setInthighestright(mpv5.usermanagement.MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE);
            u.setIsenabled(true);
            u.setGroupsids(1);
            u.setLanguage(me.__getLanguage());
            u.setLocale(me.__getLocale());
            u.setLaf(me.__getLaf());
            String pin = pin();
            u.setPassword(mpv5.utils.text.MD5HashGenerator.getInstance().hashData(pin.toCharArray()));
            if (u.save(true)) {
                ctx.settings().set("user", USER);
                ctx.settings().set("pin", pin);
                ctx.notify(String.format(Messages.POS_USER_CREATED.toString(), USER));
            }
        } catch (Exception e) {
            Log.Debug(CashRegister.class, e);
        }
    }

    private static final String USER = "kasse";

    /**
     * The demo receipt template (80 mm), imported once and assigned to the
     * user of the register for invoices; done in the background
     */
    private void ensureTemplate() {
        if (!ctx.settings().get("template", "").isEmpty()) {
            return;
        }
        ctx.background(() -> {
            try {
                mpv5.db.objects.User kasse = (mpv5.db.objects.User) mpv5.db.common.DatabaseObject.getObject(
                        mpv5.db.common.Context.getUser(), ctx.settings().get("user", USER));
                mpv5.db.objects.Template t = mpv5.handler.FirstStartSetup.importTemplate(kasse, "/extra/Kassenbon.odt",
                        Messages.POS_RECEIPT_TEMPLATE.toString(), "2,4,7,9", mpv5.globals.Constants.TYPE_INVOICE, 60000);
                return t == null ? null : t.__getIDS();
            } catch (Exception e) {
                Log.Debug(CashRegister.class, e);
                return null;
            }
        }, id -> {
            if (id != null) {
                ctx.settings().set("template", String.valueOf(id));
            }
        });
    }

    private int templateId() {
        try {
            return Integer.parseInt(ctx.settings().get("template", "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String pin() {
        return String.format("%06d", new java.security.SecureRandom().nextInt(1000000));
    }

    /**
     * Gives the user of the register a new PIN
     *
     * @return the PIN or null
     */
    private String newPin() {
        try {
            mpv5.db.objects.User u = (mpv5.db.objects.User) mpv5.db.common.DatabaseObject.getObject(mpv5.db.common.Context.getUser(), ctx.settings().get("user", USER));
            String pin = pin();
            u.setPassword(mpv5.utils.text.MD5HashGenerator.getInstance().hashData(pin.toCharArray()));
            if (u.save(true)) {
                ctx.settings().set("pin", pin);
                return pin;
            }
        } catch (Exception e) {
            Log.Debug(CashRegister.class, e);
        }
        return null;
    }

    private int port() {
        try {
            return Integer.parseInt(ctx.settings().get("port", "8090").trim());
        } catch (NumberFormatException e) {
            return 8090;
        }
    }

    private boolean startServer() {
        if (server != null && server.isRunning()) {
            return true;
        }
        service = new PosService(ctx.settings().get("customer", Messages.POS_CASH_CUSTOMER.toString()));
        service.setGroups(PosService.parseGroups(ctx.settings().get("groups", "")));
        server = new PosServer(service, port());
        try {
            String shop = mpv5.db.objects.User.getCurrentUser().getProperty("companyinfo.business");
            server.setShopName(shop == null || shop.equals("null") ? "YaBS" : shop);
            server.setDefaultUser(ctx.settings().get("user", USER));
            server.setReceipts(new PosServer.ReceiptSource() {
                @Override
                public java.io.File receipt(int invoiceId) throws Exception {
                    return mpv5.pos.Receipts.pdf(invoiceId, templateId());
                }

                @Override
                public void print(int invoiceId) throws Exception {
                    mpv5.pos.Receipts.print(mpv5.pos.Receipts.pdf(invoiceId, templateId()), templateId());
                }

                @Override
                public boolean autoPrint() {
                    return templateId() > 0 && ctx.settings().getBoolean("autoprint", false);
                }
            });
        } catch (Exception e) {
            Log.Debug(CashRegister.class, e);
        }
        try {
            server.start();
            ctx.notify(String.format(Messages.POS_STARTED.toString(), server.urls().get(0)));
            return true;
        } catch (Exception e) {
            Log.Debug(CashRegister.class, e);
            ctx.notify(String.format(Messages.POS_PORT_TAKEN.toString(), port()));
            server = null;
            return false;
        }
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    private void browse(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            Log.Debug(CashRegister.class, e);
            ctx.notify(url);
        }
    }

    private JComponent page() {
        ensureUser();
        final JLabel state = new JLabel();
        final JButton toggle = new JButton();
        final JPanel addresses = rows();
        final Runnable[] show = new Runnable[1];

        //settings: port and cash customer (stored when they change)
        final JTextField port = ModernTheme.roomy(new JTextField(String.valueOf(port()), 6));
        final JTextField customer = ModernTheme.roomy(new JTextField(ctx.settings().get("customer", Messages.POS_CASH_CUSTOMER.toString()), 18));
        Runnable store = () -> {
            ctx.settings().set("port", port.getText().trim());
            ctx.settings().set("customer", customer.getText().trim());
        };
        port.addFocusListener(focusLost(store));
        customer.addFocusListener(focusLost(store));
        JCheckBox auto = check(Messages.POS_AUTOSTART.toString(), ctx.settings().getBoolean("autostart", false));
        auto.addActionListener(e -> ctx.settings().setBoolean("autostart", auto.isSelected()));

        //state, start/stop, open
        state.setFont(ModernTheme.font(1, Font.BOLD));
        JButton open = new mpv5.ui.modern.PrimaryButton(Messages.POS_OPEN.toString(), null);
        toggle.addActionListener(e -> {
            store.run();
            if (server != null && server.isRunning()) {
                stopServer();
            } else {
                startServer();
            }
            show[0].run();
        });
        open.addActionListener(e -> {
            store.run();
            if (startServer()) {
                browse("http://localhost:" + server.port() + "/");
            }
            show[0].run();
        });
        mpv5.ui.modern.Card status = new mpv5.ui.modern.Card(Messages.POS_NAME.toString())
                .addFull(line(state, toggle, open))
                .addFull(auto)
                .addFull(Applets.hint(Messages.POS_HINT.toString()));

        //addresses with the IP: links and copy
        show[0] = () -> {
            boolean on = server != null && server.isRunning();
            state.setText(on ? Messages.POS_RUNNING.toString() : Messages.POS_STOPPED.toString());
            state.setForeground(on ? mpv5.ui.misc.RichList.GREEN : ModernTheme.muted());
            toggle.setText(on ? Messages.POS_STOP.toString() : Messages.POS_START.toString());
            addresses.removeAll();
            for (final String u : PosServer.urls(port())) {
                JButton link = new JButton("<html><u>" + u + "</u></html>");
                link.setBorderPainted(false);
                link.setContentAreaFilled(false);
                link.setFocusPainted(false);
                link.setMargin(new java.awt.Insets(0, 0, 0, 0));
                link.setBorder(javax.swing.BorderFactory.createEmptyBorder());
                link.setForeground(ModernTheme.accent());
                link.setFont(ModernTheme.font(1, Font.PLAIN));
                link.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                link.setToolTipText(Messages.POS_LINK_TIP.toString());
                link.addActionListener(e -> {
                    store.run();
                    if (startServer()) {
                        browse(u);
                    }
                    show[0].run();
                });
                JButton copy = new JButton(Messages.POS_COPY.toString());
                copy.addActionListener(e -> java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new java.awt.datatransfer.StringSelection(u), null));
                addresses.add(spread(link, copy));
            }
            addresses.revalidate();
            addresses.repaint();
        };
        mpv5.ui.modern.Card where = new mpv5.ui.modern.Card(Messages.POS_ADDRESSES.toString())
                .addFull(addresses)
                .addFull(Applets.hint(Messages.POS_ADDRESSES_HINT.toString()));

        mpv5.ui.modern.Card settings = new mpv5.ui.modern.Card(Messages.POS_SETTINGS.toString())
                .row(mpv5.ui.modern.Card.field(Messages.POS_PORT.toString(), port, 1),
                        mpv5.ui.modern.Card.field(Messages.POS_CUSTOMER.toString(), customer, 2));

        show[0].run();
        return Applets.page(null, 380, status, signIn(), where, receipt(), groupsChooser(), settings);
    }

    /**
     * The receipt: template, printing after every sale
     */
    private mpv5.ui.modern.Card receipt() {
        javax.swing.JComboBox<Object> templates = new javax.swing.JComboBox<Object>();
        templates.addItem(Messages.POS_NO_RECEIPT.toString());
        int selected = templateId();
        try {
            for (mpv5.db.common.DatabaseObject t : mpv5.db.common.DatabaseObject.getObjects(mpv5.db.common.Context.getTemplate())) {
                final int id = t.__getIDS();
                final String name = t.__getCname();
                Object item = new Object() {
                    @Override
                    public String toString() {
                        return name;
                    }

                    @Override
                    public int hashCode() {
                        return id;
                    }
                };
                templates.addItem(item);
                if (id == selected) {
                    templates.setSelectedItem(item);
                }
            }
        } catch (Exception e) {
            Log.Debug(CashRegister.class, e);
        }
        templates.addActionListener(e -> {
            Object o = templates.getSelectedItem();
            ctx.settings().set("template", templates.getSelectedIndex() <= 0 || o == null ? "0" : String.valueOf(o.hashCode()));
        });
        JCheckBox auto = check(Messages.POS_AUTOPRINT.toString(), ctx.settings().getBoolean("autoprint", false));
        auto.addActionListener(e -> ctx.settings().setBoolean("autoprint", auto.isSelected()));
        return new mpv5.ui.modern.Card(Messages.POS_RECEIPT.toString())
                .row(mpv5.ui.modern.Card.field(Messages.POS_RECEIPT_TEMPLATE_LABEL.toString(), templates))
                .addFull(auto)
                .addFull(Applets.hint(Messages.POS_RECEIPT_HINT.toString()));
    }

    /**
     * User and password of the register, a new password on request
     */
    private mpv5.ui.modern.Card signIn() {
        final JLabel user = new JLabel();
        final JLabel pin = new JLabel();
        user.setFont(ModernTheme.font(3, Font.PLAIN));
        pin.setFont(ModernTheme.font(3, Font.BOLD));
        Runnable show = () -> {
            user.setText(ctx.settings().get("user", USER));
            String p = ctx.settings().get("pin", "");
            pin.setText(p.isEmpty() ? Messages.POS_PASSWORD_UNKNOWN.toString() : p);
        };
        show.run();
        JButton again = new JButton(Messages.POS_NEW_PASSWORD.toString());
        again.addActionListener(e -> {
            if (mpv5.ui.dialogs.Popup.Y_N_dialog(Messages.POS_NEW_PASSWORD_ASK.toString())) {
                if (newPin() == null) {
                    mpv5.ui.dialogs.Popup.notice(Messages.NOT_POSSIBLE.toString());
                }
                show.run();
            }
        });
        return new mpv5.ui.modern.Card(Messages.POS_SIGNIN.toString())
                .row(mpv5.ui.modern.Card.field(Messages.POS_USER.toString(), user),
                        mpv5.ui.modern.Card.field(Messages.POS_PASSWORD.toString(), pin))
                .addFull(line(again))
                .addFull(Applets.hint(Messages.POS_SIGNIN_HINT.toString()));
    }

    /**
     * The product groups sold at the register (none checked: all products);
     * applied at once, also while the register runs
     */
    private mpv5.ui.modern.Card groupsChooser() {
        final java.util.Set<Integer> chosen = PosService.parseGroups(ctx.settings().get("groups", ""));
        JPanel list = new JPanel(new java.awt.GridLayout(0, 2, ModernTheme.px(12), ModernTheme.px(4)));
        list.setOpaque(false);
        try {
            for (mpv5.db.common.DatabaseObject g : mpv5.db.common.DatabaseObject.getObjects(mpv5.db.common.Context.getProductGroup())) {
                final int id = g.__getIDS();
                final JCheckBox c = check(g.__getCname(), chosen.contains(id));
                c.addActionListener(e -> {
                    if (c.isSelected()) {
                        chosen.add(id);
                    } else {
                        chosen.remove(id);
                    }
                    StringBuilder b = new StringBuilder();
                    for (Integer x : chosen) {
                        b.append(b.length() == 0 ? "" : ",").append(x);
                    }
                    ctx.settings().set("groups", b.toString());
                    if (service != null) {
                        service.setGroups(chosen);
                    }
                });
                list.add(c);
            }
        } catch (Exception e) {
            Log.Debug(CashRegister.class, e);
        }
        return new mpv5.ui.modern.Card(Messages.POS_GROUPS.toString())
                .addFull(list)
                .addFull(Applets.hint(Messages.POS_GROUPS_HINT.toString()));
    }

    // ------------------------------------------------------------------ layout helpers
    /**
     * @return a check box without the inner gap, so it lines up with labels
     */
    private static JCheckBox check(String text, boolean selected) {
        JCheckBox c = new JCheckBox(text, selected);
        c.setOpaque(false);
        c.setMargin(new java.awt.Insets(2, 0, 2, 0));
        c.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 0, 2, 0));
        return c;
    }

    /**
     * @return the components in a row, starting at the left edge
     */
    private static JPanel line(JComponent... cs) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        for (int i = 0; i < cs.length; i++) {
            if (i > 0) {
                p.add(javax.swing.Box.createHorizontalStrut(ModernTheme.px(10)));
            }
            p.add(cs[i]);
        }
        return p;
    }

    /**
     * @return a row with one component on the left and one on the right
     */
    private static JPanel spread(JComponent left, JComponent right) {
        JPanel p = new JPanel(new java.awt.BorderLayout(ModernTheme.px(10), 0));
        p.setOpaque(false);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(ModernTheme.px(3), 0, ModernTheme.px(3), 0));
        p.add(left, java.awt.BorderLayout.WEST);
        p.add(right, java.awt.BorderLayout.EAST);
        return p;
    }

    private static JPanel rows() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private static java.awt.event.FocusListener focusLost(Runnable r) {
        return new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                r.run();
            }
        };
    }

    private static final float LEFT_ALIGNMENT_ = java.awt.Component.LEFT_ALIGNMENT;
}
