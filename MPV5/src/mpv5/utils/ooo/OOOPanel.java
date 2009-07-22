package mpv5.utils.ooo;

import ag.ion.bion.officelayer.document.DocumentException;
import java.awt.BorderLayout;
import java.util.HashMap;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Frame;
import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.application.OfficeApplicationRuntime;
import ag.ion.bion.officelayer.desktop.GlobalCommands;
import ag.ion.bion.officelayer.desktop.IFrame;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.form.IFormComponent;

import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.noa.frame.IDispatchDelegate;
import com.sun.star.awt.XTextComponent;
import com.sun.star.beans.XPropertySet;
import com.sun.star.form.XFormComponent;
import com.sun.star.uno.UnoRuntime;
import java.awt.Color;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Hashtable;

import javax.swing.JFrame;
import mpv5.db.common.Context;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.SaveString;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.logging.LogConsole;
import mpv5.ui.frames.MPView;
import mpv5.utils.reflection.ClasspathTools;

public class OOOPanel extends JPanel {

    private String oootype;
    private String ooohome;
    private String ooohost;
    private String oooport;
    private IOfficeApplication officeApplication;
    private IFrame officeFrame;

    public OOOPanel() {
        super(new BorderLayout());
        String officeVal = LocalSettings.getProperty(LocalSettings.OFFICE_HOME);
        String[] officeVals = officeVal.split(":");
        try {
            oootype = officeVals[0];
            ooohome = officeVals[1];
            ooohost = officeVals[2];
            oooport = officeVals[3];
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            ClasspathTools.addPath(officeVals[1]);
            ClasspathTools.addPath(officeVals[1] + "program");
        } catch (java.lang.Exception exception) {
        }
    }

    /**
     * Loads the given template into this panel
     * @param odtFile
     */
    public void constructOOOPanel(final File odtFile) {
        MPView.addMessage(Messages.LOADING_OOO);
        MPView.setWaiting(true);

        ITextDocument textDocument;
        setVisible(true);

        try {
            HashMap<String, String> configuration = new HashMap<String, String>();
            if (oootype.equalsIgnoreCase(IOfficeApplication.LOCAL_APPLICATION)) {
                configuration.put(IOfficeApplication.APPLICATION_HOME_KEY, ooohome);
                configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY, IOfficeApplication.LOCAL_APPLICATION);
            } else if (oootype.equalsIgnoreCase(IOfficeApplication.REMOTE_APPLICATION)) {
                configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY, IOfficeApplication.REMOTE_APPLICATION);
                configuration.put(IOfficeApplication.APPLICATION_HOST_KEY, ooohost); //IP des anderen PCs
                configuration.put(IOfficeApplication.APPLICATION_PORT_KEY, oooport);
                configuration.put(IOfficeApplication.APPLICATION_HOME_KEY, ooohome);
            } else {
                throw new Exception("OpenOffice installation not configured!");
            }

            officeApplication = OfficeApplicationRuntime.getApplication(configuration);
            officeApplication.setConfiguration(configuration);
            officeApplication.activate();

            mpv5.Main.addOfficeApplicationToClose(officeApplication);

            officeFrame = officeApplication.getDesktopService().constructNewOfficeFrame(this);
            officeFrame.setFocus();
            if (odtFile != null) {
                IDocument document = officeApplication.getDocumentService().loadDocument(officeFrame, odtFile.getPath());
                textDocument = (ITextDocument) document;
            } else {
                officeApplication.getDocumentService().constructNewDocument(officeFrame, IDocument.WRITER, DocumentDescriptor.DEFAULT);
            }

            configureFrame();

        } catch (Exception ex) {
            Log.Debug(ex);
        } finally {
            getParent().validate();
            MPView.setWaiting(false);
        }
        MPView.addMessage(Messages.DONE_LOADING_OOO);
    }

    /**
     * Configures the given IFrame to print the closed document's path to System.out on close<br/>
     * and removes the Close and Quit capabilites of the frame
     */
    public void configureFrame() {

        officeFrame.addDispatchDelegate(GlobalCommands.SAVE, new IDispatchDelegate() {

            @Override
            public void dispatch(Object[] arg0) {
                try {
                    try {
                        QueryHandler.instanceOf().clone(Context.getFiles()).insertFile(new File(officeApplication.getDocumentService().getCurrentDocuments()[0].getPersistenceService().getLocation().getPath()),
                                MPView.getUser(), new SaveString("Template", true));
                    } catch (OfficeApplicationException ex) {
                        Log.Debug(ex);
                    }
                } catch (DocumentException ex) {
                    Log.Debug(ex);
                }
            }
        });

        officeFrame.addDispatchDelegate(GlobalCommands.OPEN_DOCUMENT, new IDispatchDelegate() {

            @Override
            public void dispatch(Object[] arg0) {
//                try {
//
////                    officeApplication.getDocumentService().loadDocument(officeFrame, officeFrame.);
//                } catch (OfficeApplicationException ex) {
//                    Log.Debug(ex);
//                }
            }
        });

        officeFrame.updateDispatches();
        officeFrame.disableDispatch(GlobalCommands.CLOSE_DOCUMENT);
        officeFrame.disableDispatch(GlobalCommands.QUIT_APPLICATION);
        officeFrame.disableDispatch(GlobalCommands.OPEN_DOCUMENT);
    }

    /**
     * Fill the form fields of the template with values
     * @param textDocument
     * @param template
     * @param data
     * @throws java.lang.Exception
     */
    public void fillFields(ITextDocument textDocument, File template, Hashtable<String, String> data) throws Exception {

        IFormComponent[] formComponents = textDocument.getFormService().getFormComponents();
        // iterate over hashtable and insert values into field masters
        java.util.Enumeration keys = data.keys();
        String key = null;
        while (keys.hasMoreElements()) {

            try {
                // get column name
                key = (String) keys.nextElement();

                for (int i = 0; i < formComponents.length; i++) {

                    XFormComponent xFormComponent = formComponents[i].getXFormComponent();
                    XTextComponent xTextComponent = formComponents[i].getXTextComponent();
                    XPropertySet propertySet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class,
                            xFormComponent);

                    if (propertySet != null && propertySet.getPropertySetInfo().hasPropertyByName("Name")) {
                        String name = propertySet.getPropertyValue("Name").toString();
                        if (name.equalsIgnoreCase(key)) {
                            xTextComponent.setText(data.get(key));
                        }
                    }
                }
            } catch (Exception noSuchElementException) {
                System.err.println(noSuchElementException.getMessage());
            }
        }
    }
}
