/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.ui.misc;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import mpv5.logging.Log;

/**
 *
 * @author Andreas
 */
public class Serializer {

    public static String serialize(Serializable whatever) {
        ByteArrayOutputStream io = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(io);
        encoder.writeObject(whatever);
        encoder.flush();
        encoder.close();
        try {
            return io.toString("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return "";
        }
    }

    public static Serializable deserialize(String whatever) {
        try {
            ByteArrayInputStream io = new ByteArrayInputStream(whatever.getBytes("UTF-8"));
            XMLDecoder decoder = new XMLDecoder(io);
            Serializable obj = (Serializable) decoder.readObject();
            decoder.close();
            return obj;
        } catch (Exception e) {
            Log.Debug(e);
            return null;
        }
    }
}
