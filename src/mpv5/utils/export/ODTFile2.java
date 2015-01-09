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
import enoa.handler.TableHandler;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.odt.ODTConstants;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.itext.extension.font.ITextFontRegistry;
import fr.opensagres.xdocreport.template.FieldExtractor;
import fr.opensagres.xdocreport.template.FieldsExtractor;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import mpv5.globals.GlobalSettings;
import mpv5.logging.Log;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.xdocreport.YabsFontFactoryImpl;
import mpv5.utils.xdocreport.YabsODTPreprocessor;
import org.odftoolkit.odfdom.converter.core.ODFConverterException;
import org.odftoolkit.odfdom.converter.pdf.PdfConverter;
import org.odftoolkit.odfdom.converter.pdf.PdfOptions;
import org.odftoolkit.odfdom.doc.OdfTextDocument;

/**
 *
 *
 */
public class ODTFile2 extends Exportable {

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
        FontFactory.setFontImp(YabsFontFactoryImpl.instance);
        ITextFontRegistry reg = ITextFontRegistry.getRegistry();
        options = PdfOptions.create();
        options.fontProvider(reg);
        report = XDocReportRegistry.getRegistry().loadReport(new FileInputStream(this), TemplateEngineKind.Velocity);
        extractor = FieldsExtractor.create();
        report.addPreprocessor(ODTConstants.CONTENT_XML_ENTRY, YabsODTPreprocessor.INSTANCE);
        report.addPreprocessor(ODTConstants.STYLES_XML_ENTRY, YabsODTPreprocessor.INSTANCE);    
    }

    @Override
    public void run() {
        try {
            Log.Debug(this, "run odt run: " + this);
            mpv5.YabsViewProxy.instance().setWaiting(true);
            if (getTarget().getAbsolutePath().endsWith(".pdf")) {
                outtmp = new FileOutputStream(f = FileDirectoryHandler.getTempFile("odt"));
                fillFields(outtmp);
                Log.Debug(this, "Replaced Fields of odt file: " + this + " to " + f);
                document = OdfTextDocument.loadDocument(f);
                Log.Debug(this, "Loaded odt file: " + f);
                PdfConverter.getInstance().convert(document, new FileOutputStream(getTarget()), options);
                Log.Debug(this, "Completed pdf file: " + getTarget());
            } else {
                fillFields(new FileOutputStream(getTarget()));
                Log.Debug(this, "Replaced Fields of odt file: " + this + " to " + getTarget());
            }

        } catch (IOException ex) {
            Log.Debug(ex);
        } catch (ODFConverterException ex) {
            Log.Debug(ex);
        } catch (Exception ex) {
            Log.Debug(ex);
        } finally {
            mpv5.YabsViewProxy.instance().setWaiting(false);
        }
    }

    private void fillFields(OutputStream out) throws IOException {
        try {
            FieldsMetadata metadata = new FieldsMetadata();
            boolean addMeta = true;

            HashMap<String, Object> d = getData();
            d.putAll(getTemplate().getData());
          
            IContext context = report.createContext();
            if (Log.isDebugging()) {
                Log.Debug(this, "All fields:");
                for (String k : d.keySet()) {
                    Log.Debug(this, "Key: " + k + " [" + d.get(k) + "]");
                }
            }

            Object table = null;
            for (String k : d.keySet()) {
                if (k.contains(TableHandler.KEY_TABLE + "1")) {
                    table = k;
                    break;
                }
            }
            if (table != null) {
                String fmt = this.getTemplate().__getFormat();
                String[] cols = fmt.split(",");
                ArrayList<String[]> list = (ArrayList<String[]>) d.get(table);
                List<Map<String, String>> positions = new ArrayList<Map<String, String>>();
                for (String[] s : list) {
                    int i = 0;
                    Map<String, String> xtable = new HashMap<String, String>();
                    for (String s1 : cols) {
                        int col = Integer.parseInt(s1) - 1;
                        String colname = "C" + i++;
                        xtable.put(colname, s[col]);
                        if (addMeta) {
                            metadata.addFieldAsList(TableHandler.KEY_TABLE + "1." + colname);
                        }
                    }
//                    for (String s2 : s) {
//                        String colname = "C" + i++;
//                        xtable.put(colname, s2);
//                        if (addMeta)
//                            metadata.addFieldAsList(TableHandler.KEY_TABLE + "1." + colname);
//                    }
                    positions.add(xtable);
                    addMeta = false;
                }
                context.put(TableHandler.KEY_TABLE + "1", positions);
            }
            report.setFieldsMetadata(metadata);
            
            report.extractFields(extractor);
            
            if (!GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.blankunusedfields.disable")) {

                Iterator<FieldExtractor> it = extractor.getFields().iterator();
                while (it.hasNext()) {
                    FieldExtractor val = it.next();
                    if (!val.getName().startsWith("{")
                            && !d.containsKey(val.getName())) {
                        if (!val.getName().startsWith(TableHandler.KEY_TABLE + "1")) {
                            d.put(val.getName(), "");
                        }
                    }
                }
            }
            context.putMap(d);
                  
            report.process(context, out);

        } catch (XDocReportException ex) {
            Log.Debug(ex);
        } catch (NoClassDefFoundError ex2) {
            Log.Debug(ex2);
        }
    }
}
