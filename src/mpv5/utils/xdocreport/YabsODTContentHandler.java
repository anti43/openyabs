/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.xdocreport;

import fr.opensagres.xdocreport.core.utils.StringUtils;
import static fr.opensagres.xdocreport.document.odt.ODTConstants.TEXT_NS;
import fr.opensagres.xdocreport.document.preprocessor.sax.BufferedDocument;
import fr.opensagres.xdocreport.document.preprocessor.sax.BufferedDocumentContentHandler;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import fr.opensagres.xdocreport.template.formatter.IDocumentFormatter;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author dev
 */
public class YabsODTContentHandler extends BufferedDocumentContentHandler<BufferedDocument> {

    private boolean textFieldParsing = false;
    private final String place = "placeholder";

    public YabsODTContentHandler(String entryName, FieldsMetadata fieldsMetadata, IDocumentFormatter formatter, Map<String, Object> sharedContext) {
        super();
    }

    @Override
    public boolean doStartElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if (isTextField(uri, localName)) {
            this.textFieldParsing = true;
            return false;
        } else {
            return super.doStartElement(uri, localName, name, attributes);
        }
    }

    @Override
    public void doEndElement(String uri, String localName, String name) throws SAXException {
        if (isTextField(uri, localName)) {
            this.textFieldParsing = false;
        } else {
            super.doEndElement(uri, localName, name);
        }
    }

    @Override
    protected void flushCharacters(String characters) {
        if (this.textFieldParsing) {
            characters = StringUtils.xmlUnescape(characters);
            characters = characters.replace("<", "$");
            characters = characters.replace(">", "");
        }
        super.flushCharacters(characters);
    }

    private boolean isTextField(String uri, String localName) {
        return TEXT_NS.equals(uri) && place.equals(localName);
    }
}
