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

import mp4.logs.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author anti43
 */
public class FileToString {
    private String content;

    public FileToString(File file) {
        BufferedReader br = null;
        String resourcename = null;
        try {
            resourcename = file.getCanonicalPath();
        } catch (IOException ex) {
            Log.Debug(this,ex);
        }
        try {
            InputStream ist = new FileInputStream(file); 
            br = new BufferedReader(new InputStreamReader(ist));
            StringBuffer contentOfFile = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                contentOfFile.append(line);
            }
            content = contentOfFile.toString();
        } catch (IOException ex) {
            Log.Debug(this,"File not found: " + resourcename,true);
        } finally {
            try {
                br.close();
            } catch (Exception ex) {
                   Log.Debug(this,"File not found: " + resourcename,true);
            }
        }
    }

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
            Log.Debug(this,"File not found: " + resourcename,true);
        } finally {
            try {
                br.close();
            } catch (Exception ex) {
                   Log.Debug(this,"File not found: " + resourcename,true);
            }
        }
    }

    public String getContent() {
        Log.Debug(this,content);
        return content;
    }
}
