/*
 *  This file is part of MP by anti43 /GPL.
 *
 *  MP is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.xml;

import java.io.File;
import mpv5.ui.frames.MPV5View;
import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author Administrator
 */
public class XMLWriter {
    private Element rootElement = new Element("defName");
    private Document myDocument = new Document();
    
    public void newDoc(String rootElementName){
        // Create the root element
        rootElement = new Element(rootElementName);
        myDocument = new Document(rootElement);
        //add an attribute to the root element
        rootElement.setAttribute(new Attribute("userid", MPV5View.getUser().getID()));
        rootElement.addContent(new Comment("Make your changes only below this line"));
    }

    public void createFile() {

        try {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(myDocument, System.out);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

    }

    public void addElement(String name, String value){
        //add some child elements
        Element elem = new Element(name);
        elem.addContent(value);
        rootElement.addContent(elem);
    }
}
