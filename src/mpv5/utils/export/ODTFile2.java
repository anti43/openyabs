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

import com.lowagie.text.FontFactory;
import mpv5.utils.images.YabsQRCodeGenerator;

import fr.opensagres.odfdom.converter.core.ODFConverterException;
import fr.opensagres.odfdom.converter.pdf.PdfConverter;
import fr.opensagres.odfdom.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.core.document.SyntaxKind;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.odt.ODTConstants;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.itext.extension.font.ITextFontRegistry;
import fr.opensagres.xdocreport.template.FieldExtractor;
import fr.opensagres.xdocreport.template.FieldsExtractor;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import fr.opensagres.xdocreport.template.formatter.NullImageBehaviour;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.MinimalHTMLWriter;
import javax.swing.text.rtf.RTFEditorKit;

import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Conversation;
import mpv5.db.objects.Item;
import mpv5.globals.GlobalSettings;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Notificator;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.xdocreport.YabsFontFactoryImpl;
import mpv5.utils.xdocreport.YabsODTPreprocessor;
//import org.odftoolkit.odfdom.converter.core.ODFConverterException;
//import org.odftoolkit.odfdom.converter.pdf.PdfConverter;
//import org.odftoolkit.odfdom.converter.pdf.PdfOptions;
import org.odftoolkit.odfdom.doc.OdfTextDocument;

/**
 * New templating system
 */
public class ODTFile2 extends Exportable {

    public static final String KEY_TABLE = "xtable";
    private static final long serialVersionUID = 1L;
    private File f = null;
    private FileOutputStream outtmp;
    private OdfTextDocument document;
    private final PdfOptions options;
    private final IXDocReport report;
    private final FieldsExtractor<FieldExtractor> extractor;

    public ODTFile2(String pathToFile) throws Exception {
        super(pathToFile);
        if (!exists()) {
            try {
                createNewFile();

            } catch (IOException ex) {
                Log.Debug(ex);
            }
        }
        if (GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.cachefonts", false)) {
            FontFactory.setFontImp(YabsFontFactoryImpl.instance);
        }
        ITextFontRegistry reg = ITextFontRegistry.getRegistry();
        options = PdfOptions.create();
        options.fontProvider(reg);
        report = XDocReportRegistry.getRegistry().loadReport(new FileInputStream(this), TemplateEngineKind.Velocity);
        extractor = FieldsExtractor.create();
        report.removePreprocessor(ODTConstants.CONTENT_XML_ENTRY);
        report.addPreprocessor(ODTConstants.CONTENT_XML_ENTRY, YabsODTPreprocessor.INSTANCE);
        report.addPreprocessor(ODTConstants.STYLES_XML_ENTRY, YabsODTPreprocessor.INSTANCE);
    }

    @Override
    public void run() {
        try {
            final File target = getTarget();
            Log.Debug(this, "run odt run: " + this + " to file " + target);
            mpv5.YabsViewProxy.instance().setWaiting(true);
            if (target.getAbsolutePath().endsWith(".pdf")) {
                outtmp = new FileOutputStream(f = FileDirectoryHandler.getTempFile("odt"));
                fillFields(outtmp);
                Log.Debug(this, "Replaced Fields of odt file: " + this + " to " + f);
                document = OdfTextDocument.loadDocument(f);
                Log.Debug(this, "Loaded odt file: " + f);
                PdfConverter.getInstance().convert(document, new FileOutputStream(target), options);
                Log.Debug(this, "Completed pdf file: " + target);
                Log.Debug(this, "Test access pdf file exists: " + target.exists() + " readable: " + target.canRead());
            } else {
                fillFields(new FileOutputStream(target));
                Log.Debug(this, "Replaced Fields of odt file: " + this + " to " + target);
            }
        } catch (Exception ex) {
            Log.Debug(ex);
        } finally {
            mpv5.YabsViewProxy.instance().setWaiting(false);
        }
    }

