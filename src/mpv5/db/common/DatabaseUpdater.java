package mpv5.db.common;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import mpv5.db.objects.Template;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;

/**
 * This class provides database updates
 * @author anti
 */
public class DatabaseUpdater {

    private Map<Double, String[]> UPDATES_DERBY = new TreeMap<Double, String[]>();
    private Map<Double, String[]> UPDATES_MYSQL = new TreeMap<Double, String[]>();

    public DatabaseUpdater() {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // derby updates
        UPDATES_DERBY.put(1.11, new String[]{
                    "ALTER TABLE products ADD COLUMN stockvalue DOUBLE DEFAULT 0 NOT NULL ",
                    "ALTER TABLE products ADD COLUMN thresholdvalue DOUBLE DEFAULT 0 NOT NULL",
                    "ALTER TABLE products ADD COLUMN intinventorytype SMALLINT DEFAULT 0 NOT NULL"
                });
        UPDATES_DERBY.put(1.12, new String[]{
                    "ALTER TABLE templates ADD COLUMN printer VARCHAR(50) DEFAULT '" + Template.PRINTER_UNDEFINED + "' NOT NULL ",});
        UPDATES_DERBY.put(1.14, new String[]{
                    "ALTER TABLE products DROP CONSTRAINT const8",});
        UPDATES_DERBY.put(1.15, new String[]{
                    //            intaddedby,dateadded,productsids,contactsids,groupsids,cname
                    "CREATE TABLE productstosuppliers(ids BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1), "
                    + "productsids BIGINT REFERENCES products(ids) ON DELETE CASCADE, "
                    + "contactsids BIGINT REFERENCES contacts(ids) ON DELETE CASCADE,"
                    + "cname VARCHAR(250) DEFAULT NULL,"
                    + "groupsids BIGINT DEFAULT 0,"
                    + "dateadded DATE NOT NULL,"
                    + "intaddedby BIGINT DEFAULT 0)",});
        UPDATES_DERBY.put(1.16, new String[]{
                    "ALTER TABLE products ADD COLUMN productlistsids BIGINT DEFAULT 0",});
        UPDATES_DERBY.put(1.17, new String[]{
                    "ALTER TABLE contacts ADD COLUMN bankaccount VARCHAR(250) DEFAULT NULL",
                    "ALTER TABLE contacts ADD COLUMN bankid VARCHAR(250) DEFAULT NULL",
                    "ALTER TABLE contacts ADD COLUMN bankname VARCHAR(250) DEFAULT NULL",
                    "ALTER TABLE contacts ADD COLUMN bankcurrency VARCHAR(250) DEFAULT NULL",
                    "ALTER TABLE contacts ADD COLUMN bankcountry VARCHAR(250) DEFAULT NULL",});
        UPDATES_DERBY.put(1.181, new String[]{
//                    "DROP TABLE valueproperties"
                });
        UPDATES_DERBY.put(1.182, new String[]{
                    //                    "ALTER TABLE items DROP COLUMN discountvalue",
                    "CREATE INDEX items_index0 ON items(cnumber)",
                    "CREATE INDEX items_index1 ON items(cname)",
                    "CREATE INDEX products_index0 ON products(cnumber)",
                    "CREATE INDEX products_index1 ON products(cname)",
                    "CREATE INDEX contacts_index0 ON contacts(cname)",
                    "CREATE TABLE valueproperties (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                    + "CONSTRAINT constvp0 UNIQUE (cname, contextids, objectids, groupsids),"
                    + "cname VARCHAR(250) NOT NULL, classname VARCHAR(250) NOT NULL, "
                    + "contextids BIGINT NOT NULL, objectids BIGINT NOT NULL,"
                    + "value CLOB(2G) DEFAULT NULL, dateadded DATE NOT NULL, intaddedby BIGINT DEFAULT 0, "
                    + "groupsids BIGINT REFERENCES groups(ids) DEFAULT 1, invisible SMALLINT DEFAULT 0, "
                    + "PRIMARY KEY (ids))",
                    "CREATE INDEX values_index0 ON valueproperties(cname, contextids, objectids)",
                    "ALTER TABLE expenses ADD COLUMN dateend DATE ",
                    "ALTER TABLE expenses ADD COLUMN ispaid SMALLINT DEFAULT 0 NOT NULL",
                    "ALTER TABLE revenues ADD COLUMN dateend DATE ",
                    "ALTER TABLE revenues ADD COLUMN ispaid SMALLINT DEFAULT 0 NOT NULL",});
        UPDATES_DERBY.put(1.183, new String[]{
                    "ALTER TABLE stages ADD COLUMN templategroup BIGINT REFERENCES groups(ids)"
                });
        UPDATES_DERBY.put(1.1841, new String[]{
                    "ALTER TABLE subitems ADD COLUMN ordernr SMALLINT DEFAULT 0 NOT NULL"
                });
        UPDATES_DERBY.put(1.1842, new String[]{
                    "ALTER TABLE valueproperties DROP COLUMN value",
                    "ALTER TABLE valueproperties ADD COLUMN value BLOB(2G) DEFAULT NULL"
                });
        UPDATES_DERBY.put(1.1843, new String[]{
                    "ALTER TABLE contacts DROP CONSTRAINT const3",});
        UPDATES_DERBY.put(1.185, new String[]{
                    "CREATE TABLE productprices (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                    + "cname VARCHAR(250) NOT NULL, "
                    + "productsids BIGINT REFERENCES products(ids) ON DELETE CASCADE, "
                    + "externalnetvalue DOUBLE DEFAULT 0, "
                    + "internalnetvalue DOUBLE DEFAULT 0, "
                    + "mincountvalue DOUBLE DEFAULT 0, "
                    + "dateadded DATE NOT NULL, intaddedby BIGINT DEFAULT 0, "
                    + "groupsids BIGINT REFERENCES groups(ids) DEFAULT 1, invisible SMALLINT DEFAULT 0, "
                    + "PRIMARY KEY (ids))"
                });
        UPDATES_DERBY.put(1.186, new String[]{
                    "CREATE TABLE scheduletypes (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "cname VARCHAR(250) NOT NULL,"
                    + "groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,"
                    + "usersids BIGINT REFERENCES users (ids)  ON DELETE CASCADE,"
                    + "dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0,"
                    + "invisible SMALLINT DEFAULT 0,"
                    + "PRIMARY KEY  (ids))",
                    "INSERT INTO scheduletypes (cname, groupsids, usersids, dateadded, invisible) VALUES ('Call',1,1,'2011-07-29',0)",
                    "INSERT INTO scheduletypes (cname, groupsids, usersids, dateadded, invisible) VALUES ('Meeting',1,1,'2011-07-29',0)",
                    "INSERT INTO scheduletypes (cname, groupsids, usersids, dateadded, invisible) VALUES ('Mail',1,1,'2011-07-29',0)",
                    "INSERT INTO scheduletypes (cname, groupsids, usersids, dateadded, invisible) VALUES ('ToDo',1,1,'2011-07-29',0)",
                    "ALTER TABLE schedule ADD COLUMN contactsids BIGINT REFERENCES contacts (ids) ON DELETE CASCADE",
                    "ALTER TABLE schedule ADD COLUMN eventtype BIGINT REFERENCES scheduletypes (ids) ON DELETE CASCADE"});
        UPDATES_DERBY.put(1.187, new String[]{
                    "CREATE TABLE conversations "
                    + "(IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                    + "cname VARCHAR(2500), "
                    + "adress VARCHAR(2500), "
                    + "cnumber VARCHAR(6), "
                    + "groupsids BIGINT DEFAULT 1,"
                    + "date DATE NOT NULL, "
                    + "dateadded DATE NOT NULL,"
                    + "intaddedby BIGINT DEFAULT 0, "
                    + "invisible SMALLINT DEFAULT 0, "
                    + "content LONG VARCHAR NOT NULL, "
                    + "PRIMARY KEY  (ids))"});    
        UPDATES_DERBY.put(1.188, new String[]{
                    "ALTER TABLE subitems ADD COLUMN inttype SMALLINT DEFAULT 0 NOT NULL"
                });
        UPDATES_DERBY.put(1.189, new String[]{
                    "ALTER TABLE conversations ADD COLUMN contactsids BIGINT REFERENCES contacts (ids) ON DELETE CASCADE"
                });
        UPDATES_DERBY.put(1.190, new String[]{
                    "CREATE TABLE activitylists (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                    + " cname VARCHAR(200) DEFAULT NULL,"
                    + " cnumber VARCHAR(250) NOT NULL,"
                    + " groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,"
                    + " contactsids BIGINT  REFERENCES contacts(ids) DEFAULT 0,"
                    + " orderids BIGINT  REFERENCES items(ids) DEFAULT 0,"
                    + " totalamount DOUBLE DEFAULT 0,"
                    + " dateadded DATE NOT NULL,"
                    + " intaddedby BIGINT DEFAULT 0,"
                    + " invisible SMALLINT DEFAULT 0,"
                    + " PRIMARY KEY (ids))",
                    //"Date", "Count", "Description", "Netto Price", "Tax Value", "Total Price", "Product", "cname"     
                    "CREATE TABLE activitylistitems (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                    + " cname VARCHAR(20) DEFAULT NULL,"
                    + " groupsids BIGINT REFERENCES groups(ids) DEFAULT 1,"
                    + " activitylistsids BIGINT REFERENCES activitylists(ids) ON DELETE CASCADE,"
                    + " datedoing DATE NOT NULL,"
                    + " quantityvalue DOUBLE DEFAULT 0 NOT NULL,"
                    + " measure VARCHAR(25) NOT NULL,"
                    + " description VARCHAR(1000) DEFAULT NULL,"                
                    + " internalvalue DOUBLE DEFAULT 0,"
                    + " taxpercentvalue DOUBLE DEFAULT 0 NOT NULL,"
                    + " totalbrutvalue DOUBLE DEFAULT 0,"
                    + " productsids BIGINT REFERENCES products(ids) ON DELETE CASCADE,"
                    + " dateadded DATE NOT NULL,"
                    + " intaddedby BIGINT DEFAULT 0,"
                    + " invisible SMALLINT DEFAULT 0,"
                    + " PRIMARY KEY (ids))"
                });    
        UPDATES_DERBY.put(1.191, new String[]{
                    "ALTER TABLE templates ADD COLUMN isupdateenabled SMALLINT DEFAULT 0 NOT NULL",
                    "ALTER TABLE templates ADD COLUMN lastmodified BIGINT DEFAULT NULL",
                    "ALTER TABLE templates ADD COLUMN pathtofile VARCHAR(2500) DEFAULT NULL"
                });
        UPDATES_DERBY.put(1.192, new String[]{
                    "ALTER TABLE subitems ADD COLUMN discount DOUBLE DEFAULT 0 NOT NULL"
                });    
        UPDATES_DERBY.put(1.193, new String[]{
                    "CREATE TABLE massprintrules (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + " cname VARCHAR(250) NOT NULL,"
                    + " inttype SMALLINT DEFAULT 0,"
                    + " content LONG VARCHAR NOT NULL,"
                    + " dateadded DATE NOT NULL,"
                    + " invisible SMALLINT DEFAULT 0,"
                    + " intaddedby BIGINT DEFAULT 0,"
                    + " groupsids BIGINT REFERENCES groups(ids) DEFAULT 1,"
                    + "PRIMARY KEY  (ids))",
                    "INSERT INTO massprintrules (cname, inttype, content, dateadded, invisible, groupsids) VALUES ('Open Bills',0, 'Select * from contacts Inner Join items on (Items.contactsids = contacts.ids) where items.intstatus = 4 and items.inttype =3', '2011-07-29',0, 1)",
                    "INSERT INTO massprintrules (cname, inttype, content, dateadded, invisible, groupsids) VALUES ('Is Customer', -1, 'Select * from contacts where iscustomer = 1', '2011-07-29',0, 1)",
                    "INSERT INTO massprintrules (cname, inttype, content, dateadded, invisible, groupsids) VALUES ('Is Suply', -1, 'Select * from contacts where iscustomer = 1', '2011-07-29',0, 1)"});        
        ////////////////////////////////////////////////////////////////////////////////////////////
        // mysql updates
        UPDATES_MYSQL.put(1.11, new String[]{
                    "ALTER TABLE products ADD COLUMN stockvalue DOUBLE DEFAULT 0 NOT NULL",
                    "ALTER TABLE products ADD COLUMN thresholdvalue DOUBLE DEFAULT 0 NOT NULL",
                    "ALTER TABLE products ADD COLUMN intinventorytype SMALLINT DEFAULT 0 NOT NULL"
                });
        UPDATES_MYSQL.put(1.12, new String[]{
                    "ALTER TABLE templates ADD COLUMN printer VARCHAR(50) DEFAULT '" + Template.PRINTER_UNDEFINED + "' NOT NULL ",});
        UPDATES_MYSQL.put(1.14, new String[]{
                    "ALTER TABLE products DROP KEY CONST8",});
        UPDATES_MYSQL.put(1.15, new String[]{
                    "CREATE TABLE productstosuppliers ("
                    + "ids             	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,"
                    + "productsids      BIGINT(20) UNSIGNED NOT NULL,"
                    + "contactsids      BIGINT(20) UNSIGNED NOT NULL,"
                    + "cname            VARCHAR(250) DEFAULT NULL,"
                    + "groupsids        BIGINT(20) DEFAULT 1,"
                    + "dateadded        DATE NOT NULL,"
                    + "intaddedby       BIGINT(20) UNSIGNED DEFAULT 0"
                    + ")ENGINE=MyISAM DEFAULT CHARSET=utf8",
                    "ALTER TABLE productstosuppliers "
                    + "ADD CONSTRAINT products0843168601 "
                    + "FOREIGN KEY(contactsids) "
                    + "REFERENCES contacts(ids) "
                    + "ON DELETE CASCADE ",
                    "ALTER TABLE productstosuppliers "
                    + "ADD CONSTRAINT products0843168602 "
                    + "FOREIGN KEY(productsids) "
                    + "REFERENCES products(ids) "
                    + "ON DELETE CASCADE "
                });
        UPDATES_MYSQL.put(1.16, new String[]{
                    "ALTER TABLE products ADD COLUMN productlistsids BIGINT(20) UNSIGNED DEFAULT 0",});

        UPDATES_MYSQL.put(1.17, new String[]{
                    "ALTER TABLE contacts ADD COLUMN bankaccount VARCHAR(250) DEFAULT NULL",
                    "ALTER TABLE contacts ADD COLUMN bankid VARCHAR(250) DEFAULT NULL",
                    "ALTER TABLE contacts ADD COLUMN bankname VARCHAR(250) DEFAULT NULL",
                    "ALTER TABLE contacts ADD COLUMN bankcurrency VARCHAR(250) DEFAULT NULL",
                    "ALTER TABLE contacts ADD COLUMN bankcountry VARCHAR(250) DEFAULT NULL",});
        UPDATES_MYSQL.put(1.181, new String[]{
//                    "ALTER TABLE valueproperties DROP INDEX IF EXISTS values_index0",
                //                    "DROP TABLE IF EXISTS valueproperties"
                });
        UPDATES_MYSQL.put(1.182, new String[]{
                    //                    "ALTER TABLE items DROP COLUMN discountvalue",
                    "CREATE INDEX items_index0 ON items(cnumber(100))",
                    "CREATE INDEX items_index1 ON items(cname(100))",
                    "CREATE INDEX products_index0 ON products(cnumber(100))",
                    "CREATE INDEX products_index1 ON products(cname(100))",
                    "CREATE INDEX contacts_index0 ON contacts(cname(100))",
                    "CREATE TABLE valueproperties ("
                    + "ids BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment, "
                    + "CONSTRAINT constvp0 UNIQUE (cname, contextids, objectids, groupsids),"
                    + "cname VARCHAR(250) NOT NULL, "
                    + "classname VARCHAR(250) NOT NULL, "
                    + "contextids BIGINT(20) UNSIGNED NOT NULL,"
                    + "objectids BIGINT(20) UNSIGNED NOT NULL,"
                    + "value LONGTEXT DEFAULT NULL, "
                    + "dateadded DATE NOT NULL, "
                    + "intaddedby BIGINT(20) UNSIGNED NOT NULL, "
                    + "groupsids BIGINT(20) UNSIGNED DEFAULT 1 REFERENCES groups(ids), "
                    + "invisible BIGINT(20) UNSIGNED DEFAULT 0"
                    + ")ENGINE=MyISAM DEFAULT CHARSET=utf8",
                    "CREATE INDEX values_index0 ON valueproperties(cname(100), contextids, objectids)",
                    "ALTER TABLE expenses ADD COLUMN dateend DATE ",
                    "ALTER TABLE expenses ADD COLUMN ispaid SMALLINT DEFAULT 0 NOT NULL",
                    "ALTER TABLE revenues ADD COLUMN dateend DATE ",
                    "ALTER TABLE revenues ADD COLUMN ispaid SMALLINT DEFAULT 0 NOT NULL",});
        UPDATES_MYSQL.put(1.183, new String[]{
                    "ALTER TABLE stages ADD COLUMN templategroup BIGINT(20) UNSIGNED DEFAULT 1 REFERENCES groups(ids)"
                });
        UPDATES_MYSQL.put(1.184, new String[]{
                    "ALTER TABLE subitems ADD COLUMN ordernr SMALLINT DEFAULT 0 NOT NULL"
                });
        UPDATES_MYSQL.put(1.1842, new String[]{
                    "ALTER TABLE valueproperties DROP COLUMN value",
                    "ALTER TABLE valueproperties ADD COLUMN value LONGBLOB DEFAULT NULL"
                });
        UPDATES_MYSQL.put(1.1843, new String[]{
                    "ALTER TABLE contacts DROP KEY CONST3",});

        UPDATES_MYSQL.put(1.185, new String[]{
                    "CREATE TABLE productprices ("
                    + "ids BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment, "
                    + "cname VARCHAR(250) NOT NULL, "
                    + "productsids BIGINT REFERENCES products(ids) ON DELETE CASCADE, "
                    + "externalnetvalue DOUBLE DEFAULT 0, "
                    + "internalnetvalue DOUBLE DEFAULT 0, "
                    + "mincountvalue DOUBLE DEFAULT 0, "
                    + "dateadded DATE NOT NULL, intaddedby BIGINT(20) UNSIGNED NOT NULL, "
                    + "groupsids BIGINT(20) UNSIGNED DEFAULT 1 REFERENCES groups(ids),"
                    + "invisible BIGINT(20) UNSIGNED DEFAULT 0"
                    + ")ENGINE=MyISAM DEFAULT CHARSET=utf8"});
        UPDATES_MYSQL.put(1.186, new String[]{
                    "CREATE TABLE scheduletypes (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "cname VARCHAR(250) NOT NULL,"
                    + "groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,"
                    + "usersids BIGINT REFERENCES users (ids)  ON DELETE CASCADE,"
                    + "dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0,"
                    + "invisible SMALLINT DEFAULT 0,"
                    + "PRIMARY KEY  (ids))",
                    "INSERT INTO scheduletypes (cname, groupsids, usersids, dateadded, invisible) VALUES ('Call',1,1,'2011-07-29',0)",
                    "INSERT INTO scheduletypes (cname, groupsids, usersids, dateadded, invisible) VALUES ('Meeting',1,1,'2011-07-29',0)",
                    "INSERT INTO scheduletypes (cname, groupsids, usersids, dateadded, invisible) VALUES ('Mail',1,1,'2011-07-29',0)",
                    "INSERT INTO scheduletypes (cname, groupsids, usersids, dateadded, invisible) VALUES ('ToDo',1,1,'2011-07-29',0)",
                    "ALTER TABLE schedule ADD COLUMN contactsids BIGINT REFERENCES contacts (ids) ON DELETE CASCADE ",
                    "ALTER TABLE schedule ADD COLUMN eventtype BIGINT REFERENCES scheduletypes (ids) ON DELETE CASCADE"});
        UPDATES_MYSQL.put(1.187, new String[]{
                    "CREATE TABLE conversations (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,"
                    + "cname VARCHAR(2500), "
                    + "adress VARCHAR(2500), "
                    + "cnumber VARCHAR(6), "
                    + "groupsids BIGINT UNSIGNED NOT NULL DEFAULT 1, "
                    + "date DATE NOT NULL,dateadded DATE NOT NULL,"
                    + "intaddedby BIGINT UNSIGNED NOT NULL DEFAULT 1, "
                    + "invisible SMALLINT DEFAULT 0, "
                    + "content LONGVARCHAR NOT NULL)ENGINE=MyISAM  DEFAULT CHARSET=utf8"});
        UPDATES_MYSQL.put(1.188, new String[]{
                    "ALTER TABLE subitems ADD COLUMN inttype SMALLINT DEFAULT 0 NOT NULL"
                });
        UPDATES_MYSQL.put(1.189, new String[]{
                    "ALTER TABLE conversations ADD COLUMN contactsids BIGINT REFERENCES contacts (ids) ON DELETE CASCADE"
                });        
        UPDATES_MYSQL.put(1.190, new String[]{
                    "CREATE TABLE activitylists (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,"
                    + " cname VARCHAR(200) DEFAULT NULL,"
                    + " cnumber VARCHAR(250) NOT NULL,"
                    + " groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,"
                    + " contactsids BIGINT  REFERENCES contacts(ids) DEFAULT 0,"
                    + " orderids BIGINT  REFERENCES items(ids) DEFAULT 0,"
                    + " totalamount DOUBLE DEFAULT 0,"
                    + " dateadded DATE NOT NULL,"
                    + " intaddedby BIGINT DEFAULT 0,"
                    + " invisible SMALLINT DEFAULT 0,"
                    + " PRIMARY KEY (ids))ENGINE=MyISAM  DEFAULT CHARSET=IS0-8859-15",
                    //"Date", "Count", "Description", "Netto Price", "Tax Value", "Total Price", "Product", "cname"     
                    "CREATE TABLE activitylistitems (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,"
                    + " cname VARCHAR(20) DEFAULT NULL,"
                    + " groupsids BIGINT REFERENCES groups(ids) DEFAULT 1,"
                    + " activitylistsids BIGINT REFERENCES activitylists(ids) ON DELETE CASCADE,"
                    + " datedoing DATE NOT NULL,"
                    + " quantityvalue DOUBLE DEFAULT 0 NOT NULL,"
                    + " measure VARCHAR(25) NOT NULL,"
                    + " description VARCHAR(1000) DEFAULT NULL,"                
                    + " internalvalue DOUBLE DEFAULT 0,"
                    + " taxpercentvalue DOUBLE DEFAULT 0 NOT NULL,"
                    + " totalbrutvalue DOUBLE DEFAULT 0,"
                    + " productsids BIGINT REFERENCES products(ids) ON DELETE CASCADE,"
                    + " dateadded DATE NOT NULL,"
                    + " intaddedby BIGINT DEFAULT 0,"
                    + " invisible SMALLINT DEFAULT 0,"
                    + " PRIMARY KEY (ids))ENGINE=MyISAM  DEFAULT CHARSET=IS0-8859-15"
                });
        UPDATES_MYSQL.put(1.191, new String[]{
                    "ALTER TABLE templates ADD COLUMN isupdateenabled SMALLINT DEFAULT 0 NOT NULL",
                    "ALTER TABLE templates ADD COLUMN lastmodified BIGINT DEFAULT NULL",
                    "ALTER TABLE templates ADD COLUMN pathtofile VARCHAR(2500) DEFAULT NULL"
                });
        UPDATES_MYSQL.put(1.192, new String[]{
                    "ALTER TABLE subitems ADD COLUMN discount DOUBLE DEFAULT 0 NOT NULL"
                });  
        UPDATES_MYSQL.put(1.193, new String[]{
                    "CREATE TABLE massprintrules (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + " cname VARCHAR(250) NOT NULL,"
                    + " content LONG VARCHAR NOT NULL,"
                    + " inttype SMALLINT DEFAULT 0,"
                    + " dateadded DATE NOT NULL,"
                    + " groupsids BIGINT REFERENCES groups(ids) DEFAULT 1,"
                    + " invisible SMALLINT DEFAULT 0,"
                    + "PRIMARY KEY  (ids))ENGINE=MyISAM  DEFAULT CHARSET=IS0-8859-15",
                    "INSERT INTO massprintrules (cname, inttype, content, dateadded, invisible, groupsids) VALUES ('Open Bills',0, 'Select * from contacts Inner Join items on (Items.contactsids = contacts.ids) where items.intstatus = 4 and items.inttype =3', '2011-07-29',0, 1)",
                    "INSERT INTO massprintrules (cname, inttype, content, dateadded, invisible, groupsids) VALUES ('Is Customer', -1, 'Select * from contacts where iscustomer = 1', '2011-07-29',0, 1)",
                    "INSERT INTO massprintrules (cname, inttype, content, dateadded, invisible, groupsids) VALUES ('Is Suply', -1, 'Select * from contacts where iscustomer = 1', '2011-07-29',0, 1)"});              
    }

