  /*
 *  This file is part of MP.
 *
 *  MP is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.db.common;

import javax.swing.UIManager;
import mpv5.globals.Messages;

/**
 * 
 */
public class DatabaseInstallation {


     /**
       * This contains the database structure for mpv5
       *
       * As SQL.Views are currently not updateable from DERBY, i use two nearly identical tables here, to store user informations.
       * First one holds a users default data, where the second table holds additional address info.
       *
     */
    public final static String[] DERBY_STRUCTURE = new String[]{

//sub tables
"CREATE TABLE groups (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(250) UNIQUE NOT NULL,description VARCHAR(750) DEFAULT NULL,defaults VARCHAR(250) DEFAULT NULL,groupsids BIGINT DEFAULT 0,dateadded DATE NOT NULL,reserve1 VARCHAR(500) default NULL,intaddedby BIGINT DEFAULT 0, reserve2 VARCHAR(500) default NULL, PRIMARY KEY  (ids))",
"CREATE TABLE tax (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(250), taxvalue DOUBLE DEFAULT 0,identifier VARCHAR(250) UNIQUE NOT NULL,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0,PRIMARY KEY  (ids))",
"CREATE TABLE history (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(250)  NOT NULL, username VARCHAR(250) NOT NULL,dbidentity VARCHAR(25)  NOT NULL, INTitem SMALLINT NOT NULL,groupsids BIGINT DEFAULT 0, dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0,reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (ids))",
"CREATE TABLE countries (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(250)  UNIQUE NOT NULL, iso SMALLINT  UNIQUE NOT NULL,groupsids BIGINT DEFAULT 1,reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (ids))",
"CREATE TABLE searchindex (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),dbidentity VARCHAR(25) NOT NULL, groupsids BIGINT, rowID BIGINT NOT NULL,text VARCHAR(5000) default NULL)",
"CREATE TABLE globalsettings (IDS BIGINT DEFAULT 1, cname VARCHAR(250) UNIQUE NOT NULL, groupsids BIGINT, value VARCHAR(250) NOT NULL)",


//Main tables, must have ids, cname, groupsids, dateadded, intaddedby, invisible
"CREATE TABLE contacts (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cnumber VARCHAR(250) UNIQUE DEFAULT NULL, taxnumber VARCHAR(250), title VARCHAR(250) default NULL,groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,country VARCHAR(50) default NULL, prename VARCHAR(250) default NULL, cname VARCHAR(250) default NULL, street VARCHAR(250) default NULL,zip VARCHAR(50) default NULL,city VARCHAR(300) default NULL, mainphone VARCHAR(250) default NULL,fax VARCHAR(250) default NULL,mobilephone VARCHAR(250) default NULL,workphone VARCHAR(250) default NULL,mailaddress VARCHAR(350) default NULL,company VARCHAR(250) DEFAULT NULL, department VARCHAR(250) DEFAULT NULL,website VARCHAR(350) default NULL,notes VARCHAR(10000),dateadded DATE NOT NULL,isactive SMALLINT DEFAULT 0,iscustomer SMALLINT DEFAULT 0,ismanufacturer SMALLINT DEFAULT 0,issupplier SMALLINT DEFAULT 0,iscompany SMALLINT DEFAULT 0,ismale SMALLINT DEFAULT 0,isenabled SMALLINT DEFAULT 1,intaddedby BIGINT DEFAULT 0, invisible SMALLINT DEFAULT 0, reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (ids))",
"CREATE TABLE users (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(250) UNIQUE NOT NULL, groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1, intdefaultaccount BIGINT DEFAULT 1, intdefaultstatus BIGINT DEFAULT 1,fullname VARCHAR(250) NOT NULL, password VARCHAR(250) NOT NULL,laf VARCHAR(50) default NULL, locale VARCHAR(50) default NULL, defcountry VARCHAR(50) default NULL, mail VARCHAR(50) default NULL, language VARCHAR(50) default NULL, inthighestright SMALLINT DEFAULT 3,isenabled SMALLINT DEFAULT 1,isrgrouped SMALLINT DEFAULT 0,isloggedin SMALLINT DEFAULT 0,datelastlog DATE default NULL, dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0,invisible SMALLINT DEFAULT 0,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (ids))",
"CREATE TABLE files (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(25) UNIQUE NOT NULL, groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,dateadded DATE NOT NULL,data BLOB(5M) NOT NULL,filesize BIGINT NOT NULL,intaddedby BIGINT DEFAULT 0,invisible SMALLINT DEFAULT 0,PRIMARY KEY  (ids))",
"CREATE TABLE languages(IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(250) UNIQUE NOT NULL, groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,longname VARCHAR(250) UNIQUE NOT NULL, filename VARCHAR(25) NOT NULL REFERENCES files(cname) ON DELETE CASCADE,dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0,invisible SMALLINT DEFAULT 0,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (ids))",
"CREATE TABLE favourites (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(250) NOT NULL, usersids BIGINT REFERENCES users (ids)  ON DELETE CASCADE,groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,itemsids BIGINT NOT NULL,dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0,invisible SMALLINT DEFAULT 0,reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (ids))",
"CREATE TABLE accounts(IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), intaccountclass SMALLINT DEFAULT 0, cname VARCHAR(250) NOT NULL, description VARCHAR(250) NOT NULL, taxvalue DOUBLE NOT NULL DEFAULT 0, dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0, intparentaccount BIGINT DEFAULT 0, groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1, invisible SMALLINT DEFAULT 0, intaccounttype SMALLINT NOT NULL, euruid SMALLINT NOT NULL, ustvauid SMALLINT NOT NULL, taxuid SMALLINT NOT NULL, frame VARCHAR(25) NOT NULL, PRIMARY KEY  (ids))",
"CREATE TABLE items (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(250) NOT NULL, cnumber VARCHAR(250) UNIQUE NOT NULL, description VARCHAR(2500) DEFAULT NULL, groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1, defaultaccountsids BIGINT  REFERENCES accounts(ids) DEFAULT 1,contactsids BIGINT REFERENCES contacts(ids)  ON DELETE CASCADE, netvalue DOUBLE DEFAULT 0,taxvalue DOUBLE DEFAULT 0, datetodo DATE DEFAULT NULL, dateend DATE DEFAULT NULL, intreminders INTEGER DEFAULT 0, inttype SMALLINT DEFAULT 0, dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0,invisible SMALLINT DEFAULT 0,intstatus SMALLINT DEFAULT 0,reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL, PRIMARY KEY  (ids))",
"CREATE TABLE itemslists (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), description VARCHAR(2500), cname VARCHAR(250), groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1, itemsids BIGINT  REFERENCES items(ids) ON DELETE RESTRICT,dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0,invisible SMALLINT DEFAULT 0,reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL, PRIMARY KEY  (ids))",
"CREATE TABLE subitems (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), cname VARCHAR(2500) default NULL,itemsids BIGINT REFERENCES items(ids)  ON DELETE CASCADE, groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,originalproductsids BIGINT DEFAULT NULL, countvalue DOUBLE DEFAULT 0 NOT NULL, quantityvalue DOUBLE DEFAULT 0 NOT NULL, measure VARCHAR(250) NOT NULL,description VARCHAR(1000) default NULL,  internalvalue DOUBLE DEFAULT 0,  externalvalue DOUBLE DEFAULT 0, taxpercentvalue DOUBLE DEFAULT 0 NOT NULL,datedelivery DATE DEFAULT NULL, dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0,invisible SMALLINT DEFAULT 0,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (ids))",
"CREATE TABLE schedule (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(250) NOT NULL, groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,usersids BIGINT REFERENCES users (ids)  ON DELETE CASCADE,itemsids BIGINT REFERENCES items (ids)  ON DELETE CASCADE,nextdate DATE NOT NULL, intervalmonth SMALLINT NOT NULL, dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0,invisible SMALLINT DEFAULT 0,reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (ids))",
"CREATE TABLE products (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), cname VARCHAR(500) NOT NULL, cnumber VARCHAR(150) , description VARCHAR(500), externalnetvalue DOUBLE DEFAULT 0, internalnetvalue DOUBLE DEFAULT 0, measure VARCHAR(250) NOT NULL, taxids BIGINT REFERENCES tax(ids) ON DELETE CASCADE, manufacturersids BIGINT DEFAULT 0, suppliersids BIGINT DEFAULT 0, groupsids  BIGINT  REFERENCES groups(ids) DEFAULT 1,url VARCHAR(250) default NULL,ean VARCHAR(25) default null, reference VARCHAR(50) default null,dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0,invisible SMALLINT DEFAULT 0, inttype SMALLINT NOT NULL, reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (ids))",
"CREATE TABLE userproperties(IDS BIGINT DEFAULT 1,cname VARCHAR(250) NOT NULL, value VARCHAR(250) NOT NULL, usersids BIGINT NOT NULL, dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0, groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,invisible SMALLINT DEFAULT 0)",
"CREATE TABLE messages(IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(1000) NOT NULL, dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0, groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,invisible SMALLINT DEFAULT 0, PRIMARY KEY  (ids))",
"CREATE TABLE mails(IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(1000) NOT NULL, dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0, groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,invisible SMALLINT DEFAULT 0, intstatus SMALLINT DEFAULT 0, usersids BIGINT REFERENCES users(ids) ON DELETE CASCADE, mailid VARCHAR(25) UNIQUE NOT NULL , PRIMARY KEY  (ids))",


//sub tables #2
"CREATE TABLE tablelock (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(250), rowID BIGINT NOT NULL, usersids BIGINT REFERENCES users(ids)  ON DELETE CASCADE,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (ids), CONSTRAINT one_lock UNIQUE(cname, rowid))",
"CREATE TABLE itemstoaccounts (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), itemsids BIGINT NOT NULL REFERENCES items(ids) ON DELETE CASCADE, accountsids BIGINT REFERENCES accounts(ids)  ON DELETE CASCADE,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (ids))",
"CREATE TABLE messagestoitems (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), itemsids BIGINT NOT NULL REFERENCES items(ids) ON DELETE CASCADE, messagesids BIGINT REFERENCES messages(ids)  ON DELETE CASCADE,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (ids))",
"CREATE TABLE trashbin (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(500), rowID BIGINT NOT NULL, description VARCHAR(2500), deleteme SMALLINT DEFAULT 1, reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (ids))",
"CREATE TABLE filestocontacts(IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(250) NOT NULL, description VARCHAR(550) DEFAULT NULL, contactsids BIGINT NOT NULL  REFERENCES contacts(ids) ON DELETE CASCADE, filename VARCHAR(25) NOT NULL REFERENCES files(cname) ON DELETE CASCADE, intsize BIGINT DEFAULT 0, mimetype VARCHAR(25) DEFAULT NULL, intaddedby BIGINT DEFAULT 0,dateadded DATE NOT NULL,groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,PRIMARY KEY  (ids))",
"CREATE TABLE filestoitems(IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(250) NOT NULL, description VARCHAR(550) DEFAULT NULL, itemsids BIGINT NOT NULL  REFERENCES items(ids) ON DELETE CASCADE,filename VARCHAR(25) NOT NULL REFERENCES files(cname) ON DELETE CASCADE,intsize BIGINT DEFAULT 0, mimetype VARCHAR(25) DEFAULT NULL, intaddedby BIGINT DEFAULT 0,dateadded DATE NOT NULL,groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,PRIMARY KEY  (ids))",
"CREATE TABLE plugins(IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(250) NOT NULL, description VARCHAR(550) DEFAULT NULL,filename VARCHAR(25) NOT NULL REFERENCES files(cname) ON DELETE CASCADE,intaddedby BIGINT DEFAULT 0,dateadded DATE NOT NULL,groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,PRIMARY KEY  (ids))",
"CREATE TABLE pluginstousers(IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(250) NOT NULL, usersids BIGINT NOT NULL, pluginsids BIGINT NOT NULL REFERENCES plugins(ids) ON DELETE CASCADE,intaddedby BIGINT DEFAULT 0,dateadded DATE NOT NULL,groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,PRIMARY KEY  (ids))",
"CREATE TABLE formatstousers(IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),cname VARCHAR(250) NOT NULL, usersids BIGINT REFERENCES users(ids) ON DELETE CASCADE, inttype SMALLINT  DEFAULT 0,PRIMARY KEY  (ids))",
"CREATE TABLE addresses (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,contactsids BIGINT REFERENCES contacts(ids),title VARCHAR(250) default NULL, taxnumber VARCHAR(250),prename VARCHAR(250) default NULL, cname VARCHAR(250) default NULL, street VARCHAR(250) default NULL,zip VARCHAR(50) default NULL,city VARCHAR(300) default NULL, company VARCHAR(250) DEFAULT NULL, department VARCHAR(250) DEFAULT NULL,country VARCHAR(50) default NULL, ismale SMALLINT DEFAULT 0,intaddedby BIGINT DEFAULT 0,dateadded DATE DEFAULT NULL,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (ids))",

//Trigger
"CREATE TRIGGER contacts_indexer1 AFTER INSERT ON contacts REFERENCING NEW AS newdata FOR EACH ROW INSERT INTO searchindex (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids, 'contacts',newdata.ids,newdata.cnumber||' '||newdata.taxnumber||' '||newdata.title||' '||newdata.country||' '|| newdata.prename||' '|| newdata.cname||' '|| newdata.street||' '||newdata.zip||' '|| newdata.city ||' '||newdata.mainphone||' '||newdata.fax||' '||newdata.mobilephone||' '||newdata.workphone||' '||newdata.mailaddress||' '||newdata.company||' '|| newdata.department||' '||newdata.website||' '||newdata.notes)",
"CREATE TRIGGER contacts_indexer2 AFTER UPDATE ON contacts REFERENCING NEW AS newdata FOR EACH ROW DELETE FROM searchindex WHERE dbidentity = 'contacts' AND  rowid = newdata.ids",
"CREATE TRIGGER contacts_indexer3 AFTER UPDATE ON contacts REFERENCING NEW AS newdata FOR EACH ROW INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'contacts',newdata.ids,newdata.cnumber||' '||newdata.taxnumber||' '||newdata.title||' '||newdata.country||' '|| newdata.prename||' '|| newdata.cname||' '|| newdata.street||' '||newdata.zip||' '|| newdata.city ||' '||newdata.mainphone||' '||newdata.fax||' '||newdata.mobilephone||' '||newdata.workphone||' '||newdata.mailaddress||' '||newdata.company||' '|| newdata.department||' '||newdata.website||' '||newdata.notes)",
"CREATE TRIGGER contacts_indexer4 AFTER DELETE ON contacts REFERENCING OLD AS newdata FOR EACH ROW DELETE FROM searchindex WHERE dbidentity = 'contacts' AND  rowid = newdata.ids",
"CREATE TRIGGER contacts_trash1   AFTER UPDATE ON contacts REFERENCING NEW AS newdata FOR EACH ROW INSERT INTO trashbin (deleteme, cname, rowid, description) VALUES (newdata.invisible,'contacts',newdata.ids,newdata.cnumber||' ('|| newdata.cname||')')",
"CREATE TRIGGER contacts_trash2 AFTER DELETE ON contacts REFERENCING OLD AS newdata FOR EACH ROW DELETE FROM trashbin WHERE cname = 'contacts' AND  rowid = newdata.ids",


"CREATE TRIGGER filestocontacts_indexer1 AFTER INSERT ON filestocontacts REFERENCING NEW AS newdata FOR EACH ROW INSERT INTO searchindex (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'filestocontacts',newdata.ids,newdata.cname||' '||newdata.description||' '||newdata.filename)",
"CREATE TRIGGER filestocontacts_indexer2 AFTER UPDATE ON filestocontacts REFERENCING NEW AS newdata FOR EACH ROW DELETE FROM searchindex WHERE dbidentity = 'filestocontacts' AND  rowid = newdata.ids",
"CREATE TRIGGER filestocontacts_indexer3 AFTER UPDATE ON filestocontacts REFERENCING NEW AS newdata FOR EACH ROW INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'filestocontacts',newdata.ids,newdata.cname||' '||newdata.description||' '||newdata.filename)",
"CREATE TRIGGER filestocontacts_indexer4 AFTER DELETE ON filestocontacts REFERENCING OLD AS newdata FOR EACH ROW DELETE FROM searchindex WHERE dbidentity = 'filestocontacts' AND  rowid = newdata.ids",
"CREATE TRIGGER groups_indexer1 AFTER INSERT ON groups REFERENCING NEW AS newdata FOR EACH ROW INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'groups',newdata.ids,newdata.cname||' '||newdata.description||' '||newdata.dateadded)",
"CREATE TRIGGER groups_indexer2 AFTER UPDATE ON groups REFERENCING NEW AS newdata FOR EACH ROW DELETE FROM searchindex WHERE dbidentity = 'groups' AND  rowid = newdata.ids",
"CREATE TRIGGER groups_indexer3 AFTER UPDATE ON groups REFERENCING NEW AS newdata FOR EACH ROW INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'groups',newdata.ids,newdata.cname||' '||newdata.description||' '||newdata.dateadded)",
"CREATE TRIGGER groups_indexer4 AFTER DELETE ON groups REFERENCING OLD AS newdata FOR EACH ROW DELETE FROM searchindex WHERE dbidentity = 'groups' AND  rowid = newdata.ids",
"CREATE TRIGGER items_indexer1 AFTER INSERT ON items REFERENCING NEW AS newdata FOR EACH ROW INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'items',newdata.ids,newdata.cname||' '||newdata.dateadded)",
"CREATE TRIGGER items_indexer2 AFTER UPDATE ON items REFERENCING NEW AS newdata FOR EACH ROW DELETE FROM searchindex WHERE dbidentity = 'items' AND  rowid = newdata.ids",
"CREATE TRIGGER items_indexer3 AFTER UPDATE ON items REFERENCING NEW AS newdata FOR EACH ROW INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'items',newdata.ids,newdata.cname||' '||newdata.dateadded)",
"CREATE TRIGGER items_indexer4 AFTER DELETE ON items REFERENCING OLD AS newdata FOR EACH ROW DELETE FROM searchindex WHERE dbidentity = 'items' AND  rowid = newdata.ids",

"CREATE TRIGGER items_trash2 AFTER DELETE ON items REFERENCING OLD AS newdata FOR EACH ROW DELETE FROM trashbin WHERE cname = 'items' AND  rowid = newdata.ids",
"CREATE TRIGGER items_trash1 AFTER UPDATE ON items REFERENCING NEW AS newdata FOR EACH ROW INSERT INTO trashbin (deleteme, cname, rowid, description) VALUES (newdata.invisible,'items',newdata.ids,newdata.cname)",


"CREATE TRIGGER subitems_indexer1 AFTER INSERT ON subitems REFERENCING NEW AS newdata FOR EACH ROW INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'subitems',newdata.ids,newdata.cname||' '||newdata.description||' '||newdata.dateadded)",
"CREATE TRIGGER subitems_indexer2 AFTER UPDATE ON subitems REFERENCING NEW AS newdata FOR EACH ROW DELETE FROM searchindex WHERE dbidentity = 'subitems' AND  rowid = newdata.ids",
"CREATE TRIGGER subitems_indexer3 AFTER UPDATE ON subitems REFERENCING NEW AS newdata FOR EACH ROW INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'subitems',newdata.ids,newdata.cname||' '||newdata.description||' '||newdata.dateadded)",
"CREATE TRIGGER subitems_indexer4 AFTER DELETE ON subitems REFERENCING OLD AS newdata FOR EACH ROW DELETE FROM searchindex WHERE dbidentity = 'subitems' AND  rowid = newdata.ids",
"CREATE TRIGGER products_indexer1 AFTER INSERT ON products REFERENCING NEW AS newdata FOR EACH ROW INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'products',newdata.ids,newdata.cname||' '||newdata.cnumber||' '||newdata.description||' '||newdata.dateadded)",
"CREATE TRIGGER products_indexer2 AFTER UPDATE ON products REFERENCING NEW AS newdata FOR EACH ROW DELETE FROM searchindex WHERE dbidentity = 'products' AND  rowid = newdata.ids",
"CREATE TRIGGER products_indexer3 AFTER UPDATE ON products REFERENCING NEW AS newdata FOR EACH ROW INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'products',newdata.ids,newdata.cname||' '||newdata.cnumber||' '||newdata.description||' '||newdata.dateadded)",
"CREATE TRIGGER products_indexer4 AFTER DELETE ON products REFERENCING OLD AS newdata FOR EACH ROW DELETE FROM searchindex WHERE dbidentity = 'products' AND  rowid = newdata.ids",


"CREATE TRIGGER products_trash1 AFTER UPDATE ON products REFERENCING NEW AS newdata FOR EACH ROW INSERT INTO trashbin (deleteme, cname, rowid, description) VALUES (newdata.invisible, 'products',newdata.ids,newdata.cnumber||' ('|| newdata.cname||')')",
"CREATE TRIGGER messages_trash1 AFTER UPDATE ON messages REFERENCING NEW AS newdata FOR EACH ROW INSERT INTO trashbin (deleteme, cname, rowid, description) VALUES (newdata.invisible, 'messages',newdata.ids,' ('|| newdata.cname||')')",
"CREATE TRIGGER messages_trash2 AFTER DELETE ON messages REFERENCING OLD AS newdata FOR EACH ROW DELETE FROM trashbin WHERE cname = 'messages' AND  rowid = newdata.ids",
"CREATE TRIGGER products_trash2 AFTER DELETE ON products REFERENCING OLD AS newdata FOR EACH ROW DELETE FROM trashbin WHERE cname = 'products' AND  rowid = newdata.ids",


"CREATE TRIGGER thrash_handler1 AFTER INSERT ON trashbin FOR EACH STATEMENT DELETE FROM trashbin WHERE deleteme = 0",
"CREATE TRIGGER thrash_handler2 AFTER INSERT ON trashbin FOR EACH STATEMENT DELETE FROM trashbin WHERE ids IN (SELECT ids FROM trashbin WHERE EXISTS( SELECT ids FROM trashbin AS tmptable WHERE trashbin.cname = tmptable.cname AND trashbin.rowid = tmptable.rowid HAVING trashbin.ids < MAX(tmptable.ids) ) )",

"INSERT INTO tax(cname, dateadded, identifier) VALUES ('Default 0%', '2009-04-03 09:31:33', 'Default 0%')",
"INSERT INTO tax(cname, dateadded, identifier, taxvalue) VALUES ('Default 19%', '2009-04-03 09:31:33', 'Default 19%', 19.0)",
"INSERT INTO tax(cname, dateadded, identifier, taxvalue) VALUES ('Default 7%', '2009-04-03 09:31:33', 'Default 7%', 7.0)",

"INSERT INTO groups (cname,description, dateadded) VALUES ('"+Messages.GROUPNAMES+"','This group holds all yet ungrouped items.', '2009-04-03 09:31:33')",
"INSERT INTO groups (cname,description, dateadded) VALUES ('ungrouped','This group holds all yet ungrouped items.', '2009-04-03 09:31:33')",
"INSERT INTO accounts (cname,description, dateadded, taxvalue, intaccounttype, frame, euruid, ustvauid, taxuid) VALUES ('"+Messages.ACCOUNTNAMES+"','This account is the parent account of all account frames.', '2009-04-03 09:31:33', 0.0, 0, 'builtin',0,0,0)",
"INSERT INTO users (fullname,password,cname,laf,locale,mail,language,inthighestright,datelastlog,isenabled, dateadded ) VALUES ('Administrator','5f4dcc3b5aa765d61d8327deb882cf99','admin','" + UIManager.getSystemLookAndFeelClassName() + "','en_GB','','buildin_en',0,'2009-04-03 09:31:33',1,'2009-04-03 09:31:33')",
"INSERT INTO countries (iso, cname ) VALUES (276,'Deutschland')"

    };

