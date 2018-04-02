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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import fr.opensagres.xdocreport.document.images.ByteArrayImageProvider;
import fr.opensagres.xdocreport.document.images.IImageProvider;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Item;
import mpv5.db.objects.User;
import mpv5.logging.Log;

/**
 *
 * @author andreas
 */
public class YabsQRCodeGenerator {

    static final String CRLF = "\n";

    private BufferedImage image;
    private int size = 125;
    private String text;

    public YabsQRCodeGenerator(DatabaseObject dob) {
        Item dataOwner = (Item) dob;
        text = "BCD" + YabsQRCodeGenerator.CRLF;
        text += "002" + YabsQRCodeGenerator.CRLF;
        text += "2" + YabsQRCodeGenerator.CRLF;
        text += "SCT" + YabsQRCodeGenerator.CRLF;
        text += User.getCurrentUser().getProperty("companyinfo.banknumber") + YabsQRCodeGenerator.CRLF;
        text += User.getCurrentUser().getProperty("companyinfo.business") + YabsQRCodeGenerator.CRLF;
        text += User.getCurrentUser().getProperty("companyinfo.account") + YabsQRCodeGenerator.CRLF;
        text += "EUR" + dataOwner.getGrossAmount().toString() + YabsQRCodeGenerator.CRLF;
        text += "" + YabsQRCodeGenerator.CRLF;
        text += "" + YabsQRCodeGenerator.CRLF;
        text += Item.getTypeString(dataOwner.__getInttype()) + ": " + dataOwner.__getCname() + YabsQRCodeGenerator.CRLF;
        text += "Bitte prüfen Sie die Angaben nochmals!";
    }

    public void generate() {
        try {
            Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            hintMap.put(EncodeHintType.MARGIN, 1);
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size, hintMap);
            image = new BufferedImage(size + 5, size + 5, BufferedImage.TYPE_INT_RGB);
            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, size + 5, size + 5);
            graphics.setColor(Color.BLACK);
            double thickness = 2;
            Stroke oldStroke = graphics.getStroke();
            graphics.setStroke(new BasicStroke((float) thickness));
            graphics.drawRoundRect(5, 5, size - 10, size - 10, 30, 30);
            graphics.setStroke(oldStroke);

            for (int i = 3; i < size; i++) {
                for (int j = 3; j < size; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            Font font = new Font(null, Font.PLAIN, 10);
            AffineTransform affineTransform = new AffineTransform();
            affineTransform.rotate(Math.toRadians(270), 0, 0);
            Font rotatedFont = font.deriveFont(affineTransform);
            graphics.setFont(rotatedFont);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(size - 12, 22, 10, size - 40);
            graphics.setColor(Color.BLACK);
            graphics.drawString("Zahlen mit Code", size - 2, size - 20);
        } catch (Exception e) {
            Log.Debug(e);
        }
        Log.Debug(this, "You have successfully created QR Code.");
    }

    public IImageProvider getImageProvider() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        InputStream fis = new ByteArrayInputStream(os.toByteArray());
        IImageProvider qrcode = new ByteArrayImageProvider(fis);
        qrcode.setSize((float) image.getWidth(), (float) image.getHeight());
        return qrcode;
    }
}
