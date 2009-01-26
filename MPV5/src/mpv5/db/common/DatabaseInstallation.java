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
 *
 * @author anti43
 */
public class DatabaseInstallation {

    public final static String[] DERBY_STRUCTURE = new String[]{"CREATE TABLE contacts (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "cnumber VARCHAR(250),title VARCHAR(250) default NULL," +
        "prename VARCHAR(250) default NULL, cname VARCHAR(250) default NULL, street VARCHAR(250) default NULL," +
        "zip VARCHAR(50) default NULL,city VARCHAR(300) default NULL, mainphone VARCHAR(250) default NULL," +
        "fax VARCHAR(250) default NULL,mobilephone VARCHAR(250) default NULL,workphone VARCHAR(250) default NULL," +
        "mailaddress VARCHAR(350) default NULL,companyuid INTEGER default NULL," +
        "website VARCHAR(350) default NULL,notes VARCHAR(10000),taxid VARCHAR(350) default NULL," +
        "dateadded DATE DEFAULT CURRENT_DATE,isactive SMALLINT DEFAULT 0,iscustomer SMALLINT DEFAULT 0," +
        "ismanufacturer SMALLINT DEFAULT 0,issupplier SMALLINT DEFAULT 0,iscompany SMALLINT DEFAULT 0," +
        "ismale SMALLINT DEFAULT 0,isenabled SMALLINT DEFAULT 1,addedby VARCHAR(350) default NULL," +
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL," +
        "PRIMARY KEY  (ids))",

        "CREATE TABLE users (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"+
        "cname VARCHAR(250) UNIQUE NOT NULL, " +
        "password VARCHAR(250) NOT NULL,"+
        "laf VARCHAR(50) default NULL, "+
        "locale VARCHAR(50) default NULL, "+
        "mail VARCHAR(50) default NULL, "+
        "language VARCHAR(50) default NULL, "+
        "inthighestright SMALLINT DEFAULT 3,"+
        "isenabled SMALLINT DEFAULT 1,"+
        "isloggedin SMALLINT DEFAULT 1,"+
        "datelastlog DATE default NULL, "+
        "reserve1 VARCHAR(500) default NULL,reserve2 VARCHAR(500) default NULL,"+
        "PRIMARY KEY  (ids))",

        "INSERT INTO users (password,cname,laf,locale,mail,language,inthighestright,datelastlog ) VALUES ('5f4dcc3b5aa765d61d8327deb882cf99','admin','com.sun.java.swing.plaf.windows.WindowsLookAndFeel','de_DE','','EN',0,'2009-01-26 05:45:38')"
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
