/**
 * Language.java
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

import java.util.Arrays;
import java.util.List;

/**
 * Defines language information for the Google Translate API.
 *
 * @author Richard Midwinter
 */
public final class Language {

    public static final String ARABIC = "ar";
    public static final String BULGARIAN = "bg";
    public static final String CATALAN = "ca";
    public static final String CHINESE = "zh";
    public static final String CHINESE_SIMPLIFIED = "zh-CN";
    public static final String CHINESE_TRADITIONAL = "zh-TW";
    public static final String CROATIAN = "hr";
    public static final String CZECH = "cs";
    public static final String DANISH = "da";
    public static final String DUTCH = "nl";
    public static final String ENGLISH = "en";
    public static final String FILIPINO = "tl";
    public static final String FINNISH = "fi";
    public static final String FRENCH = "fr";
    public static final String GERMAN = "de";
    public static final String GREEK = "el";
    public static final String HEBREW = "iw";
    public static final String HINDI = "hi";
    public static final String INDONESIAN = "id";
    public static final String ITALIAN = "it";
    public static final String JAPANESE = "ja";
    public static final String KOREAN = "ko";
    public static final String LATVIAN = "lv";
    public static final String LITHUANIAN = "lt";
    public static final String NORWEGIAN = "no";
    public static final String POLISH = "pl";
    public static final String PORTUGESE = "pt";
    public static final String ROMANIAN = "ro";
    public static final String RUSSIAN = "ru";
    public static final String SERBIAN = "sr";
    public static final String SLOVAK = "sk";
    public static final String SLOVENIAN = "sl";
    public static final String SPANISH = "es";
    public static final String SWEDISH = "sv";
    public static final String UKRANIAN = "uk";
    public static final String VIETNAMESE = "vi";
    public static final List<String> validLanguages = Arrays.asList(new String[]{
                ARABIC, BULGARIAN, CATALAN, CHINESE, CHINESE_SIMPLIFIED,
                CHINESE_TRADITIONAL, CROATIAN, CZECH, DANISH, DUTCH,
                ENGLISH, FILIPINO, FINNISH, FRENCH, GERMAN, GREEK, HEBREW,
                ITALIAN, JAPANESE, KOREAN, LATVIAN, LITHUANIAN, NORWEGIAN,
                POLISH, PORTUGESE, ROMANIAN, RUSSIAN, SERBIAN, SLOVAK,
                SLOVENIAN, SPANISH, SWEDISH, UKRANIAN, VIETNAMESE
            });

    /**
     * Checks a given language is available to use with Google Translate.
     *
     * @param language The language code to check for.
     * @return true if this language is available to use with Google Translate, false otherwise.
     */
    public static boolean isValidLanguage(String language) {
        return validLanguages.contains(language);
    }
}
