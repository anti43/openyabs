/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.ui.misc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import mpv5.ui.beans.LabeledTextField;

/**
 *
 * @author Andreas
 */
public class TextFieldCalculator implements PropertyChangeListener {

    private final JLabel result;
    private LabeledTextField[] sums;
    private LabeledTextField[] multiplies;
    private LabeledTextField[] subs;

    public TextFieldCalculator(JLabel result) {
        this.result = result;

    }

    /**
     * 
     * @param sums
     */
    public void sum(LabeledTextField... sums) {
        this.sums = sums;
    }

    /**
     * 
     * @param sums
     */
    public void multiply(LabeledTextField... ms) {
        this.multiplies = ms;
    }

    /**
     *
     * @param sums
     */
    public void substract(LabeledTextField... subs) {
        this.subs = subs;
    }

    public void setTrigger(LabeledTextField trigger) {
        trigger.getTextField().addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("text")) {
            sumUp();
        }
    }

    private void sumUp() {
        Double value = 0d;
        for (int i = 0; i < sums.length; i++) {
            LabeledTextField labeledTextField = sums[i];
            value += labeledTextField.getValue(0d);
        }

        for (int i = 0; i < subs.length; i++) {
            LabeledTextField labeledTextField = subs[i];
            value -= labeledTextField.getValue(0d);
        }

        for (int i = 0; i < multiplies.length; i++) {
            LabeledTextField labeledTextField = multiplies[i];
            value *= labeledTextField.getValue(0d);
        }

        result.setText(String.valueOf(value));
    }
}
