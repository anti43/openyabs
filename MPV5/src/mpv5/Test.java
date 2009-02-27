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
package mpv5;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.db.common.DatabaseObject;
import mpv5.items.div.Contact;
import mpv5.utils.xml.XMLReader;
import org.jdom.JDOMException;

/**
 *
 * @author anti
 */
public class Test {

    public static void main(String[] args) {
        try {
//        try {
//            new XMLReader().newDoc(new File("contacts.xml"), true);
//
//        } catch (JDOMException ex) {
//            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//        }
            XMLReader r = new XMLReader();
            r.newDoc(new File("contacts.xml"), true); 
            ArrayList<DatabaseObject> l = r.getObjects(new Contact());
           
            for (int i = 0; i < l.size(); i++) {

                DatabaseObject databaseObject = l.get(i);
                System.out.println(databaseObject.__getCName());
      
            }
        } catch (Exception ex) {
           ex.printStackTrace();
        } 
    }
}
