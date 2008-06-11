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
package mp3.classes.utils;

import mp3.classes.utils.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import mp3.classes.objects.ungrouped.MyData;

 
/**
 *
 * @author anti43
 */
public abstract class Formater {

    public final static int DATE = 0;
    public final static int PRICE = 1;
    public final static int INTEGER = 2;
    public final static int YEAR = 3;

    public static void clearText(JPanel jPanel6) {
        JTextField jt = null;
        Object p;
        JEditorPane ja =null;
        JTextArea je =null;
        
       for(int i=0;i<jPanel6.getComponents().length;i++){
       
        
       
           try {
               p=(java.lang.Object) jPanel6.getComponents()[i];
               
               if(p.getClass().isInstance(new JTextField())) {

                    jt = (JTextField) jPanel6.getComponents()[i];
                    jt.setText("");
                }
               
                 
               if(p.getClass().isInstance(new JEditorPane())) {

                    ja = (JEditorPane) jPanel6.getComponents()[i];
                    ja.setText(null);
                }
               
               
                if(p.getClass().isInstance(new JTextArea())) {

                    je = (JTextArea) jPanel6.getComponents()[i];
                    je.setText("");
                }
           } catch (Exception exception) {
           }

       
       
       
       }
    }

    /**
     * 
     * @param jTextField
     * @param length
     */
    public static void cut(JTextField jTextField, int length) {
        try {
            jTextField.setText(jTextField.getText().substring(0, length));

        } catch (Exception exception) {
        }

    }
    
    
    /**
     * 
     * @param von
     * @param bis
     * @param datum
     * @return
     */
    public static boolean getBetween(String von, String bis, String datum){
    
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
    
            try {

                Date vond = df.parse(von);
                Date bisd = df.parse(bis);
                Date dat = df.parse(datum);
  

               if ((dat.after(vond) && dat.before(bisd)) || dat.equals(vond) || dat.equals(bisd)){
                    return true;
                    
                }
                else {
                
                    return false;
                
                }

            } catch (ParseException ex) {
                Log.Debug(ex.getMessage());
            }
    
       return false;
    
    }
    

    /**
     * 
     * @param string
     * @return
     */
    public static Date getDate(String string) {
        DateFormat df;
        df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        Date date;
        try {

            date = df.parse(string);
        } catch (ParseException ex) {
            return new Date();
        }
        return date;
    }

    /**
     * 
     * @param table
     * @param columns
     * @return
     */
    public static String[][] formatTableArray(String[][] table, int[] columns) {



        for (int i = 0; i < table.length; i++) {

            for (int z = 0; z < table[i].length; z++) {


                for (int h = 0; h < columns.length; h++) {

                    if (z == columns[h]) {

                        if (table[i][z].equals("0")) {
                            table[i][z] = "Nein";
                        } else {
                            table[i][z] = "Ja";
                        }
                    }


                }


            }

        }


        return table;

    }

    public static String formatDecimal(Double price) {

        DecimalFormat n = new DecimalFormat("0.00");

        return n.format(price);


    }

    public static Double formatDecimalDouble(Double price) {

        DecimalFormat n = new DecimalFormat("0.00");
        String str = n.format(price);
        
        return Double.valueOf(str.replaceAll(",", "."));



    }

    public static String formatMoney(String price) {

         NumberFormat formatter = NumberFormat.getCurrencyInstance(MyData.instanceOf().getLocale());      
         
         return formatter.format(Double.valueOf(price));

    }
    
    public static String formatMoney(Double price) {

        NumberFormat n = NumberFormat.getCurrencyInstance(MyData.instanceOf().getLocale());

        return n.format(price);

    }

    public static boolean check(String string, int mode) {

        DateFormat df;
        df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        Date date;
        Double number;
        Integer integer;

        switch (mode) {
            case 0:
                try {

                    date = df.parse(string);
                } catch (ParseException ex) {
                    return false;
                }

                return true;

            case 1:
                try {


                    number = Double.valueOf(string);
                } catch (Exception ex) {
                    Log.Debug(ex.getMessage());
                    return false;

                }

                return true;


            case 2:
                try {

                    integer = Integer.valueOf(string);
                } catch (Exception ex) {
                    return false;
                }

                return true;


            case 3:
                try {

                     if(string.length()>4)return false;
                     SimpleDateFormat dfg = new SimpleDateFormat("yyyy");
                    
                     Log.Debug(dfg.parse(string));
                } catch (Exception ex) {
                    return false;
                }

                return true;
            }
        return false;
    }

       
    
