package mpv5.utils.text;

//~--- JDK imports ------------------------------------------------------------

import java.security.*;

/**
 *
 * @author pertinax
 */
public class MD5HashGenerator {
    private static final char[]     hexChars = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
    static private MD5HashGenerator md5      = null;
    private MessageDigest           md       = null;

    /**
     * Constructor is private so you must use the getInstance method
     */
    private MD5HashGenerator() throws NoSuchAlgorithmException {
        md = MessageDigest.getInstance("MD5");
    }

    /**
     * This returns the singleton instance
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static MD5HashGenerator getInstance() throws NoSuchAlgorithmException {
        if (md5 == null) {
            md5 = new MD5HashGenerator();
        }

        return (md5);
    }

    public String hashData(char[] password) {
        byte[] byteArray = new byte[password.length];

        for (int i = 0; i < password.length; i++) {
            byteArray[i] = (byte) password[i];
        }

        return hashData(byteArray);
    }

    public String hashData(byte[] dataToHash) {
        return hexStringFromBytes((calculateHash(dataToHash)));
    }

    private byte[] calculateHash(byte[] dataToHash) {
        md.update(dataToHash, 0, dataToHash.length);

        return (md.digest());
    }

    public String hexStringFromBytes(byte[] b) {
        String hex = "";
        int    msb;
        int    lsb = 0;
        int    i;

        // MSB maps to idx 0
        for (i = 0; i < b.length; i++) {
            msb = ((int) b[i] & 0x000000FF) / 16;
            lsb = ((int) b[i] & 0x000000FF) % 16;
            hex = hex + hexChars[msb] + hexChars[lsb];
        }

        return (hex);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
