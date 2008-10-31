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
package mp4.interfaces;

import java.awt.Image;
import java.io.File;

/**
 *
 * @author Andreas
 */
public interface Template {

 
     /*
     * 
     * Returns the path to this printable objects (temp)file
     */
    public String getPath();
   
    /*
     * If this is a PDF Template,
     * return the fields, else null
     */
    public String[][] getFields();
    
    /*
     * If this is a PDF Template and has an image,
     * return the image, else null
     */
    public Image getImage();
    /*
     * 
     * Returns the path to this printable objects target file
     */
    public File getTargetFile();
    
    /*
     * If this is a PDF Template,
     * return the path to template, else null
     */
    public String getTemplate();
}
