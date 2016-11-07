
/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.tables;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import mpv5.globals.Constants;
import mpv5.logging.Log;

class DynamicArithmetic {

    private String toParse;
    private final char[] operators = "+-:/*%".toCharArray();
    private final ArrayList<BigDecimal> vals = new ArrayList<BigDecimal>();
    private char operator = 0;
    public BigDecimal result = BigDecimal.ZERO;
    private final BigDecimal hundert = Constants.BD100;
    private int openTerms = 0;
    private final HashMap<Integer, BigDecimal> values;
    private DynamicArithmetic da;

    public DynamicArithmetic(String toParse, HashMap<Integer, BigDecimal> values) throws ParseFormatException {
      //  Log.Debug(this, toParse);
        this.toParse = toParse;
        this.values = values;
        result = parse();
    }

    private BigDecimal parse() throws ParseFormatException {
        String columnString = "";
        boolean pushValueString = false;

        if (toParse.charAt(0) != '(') {
            //Log.Debug(this, "First Char is not a Term opener --> enclosing it");
            toParse = "(" + toParse + ")";
        }

        for (int i = 0; i < toParse.length(); i++) {
            char ch = toParse.charAt(i);
            switch (ch) {
                case '[':
                    //Log.Debug(this, "Value Opener found --> append Value String on ");
                    pushValueString = true;
                    break;
                case ']':
                    //Log.Debug(this, "Value Closer found --> append Value String off");
                    pushValueString = false;
                    if (!columnString.equals("")) {
                        Integer col=Integer.parseInt(columnString);
                        if (values.containsKey(col)) {
                            vals.add(values.get(col));
                        } else {
                            vals.add(null);
                        }
                    } else {
                        throw new ParseFormatException("Parsing Error at Position [" + i + "]: No Value given:\n"
                                + "Arithmetic Expression: " + toParse);
                    }
                    columnString = "";
                    break;
                case '(':
                    //Log.Debug(this, "Term Opener found --> increment Stack 3");
                    if (openTerms == 0) {
                        vals.clear();
                        openTerms = openTerms + 1;
                    } else {
                        parseForStaples();
                        return result;
                    }
                    break;
                case ')':
                    //Log.Debug(this, "Term Closer found --> decrement Stack 3");
                    openTerms = openTerms - 1;
                    calc();
                    break;
                default:
                    if (Character.isDigit(ch)) {
                        //Log.Debug(this, "Column found --> put it to Stack ??");
                        if (pushValueString) {
                            columnString = columnString + ch;
                        } else {
                            throw new ParseFormatException("Parsing Error at Position [" + i + "]: No Open Value Definition!\n"
                                    + "Arithmetic Expression: " + toParse);
                        }
                    } else {
                        for (int j = 0; j < operators.length; j++) {
                            if (ch == operators[j]) {
                                //Log.Debug(this, "Operator found --> put it to Stack ??");
                                if (operator != ch && operator != 0) {
                                    //Log.Debug(this, "Operator changed-->checking for Arithmetic Rules ...");
                                    if (ch == '*' || ch == ':' || ch == '/' || ch == '%') {
                                        //Log.Debug(this, "Handle privileged ...");
                                        values.put(999999, vals.get(vals.size() - 1));
                                        da = new DynamicArithmetic("([999999]" + toParse.substring(i), values);
                                        values.remove(999999);
                                        vals.set(vals.size() - 1, da.result);
                                        openTerms = 0;
                                        calc();
                                        return result;
                                    } else {
                                        //Log.Debug(this, "normal Switch of 'same level operators' or comming from privileged");
                                        openTerms = openTerms - 1;
                                        calc();
                                        openTerms = openTerms + 1;
                                        vals.clear();
                                        vals.add(result);
                                        operator = ch;
                                    }
                                } else {
                                    operator = ch;
                                }
                                break;
                            }
                        }
                    }
            }
        }
        return result;
    }

    private void calc() throws ParseFormatException {
        BigDecimal tmp = BigDecimal.ZERO;
        if (!vals.isEmpty()) {
            switch (operator) {
                case '+':
                    for (int k = 0; k < vals.size(); k++) {
                        BigDecimal val = vals.get(k) == null ? BigDecimal.ZERO : vals.get(k);
                        if (k == 0) {
                            tmp = val;
                        } else {
                            tmp = tmp.add(val);
                        }
                    }
                    if (openTerms == 0) {
                        result = tmp;
                    }
                    break;
                case '-':
                    for (int k = 0; k < vals.size(); k++) {
                        BigDecimal val = vals.get(k) == null ? BigDecimal.ZERO : vals.get(k);
                        if (k == 0) {
                            tmp = val;
                        } else {
                            tmp = tmp.subtract(val);
                        }
                    }
                    if (openTerms == 0) {
                        result = tmp;
                    }
                    break;
                case ':':
                    for (int k = 0; k < vals.size(); k++) {
                        BigDecimal val = vals.get(k) == null ? BigDecimal.ONE : vals.get(k);
                        if (k == 0) {
                            tmp = val;
                        } else {
                            tmp = tmp.divide(val, 9, BigDecimal.ROUND_HALF_UP);
                        }
                    }
                    if (openTerms == 0) {
                        result = tmp;
                    }
                    break;
                case '/':
                    for (int k = 0; k < vals.size(); k++) {
                        BigDecimal val = vals.get(k) == null ? BigDecimal.ONE : vals.get(k);
                        if (k == 0) {
                            tmp = val;
                        } else {
                            tmp = tmp.divide(val, 9, BigDecimal.ROUND_HALF_UP);
                        }
                    }
                    if (openTerms == 0) {
                        result = tmp;
                    }
                    break;
                case '*':
                    for (int k = 0; k < vals.size(); k++) {
                        BigDecimal val = vals.get(k) == null ? BigDecimal.ONE : vals.get(k);
                        if (k == 0) {
                            tmp = val;
                        } else {
                            tmp = tmp.multiply(val);
                        }
                    }
                    if (openTerms == 0) {
                        result = tmp;
                    }
                    break;
                case '%':
                    for (int k = 0; k < vals.size(); k++) {
                        BigDecimal val = vals.get(k) == null ? BigDecimal.ZERO : vals.get(k);
                        if (k == 0) {
                            tmp = val;
                        } else {
                            BigDecimal a=tmp;
                            tmp = tmp.multiply(val).divide(hundert, 9, BigDecimal.ROUND_HALF_UP);
                            //Log.Debug(this, tmp+" = "+a+".multiply("+val+").divide(hundert, 9, BigDecimal.ROUND_HALF_UP);");
                        }
                    }
                    if (openTerms == 0) {
                        result = tmp;
                    }
                    break;
                default:
                    if (vals.size() == 1) {
                        result = vals.get(0) == null ? BigDecimal.ZERO : vals.get(0);
                    } else {
                        throw new ParseFormatException("Parsing Error: Missing Operator!\n"
                                + "Arithmetic Expression: " + toParse);
                    }
            }
        }
    }

    private void parseForStaples() {
        int i = 900000;
        while (toParse.contains(")")) {
            int high = toParse.indexOf(")") + 1;
            int low = toParse.lastIndexOf("(", high);
            try {
                da = new DynamicArithmetic(toParse.substring(low, high), values);
                values.put(i, da.result);
            } catch (ParseFormatException ex) {
                Log.Debug(ex);
            }
            toParse = toParse.replace(toParse.substring(low, high), "[" + i + "]");
            i++;
        }

        try {
            da = new DynamicArithmetic(toParse, values);
            result = da.result;
        } catch (ParseFormatException ex) {
            Log.Debug(ex);
        }
    }
}
