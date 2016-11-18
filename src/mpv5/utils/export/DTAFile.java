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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import mpv5.db.common.Context;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Tax;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.handler.VariablesHandler;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;
import mpv5.utils.date.DateConverter;
import dtaus.DTAus;
import dtaus.Konto;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileReaderWriter;
import mpv5.utils.jobs.Waitable;

/**
 *
 *  
 */
public class DTAFile extends Exportable implements Waitable {

    private DTAFile(String pathToFile) {
        super(pathToFile);
        if (!exists()) {
            try {
                createNewFile();
            } catch (IOException ex) {
                Log.Debug(ex);
            }
        }
    }

    public DTAFile(HashMap<String, Object> map) {
        this(FileDirectoryHandler.getTempFile("export-" + DateConverter.getTodayDBDate(), "dtaus").getAbsolutePath());
        setData(map);
    }


    public void run() {

        try {

            Log.Debug(this, "run: ");
            mpv5.YabsViewProxy.instance().setWaiting(true);
            Log.Debug(this, "All fields:");
            HashMap<String, Object> datas = getData();

            DTAus dta = null;
            try {
                dta = new DTAus(User.getCurrentUser().getDTAAccount(), "lk");
            } catch (Exception e) {
                Popup.notice(Messages.DTAUS_NOT_SET);
                return;
            }
            List<String> control = new ArrayList<String>();

            for (Iterator<String> it = datas.keySet().iterator(); it.hasNext();) {
                Item dbo = (Item) datas.get(it.next());
                Contact c = (Contact) Item.getObject(Context.getContact(), dbo.__getContactsids());

                Konto k = new Konto(c.__getBankid(), c.__getBankaccount(), c.__getBankname());
//
                DTAus.Transaction t = dta.new Transaction(k);
                List<String> usages = User.getCurrentUser().getDTAUsages();
                for (int i = 0; i < usages.size(); i++) {
                    String string = usages.get(i);
                    t.addUsage(VariablesHandler.parse(string, dbo));
                }

                String cid = "";
                if (c.__getCNumber().length() > 10) {
                    cid = c.__getCNumber().substring(c.__getCNumber().length() - 10, c.__getCNumber().length());
                } else {
                    cid = c.__getCNumber();
                }
                t.internalCustomerId = cid;

                BigDecimal value = null;

                value = //netvalue
                            dbo.__getTaxvalue().add(dbo.__getNetvalue());

                if (value.doubleValue() > 0.15) {
                    value.setScale(2, RoundingMode.HALF_UP);
                    DecimalFormat f = new DecimalFormat("##,###0.00");
                    DecimalFormatSymbols sym = new DecimalFormatSymbols();
                    sym.setDecimalSeparator(',');
                    sym.setGroupingSeparator('.');
                    f.setDecimalFormatSymbols(sym);
                    t.setValue(f.format(value.doubleValue()));
                    dta.addEntry(t);
                    control.add(c.__getCname() + "\t" + c.__getBankaccount() + "\t" + c.__getBankname() + "\t" + dbo.__getCnumber() + "\t" + value.toPlainString());
                }
            }

            FileReaderWriter w = new FileReaderWriter(this);
            w.writeOnce(dta.toDTAstring());
            Popup.notice(control, Messages.DTAUS_CREATED);

        } catch (Exception ex) {
            Log.Debug(ex);
        } finally {
            mpv5.YabsViewProxy.instance().setWaiting(false);
        }
    }

    public Exception waitFor() {
        try {
            run();
        } catch (Exception e) {
            return e;
        }
        return null;
    }
}
