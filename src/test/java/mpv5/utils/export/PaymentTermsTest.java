package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import mpv5.db.objects.SubItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mustangproject.Invoice;
import org.mustangproject.TradeParty;

class PaymentTermsTest {

    @TempDir
    Path tmp;

    @Test
    void companyAccountsMainFirstThenFurther() {
        Map<String, String> c = ZugpferdFileTest.company();
        c.put(BankAccount.KEY_ACCOUNTS, BankAccount.toJson(Arrays.asList(
                new BankAccount("Filialen", "DE89 3704 0044 0532 0130 00", "COBADEFFXXX", "Commerzbank"),
                new BankAccount("doppelt", "DE02120300000000202051", "", ""))));
        List<BankAccount> accounts = BankAccount.companyAccounts(c::get);
        assertEquals(2, accounts.size(), "duplicate of the main account left out");
        assertEquals("DE02120300000000202051", accounts.get(0).getIban(), "company info account first");
        assertEquals("DE89370400440532013000", accounts.get(1).getIban());
        assertEquals("Filialen - DE89 3704 0044 0532 0130 00", accounts.get(1).toString());
        assertTrue(BankAccount.fromJson("kaputt").isEmpty());
    }

    @Test
    void senderUsesTheAccountOfTheContact() {
        Map<String, String> c = ZugpferdFileTest.company();
        BankAccount filialen = new BankAccount("Filialen", "DE89370400440532013000", "COBADEFFXXX", "Commerzbank");
        TradeParty sender = ZugpferdFile.createSender(c::get, filialen);
        assertEquals(1, sender.getBankDetails().size());
        assertEquals("DE89370400440532013000", sender.getBankDetails().get(0).getIBAN());
        assertEquals("COBADEFFXXX", sender.getBankDetails().get(0).getBIC());
        assertEquals("DE02120300000000202051", ZugpferdFile.createSender(c::get).getBankDetails().get(0).getIBAN(),
                "default: account of the company info");
    }

    @Test
    void skonto() {
        PaymentTerms t = new PaymentTerms(30, new BigDecimal("2"), 10, null);
        assertTrue(t.hasSkonto());
        assertEquals("#SKONTO#TAGE=10#PROZENT=2.00#", t.xrechnungSkonto());
        assertEquals(new BigDecimal("7.14"), t.skontoAmount(new BigDecimal("357.00")));
        assertEquals("2", t.formatPercent());
        assertFalse(new PaymentTerms(30, new BigDecimal("2"), 0, null).hasSkonto(), "days missing");
        assertEquals(new BigDecimal("2.5"), PaymentTerms.parsePercent("2,5 %"));
        assertEquals(BigDecimal.ZERO, PaymentTerms.parsePercent("abc"));
    }

    @Test
    void skontoInXRechnungIsValid() throws Exception {
        Invoice invoice = ZugpferdFileTest.build();
        ZugpferdFile.applySkonto(invoice, new PaymentTerms(14, new BigDecimal("2"), 7, null));
        File xml = tmp.resolve("skonto.xml").toFile();
        ZugpferdFile.writeXml(invoice, xml);
        String s = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
        assertTrue(s.contains("#SKONTO#TAGE=7#PROZENT=2.00#"), s);
        EInvoiceValidator.Result r = EInvoiceValidator.validate(xml);
        assertTrue(r.isValid(), r.getErrors().toString());
    }

    @Test
    void paymentTermFromTheCompanyDefault() {
        Map<String, String> c = ZugpferdFileTest.company();
        c.remove("payterm");
        assertEquals(ZugpferdFile.DEFAULT_PAYTERM_DAYS,
                PaymentTerms.forContact(null, 0, ZugpferdFile.PAYTERM_UNSET, c::get).getDays(), "nothing set");
        c.put("payterm", "30");
        assertEquals(30, PaymentTerms.forContact(null, 0, ZugpferdFile.PAYTERM_UNSET, c::get).getDays());
        assertEquals(10, PaymentTerms.forContact(null, 10, ZugpferdFile.PAYTERM_UNSET, c::get).getDays(), "contact");
        assertEquals(0, PaymentTerms.forContact(null, 0, 0, c::get).getDays(), "template: payable immediately");
        c.put("payterm", "0");
        PaymentTerms immediate = PaymentTerms.forContact(null, 0, ZugpferdFile.PAYTERM_UNSET, c::get);
        assertTrue(immediate.isImmediate());
        java.util.Date issue = new java.util.Date();
        assertEquals(issue, immediate.dueDate(issue));
        assertEquals(mpv5.globals.Messages.ZUGFERD_PAYABLE_IMMEDIATELY.toString(), immediate.text(issue));
    }
}
