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

import java.time.Instant;

/**
 * A technical security device (TSE, KassenSichV / BSI TR-03153) as the cash
 * register uses it: a transaction is started before a sale is booked and
 * finished with the data of the sale; the TSE signs it.
 * <p>
 * Implementations: {@link DummyTse} (tests and demo, NOT a TSE); a certified
 * TSE (e.g. Epson via JavaPOS) is still to come, see https://github.com/anti43/openyabs/wiki/Kasse-und-TSE.
 */
public interface Tse {

    /**
     * Process type of a sale
     */
    String KASSENBELEG = "Kassenbeleg-V1";
    /**
     * Process type of other events (drawer opened without a sale etc.)
     */
    String SONSTIGER_VORGANG = "SonstigerVorgang";

    /**
     * A started transaction
     */
    final class Started {

        public final long transactionNumber;
        public final Instant start;

        public Started(long transactionNumber, Instant start) {
            this.transactionNumber = transactionNumber;
            this.start = start;
        }
    }

    /**
     * A finished (signed) transaction
     */
    final class Finished {

        public final long transactionNumber;
        public final Instant start;
        public final Instant end;
        public final long signatureCounter;
        public final String signature;
        public final String serial;
        public final String algorithm;
        public final String timeFormat;
        public final String publicKey;

        public Finished(long transactionNumber, Instant start, Instant end, long signatureCounter, String signature,
                String serial, String algorithm, String timeFormat, String publicKey) {
            this.transactionNumber = transactionNumber;
            this.start = start;
            this.end = end;
            this.signatureCounter = signatureCounter;
            this.signature = signature;
            this.serial = serial;
            this.algorithm = algorithm;
            this.timeFormat = timeFormat;
            this.publicKey = publicKey;
        }
    }

    /**
     * The TSE does not answer or refuses: the sale goes on, marked as "TSE
     * failed" (KassenSichV: failure is documented, receipts show it)
     */
    final class TseException extends Exception {

        private static final long serialVersionUID = 1L;

        public TseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * @return e.g. "Dummy"
     */
    String name();

    /**
     * @return true only for a certified TSE; false: receipts must say that
     * they are not protected
     */
    boolean isCertified();

    Started start(String clientId, String processType, String processData) throws TseException;

    Finished finish(Started started, String clientId, String processType, String processData) throws TseException;
}
