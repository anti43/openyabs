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
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code39Writer;
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
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Code 39 barcode image generator for document numbers ({@code cnumber}) on ODT
 * templates. Placeholder in ODT templates: an image named {@code Barcode.img}
 * (or {@code Code39.img}).
 */
public final class YabsBarcodeGenerator {

    /**
     * Standard Code 39 character set (43 symbols; start/stop {@code *} is added
     * automatically by ZXing's {@link Code39Writer}).
     */
    private static final String CODE39_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%";
    private static final int MAX_LENGTH = 80;
    /**
     * ISO/IEC 16388 minimum quiet zone on each side: 10 narrow module widths.
     */
    private static final int QUIET_ZONE_MODULES = 10;

    private YabsBarcodeGenerator() {
    }

    /**
     * Normalizes and validates a string for standard Code 39 encoding.
     * Whitespace runs are collapsed, leading/trailing whitespace and optional
     * surrounding {@code *} start/stop delimiters are stripped, and letters are
     * uppercased.
     *
     * @param text raw value (e.g. a document {@code cnumber})
     * @return the normalized Code 39 payload without surrounding {@code *}
     * @throws IllegalArgumentException if the text is empty, longer than 80
     * characters, or contains characters outside the Code 39 alphabet
     */
    public static String normalizeCode39(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Barcode text must not be empty");
        }
        String s = text.replaceAll("[\\r\\n\\t]+", " ").trim().toUpperCase(Locale.ROOT);
        if (s.length() >= 2 && s.startsWith("*") && s.endsWith("*")) {
            s = s.substring(1, s.length() - 1).trim();
        }
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Barcode text must not be empty");
        }
        if (s.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Code 39 supports at most " + MAX_LENGTH + " characters");
        }
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (CODE39_ALPHABET.indexOf(ch) < 0) {
                throw new IllegalArgumentException("Unsupported Code 39 character: '" + ch + "'");
            }
        }
        return s;
    }

    /**
     * Renders a crisp Code 39 barcode with quiet zones and centered
     * human-readable text below the bars.
     *
     * @param text the value to encode
     * @return RGB image ready for PNG encoding
     */
    public static BufferedImage code39Image(String text) {
        String value = normalizeCode39(text);
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, 0);
        BitMatrix matrix;
        try {
            matrix = new Code39Writer().encode(value, BarcodeFormat.CODE_39, 0, 1, hints);
        } catch (WriterException e) {
            throw new IllegalArgumentException("Cannot encode Code 39 value: " + value, e);
        }

        int totalModules = matrix.getWidth() + 2 * QUIET_ZONE_MODULES;
        int moduleWidth = Math.max(2, Math.min(4, 720 / totalModules));
        int quiet = QUIET_ZONE_MODULES * moduleWidth;
        int width = totalModules * moduleWidth;
        int height = Math.max(100, width / 5);
        int topMargin = Math.max(4, height / 24);
        int captionHeight = Math.max(22, height / 4);
        int barHeight = height - topMargin - captionHeight;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.setColor(Color.BLACK);
            for (int x = 0; x < matrix.getWidth(); x++) {
                if (matrix.get(x, 0)) {
                    g.fillRect(quiet + x * moduleWidth, topMargin, moduleWidth, barHeight);
                }
            }

            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int fontSize = Math.max(14, (int) Math.round(captionHeight * 0.78));
            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            int maxTextWidth = width - 2 * quiet;
            int textWidth = fm.stringWidth(value);
            if (textWidth > maxTextWidth && maxTextWidth > 0) {
                fontSize = Math.max(10, (int) Math.floor((double) fontSize * maxTextWidth / textWidth));
                font = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);
                g.setFont(font);
                fm = g.getFontMetrics();
                textWidth = fm.stringWidth(value);
            }
            int textX = (width - textWidth) / 2;
            int textY = topMargin + barHeight + Math.max(2, (captionHeight - fm.getHeight()) / 2) + fm.getAscent();
            int maxBaseline = height - fm.getDescent() - 1;
            if (textY > maxBaseline) {
                textY = maxBaseline;
            }
            g.drawString(value, textX, textY);
        } finally {
            g.dispose();
        }
        return img;
    }

    /**
     * @param text the value to encode
     * @return PNG bytes of the Code 39 barcode
     * @throws IOException if PNG encoding fails
     */
    public static byte[] code39Png(String text) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(code39Image(text), "png", os);
        return os.toByteArray();
    }

    /**
     * Wraps pre-rendered PNG bytes into an XDocReport image provider that
     * preserves the ODT template frame's dimensions.
     *
     * @param png PNG bytes
     * @return image provider for XDocReport context
     * @throws IOException if the PNG stream cannot be read
     */
    public static IImageProvider imageProvider(byte[] png) throws IOException {
        IImageProvider provider = new ByteArrayImageProvider(new ByteArrayInputStream(png));
        //keeps the size of the frame in the template instead of blowing it up to raw pixel dimensions
        provider.setUseImageSize(false);
        return provider;
    }

    /**
     * Generates a Code 39 barcode image provider for ODT templates.
     *
     * @param text the value to encode
     * @return image provider preserving template frame size
     * @throws IOException if PNG encoding fails
     */
    public static IImageProvider code39(String text) throws IOException {
        return imageProvider(code39Png(text));
    }
}
