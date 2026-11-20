/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openyabs.heilpraktiker;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-256-GCM encryption at rest for sensitive medical notes (Art. 9 GDPR /
 * § 203 StGB) stored in {@code hp_patient_data.soap_notes}.
 * <p>
 * The symmetric key is stored outside the YaBS database (in the plugin's local
 * data folder with owner-only file permissions on POSIX systems), so a plain
 * database copy does not expose SOAP treatment notes in clear text. Legacy
 * unencrypted values without the {@value #PREFIX} marker are read transparently
 * and encrypted on the next save.
 */
public final class HeilpraktikerCrypto {

    public static final String PREFIX = "ENC:v1:";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_BITS = 256;
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static volatile SecretKey activeKey;

    private HeilpraktikerCrypto() {
    }

    public static void setActiveKey(SecretKey key) {
        activeKey = key;
    }

    public static SecretKey getActiveKey() {
        return activeKey;
    }

    /**
     * Generates a new random 256-bit AES key.
     */
    public static SecretKey generateKey() throws GeneralSecurityException {
        KeyGenerator gen = KeyGenerator.getInstance(ALGORITHM);
        gen.init(KEY_BITS, RANDOM);
        return gen.generateKey();
    }

    /**
     * Loads a 256-bit AES key from {@code keyFile}, or generates and persists a
     * new one (with owner-only {@code 0600} permissions where supported) if the
     * file does not exist yet.
     */
    public static SecretKey loadOrCreateKey(File keyFile) throws IOException, GeneralSecurityException {
        if (keyFile == null) {
            return null;
        }
        if (keyFile.isFile()) {
            String raw = Files.readString(keyFile.toPath(), StandardCharsets.UTF_8).trim();
            byte[] bytes = Base64.getDecoder().decode(raw);
            if (bytes.length != 32) {
                throw new GeneralSecurityException("Invalid AES-256 key length in " + keyFile.getName());
            }
            return new SecretKeySpec(bytes, ALGORITHM);
        }
        File parent = keyFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        SecretKey key = generateKey();
        String encoded = Base64.getEncoder().encodeToString(key.getEncoded());
        Files.writeString(keyFile.toPath(), encoded, StandardCharsets.UTF_8);
        try {
            Set<PosixFilePermission> perms = EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(keyFile.toPath(), perms);
        } catch (UnsupportedOperationException | IOException ignored) {
            // Non-POSIX filesystem (e.g. Windows)
        }
        return key;
    }

    /**
     * @return {@code true} if {@code value} is an encrypted payload produced by
     * this class
     */
    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    /**
     * Encrypts {@code plaintext} with AES-256-GCM and prefixes {@value #PREFIX}.
     * Returns {@code ""} for null/empty input. If {@code key} is null, returns
     * {@code plaintext} unchanged.
     */
    public static String encrypt(String plaintext, SecretKey key) throws GeneralSecurityException {
        if (plaintext == null || plaintext.isEmpty()) {
            return "";
        }
        if (key == null) {
            return plaintext;
        }
        byte[] iv = new byte[IV_BYTES];
        RANDOM.nextBytes(iv);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + ct.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ct, 0, combined, iv.length, ct.length);
        return PREFIX + Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Decrypts a stored value. Plain-text values without {@value #PREFIX} are
     * returned unchanged for transparent migration of legacy rows.
     */
    public static String decrypt(String stored, SecretKey key) throws GeneralSecurityException {
        if (stored == null || stored.isEmpty()) {
            return "";
        }
        if (!isEncrypted(stored)) {
            return stored;
        }
        if (key == null) {
            throw new GeneralSecurityException("Verschl\u00fcsselungsschl\u00fcssel f\u00fcr SOAP-Notizen nicht verf\u00fcgbar");
        }
        byte[] combined = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
        if (combined.length <= IV_BYTES) {
            throw new GeneralSecurityException("Ung\u00fcltiger verschl\u00fcsselter Wert");
        }
        byte[] iv = new byte[IV_BYTES];
        byte[] ct = new byte[combined.length - IV_BYTES];
        System.arraycopy(combined, 0, iv, 0, IV_BYTES);
        System.arraycopy(combined, IV_BYTES, ct, 0, ct.length);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        byte[] pt = cipher.doFinal(ct);
        return new String(pt, StandardCharsets.UTF_8);
    }
}
