package mpv5.ui.panels;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PagePanel;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import javax.swing.JEditorPane;

import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.Formattable;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.MailMessage;
import mpv5.globals.Messages;
import mpv5.handler.VariablesHandler;
import mpv5.logging.Log;
import mpv5.mail.SimpleMail;
import mpv5.ui.dialogs.BigPopup;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.export.Export;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileReaderWriter;
import mpv5.utils.jobs.Waiter;
import mpv5.utils.print.PrintJob;

/**
 *
 *
 */
public class PreviewWaiter implements Waiter {

    private static final long serialVersionUID = 2L;
    private File file;
    private DatabaseObject dataOwner;
    private DataPanel parent;

    /**
     * Creates new form
     */
    public PreviewWaiter() {
    }

    /**
     * Creates new preview for the given file. Currently supported file types:
     * <li>PDF
     * <li>ODT
     * <li>TXT
     *
     * @param file
     */
    public PreviewWaiter(File file) {
        openl(file);
    }


    /**
     * @return the dataOwner
     */
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    /**
     * @param dataOwner the dataOwner to set
     */
    public void setDataOwner(DatabaseObject dataOwner) {
        this.dataOwner = dataOwner;
    }

    @Override
    public void set(Object object, Exception exception) throws Exception {
        if (exception == null) {
            try {
                if (object instanceof List) {
                    openl(((Export) ((List) object).get(0)).getTargetFile());
                } else if (object instanceof Export) {
                    openl(((Export) object).getTargetFile());
                } else {
                    openl((File) object);
                }
            } catch (Exception e) {
                throw e;
            } finally {
            }
        } else {
            throw exception;
        }
    }

    private void openl(File file) {
        if (file.getName().split("\\.").length < 2) {
            throw new UnsupportedOperationException("The file must have an extension: " + file);
        }

        this.file = file;

        String extension = file.getName().substring(file.getName().lastIndexOf("."), file.getName().length());

        Log.Debug(this, "Found extension: " + extension);

        if (extension.equalsIgnoreCase(".pdf")) {
            try {
                FileDirectoryHandler.open(file);
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        } else if (extension.equalsIgnoreCase(".odt")) {
            try {
                FileDirectoryHandler.open(file);

            } catch (Exception ex) {
                Log.Debug(ex);
            }
        } else if (extension.equalsIgnoreCase(".txt")) {
            try {
                FileDirectoryHandler.open(file);
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        } else {
            FileDirectoryHandler.open(file);

        }
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(DataPanel parent) {
        this.parent = parent;
    }

}
