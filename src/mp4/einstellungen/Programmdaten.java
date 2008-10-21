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
import mp4.plugin.mpplugin;

/**
 *
 * @author Andreas
 */
public class Programmdaten implements mp4.datenbank.installation.Tabellen {

    private DataHandler pluginhandler;
    private DataHandler datahandler;
    private static Programmdaten dat;

    public static Programmdaten instanceOf() {
        if (dat == null) {
            dat = new Programmdaten();
            return dat;
        }
        return dat;
    }

    public Programmdaten() {
        datahandler = new DataHandler(TABLE_PROG_DATA);
        pluginhandler = new DataHandler(TABLE_PLUGINS);
    }

    public void addONLOAD_PLUGIN(mpplugin elem) {
        pluginhandler.addRow(elem.getUID().toString(), null);
    }

    public boolean getONLOAD(mpplugin elem) {
        if (pluginhandler.getString(elem.getUID().toString()) != null) {
            return true;
        } else {
            return false;
        }
    }

    public void removeONLOAD_PLUGIN(mpplugin elem) {
        pluginhandler.deleteRow(elem.getUID().toString());
    }

    public File getDIR_CACHE() {
        return new File(datahandler.getString("IMAGE_CACHE_FOLDER"));
    }

    public Integer getPRODUCTPANEL_CHECKBOX_SCALEIMAGE_SIZE() {
        return datahandler.getInteger("PRODUCTPANEL_CHECKBOX_SCALEIMAGE_SIZE");
    }

    public boolean getPRODUCTPANEL_CHECKBOX_SCALEIMAGE_state() {
        return datahandler.getBoolean("PRODUCTPANEL_CHECKBOX_SCALEIMAGE");
    }

    public String getWARENGRUPPEN_SEPARATOR() {
        return datahandler.getString("WARENGRUPPEN_SEPARATOR");
    }

    public void setBILLPANEL_CHECKBOX_MITFIRMENNAME(boolean BILLPANEL_CHECKBOX_MITFIRMENNAME) {
        datahandler.setBoolean("BILLPANEL_CHECKBOX_MITFIRMENNAME", BILLPANEL_CHECKBOX_MITFIRMENNAME);
    }

    public boolean getBILLPANEL_CHECKBOX_MITFIRMENNAME_state() {
        return datahandler.getBoolean("BILLPANEL_CHECKBOX_MITFIRMENNAME");
    }

    public void setPRODUCTPICKER_EAN(boolean PRODUCTPICKER_EAN) {
        datahandler.setBoolean("PRODUCTPICKER_EAN", PRODUCTPICKER_EAN);
    }

    public void setPRODUCTPICKER_NAME(boolean PRODUCTPICKER_NAME) {
        datahandler.setBoolean("PRODUCTPICKER_NAME", PRODUCTPICKER_NAME);
    }

    public void setPRODUCTPICKER_TEXT(boolean PRODUCTPICKER_TEXT) {
        datahandler.setBoolean("PRODUCTPICKER_TEXT", PRODUCTPICKER_TEXT);
    }

    public boolean getPRODUCTPICKER_EAN() {
        return datahandler.getBoolean("PRODUCTPICKER_EAN");
    }

    public boolean getPRODUCTPICKER_NAME() {
        return datahandler.getBoolean("PRODUCTPICKER_NAME");
    }

    public boolean getPRODUCTPICKER_TEXT() {
        return datahandler.getBoolean("PRODUCTPICKER_TEXT");
    }

    public void setSERVICES_NUMMER_FORMAT(String SERVICES_NUMMER_FORMAT) {
        datahandler.setString("SERVICES_NUMMER_FORMAT", SERVICES_NUMMER_FORMAT);
    }

    public String getSERVICES_NUMMER_FORMAT() {
        return datahandler.getString("SERVICES_NUMMER_FORMAT");
    }

