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
package mpv5.utils.export;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.jobs.Waitable;
import mpv5.utils.text.Iban;

/**
 * SEPA direct debit file (SEPA-Lastschrift, pain.008.001.08, CORE, recurring)
 * for unpaid invoices, to upload in online banking. Creditor: the account and
 * the creditor ID of the company info. Debtor: the bank account of the
 * contact; the mandate reference is the customer number, the date of the
 * mandate the date the contact was created.
 */
public class SepaDirectDebit extends Exportable implements Waitable {

    /**
     * Company setting (companyinfo.sepacreditorid): the creditor identifier
     */
    public static final String KEY_CREDITOR_ID = "sepacreditorid";

    /**
     * The creditor (you)
     */
    public static class Creditor {

        final String name, iban, bic, creditorId;

        public Creditor(String name, String iban, String bic, String creditorId) {
            this.name = name;
            this.iban = Iban.normalize(iban);
            this.bic = Iban.normalize(bic);
            this.creditorId = normalizeCreditorId(creditorId);
        }
    }

    /**
     * One collection
     */
    public static class Debit {

        final String endToEndId, name, iban, bic, mandateId, reference;
        final LocalDate mandateDate;
        final BigDecimal amount;

        public Debit(String endToEndId, String name, String iban, String bic, String mandateId,
                LocalDate mandateDate, BigDecimal amount, String reference) {
            this.endToEndId = endToEndId;
            this.name = name;
            this.iban = Iban.normalize(iban);
            this.bic = Iban.normalize(bic);
            this.mandateId = mandateId;
            this.mandateDate = mandateDate;
            this.amount = amount.setScale(2, RoundingMode.HALF_UP);
            this.reference = reference;
        }
    }

    public SepaDirectDebit(HashMap<String, Object> map) {
        super(FileDirectoryHandler.getTempFile("sepa-lastschrift-" + LocalDate.now(), "xml").getAbsolutePath());
        setData(map);
    }

    private final List<Debit> debits = new ArrayList<Debit>();
    private final List<String> done = new ArrayList<String>();
    private final List<String> skipped = new ArrayList<String>();
    private Creditor creditor;