    public static void formatUneditableTable(JTable jTable1, String[][] data, String[] header) {



        jTable1.setModel(new javax.swing.table.DefaultTableModel(
                data,
                header) {

            boolean[] canEdits = new boolean[]{
                false, false, false, false, false,
                false, false, false, false, false,
                false, false, false, false, false
            };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdits[columnIndex];
            }
        });

        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stripFirst(jTable1);

    }

    /**
     * 
     * @param list
     * @return 
     */
    public static Integer[][][] listToIntegerArray(ArrayList list) {

        Integer[][][] str = new Integer[list.size()][][];

        ArrayList a, b;

        for (int i = 0; i < list.size(); i++) {

            a = (ArrayList) list.get(i);
            str[i] = new Integer[a.size()][];

            for (int m = 0; m < a.size(); m++) {

                b = (ArrayList) a.get(m);

                str[i][m] = new Integer[b.size()];

                for (int k = 0; k < a.size(); k++) {

                    str[i][m][k] = (Integer) b.get(k);
                }


            }

        }

        Log.Debug(str);

        return str;
    }

    /**
     * 
     * @param list
     * @return 
     */
    public static String[] listToStringArray(ArrayList list) {

        String[] str = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {

            str[i] = (String) list.get(i);
        }
        return str;
    }

    public static String[][] merge(String[][] prods, String[][] bills) {
        if (prods == null) {
            prods = new String[0][0];
        }
        if (bills == null) {
            bills = new String[0][0];
        }

        int z = 0;
        if (prods != null && prods.length > 0) {
            z = prods[0].length;
        } else if (bills != null && bills.length > 0) {
            z = bills[0].length;
        } else {

            z = 0;
        }


        String[][] st = new String[prods.length + bills.length][z];
        int i = 0;

        for (i = 0; i < prods.length; i++) {

            for (int k = 0; k < prods[i].length; k++) {

                st[i][k] = prods[i][k];


            }

        }

        for (int l = 0; l < bills.length; l++) {

            for (int k = 0; k < bills[l].length; k++) {

                st[i + l][k] = bills[l][k];


            }

        }

        return st;

    }

    /**
     * 
     * @param table 
     */
    public static void stripFirst(JTable table) {
        Formater.stripFirstColumn(table);
    }
    /**
     * 
     * @param table
     */
    public static void stripSecondColumn(JTable table) {
        table.getColumn(table.getColumnName(1)).setWidth(0);
        table.getColumn(table.getColumnName(1)).setPreferredWidth(0);
        table.getColumn(table.getColumnName(1)).setMinWidth(0);
        table.getColumn(table.getColumnName(1)).setMaxWidth(0);

        table.doLayout();
    }
    /**
     * 
     * @param table
     */
    public static void stripFirstColumn(JTable table) {
        table.getColumn(table.getColumnName(0)).setWidth(0);
        table.getColumn(table.getColumnName(0)).setPreferredWidth(0);
        table.getColumn(table.getColumnName(0)).setMinWidth(0);
        table.getColumn(table.getColumnName(0)).setMaxWidth(0);

        table.doLayout();
    }

    public static void format(JTable table, int column, int width) {
        table.getColumn(table.getColumnName(column)).setWidth(width);
        table.getColumn(table.getColumnName(column)).setPreferredWidth(width);
        table.getColumn(table.getColumnName(column)).setMinWidth(width);
        table.getColumn(table.getColumnName(column)).setMaxWidth(width);


    }

    public static String[] reverseArray(String[] str) {
        int i = 0, j = str.length - 1;
        while (i < j) {
            String h = str[i];
            str[i] = str[j];
            str[j] = h;
            i++;
            j--;
        }
        return str;

    }

    public static String[][] reverseArray(String[][] str) {
        int i = 0, j = str.length - 1;
        while (i < j) {
            String[] h = str[i];
            str[i] = str[j];
            str[j] = h;
            i++;
            j--;
        }
        return str;

    }
 public static String formatYear(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy");

        return df.format(date);

    }
    public static String formatDate(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

        return df.format(date);

    }
    /**
     * 
     * @param field
     * @return
     */
    public static InputVerifier getDoubleInputVerfier(JTextField field){
        return new DoubleInputVerifier(field);
    
    }
    /**
     * 
     * @param field
     * @return
     */
    public static InputVerifier getDateInputVerfier(JTextField field){
        return new DateInputVerifier(field);
    
    }
}
class FormattedTextFieldVerifier extends InputVerifier {
     public boolean verify(JComponent input) {
         if (input instanceof JFormattedTextField) {
             JFormattedTextField ftf = (JFormattedTextField)input;
             AbstractFormatter formatter = ftf.getFormatter();
             if (formatter != null) {
                 String text = ftf.getText();
                 try {
                      formatter.stringToValue(text);
                      return true;
                  } catch (ParseException pe) {
                      return false;
                  }
              }
          }
          return true;
      }
    @Override
      public boolean shouldYieldFocus(JComponent input) {
          return verify(input);
      }

    
  }

class DateInputVerifier extends javax.swing.InputVerifier {
    public DateInputVerifier(JComponent input) {
        super();
        
        this.shouldYieldFocus(input);
    }

    public boolean verify(javax.swing.JComponent input) {
        javax.swing.JTextField jTF = (javax.swing.JTextField) input;
        String sInput = jTF.getText();
        //DateChecker ist eine Klasse, welche Daten auf Korrektheit prüft
        return Formater.check(sInput, Formater.DATE);
    }

    @Override
    public boolean shouldYieldFocus(javax.swing.JComponent input) {
        if (!verify(input)) {
            //Textfeld Vordergrund rot färben
            input.setBackground(java.awt.Color.RED);
            return false;
        }
        else {
            input.setBackground(java.awt.Color.WHITE);
            return true;
        }
    }
}

class DoubleInputVerifier extends javax.swing.InputVerifier {
    public DoubleInputVerifier(JComponent input) {
        super();
        this.shouldYieldFocus(input);
    }

    public boolean verify(javax.swing.JComponent input) {
        javax.swing.JTextField jTF = (javax.swing.JTextField) input;
        String sInput = jTF.getText().replaceAll(",", ".");
        //DateChecker ist eine Klasse, welche Daten auf Korrektheit prüft
        return Formater.check(sInput, Formater.PRICE);
    }

    @Override
    public boolean shouldYieldFocus(javax.swing.JComponent input) {
        if (!verify(input)) {
            //Textfeld Vordergrund rot färben
            input.setBackground(java.awt.Color.RED);
            ((JTextField) input).setText("0"); 
            return false;
        }
        else {
            input.setBackground(java.awt.Color.WHITE);
            return true;
        }
    }
}
