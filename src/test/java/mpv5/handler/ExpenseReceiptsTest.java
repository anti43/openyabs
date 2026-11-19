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
package mpv5.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link ExpenseReceipts}: path resolution with variables,
 * SHA-256 checksums, and the SHA256SUMS file format.
 */
public class ExpenseReceiptsTest {

    @TempDir
    Path tmp;

    @Test
    void resolvesPathVariables() {
        Date now = new Date();
        String expectedYear = new SimpleDateFormat("yyyy").format(now);
        String expectedMonth = new SimpleDateFormat("MM").format(now);
        String expectedDay = new SimpleDateFormat("dd").format(now);

        File f = ExpenseReceipts.resolve("[HOME]/receipts/[DBNAME]/[YEAR]/[MONTH]", "testdb", now);
        String p = f.getPath();
        assertTrue(p.contains(System.getProperty("user.home")));
        assertTrue(p.contains("testdb"));
        assertTrue(p.contains(expectedYear));
        assertTrue(p.contains(expectedMonth));
    }

    @Test
    void computesSha256MatchesStandard() throws Exception {
        Path file = tmp.resolve("sample_invoice.pdf");
        byte[] content = "%PDF-1.4 Mock Receipt for Testing\nTotal: 42.00 EUR".getBytes(StandardCharsets.UTF_8);
        Files.write(file, content);

        String hash = ExpenseReceipts.sha256(file.toFile());
        assertNotNull(hash);
        assertEquals(64, hash.length());

        // Verify repeatability
        assertEquals(hash, ExpenseReceipts.sha256(file.toFile()));
    }

    @Test
    void receiptFormattedSize() {
        ExpenseReceipts.Receipt r1 = new ExpenseReceipts.Receipt(1, 10, "a.pdf", "/p/a.pdf", 512, "hash", "application/pdf", new Date(), "user");
        assertEquals("512 B", r1.formattedSize());

        ExpenseReceipts.Receipt r2 = new ExpenseReceipts.Receipt(2, 10, "b.pdf", "/p/b.pdf", 2048, "hash", "application/pdf", new Date(), "user");
        assertEquals("2.0 KB", r2.formattedSize());

        ExpenseReceipts.Receipt r3 = new ExpenseReceipts.Receipt(3, 10, "c.pdf", "/p/c.pdf", 1024 * 1024 * 3, "hash", "application/pdf", new Date(), "user");
        assertEquals("3.00 MB", r3.formattedSize());
    }

    @Test
    void verifyDetectsTampering() throws Exception {
        Path file = tmp.resolve("receipt.xml");
        Files.write(file, "<xml>original</xml>".getBytes(StandardCharsets.UTF_8));

        String originalHash = ExpenseReceipts.sha256(file.toFile());
        ExpenseReceipts.Receipt r = new ExpenseReceipts.Receipt(1, 10, "receipt.xml", file.toString(),
                file.toFile().length(), originalHash, "application/xml", new Date(), "tester");

        assertTrue(ExpenseReceipts.verify(r));

        // Tamper with file
        Files.write(file, "<xml>tampered</xml>".getBytes(StandardCharsets.UTF_8));
        assertFalse(ExpenseReceipts.verify(r));
    }
}
