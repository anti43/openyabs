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
package enoa.handler;

import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.DocumentException;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.filter.IFilter;
import ag.ion.bion.officelayer.filter.MSOffice97Filter;
import ag.ion.bion.officelayer.filter.PDFFilter;
import ag.ion.bion.officelayer.filter.TextFilter;
import ag.ion.bion.officelayer.form.IFormComponent;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextField;
import ag.ion.bion.officelayer.text.ITextFieldService;
import ag.ion.bion.officelayer.text.IVariableTextFieldMaster;
import ag.ion.noa.NOAException;
import com.sun.star.awt.XTextComponent;
import com.sun.star.beans.XPropertySet;
import com.sun.star.form.XFormComponent;
import com.sun.star.uno.UnoRuntime;
import enoa.connection.NoaConnection;
import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class OpenOffice Documents IO
 */
public class DocumentHandler {

    /**
     * All known OO file extensions
     */
    public static final String EXTENSION = ".*ott$|.*sxw$|.*doc$|.*xls$|.*odt$|.*ods$|.*pps$|.*odt$|.*ppt$|.*odp$";
    /**
     * A FileFilter looking for OO files
     */
    public static FileFilter OFFICE_FILE_FILTER = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            return pathname.getName().matches(EXTENSION);
        }
    };
    private final NoaConnection connection;
    private final DocumentDescriptor descriptor;

    /**
     * Creates a new Document Handler on top of the given connection
     * @param connection The OO connection to use
     */
    public DocumentHandler(NoaConnection connection) {
        if (connection != null) {
            this.connection = connection;
            descriptor = DocumentDescriptor.DEFAULT_HIDDEN;
        } else {
            throw new NullPointerException("Connection can not be null");
        }
    }

    /**
     * Load an existing document into the Document Handler and return an IDocument
     * @param file The file to load
     * @param asTemplate If true, the file is treatened as template (.ott)
     * @return
     * @throws Exception Any error thrown
     */
    public IDocument loadDocument(File file, boolean asTemplate) throws Exception {
        if (!OFFICE_FILE_FILTER.accept(file)) {
            throw new UnsupportedOperationException("The file extension must match: " + EXTENSION);
        }
        if (asTemplate) {
            descriptor.setAsTemplate(asTemplate);
        }

        return connection.getDocumentService().loadDocument(file.getPath());
    }

    /**
     * Creates a new, empty text document (.odt)
     * @return
     * @throws Exception
     */
    public ITextDocument newTextDocument() throws Exception {
        IDocument document = connection.getDocumentService().constructNewDocument(IDocument.WRITER, descriptor);
        ITextDocument textDocument = (ITextDocument) document;
        return textDocument;
    }

    /**
     * Save the given document to the physical location of the given file.
     * @param doc
     * @param file
     * @throws DocumentException
     */
    public synchronized static void saveAs(IDocument doc, File file) throws DocumentException {

        doc.reformat();

        if (file.getName().split("\\.").length < 2) {
            throw new UnsupportedOperationException("The file must have an extension: " + file);
        }

        IFilter filter = null;
        String extension = file.getName().substring(file.getName().lastIndexOf("."), file.getName().length());
        if (extension.equalsIgnoreCase(".pdf")) {
            filter = PDFFilter.FILTER;
        } else if (extension.equalsIgnoreCase(".doc")) {
            filter = MSOffice97Filter.FILTER;
        } else if (extension.equalsIgnoreCase(".txt")) {
            filter = TextFilter.FILTER;
        }

        if (filter != null) {
            doc.getPersistenceService().export(file.getPath(), filter);
        } else {
            throw new UnsupportedOperationException("File extension not supported: " + extension);
        }

    }

    /**
     * Fill the Form Fields of the template with values
     * @param textDocument
     * @param data
     * @throws Exception
     * @throws NOAException
     */
    public synchronized static void fillFormFields(ITextDocument textDocument, HashMap<String, String> data) throws Exception, NOAException {

        IFormComponent[] formComponents = textDocument.getFormService().getFormComponents();
        Iterator<String> keys = data.keySet().iterator();
        String key = null;
        while (keys.hasNext()) {

            // get column name
            key = keys.next();
            for (int i = 0; i < formComponents.length; i++) {

                XFormComponent xFormComponent = formComponents[i].getXFormComponent();
                XTextComponent xTextComponent = formComponents[i].getXTextComponent();
                XPropertySet propertySet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class,
                        xFormComponent);

                if (propertySet != null && propertySet.getPropertySetInfo().hasPropertyByName("Name")) {
                    String n = propertySet.getPropertyValue("Name").toString();
                    if (n.matches(key)) {
                        xTextComponent.setText(data.get(key));
                    }
                }
            }
            textDocument.getTextFieldService().refresh();
        }
    }

    /**
     * Fill the Placeholder Fields of the template with values
     * @param textDocument
     * @param data
     * @throws Exception
     * @throws NOAException
     */
    public synchronized static void fillPlaceholderFields(ITextDocument textDocument, HashMap<String, String> data) throws Exception, NOAException {
        Iterator<String> keys = data.keySet().iterator();
        String key = null;
        while (keys.hasNext()) {
            // get column name
            key = keys.next();
            try {
                ITextFieldService textFieldService = textDocument.getTextFieldService();
                ITextField[] placeholders = textFieldService.getPlaceholderFields();
                for (int i = 0; i <
                        placeholders.length; i++) {
                    String placeholderDisplayText = placeholders[i].getDisplayText();
                    if (placeholderDisplayText.matches(key)) {
                        placeholders[i].getTextRange().setText(data.get(key));
                    }

                }
            } catch (java.lang.Exception ex) {
            }
            textDocument.getTextFieldService().refresh();
        }

    }

    /**
     * Fill the Variable Text Fields of the template with values
     * @param textDocument
     * @param data
     * @throws Exception
     * @throws NOAException
     */
    public synchronized static void fillTextVariableFields(ITextDocument textDocument, HashMap<String, String> data) throws Exception, NOAException {
        Iterator<String> keys = data.keySet().iterator();
        String key = null;
        IVariableTextFieldMaster x;

        while (keys.hasNext()) {
            // get column name
            key = keys.next();
            ITextFieldService textFieldService = textDocument.getTextFieldService();
            x =
                    textFieldService.getVariableTextFieldMaster(key);
            if (x != null) {
                ITextField[] variables = x.getVariableTextFields();
                for (int i = 0; i <
                        variables.length; i++) {

                    XPropertySet xPropertySetField = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, variables[i].getXTextContent());


                    if (xPropertySetField.getPropertyValue(
                            "CurrentPresentation").toString().matches(key)) {
                        xPropertySetField.setPropertyValue("Content", data.get(key));
                    }
                }

                textDocument.getTextFieldService().refresh();
            }
        }
    }

    /**
     * Export a file to another format/file. Supported target formats:
     * <li>pdf
     * <li>odt
     * <li>txt
     * @param source The file to export
     * @param target The target file
     * @return The target file
     * @throws DocumentException
     */
    public File export(File source, File target) throws DocumentException {
        if (target.getName().split("\\.").length < 2) {
            throw new UnsupportedOperationException("The file must have an extension: " + target);
        }

        IFilter filter = null;
        String extension = target.getName().substring(target.getName().lastIndexOf("."), target.getName().length());
        if (extension.equalsIgnoreCase(".pdf")) {
            filter = PDFFilter.FILTER;
        } else if (extension.equalsIgnoreCase(".doc")) {
            filter = MSOffice97Filter.FILTER;
        } else if (extension.equalsIgnoreCase(".txt")) {
            filter = TextFilter.FILTER;
        }

        if (filter != null) {
            NoaConnection.getConnection().getDocumentService().loadDocument(source.getAbsolutePath()).getPersistenceService().export(target.getPath(), filter);
        } else {
            throw new UnsupportedOperationException("File extension not supported: " + extension);
        }

        return target;
    }
}
