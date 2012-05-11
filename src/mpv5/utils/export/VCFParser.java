/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.export;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Address;
import mpv5.db.objects.Contact;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Notificator;
import mpv5.utils.files.FileReaderWriter;
import net.sf.vcard4j.java.AddressBook;
import net.sf.vcard4j.java.VCard;
import net.sf.vcard4j.java.type.*;
import net.sf.vcard4j.parser.DomParser;
import net.sf.vcard4j.parser.VCardParseException;
import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;

/**
 *
 * @author anti
 */
public class VCFParser {

    /**
     * Parse a file into {@Link VCard} objects
     * @param f
     * @return
     * @throws VCardParseException
     * @throws IOException
     */

    public static synchronized List<VCard> parse(File f) throws VCardParseException, IOException {
        DomParser parser = new DomParser();
        Document document = new DocumentImpl();
        parser.parse(new FileInputStream(f), document);

        List<VCard> cards = new Vector<VCard>();
        AddressBook addressBook = new AddressBook(document);
        for (Iterator vcards = addressBook.getVCards(); vcards.hasNext();) {
            VCard vcard = (VCard) vcards.next();
            cards.add(vcard);
        }

        return cards;
    }

    /**
     * Create a Contactlist from VCards
     * @param list
     * @return
     */
    public static List<Contact> toContacts(List<VCard> list) {

        List<Contact> contacts = new Vector<Contact>(list.size());
        for (VCard card : list) {

            Contact c = new Contact();
            try {
//    private String prename = "";
//    public String cname = "";
                c.setCname(((N) card.getTypes("N").next()).getFamily());
                c.setPrename(((N) card.getTypes("N").next()).getGiven());
//    private String cnumber = "";
//    private String taxnumber = "";
//    private String title = "";
                c.setTitle(((TITLE) card.getTypes("TITLE").next()).get());
//    private String street = "";
                c.setStreet(((ADR) card.getTypes("ADR").next()).getStreet());
//    private String zip = "";
                c.setZip(((ADR) card.getTypes("ADR").next()).getPcode());
//    private String city = "";
                c.setCity(((ADR) card.getTypes("ADR").next()).getLocality());
//    private String mainphone = "";
//    private String workphone = "";
//    private String fax = "";
//    private String mobilephone = "";
                for (Iterator tels = card.getTypes("TEL"); tels.hasNext();) {
                    TEL tel = (TEL) tels.next();
                    if (((TEL.Parameters) tel.getParameters()).containsTYPE(TEL.Parameters.TYPE_CELL)) {
                        c.setMobilephone(tel.get());
                    }

                    if (((TEL.Parameters) tel.getParameters()).containsTYPE(TEL.Parameters.TYPE_WORK)) {
                        c.setWorkphone(tel.get());
                    }

                    if (((TEL.Parameters) tel.getParameters()).containsTYPE(TEL.Parameters.TYPE_FAX)) {
                        c.setFax(tel.get());
                    }

                    if (((TEL.Parameters) tel.getParameters()).containsTYPE(TEL.Parameters.TYPE_HOME)) {
                        c.setMainphone(tel.get());
                    }
                }

//    private String mailaddress = "";
                c.setMailaddress(((EMAIL) card.getTypes("EMAIL").next()).get());
//    private String website = "";
                c.setWebsite(((URL) card.getTypes("URL").next()).get());
//    private String notes = "";
                c.setNotes(((NOTE) card.getTypes("NOTE").next()).get());
//    private String company = "";
                c.setCompany(((ORG) card.getTypes("ORG").next()).getOrgname());
//    private String department = "";
                c.setDepartment(((ORG) card.getTypes("ORG").next()).getOrgunit());
//    private boolean ismale = true;
//    private boolean isenabled = true;
//    private boolean iscompany = false;
//    private boolean iscustomer = false;
//    private boolean ismanufacturer = false;
//    private boolean issupplier = false;
//    private String country = "";
                c.setCountry(((ADR) card.getTypes("ADR").next()).getCountry());

            } catch (Exception e) {
                Log.Debug(VCFParser.class, e.getLocalizedMessage());
                Notificator.raiseNotification(e.getLocalizedMessage());
            }

            contacts.add(c);
        }

//BEGIN:VCARD
//VERSION:3.0
//N:Gump;Forrest
//FN:Forrest Gump
//ORG:Bubba Gump Shrimp Co.
//TITLE:Shrimp Man
//TEL;TYPE=WORK,VOICE:(111) 555-1212
//TEL;TYPE=HOME,VOICE:(404) 555-1212
//ADR;TYPE=WORK:;;100 Waters Edge;Baytown;LA;30314;United States of America
//LABEL;TYPE=WORK:100 Waters Edge\nBaytown, LA 30314\nUnited States of America
//ADR;TYPE=HOME:;;42 Plantation St.;Baytown;LA;30314;United States of America
//LABEL;TYPE=HOME:42 Plantation St.\nBaytown, LA 30314\nUnited States of America
//EMAIL;TYPE=PREF,INTERNET:forrestgump@example.com
//REV:20080424T195243Z
//END:VCARD

//Name	Description	Semantic
//N	Name	a structured representation of the name of the person, place or thing associated with the vCard object.
//FN	Formatted Name	the formatted name string associated with the vCard object
//PHOTO	Photograph	an image or photograph of the individual associated with the vCard
//BDAY	Birthday	date of birth of the individual associated with the vCard
//ADR	Delivery Address	a structured representation of the physical delivery address for the vCard object
//LABEL	Label Address	addressing label for physical delivery to the person/object associated with the vCard
//TEL	Telephone	the canonical number string for a telephone number for telephony communication with the vCard object
//EMAIL	Email	the address for electronic mail communication with the vCard object
//MAILER	Email Program (Optional)	Type of email program used
//TZ	Time Zone	information related to the standard time zone of the vCard object
//GEO	Global Positioning	The property specifies a latitude and longitude
//TITLE	Title	specifies the job title, functional position or function of the individual associated with the vCard object within an organization (V. P. Research and Development)
//ROLE	Role or occupation	the role, occupation, or business category of the vCard object within an organization (eg. Executive)
//LOGO	Logo	an image or graphic of the logo of the organization that is associated with the individual to which the vCard belongs
//AGENT	Agent	information about another person who will act on behalf of the vCard object. Typically this would be an area administrator, assistant, or secretary for the individual
//ORG	Organization Name or Organizational unit	the name and optionally the unit(s) of the organization associated with the vCard object. This property is based on the X.520 Organization Name attribute and the X.520 Organization Unit attribute
//NOTE	Note	specifies supplemental information or a comment that is associated with the vCard
//REV	Last Revision	combination of the calendar date and time of day of the last update to the vCard object
//SOUND	Sound	By default, if this property is not grouped with other properties it specifies the pronunciation of the Formatted Name property of the vCard object.
//URL	URL	An URL is a representation of an Internet location that can be used to obtain real-time information about the vCard object
//UID	Unique Identifier	specifies a value that represents a persistent, globally unique identifier associated with the object
//VERSION	Version	Version of the vCard Specification
//KEY	Public Key	the public encryption key associated with the vCard object

        return contacts;
    }

