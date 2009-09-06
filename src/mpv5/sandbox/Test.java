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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import mpv5.globals.Messages;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileReaderWriter;
import mpv5.utils.files.Zip;
import mpv5.utils.text.RandomText;

/**
 *
 */
public class Test {
public static void main(String... aArgs) throws NoSuchAlgorithmException, IOException{

      String pwdir = new RandomText(8).getString();
        String user = new RandomText(8).getString();
        String password = new RandomText(8).getString();
        String crypt = "{SHA}" + new sun.misc.BASE64Encoder().encode(java.security.MessageDigest.getInstance("SHA1").digest(password.getBytes()));
        String content1 =
                "AuthUserFile " + pwdir + "/.htpasswd\n" +
                "AuthGroupFile /dev/null\n" +
                "AuthName \"" + user + "\"\n" +
                "AuthType Basic\n" +
                "<Limit GET POST>\n" +
                "require valid-user\n" +
                "</Limit>\n" +
                "<Files *.ini>\n" +
                "Order Deny,Allow\n" +
                "Deny from all\n" +
                "</Files>\n";

        String content2 = user + ":" + crypt;
        String tmp = "/home/anti/Desktop/" + RandomText.getText();
        File f1 = new File(tmp + File.separator + ".htaccess");
        f1.getParentFile().mkdirs();
        f1.createNewFile();
        File f2 = new File(tmp + File.separator + pwdir + File.separator + ".htpasswd");
        f2.getParentFile().mkdirs();
        f2.createNewFile();
        FileReaderWriter htacc = new FileReaderWriter(f1);
        FileReaderWriter htpw = new FileReaderWriter(f2);

        if (htacc.write0(content1) && htpw.write0(content2)) {
            DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY);
            d.setSelectedFile(new File("generated.zip"));
            if (d.saveFile()) {
                Zip.zip(tmp, d.getFile().getPath());
            }}

//    final JDialog dialog = new JDialog();
//                    dialog.setTitle(Messages.NOTICE.getValue());
//                    final JOptionPane optionPane = new JOptionPane(
//                                    Messages.OO_LATER.toString());
//                    optionPane.addPropertyChangeListener(
//                        new PropertyChangeListener() {
//                            public void propertyChange(PropertyChangeEvent e) {
//                                String prop = e.getPropertyName();
//
//                                if (dialog.isVisible()
//                                 && (e.getSource() == optionPane)
//                                 && (JOptionPane.VALUE_PROPERTY.equals(prop))) {
//                                    //If you were going to check something
//                                    //before closing the window, you'd do
//                                    //it here.
//                                    dialog.setVisible(false);
//                                }
//                            }
//                        });
//
//                    dialog.setContentPane(optionPane);
//                    dialog.pack();
////                    dialog.setLocationRelativeTo(this);
//                    dialog.setAlwaysOnTop(true);
//                    dialog.setVisible(true);
//    List<String> insects = Arrays.asList("5", "67", "", "1");
//    log(insects + " - Original Data");
//    sortList(insects);
//    log(insects + " - Sorted Data");
//
//    Map<String,String> capitals = new LinkedHashMap<String, String>();
//    capitals.put("finland", "Helsinki");
//    capitals.put("United States", "Washington");
//    capitals.put("Mongolia", "Ulan Bator");
//    capitals.put("Canada", "Ottawa");
//    log(capitals + " - Original Data");
//    log(sortMapByKey(capitals) + " - Sorted Data");
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
