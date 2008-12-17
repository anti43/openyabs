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
import java.io.FileWriter;
import java.util.List;
import mpv5.globals.Messages;
import mpv5.ui.dialogs.DialogForFile;
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


    public void addNode(String name, String attribute) {
        Element elem = new Element(name);
        elem.setAttribute("id", attribute);
        rootElement.addContent(elem);
    }
    
    public void newDoc(String rootElementName){
        // Create the root element
        rootElement = new Element(rootElementName);
        myDocument = new Document(rootElement);
        //add an attribute to the root element
        rootElement.setAttribute(new Attribute("userid", MPV5View.getUser().getID()));
        rootElement.addContent(new Comment("Make your changes only below this line"));
    }

    public void createFile(String filename) {
        DialogForFile dialog = new DialogForFile(DialogForFile.FILES_ONLY);
        dialog.setSelectedFile(new File(filename + ".xml"));
        if(dialog.saveFile()){
        try {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(myDocument, new FileWriter(dialog.getFile()));
            MPV5View.addMessage(Messages.FILE_SAVED + dialog.getFile().getPath());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        }
    }

    public void addElement(String nodename, String attributevalue, String name, String value){
        //add some child elements
        Element elem = new Element(name);
        elem.addContent(value);
        @SuppressWarnings("unchecked")
        List<Element> list = (List<Element>)rootElement.getContent();
        for (int i = 0; i < list.size(); i++) {
           if(list.get(i) instanceof Element) if(list.get(i).getAttribute("id")!=null
                   && list.get(i).getAttribute("id").getValue().equals(attributevalue))list.get(i).addContent(elem);
        }

    }

    public void addNode(String name){
        rootElement.addContent(new Element(name));
    }
}
