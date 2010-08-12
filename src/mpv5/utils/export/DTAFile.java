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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import mpv5.db.common.Context;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
import mpv5.utils.date.DateConverter;
import mpv5.utils.files.FileReaderWriter;
import org.kapott.hbci.structures.Value;
import org.kapott.hbci.swift.DTAUS;

/**
 *
 *  
 */
public class DTAFile extends Exportable {

    public DTAFile(String pathToFile) {
        super(pathToFile);
        if (!exists()) {
            try {
                createNewFile();
            } catch (IOException ex) {
                Log.Debug(ex);
            }
        }
    }

    @Override
    public void run() {

        try {

            Log.Debug(this, "run: ");
            MPView.setWaiting(true);
            Log.Debug(this, "All fields:");
            HashMap<String, Object> datas = getData();

            DTAUS dta = new DTAUS(User.getCurrentUser().getBankAccount(), DTAUS.TYPE_DEBIT);

            for (Iterator<String> it = datas.keySet().iterator(); it.hasNext();) {
                Item dbo = (Item) datas.get(it.next());
                Contact c = (Contact) Item.getObject(Context.getContact(), dbo.__getContactsids());
                DTAUS.Transaction t = dta.new Transaction();
                t.addUsage(Messages.DTAUS_REASON.getValue());
                t.addUsage(dbo.__getCnumber());
                t.addUsage(DateConverter.getDefDateString(dbo.__getDateadded()));
                t.internalCustomerId = c.__getCNumber().substring(c.__getCNumber().length() - 10, c.__getCNumber().length());
                t.value = new Value(
                        (dbo.__getNetvalue().add(dbo.__getTaxvalue().add(dbo.__getShippingvalue())).multiply(new BigDecimal("100")).longValueExact()));

                dta.addEntry(t);
            }

            FileReaderWriter w = new FileReaderWriter(this);
            w.write(dta.toString());

        } catch (Exception ex) {
            Log.Debug(ex);
        } finally {
            MPView.setWaiting(false);
        }
    }
}