    /**
     * Update the database from the specified to the max available version
     * @param version
     */
    public void updateFrom(double version) throws Exception {
        Log.Debug(DatabaseUpdater.class, "Updating database from " + version);

        double newVersion = version;

        if (ConnectionTypeHandler.getDriverType() == ConnectionTypeHandler.DERBY) {
            for (Iterator<Double> keys = UPDATES_DERBY.keySet().iterator(); keys.hasNext();) {
                Double vers = keys.next();
                if (vers.doubleValue() > version) {
                    String[] val = UPDATES_DERBY.get(vers);
                    try {
                        DatabaseConnection.instanceOf().runQueries(val);
                        if (vers > newVersion) {
                            newVersion = vers;
                        }
                    } catch (Exception ex) {
                        if (ex.getMessage().contains("does not exist")) {
                            Log.Debug(this, ex.getMessage());
                        } else {
                            throw ex;
                        }
                    }
                }
            }
        } else {
            for (Iterator<Double> keys = UPDATES_MYSQL.keySet().iterator(); keys.hasNext();) {
                Double vers = keys.next();
                if (vers.doubleValue() > version) {
                    String[] val = UPDATES_MYSQL.get(vers);
                    try {
                        DatabaseConnection.instanceOf().runQueries(val);
                        if (vers > newVersion) {
                            newVersion = vers;
                        }
                    } catch (Exception ex) {
                        if (ex.getMessage().contains("does not exist")) {
                            Log.Debug(this, ex.getMessage());
                        } else {
                            throw ex;
                        }
                    }
                }
            }
        }

        setVersion(newVersion);
    }

    private void setVersion(double newVersion) {
        try {
            DatabaseConnection.instanceOf().runQueries(
                    new String[]{
                        "UPDATE globalsettings SET value ='" + newVersion + "' WHERE cname = 'yabs_dbversion'"});
        } catch (Exception ex) {
            Log.Debug(ex);
            Popup.error(ex);
        }

    }
}
