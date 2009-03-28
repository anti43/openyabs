/*
 *  This file is part of MP by anti43 /GPL.
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

/**
 * @author anti43
 */
public class DatabaseInstallation {


     /**
       * This contains the database structure for mpv5
       *
       * As SQL.Views are currently not updateable from DERBY, i use two nearly identical tables here, to store user informations.
       * First one holds a users default data, where the second table holds additional address info.
       *
       * As this may change in the future, here is the CREATE VIEW statement (may get inaccurate):
       *
       * "CREATE TABLE contactdetails (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
       * "cnumber VARCHAR(250),taxnumber VARCHAR(250)," +
       * "mainphone VARCHAR(250) default NULL," +
       * "fax VARCHAR(250) default NULL,mobilephone VARCHAR(250) default NULL,workphone VARCHAR(250) default NULL," +
       * "mailaddress VARCHAR(350) default NULL," +
       * "website VARCHAR(350) default NULL,notes VARCHAR(10000)," +
       * "dateadded DATE DEFAULT CURRENT_DATE,isactive SMALLINT DEFAULT 0,iscustomer SMALLINT DEFAULT 0," +
       * "ismanufacturer SMALLINT DEFAULT 0,issupplier SMALLINT DEFAULT 0,iscompany SMALLINT DEFAULT 0," +
       * "isenabled SMALLINT DEFAULT 1,addedby VARCHAR(350) default NULL," +
       * "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
       * "PRIMARY KEY  (ids))",
       *
       * "CREATE TABLE addresses (IDT BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
       * "contactid BIGINT REFERENCES contactdetails(ids)," +
       * "title VARCHAR(250) default NULL," +
       * "prename VARCHAR(250) default NULL, cname VARCHAR(250) default NULL, street VARCHAR(250) default NULL," +
       * "zip VARCHAR(50) default NULL,city VARCHAR(300) default NULL, " +
       * "company VARCHAR(250) DEFAULT NULL, department VARCHAR(250) DEFAULT NULL," +
       * "ismale SMALLINT DEFAULT 0," +
       * "PRIMARY KEY  (IDT))",
       *
       * "CREATE VIEW contacts AS SELECT * FROM contactdetails " +
       * "LEFT JOIN addresses ON addresses.contactid = contactdetails.ids",
     */
    public final static String[] DERBY_STRUCTURE = new String[]{
//Main tables
        "CREATE TABLE groups (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "cname VARCHAR(250) UNIQUE NOT NULL," +
        "description VARCHAR(750) DEFAULT NULL," +
        "defaults VARCHAR(250) DEFAULT NULL," +
        "groupsids BIGINT DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL, " +
        "PRIMARY KEY  (ids))",

        "CREATE TABLE contacts (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "cnumber VARCHAR(250),taxnumber VARCHAR(250),title VARCHAR(250) default NULL," +
        "groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1," +
        "country VARCHAR(50) default NULL, "+
        "prename VARCHAR(250) default NULL, cname VARCHAR(250) default NULL, street VARCHAR(250) default NULL," +
        "zip VARCHAR(50) default NULL,city VARCHAR(300) default NULL, mainphone VARCHAR(250) default NULL," +
        "fax VARCHAR(250) default NULL,mobilephone VARCHAR(250) default NULL,workphone VARCHAR(250) default NULL," +
        "mailaddress VARCHAR(350) default NULL,company VARCHAR(250) DEFAULT NULL, department VARCHAR(250) DEFAULT NULL," +
        "website VARCHAR(350) default NULL,notes VARCHAR(10000)," +
        "dateadded DATE DEFAULT NULL,isactive SMALLINT DEFAULT 0,iscustomer SMALLINT DEFAULT 0," +
        "ismanufacturer SMALLINT DEFAULT 0,issupplier SMALLINT DEFAULT 0,iscompany SMALLINT DEFAULT 0," +
        "ismale SMALLINT DEFAULT 0,isenabled SMALLINT DEFAULT 1,addedby VARCHAR(350) default NULL," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
        "PRIMARY KEY  (ids))",

        "CREATE TABLE users (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"+
        "cname VARCHAR(250) UNIQUE NOT NULL, " +
        "groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1," +
        "fullname VARCHAR(250) NOT NULL, " +
        "password VARCHAR(250) NOT NULL,"+
        "laf VARCHAR(50) default NULL, "+
        "locale VARCHAR(50) default NULL, "+
        "defcountry VARCHAR(50) default NULL, "+
        "mail VARCHAR(50) default NULL, "+
        "language VARCHAR(50) default NULL, "+
        "inthighestright SMALLINT DEFAULT 3,"+
        "isenabled SMALLINT DEFAULT 1,"+
        "isloggedin SMALLINT DEFAULT 0,"+
        "datelastlog DATE default NULL, "+
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,"+
        "PRIMARY KEY  (ids))",

        "CREATE TABLE files (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"+
        "cname VARCHAR(25) UNIQUE NOT NULL, " +
        "groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1," +
        "dateadded DATE default NULL, "+
        "data BLOB(5M) NOT NULL,"+
        "filesize BIGINT NOT NULL,"+
        "PRIMARY KEY  (ids))",

        "CREATE TABLE languages(IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"+
        "cname VARCHAR(250) UNIQUE NOT NULL, " +
        "groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1," +
        "longname VARCHAR(250) NOT NULL, " +
        "filename VARCHAR(25) NOT NULL REFERENCES files(cname) ON DELETE CASCADE,"+
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,"+
        "PRIMARY KEY  (ids))",

        "CREATE TABLE favourites (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "cname VARCHAR(250) NOT NULL, " +
        "usersids BIGINT REFERENCES users (ids)  ON DELETE CASCADE," +
        "groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1," +
        "itemsids BIGINT NOT NULL," +
        "reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL," +
        "PRIMARY KEY  (ids))",

        "CREATE TABLE items (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "cnumber VARCHAR(250), cname VARCHAR(250) UNIQUE NOT NULL, " +
        "groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1," +
        "contactsids BIGINT REFERENCES contacts(ids)  ON DELETE CASCADE," +
        "dateadded DATE DEFAULT NULL, isactive SMALLINT DEFAULT 0, isfinished SMALLINT DEFAULT 0," +
        "value DOUBLE DEFAULT 0," +
        "taxvalue DOUBLE DEFAULT 0, datetodo DATE DEFAULT NULL, dateend DATE DEFAULT NULL, intreminders INTEGER DEFAULT 0," +
        "isBill SMALLINT DEFAULT 0,"+
        "isOrder SMALLINT DEFAULT 0,"+
        "isOffer SMALLINT DEFAULT 0,"+
        "reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL, " +
        "PRIMARY KEY  (ids))",

        "CREATE TABLE schedule (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "cname VARCHAR(250) NOT NULL, " +
        "groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1," +
        "usersids BIGINT REFERENCES users (ids)  ON DELETE CASCADE," +
        "itemsids BIGINT REFERENCES items (ids)  ON DELETE CASCADE," +
        "nextdate DATE NOT NULL, " +
        "intervalmonth SMALLINT NOT NULL, " +
        "reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL," +
        "PRIMARY KEY  (ids))",

        "CREATE TABLE tax (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "cname VARCHAR(250), taxvalue DOUBLE DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
        "PRIMARY KEY  (ids))",

        "CREATE TABLE products (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "cname VARCHAR(500) NOT NULL, cnumber VARCHAR(150) , description VARCHAR(500), pricenetvalue DOUBLE DEFAULT 0, buypricevalue DOUBLE DEFAULT 0, " +
        "taxids BIGINT REFERENCES tax(ids) ON DELETE CASCADE, manufacturer BIGINT REFERENCES contacts(ids)  ON DELETE CASCADE DEFAULT NULL," +
        "supplier BIGINT REFERENCES contacts(ids)  ON DELETE CASCADE DEFAULT NULL, groupsids  BIGINT  REFERENCES groups(ids) DEFAULT 1," +
        "dateadded DATE NOT NULL, url VARCHAR(250) default NULL,ean VARCHAR(25) default null, reference VARCHAR(50) default null," +
        "reserve1 VARCHAR(500) default NULL," +
        "reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (ids))",

//Subtables
        "CREATE TABLE countries (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "cname VARCHAR(250)  UNIQUE NOT NULL, iso SMALLINT  UNIQUE NOT NULL," +
        "reserve1 VARCHAR(500) default NULL, reserve2 VARCHAR(500) default NULL," +
        "PRIMARY KEY  (ids))",

        "CREATE TABLE tablelock (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "cname VARCHAR(250), rowID BIGINT NOT NULL, usersids BIGINT REFERENCES users (ids)  ON DELETE CASCADE," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
        "PRIMARY KEY  (ids))",

        "CREATE TABLE filestocontacts(IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"+
        "cname VARCHAR(250) NOT NULL, " +
        "description VARCHAR(550) DEFAULT NULL, " +
        "contactsids BIGINT NOT NULL  REFERENCES contacts(ids) ON DELETE CASCADE," +
        "filename VARCHAR(25) NOT NULL REFERENCES files(cname) ON DELETE CASCADE,"+
        "PRIMARY KEY  (ids))",
                
        "CREATE TABLE subitems (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
        "cname VARCHAR(250) default NULL," +
        "itemsids BIGINT REFERENCES items(ids)  ON DELETE CASCADE, " +
        "groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1," +
        "originalproductsids BIGINT DEFAULT NULL, " +
        "countvalue DOUBLE DEFAULT 0 NOT NULL, quantityvalue DOUBLE DEFAULT 0 NOT NULL, measure VARCHAR(250) NOT NULL," +
        "description VARCHAR(1000) default NULL,  value DOUBLE DEFAULT 0 NOT NULL, taxpercentvalue DOUBLE DEFAULT 0 NOT NULL," +
        "datedelivery DATE DEFAULT NULL, reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,PRIMARY KEY  (ids))",
       
        "CREATE TABLE addresses (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1," +
        "contactsids BIGINT REFERENCES contacts(ids)," +
        "title VARCHAR(250) default NULL, taxnumber VARCHAR(250)," +
        "prename VARCHAR(250) default NULL, cname VARCHAR(250) default NULL, street VARCHAR(250) default NULL," +
        "zip VARCHAR(50) default NULL,city VARCHAR(300) default NULL, " +
        "company VARCHAR(250) DEFAULT NULL, department VARCHAR(250) DEFAULT NULL," +
        "country VARCHAR(50) default NULL, "+
        "ismale SMALLINT DEFAULT 0," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
        "PRIMARY KEY  (ids))",

//Initial data
        "INSERT INTO groups (cname,description) VALUES ('ungrouped','This group holds all yet ungrouped items.')",
        "INSERT INTO users (fullname,password,cname,laf,locale,mail,language,inthighestright,datelastlog,isenabled ) VALUES ('Administrator','5f4dcc3b5aa765d61d8327deb882cf99','admin','de.muntjak.tinylookandfeel.TinyLookAndFeel','de_DE','','buildin_en',0,'2009-01-26 05:45:38',1)"

    };
    
    private String[] MYSQL_STRUCTURE;
    private String[] CUSTOM_STRUCTURE;

    public String[] getStructure() {
        if (ConnectionTypeHandler.getDriverType() == ConnectionTypeHandler.DERBY) {
            return DERBY_STRUCTURE;
        } else if (ConnectionTypeHandler.getDriverType() == ConnectionTypeHandler.DERBY) {
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
