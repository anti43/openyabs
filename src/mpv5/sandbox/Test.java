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

package mpv5.sandbox;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public class Test {
public static void main(String... aArgs){
    List<String> insects = Arrays.asList("5", "67", "", "1");
    log(insects + " - Original Data");
    sortList(insects);
    log(insects + " - Sorted Data");

    Map<String,String> capitals = new LinkedHashMap<String, String>();
    capitals.put("finland", "Helsinki");
    capitals.put("United States", "Washington");
    capitals.put("Mongolia", "Ulan Bator");
    capitals.put("Canada", "Ottawa");
    log(capitals + " - Original Data");
    log(sortMapByKey(capitals) + " - Sorted Data");
  }

  private static void sortList(List<String> aItems){
    Collections.sort(aItems, String.CASE_INSENSITIVE_ORDER);
  }

  private static void log(Object aObject){
    System.out.println(String.valueOf(aObject));
  }

  private static Map<String, String> sortMapByKey(Map<String, String> aItems){
    TreeMap<String, String> result =
      new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER)
    ;
    result.putAll(aItems);
    return result;
  }

}
