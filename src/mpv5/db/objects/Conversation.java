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
package mpv5.db.objects;

import enoa.handler.TemplateHandler;
import java.util.Date;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.Templateable;
import mpv5.globals.Constants;
import mpv5.handler.FormatHandler;
import mpv5.ui.panels.ConversationPanel;
import mpv5.utils.images.MPIcon;

/**
 *
 *  
 */
public class Conversation
        extends DatabaseObject
        implements Templateable {

    private static final long serialVersionUID = 6039340924254489575L;
    private String cnumber = "";
    private int contactsids;
    private String adress = "";
    private Date date = null;
    private String content = "";

    public int __getContactsids() {
        return contactsids;
    }

    public void setContactsids(int contactsids) {
        this.contactsids = contactsids;
    }

    
    public String __getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String __getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public String __getCnumber() {
        return cnumber;
    }

    public void setCnumber(String cnumber) {
        this.cnumber = cnumber;
    }

    public Date __getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Conversation() {
        setContext(Context.getConversation());
    }

    @Override
    public String toString() {
        return __getCname();
    }

    @Override
    public JComponent getView() {
        return ConversationPanel.instanceOf();
    }

    @Override
    public MPIcon getIcon() {
        return null;
    }

    public int templateType() {
       return Constants.TYPE_CONVERSATION;
    }

    public int templateGroupIds() {
       return this.__getGroupsids();
    }

    public FormatHandler getFormatHandler() {
        return null;
    }
}
