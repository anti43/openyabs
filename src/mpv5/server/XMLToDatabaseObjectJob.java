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
package mpv5.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import mpv5.db.common.DatabaseObject;
import mpv5.logging.Log;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileReaderWriter;
import mpv5.utils.jobs.Job;
import mpv5.utils.xml.XMLReader;

/**
 *
 */
public class XMLToDatabaseObjectJob extends MPServerJob {
    private ArrayList<ArrayList<DatabaseObject>> dos;

    /**
     *
     * @param xmlData
     * @param socket
     * @throws IOException
     */
    public XMLToDatabaseObjectJob(String xmlData, Socket socket) throws IOException {
        super(xmlData, socket);
    }

    public XMLToDatabaseObjectJob() {
        super();
    }

    @Override
    public Exception waitFor() {
        try {
            File file = FileDirectoryHandler.getTempFile("xml");
            FileReaderWriter fr = new FileReaderWriter(file);
            fr.writeOnce(super.getXmlData());
            XMLReader x = new XMLReader();
            x.newDoc(file, true);
            dos = x.getObjects();
            return null;
        } catch (Exception ex) {
            Log.Debug(ex);
            super.getOut().println(ex.getMessage());
            return ex;
        }
    }

    @Override
    public void set(Object object, Exception e) throws Exception {
        if (e == null) {
            try {
                ArrayList<DatabaseObject> l = new ArrayList<DatabaseObject>();

                for (int i = 0; i < dos.size(); i++) {
                    ArrayList<DatabaseObject> arrayList = dos.get(i);
                    for (int j = 0; j < arrayList.size(); j++) {
                        DatabaseObject databaseObject = arrayList.get(j);
                        l.add(databaseObject);
                    }
                }

                for (int i = 0; i < l.size(); i++) {
                    DatabaseObject databaseObject = l.get(i);
                    if (databaseObject.save()) {
                        Log.Debug(this, "Done for: " + databaseObject.__getCName());
                        super.getOut().println(MPServerRunner.COMMAND_DONE);
                    } else {
                        super.getOut().println(MPServerRunner.ERROR_OCCOURED + " [" + databaseObject + "]");
                    }
                }
            } catch (Exception ex) {
                Log.Debug(ex);
                super.getOut().println(ex.getMessage());
            }
        } else {
            super.getOut().println(e.getMessage());
        }
    }

    @Override
    public void start() {
        new Job(this, this, MPServerRunner.JOB_DONE).execute();
    }
}
