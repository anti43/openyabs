package org.openyabs.datev;

import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * Saves the dialogs of the DATEV exports (and the journal with its export
 * menu) as PNG, for the documentation and to look at the content
 */
final class DatevExportScreenshots {

    private DatevExportScreenshots() {
    }

    static void save(File dir, String name, DatevExportDialog.Content c, int tab) throws Exception {
        dir.mkdirs();
        SwingUtilities.invokeAndWait(() -> {
            JFrame f = new JFrame(c.title);
            JComponent p = DatevExportDialog.panel(c);
            JTabbedPane tabs = find(p, JTabbedPane.class);
            if (tabs != null && tab < tabs.getTabCount()) {
                tabs.setSelectedIndex(tab);
            }
            f.setContentPane(p);
            f.pack();
            f.setVisible(true);
            shoot(f.getRootPane(), new File(dir, name));
            f.dispose();
        });
    }

    /**
     * The settings page of the DATEV plugin, the whole form (not only what a
     * window shows)
     */
    static void settings(File dir, String name) throws Exception {
        dir.mkdirs();
        SwingUtilities.invokeAndWait(() -> {
            JComponent page = DatevSettingsPage.create(null);
            javax.swing.JScrollPane sp = find(page, javax.swing.JScrollPane.class);
            JComponent form = sp != null ? (JComponent) sp.getViewport().getView() : page;
            JFrame f = new JFrame("DATEV-Export");
            f.setContentPane(page);
            f.setSize(form.getPreferredSize().width + 40, form.getPreferredSize().height + 120);
            f.setVisible(true);
            f.validate();
            form.setSize(form.getPreferredSize());
            layout(form);
            shoot(form, new File(dir, name));
            f.dispose();
        });
    }

    static void journal(File dir, String name, Date from, Date to) throws Exception {
        dir.mkdirs();
        SwingUtilities.invokeAndWait(() -> {
            try {
                JFrame f = new JFrame("Journal");
                mpv5.ui.panels.JournalPanel j = new mpv5.ui.panels.JournalPanel();
                f.setContentPane(j);
                f.setSize(1400, 900);
                f.setVisible(true);
                java.lang.reflect.Field t = mpv5.ui.panels.JournalPanel.class.getDeclaredField("timeframeChooser1");
                t.setAccessible(true);
                ((mpv5.ui.beans.TimeframeChooser) t.get(j)).setTime(new mpv5.utils.date.vTimeframe(from, to));
                j.refresh();
                f.validate();
                shoot(f.getRootPane(), new File(dir, name.replace(".png", "-panel.png")));
                java.lang.reflect.Method m = mpv5.ui.panels.JournalPanel.class.getDeclaredMethod("exportMenu");
                m.setAccessible(true);
                javax.swing.JPopupMenu menu = (javax.swing.JPopupMenu) m.invoke(j);
                menu.pack();
                menu.setSize(menu.getPreferredSize());
                layout(menu);
                shoot(menu, new File(dir, name));
                f.dispose();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private static void layout(Container c) {
        c.doLayout();
        for (Component k : c.getComponents()) {
            if (k instanceof Container) {
                layout((Container) k);
            }
        }
    }

    private static void shoot(JComponent c, File target) {
        BufferedImage img = new BufferedImage(Math.max(1, c.getWidth()), Math.max(1, c.getHeight()), BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = img.createGraphics();
        c.printAll(g);
        g.dispose();
        try {
            ImageIO.write(img, "png", target);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T find(Component c, Class<T> type) {
        if (type.isInstance(c)) {
            return (T) c;
        }
        if (c instanceof Container) {
            for (Component k : ((Container) c).getComponents()) {
                T t = find(k, type);
                if (t != null) {
                    return t;
                }
            }
        }
        return null;
    }
}
