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
package noa.handler;

import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocument;
import noa.connection.NoaConnection;
import java.io.File;

/**
 *
 */
public class DocumentHandler {

    private final NoaConnection connection;
    private final DocumentDescriptor descriptor;

    public DocumentHandler(NoaConnection connection) {
        this.connection = connection;
        descriptor = DocumentDescriptor.DEFAULT_HIDDEN;
    }

    public IDocument loadDocument(File file, boolean asTemplate) throws Exception {
        if (asTemplate) {
            descriptor.setAsTemplate(asTemplate);
        }
        return connection.getDocumentService().loadDocument(file.getPath());
    }

    public IDocument newDocument() throws Exception {
        return connection.getDocumentService().constructNewDocument("", descriptor);
    }
}
