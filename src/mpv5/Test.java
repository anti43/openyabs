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

import com.sun.star.uno.Exception;
import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import java.util.Vector;
import javax.swing.table.TableColumn;
import mpv5.db.common.NodataFoundException;
import mpv5.logging.Log;
import mpv5.logging.LogConsole;
import mpv5.utils.export.Export;
import mpv5.utils.export.ODTFile;

import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.application.OfficeApplicationRuntime;
import ag.ion.bion.officelayer.desktop.DesktopException;
import ag.ion.bion.officelayer.desktop.IFrame;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.DocumentException;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentDescriptor;
import ag.ion.bion.officelayer.filter.PDFFilter;
import ag.ion.bion.officelayer.form.IFormComponent;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextField;
import ag.ion.bion.officelayer.text.ITextFieldService;
import ag.ion.bion.officelayer.text.IVariableTextFieldMaster;
import ag.ion.bion.officelayer.text.TextException;
import ag.ion.noa.NOAException;
import com.sun.star.awt.XTextComponent;
import com.sun.star.beans.*;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.document.XDocumentInfoSupplier;
import com.sun.star.io.IOException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import com.sun.star.frame.*;
import com.sun.star.text.XTextDocument;
import com.sun.star.form.XFormComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.DateTime;
import enoa.connection.NoaConnection;
import java.io.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
import mpv5.ui.panels.PreviewPanel;
import mpv5.utils.reflection.ClasspathTools;
import ooo.connector.BootstrapSocketConnector;

public class Test {

    private static String oootype;
    private static String ooohome;
    private static String ooohost;
    private static String oooport;

    public static void main(String[] args) throws NodataFoundException, FileNotFoundException, OfficeApplicationException, NOAException, DocumentException, InterruptedException {

//        System.out.println("ff.hh".substring("ff.hh".lastIndexOf(".")+1));
        LogConsole.setLogStreams(false, true, false);
        Log.setLogLevel(Log.LOGLEVEL_DEBUG);

        try {
            NoaConnection.startOOServer(LocalSettings.getProperty(LocalSettings.OFFICE_HOME),
                    Integer.valueOf(LocalSettings.getProperty(LocalSettings.OFFICE_PORT)));
        } catch (java.lang.Exception numberFormatException) {
            numberFormatException.printStackTrace();
        }

        Thread.sleep(5000);

        Export e = new Export();
        ODTFile f = new ODTFile("/home/anti/aaa.odt");
        e.put("number", "some value#");
        e.setFile(f);
        File g = new File("/home/anti/exp" + Math.random() + ".pdf");
        File g2 = new File("/home/anti/exp0.01511272191122559.pdf");

        e.processData(g);



        Thread.sleep(15000);

        NoaConnection.stopOOOServer();
        System.exit(0);



//        System.exit(0);

//
//        JFrame x = new JFrame();
//       OOOPanel p = new OOOPanel();
//        x.add(p);
//        x.setVisible(true);
//        p.constructOOOPanel(f);

//
//        DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY, new File("export.pdf"));
//        d.setFileFilter(DialogForFile.PDF_FILES);
//
//        if (d.saveFile()) {
//             e.processData(d.getFile());
//        }
//        JPanel jPanel1 = new JPanel(new BorderLayout());
//        JFrame frame = new JFrame();
//
//        final JTable t = new JTable(new DefaultTableModel(new Object[][]{{null},{null},{null},{null}}, new String[]{"1"}));
//        new CellRendererBox(t).setRendererTo(0);
//
//        jPanel1.add(t, BorderLayout.CENTER);
//        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//        frame.addWindowListener(new WindowAdapter() {
//
//            @Override
//            public void windowClosing(WindowEvent e) {
//               TableModel model =  t.getModel();
//                for (int i = 0; i < model.getRowCount(); i++) {
//                    System.out.println(model.getValueAt(i, 0));
//                    System.exit(0);
//                }
//            }
//        });
//        frame.add(jPanel1);
//        frame.pack();
//        frame.setVisible(true);
//new Context(null).prepareSQLString("");


//        DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY, new File("export.pdf"));
//        d.setFileFilter(DialogForFile.PDF_FILES);
//
//        if (d.saveFile()) {
//            try {
//                e.processData(d.getFile());
//            } catch (Exception ex) {
//                Log.Debug(ex);
//            }
//        }
//        JPanel jPanel1 = new JPanel(new BorderLayout());
//        JFrame frame = new JFrame();
//
//        final JTable t = new JTable(new DefaultTableModel(new Object[][]{{null},{null},{null},{null}}, new String[]{"1"}));
//        new CellRendererBox(t).setRendererTo(0);
//
//        jPanel1.add(t, BorderLayout.CENTER);
//        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//        frame.addWindowListener(new WindowAdapter() {
//
//            @Override
//            public void windowClosing(WindowEvent e) {
//               TableModel model =  t.getModel();
//                for (int i = 0; i < model.getRowCount(); i++) {
//                    System.out.println(model.getValueAt(i, 0));
//                    System.exit(0);
//                }
//            }
//        });
//        frame.add(jPanel1);
//        frame.pack();
//        frame.setVisible(true);

//new Context(null).prepareSQLString("");
    }

