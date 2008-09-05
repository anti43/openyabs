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
package mp4.utils.ui.inputfields;


import java.awt.Color;
import java.text.ParseException;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import mp4.utils.datum.DateConverter;
import mp4.utils.zahlen.vDouble;


/**
 *
 * @author anti43
 */
public abstract class InputVerifiers {


    /**
     * 
     * @param field
     * @return
     */
    public static InputVerifier getDoubleInputVerfier(JTextField field) {
        return new DoubleInputVerifier(field);

    }

    /**
     * 
     * @param field
     * @return
     */
    public static InputVerifier getDateInputVerfier(JTextField field) {
        return new DateInputVerifier(field);

    }

  
}
class FormattedTextFieldVerifier extends InputVerifier {

    public boolean verify(JComponent input) {
        if (input instanceof JFormattedTextField) {
            JFormattedTextField ftf = (JFormattedTextField) input;
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
        return (DateConverter.getDate(sInput) != null);
    }

    @Override
    public boolean shouldYieldFocus(javax.swing.JComponent input) {
        if (!verify(input)) {
            //Textfeld Vordergrund rot färben
            input.setBorder(new EtchedBorder(Color.WHITE, Color.BLUE));
            return false;
        } else {
            input.setBorder(new EtchedBorder(Color.WHITE, Color.GRAY));
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
        vDouble val = new vDouble(sInput);
        return val.isVerified;
    }

    @Override
    public boolean shouldYieldFocus(javax.swing.JComponent input) {
        if (!verify(input)) {
            //Textfeld Vordergrund rot färben
            input.setBorder(new EtchedBorder(Color.WHITE, Color.BLUE));
            ((JTextField) input).setText("0");
            return false;
        } else {
            input.setBorder(new EtchedBorder(Color.WHITE, Color.GRAY));
            return true;
        }
    }
}