    /**
     * Collects the debits; call before the file is created (and before asking
     * where to save it)
     *
     * @return null if the file can be created, else what is missing
     */
    public String check() {
        debits.clear();
        done.clear();
        skipped.clear();
        creditor = companyCreditor(ZugpferdFile.companySettings());
        if (creditor == null) {
            return Messages.SEPA_DD_NO_CREDITOR.toString();
        }
        for (Object o : getData().values()) {
            Item item = (Item) o;
            String line = item.__getCnumber();
            try {
                Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), item.__getContactsids());
                line += "  " + c.__getCname();
                Debit d = debitOf(item, c);
                if (d != null) {
                    debits.add(d);
                    done.add(line + "  " + d.amount.toPlainString() + " EUR");
                    continue;
                }
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
            skipped.add(line);
        }
        if (debits.isEmpty()) {
            StringBuilder s = new StringBuilder(Messages.SEPA_DD_NOTHING.toString());
            for (String line : skipped) {
                s.append("\n").append(line);
            }
            return s.toString();
        }
        return null;
    }

    @Override
    void run() {
        try {
            mpv5.YabsViewProxy.instance().setWaiting(true);
            String problem = creditor == null ? check() : null;
            if (problem != null) {
                throw new IllegalStateException(problem);
            }
            String xml = toXml(creditor, debits, nextCollectionDate(LocalDate.now()), LocalDateTime.now());
            Files.write(toPath(), xml.getBytes(StandardCharsets.UTF_8));
            List<String> report = new ArrayList<String>(done);
            if (!skipped.isEmpty()) {
                report.add("");
                report.add(Messages.SEPA_DD_SKIPPED.toString());
                report.addAll(skipped);
            }
            Popup.notice(report, Messages.SEPA_DD_CREATED);
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            Log.Debug(ex);
            throw new IllegalStateException(ex.getMessage(), ex);
        } finally {
            mpv5.YabsViewProxy.instance().setWaiting(false);
        }
    }

    @Override
    public Exception waitFor() {
        try {
            run();
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    /**
     * @param company lookup for companyinfo.* keys
     * @return the creditor, null if the IBAN or the creditor ID is missing
     */
    public static Creditor companyCreditor(Function<String, String> company) {
        List<BankAccount> accounts = BankAccount.companyAccounts(company);
        String cid = company.apply(KEY_CREDITOR_ID);
        if (accounts.isEmpty() || !Iban.isValid(accounts.get(0).getIban()) || !isValidCreditorId(cid)) {
            return null;
        }
        String person = (ZugpferdFile.nvl(company.apply("firstname")) + " " + ZugpferdFile.nvl(company.apply("name"))).trim();
        String name = ZugpferdFile.firstNonEmpty(company.apply("business"), person);
        return new Creditor(name, accounts.get(0).getIban(), accounts.get(0).getBic(), cid);
    }

    /**
     * @return the collection of the invoice, null without a valid IBAN of the
     * contact or without an amount
     */
    static Debit debitOf(Item item, Contact c) {
        //older versions mixed up the fields: take whichever holds a valid IBAN / BIC
        String iban = Iban.isValid(c.__getBankaccount()) ? c.__getBankaccount()
                : Iban.isValid(c.__getBankid()) ? c.__getBankid() : null;
        BigDecimal amount = item.getGrossAmount();
        if (iban == null || amount == null || amount.compareTo(new BigDecimal("0.01")) < 0) {
            return null;
        }
        String bic = Iban.isValidBic(c.__getBankid()) ? c.__getBankid()
                : Iban.isValidBic(c.__getBankaccount()) ? c.__getBankaccount() : "";
        String name = ZugpferdFile.firstNonEmpty(c.__getCompany(),
                (ZugpferdFile.nvl(c.__getPrename()) + " " + ZugpferdFile.nvl(c.__getCname())).trim());
        LocalDate signed = c.__getDateadded() == null ? LocalDate.now()
                : c.__getDateadded().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return new Debit(item.__getCnumber(), name, iban, bic, c.__getCNumber(), signed, amount,
                Item.getTypeString(item.__getInttype()) + " " + item.__getCnumber());
    }

    /**
     * @return the next working day (Monday - Friday) after the given day
     */
    public static LocalDate nextCollectionDate(LocalDate today) {
        LocalDate d = today.plusDays(1);
        while (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY) {
            d = d.plusDays(1);
        }
        return d;
    }

    /**
     * The file, pain.008.001.08
     *
     * @param creditor
     * @param debits
     * @param collection the requested collection date
     * @param now the creation time
     * @return the XML document
     */
    public static String toXml(Creditor creditor, List<Debit> debits, LocalDate collection, LocalDateTime now) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Debit d : debits) {
            sum = sum.add(d.amount);
        }
        String msgId = "YABS-" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        StringBuilder x = new StringBuilder();
        x.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        x.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.008.001.08\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
        x.append(" <CstmrDrctDbtInitn>\n");
        x.append("  <GrpHdr>\n");
        x.append("   <MsgId>").append(msgId).append("</MsgId>\n");
        x.append("   <CreDtTm>").append(now.withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("</CreDtTm>\n");
        x.append("   <NbOfTxs>").append(debits.size()).append("</NbOfTxs>\n");
        x.append("   <CtrlSum>").append(sum.toPlainString()).append("</CtrlSum>\n");
        x.append("   <InitgPty><Nm>").append(text(creditor.name, 70)).append("</Nm></InitgPty>\n");
        x.append("  </GrpHdr>\n");
        x.append("  <PmtInf>\n");
        x.append("   <PmtInfId>").append(msgId).append("-1</PmtInfId>\n");
        x.append("   <PmtMtd>DD</PmtMtd>\n");
        x.append("   <BtchBookg>true</BtchBookg>\n");
        x.append("   <NbOfTxs>").append(debits.size()).append("</NbOfTxs>\n");
        x.append("   <CtrlSum>").append(sum.toPlainString()).append("</CtrlSum>\n");
        x.append("   <PmtTpInf><SvcLvl><Cd>SEPA</Cd></SvcLvl><LclInstrm><Cd>CORE</Cd></LclInstrm><SeqTp>RCUR</SeqTp></PmtTpInf>\n");
        x.append("   <ReqdColltnDt>").append(collection).append("</ReqdColltnDt>\n");
        x.append("   <Cdtr><Nm>").append(text(creditor.name, 70)).append("</Nm></Cdtr>\n");
        x.append("   <CdtrAcct><Id><IBAN>").append(creditor.iban).append("</IBAN></Id></CdtrAcct>\n");
        x.append("   <CdtrAgt>").append(agent(creditor.bic)).append("</CdtrAgt>\n");
        x.append("   <ChrgBr>SLEV</ChrgBr>\n");
        x.append("   <CdtrSchmeId><Id><PrvtId><Othr><Id>").append(creditor.creditorId)
                .append("</Id><SchmeNm><Prtry>SEPA</Prtry></SchmeNm></Othr></PrvtId></Id></CdtrSchmeId>\n");
        for (Debit d : debits) {
            x.append("   <DrctDbtTxInf>\n");
            x.append("    <PmtId><EndToEndId>").append(text(d.endToEndId, 35)).append("</EndToEndId></PmtId>\n");
            x.append("    <InstdAmt Ccy=\"EUR\">").append(d.amount.toPlainString()).append("</InstdAmt>\n");
            x.append("    <DrctDbtTx><MndtRltdInf><MndtId>").append(text(d.mandateId, 35))
                    .append("</MndtId><DtOfSgntr>").append(d.mandateDate).append("</DtOfSgntr></MndtRltdInf></DrctDbtTx>\n");
            x.append("    <DbtrAgt>").append(agent(d.bic)).append("</DbtrAgt>\n");
            x.append("    <Dbtr><Nm>").append(text(d.name, 70)).append("</Nm></Dbtr>\n");
            x.append("    <DbtrAcct><Id><IBAN>").append(d.iban).append("</IBAN></Id></DbtrAcct>\n");
            x.append("    <RmtInf><Ustrd>").append(text(d.reference, 140)).append("</Ustrd></RmtInf>\n");
            x.append("   </DrctDbtTxInf>\n");
        }
        x.append("  </PmtInf>\n");
        x.append(" </CstmrDrctDbtInitn>\n");
        x.append("</Document>\n");
        return x.toString();
    }

    private static String agent(String bic) {
        return bic == null || bic.isEmpty()
                ? "<FinInstnId><Othr><Id>NOTPROVIDED</Id></Othr></FinInstnId>"
                : "<FinInstnId><BICFI>" + bic + "</BICFI></FinInstnId>";
    }

    /**
     * Text in the SEPA character set (umlauts written out), XML escaped
     */
    static String text(String s, int max) {
        String v = s == null ? "" : s;
        v = v.replace("ä", "ae").replace("ö", "oe").replace("ü", "ue")
                .replace("Ä", "Ae").replace("Ö", "Oe").replace("Ü", "Ue")
                .replace("ß", "ss").replace("&", "+");
        v = java.text.Normalizer.normalize(v, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        v = v.replaceAll("[^A-Za-z0-9/\\-?:().,'+ ]", " ").replaceAll("\\s+", " ").trim();
        if (v.isEmpty()) {
            v = "NOTPROVIDED";
        }
        return v.length() > max ? v.substring(0, max).trim() : v;
    }

    /**
     * @return the creditor ID without spaces, upper case
     */
    public static String normalizeCreditorId(String id) {
        return id == null ? "" : id.replaceAll("\\s", "").toUpperCase();
    }

    /**
     * Checks a SEPA creditor identifier (e.g. DE98ZZZ09999999999): country,
     * check digits (ISO 7064 mod 97-10 over the national part and the country),
     * business code
     *
     * @param id
     * @return true if valid
     */
    public static boolean isValidCreditorId(String id) {
        String v = normalizeCreditorId(id);
        if (!v.matches("[A-Z]{2}[0-9]{2}[A-Z0-9]{3}[A-Z0-9]{1,28}")) {
            return false;
        }
        String check = v.substring(7) + v.substring(0, 2) + "00";
        StringBuilder digits = new StringBuilder();
        for (char ch : check.toCharArray()) {
            digits.append(Character.isDigit(ch) ? String.valueOf(ch) : String.valueOf(ch - 'A' + 10));
        }
        int expected = 98 - new BigInteger(digits.toString()).mod(BigInteger.valueOf(97)).intValue();
        return expected == Integer.parseInt(v.substring(2, 4));
    }
}
