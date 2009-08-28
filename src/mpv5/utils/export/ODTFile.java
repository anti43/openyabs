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

import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.text.ITextDocument;
import enoa.connection.NoaConnection;
import enoa.handler.DocumentHandler;
import java.io.IOException;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;

/**
 *
 *  
 */
public class ODTFile extends Exportable {

    private NoaConnection nc;
    private final DocumentHandler dh;
    private IDocument df;

    public ODTFile(String pathToFile) {
        super(pathToFile);
        if (!exists()) {
            try {
                createNewFile();
            } catch (IOException ex) {
                Log.Debug(ex);
            }
        }
        nc = NoaConnection.getConnection();
        dh = new DocumentHandler(nc);
        try {
            df = dh.loadDocument(this, false);
            Log.Debug(this, "Loaded odt file: " + this);
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }

    @Override
    public void run() {
        
        Log.Debug(this, "run: ");
        MPView.setWaiting(true);
        try {
            dh.fillPlaceholderFields((ITextDocument) df, getData());
//            dh.fillTextVariableFields((ITextDocument) df, getData());//Omitted for performance reasons
            dh.fillTables((ITextDocument)df, getData());
            dh.saveAs(df, getTarget());
        } catch (Exception ex) {
            Log.Debug(ex);
        } finally {
            MPView.setWaiting(false);
            
        }
    }
}
