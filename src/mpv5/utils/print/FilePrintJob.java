/*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.print;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mpv5.db.common.DatabaseObject;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileReaderWriter;
import mpv5.utils.files.TextDatFile;
import mpv5.utils.xml.XMLWriter;

/**
 *
 * 
 */
public class FilePrintJob {

    private DatabaseObject dbobj;
    private ArrayList<DatabaseObject> dbobjarr;

    public FilePrintJob(ArrayList<DatabaseObject> dataOwner) {
        this.dbobjarr = dataOwner;
    }

    public FilePrintJob(DatabaseObject dataOwner) {
        this.dbobj = dataOwner;
    }

    public void toCSV() {
        TextDatFile f = new TextDatFile();
        String[] head = null;
        String[][] dat = null;
        String name = null;

        if (dbobj != null) {
            dbobjarr = new ArrayList<DatabaseObject>();
            dbobjarr.add(dbobj);
            name = dbobj.__getCname();
        }

        if (dbobjarr != null) {

            List<String[]> data = dbobjarr.get(0).getValues();
            if (name == null) {
                name = dbobjarr.get(0).getDbIdentity();
            }
            head = new String[data.size()];
            for (int i = 0; i < data.size(); i++) {
                head[i] = data.get(i)[0];
            }
            dat = new String[dbobjarr.size()][data.size()];
            for (int i = 0; i < dbobjarr.size(); i++) {
                data = dbobjarr.get(0).getValues();
                for (int k = 0; k < data.size(); k++) {
                    dat[i][k] = data.get(k)[1];
                }
            }
        }

        f.setHeader(head);
        f.setData(dat);
        mpv5.YabsViewProxy.instance().showFilesaveDialogFor(f.createFile(name));
    }

    public void toVCF() {
        String name = "contacts.vcf";
        ArrayList<String[]> specs = new ArrayList<String[]>();
        specs.add(new String[]{"N:", "cname;prename;;title"});
        specs.add(new String[]{"FN:", "cnumber"});
        specs.add(new String[]{"ORG:", "companyid"});
        specs.add(new String[]{"EMAIL;type=WORK:", "mailaddress"});
        specs.add(new String[]{"URL;type=WORK:", "website"});
        specs.add(new String[]{"TEL;type=HOME:", "mainphone"});
        specs.add(new String[]{"TEL;type=FAX:", "fax"});
        specs.add(new String[]{"TEL;type=WORK:", "workphone"});
        specs.add(new String[]{"TEL;type=WORK:", "mobilephone"});
        specs.add(new String[]{"ADR;type=WORK:", ";;street;city;;zip;"});
        specs.add(new String[]{"NOTE;QUOTED-PRINTABLE:", "notes"});


        if (dbobj != null) {
            dbobjarr = new ArrayList<DatabaseObject>();
            dbobjarr.add(dbobj);
            name = dbobj.__getCname();
        }
        File f = FileDirectoryHandler.getTempFile(name, "vcf");
        FileReaderWriter rw = new FileReaderWriter(f);

        if (dbobjarr != null) {
            for (int i = 0; i < dbobjarr.size(); i++) {
                DatabaseObject databaseObject = dbobjarr.get(i);
               List<String[]> data = databaseObject.getValues();
                if (name == null) {
                    name = databaseObject.getDbIdentity();
                }
                for (int h = 0; h < data.size(); h++) {
                    for (int idx = 0; idx < specs.size(); idx++) {
                        String[] elem = specs.get(idx);
                        if (elem[1].contains(data.get(h)[0].toLowerCase())) {
                            elem[1] = elem[1].replace(data.get(h)[0].toLowerCase(), data.get(h)[1]).replaceAll("[\\r\\n]", " ");
                        }
                    }
                }
                String vCard = "BEGIN:VCARD\n";
                for (int j = 0; j < specs.size(); j++) {
                    String[] strings = specs.get(j);
                    vCard += strings[0] + strings[1] + "\n";
                }
                vCard += "VERSION:3.0\n" + "END:VCARD\n";
                rw.write(vCard);
            }
        }

        mpv5.YabsViewProxy.instance().showFilesaveDialogFor(f);
    }

    public void toXML() {
        XMLWriter xmlw = new XMLWriter();
        xmlw.newDoc(true);
        String name = dbobj.__getCname();

        if (dbobj != null) {
            dbobjarr = new ArrayList<DatabaseObject>();
            dbobjarr.add(dbobj);

            xmlw.add(dbobjarr);
        }

       
        mpv5.YabsViewProxy.instance().showFilesaveDialogFor(xmlw.createFile(name));
    }
}
