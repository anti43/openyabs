/*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */

package mpv5.ui.misc;


import javax.swing.*;
import java.awt.event.*;
import java.math.BigDecimal;
import mpv5.globals.Messages;
import mpv5.utils.numberformat.FormatNumber;

/*
 * JCalculator 0.1b (http://www.sourceforge.net/projects/javcalc)
 */

public class JCalc {

    private String previousNumber = "0";
    private String currentNumber = "0";
    private char lastOperator = 0;
    private boolean equalsHit = false;
    private boolean waiting = false;
    private JTextField textf;
    private NumberListener nl;
    private OperationListener ol;
    private FunctionListener fl;
    private JLabel label;

    public JCalc(JTextField text, JLabel label){
        this.textf = text;
        this.label = label;
        nl = new NumberListener();
        ol = new OperationListener();
        fl = new FunctionListener();
    }


    private void write(String number) {
       textf.setText(number);
    }

    private String read() {
        return textf.getText();
    }

    public NumberListener getNl() {
        return nl;
    }

    public OperationListener getOl() {
        return ol;
    }

    public FunctionListener getFl() {
        return fl;
    }

    public class NumberListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            String userInput = evt.getActionCommand();
            String currentDisplay = read();
            if (!userInput.equals(".")) {
                if (equalsHit) {
                    String output = userInput;
                    equalsHit = false;
                    waiting = false;
                    write(output);
                }//end if
                else if (waiting) {
                    String output = userInput;
                    write(output);
                    waiting = false;
                }//end if
                else if (currentDisplay.equals("0")) {
                    write(userInput);
                } else {
                    write(currentDisplay + userInput);
                }
            }//end if
            else {
                if (equalsHit) {
                    equalsHit = false;
                    waiting = false;
                    write("0.");
                }//end if
                else if (waiting) {
                    waiting = false;
                    write("0.");
                }//end if
                else if (currentDisplay.equals("0")) {
                    write(currentDisplay + ".");
                } else if (currentDisplay.indexOf(".") < 0) {
                    write(currentDisplay + ".");
                }
            }//end else
        }//end actionPerformed
    }//end NumberListener

    public class FunctionListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            String userInput = evt.getActionCommand();
            if (userInput.equals("Clear")) {
                clear();
            }
        }//end actionPerformed

        public void clear() {
            previousNumber = "0";
            currentNumber = "0";
            lastOperator = 0;
            equalsHit = false;
            waiting = false;
            write("0");
        }//end clear
    }//end FunctionListener

    public class OperationListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            String userInput = evt.getActionCommand();
            char character = userInput.charAt(0);
            label.setText(String.valueOf(character));
            switch (character) {
                case '-':
                    subtract();
                    break;
                case '*':
                    multiply();
                    break;
                case '/':
                    divide();
                    break;
                case '+':
                    add();
                    break;
                case '=':
                    equalsSign();
                    break;
                case 'x':
                    square();
                    break;
                case 'X':
                    exponentiate();
                    break;
                case '!':
                    factorial();
                    break;
                case '%':
                    mod();
                    break;
            }
        }

        public void subtract() {
            if (!waiting) {
                equalsHit = false;
                waiting = true;
                previousNumber = read();
                lastOperator = '-';
            } else if (lastOperator == '-') {
                String currentNumber = read();
                double num1 = 0;
                double num2 = 0;
                try {
                    num1 = Double.parseDouble(previousNumber);
                    num2 = Double.parseDouble(currentNumber);
                } catch (NumberFormatException e) {
                    write(Messages.ERROR_OCCURED.getValue());
                    previousNumber = "0";
                    lastOperator = 0;
                    equalsHit = true;
                }
                double numericAnswer = num1 - num2;
                previousNumber = numericAnswer + "";
                previousNumber = format(previousNumber);
                write(previousNumber);
            }
        }

        public void multiply() {
            if (!waiting) {
                equalsHit = false;
                waiting = true;
                previousNumber = read();
                lastOperator = '*';
            } else if (lastOperator == '*') {
                String currentNumber = read();
                double num1 = 0;
                double num2 = 0;
                try {
                    num1 = Double.parseDouble(previousNumber);
                    num2 = Double.parseDouble(currentNumber);
                } catch (NumberFormatException e) {
                    write(Messages.ERROR_OCCURED.getValue());
                    previousNumber = "0";
                    lastOperator = 0;
                    equalsHit = true;
                }
                double numericAnswer = num1 * num2;
                previousNumber = numericAnswer + "";
                previousNumber = format(previousNumber);
                write(previousNumber);
            }
        }


        public void divide() {
            if (!waiting) {
                equalsHit = false;
                waiting = true;
                previousNumber = read();
                lastOperator = '/';
            } else if (lastOperator == '/') {
                String currentNumber = read();
                double num1 = 0;
                double num2 = 0;
                try {
                    num1 = Double.parseDouble(previousNumber);
                    num2 = Double.parseDouble(currentNumber);
                } catch (NumberFormatException e) {
                    write(Messages.ERROR_OCCURED.getValue());
                    previousNumber = "0";
                    lastOperator = 0;
                    equalsHit = true;
                }
                double numericAnswer = num1 / num2;
                previousNumber = numericAnswer + "";
                previousNumber = format(previousNumber);
                write(previousNumber);
            }
        }

        public void add() {
            if (!waiting) {
                equalsHit = false;
                waiting = true;
                previousNumber = read();
                lastOperator = '+';
            } else if (lastOperator == '+') {
                String currentNumber = read();
                double num1 = 0;
                double num2 = 0;
                try {
                    num1 = Double.parseDouble(previousNumber);
                    num2 = Double.parseDouble(currentNumber);
                } catch (NumberFormatException e) {
                    write(Messages.ERROR_OCCURED.getValue());
                    previousNumber = "0";
                    lastOperator = 0;
                    equalsHit = true;
                }
                double numericAnswer = num1 + num2;
                previousNumber = numericAnswer + "";
                previousNumber = format(previousNumber);
                write(previousNumber);
            }
        }

        public void equalsSign() {
            if (!equalsHit) {
                currentNumber = read();
            }
            equalsHit = true;
            double num1 = 0;
            double num2 = 0;
            try {
                num1 = Double.parseDouble(previousNumber);
                num2 = Double.parseDouble(currentNumber);
            } catch (NumberFormatException e) {
                write(Messages.ERROR_OCCURED.getValue());
                previousNumber = "0";
                lastOperator = 0;
                equalsHit = true;
            }
            switch (lastOperator) {
                case '*':
                    previousNumber = (num1 * num2) + "";
                    break;
                case '+':
                    previousNumber = (num1 + num2) + "";
                    break;
                case '-':
                    previousNumber = (num1 - num2) + "";
                    break;
                case '/':
                    previousNumber = (num1 / num2) + "";
                    break;
                case 'X':
                    previousNumber = Math.pow(num1, num2) + "";
                    break;
                case '%':
                    previousNumber = (num1 % num2) + "";
                    break;
            }
            previousNumber = format(previousNumber);
            write(previousNumber);
        }

        public void square() {
            previousNumber = read();
            double number = 0;
            try {
                number = Double.parseDouble(previousNumber);
            } catch (NumberFormatException e) {
                write(Messages.ERROR_OCCURED.getValue());
                previousNumber = "0";
                lastOperator = 0;
                equalsHit = true;
            }
            number *= number;
            previousNumber = number + "";
            previousNumber = format(previousNumber);
            write(previousNumber);
            equalsHit = true;
            lastOperator = 'x';
        }

        public void exponentiate() {
            if (!waiting) {
                equalsHit = false;
                waiting = true;
                previousNumber = read();
                lastOperator = 'X';
            } else if (lastOperator == 'X') {
                String currentNumber = read();
                double num1 = 0;
                double num2 = 0;
                try {
                    num1 = Double.parseDouble(previousNumber);
                    num2 = Double.parseDouble(currentNumber);
                } catch (NumberFormatException e) {
                    write(Messages.ERROR_OCCURED.getValue());
                    previousNumber = "0";
                    lastOperator = 0;
                    equalsHit = true;
                }
                double numericAnswer = Math.pow(num1, num2);
                previousNumber = numericAnswer + "";
                previousNumber = format(previousNumber);
                write(previousNumber);
            }
        }

        public void factorial() {
            previousNumber = read();
            double number = 0;
            try {
                number = Double.parseDouble(previousNumber);
            } catch (NumberFormatException e) {
                write(Messages.ERROR_OCCURED.getValue());
                previousNumber = "0";
                lastOperator = 0;
                equalsHit = true;
            }
            int factorial = (int) number;
            if (factorial == 0) {
                factorial = 1;
                previousNumber = factorial + "";
                write(previousNumber);
                equalsHit = true;
                lastOperator = 'x';
            } else {
                for (int i = (int) number - 1; i > 1; i--) {
                    factorial *= i;
                }
                previousNumber = factorial + "";
                write(previousNumber);
                equalsHit = true;
                lastOperator = 'x';
            }
        }

        public void mod() {
            if (!waiting) {
                equalsHit = false;
                waiting = true;
                previousNumber = read();
                lastOperator = '%';
            } else if (lastOperator == '%') {
                if (!equalsHit) {
                    currentNumber = read();
                }
//                System.out.println(currentNumber);
                double num1 = 0;
                double num2 = 0;
                try {
                    num1 = Double.parseDouble(previousNumber);
                    num2 = Double.parseDouble(currentNumber);
                } catch (NumberFormatException e) {
                    write(Messages.ERROR_OCCURED.getValue());
                    previousNumber = "0";
                    lastOperator = 0;
                    equalsHit = true;
                }
                double numericAnswer = num1 % num2;
                previousNumber = numericAnswer + "";
                previousNumber = format(previousNumber);
                write(previousNumber);
            }
        }

        public String format(String answer) {
            double theAnswer = 0;
            try {
                theAnswer = FormatNumber.round(new BigDecimal(answer)).doubleValue();
            } catch (NumberFormatException e) {
                write(Messages.ERROR_OCCURED.getValue());
                previousNumber = "0";
                lastOperator = 0;
                equalsHit = true;
            }
            if (theAnswer == (int) theAnswer) {
                return (int) theAnswer + "";
            } else {
                return theAnswer + "";
            }
        }
    }
}

