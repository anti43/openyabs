/*
 * 
This file is part of YaBS.

YaBS is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

YaBS is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with YaBS.  If not, see <http://www.gnu.org/licenses/>.

 * 
 */
package mpv5.utils.date;


import java.util.Date;

/**
 *
 * 
 */
public class vDate {
    private boolean isVerified = false;
    private String defDate = null;
    private String sqlDate = null;
    private Date date = null;
    private String ovalue = "";

    public vDate(String date) {

        ovalue = date;

        try {
            this.date = DateConverter.getDate(date);
            defDate = DateConverter.getDefDateString(this.date);
            sqlDate = DateConverter.getSQLDateString(this.date);

            if (this.date != null && defDate != null && sqlDate != null) {
                this.isVerified = true;
            }
        } catch (Exception e) {
            this.isVerified = false;
        }

    }

    /**
     * @return the isVerified
     */
    public boolean isIsVerified() {
        return isVerified;
    }

    /**
     * @return the defDate
     */
    public String getDefDate() {
        return defDate;
    }

    /**
     * @return the sqlDate
     */
    public String getSqlDate() {
        return sqlDate;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @return the ovalue
     */
    public String getOvalue() {
        return ovalue;
    }
}
