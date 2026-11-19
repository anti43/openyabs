package mpv5.db.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.globals.Constants;
import org.junit.jupiter.api.Test;

/**
 * Which scripts of the script manager apply to a record: all documents and
 * the type of the document, the roles of a contact, the group and its
 * parents
 */
class ScriptScopeTest {

    @Test
    void documentsGetAllDocumentsAndTheirTypeWhateverTheyWereLoadedAs() {
        Item offer = new Item();
        offer.setInttype(Constants.TYPE_OFFER);
        assertEquals(Arrays.asList(ScriptScope.ALL_DOCUMENTS, Context.getOffer().getId()), ScriptScope.kinds(offer));
        Item invoice = new Item();
        invoice.setInttype(Constants.TYPE_INVOICE);
        assertEquals(Arrays.asList(ScriptScope.ALL_DOCUMENTS, Context.getInvoice().getId()), ScriptScope.kinds(invoice));
        assertEquals(Context.getCredit().getId(), ScriptScope.documentKind(Constants.TYPE_CREDIT, -1));
        assertEquals(Context.getDeposit().getId(), ScriptScope.documentKind(Constants.TYPE_DEPOSIT, -1));
        assertEquals(Context.getPartPayment().getId(), ScriptScope.documentKind(Constants.TYPE_PART_PAYMENT, -1));
        assertEquals(Context.getOrder().getId(), ScriptScope.documentKind(Constants.TYPE_ORDER, -1));
        assertEquals(77, ScriptScope.documentKind(Constants.TYPE_CONTRACT, 77));
    }

    @Test
    void contactsGetAllContactsAndTheirRoles() {
        Contact c = new Contact();
        c.setisCustomer(true);
        c.setisSupplier(false);
        c.setisManufacturer(false);
        assertEquals(Arrays.asList(Context.getContact().getId(), Context.getCustomer().getId()), ScriptScope.kinds(c));
        c.setisSupplier(true);
        assertEquals(Arrays.asList(Context.getContact().getId(), Context.getCustomer().getId(), Context.getSupplier().getId()),
                ScriptScope.kinds(c));
    }

    @Test
    void groupOfTheScriptMustBeAllGroupsTheGroupOrAParent() {
        //record in group 7, its parent 3, the root 1
        java.util.List<Integer> chain = Arrays.asList(7, 3, 1);
        assertEquals(0, ScriptScope.groupRank(1, chain));
        assertEquals(0, ScriptScope.groupRank(0, chain));
        assertEquals(1, ScriptScope.groupRank(3, chain));
        assertEquals(2, ScriptScope.groupRank(7, chain));
        assertEquals(-1, ScriptScope.groupRank(9, chain));
        //a record without group: only scripts for all groups
        assertEquals(0, ScriptScope.groupRank(1, Collections.<Integer>emptyList()));
        assertEquals(-1, ScriptScope.groupRank(3, Collections.<Integer>emptyList()));
    }
}
