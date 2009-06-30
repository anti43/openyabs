/*
 *  This file is part of MP.
 *
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.db.objects;

import java.util.ArrayList;
import javax.swing.JComponent;

import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryHandler;

/**
 *
 *  Messages to be printed on Bills etc
 */
public class MailMessage extends DatabaseObject {

    private int intstatus;
    private int usersids;
    private String mailid;

    /**
     * A mail which is sent but not yet received
     */
    public static final int STATUS_QUEUED = 0;
    /**
     * After a mail has been sent and is received,
     * it enters this status as long as it is not marked as <b>READ</b>
     */
    public static final int STATUS_SENT = 1;
    /**
     * After a mail has been read, it enters this status
     */
    public static final int STATUS_READ = 2;


    public MailMessage() {
        context.setDbIdentity(Context.IDENTITY_MAIL);
        context.setIdentityClass(this.getClass());
    }

    @Override
    public JComponent getView() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Get all Items which are assigned to this message
     * @return
     * @throws mpv5.db.common.NodataFoundException
     */
    public ArrayList<Item> getItemsWithMessage() throws NodataFoundException {
        ArrayList<Item> tmp = DatabaseObject.getReferencedObjects((Item) DatabaseObject.getObject(Context.getItems()), Context.getMessagesToItems());

        return tmp;
    }
//
//    /**
//     * Get all messages where the given item is currently assigned to
//     * @param item
//     * @return
//     * @throws mpv5.db.common.NodataFoundException
//     */
//    public static ArrayList<MailMessage> getMessagesOfItem(Item item) throws NodataFoundException {
//        Object[][] tmp = QueryHandler.instanceOf().clone(Context.getMessagesToItems()).select("messagesids", new String[]{"itemsids", item.__getIDS().toString(), ""});
//        ArrayList<MailMessage> l = new ArrayList<MailMessage>();
//
//        for (int i = 0; i < tmp.length; i++) {
//            int id = Integer.valueOf(tmp[i][0].toString());
//            l.add((MailMessage) DatabaseObject.getObject(Context.getMessages(), id));
//        }
//
//        return l;
//    }

  @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
    }

    /**
     * @return the intstatus
     */
    public int __getIntstatus() {
        return intstatus;
    }

    /**
     * @param intstatus the intstatus to set
     */
    public void setIntstatus(int intstatus) {
        this.intstatus = intstatus;
    }

    /**
     * @return the usersids
     */
    public int __getUsersids() {
        return usersids;
    }

    /**
     * @param usersids the usersids to set
     */
    public void setUsersids(int usersids) {
        this.usersids = usersids;
    }

    /**
     * @return the mailid
     */
    public String __getMailid() {
        return mailid;
    }

    /**
     * @param mailid the mailid to set
     */
    public void setMailid(String mailid) {
        this.mailid = mailid;
    }
}
