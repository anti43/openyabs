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

import mpv5.utils.xdocreport.YabsFontFactoryImpl;
import mpv5.utils.xdocreport.YabsODTPreprocessor;
import com.lowagie.text.FontFactory;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.odt.ODTConstants;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.itext.extension.font.ITextFontRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import mpv5.logging.Log;
import mpv5.utils.files.FileDirectoryHandler;
import org.odftoolkit.odfdom.converter.pdf.PdfConverter;
import org.odftoolkit.odfdom.converter.pdf.PdfOptions;
import org.odftoolkit.odfdom.doc.OdfTextDocument;

/**
 *
 *
 */
public class ODTFile2 extends Exportable {

    private static final long serialVersionUID = 1L;

    public ODTFile2(String pathToFile) throws Exception {
        super(pathToFile);
        if (!exists()) {
            try {
                createNewFile();
            } catch (IOException ex) {
                Log.Debug(ex);
            }
        }
    }

    @Override
    public void run() {
        try {
            Log.Debug(this, "run odt run: " + this);
            mpv5.YabsViewProxy.instance().setWaiting(true);
            File f;
            FileOutputStream outtmp = new FileOutputStream(f = FileDirectoryHandler.getTempFile("odt"));
            if (getTarget().getAbsolutePath().endsWith(".pdf")) {
                fillFields(new FileInputStream(this), outtmp);
                Log.Debug(this, "Replaced Fields of odt file: " + this + " to " + f);
                OdfTextDocument document
                        = OdfTextDocument.loadDocument(f);
                Log.Debug(this, "Loaded odt file: " + f);
                YabsFontFactoryImpl yffi = new YabsFontFactoryImpl();
                FontFactory.setFontImp(yffi);
                PdfOptions options = PdfOptions.create();
                ITextFontRegistry reg = ITextFontRegistry.getRegistry();
                options.fontProvider(reg);
                PdfConverter.getInstance().convert(document, new FileOutputStream(getTarget()), options);
                Log.Debug(this, "Completed pdf file: " + getTarget());
                f.deleteOnExit();
                this.deleteOnExit();
            } else {
                fillFields(new FileInputStream(this), new FileOutputStream(getTarget()));
                Log.Debug(this, "Replaced Fields of odt file: " + this + " to " + getTarget());
            }

        } catch (Exception ex) {
            Log.Debug(ex);
        } finally {
            mpv5.YabsViewProxy.instance().setWaiting(false);
        }
    }

    private void fillFields(InputStream in, OutputStream out) throws IOException, XDocReportException {
        IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Velocity);
        report.addPreprocessor(ODTConstants.CONTENT_XML_ENTRY, YabsODTPreprocessor.INSTANCE);
        report.addPreprocessor(ODTConstants.STYLES_XML_ENTRY, YabsODTPreprocessor.INSTANCE);

        IContext context = report.createContext();
        if (Log.isDebugging()) {
            Log.Debug(this, "All fields:");
            for (String k : getData().keySet()) {
                Log.Debug(this, "Key: " + k + " [" + getData().get(k) + "]");
            }
        }
        HashMap<String, Object> d = getData();
        d.putAll(getTemplate().getData());
        context.putMap(d);

        report.process(context, out);
    }
}
