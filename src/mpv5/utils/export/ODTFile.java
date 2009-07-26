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
package mpv5.utils.export;

import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.text.ITextDocument;
import enoa.connection.NoaConnection;
import enoa.handler.DocumentHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.logging.Log;

/**
 *
 *  
 */
public class ODTFile extends Exportable {

    private NoaConnection nc;

    public ODTFile(String pathToFile) {
        super(pathToFile);
        nc = NoaConnection.getConnection();
    }

    @Override
    public void run() {
        Log.Debug(this, "run: ");
        DocumentHandler dh = new DocumentHandler(nc);
        try {
            IDocument df = dh.loadDocument(this, true);
            DocumentHandler.fillFormFields((ITextDocument) df, getData());
            DocumentHandler.fillPlaceholderFields((ITextDocument) df, getData());
            DocumentHandler.fillTextVariableFields((ITextDocument) df, getData());
            DocumentHandler.saveAs(df, getTarget());
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }
}
