/*
 *  This file is part of MP.
 *
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.images;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import mpv5.logging.Log;

/**
 *
 */
public class MPIcon extends ImageIcon {

    private static final long serialVersionUID = 1L;
    public static String ICON_DIRECTORY = "/mpv5/resources/images/22/mimetypes/";


    /**
     *
     * @param icon
     */
    public MPIcon(Icon icon) {
        super(iconToImage(icon));
    }

    /**
     * 
     * @param icon
     */
    public MPIcon(ImageIcon icon) {
        super(icon.getImage());
    }

    /**
     * 
     * @param icon
     */
    public MPIcon(Image icon) {
        super(icon);
    }

    /**
     * 
     * @param resource
     */
    public MPIcon(String resource) {
        super(MPIcon.class.getResource(resource));
    }

    private MPIcon() {
        super();
    }

    /**
     *
     * @param icon
     * @return
     */
    public static Image iconToImage(Icon icon) {
        if (icon instanceof ImageIcon) {
            return ((ImageIcon) icon).getImage();
        } else {
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            BufferedImage image = gc.createCompatibleImage(w, h);
            Graphics2D g = image.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            return image;
        }
    }

//    /**
//     * Paints the icon.
//     * The top-left corner of the icon is drawn at
//     * the point (<code>x</code>, <code>y</code>)
//     * in the coordinate space of the graphics context <code>g</code>.
//     * If this icon has no image observer,
//     * this method uses the <code>c</code> component
//     * as the observer.
//     *
//     * @param c the component to be used as the observer
//     *          if this icon has no image observer
//     * @param g the graphics context
//     * @param x the X coordinate of the icon's top-left corner
//     * @param y the Y coordinate of the icon's top-left corner
//     */
//    @Override
//    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
//        Image image = getImage();
//        if (super.getImageObserver() == null) {
//            g.drawImage(image, x, y, c);
//        } else {
//            g.drawImage(image, x, y, super.getImageObserver());
//        }
//    }

    /**
     * Creates an Icon with the specified size, fast and ugly
     * @param maxWidthHeigth
     * @return
     */
    public Icon getIcon(int maxWidthHeigth) {
        return getIcon(maxWidthHeigth, -1);
    }

    /**
     * Creates an Icon with the specified size, nice but slowly
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public Icon getIcon(int maxWidth, int maxHeight) {
        if (!cache.containsKey(maxWidth + "@" + maxHeight)) {
            BufferedImage bi = toBufferedImage(this.getImage());
            bi = getScaledInstance(bi, maxWidth, maxHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, maxHeight > 0);
            ImageIcon imic = new ImageIcon(bi);
            cache.put(maxWidth + "@" + maxHeight, imic);
            return imic;
        } else {
            return cache.get(maxWidth + "@" + maxHeight);
        }
    }
    private HashMap<String, Icon> cache = new HashMap<String, Icon>();

    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public static BufferedImage getScaledInstance(BufferedImage img,
            int targetWidth,
            int targetHeight,
            Object hint,
            boolean higherQuality) {
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            return toBufferedImage(img.getScaledInstance(targetWidth, -1, BufferedImage.SCALE_SMOOTH));
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }

    //
    /**
     * This method returns a buffered image with the contents of an image
     * @param image
     * @return
     */
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        // Determine if the image has transparent pixels; for this method's
        // implementation, see e661 Determining If an Image Has Transparent Pixels
        boolean hasAlpha = false;
        try {
            hasAlpha = hasAlpha(image);
        } catch (Exception e) {
            Log.Debug(MPIcon.class, "Could not determine alpha of image: " + image);
        }

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }

            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                    image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }

        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }

    // 
    /**
     * This method returns true if the specified image has transparent pixels
     * @param image
     * @return
     */
    public static boolean hasAlpha(Image image) {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage) image;
            return bimage.getColorModel().hasAlpha();
        }

        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }

        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return cm.hasAlpha();
    }
}
