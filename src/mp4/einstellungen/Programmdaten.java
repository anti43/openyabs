/*
 *  This file is part of MP by anti43 /GPL.
 *  
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *  
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *  
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mp4.einstellungen;

import java.awt.Dimension;
import java.io.File;

/**
 *
 * @author Andreas
 */
public class Programmdaten implements mp4.datenbank.installation.Tabellen {

    private DataHandler handler;
    private static Programmdaten dat;

    public static Programmdaten instanceOf() {
        if (dat == null) {
            dat = new Programmdaten();
            return dat;
        }
        return dat;
    }

    public Programmdaten() {
        handler = new DataHandler(TABLE_PROG_DATA);

    }

    public File getDIR_CACHE() {
        return new File(handler.getString("IMAGE_CACHE_FOLDER"));
    }

    public Integer getPRODUCTPANEL_CHECKBOX_SCALEIMAGE_SIZE() {
        return handler.getInteger("PRODUCTPANEL_CHECKBOX_SCALEIMAGE_SIZE");
    }

    public boolean getPRODUCTPANEL_CHECKBOX_SCALEIMAGE_state() {
        return handler.getBoolean("PRODUCTPANEL_CHECKBOX_SCALEIMAGE");
    }

    public String getWARENGRUPPEN_SEPARATOR() {
        return handler.getString("WARENGRUPPEN_SEPARATOR");
    }

    public void setPRODUCTPICKER_EAN(boolean PRODUCTPICKER_EAN) {
        handler.setBoolean("PRODUCTPICKER_EAN", PRODUCTPICKER_EAN);
    }

    public void setPRODUCTPICKER_NAME(boolean PRODUCTPICKER_NAME) {
       handler.setBoolean("PRODUCTPICKER_NAME", PRODUCTPICKER_NAME);
    }

    public void setPRODUCTPICKER_TEXT(boolean PRODUCTPICKER_TEXT) {
        handler.setBoolean("PRODUCTPICKER_TEXT", PRODUCTPICKER_TEXT);
    }
    
   public boolean getPRODUCTPICKER_EAN() {
       return handler.getBoolean("PRODUCTPICKER_EAN");
    }

    public boolean getPRODUCTPICKER_NAME() {
       return handler.getBoolean("PRODUCTPICKER_NAME");
    }

    public boolean getPRODUCTPICKER_TEXT() {
        return handler.getBoolean("PRODUCTPICKER_TEXT");
    }

    public void setSERVICES_NUMMER_FORMAT(String SERVICES_NUMMER_FORMAT) {
        handler.setString("SERVICES_NUMMER_FORMAT", SERVICES_NUMMER_FORMAT);
    }

    public String getSERVICES_NUMMER_FORMAT() {
        return handler.getString("SERVICES_NUMMER_FORMAT");
    }

    public void setCUSTOMER_NUMMER_FORMAT(String CUSTOMER_NUMMER_FORMAT) {
        handler.setString("CUSTOMER_NUMMER_FORMAT", CUSTOMER_NUMMER_FORMAT);
    }

    public String getPRODUCT_NUMMER_FORMAT() {
        return handler.getString("PRODUCT_NUMMER_FORMAT");
    }

    public void setPRODUCT_NUMMER_FORMAT(String PRODUCT_NUMMER_FORMAT) {
        handler.setString("PRODUCT_NUMMER_FORMAT", PRODUCT_NUMMER_FORMAT);
    }

    public String getCUSTOMER_NUMMER_FORMAT() {
        return handler.getString("CUSTOMER_NUMMER_FORMAT");
    }

    public void setSUPPLIER_NUMMER_FORMAT(String SUPPLIER_NUMMER_FORMAT) {
        handler.setString("SUPPLIER_NUMMER_FORMAT", SUPPLIER_NUMMER_FORMAT);
    }

    public String getSUPPLIER_NUMMER_FORMAT() {
        return handler.getString("SUPPLIER_NUMMER_FORMAT");
    }

    public void setMANUFACTURER_NUMMER_FORMAT(String MANUFACTURER_NUMMER_FORMAT) {
        handler.setString("MANUFACTURER_NUMMER_FORMAT", MANUFACTURER_NUMMER_FORMAT);
    }

    public String getMANUFACTURER_NUMMER_FORMAT() {
        return handler.getString("MANUFACTURER_NUMMER_FORMAT");
    }

