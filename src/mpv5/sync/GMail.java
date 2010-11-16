package mpv5.sync;

import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.BillingInformation;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.contacts.GroupMembershipInfo;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.ExtendedProperty;
import com.google.gdata.data.extensions.Im;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.data.extensions.PostalAddress;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import java.io.IOException;
import java.io.ObjectOutputStream.PutField;
import java.net.URL;
import java.util.HashMap;
import mpv5.db.objects.Contact;
import mpv5.globals.Constants;
import mpv5.globals.GlobalSettings;

/**
 *
 * @author andreas
 */
public class GMail {

    /**
     * @param user
     * @param password
     * @return 
     * @throws AuthenticationException
     * @throws ServiceException
     * @throws IOException
     */
    public static HashMap<String, Contact> getAllContacts(String user, char[] password) throws AuthenticationException, ServiceException, IOException {
        ContactsService myService = new ContactsService(Constants.VERSION);
        myService.setUserCredentials(user, String.valueOf(password));
        return getAllContacts(myService);
    }

    private static HashMap<String, Contact> getAllContacts(ContactsService myService)
            throws ServiceException, IOException {
        // Request the feed
        URL feedUrl = new URL(!GlobalSettings.hasProperty("google.contacts.feedurl")
                ? "https://www.google.com/m8/feeds/contacts/default/full" : GlobalSettings.getProperty("google.contacts.feedurl"));
        ContactFeed resultFeed = myService.getFeed(feedUrl, ContactFeed.class);

        HashMap<String, Contact> result = new HashMap<String, Contact>();

        for (int i = 0; i < resultFeed.getEntries().size(); i++) {
            ContactEntry entry = resultFeed.getEntries().get(i);
            Contact c = new Contact();

            if (entry.hasName()) {
                Name name = entry.getName();
                if (name.hasGivenName()) {
                    c.setPrename(name.getGivenName().getValue());
                }
                if (name.hasNamePrefix()) {
                    c.setTitle(name.getNamePrefix().getValue());
                }
                if (name.hasGivenName()) {
                    c.setCName(name.getFamilyName().getValue());
                }
                if (name.hasAdditionalName()) {
                }
            }

            for (PostalAddress post : entry.getPostalAddresses()) {
                if (post.getPrimary()) {
                    c.setStreet(post.getValue());
                }//FIXME use all
            }

            for (PhoneNumber p : entry.getPhoneNumbers()) {
                if (p.getPrimary()) {
                    c.setMainphone(p.getPhoneNumber());
                } else {
                    c.setMobilephone(p.getPhoneNumber());
                }//FIXME use all numbers
            }

            if (entry.hasGender()) {
                c.setisMale(entry.getGender().toString().equals("male"));
            }

            if (entry.hasOrganizations());//FIXME

            for (Email email : entry.getEmailAddresses()) {
                c.setMailaddress(email.getAddress());
            }//FIXME use all addresses

            for (Im im : entry.getImAddresses()) {
                c.setNotes(im.getLabel() + " " + im.getAddress() + "\n");
            }

            for (GroupMembershipInfo group : entry.getGroupMembershipInfos()) {
            }

            for (ExtendedProperty property : entry.getExtendedProperties()) {
                if (property.getValue() != null) {
                } else if (property.getXmlBlob() != null) {
                }
            }

            Link photoLink = entry.getContactPhotoLink();
            if (photoLink.getEtag() != null) {
            }

            c.settaxnumber(entry.getId());

            result.put(entry.getId(), c);
        }

        return result;
    }
}
