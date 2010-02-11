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
package mpv5.db.objects;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.table.TableModel;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.handler.MPEnum;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.export.Exportable;
import mpv5.utils.export.ODTFile;
import mpv5.utils.export.PDFFile;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.images.MPIcon;
import mpv5.utils.text.RandomText;

/**
 * This class represents Yabs Templates
 *  
 */
public class Template extends DatabaseObject {

    /**
     * The cache of the templates
     */
    public static HashMap<String, Template> templateCache = new HashMap<String, Template>();
    public static HashMap<String, Group> notification = new HashMap<String, Group>();
    /**
     * Indicates no printer association
     */
    public static final String PRINTER_UNDEFINED = "printer_undefined";

    /**
     * 
     * @param dataOwner
     * @return
     */
    public static Template loadTemplate(DatabaseObject dataOwner) {
        Integer type = null;
        if (dataOwner instanceof Item) {
            type = ((Item) dataOwner).__getInttype();
        } else if (dataOwner instanceof Product) {
            type = TYPE_PRODUCT;
        } else if (dataOwner instanceof Reminder) {
            type = TYPE_REMINDER;
        } else if (dataOwner instanceof Contact) {
            type = TYPE_CONTACT;
        }
        String key = mpv5.db.objects.User.getCurrentUser() + "@" + type + "@" + dataOwner.__getGroupsids();
        if (templateCache.containsKey(key)) {
            return templateCache.get(key);
        } else {

            if (type != null) {
                ReturnValue data = QueryHandler.getConnection().freeQuery(
                        "SELECT templatesids FROM templatestousers  LEFT OUTER JOIN templates AS templates0 ON "
                        + "templates0.ids = templatestousers.templatesids WHERE templatestousers.usersids="
                        + mpv5.db.objects.User.getCurrentUser().__getIDS()
                        + " AND "
                        + "templates0.mimetype='" + type
                        + "' AND templatestousers.IDS>0 "
                        + "AND (templates0.groupsids =" + dataOwner.__getGroupsids() + " OR templates0.groupsids =" + 1 + ")", MPSecurityManager.VIEW, null);
                Template preloadedTemplate = null;
                if (data.hasData()) {
                    try {
                        preloadedTemplate = (Template) DatabaseObject.getObject(Context.getTemplate(), Integer.valueOf(data.getData()[data.getData().length - 1][0].toString()));
                        if (preloadedTemplate.getFile().getName().endsWith("odt")) {
                            if (LocalSettings.getBooleanProperty(LocalSettings.OFFICE_USE)) {
                                preloadedTemplate.exFile = new ODTFile(preloadedTemplate.getFile().getPath());
                                Log.Debug(Template.class, "Loaded template: " + preloadedTemplate);
                                MPView.addMessage(preloadedTemplate + Messages.LOADED.toString());
                            } else {
//                                Popup.notice(Messages.NOT_POSSIBLE + "\n" + Messages.OOCONNERROR);
                                return null;
                            }
                        } else {
                            preloadedTemplate.exFile = new PDFFile(preloadedTemplate.getFile().getPath());
                        }
                        templateCache.put(key, preloadedTemplate);
                    } catch (NodataFoundException ex) {
                        Log.Debug(Template.class, "Invalid template: " + data.getData()[data.getData().length - 1][0].toString());
                        return null;
                    }
                } else {
                    try {
                        if (!(notification.containsKey(type) && notification.get(type).equals(Group.getObject(Context.getGroup(), dataOwner.__getGroupsids())))) {
                            MPView.addMessage(Messages.OO_NO_TEMPLATE + ": " + type + " [" + mpv5.db.objects.User.getCurrentUser() + "] [" + Group.getObject(Context.getGroup(), dataOwner.__getGroupsids()) + "]");
                            Log.Debug(Template.class, "No template found for " + type + " for user: " + mpv5.db.objects.User.getCurrentUser() + " in GROUP " + Group.getObject(Context.getGroup(), dataOwner.__getGroupsids()));
                            notification.put(type.toString(), (Group) Group.getObject(Context.getGroup(), dataOwner.__getGroupsids()));
                        }

                    } catch (NodataFoundException nodataFoundException) {
                        Log.Debug(Template.class, nodataFoundException.getMessage());
                    }
                }
                return preloadedTemplate;
            } else {
                return null;
            }
        }
    }

