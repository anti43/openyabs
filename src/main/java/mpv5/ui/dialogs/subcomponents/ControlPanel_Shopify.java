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
package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import mpv5.data.PropertyStore;
import mpv5.globals.GlobalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.PrimaryButton;
import mpv5.webshop.ShopifyImport;

/**
 * Webshop: Shopify. Shop address and access token, what the orders become,
 * test the connection, import the new orders.
 */
public class ControlPanel_Shopify extends JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    public final String UNAME = "shopify";
    private static final String DOCS = "https://github.com/anti43/openyabs/wiki/Shopify-(EN)";
    private final JTextField shop = new JTextField(28);
    private final JPasswordField token = new JPasswordField(28);
    private final JTextField version = new JTextField(10);
    private final JComboBox<String> doctype = new JComboBox<String>();
    private final JLabel last = new JLabel(" ");
    private final JPanel actions;

    public ControlPanel_Shopify() {
        super(new BorderLayout());
        setOpaque(false);
        doctype.addItem(Messages.TYPE_ORDER.toString());
        doctype.addItem(Messages.TYPE_INVOICE.toString());

        Card connection = new Card(Messages.SHOPIFY_CARD_CONNECTION.toString())
                .row(Card.field(Messages.SHOPIFY_SHOP.toString(), shop))
                .row(Card.field(Messages.SHOPIFY_TOKEN.toString(), token))
                .row(Card.field(Messages.SHOPIFY_VERSION.toString(), version))
                .addFull(Applets.hint(Messages.SHOPIFY_TOKEN_HINT.toString()));
        Card orders = new Card(Messages.SHOPIFY_CARD_ORDERS.toString())
                .row(Card.field(Messages.SHOPIFY_DOCTYPE.toString(), doctype))
                .addFull(last)
                .addFull(Applets.hint(Messages.SHOPIFY_ORDERS_HINT.toString()));
        add(Applets.page(Messages.SHOPIFY_INFO.toString(), 420, connection, orders), BorderLayout.CENTER);

        JButton test = new JButton(Messages.SHOPIFY_TEST.toString());
        test.addActionListener(e -> test());
        JButton docs = new JButton(Messages.SCRIPTS_DOCS.toString());
        docs.addActionListener(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(DOCS));
            } catch (Exception ex) {
                Popup.notice(DOCS);
            }
        });
        JButton save = new JButton(Messages.SHOPIFY_SAVE.toString());
        save.addActionListener(e -> {
            store();
            mpv5.YabsViewProxy.instance().addMessage(Messages.SHOPIFY_SAVED.toString());
        });
        PrimaryButton run = new PrimaryButton(Messages.SHOPIFY_IMPORT.toString(), "/images/16/arrow-down.png");
        run.addActionListener(e -> importNow());
        actions = Applets.actionBar(new JComponent[]{docs, test}, new JComponent[]{save, run});
        reset();
    }

    private void store() {
        GlobalSettings.setProperty(ShopifyImport.KEY_SHOP, ShopifyImport.normalizeShop(shop.getText()));
        GlobalSettings.setProperty(ShopifyImport.KEY_TOKEN, new String(token.getPassword()).trim());
        GlobalSettings.setProperty(ShopifyImport.KEY_VERSION, version.getText().trim());
        GlobalSettings.setProperty(ShopifyImport.KEY_DOCTYPE, doctype.getSelectedIndex() == 1 ? "invoice" : "order");
        GlobalSettings.save();
        shop.setText(GlobalSettings.getProperty(ShopifyImport.KEY_SHOP));
    }

    private void test() {
        store();
        final ShopifyImport s = ShopifyImport.fromSettings();
        mpv5.YabsViewProxy.instance().setWaiting(true);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return s.test();
            }

            @Override
            protected void done() {
                mpv5.YabsViewProxy.instance().setWaiting(false);
                try {
                    Popup.notice(String.format(Messages.SHOPIFY_TEST_OK.toString(), get()));
                } catch (Exception e) {
                    Log.Debug(this, e);
                    Popup.notice(Messages.SHOPIFY_TEST_FAILED + "\n" + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                }
            }
        }.execute();
    }

    private void importNow() {
        store();
        final ShopifyImport s = ShopifyImport.fromSettings();
        mpv5.YabsViewProxy.instance().setWaiting(true);
        new SwingWorker<ShopifyImport.Result, Void>() {
            @Override
            protected ShopifyImport.Result doInBackground() throws Exception {
                return s.importNew();
            }

            @Override
            protected void done() {
                mpv5.YabsViewProxy.instance().setWaiting(false);
                showLast();
                try {
                    ShopifyImport.Result r = get();
                    List<String> lines = new ArrayList<String>(r.warnings);
                    Popup.notice(lines, String.format(Messages.SHOPIFY_RESULT.toString(), r.orders, r.contacts, r.positions));
                } catch (Exception e) {
                    Log.Debug(this, e);
                    Popup.notice(Messages.SHOPIFY_TEST_FAILED + "\n" + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                }
            }
        }.execute();
    }

    private void showLast() {
        String id = GlobalSettings.getProperty(ShopifyImport.KEY_LAST);
        last.setText(id == null || id.isEmpty() || "0".equals(id) ? Messages.SHOPIFY_NOTHING_YET.toString()
                : String.format(Messages.SHOPIFY_LAST.toString(), id));
    }

    @Override
    public void setValues(PropertyStore values) {
    }

    @Override
    public String getUname() {
        return UNAME;
    }

    @Override
    public final void reset() {
        String s = GlobalSettings.getProperty(ShopifyImport.KEY_SHOP);
        shop.setText(s == null ? "" : s);
        String t = GlobalSettings.getProperty(ShopifyImport.KEY_TOKEN);
        token.setText(t == null ? "" : t);
        String v = GlobalSettings.getProperty(ShopifyImport.KEY_VERSION);
        version.setText(v == null || v.isEmpty() ? ShopifyImport.DEFAULT_VERSION : v);
        doctype.setSelectedIndex("invoice".equals(GlobalSettings.getProperty(ShopifyImport.KEY_DOCTYPE)) ? 1 : 0);
        showLast();
    }

    @Override
    public Component getAndRemoveActionPanel() {
        return actions;
    }
}
