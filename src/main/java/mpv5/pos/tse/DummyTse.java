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
package mpv5.pos.tse;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import mpv5.globals.GlobalSettings;

/**
 * NOT a TSE: behaves like one (transaction numbers, signature counter,
 * signature over the data) for development, tests and demonstrations. It is
 * not certified and protects nothing; receipts say so. Counters and the key
 * are kept in the global settings.
 */
public final class DummyTse implements Tse {

    private static final String P = "org.openyabs.pos.tse.dummy.";

    @Override
    public String name() {
        return "Dummy";
    }

    @Override
    public boolean isCertified() {
        return false;
    }

    private static synchronized long next(String key) {
        long n = 0;
        try {
            n = Long.parseLong(GlobalSettings.getProperty(P + key, "0"));
        } catch (NumberFormatException e) {
            //start at 1
        }
        n++;
        GlobalSettings.setProperty(P + key, String.valueOf(n));
        GlobalSettings.save();
        return n;
    }

    private static synchronized String setting(String key, int randomBytes) {
        String v = GlobalSettings.getProperty(P + key);
        if (v == null || v.isEmpty() || "null".equals(v)) {
            byte[] b = new byte[randomBytes];
            new SecureRandom().nextBytes(b);
            v = Base64.getEncoder().encodeToString(b);
            GlobalSettings.setProperty(P + key, v);
            GlobalSettings.save();
        }
        return v;
    }

    /**
     * @return the serial of this dummy, e.g. YABS-DUMMY-3F2A...
     */
    public String serial() {
        String s = setting("serial", 8);
        StringBuilder hex = new StringBuilder();
        for (byte b : Base64.getDecoder().decode(s)) {
            hex.append(String.format("%02X", b));
        }
        return "YABS-DUMMY-" + hex;
    }

    @Override
    public Started start(String clientId, String processType, String processData) {
        return new Started(next("transaction"), Instant.now());
    }

    @Override
    public Finished finish(Started started, String clientId, String processType, String processData) throws TseException {
        try {
            Instant end = Instant.now();
            long counter = next("signaturecounter");
            String data = String.join(";", clientId, String.valueOf(started.transactionNumber), String.valueOf(counter),
                    ProcessData.utc(started.start), ProcessData.utc(end), processType, processData);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(Base64.getDecoder().decode(setting("key", 32)), "HmacSHA256"));
            String sig = Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
            return new Finished(started.transactionNumber, started.start, end, counter, sig, serial(),
                    "HMAC-SHA256 (Dummy, keine TSE)", "utcTime", "");
        } catch (Exception e) {
            throw new TseException(e.getMessage(), e);
        }
    }
}
