/*
 * * This file is an extension part of MP by anti43 /GPL.
 *
 * MP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MP. If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.models;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * This class reads and writes a HTML document with StAX. Tags with an id
 * attribute will be filled with values from a hash map.
 * @author hnauheim
 */
public class HtmlFormRenderer {

  private XMLEventFactory fact = XMLEventFactory.newInstance();
  private String htmlform;
  private Map<String, String> map;
  private double sum;

  /**
   * StaxParser for reading, filling and writing of a given HTML file.
   * @param htmlform Name of the HTML file to use
   * @param model The model including the values to write to the HTML file
   * @return An OutputStream with the resulting HTML file.
   */
  public OutputStream parseHtml(String htmlform, Map<String, String> map) {
    this.htmlform = htmlform;
    this.map = map;

    ByteArrayOutputStream boas = new ByteArrayOutputStream();
    try {
      InputStream inputStream = getClass().getResourceAsStream(htmlform);
//      FileInputStream inputStream = new FileInputStream(htmlform);
      XMLInputFactory inf = XMLInputFactory.newInstance();
      XMLEventReader reader = inf.createXMLEventReader(inputStream);
      XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(
          boas);
//      XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(
//          new FileWriter("out.html"));

      Characters ch = null;
      boolean sta = false;
      while (reader.hasNext()) {
        XMLEvent event = (XMLEvent) reader.next();
        if (event.isStartElement()) {
          StartElement start = (StartElement) event;
          writer.add(event);
          Iterator it = start.getAttributes();
          while (it.hasNext()) {
            Attribute attr = (Attribute) it.next();
            if (attr.getName().getLocalPart().equals("id")) {
              sta = true;
              ch = (getValues(attr.getValue()));
            }
          }
        } else {
          if (event.getEventType() == event.END_ELEMENT) {
            if ((ch != null) && (sta == true)) {
              String s = ch.getData();
              if ((s != null) && (s.contains("\n"))) {
                StringTokenizer tok = new StringTokenizer(s, "\n");
                while (tok.hasMoreTokens()) {
                  writer.add(fact.createCharacters(tok.nextToken()));
                  writer.add(fact.createStartElement("", "", "br"));
                  writer.add(fact.createEndElement("", "", "br"));
                }
              } else {
                writer.add(ch);
              }
            }
          }
          sta = false;
          writer.add(event);
        }
      }
      writer.flush();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return boas;
  }

  /**
   * Transforms a value from the result map to a character event.<br />
   * @param att The name of the id-attribute in the HTML document
   * @return A character event
   */
  private Characters getValues(String att) {
    String value = null;
    if (att.contains("+")) {
      String[] arr = att.substring(4, att.length()).split("\\+");
      sum = 0;
      for (int i = 0; i < arr.length; i++) {
        String s = att.substring(0, 4) + arr[i];
        if (map.containsKey(s)) {
          String ir = map.get(s);
          sum = sum + Double.parseDouble(ir);
        }
        map.put(att, sum + "");
        value = sum + "";
      }
    } else {
      if (map.containsKey(att)) {
        value = map.get(att);
      }
    }
    return fact.createCharacters(value);
  }
}
