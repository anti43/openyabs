/*
 * 
 *  *  This file is part of MP by anti43 /GPL.
 *  *  
 *  *      MP is free software: you can redistribute it and/or modify
 *  *      it under the terms of the GNU General Public License as published by
 *  *      the Free Software Foundation, either version 3 of the License, or
 *  *      (at your option) any later version.
 *  *  
 *  *      MP is distributed in the hope that it will be useful,
 *  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 * *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *      GNU General Public License for more details.
 *  *  
 *  *      You should have received a copy of the GNU General Public License
 *  *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package mp3.classes.interfaces;

import java.io.File;

/**
 *
 * @author anti43
 */
public interface Constants {
    
     /**
     * Version info
     */
    public static String VERSION = "3.61";
    public static String TITLE = "\"DustBunny\"";
    public static String VERSION_URL = "Not Used";

    
     /**
     * Home directory of user
     */
    public final String HOME = System.getProperty("user.home");
    
     /**
     * Name of databasedir 
     */
    public static final String DATABASENAME = "mpv36Database";
   
     /**
     * Full path to database dir 
     */
    public static final String DATABASEPATH = Constants.HOME + File.separator +".mp" ;
    
    /**
     * Full path to settings file 
     */
    public static String SETTINGS  = Constants.HOME + File.separator +".mp" + File.separator + "settings36.mp";

    
    public String[] MONTHS= new String[]{"Jan","Feb","Mar","Apr","Mai","Jun","Jul","Aug","Sep","Okt","Nov","Dez"};
}
