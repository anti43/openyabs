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
        server = new PosServer(new PosService(ctx.settings().get("customer", Messages.POS_CASH_CUSTOMER.toString())), port());
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

        JLabel state = new JLabel();
        state.setFont(ModernTheme.font(1, Font.BOLD));
        JLabel urls = new JLabel();
        JButton toggle = new JButton();
        JButton open = new JButton(Messages.POS_OPEN.toString());
        Runnable show = () -> {
            boolean on = server != null && server.isRunning();
            state.setText(on ? Messages.POS_RUNNING.toString() : Messages.POS_STOPPED.toString());
            state.setForeground(on ? mpv5.ui.misc.RichList.GREEN : ModernTheme.muted());
            urls.setText(on ? "<html>" + Messages.POS_ADDRESSES + "<br>" + String.join("<br>", server.urls()) + "</html>" : " ");
            toggle.setText(on ? Messages.POS_STOP.toString() : Messages.POS_START.toString());
        };
        toggle.addActionListener(e -> {
            ctx.settings().set("port", port.getText().trim());
            ctx.settings().set("customer", customer.getText().trim());
            if (server != null && server.isRunning()) {
                stopServer();
            } else {
                startServer();
            }
            show.run();
        });
        open.addActionListener(e -> {
            ctx.settings().set("port", port.getText().trim());
            ctx.settings().set("customer", customer.getText().trim());
            if (startServer()) {
                browse("http://localhost:" + server.port() + "/");
            }
            show.run();
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), 0));
        buttons.setOpaque(false);
        buttons.add(state);
        buttons.add(toggle);
        buttons.add(open);
        p.add(javax.swing.Box.createVerticalStrut(ModernTheme.px(10)));
        p.add(buttons);
        p.add(javax.swing.Box.createVerticalStrut(ModernTheme.px(6)));
        p.add(urls);
        show.run();
        return p;
    }

    private static final float LEFT_ALIGNMENT_ = java.awt.Component.LEFT_ALIGNMENT;
}
