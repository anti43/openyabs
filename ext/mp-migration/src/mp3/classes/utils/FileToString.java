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
package mp3.classes.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anti43
 */
public class FileToString {
    private String content;

    public FileToString(String resourcename) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(resourcename)));
            StringBuffer contentOfFile = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                contentOfFile.append(line);
            }
            content = contentOfFile.toString();
        } catch (IOException ex) {
            Log.Debug("File not found: " + resourcename,true);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                   Log.Debug("File not found: " + resourcename,true);
            }
        }
    }

    public String getContent() {
        Log.Debug(content);
        return content;
    }
}
