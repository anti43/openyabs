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

/**
 *
 * @author Andreas
 */
public class Programmdaten implements mp4.datenbank.struktur.Tabellen {

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

    public Integer getPRODUCTPANEL_CHECKBOX_SCALEIMAGE_SIZE() {
         return handler.getInteger("PRODUCTPANEL_CHECKBOX_SCALEIMAGE_SIZE");
    }

    public boolean getPRODUCTPANEL_CHECKBOX_SCALEIMAGE_state() {
        return handler.getBoolean("PRODUCTPANEL_CHECKBOX_SCALEIMAGE");
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