    static class CellRendererBox extends JLabel implements TableCellRenderer {

        private final JTable table;
        private Vector<JLabel> labels = new Vector<JLabel>();
        private JLabel label = new JLabel();

        /**
         * Create a new CellRenderer which holds a MPComboBox with the given Context's data as model. Will not assign itself to any column.
         * @param c
         * @param table
         */
        public CellRendererBox(JTable table) {
            super();
            this.table = table;
        }

        /**
         * Set this renderer to the given column
         * @param column
         */
        public void setRendererTo(int column) {
            TableColumn col = table.getColumnModel().getColumn(column);
            col.setCellEditor(new JComboBoxEditor(new JComboBox(new Object[]{"test1", "test2"})));
            col.setCellRenderer(this);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (hasFocus && isSelected) {
                if (isSelected) {
                    setForeground(table.getSelectionForeground());
                    super.setBackground(table.getSelectionBackground());
                } else {
                    setForeground(table.getForeground());
                    setBackground(table.getBackground());
                }
                label.setText((value == null) ? "" : value.toString());
                return label;
            } else {
                label.setText((value == null) ? "" : value.toString());
                return label;
            }
        }

        class JComboBoxEditor extends DefaultCellEditor {

            private final JComboBox box;

            public JComboBoxEditor(JComboBox b) {
                super(b);
                this.box = b;
            }
        }
    }
}
//        File file = new File("/home//Desktop/ttt.pdf");
//
//        if (file.exists()) {
//
//        RandomAccessFile raf = new RandomAccessFile(file, "r");
//        FileChannel channel = raf.getChannel();
//        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
//        PDFFile pdffile = new PDFFile(buf);
//
//        // draw the first page to an image
//        PDFPage page = pdffile.getPage(0);
//
//        //get the width and height for the doc at the default zoom
//        Rectangle rect = new Rectangle(0,0,
//                (int)page.getBBox().getWidth(),
//                (int)page.getBBox().getHeight());
//
//        //generate the image
//        Image img = page.getImage(
//                rect.width, rect.height, //width & height
//                rect, // clip rect
//                null, // null for the ImageObserver
//                true, // fill background with white
//                true  // block until drawing is done
//                );
//show the image in a frame
//            frame.add(jPanel1);
//            frame.pack();
//            frame.setVisible(true);
//            // show the first page AFTER the frame is displayed!
//            PDFPage page = pdffile.getPage(0);
//            panel.showPage(page);
//            panel.useZoomTool(true);
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
//            new PDFFormTest(new File("/home//Desktop/euerformular.pdf")).fillFields1();
////        new SplashScreen(new ImageIcon(Test.class.getResource("/mpv5/resources/images/background.png")));
////        try {
////        try {
////            new XMLReader().newDoc(new File("contacts.xml"), true);
////
////        } catch (JDOMException ex) {
////            mpv5.logging.Log.Debug(ex);//Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
////        } catch (IOException ex) {
////            mpv5.logging.Log.Debug(ex);//Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
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
//            mpv5.logging.Log.Debug(ex);//Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (DocumentException ex) {
//            mpv5.logging.Log.Debug(ex);//Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        new SplashScreen(new ImageIcon(Test.class.getResource("/mpv5/resources/images/background.png")));
//        try {
//        try {
//            new XMLReader().newDoc(new File("contacts.xml"), true);
//
//        } catch (JDOMException ex) {
//            mpv5.logging.Log.Debug(ex);//Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            mpv5.logging.Log.Debug(ex);//Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
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
//            mpv5.logging.Log.Debug(ex);//Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
//        }

    


