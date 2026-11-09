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
        if (ctx.settings().getBoolean("autostart", false)) {
            startServer();
        }
    }

    @Override
    public void stop() {
        stopServer();
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
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Applets.hint(Messages.POS_HINT.toString()));
        p.add(javax.swing.Box.createVerticalStrut(ModernTheme.px(10)));

        JTextField port = ModernTheme.roomy(new JTextField(String.valueOf(port()), 6));
        JTextField customer = ModernTheme.roomy(new JTextField(ctx.settings().get("customer", Messages.POS_CASH_CUSTOMER.toString()), 18));
        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), 0));
        fields.setOpaque(false);
        fields.add(new JLabel(Messages.POS_PORT.toString()));
        fields.add(port);
        fields.add(new JLabel(Messages.POS_CUSTOMER.toString()));
        fields.add(customer);
        fields.setAlignmentX(LEFT_ALIGNMENT_);
        p.add(fields);

        JCheckBox auto = new JCheckBox(Messages.POS_AUTOSTART.toString(), ctx.settings().getBoolean("autostart", false));
        auto.setOpaque(false);
        auto.addActionListener(e -> ctx.settings().setBoolean("autostart", auto.isSelected()));
        p.add(auto);

        p.add(javax.swing.Box.createVerticalStrut(ModernTheme.px(10)));
        p.add(groupsChooser());

        JLabel state = new JLabel();
        state.setFont(ModernTheme.font(1, Font.BOLD));
        JPanel urls = new JPanel();
        urls.setOpaque(false);
        urls.setLayout(new BoxLayout(urls, BoxLayout.Y_AXIS));
        urls.setAlignmentX(LEFT_ALIGNMENT_);
        JButton toggle = new JButton();
        JButton open = new JButton(Messages.POS_OPEN.toString());
        Runnable[] show = new Runnable[1];
        show[0] = () -> {
            boolean on = server != null && server.isRunning();
            state.setText(on ? Messages.POS_RUNNING.toString() : Messages.POS_STOPPED.toString());
            state.setForeground(on ? mpv5.ui.misc.RichList.GREEN : ModernTheme.muted());
            toggle.setText(on ? Messages.POS_STOP.toString() : Messages.POS_START.toString());
            //the addresses with the IP of this computer, a click opens the register
            urls.removeAll();
            JLabel title = new JLabel(Messages.POS_ADDRESSES.toString());
            title.setFont(ModernTheme.font(0, Font.BOLD));
            urls.add(title);
            for (final String u : PosServer.urls(port())) {
                JButton link = new JButton("<html><u>" + u + "</u></html>");
                link.setBorderPainted(false);
                link.setContentAreaFilled(false);
                link.setFocusPainted(false);
                link.setMargin(new java.awt.Insets(2, 0, 2, 0));
                link.setForeground(ModernTheme.accent());
                link.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                link.setToolTipText(Messages.POS_LINK_TIP.toString());
                link.addActionListener(e -> {
                    if (startServer()) {
                        browse(u);
                    }
                    show[0].run();
                });
                JButton copy = new JButton(Messages.ACTIVITY_COPY_PATH.toString().replace("Pfad", "Adresse").replace("path", "address"));
                copy.addActionListener(e -> java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new java.awt.datatransfer.StringSelection(u), null));
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), 0));
                row.setOpaque(false);
                row.setAlignmentX(LEFT_ALIGNMENT_);
                row.add(link);
                row.add(copy);
                urls.add(row);
            }
            urls.revalidate();
            urls.repaint();
        };
        toggle.addActionListener(e -> {
            ctx.settings().set("port", port.getText().trim());
            ctx.settings().set("customer", customer.getText().trim());
            if (server != null && server.isRunning()) {
                stopServer();
            } else {
                startServer();
            }
            show[0].run();
        });
        open.addActionListener(e -> {
            ctx.settings().set("port", port.getText().trim());
            ctx.settings().set("customer", customer.getText().trim());
            if (startServer()) {
                browse("http://localhost:" + server.port() + "/");
            }
            show[0].run();
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(LEFT_ALIGNMENT_);
        buttons.add(state);
        buttons.add(toggle);
        buttons.add(open);
        p.add(javax.swing.Box.createVerticalStrut(ModernTheme.px(10)));
        p.add(buttons);
        p.add(javax.swing.Box.createVerticalStrut(ModernTheme.px(8)));
        p.add(urls);
        show[0].run();
        //rows keep their height, the space goes below
        JPanel top = new JPanel(new java.awt.BorderLayout());
        top.setOpaque(false);
        top.add(p, java.awt.BorderLayout.NORTH);
        return top;
    }

    /**
     * The product groups sold at the register (none checked: all products);
     * applied at once, also while the register runs
     */
    private JComponent groupsChooser() {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setAlignmentX(LEFT_ALIGNMENT_);
        JLabel title = new JLabel(Messages.POS_GROUPS.toString());
        title.setFont(ModernTheme.font(0, Font.BOLD));
        box.add(title);
        box.add(Applets.hint(Messages.POS_GROUPS_HINT.toString()));
        final java.util.Set<Integer> chosen = PosService.parseGroups(ctx.settings().get("groups", ""));
        JPanel list = new JPanel(new mpv5.ui.modern.WrapLayout(FlowLayout.LEFT, ModernTheme.px(8), ModernTheme.px(2)));
        list.setOpaque(false);
        list.setAlignmentX(LEFT_ALIGNMENT_);
        try {
            for (mpv5.db.common.DatabaseObject g : mpv5.db.common.DatabaseObject.getObjects(mpv5.db.common.Context.getProductGroup())) {
                final int id = g.__getIDS();
                final JCheckBox c = new JCheckBox(g.__getCname(), chosen.contains(id));
                c.setOpaque(false);
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
        box.add(list);
        return box;
    }

    private static final float LEFT_ALIGNMENT_ = java.awt.Component.LEFT_ALIGNMENT;
}
