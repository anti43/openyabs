/*
 * 
 *  *  This file is part of MP by anti43 /GPL.
 *  *  
 *  *      MP is free software: you can redistribute it and/or modify
 *  *      it under the terms of the GNU General Public License as published by
 *  *      the Free Software Foundation, either version 3 of the License, or
 *  *      (at your option) any later version.
 *  *  
 *  *      MP is distributed in the hope that it will be useful,
 *  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 * *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *      GNU General Public License for more details.
 *  *  
 *  *      You should have received a copy of the GNU General Public License
 *  *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package mp4.utils.files;

import java.io.File;
import java.io.IOException;
import mp4.globals.Strings;
import mp4.items.Popup;
import mp4.einstellungen.Einstellungen;


/**
 *
 * @author anti43
 */
public class Browser {

    public Browser(File file) {

        try {


            Process proc = Runtime.getRuntime().exec(Einstellungen.instanceOf().getBrowser() + " " + file.getAbsolutePath());
        } catch (IOException ex) {

            new Popup(Strings.NO_BROWSER);
        
        }

    }

    public Browser(String file) {

        new Browser(new File(file));

    }
}