    public void setKunde_NUMMER_FORMAT(String Kunde_NUMMER_FORMAT) {
        datahandler.setString("Kunde_NUMMER_FORMAT", Kunde_NUMMER_FORMAT);
    }

    public String getPRODUCT_NUMMER_FORMAT() {
        return datahandler.getString("PRODUCT_NUMMER_FORMAT");
    }

    public void setPRODUCT_NUMMER_FORMAT(String PRODUCT_NUMMER_FORMAT) {
        datahandler.setString("PRODUCT_NUMMER_FORMAT", PRODUCT_NUMMER_FORMAT);
    }

    public String getKunde_NUMMER_FORMAT() {
        return datahandler.getString("Kunde_NUMMER_FORMAT");
    }

    public void setSUPPLIER_NUMMER_FORMAT(String SUPPLIER_NUMMER_FORMAT) {
        datahandler.setString("SUPPLIER_NUMMER_FORMAT", SUPPLIER_NUMMER_FORMAT);
    }

    public String getSUPPLIER_NUMMER_FORMAT() {
        return datahandler.getString("SUPPLIER_NUMMER_FORMAT");
    }

    public void setMANUFACTURER_NUMMER_FORMAT(String MANUFACTURER_NUMMER_FORMAT) {
        datahandler.setString("MANUFACTURER_NUMMER_FORMAT", MANUFACTURER_NUMMER_FORMAT);
    }

    public String getMANUFACTURER_NUMMER_FORMAT() {
        return datahandler.getString("MANUFACTURER_NUMMER_FORMAT");
    }

    public void setWARENGRUPPEN_SEPARATOR(String WARENGRUPPEN_SEPARATOR) {
        datahandler.setString("WARENGRUPPEN_SEPARATOR", WARENGRUPPEN_SEPARATOR);
    }

    public void setPRODUCTPANEL_CHECKBOX_SCALEIMAGE(boolean PRODUCTPANEL_CHECKBOX_SCALEIMAGE) {
        datahandler.setBoolean("PRODUCTPANEL_CHECKBOX_SCALEIMAGE", PRODUCTPANEL_CHECKBOX_SCALEIMAGE);
    }

    public void setPRODUCTPANEL_CHECKBOX_SCALEIMAGE_SIZE(Integer PRODUCTPANEL_CHECKBOX_SCALEIMAGE_SIZE) {
        datahandler.setInteger("PRODUCTPANEL_CHECKBOX_SCALEIMAGE_SIZE", PRODUCTPANEL_CHECKBOX_SCALEIMAGE_SIZE);
    }

    public boolean getUSE_AUTHENTIFICATION() {
        return datahandler.getBoolean("USE_AUTHENTIFICATION");
    }

    public void setUSE_AUTHENTIFICATION(boolean USE_AUTHENTIFICATION) {
        datahandler.setBoolean("USE_AUTHENTIFICATION", USE_AUTHENTIFICATION);
    }

    public boolean getBILLPANEL_CHECKBOX_MITLIEFERSCHEIN_state() {
        return datahandler.getBoolean("BILLPANELCHECKBOXMITLIEFERSCHEIN");
    }

    public void setBILLPANEL_CHECKBOX_MITLIEFERSCHEIN(boolean BILLPANEL_CHECKBOX_MITLIEFERSCHEIN) {
        datahandler.setBoolean("BILLPANELCHECKBOXMITLIEFERSCHEIN", BILLPANEL_CHECKBOX_MITLIEFERSCHEIN);
    }

    public boolean getBILLPANEL_CHECKBOX_MITANEGBOT_state() {
        return datahandler.getBoolean("BILLPANEL_CHECKBOX_MITANGEBOT");
    }

    public void setBILLPANEL_CHECKBOX_MITANGEBOT(boolean BILLPANEL_CHECKBOX_MITANGEBOT) {
        datahandler.setBoolean("BILLPANEL_CHECKBOX_MITANGEBOT", BILLPANEL_CHECKBOX_MITANGEBOT);
    }

