/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.tables;

/**
 *
 * @author Jan Hahnisch
 */
public class ParseFormatException
        extends Exception {

    private static final long serialVersionUID = 1L;

    public ParseFormatException() {
    }

    public ParseFormatException(String s) {
        super(s);
    }
}
