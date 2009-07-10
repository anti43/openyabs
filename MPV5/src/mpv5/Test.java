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
package mpv5;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import mpv5.db.common.Context;
import mpv5.db.common.QueryHandler;
import mpv5.server.MPServer;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.date.DateConverter;
import mpv5.utils.models.*;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PagePanel;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;


/**
 *
 *  anti
 */
public class Test {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        JPanel jPanel1 = new JPanel(new BorderLayout());
        JFrame frame = new JFrame();
        File file = new File("/home/anti/Desktop/ttt.pdf");

        if (file.exists()) {

        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel channel = raf.getChannel();
        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        PDFFile pdffile = new PDFFile(buf);

        // draw the first page to an image
        PDFPage page = pdffile.getPage(0);

        //get the width and height for the doc at the default zoom
        Rectangle rect = new Rectangle(0,0,
                (int)page.getBBox().getWidth(),
                (int)page.getBBox().getHeight());

        //generate the image
        Image img = page.getImage(
                rect.width, rect.height, //width & height
                rect, // clip rect
                null, // null for the ImageObserver
                true, // fill background with white
                true  // block until drawing is done
                );

        //show the image in a frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JLabel(new ImageIcon(img)));
        frame.pack();
        frame.setVisible(true);

//            frame.add(jPanel1);
//            frame.pack();
//            frame.setVisible(true);
//            // show the first page AFTER the frame is displayed!
//            PDFPage page = pdffile.getPage(0);
//            panel.showPage(page);
//            panel.useZoomTool(true);

        }
//        Multivalent v = Multivalent.getInstance();
//        Browser br = v.getBrowser("name", "Basic", false);
//        PDF pdf = (PDF) Behavior.getInstance("AdobePDF", "AdobePDF", null, null, null);
//        pdf.setInput(file);
//        Document doc = new Document("doc", null, null);
//        pdf.parse(doc);
//
//        br.setCurDocument(doc);
//        frame.add(br);
//        frame.pack();
//        frame.setVisible(true);


//        String txt="_$dfgdfg$_";
//
//    Pattern p = Pattern.compile("_\\$(.*?)\\$_");
//    Matcher m = p.matcher(txt);
//    if (m.find())
//    System.out.println(m);



//        JFrame f = new JFrame("test");
//        JTable t = new JTable(new Object[][]{{null}, {null}, {null}, {null}, {null}}, new Object[]{"header"});
//
//        class renderer extends JComboBox implements TableCellRenderer {
//
//            public renderer() {
//                super(new String[]{"1", "2", "3"});
//            }
//
//            @Override
//            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//                if (hasFocus) {
//                   return this;
//                } else {
////                    return this;
//                    return new JLabel(String.valueOf(getSelectedItem()));
//                }
//            }
//        }
//
//       class editor extends DefaultCellEditor {
//        public editor(ComboBoxModel m) {
//            super(new JComboBox(m));
//        }
//    }
//
//        t.setDefaultRenderer(Object.class, new renderer());
//        t.setDefaultEditor(Object.class, new editor(((JComboBox)t.getDefaultRenderer(Object.class)).getModel()));
//        f.add(t, BorderLayout.CENTER);
//        f.pack();
//        f.setVisible(true);

//


//        System.out.println( Context.getItems().prepareSQLString("SELECT COUNT(ids) FROM items"));
//
//       QueryHandler.getConnection().freeQuery(
//               Context.getItems().prepareSQLString("SELECT COUNT(ids) FROM items WHERE accountsids = 1"),
//               MPSecurityManager.VIEW, "Items im Konto Irgendwas gezählt.");
//       QueryHandler.getConnection().freeQuery(
//               Context.getItems().prepareSQLString("SELECT COUNT(ids) FROM items WHERE accountsids = 1"),
//               MPSecurityManager.VIEW, "Items im Konto Irgendwas gezählt.");
//        new MPServer();

//        try {
//        try {
//            new PDFFormTest(new File("/home/anti/Desktop/euerformular.pdf")).fillFields();
////        new SplashScreen(new ImageIcon(Test.class.getResource("/mpv5/resources/images/background.png")));
////        try {
////        try {
////            new XMLReader().newDoc(new File("contacts.xml"), true);
////
////        } catch (JDOMException ex) {
////            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
////        } catch (IOException ex) {
////            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
////        }
////            XMLReader r = new XMLReader();
////            r.newDoc(new File("contacts.xml"), true);
////            ArrayList<DatabaseObject> l = r.getObjects(new Contact());
////
////            for (int i = 0; i < l.size(); i++) {
////
////                DatabaseObject databaseObject = l.get(i);
////                System.out.println(databaseObject.__getCName());
////
////            }
////        } catch (Exception ex) {
////           ex.printStackTrace();
////        }
//        } catch (IOException ex) {
//            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (DocumentException ex) {
//            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        new SplashScreen(new ImageIcon(Test.class.getResource("/mpv5/resources/images/background.png")));
//        try {
//        try {
//            new XMLReader().newDoc(new File("contacts.xml"), true);
//
//        } catch (JDOMException ex) {
//            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//        }
//            XMLReader r = new XMLReader();
//            r.newDoc(new File("contacts.xml"), true);
//            ArrayList<DatabaseObject> l = r.getObjects(new Contact());
//
//            for (int i = 0; i < l.size(); i++) {
//
//                DatabaseObject databaseObject = l.get(i);
//                System.out.println(databaseObject.__getCName());
//
//            }
//        } catch (Exception ex) {
//           ex.printStackTrace();
//        }
//        Object[][] ihreDaten = new Object[][]{
//            {new Date(), "Item 1"},
//            {DateConverter.addYears(new Date(), 1), "Item 2"},
//            {DateConverter.addYears(new Date(), 2), "Item 3"},
//            {DateConverter.addYears(new Date(), 3), "Item 4"}};
//
//        MPComboboxModel model = MPComboBoxModelItem.toModel(ihreDaten);
//        JComboBox combobox = new JComboBox(model);
//
//        combobox.setSelectedIndex(MPComboBoxModelItem.getItemIDfromValue("Item 4", combobox.getModel()));
//
//        Date d = (Date) ((MPComboBoxModelItem)combobox.getSelectedItem()).getIdObject();
//        System.out.println(d);


//
//        DateConverter.getQuarter();Locale.getDefault();
//            new DecimalFormat("'#'#'#'''-000").format(1000l);
//
//
//            File file = new File("D:\\projeler\\hal\\monitor\\hal_monitor\\msg_deneme.xml");
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//            DocumentBuilder db = dbf.newDocumentBuilder();
//            Document doc = db.parse(file);
//            doc.getDocumentElement().normalize();
//            System.out.println("Root element " + doc.getDocumentElement().getNodeName());
//
//            NodeList nodeLst = doc.getElementsByTagName("messages");
//
//
//            for (int s = 0; s < nodeLst.getLength(); s++) {
//                Node fstNode = nodeLst.item(s);
//                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
//
//                    Element fstElmnt = (Element) fstNode;
//                    NodeList msgNameElmntLst = fstElmnt.getElementsByTagName("name");
////                    Element msgNameElmnt = (Element) msgNameElmntLst.item(0);
//                    NodeList msgName = msgNameElmnt.getChildNodes();
//                    System.out.println("Message Name : " + (msgName.item(0)).getNodeValue());
//                    NodeList trMode = fstElmnt.getElementsByTagName("trMode");
//                    Element trModeElmnt = (Element) trMode.item(0);
//                    NodeList tr = trModeElmnt.getChildNodes();
//                    System.out.println("TRMode : " + (tr.item(0)).getNodeValue());
//                    /***GET THE NAME OF FIELD NODE**/
//
//                     NodeList nodeLst_fields = fstNode.getChildNodes();
//
//                    for (int i = 0; i < nodeLst_fields.getLength(); i++) {
//                       Node fstFieldNode = nodeLst_fields.item(i);
//
//                            if (fstFieldNode.getNodeType() == Node.ELEMENT_NODE && fstFieldNode.getNodeName().equals("fields")) {
//                                Element fstFieldElmnt = (Element) fstFieldNode;
//                                NodeList fields = fstFieldElmnt.getElementsByTagName("name");
//                                Element fieldNameElmnt = (Element) fields.item(0);
//                                NodeList field = fieldNameElmnt.getChildNodes();
//                                System.out.println("Field Name : " + (field.item(0)).getNodeValue());
//                                //} //end field if
//                            } //end field for
//
//
//                    } // end if
//                } // end forChuck Norris doesn’t deploy web applications, he roundhouse kicks them into the server.
//            } // end tryA synchronize doesn’t protect against Chuck Norris, if he wants the object, he takes it.
//        } catch (Exception ex) {
//            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//        }

    }
}

