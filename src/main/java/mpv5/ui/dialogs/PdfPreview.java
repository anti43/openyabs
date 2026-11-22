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
    /**
     * the record shown, may be null; an invoice is issued by print, save as
     * and open (the preview is a draft)
     */
    private final mpv5.db.common.DatabaseObject owner;
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
        show(file, null);
    }

    /**
     * Shows the preview of a record; for an invoice, print, save as and open
     * in the PDF viewer issue it first (e-invoice as set for the customer,
     * finalized by a running GoBD plugin), after a warning
     *
     * @param file a PDF
     * @param owner the record, may be null
     */
    public static void show(File file, mpv5.db.common.DatabaseObject owner) {
        show(file, owner, null);
    }

    /**
     * As {@link #show(File, DatabaseObject)}; the appended documents (e.g. the
     * order confirmation) follow the invoice in the issued file too
     *
     * @param file
     * @param owner
     * @param appended may be null
     */
    public static void show(File file, mpv5.db.common.DatabaseObject owner, mpv5.db.objects.Template[] appended) {
        show(file, owner, appended, null);
    }

    /**
     * As {@link #show(File, DatabaseObject, Template[])}, with a stamp over
     * the pages (e.g. "STORNIERT" on a cancelled original): only on the
     * screen, the file stays as it is (print, save and open use the file)
     *
     * @param file
     * @param owner
     * @param appended may be null
     * @param stamp may be null
     */
    public static void show(File file, mpv5.db.common.DatabaseObject owner, mpv5.db.objects.Template[] appended, String stamp) {
        //one preview per record (or file): a second request (double click, two buttons, a
        //preview finished late) replaces the open window instead of opening another one
        for (java.awt.Window w : java.awt.Window.getWindows()) {
            if (w instanceof PdfPreview && w.isDisplayable() && ((PdfPreview) w).shows(file, owner)) {
                w.dispose();
            }
        }
        PdfPreview p = new PdfPreview(file, owner);
        p.appended = appended;
        p.stamp = stamp;
        p.setVisible(true);
    }

    /**
     * @return true if this window shows the record (same context and id),
     * or the file if there is no record
     */
    private boolean shows(File f, mpv5.db.common.DatabaseObject o) {
        Integer id = o == null ? null : o.__getIDS();
        if (id != null && id > 0 && owner != null) {
            return id.equals(owner.__getIDS()) && String.valueOf(o.getDbIdentity()).equals(String.valueOf(owner.getDbIdentity()));
        }
        return o == null && owner == null && f != null && f.equals(file);
    }

    /**
     * documents whose pages follow the invoice in the issued file, may be
     * null
     */
    private mpv5.db.objects.Template[] appended;
    /**
     * shown over the pages (not in the file), may be null
     */
    private String stamp;

    private PdfPreview(File file, mpv5.db.common.DatabaseObject owner) {
        super(file.getName() + " – " + Messages.ACTION_PREVIEW);
        this.file = file;
        this.owner = owner;
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
        print.addActionListener(e -> withDocument(null, f -> {
            try {
                mpv5.utils.print.PrintJob2.print(f, null);
            } catch (Exception ex) {
                Log.Debug(this, ex);
                Popup.error(ex);
            }
        }));
        JButton save = new JButton(Messages.PDF_PREVIEW_SAVE.toString());
        save.addActionListener(e -> saveAs());
        PrimaryButton external = new PrimaryButton(Messages.PDF_PREVIEW_OPEN.toString(), null);
        external.addActionListener(e -> withDocument(null, f -> mpv5.utils.files.FileDirectoryHandler.open(f)));

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
                    if (stamp != null && !stamp.trim().isEmpty()) {
                        for (BufferedImage img : rendered) {
                            stamp(img, stamp);
                        }
                    }
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

    /**
     * Paints the text diagonally in red over the page (the image shown)
     */
    private static void stamp(BufferedImage img, String text) {
        java.awt.Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = img.getWidth();
            int h = img.getHeight();
            java.awt.Font font = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, Math.max(24, w / 9));
            g.setFont(font);
            java.awt.FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(text);
            g.translate(w / 2, h / 2);
            g.rotate(-Math.atan2(h, w));
            g.setColor(new Color(200, 30, 30, 90));
            int pad = fm.getHeight() / 4;
            g.setStroke(new java.awt.BasicStroke(Math.max(3, w / 300f)));
            g.drawRect(-tw / 2 - pad, -fm.getAscent() - pad / 2, tw + 2 * pad, fm.getAscent() + fm.getDescent() + pad);
            g.drawString(text, -tw / 2, 0);
        } finally {
            g.dispose();
        }
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
        final mpv5.db.objects.Item invoice = issuedInvoice();
        if (invoice != null && lockedFile(invoice) == null && !confirmIssue()) {
            return;
        }
        JFileChooser c = new JFileChooser(invoice != null ? mpv5.db.objects.User.getSaveDir(invoice) : mpv5.db.objects.User.getSaveDir(null));
        c.setSelectedFile(invoice != null ? mpv5.utils.export.IssuedDocument.defaultFile(invoice) : new File(c.getCurrentDirectory(), file.getName()));
        if (c.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File target = c.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".pdf")) {
            target = new File(target.getPath() + ".pdf");
        }
        final File chosen = target;
        if (invoice != null) {
            //the issued document (e-invoice), not the draft
            withDocument(chosen, f -> mpv5.YabsViewProxy.instance().addMessage(f.getPath()), false);
            return;
        }
        try {
            Files.copy(file.toPath(), chosen.toPath(), StandardCopyOption.REPLACE_EXISTING);
            mpv5.YabsViewProxy.instance().addMessage(chosen.getPath());
        } catch (Exception ex) {
            Log.Debug(this, ex);
            Popup.error(ex);
        }
    }

    /**
     * The property (per user): no warning before an invoice is issued
     */
    private static final String NO_ISSUE_WARNING = "noissuewarning"; //NOI18N

    /**
     * @return the invoice of the preview if print, save and open issue it,
     * else null
     */
    private mpv5.db.objects.Item issuedInvoice() {
        if (owner instanceof mpv5.db.objects.Item && mpv5.utils.export.IssuedDocument.applies((mpv5.db.objects.Item) owner)) {
            return (mpv5.db.objects.Item) owner;
        }
        return null;
    }

    /**
     * @return the archived original if a plugin locked the invoice, else null
     */
    private static File lockedFile(mpv5.db.objects.Item invoice) {
        try {
            mpv5.plugin.ExtensionManager em = mpv5.plugin.ExtensionManager.instanceOrNull();
            mpv5.plugin.ExtensionManager.LockedDocument d = em == null ? null : em.lockedDocument(invoice);
            return d == null || d.file == null || !d.file.isFile() ? null : d.file;
        } catch (Throwable t) {
            Log.Debug(PdfPreview.class, t);
            return null;
        }
    }

    /**
     * Warns that the invoice can not be changed after the action, with "do
     * not warn again"
     *
     * @return true to go on
     */
    private boolean confirmIssue() {
        //only a plugin that finalizes (GoBD) makes the invoice unchangeable
        try {
            mpv5.plugin.ExtensionManager em = mpv5.plugin.ExtensionManager.instanceOrNull();
            if (em == null || !em.locksDocuments()) {
                return true;
            }
        } catch (Throwable t) {
            Log.Debug(this, t);
            return true;
        }
        mpv5.db.objects.User user = mpv5.db.objects.User.getCurrentUser();
        try {
            if (user.getProperties().getProperty(PdfPreview.class.getSimpleName(), NO_ISSUE_WARNING)) {
                return true;
            }
        } catch (Exception e) {
            Log.Debug(this, e);
        }
        javax.swing.JCheckBox dontWarn = new javax.swing.JCheckBox(Messages.PDF_PREVIEW_ISSUE_DONT_WARN.toString());
        //short, wrapped to a fixed width: the buttons fit the dialog
        JLabel text = new JLabel("<html><div style='width:" + ModernTheme.px(300) + "px'>"
                + Messages.PDF_PREVIEW_ISSUE_WARNING.toString().replace("&", "&amp;").replace("<", "&lt;") + "</div></html>");
        JPanel message = new JPanel(new BorderLayout(0, ModernTheme.px(10)));
        message.setOpaque(false);
        message.add(text, BorderLayout.CENTER);
        message.add(dontWarn, BorderLayout.SOUTH);
        int answer = javax.swing.JOptionPane.showConfirmDialog(this, message, Messages.PDF_PREVIEW_ISSUE_TITLE.toString(),
                javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE);
        if (answer != javax.swing.JOptionPane.OK_OPTION) {
            return false;
        }
        if (dontWarn.isSelected()) {
            try {
                user.getProperties().changeProperty(PdfPreview.class.getSimpleName(), NO_ISSUE_WARNING, true);
            } catch (Exception e) {
                Log.Debug(this, e);
            }
        }
        return true;
    }

    /**
     * Runs the action with the document to print / open: for an invoice the
     * issued document (after the warning), else the previewed file
     *
     * @param target where to write the issued document, null: its usual
     * place
     * @param action
     */
    private void withDocument(File target, java.util.function.Consumer<File> action) {
        withDocument(target, action, true);
    }

    private void withDocument(File target, final java.util.function.Consumer<File> action, boolean warn) {
        final mpv5.db.objects.Item invoice = issuedInvoice();
        if (invoice == null) {
            action.accept(file);
            return;
        }
        File locked = lockedFile(invoice);
        if (locked != null) {
            //already issued and finalized: the archived original
            if (target != null) {
                try {
                    Files.copy(locked.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    action.accept(target);
                } catch (Exception ex) {
                    Log.Debug(this, ex);
                    Popup.error(ex);
                }
            } else {
                action.accept(locked);
            }
            return;
        }
        if (warn && !confirmIssue()) {
            return;
        }
        final File pdf = target != null ? target : mpv5.utils.export.IssuedDocument.defaultFile(invoice);
        mpv5.utils.export.ExportProgress.run(Messages.PDF_PREVIEW_ISSUE_TITLE + " " + invoice.__getCnumber(),
                () -> mpv5.utils.export.IssuedDocument.issue(invoice, pdf, appended),
                (issued, failure) -> {
                    if (failure != null) {
                        Log.Debug(this, failure);
                        //e.g. the e-invoice can not be created: nothing is printed, saved or opened
                        Popup.warn(this, failure.getLocalizedMessage());
                    } else if (issued != null) {
                        //the issued document may be locked now: the archived original if so
                        File l = lockedFile(invoice);
                        action.accept(l != null && target == null ? l : issued);
                    }
                });
    }
}
