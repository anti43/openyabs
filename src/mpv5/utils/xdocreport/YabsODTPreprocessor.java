/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.xdocreport;

import fr.opensagres.xdocreport.document.odt.preprocessor.ODTPreprocessor;
import fr.opensagres.xdocreport.document.preprocessor.IXDocPreprocessor;
import fr.opensagres.xdocreport.document.preprocessor.sax.BufferedDocumentContentHandler;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import fr.opensagres.xdocreport.template.formatter.IDocumentFormatter;
import java.util.Map;

/**
 *
 * @author dev
 */
public class YabsODTPreprocessor extends ODTPreprocessor {

    public static final IXDocPreprocessor INSTANCE = new YabsODTPreprocessor();

    @Override
    protected BufferedDocumentContentHandler<?> createBufferedDocumentContentHandler(String entryName,
            FieldsMetadata fieldsMetadata,
            IDocumentFormatter formatter,
            Map<String, Object> sharedContext) {
        //fieldsMetadata.setEvaluateEngineOnlyForFields(true);
        return new YabsODTContentHandler(entryName, fieldsMetadata, formatter, sharedContext);
    }
}
