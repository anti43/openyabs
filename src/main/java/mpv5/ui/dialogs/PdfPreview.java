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
package mpv5.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Shows a PDF inside YaBS (rendered with PDFBox): all pages one below the
 * other, zoom, fit to width; print, save as, open in the system viewer.
 */
public final class PdfPreview extends JFrame {

    private static final long serialVersionUID = 1L;
    private final File file;
    private final JPanel pages = new JPanel();
    private final JScrollPane scroll;
    private final JLabel zoomLabel = new JLabel();
    private final List<BufferedImage> rendered = new ArrayList<BufferedImage>();
    private float zoom = 1f;
    private boolean fitWidth = true;
    private static final float DPI = 150f;

    /**
     * Shows the file; if it can not be rendered it is opened in the system
     * viewer
     *
     * @param file a PDF
     */
    public static void show(File file) {
        new PdfPreview(file).setVisible(true);
    }

    private PdfPreview(File file) {
        super(file.getName() + " – " + Messages.ACTION_PREVIEW);
        this.file = file;
        try {
            setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(mpv5.Main.class.getResource(mpv5.globals.Constants.ICON)));
        } catch (Exception e) {
            Log.Debug(this, e);
        }
        pages.setLayout(new BoxLayout(pages, BoxLayout.PAGE_AXIS));
        pages.setBackground(new Color(0x80, 0x84, 0x8a));
        pages.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(12), ModernTheme.px(12), ModernTheme.px(12), ModernTheme.px(12)));
        scroll = new JScrollPane(pages);
        scroll.getVerticalScrollBar().setUnitIncrement(ModernTheme.px(24));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (fitWidth) {
                    layoutPages();
                }
            }
        });

        JButton smaller = new JButton("−");
        smaller.addActionListener(e -> zoom(zoom / 1.2f));
        JButton larger = new JButton("+");
        larger.addActionListener(e -> zoom(zoom * 1.2f));
        JButton fit = new JButton(Messages.PDF_PREVIEW_FIT.toString());
        fit.addActionListener(e -> {
            fitWidth = true;
            layoutPages();
        });
        JButton print = new JButton(Messages.PDF_PREVIEW_PRINT.toString());
        print.addActionListener(e -> {
            try {
                mpv5.utils.print.PrintJob2.print(file, null);
            } catch (Exception ex) {
                Log.Debug(this, ex);
                Popup.error(ex);
            }
        });
        JButton save = new JButton(Messages.PDF_PREVIEW_SAVE.toString());
        save.addActionListener(e -> saveAs());
        PrimaryButton external = new PrimaryButton(Messages.PDF_PREVIEW_OPEN.toString(), null);
        external.addActionListener(e -> mpv5.utils.files.FileDirectoryHandler.open(file));

        JPanel zoomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(4), 0));
        zoomBar.setOpaque(false);
        zoomBar.add(smaller);
        zoomBar.add(zoomLabel);
        zoomBar.add(larger);
        zoomBar.add(Box.createHorizontalStrut(ModernTheme.px(6)));
        zoomBar.add(fit);
        JPanel bar = Applets.actionBar(new JComponent[]{zoomBar}, new JComponent[]{print, save, external});

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(ModernTheme.background());
        content.add(scroll, BorderLayout.CENTER);
        content.add(bar, BorderLayout.SOUTH);
        setContentPane(content);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(ModernTheme.px(820), ModernTheme.px(900)));
        setLocationRelativeTo(mpv5.ui.dialogs.Popup.identifier);
        pages.add(new JLabel(Messages.PDF_PREVIEW_LOADING.toString()));
        render();
    }

    private void render() {
        new SwingWorker<List<BufferedImage>, Void>() {
            @Override
            protected List<BufferedImage> doInBackground() throws Exception {
                List<BufferedImage> l = new ArrayList<BufferedImage>();
                try (PDDocument doc = Loader.loadPDF(file)) {
                    PDFRenderer r = new PDFRenderer(doc);
                    for (int i = 0; i < doc.getNumberOfPages(); i++) {
                        l.add(r.renderImageWithDPI(i, DPI));
                    }
                }
                return l;
            }

            @Override
            protected void done() {
                try {
                    rendered.addAll(get());
                    layoutPages();
                } catch (Exception e) {
                    //not readable by PDFBox: the system viewer
                    Log.Debug(PdfPreview.this, e);
                    dispose();
                    mpv5.utils.files.FileDirectoryHandler.open(file);
                }
            }
        }.execute();
    }

    private void zoom(float z) {
        fitWidth = false;
        zoom = Math.max(0.25f, Math.min(4f, z));
        layoutPages();
    }

    private void layoutPages() {
        if (rendered.isEmpty()) {
            return;
        }
        if (fitWidth) {
            int avail = scroll.getViewport().getWidth() - ModernTheme.px(24) - scroll.getVerticalScrollBar().getWidth();
            if (avail > 50) {
                zoom = (float) avail / rendered.get(0).getWidth();
            }
        }
        pages.removeAll();
        for (BufferedImage img : rendered) {
            int w = Math.max(1, Math.round(img.getWidth() * zoom));
            int h = Math.max(1, Math.round(img.getHeight() * zoom));
            JLabel page = new JLabel(new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
            page.setAlignmentX(CENTER_ALIGNMENT);
            page.setBorder(BorderFactory.createLineBorder(new Color(0x60, 0x64, 0x6a)));
            pages.add(page);
            pages.add(Box.createVerticalStrut(ModernTheme.px(12)));
        }
        zoomLabel.setText(Math.round(zoom * DPI / 72f * 100) + " %");
        pages.revalidate();
        pages.repaint();
    }

    private void saveAs() {
        JFileChooser c = new JFileChooser(mpv5.db.objects.User.getSaveDir(null));
        c.setSelectedFile(new File(c.getCurrentDirectory(), file.getName()));
        if (c.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.copy(file.toPath(), c.getSelectedFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
                mpv5.YabsViewProxy.instance().addMessage(c.getSelectedFile().getPath());
            } catch (Exception ex) {
                Log.Debug(this, ex);
                Popup.error(ex);
            }
        }
    }
}
