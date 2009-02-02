/**
 * Translate.java
 *
 * Copyright (C) 2007,  Richard Midwinter
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.google.api.translate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONObject;

/**
 * Makes the Google Translate API available to Java applications.
 * 
 * @author Richard Midwinter
 * @author Emeric Vernat
 * @author Juan B Cabral
 */
public class Translate {

    private static final String ENCODING = "UTF-8";
    private static final String URL_STRING = "http://ajax.googleapis.com/ajax/services/language/translate?v=1.0&langpair=";
    private static final String TEXT_VAR = "&q=";

    /**
     * Translates text from a given language to another given language using Google Translate
     * 
     * @param text The String to translate.
     * @param from The language code to translate from.
     * @param to The language code to translate to.
     * @return The translated String.
     * @throws MalformedURLException
     * @throws IOException
     */
    public static String translate(String text, String from, String to) throws Exception {
        return retrieveTranslation(text, from, to);
    }

    /**
     * Forms an HTTP request and parses the response for a translation.
     * 
     * @param text The String to translate.
     * @param from The language code to translate from.
     * @param to The language code to translate to.
     * @return The translated String.
     * @throws Exception
     */
    private static String retrieveTranslation(String text, String from, String to) throws Exception {
        try {
            StringBuilder url = new StringBuilder();
            url.append(URL_STRING).append(from).append("%7C").append(to);
            url.append(TEXT_VAR).append(URLEncoder.encode(text, ENCODING));

            HttpURLConnection uc = (HttpURLConnection) new URL(url.toString()).openConnection();
            try {
                String result = toString(uc.getInputStream());

                JSONObject json = new JSONObject(result);
                return ((JSONObject) json.get("responseData")).getString("translatedText");
            } finally { // http://java.sun.com/j2se/1.5.0/docs/guide/net/http-keepalive.html
                uc.getInputStream().close();
                if (uc.getErrorStream() != null) {
                    uc.getErrorStream().close();
                }
            }
        } catch (Exception ex) {
            throw new Exception("[google-api-translate-java] Error retrieving translation.", ex);
        }
    }

    /**
     * Reads an InputStream and returns its contents as a String. Also effects rate control.
     * @param inputStream The InputStream to read from.
     * @return The contents of the InputStream as a String.
     * @throws Exception
     */
    private static String toString(InputStream inputStream) throws Exception {
        StringBuilder outputBuilder = new StringBuilder();
        try {
            String string;
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, ENCODING));
                while (null != (string = reader.readLine())) {
                    outputBuilder.append(string).append('\n');
                }
            }
        } catch (Exception ex) {
            throw new Exception("[google-api-translate-java] Error reading translation stream.", ex);
        }
        return outputBuilder.toString();
    }
}