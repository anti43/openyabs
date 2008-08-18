/*
 *  This file is part of MP by anti43 /GPL.
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
package mp4.utils.bilder;

import java.awt.Image;
import java.awt.MediaTracker;
import javax.swing.JFrame;
import mp3.classes.utils.Log;

/**
 *
 * @author Andreas
 */
public class ImageFormat {

    public static Image ResizeImage(Image image, int width) {
        Log.Debug("Resize image..");
        if (width > 1000) {
            Log.Debug("Image will not be resized > 1000!");
            width = 1000;
        }
        return image.getScaledInstance(width, -1, Image.SCALE_SMOOTH);
    }
    
    public static Image prepareImage(Image image)
        {
 
          MediaTracker mt = new MediaTracker(new JFrame());
          mt.addImage(image, 0);
          try {
          //Warten, bis das Image vollständig geladen ist,
             mt.waitForAll();
           } catch (InterruptedException e) {
           //nothing
           }
          return image;
        }

}