    public final static String[] MYSQL_STRUCTURE = new String[]{
"CREATE TABLE groups (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(250) UNIQUE NOT NULL,description VARCHAR(750) DEFAULT NULL,defaults VARCHAR(250) DEFAULT NULL,groupsids BIGINT ,dateadded DATE NOT NULL,reserve1 VARCHAR(500) default NULL,intaddedby BIGINT , reserve2 VARCHAR(500) default NULL)",
"CREATE TABLE tax (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(250), taxvalue DOUBLE ,identifier VARCHAR(250) UNIQUE NOT NULL,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,dateadded DATE NOT NULL,intaddedby BIGINT )",
"CREATE TABLE history (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(250)  NOT NULL, username VARCHAR(250) NOT NULL,dbidentity VARCHAR(25)  NOT NULL, INTitem SMALLINT NOT NULL,groupsids BIGINT , dateadded DATE NOT NULL,intaddedby BIGINT ,reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL)",
"CREATE TABLE countries (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(250)  UNIQUE NOT NULL, iso SMALLINT  UNIQUE NOT NULL,groupsids BIGINT ,reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL)",
"CREATE TABLE searchindex (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,dbidentity VARCHAR(25) NOT NULL, groupsids BIGINT, rowID BIGINT NOT NULL,text VARCHAR(5000) default NULL)",

//Main tables, must have ids, cname, groupsids, dateadded, intaddedby, invisible
"CREATE TABLE contacts (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cnumber VARCHAR(250) UNIQUE DEFAULT NULL, taxnumber VARCHAR(250), title VARCHAR(250) default NULL,groupsids BIGINT REFERENCES groups(ids),country VARCHAR(50) default NULL, prename VARCHAR(250) default NULL, cname VARCHAR(250) default NULL, street VARCHAR(250) default NULL,zip VARCHAR(50) default NULL,city VARCHAR(300) default NULL, mainphone VARCHAR(250) default NULL,fax VARCHAR(250) default NULL,mobilephone VARCHAR(250) default NULL,workphone VARCHAR(250) default NULL,mailaddress VARCHAR(350) default NULL,company VARCHAR(250) DEFAULT NULL, department VARCHAR(250) DEFAULT NULL,website VARCHAR(350) default NULL,notes VARCHAR(10000),dateadded DATE NOT NULL,isactive SMALLINT ,iscustomer SMALLINT ,ismanufacturer SMALLINT ,issupplier SMALLINT ,iscompany SMALLINT ,ismale SMALLINT ,isenabled SMALLINT ,intaddedby BIGINT , invisible SMALLINT DEFAULT 0 , reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL)",
"CREATE TABLE users (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(250) UNIQUE NOT NULL, groupsids BIGINT  REFERENCES groups(ids), intdefaultaccount BIGINT , intdefaultstatus BIGINT ,fullname VARCHAR(250) NOT NULL, password VARCHAR(250) NOT NULL,laf VARCHAR(50) default NULL, locale VARCHAR(50) default NULL, defcountry VARCHAR(50) default NULL, mail VARCHAR(50) default NULL, language VARCHAR(50) default NULL, inthighestright SMALLINT DEFAULT 3,isenabled SMALLINT ,isrgrouped SMALLINT ,isloggedin SMALLINT ,datelastlog DATE default NULL, dateadded DATE NOT NULL,intaddedby BIGINT ,invisible SMALLINT DEFAULT 0 ,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL)",
"CREATE TABLE files (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(25) UNIQUE NOT NULL, groupsids BIGINT  REFERENCES groups(ids),dateadded DATE NOT NULL,data BLOB NOT NULL,filesize BIGINT NOT NULL,intaddedby BIGINT ,invisible SMALLINT DEFAULT 0 )",
"CREATE TABLE languages(IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(250) UNIQUE NOT NULL, groupsids BIGINT  REFERENCES groups(ids),longname VARCHAR(250) UNIQUE NOT NULL, filename VARCHAR(25) NOT NULL REFERENCES files(cname) ON DELETE CASCADE,dateadded DATE NOT NULL,intaddedby BIGINT ,invisible SMALLINT DEFAULT 0 ,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL)",
"CREATE TABLE favourites (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(250) NOT NULL, usersids BIGINT REFERENCES users (ids)  ON DELETE CASCADE,groupsids BIGINT  REFERENCES groups(ids),itemsids BIGINT NOT NULL,dateadded DATE NOT NULL,intaddedby BIGINT ,invisible SMALLINT DEFAULT 0 ,reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL)",
"CREATE TABLE accounts(IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment, intaccountclass SMALLINT , cname VARCHAR(250) NOT NULL, description VARCHAR(250) NOT NULL, taxvalue DOUBLE NOT NULL , dateadded DATE NOT NULL,intaddedby BIGINT , intparentaccount BIGINT , groupsids BIGINT  REFERENCES groups(ids), invisible SMALLINT DEFAULT 0 , intaccounttype SMALLINT NOT NULL, euruid SMALLINT NOT NULL, ustvauid SMALLINT NOT NULL, taxuid SMALLINT NOT NULL, frame VARCHAR(25) NOT NULL)",
"CREATE TABLE items (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(250) NOT NULL, cnumber VARCHAR(250) UNIQUE NOT NULL, description VARCHAR(2500) DEFAULT NULL, groupsids BIGINT  REFERENCES groups(ids), defaultaccountsids BIGINT  REFERENCES accounts(ids) ,contactsids BIGINT REFERENCES contacts(ids)  ON DELETE CASCADE, netvalue DOUBLE ,taxvalue DOUBLE , datetodo DATE DEFAULT NULL, dateend DATE DEFAULT NULL, intreminders INTEGER , inttype SMALLINT , dateadded DATE NOT NULL,intaddedby BIGINT ,invisible SMALLINT DEFAULT 0 ,intstatus SMALLINT ,reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL)",
"CREATE TABLE itemslists (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment, description VARCHAR(2500), cname VARCHAR(250), groupsids BIGINT  REFERENCES groups(ids), itemsids BIGINT  REFERENCES items(ids) ON DELETE RESTRICT,dateadded DATE NOT NULL,intaddedby BIGINT ,invisible SMALLINT DEFAULT 0 ,reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL)",
"CREATE TABLE subitems (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment, cname VARCHAR(2500) default NULL,itemsids BIGINT REFERENCES items(ids)  ON DELETE CASCADE, groupsids BIGINT  REFERENCES groups(ids),originalproductsids BIGINT DEFAULT NULL, countvalue DOUBLE  NOT NULL, quantityvalue DOUBLE  NOT NULL, measure VARCHAR(250) NOT NULL,description VARCHAR(1000) default NULL,  internalvalue DOUBLE ,  externalvalue DOUBLE , taxpercentvalue DOUBLE  NOT NULL,datedelivery DATE DEFAULT NULL, dateadded DATE NOT NULL,intaddedby BIGINT ,invisible SMALLINT DEFAULT 0 ,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL)",
"CREATE TABLE schedule (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(250) NOT NULL, groupsids BIGINT  REFERENCES groups(ids),usersids BIGINT REFERENCES users (ids)  ON DELETE CASCADE,itemsids BIGINT REFERENCES items (ids)  ON DELETE CASCADE,nextdate DATE NOT NULL, intervalmonth SMALLINT NOT NULL, dateadded DATE NOT NULL,intaddedby BIGINT ,invisible SMALLINT DEFAULT 0 ,reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL)",
"CREATE TABLE products (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment, cname VARCHAR(500) NOT NULL, cnumber VARCHAR(150) , description VARCHAR(500), externalnetvalue DOUBLE , internalnetvalue DOUBLE , measure VARCHAR(250) NOT NULL, taxids BIGINT REFERENCES tax(ids) ON DELETE CASCADE, manufacturersids BIGINT , suppliersids BIGINT , groupsids  BIGINT  REFERENCES groups(ids),url VARCHAR(250) default NULL,ean VARCHAR(25) default null, reference VARCHAR(50) default null,dateadded DATE NOT NULL,intaddedby BIGINT ,invisible SMALLINT DEFAULT 0 , inttype SMALLINT NOT NULL, reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL)",
"CREATE TABLE userproperties(IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(250) NOT NULL, value VARCHAR(250) NOT NULL, usersids BIGINT NOT NULL, dateadded DATE NOT NULL,intaddedby BIGINT , groupsids BIGINT  REFERENCES groups(ids),invisible SMALLINT DEFAULT 0 )",
"CREATE TABLE messages(IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(1000) NOT NULL, dateadded DATE NOT NULL,intaddedby BIGINT , groupsids BIGINT  REFERENCES groups(ids),invisible SMALLINT DEFAULT 0 )",
"CREATE TABLE mails(IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(1000) NOT NULL, dateadded DATE NOT NULL,intaddedby BIGINT , groupsids BIGINT  REFERENCES groups(ids),invisible SMALLINT DEFAULT 0 , intstatus SMALLINT , usersids BIGINT REFERENCES users(ids) ON DELETE CASCADE, mailid VARCHAR(25) UNIQUE NOT NULL )",


//sub tables #2
"CREATE TABLE tablelock (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(250), rowID BIGINT NOT NULL, usersids BIGINT REFERENCES users(ids)  ON DELETE CASCADE,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL, CONSTRAINT one_lock UNIQUE(cname, rowid))",
"CREATE TABLE itemstoaccounts (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment, itemsids BIGINT NOT NULL REFERENCES items(ids) ON DELETE CASCADE, accountsids BIGINT REFERENCES accounts(ids)  ON DELETE CASCADE,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL)",
"CREATE TABLE messagestoitems (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment, itemsids BIGINT NOT NULL REFERENCES items(ids) ON DELETE CASCADE, messagesids BIGINT REFERENCES messages(ids)  ON DELETE CASCADE,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL)",
"CREATE TABLE trashbin (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(500), rowID BIGINT NOT NULL, description VARCHAR(2500), deleteme SMALLINT , reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL)",
"CREATE TABLE filestocontacts(IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(250) NOT NULL, description VARCHAR(550) DEFAULT NULL, contactsids BIGINT NOT NULL  REFERENCES contacts(ids) ON DELETE CASCADE, filename VARCHAR(25) NOT NULL REFERENCES files(cname) ON DELETE CASCADE, intsize BIGINT , mimetype VARCHAR(25) DEFAULT NULL, intaddedby BIGINT ,dateadded DATE NOT NULL,groupsids BIGINT  REFERENCES groups(ids))",
"CREATE TABLE filestoitems(IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(250) NOT NULL, description VARCHAR(550) DEFAULT NULL, itemsids BIGINT NOT NULL  REFERENCES items(ids) ON DELETE CASCADE,filename VARCHAR(25) NOT NULL REFERENCES files(cname) ON DELETE CASCADE,intsize BIGINT , mimetype VARCHAR(25) DEFAULT NULL, intaddedby BIGINT ,dateadded DATE NOT NULL,groupsids BIGINT  REFERENCES groups(ids))",
"CREATE TABLE plugins(IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(250) NOT NULL, description VARCHAR(550) DEFAULT NULL,filename VARCHAR(25) NOT NULL REFERENCES files(cname) ON DELETE CASCADE,intaddedby BIGINT ,dateadded DATE NOT NULL,groupsids BIGINT  REFERENCES groups(ids))",
"CREATE TABLE pluginstousers(IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(250) NOT NULL, usersids BIGINT NOT NULL, pluginsids BIGINT NOT NULL REFERENCES plugins(ids) ON DELETE CASCADE,intaddedby BIGINT ,dateadded DATE NOT NULL,groupsids BIGINT  REFERENCES groups(ids))",
"CREATE TABLE formatstousers(IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,cname VARCHAR(250) NOT NULL, usersids BIGINT REFERENCES users(ids) ON DELETE CASCADE, inttype SMALLINT  )",
"CREATE TABLE addresses (IDS BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,groupsids BIGINT  REFERENCES groups(ids),contactsids BIGINT REFERENCES contacts(ids),title VARCHAR(250) default NULL, taxnumber VARCHAR(250),prename VARCHAR(250) default NULL, cname VARCHAR(250) default NULL, street VARCHAR(250) default NULL,zip VARCHAR(50) default NULL,city VARCHAR(300) default NULL, company VARCHAR(250) DEFAULT NULL, department VARCHAR(250) DEFAULT NULL,country VARCHAR(50) default NULL, ismale SMALLINT ,intaddedby BIGINT ,dateadded DATE DEFAULT NULL,reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL)",

////Trigger
//"CREATE TRIGGER contacts_indexer1 AFTER INSERT ON contacts REFERENCING NEW AS newdata FOR EACH ROW BEGIN INSERT INTO searchindex (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids, 'contacts',newdata.ids,newdata.cnumber||' '||newdata.taxnumber||' '||newdata.title||' '||newdata.country||' '|| newdata.prename||' '|| newdata.cname||' '|| newdata.street||' '||newdata.zip||' '|| newdata.city ||' '||newdata.mainphone||' '||newdata.fax||' '||newdata.mobilephone||' '||newdata.workphone||' '||newdata.mailaddress||' '||newdata.company||' '|| newdata.department||' '||newdata.website||' '||newdata.notes)",
//"CREATE TRIGGER contacts_indexer2 AFTER UPDATE ON contacts REFERENCING NEW AS newdata FOR EACH ROW BEGIN DELETE FROM searchindex WHERE dbidentity = 'contacts' AND  rowid = newdata.ids",
//"CREATE TRIGGER contacts_indexer3 AFTER UPDATE ON contacts REFERENCING NEW AS newdata FOR EACH ROW BEGIN INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'contacts',newdata.ids,newdata.cnumber||' '||newdata.taxnumber||' '||newdata.title||' '||newdata.country||' '|| newdata.prename||' '|| newdata.cname||' '|| newdata.street||' '||newdata.zip||' '|| newdata.city ||' '||newdata.mainphone||' '||newdata.fax||' '||newdata.mobilephone||' '||newdata.workphone||' '||newdata.mailaddress||' '||newdata.company||' '|| newdata.department||' '||newdata.website||' '||newdata.notes)",
//"CREATE TRIGGER contacts_indexer4 AFTER DELETE ON contacts REFERENCING OLD AS newdata FOR EACH ROW BEGIN DELETE FROM searchindex WHERE dbidentity = 'contacts' AND  rowid = newdata.ids",
//"CREATE TRIGGER contacts_trash1   AFTER UPDATE ON contacts REFERENCING NEW AS newdata FOR EACH ROW BEGIN INSERT INTO trashbin (deleteme, cname, rowid, description) VALUES (newdata.invisible,'contacts',newdata.ids,newdata.cnumber||' ('|| newdata.cname||')')",
//"CREATE TRIGGER contacts_trash2 AFTER DELETE ON contacts REFERENCING OLD AS newdata FOR EACH ROW BEGIN DELETE FROM trashbin WHERE cname = 'contacts' AND  rowid = newdata.ids",
//
//
//"CREATE TRIGGER filestocontacts_indexer1 AFTER INSERT ON filestocontacts REFERENCING NEW AS newdata FOR EACH ROW BEGIN INSERT INTO searchindex (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'filestocontacts',newdata.ids,newdata.cname||' '||newdata.description||' '||newdata.filename)",
//"CREATE TRIGGER filestocontacts_indexer2 AFTER UPDATE ON filestocontacts REFERENCING NEW AS newdata FOR EACH ROW BEGIN DELETE FROM searchindex WHERE dbidentity = 'filestocontacts' AND  rowid = newdata.ids",
//"CREATE TRIGGER filestocontacts_indexer3 AFTER UPDATE ON filestocontacts REFERENCING NEW AS newdata FOR EACH ROW BEGIN INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'filestocontacts',newdata.ids,newdata.cname||' '||newdata.description||' '||newdata.filename)",
//"CREATE TRIGGER filestocontacts_indexer4 AFTER DELETE ON filestocontacts REFERENCING OLD AS newdata FOR EACH ROW BEGIN DELETE FROM searchindex WHERE dbidentity = 'filestocontacts' AND  rowid = newdata.ids",
//"CREATE TRIGGER groups_indexer1 AFTER INSERT ON groups REFERENCING NEW AS newdata FOR EACH ROW BEGIN INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'groups',newdata.ids,newdata.cname||' '||newdata.description||' '||newdata.dateadded)",
//"CREATE TRIGGER groups_indexer2 AFTER UPDATE ON groups REFERENCING NEW AS newdata FOR EACH ROW BEGIN DELETE FROM searchindex WHERE dbidentity = 'groups' AND  rowid = newdata.ids",
//"CREATE TRIGGER groups_indexer3 AFTER UPDATE ON groups REFERENCING NEW AS newdata FOR EACH ROW BEGIN INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'groups',newdata.ids,newdata.cname||' '||newdata.description||' '||newdata.dateadded)",
//"CREATE TRIGGER groups_indexer4 AFTER DELETE ON groups REFERENCING OLD AS newdata FOR EACH ROW BEGIN DELETE FROM searchindex WHERE dbidentity = 'groups' AND  rowid = newdata.ids",
//"CREATE TRIGGER items_indexer1 AFTER INSERT ON items REFERENCING NEW AS newdata FOR EACH ROW BEGIN INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'items',newdata.ids,newdata.cname||' '||newdata.dateadded)",
//"CREATE TRIGGER items_indexer2 AFTER UPDATE ON items REFERENCING NEW AS newdata FOR EACH ROW BEGIN DELETE FROM searchindex WHERE dbidentity = 'items' AND  rowid = newdata.ids",
//"CREATE TRIGGER items_indexer3 AFTER UPDATE ON items REFERENCING NEW AS newdata FOR EACH ROW BEGIN INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'items',newdata.ids,newdata.cname||' '||newdata.dateadded)",
//"CREATE TRIGGER items_indexer4 AFTER DELETE ON items REFERENCING OLD AS newdata FOR EACH ROW BEGIN DELETE FROM searchindex WHERE dbidentity = 'items' AND  rowid = newdata.ids",
//
//"CREATE TRIGGER items_trash2 AFTER DELETE ON items REFERENCING OLD AS newdata FOR EACH ROW BEGIN DELETE FROM trashbin WHERE cname = 'items' AND  rowid = newdata.ids",
//"CREATE TRIGGER items_trash1 AFTER UPDATE ON items REFERENCING NEW AS newdata FOR EACH ROW BEGIN INSERT INTO trashbin (deleteme, cname, rowid, description) VALUES (newdata.invisible,'items',newdata.ids,newdata.cname)",
//
//
//"CREATE TRIGGER subitems_indexer1 AFTER INSERT ON subitems REFERENCING NEW AS newdata FOR EACH ROW BEGIN INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'subitems',newdata.ids,newdata.cname||' '||newdata.description||' '||newdata.dateadded)",
//"CREATE TRIGGER subitems_indexer2 AFTER UPDATE ON subitems REFERENCING NEW AS newdata FOR EACH ROW BEGIN DELETE FROM searchindex WHERE dbidentity = 'subitems' AND  rowid = newdata.ids",
//"CREATE TRIGGER subitems_indexer3 AFTER UPDATE ON subitems REFERENCING NEW AS newdata FOR EACH ROW BEGIN INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'subitems',newdata.ids,newdata.cname||' '||newdata.description||' '||newdata.dateadded)",
//"CREATE TRIGGER subitems_indexer4 AFTER DELETE ON subitems REFERENCING OLD AS newdata FOR EACH ROW BEGIN DELETE FROM searchindex WHERE dbidentity = 'subitems' AND  rowid = newdata.ids",
//"CREATE TRIGGER products_indexer1 AFTER INSERT ON products REFERENCING NEW AS newdata FOR EACH ROW BEGIN INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'products',newdata.ids,newdata.cname||' '||newdata.cnumber||' '||newdata.description||' '||newdata.dateadded)",
//"CREATE TRIGGER products_indexer2 AFTER UPDATE ON products REFERENCING NEW AS newdata FOR EACH ROW BEGIN DELETE FROM searchindex WHERE dbidentity = 'products' AND  rowid = newdata.ids",
//"CREATE TRIGGER products_indexer3 AFTER UPDATE ON products REFERENCING NEW AS newdata FOR EACH ROW BEGIN INSERT INTO searchindex  (groupsids, dbidentity, rowid, text) VALUES (newdata.groupsids,'products',newdata.ids,newdata.cname||' '||newdata.cnumber||' '||newdata.description||' '||newdata.dateadded)",
//"CREATE TRIGGER products_indexer4 AFTER DELETE ON products REFERENCING OLD AS newdata FOR EACH ROW BEGIN DELETE FROM searchindex WHERE dbidentity = 'products' AND  rowid = newdata.ids",
//
//
//"CREATE TRIGGER products_trash1 AFTER UPDATE ON products REFERENCING NEW AS newdata FOR EACH ROW BEGIN INSERT INTO trashbin (deleteme, cname, rowid, description) VALUES (newdata.invisible, 'products',newdata.ids,newdata.cnumber||' ('|| newdata.cname||')')",
//"CREATE TRIGGER messages_trash1 AFTER UPDATE ON messages REFERENCING NEW AS newdata FOR EACH ROW BEGIN INSERT INTO trashbin (deleteme, cname, rowid, description) VALUES (newdata.invisible, 'messages',newdata.ids,' ('|| newdata.cname||')')",
//"CREATE TRIGGER messages_trash2 AFTER DELETE ON messages REFERENCING OLD AS newdata FOR EACH ROW BEGIN DELETE FROM trashbin WHERE cname = 'messages' AND  rowid = newdata.ids",
//"CREATE TRIGGER products_trash2 AFTER DELETE ON products REFERENCING OLD AS newdata FOR EACH ROW BEGIN DELETE FROM trashbin WHERE cname = 'products' AND  rowid = newdata.ids",
//
//
//"CREATE TRIGGER thrash_handler1 AFTER INSERT ON trashbin FOR EACH STATEMENT DELETE FROM trashbin WHERE deleteme = 0",
//"CREATE TRIGGER thrash_handler2 AFTER INSERT ON trashbin FOR EACH STATEMENT DELETE FROM trashbin WHERE ids IN (SELECT ids FROM trashbin WHERE EXISTS( SELECT ids FROM trashbin AS tmptable WHERE trashbin.cname = tmptable.cname AND trashbin.rowid = tmptable.rowid HAVING trashbin.ids < MAX(tmptable.ids) ) )",

"INSERT INTO tax(cname, dateadded, identifier) VALUES ('Default', '2009-04-03 09:31:33', '%')",
"INSERT INTO tax(cname, dateadded, identifier, taxvalue) VALUES ('Default', '2009-04-03 09:31:33', '9%', 19.0)",
"INSERT INTO tax(cname, dateadded, identifier, taxvalue) VALUES ('Default', '2009-04-03 09:31:33', 'Default 7%', 7.0)",

"INSERT INTO groups (cname,description, dateadded) VALUES ('"+Messages.GROUPNAMES+"','This group holds all yet ungrouped items.', '2009-04-03 09:31:33')",
"INSERT INTO groups (cname,description, dateadded) VALUES ('ungrouped','This group holds all yet ungrouped items.', '2009-04-03 09:31:33')",
"INSERT INTO accounts (cname,description, dateadded, taxvalue, intaccounttype, frame, euruid, ustvauid, taxuid) VALUES ('"+Messages.ACCOUNTNAMES+"','This account is the parent account of all account frames.', '2009-04-03 09:31:33', 0.0, 0, 'builtin',0,0,0)",
"INSERT INTO users (fullname,password,cname,laf,locale,mail,language,inthighestright,datelastlog,isenabled, dateadded ) VALUES ('Administrator','5f4dcc3b5aa765d61d8327deb882cf99','admin','" + UIManager.getSystemLookAndFeelClassName() + "','en_GB','','buildin_en',0,'2009-04-03 09:31:33',1,'2009-04-03 09:31:33')",
"INSERT INTO countries (iso, cname ) VALUES (276,'Deutschland')"

    };

    private String[] CUSTOM_STRUCTURE;

    public String[] getStructure() {
        if (ConnectionTypeHandler.getDriverType() == ConnectionTypeHandler.DERBY) {
            return DERBY_STRUCTURE;
        } else if (ConnectionTypeHandler.getDriverType() == ConnectionTypeHandler.MYSQL) {
            return MYSQL_STRUCTURE;
        } else {
            return CUSTOM_STRUCTURE;
        }
    }

    /**
     * @param CUSTOM_STRUCTURE the CUSTOM_STRUCTURE SQL commands to set
     */
    public void setCUSTOM(String[] CUSTOM_STRUCTURE) {
        this.CUSTOM_STRUCTURE = CUSTOM_STRUCTURE;
    }
}
