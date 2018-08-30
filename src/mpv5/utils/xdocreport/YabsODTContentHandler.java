/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.xdocreport;


import fr.opensagres.xdocreport.core.document.DocumentKind;
import fr.opensagres.xdocreport.core.utils.StringUtils;
import static fr.opensagres.xdocreport.document.odt.ODTConstants.TEXT_NS;
import fr.opensagres.xdocreport.document.odt.preprocessor.ODTBufferedDocumentContentHandler;
import fr.opensagres.xdocreport.document.preprocessor.sax.BufferedElement;
import fr.opensagres.xdocreport.document.textstyling.ITransformResult;
import fr.opensagres.xdocreport.template.formatter.FieldMetadata;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import fr.opensagres.xdocreport.template.formatter.IDocumentFormatter;
import java.util.Map;

import mpv5.logging.Log;
import mpv5.utils.export.ODTFile2;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author dev
 */
public class YabsODTContentHandler extends ODTBufferedDocumentContentHandler {

    private static final String TEXT_P = "text:p";
    
    private boolean textFieldParsing = false;
    private boolean inTargetTable = false;
    private boolean inTargetCol = false;
    private final String place = "placeholder";
    private final String TABLE_NS = "urn:oasis:names:tc:opendocument:xmlns:table:1.0";
    private final String tab = "table";
    private final String col = "table-cell";
    private final String row = "table-row";
    private final String para = "p";
    private String currentTableKey = null;
    private int i;

    public YabsODTContentHandler(String entryName, FieldsMetadata fieldsMetadata, IDocumentFormatter formatter, Map<String, Object> sharedContext) {
        super(entryName, fieldsMetadata, formatter, sharedContext);
    }

    @Override
    public boolean doStartElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if (isTextField(uri, localName)) {
            this.textFieldParsing = true;
            return false;
        } else if (isTable(uri, localName)) {
            String a = attributes.getValue(0);
            if (a.startsWith(ODTFile2.KEY_TABLE )) {
                this.inTargetTable = true;
                this.currentTableKey = a;
            }
        } else if (isColumn(uri, localName)) {
            if (this.inTargetTable) {
                this.inTargetCol = true;
                i++;
            }
        } else if (isParagraph(uri, localName)) {
            if (this.inTargetCol) {
                if (super.doStartElement(uri, localName, name, attributes)) {

                    Log.Debug(this, uri + ", " + localName + ", " + name + ", " + currentTableKey);
                    BufferedElement buffer = this.getCurrentElement();
                    buffer.append(">");
                    String tmp = "$" + currentTableKey + ".C" + (i - 1);
                    //buffer.append(tmp);
                    buffer.append(this.processRowIfNeeded(tmp, false));
                    return false;
                }
            }
        }

        return super.doStartElement(uri, localName, name, attributes);
    }

    @Override
    public void doEndElement(String uri, String localName, String name) throws SAXException {
        if (isTextField(uri, localName)) {
            this.textFieldParsing = false;
            return;
        }
        if (isTable(uri, localName) || isRow(uri, localName)) {
            this.inTargetTable = false;
            i = 0;
        } else if (isColumn(uri, localName)) {
            this.getCurrentElement();
            this.inTargetCol = false;
        }
        super.doEndElement(uri, localName, name);
    }

    @Override
    protected void flushCharacters(String characters) {
        if (this.textFieldParsing) {
            characters = StringUtils.xmlUnescape(characters);
            characters = characters.replace("<", "$");
            characters = characters.replace(">", "");
            //characters = characters.replace(".", "#");

            IDocumentFormatter formatter = getFormatter();
            if (formatter != null
                    && (formatter.containsInterpolation(characters) || formatter.hasDirective(characters))) {
                // It's an interpolation, unescape the XML
                characters = StringUtils.xmlUnescape(characters);
            }
            characters = customFormat(characters, formatter);
            String fieldName = characters;
            if (processScriptBefore(fieldName)) {
                return;
            }
            if (processScriptAfter(fieldName)) {
                return;
            }
            if (getFormatter() != null) {
                FieldMetadata fieldAsTextStyling = getFieldAsTextStyling(fieldName);
                if (fieldAsTextStyling != null) {
                    // register parent buffered element
                    long variableIndex = getVariableIndex();
                    BufferedElement textPElement = getCurrentElement().findParent(TEXT_P);
                    if (textPElement == null) {
                        textPElement = getCurrentElement().getParent();
                    }
                    String elementId = registerBufferedElement(variableIndex, textPElement);

                    // Transform field name if it is inside a table row.
                    // See https://code.google.com/p/xdocreport/issues/detail?id=313
                    String newFieldName = super.processRowIfNeeded(fieldName);
                    if (StringUtils.isEmpty(newFieldName)) {
                        newFieldName = fieldName;
                    }

                    // [#assign
                    // 1327511861250_id=___TextStylingRegistry.transform(comments_odt,"NoEscape","ODT","1327511861250_id",___context)]
                    String setVariableDirective
                            = getFormatter().formatAsCallTextStyling(variableIndex, newFieldName, DocumentKind.ODT.name(),
                                    fieldAsTextStyling.getSyntaxKind(),
                                    fieldAsTextStyling.isSyntaxWithDirective(), elementId,
                                    super.getEntryName());

                    String textBefore
                            = getFormatter().formatAsTextStylingField(variableIndex, ITransformResult.TEXT_BEFORE_PROPERTY);
                    String textBody
                            = getFormatter().formatAsTextStylingField(variableIndex, ITransformResult.TEXT_BODY_PROPERTY);
                    String textEnd
                            = getFormatter().formatAsTextStylingField(variableIndex, ITransformResult.TEXT_END_PROPERTY);

                    textPElement.setContentBeforeStartTagElement(formatDirective(setVariableDirective + textBefore));
                    textPElement.setContentAfterEndTagElement(formatDirective(textEnd));
                    super.flushCharacters(formatDirective(textBody));
                    return;
                } else {
                    // Simple field.
                    characters = formatDirective(characters);
                }
            }
        }
        super.flushCharacters(characters);
    }

    private boolean isTextField(String uri, String localName) {
        return TEXT_NS.equals(uri) && place.equals(localName);
    }

    private boolean isTable(String uri, String localName) {
        return TABLE_NS.equals(uri) && tab.equals(localName);
    }

    private boolean isColumn(String uri, String localName) {
        return TABLE_NS.equals(uri) && col.equals(localName);
    }

    private boolean isParagraph(String uri, String localName) {
        return TEXT_NS.equals(uri) && para.equals(localName);
    }

    private boolean isRow(String uri, String localName) {
        return TABLE_NS.equals(uri) && row.equals(localName);
    }

    private String customFormat(String content, IDocumentFormatter formatter) {
        FieldsMetadata metadata = getFieldsMetadata();
        if (metadata == null) {
            return content;
        }
        String newContent = metadata.customFormat(content, formatter);
        return newContent != null ? newContent : content;
    }
}