    public static MPEnum[] getTypes() {
        MPEnum[] types = new MPEnum[10];
        types[0] = new MPEnum() {

            public Integer getId() {
                return TYPE_BILL;
            }

            public String getName() {
                return Messages.TYPE_BILL.toString();
            }
        };

        types[1] = new MPEnum() {

            public Integer getId() {
                return TYPE_OFFER;
            }

            public String getName() {
                return Messages.TYPE_OFFER.toString();
            }
        };

        types[2] = new MPEnum() {

            public Integer getId() {
                return TYPE_ORDER;
            }

            public String getName() {
                return Messages.TYPE_ORDER.toString();
            }
        };

        types[3] = new MPEnum() {

            public Integer getId() {
                return TYPE_CONTACT;
            }

            public String getName() {
                return Messages.TYPE_CONTACT.toString();
            }
        };

        types[4] = new MPEnum() {

            public Integer getId() {
                return TYPE_DELIVERY_NOTE;
            }

            public String getName() {
                return Messages.TYPE_DELIVERY.toString();
            }
        };

        types[5] = new MPEnum() {

            public Integer getId() {
                return TYPE_ORDER_CONFIRMATION;
            }

            public String getName() {
                return Messages.TYPE_CONFIRMATION.toString();
            }
        };

        types[6] = new MPEnum() {

            public Integer getId() {
                return TYPE_CONTACT;
            }

            public String getName() {
                return Messages.TYPE_CONTACT.toString();
            }
        };

        types[7] = new MPEnum() {

            public Integer getId() {
                return TYPE_PRODUCT;
            }

            public String getName() {
                return Messages.TYPE_PRODUCT.toString();
            }
        };

        types[8] = new MPEnum() {

            public Integer getId() {
                return TYPE_REMINDER;
            }

            public String getName() {
                return Messages.TYPE_REMINDER.toString();
            }
        };

        types[9] = new MPEnum() {

            public Integer getId() {
                return TYPE_JOURNAL;
            }

            public String getName() {
                return Messages.TYPE_JOURNAL.toString();
            }
        };

        return types;
    }
    public static final int TYPE_BILL = 0;
    public static final int TYPE_ORDER = 1;
    public static final int TYPE_OFFER = 2;
    public static final int TYPE_DELIVERY_NOTE = 3;
    public static final int TYPE_ORDER_CONFIRMATION = 4;
    public static final int TYPE_PRODUCT = 5;
    public static final int TYPE_SERVICE = 6;
    public static final int TYPE_REMINDER = 7;
    public static final int TYPE_CONTACT = 8;
    public static final int TYPE_JOURNAL = 9;
    private String description = "";
    private String filename = "";
    private String printer = "";
    private int intsize;
    private String mimetype;
    private File file;
    private String format = DEFAULT_FORMAT;
    private Exportable exFile;
    /**
     * Represents the default column order
     */
    public static String DEFAULT_FORMAT = "1,2,3,4,5,6,7,8,9,10,11";

    public Template() {
        context = Context.getTemplate();
    }

    @Override
    public String __getCName() {
        return cname;
    }

    @Override
    public JComponent getView() {
        return null;
    }

    @Override
    public void setCName(String name) {
        cname = name;
    }

    /**
     * @return the description
     */
    public String __getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the mimetype
     */
    public String __getMimetype() {
        return mimetype;
    }

    /**
     * @param mimetype the mimetype to set
     */
    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    /**
     * @return the intsize
     */
    public int __getIntsize() {
        return intsize;
    }

    /**
     * @param intsize the intsize to set
     */
    public void setIntsize(int intsize) {
        this.intsize = intsize;
    }
    MPIcon icon;

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        if (icon == null) {
            try {
                Log.Debug(this, "Determining Icon for " + __getCName());
                icon = new MPIcon(MPIcon.DIRECTORY_DEFAULT_ICONS + __getCName().substring(__getCName().lastIndexOf(".") + 1, __getCName().length()) + ".png");
                return icon;
            } catch (Exception e) {
                Log.Debug(this, "Icon file not existing in " + MPIcon.DIRECTORY_DEFAULT_ICONS);
                try {
                    JFileChooser chooser = new JFileChooser();
                    icon = new MPIcon(chooser.getIcon(new File(filename)));
                    return icon;
                } catch (Exception ez) {
                    Log.Debug(this, ez);
                    icon = new MPIcon(MPIcon.DIRECTORY_DEFAULT_ICONS + "folder_tar.png");
                    return icon;
                }
            }
        } else {
            return icon;
        }
    }

    /**
     * Fetches the physical file from db
     * @return
     */
    public synchronized File getFile() {
        if (file == null) {
            try {
                file = QueryHandler.instanceOf().clone(Context.getFiles()).
                        retrieveFile(filename,
                        new File(FileDirectoryHandler.getTempDir() + "~" + RandomText.getText() + "_" + cname));
                file.deleteOnExit();
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
        return file;
    }

    /**
     * @return the filename
     */
    public String __getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public boolean delete() {
        QueryCriteria c = new QueryCriteria();
        c.addAndCondition("filename", filename);
        QueryHandler.instanceOf().clone(Context.getFiles()).delete(c);
        return super.delete();
    }

    /**
     * @return the format
     */
    public String __getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Preload the template files
     */
    public static void cacheTemplates() {
        templateCache.clear();
        List<DatabaseObject> l = new Vector<DatabaseObject>();
        Item it1 = new Item();
        it1.setInttype(Item.TYPE_BILL);
        l.add(it1);

        Item it2 = new Item();
        it2.setInttype(Item.TYPE_OFFER);
        l.add(it2);

        Item it3 = new Item();
        it3.setInttype(Item.TYPE_ORDER);
        l.add(it3);

        Item it4 = new Item();
        it4.setInttype(Item.TYPE_DELIVERY_NOTE);
        l.add(it4);

        Product it5 = new Product();
        it5.setInttype(Product.TYPE_PRODUCT);
        l.add(it5);

        Product it6 = new Product();
        it6.setInttype(Product.TYPE_SERVICE);
        l.add(it6);

        Reminder it7 = new Reminder();
        l.add(it7);

        Contact it8 = new Contact();
        l.add(it8);

        for (int i = 0; i < l.size(); i++) {
            final DatabaseObject databaseObject = l.get(i);
            Runnable runnable = new Runnable() {

                public void run() {
                    Template t = loadTemplate(databaseObject);
                }
            };
            new Thread(runnable).start();
        }
    }

    /**
     * @return the exFile
     */
    public Exportable getExFile() {
        return exFile;
    }

    /**
     * Injects a table resource
     * @param key
     * @param model
     */
    public void injectTable(String key, TableModel model) {
        getTables().put(key, model);
    }
    private HashMap<String, TableModel> tables = new HashMap<String, TableModel>();

    /**
     * @return the tables
     */
    public HashMap<String, TableModel> getTables() {
        return tables;
    }

    /**
     * @return the printer
     */
    public String __getPrinter() {
        return printer;
    }

    /**
     * @param printer the printer to set
     */
    public void setPrinter(String printer) {
        this.printer = printer;
    }
}