    public void setBILLPANEL_CHECKBOX_NETTOPREISE(boolean BILLPANEL_CHECKBOX_NETTOPREISE) {
        datahandler.setBoolean("BILLPANEL_CHECKBOX_NETTOPREISE", BILLPANEL_CHECKBOX_NETTOPREISE);
    }

    public boolean getBILLPANEL_CHECKBOX_NETTOPREISE_state() {
        return datahandler.getBoolean("BILLPANEL_CHECKBOX_NETTOPREISE");
    }

    public Integer getMAINFRAME_TAB() {
        return datahandler.getInteger("MAINFRAME_TAB");
    }

    public void setMAINFRAME_TAB(Integer MAINFRAME_TAB) {
        datahandler.setInteger("MAINFRAME_TAB", MAINFRAME_TAB);
    }

    public Dimension getMAINFRAME_WINDOW_STATE() {
        return new Dimension(Integer.valueOf(datahandler.getString("MAINFRAME_WINDOW_STATE").split(",")[1]), Integer.valueOf(datahandler.getString("MAINFRAME_WINDOW_STATE").split(",")[0]));
    }

    public void setMAINFRAME_WINDOW_STATE(Dimension MAINFRAME_WINDOW_STATE) {
        datahandler.setString("MAINFRAME_WINDOW_STATE", (int) MAINFRAME_WINDOW_STATE.getHeight() + "," + (int) MAINFRAME_WINDOW_STATE.getWidth());
    }

    public String getMAHNUNG_TEXT_DEFAULT() {
        return datahandler.getString("MAHNUNG_TEXT_DEFAULT");
    }

    public void setMAHNUNG_TEXT_DEFAULT(String MAHNUNG_TEXT_DEFAULT) {
        datahandler.setString("MAHNUNG_TEXT_DEFAULT", MAHNUNG_TEXT_DEFAULT);
    }

    public String getRECHNUNG_NUMMER_FORMAT() {
        return datahandler.getString("RECHNUNG_NUMMER_FORMAT");
    }

    public void setRECHNUNG_NUMMER_FORMAT(String RECHNUNG_NUMMER_FORMAT) {
        datahandler.setString("RECHNUNG_NUMMER_FORMAT", RECHNUNG_NUMMER_FORMAT);
    }

    public String getANGEBOT_NUMMER_FORMAT() {
        return datahandler.getString("ANGEBOT_NUMMER_FORMAT");
    }

    public void setANGEBOT_NUMMER_FORMAT(String ANGEBOT_NUMMER_FORMAT) {
        datahandler.setString("ANGEBOT_NUMMER_FORMAT", ANGEBOT_NUMMER_FORMAT);
    }

    public String getIMAGE_CACHE_FOLDER() {
        return datahandler.getString("IMAGE_CACHE_FOLDER");
    }

    public void setIMAGE_CACHE_FOLDER(String IMAGE_CACHE_FOLDER) {
        datahandler.setString("IMAGE_CACHE_FOLDER", IMAGE_CACHE_FOLDER);
    }

    public String getPLUGIN_FOLDER() {
        return datahandler.getString("PLUGIN_FOLDER");
    }

    public void setPLUGIN_FOLDER(String PLUGIN_FOLDER) {
        datahandler.setString("PLUGIN_FOLDER", PLUGIN_FOLDER);
    }

    public void setBILLPANEL_MASK(String BILLPANEL_MASK) {
        datahandler.setString("BILLPANEL_MASK", BILLPANEL_MASK);
    }

    public String getBILLPANEL_MASK() {
        return datahandler.getString("BILLPANEL_MASK");
    }

    public String getSERVICEPANEL_MASK() {
        return datahandler.getString("SERVICEPANEL_MASK");
    }

    public void setSERVICEPANEL_MASK(String SERVICEPANEL_MASK) {
        datahandler.setString("SERVICEPANEL_MASK", SERVICEPANEL_MASK);
    }
}