    public void setWARENGRUPPEN_SEPARATOR(String WARENGRUPPEN_SEPARATOR) {
        handler.setString("WARENGRUPPEN_SEPARATOR", WARENGRUPPEN_SEPARATOR);
    }

    public void setPRODUCTPANEL_CHECKBOX_SCALEIMAGE(boolean PRODUCTPANEL_CHECKBOX_SCALEIMAGE) {
        handler.setBoolean("PRODUCTPANEL_CHECKBOX_SCALEIMAGE", PRODUCTPANEL_CHECKBOX_SCALEIMAGE);
    }

    public void setPRODUCTPANEL_CHECKBOX_SCALEIMAGE_SIZE(Integer PRODUCTPANEL_CHECKBOX_SCALEIMAGE_SIZE) {
        handler.setInteger("PRODUCTPANEL_CHECKBOX_SCALEIMAGE_SIZE", PRODUCTPANEL_CHECKBOX_SCALEIMAGE_SIZE);
    }

    public boolean getUSE_AUTHENTIFICATION() {
        return handler.getBoolean("USE_AUTHENTIFICATION");
    }

    public void setUSE_AUTHENTIFICATION(boolean USE_AUTHENTIFICATION) {
        handler.setBoolean("USE_AUTHENTIFICATION", USE_AUTHENTIFICATION);
    }

    public boolean getBILLPANEL_CHECKBOX_MITLIEFERSCHEIN_state() {
        return handler.getBoolean("BILLPANELCHECKBOXMITLIEFERSCHEIN");
    }

    public void setBILLPANEL_CHECKBOX_MITLIEFERSCHEIN(boolean BILLPANEL_CHECKBOX_MITLIEFERSCHEIN) {
        handler.setBoolean("BILLPANELCHECKBOXMITLIEFERSCHEIN", BILLPANEL_CHECKBOX_MITLIEFERSCHEIN);
    }

    public Integer getMAINFRAME_TAB() {
        return handler.getInteger("MAINFRAME_TAB");
    }

    public void setMAINFRAME_TAB(Integer MAINFRAME_TAB) {
        handler.setInteger("MAINFRAME_TAB", MAINFRAME_TAB);
    }

    public Dimension getMAINFRAME_WINDOW_STATE() {
        return new Dimension(Integer.valueOf(handler.getString("MAINFRAME_WINDOW_STATE").split(",")[1]), Integer.valueOf(handler.getString("MAINFRAME_WINDOW_STATE").split(",")[0]));
    }

    public void setMAINFRAME_WINDOW_STATE(Dimension MAINFRAME_WINDOW_STATE) {
        handler.setString("MAINFRAME_WINDOW_STATE", (int) MAINFRAME_WINDOW_STATE.getHeight() + "," + (int) MAINFRAME_WINDOW_STATE.getWidth());
    }

    public String getMAHNUNG_TEXT_DEFAULT() {
        return handler.getString("MAHNUNG_TEXT_DEFAULT");
    }

    public void setMAHNUNG_TEXT_DEFAULT(String MAHNUNG_TEXT_DEFAULT) {
        handler.setString("MAHNUNG_TEXT_DEFAULT", MAHNUNG_TEXT_DEFAULT);
    }

    public String getRECHNUNG_NUMMER_FORMAT() {
        return handler.getString("RECHNUNG_NUMMER_FORMAT");
    }

    public void setRECHNUNG_NUMMER_FORMAT(String RECHNUNG_NUMMER_FORMAT) {
        handler.setString("RECHNUNG_NUMMER_FORMAT", RECHNUNG_NUMMER_FORMAT);
    }

    public String getANGEBOT_NUMMER_FORMAT() {
        return handler.getString("ANGEBOT_NUMMER_FORMAT");
    }

    public void setANGEBOT_NUMMER_FORMAT(String ANGEBOT_NUMMER_FORMAT) {
        handler.setString("ANGEBOT_NUMMER_FORMAT", ANGEBOT_NUMMER_FORMAT);
    }

    public String getIMAGE_CACHE_FOLDER() {
        return handler.getString("IMAGE_CACHE_FOLDER");
    }

    public void setIMAGE_CACHE_FOLDER(String IMAGE_CACHE_FOLDER) {
        handler.setString("IMAGE_CACHE_FOLDER", IMAGE_CACHE_FOLDER);
    }
}