    private void fillFields(OutputStream out) throws IOException {
        try {
            FieldsMetadata metadata = new FieldsMetadata();


            HashMap<String, Object> templateData = getData();
            templateData.putAll(getTemplate().getData());
            templateData.putAll(getTemplate().getTablesAsMap());

            IContext context = report.createContext();
            if (Log.isDebugging()) {
                Log.Debug(this, "All fields:");
                for (String k : templateData.keySet()) {
                    Log.Debug(this, "Key: " + k + " [" + templateData.get(k) + "]");
                }
            }

            String fmt = this.getTemplate().__getFormat();
            String[] cols = fmt.split(",");

            //I wanted to do this fully dynamic but then the xdocreport shows only keys not values, no idea why
            //https://github.com/opensagres/xdocreport/issues/111
            Object tablel = templateData.remove("item.xtable1");
            if (tablel != null) {
                filltable(1, cols, tablel, metadata, context);
            }
            Object table2 = templateData.remove("item.reference.xtable1");
            if (table2 != null) {
                filltable(2, cols, table2, metadata, context);
            }

            DatabaseObject dob = ((Export) templateData).getDob();

            if (dob instanceof Item &&
                    (((Item) dob).__getInttype() == Item.TYPE_INVOICE
                            || ((Item) dob).__getInttype() == Item.TYPE_PART_PAYMENT
                            || ((Item) dob).__getInttype() == Item.TYPE_DEPOSIT)) {
                YabsQRCodeGenerator QRGen = new YabsQRCodeGenerator(dob);
                QRGen.generate();
                context.put("QRCode.img", QRGen.getImageProvider());
                metadata.addFieldAsImage("QRCode.img");
                metadata.addFieldAsImage("imageNotExistsAndRemoveImageTemplate", NullImageBehaviour.RemoveImageTemplate);
                metadata.addFieldAsImage("imageNotExistsAndKeepImageTemplate", NullImageBehaviour.KeepImageTemplate);
            }

            report.setFieldsMetadata(metadata);
            report.extractFields(extractor);

            boolean doBlank = !GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.blankunusedfields.disable");

            for (FieldExtractor val : extractor.getFields()) {
                String key = val.getName();

                if (!val.getName().startsWith("{")
                        && !val.getName().endsWith(".img")
                        && !val.getName().startsWith(KEY_TABLE)) {
                    Object value = templateData.get(key);
                    if (value == null && doBlank) {
                        value = "";
                    }
                    Log.Debug(this, "Found value: " + String.valueOf(value) + " for key: " + key);
                    context.put(key, value);
                } else {
                    Log.Debug(this, "Ignoring key: " + key);
                }

            }

            report.process(context, out);

        } catch (XDocReportException | NoClassDefFoundError ex) {
            Log.Debug(ex);
        }
    }

    private void filltable(int index, String[] cols, Object tablel, FieldsMetadata metadata, IContext context) {
        boolean addMeta = true;
        int ix = 0;
        if (tablel instanceof List) {
            List list = (List) tablel;
            List<Map<String, String>> positions = new ArrayList<Map<String, String>>();
            for (Object s : list) {
                ix++;
                String[] tableData = (String[]) s;
                Log.Debug(this, "Table " + index + " row " + ix + ": " + Arrays.asList(tableData));
                int i = 0;
                Map<String, String> xtable = new HashMap<String, String>();
                for (String s1 : cols) {
                    int col = Integer.valueOf(s1) - 1;

                    if (tableData.length > col) {
                        String colname = "C" + i++;
                        xtable.put(colname, tableData[col]);
                        if (addMeta) {
                            metadata.addFieldAsList(KEY_TABLE + index + "." + colname);
                            //metadata.addField(KEY_TABLE + index + "." + colname, true, null, SyntaxKind.Html.name(), false);
                        }
                    } else {
                        Notificator.raiseNotification("Invalid column definition in " + this.getTemplate().getCname() + ": " + col + ">" + (tableData.length - 1) + ")", false);
                    }
                }
                /*for (int j = i; j < i + 10; j++) {
                    String colname = "C" + j;
                    xtable.put(colname, "");//fill other cols
                    if (addMeta) {
                        metadata.addFieldAsList(KEY_TABLE + index + "." + colname);
                    }
                }*/

                positions.add(xtable);
                addMeta = false;
            }
            context.put(KEY_TABLE + index, positions);
        } else {
            Log.Debug(new Exception(String.valueOf(tablel.getClass())));
        }
    }
}
