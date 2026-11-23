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
package mpv5.ui.dialogs.belegwizard;

import java.math.BigDecimal;
import java.util.Date;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.User;
import mpv5.handler.ContactService;
import mpv5.handler.ItemService;
import mpv5.utils.date.DateConverter;

/**
 * Mutable document draft state shared across all steps of the Beleg-Wizard.
 */
public class BelegWizardState {

    private int itemType = Item.TYPE_INVOICE;
    private Contact receiver;
    private int groupId;
    private int accountId;
    private int statusId = Item.STATUS_IN_PROGRESS;
    private Date docDate = new Date();
    private Date serviceDate;
    private Date dueDate;
    private String title = "";
    private String notes = "";
    private BigDecimal customerTaxRate;

    public BelegWizardState() {
        User u = User.getCurrentUser();
        this.groupId = (u != null && u.__getGroupsids() > 0) ? u.__getGroupsids() : 1;
        this.accountId = (u != null && u.__getIntdefaultaccount() > 0) ? u.__getIntdefaultaccount() : 1;
        this.statusId = (u != null && u.__getIntdefaultstatus() > 0) ? u.__getIntdefaultstatus() : Item.STATUS_IN_PROGRESS;
        Date d = ItemService.dueDate(null, false);
        this.dueDate = d != null ? d : DateConverter.addDays(new Date(), 14);
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public Contact getReceiver() {
        return receiver;
    }

    /**
     * Sets the receiver and adopts their group, customer tax rate, and payment
     * term due date.
     */
    public void setReceiver(Contact receiver) {
        this.receiver = receiver;
        if (receiver != null) {
            if (receiver.__getGroupsids() > 0) {
                this.groupId = receiver.__getGroupsids();
            }
            this.customerTaxRate = ContactService.customerTaxRate(receiver);
            Date due = ItemService.dueDate(receiver, false);
            if (due != null) {
                this.dueDate = due;
            }
        } else {
            this.customerTaxRate = null;
        }
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId > 0 ? groupId : 1;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId > 0 ? accountId : 1;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public Date getDocDate() {
        return docDate != null ? docDate : new Date();
    }

    public void setDocDate(Date docDate) {
        this.docDate = docDate != null ? docDate : new Date();
    }

    public Date getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(Date serviceDate) {
        this.serviceDate = serviceDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public String getNotes() {
        return notes == null ? "" : notes;
    }

    public void setNotes(String notes) {
        this.notes = notes == null ? "" : notes;
    }

    public BigDecimal getCustomerTaxRate() {
        return customerTaxRate;
    }

    public void setCustomerTaxRate(BigDecimal customerTaxRate) {
        this.customerTaxRate = customerTaxRate;
    }
}
