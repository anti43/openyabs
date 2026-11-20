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
package mpv5.utils.export;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.Templateable;
import mpv5.globals.Messages;
import mpv5.handler.TemplateHandler;
import mpv5.logging.Log;
import mpv5.plugin.api.LockedDocumentProvider;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.date.DateConverter;
import mpv5.utils.jobs.Waiter;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;

/**
 * A duplicate of a locked record whose archived original is missing on disk.
 * <p>
 * The original (the file that was sent and archived) can not be created again:
 * a new file would be a second document with the same number, possibly with
 * other content (template, company data, address), and for invoices a second
 * invoice (§ 14c UStG). The right fix is to restore the original from a
 * backup. If a readable copy is needed, the duplicate is created from the
 * stored data, as a plain PDF (never an e-invoice), with "duplicate - copy,
 * not the original" stamped on every page. It is saved next to the other
 * documents of the record and never replaces the original.
 */
public final class LockedDocumentCopy {

    private LockedDocumentCopy() {
    }

    /**
     * Tells the user that the archived original is missing and offers a
     * duplicate
     *
     * @param record the locked record
     * @param missing the archived original that is missing
     * @param provider the plugin that locked the record, may be null
     */
    public static void offer(DatabaseObject record, File missing, LockedDocumentProvider provider) {
        String head = Messages.LOCKED_DOCUMENT_MISSING + "\n" + missing.getPath();
        if (!(record instanceof Templateable) || !record.isExisting()) {
            Popup.notice(head);
            return;
        }
        String refused = null;
        List<String> warnings = Collections.emptyList();
        if (provider != null) {
            try {
                refused = provider.duplicateRefused(record);
                List<String> w = provider.duplicateWarnings(record);
                if (w != null) {
                    warnings = w;
                }
            } catch (Throwable t) {
                Log.Debug(LockedDocumentCopy.class, t);
                refused = t.getMessage();
            }
        }
        if (refused != null) {
            Popup.notice(head + "\n\n" + Messages.LOCKED_DOCUMENT_DUPLICATE_REFUSED + "\n" + refused);
            return;
        }
        StringBuilder question = new StringBuilder(head).append("\n\n").append(Messages.LOCKED_DOCUMENT_DUPLICATE_ASK);
        if (!warnings.isEmpty()) {
            question.append("\n\n").append(Messages.LOCKED_DOCUMENT_DUPLICATE_WARNINGS);
            for (String w : warnings) {
                question.append("\n- ").append(w);
            }
        }
        if (!Popup.Y_N_dialog(question.toString())) {
            return;
        }
        create(record, missing, provider);
    }

    /**
     * Creates the duplicate from the template of the record, stamps it, opens
     * it and tells the provider
     */
    private static void create(DatabaseObject record, File missing, LockedDocumentProvider provider) {
        final Templateable t = (Templateable) record;
        File dir = mpv5.db.objects.User.getSaveDir(record);
        dir.mkdirs();
        //named as the PDF export names the record (DatabaseObject#toPdf)
        final File target = unique(dir, t.getFormatHandler().toUserString() + "-" + Messages.LOCKED_DOCUMENT_DUPLICATE_FILE);
        Export.createFileWithProgress(() -> TemplateHandler.loadTemplate(t.templateGroupIds(), t.templateType()), record, new Waiter() {
            @Override
            public void set(Object object, Exception exception) throws Exception {
                if (exception != null) {
                    Popup.error(exception);
                    return;
                }
                File created = object instanceof Export ? ((Export) object).getTargetFile()
                        : object instanceof List && !((List<?>) object).isEmpty() && ((List<?>) object).get(0) instanceof Export
                        ? ((Export) ((List<?>) object).get(0)).getTargetFile() : (File) object;
                stamp(created, target, stampText(new Date()));
                if (provider != null) {
                    try {
                        provider.duplicateCreated(record, missing, target);
                    } catch (Throwable e) {
                        Log.Debug(LockedDocumentCopy.class, e);
                    }
                }
                mpv5.YabsViewProxy.instance().addMessage(Messages.LOCKED_DOCUMENT_DUPLICATE_CREATED + " " + target.getPath());
                mpv5.utils.files.FileDirectoryHandler.open(target);
            }
        });
    }

    /**
     * @param day the day the duplicate is created
     * @return the line stamped at the top of every page
     */
    public static String stampText(Date day) {
        return String.format(Messages.LOCKED_DOCUMENT_DUPLICATE_STAMP.toString(), DateConverter.getDefDateString(day));
    }

    /**
     * Writes a copy of the PDF with the duplicate stamp on every page: a large
     * diagonal word and a line at the top; the document title says it too
     *
     * @param pdf the PDF created from the template
     * @param target the stamped duplicate
     * @param line the line at the top of every page
     * @throws IOException
     */
    public static void stamp(File pdf, File target, String line) throws IOException {
        String word = Messages.LOCKED_DOCUMENT_DUPLICATE_WORD.toString();
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            for (PDPage page : doc.getPages()) {
                PDRectangle box = page.getCropBox();
                try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    //the diagonal word, light, over the whole page
                    PDExtendedGraphicsState light = new PDExtendedGraphicsState();
                    light.setNonStrokingAlphaConstant(0.18f);
                    cs.saveGraphicsState();
                    cs.setGraphicsStateParameters(light);
                    cs.setNonStrokingColor(new Color(200, 0, 0));
                    float size = Math.min(box.getWidth(), box.getHeight()) / 6f;
                    float width = bold.getStringWidth(word) / 1000f * size;
                    double angle = Math.atan2(box.getHeight(), box.getWidth());
                    float cx = box.getLowerLeftX() + box.getWidth() / 2f;
                    float cy = box.getLowerLeftY() + box.getHeight() / 2f;
                    Matrix m = Matrix.getRotateInstance(angle, cx, cy);
                    m.translate(-width / 2f, -size / 3f);
                    cs.beginText();
                    cs.setFont(bold, size);
                    cs.setTextMatrix(m);
                    cs.showText(word);
                    cs.endText();
                    cs.restoreGraphicsState();
                    //the line at the top, fully visible
                    float lineSize = 10f;
                    float lineWidth = bold.getStringWidth(line) / 1000f * lineSize;
                    cs.setNonStrokingColor(new Color(200, 0, 0));
                    cs.beginText();
                    cs.setFont(bold, lineSize);
                    cs.newLineAtOffset(box.getLowerLeftX() + Math.max(10f, (box.getWidth() - lineWidth) / 2f),
                            box.getUpperRightY() - 18f);
                    cs.showText(line);
                    cs.endText();
                }
            }
            String title = doc.getDocumentInformation().getTitle();
            doc.getDocumentInformation().setTitle((title == null || title.isEmpty() ? "" : title + " - ") + word);
            doc.getDocumentInformation().setCustomMetadataValue("YaBS-Duplicate", line);
            doc.save(target);
        }
    }

    private static File unique(File dir, String name) {
        File f = new File(dir, name + ".pdf");
        for (int i = 2; f.exists(); i++) {
            f = new File(dir, name + "-" + i + ".pdf");
        }
        return f;
    }
}
