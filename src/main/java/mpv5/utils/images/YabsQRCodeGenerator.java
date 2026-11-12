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
package mpv5.utils.images;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import fr.opensagres.xdocreport.document.images.ByteArrayImageProvider;
import fr.opensagres.xdocreport.document.images.IImageProvider;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import javax.imageio.ImageIO;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Item;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.utils.export.BankAccount;
import mpv5.utils.export.ZugpferdFile;

/**
 * EPC QR code ("GiroCode", EPC069-12 version 002) for SEPA credit transfers:
 * scanned by a banking app, it fills in the transfer (IBAN, recipient, amount,
 * reference). Placeholder in ODT templates: an image named QRCode.img.
 */
public class YabsQRCodeGenerator {

    /**
     * Pixels per module of the code
     */
    private static final int SCALE = 6;
    private final String text;
    private BufferedImage image;

    /**
     * @param dob the invoice
     * @throws IllegalStateException if the company info has no IBAN
     */
    public YabsQRCodeGenerator(DatabaseObject dob) {
        Item item = (Item) dob;
        Function<String, String> company = ZugpferdFile.companySettings();
        DatabaseObject contact = null;
        try {
            contact = DatabaseObject.getObject(Context.getContact(), item.__getContactsids());
        } catch (Exception e) {
            Log.Debug(YabsQRCodeGenerator.class, e.getMessage());
        }
        BankAccount account = contact == null
                ? (BankAccount.companyAccounts(company).isEmpty() ? null : BankAccount.companyAccounts(company).get(0))
                : BankAccount.forContact(contact, company);
        if (account == null || account.getIban().isEmpty()) {
            throw new IllegalStateException("No IBAN in the company info");
        }
        String person = (ZugpferdFile.nvl(company.apply("firstname")) + " " + ZugpferdFile.nvl(company.apply("name"))).trim();
        String name = ZugpferdFile.firstNonEmpty(company.apply("business"), person);
        text = payload(name, account.getIban(), account.getBic(), item.getGrossAmount(),
                Item.getTypeString(item.__getInttype()) + " " + item.__getCname());
    }

    /**
     * The content of the code (EPC069-12, version 002, UTF-8)
     *
     * @param name recipient, max 70 characters
     * @param iban
     * @param bic optional
     * @param amount in EUR; no amount if null or not between 0.01 and 999999999.99
     * @param reference text for the recipient, max 140 characters
     * @return the lines of the code
     */
    public static String payload(String name, String iban, String bic, BigDecimal amount, String reference) {
        String eur = "";
        if (amount != null) {
            BigDecimal a = amount.setScale(2, RoundingMode.HALF_UP);
            if (a.compareTo(new BigDecimal("0.01")) >= 0 && a.compareTo(new BigDecimal("999999999.99")) <= 0) {
                eur = "EUR" + a.toPlainString();
            }
        }
        return String.join("\n",
                "BCD", //service tag
                "002", //version
                "1", //UTF-8
                "SCT", //SEPA credit transfer
                clean(bic, 11),
                clean(name, 70),
                clean(iban, 34).replace(" ", ""),
                eur,
                "", //purpose
                "", //structured reference
                clean(reference, 140));
    }

    private static String clean(String s, int max) {
        String v = s == null ? "" : s.replaceAll("[\\r\\n]+", " ").trim();
        return v.length() > max ? v.substring(0, max) : v;
    }

    /**
     * @return the content of the code
     */
    public String getText() {
        return text;
    }

    /**
     * Draws the code with a quiet zone and the caption "Pay with code" below it
     */
    public void generate() {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 0);
            BitMatrix m = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, hints);
            int quiet = 4 * SCALE;
            int code = m.getWidth() * SCALE;
            int w = code + 2 * quiet;
            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 3 * SCALE);
            int captionHeight = 4 * SCALE;
            image = new BufferedImage(w, w + captionHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.setColor(Color.BLACK);
            for (int x = 0; x < m.getWidth(); x++) {
                for (int y = 0; y < m.getHeight(); y++) {
                    if (m.get(x, y)) {
                        g.fillRect(quiet + x * SCALE, quiet + y * SCALE, SCALE, SCALE);
                    }
                }
            }
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            String caption = Messages.QR_CAPTION.toString();
            g.drawString(caption, (w - fm.stringWidth(caption)) / 2, w + fm.getAscent() - SCALE);
            g.dispose();
        } catch (Exception e) {
            Log.Debug(e);
        }
    }

    /**
     * @return the image as PNG
     * @throws IOException
     */
    public byte[] toPng() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        return os.toByteArray();
    }

    /**
     * A plain QR code without caption (e.g. the TSE data of a receipt)
     *
     * @param text
     * @return the image for a template
     * @throws Exception
     */
    public static IImageProvider plain(String text) throws Exception {
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        hints.put(EncodeHintType.MARGIN, 0);
        BitMatrix m = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, hints);
        int scale = 4;
        int quiet = 4 * scale;
        int w = m.getWidth() * scale + 2 * quiet;
        BufferedImage img = new BufferedImage(w, w, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, w);
        g.setColor(Color.BLACK);
        for (int x = 0; x < m.getWidth(); x++) {
            for (int y = 0; y < m.getHeight(); y++) {
                if (m.get(x, y)) {
                    g.fillRect(quiet + x * scale, quiet + y * scale, scale, scale);
                }
            }
        }
        g.dispose();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(img, "png", os);
        //keeps the size of the frame in the template
        IImageProvider p = new ByteArrayImageProvider(new ByteArrayInputStream(os.toByteArray()));
        p.setUseImageSize(false);
        return p;
    }

    public IImageProvider getImageProvider() throws IOException {
        IImageProvider qrcode = new ByteArrayImageProvider(new ByteArrayInputStream(toPng()));
        qrcode.setSize((float) image.getWidth(), (float) image.getHeight());
        return qrcode;
    }
}