    /**
     *
     * @param cs
     * @param saveTo
     */
    public static synchronized void toVCard(ArrayList<DatabaseObject> cs, File saveTo) {

        FileReaderWriter rw = new FileReaderWriter(saveTo);

        for (int i = 0; i < cs.size(); i++) {
            Contact c = (Contact) cs.get(i);
            String text =
                    "BEGIN:VCARD\n"
                    + "VERSION:3.0\n"
                    + "N:" + c.__getCname() + ";" + c.__getPrename()
                    + "\n"
                    + "FN:" + c.__getPrename() + " " + c.__getCname()
                    + "\n"
                    + "ORG:" + c.__getCompany() + ";" + c.__getDepartment()
                    + "\n"
                    + "TITLE:" + c.__getTitle()
                    + "\n"
                    + "TEL;TYPE=WORK:" + c.__getWorkphone()
                    + "\n"
                    + "TEL;TYPE=HOME:" + c.__getMainphone()
                    + "\n"
                    + "TEL;TYPE=FAX:" + c.__getFax()
                    + "\n"
                    + "TEL;TYPE=CELL:" + c.__getMobilephone()
                    + "\n"
                    + "ADR;TYPE=WORK:;;" + c.__getStreet() + ";" + c.__getCity() + ";" + ";" + c.__getZip() + ";" + c.__getCountry()
                    + "\n";

            try {
                List data = DatabaseObject.getReferencedObjects(c, Context.getAddress());
                for (int ix = 0; ix < data.size(); ix++) {
                    Address a = (Address) data.get(i);
                    text += "ADR;TYPE=WORK:;;" + a.__getStreet() + ";" + a.__getCity() + ";" + ";" + a.__getZip() + ";" + a.__getCountry()
                            + "\n";
                }
            } catch (NodataFoundException ex) {
            }

            text += "EMAIL;TYPE=PREF,INTERNET:" + c.__getMailaddress() +
                    "\n"
                    + "URL:" + c.__getWebsite() +
                    "\n"
                    + "NOTE:" + c.__getNotes() + " (" + c.__getTaxnumber() + ")" +
                    "\n"
                    + "END:VCARD";

            rw.write(text);
        }
    }

}
