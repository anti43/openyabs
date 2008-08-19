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
package mp4.klassen.objekte;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

import mp3.classes.layer.Popup;
import mp3.classes.utils.Log;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.utils.datum.DateConverter;

/**
 *
 * @author anti43
 */
public class ProductImage extends mp3.classes.layer.Things implements mp4.datenbank.struktur.Tabellen {

    private Integer Produktid = null;
    private String Path = null;
    private Date Datum = new Date();
    public Integer id = 0;

    public Integer getId() {
        return id;
    }

    public void destroy() {
        this.delete(this.id);
        this.id = 0;
    }

    public ProductImage() {
        super(ConnectionHandler.instanceOf().clone(TABLE_PRODUCTS_FILES));
    }

    /**
     * 
     * @param id 
     */
    public ProductImage(Integer id) {
        super(ConnectionHandler.instanceOf().clone(TABLE_PRODUCTS_FILES));
        this.id = id;
        this.explode(this.selectLast("productid,url,datum", "id", id.toString(), true));
    }

    public boolean hasImage() {
        if (getPath() != null) {
            if (getImage() != null) {
                return true;
            }
        }
        return false;
    }

    public ProductImage searchImage(Integer id) {
        ProductImage image = null;
        String[] data = this.selectLast("productid,url,datum", "productid", id.toString(), true);
        if (data != null && data.length > 0) {
            image = new ProductImage();
            image.setProduktid(Integer.valueOf(data[0]));
            try {
                image.setPath(new File(data[1]).toURI());//.getPath().replaceAll(" ", "%20")
            } catch (Exception ex) {
                image.setPath(null);
                Log.Debug(ex);
            }
            image.setDatum(DateConverter.getDate(data[2]));
        }
        return image;
    }

    public URI getURI() {
        try {
            if (Path != null) {
                return new URI(Path.replaceAll(" ", "%20"));
            }
        } catch (Exception ex) {
            Log.Debug(ex);
        }
        return null;
    }

    private void explode(String[] select) {
        try {
            this.setProduktid(Integer.valueOf(select[0]));
            this.setPath(new File(select[1]).toURI());
            this.setDatum(DateConverter.getDate(select[2]));
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }

    private String collect() {
        String str = "";
        str = str + this.getProduktid() + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + this.getPath() + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
        str = str + "(;;2#4#1#1#8#0#;;)" + DateConverter.getSQLDateString(this.getDatum()) + "(;;2#4#1#1#8#0#;;)";
        return str;
    }

    public void save() {

        if (id > 0) {
            this.update(TABLE_PRODUCTS_FILES_FIELDS, this.collect(), id.toString());
            isSaved = true;
        } else if (id == 0) {
            this.id = this.insert(TABLE_PRODUCTS_FILES_FIELDS, this.collect());
        }
    }

    public Integer getProduktid() {
        return Produktid;
    }

    public void setProduktid(Integer Produktid) {
        this.Produktid = Produktid;
        this.isSaved = false;
    }

    public String getPath() {
        if (getURI() != null) {
            return getURI().getPath();
        } else {
            return null;
        }
    }

    public void setPath(URI ImageURI) {
        if (ImageURI != null) {
            this.Path = ImageURI.toString();
        }
        this.isSaved = false;
    }

    public Date getDatum() {
        return Datum;
    }

    public void setDatum(Date Datum) {
        this.Datum = Datum;
        this.isSaved = false;
    }

    public Image getImage() {
        Image coverImg = Toolkit.getDefaultToolkit().getImage("/bilder/medium/messagebox_warning.png");
        try {
            if (getPath() != null) {
                Log.Debug("Loading Image ..: " + getURI().getPath());
                coverImg = Toolkit.getDefaultToolkit().createImage(getURI().getPath());
                return coverImg;
            }
        } catch (Exception ex) {
            Popup.error(ex.getMessage(), "Das Bild konnte nicht geladen werden.");
        }
        return coverImg;
    }

    public ImageIcon getImageIcon() {
        Image coverImg;
        try {
            if (getPath() != null) {
                Log.Debug("Loading ImageIcon ..: " + getURI().getPath());
                coverImg = Toolkit.getDefaultToolkit().createImage(getURI().getPath());
                return new ImageIcon(coverImg);
            }
        } catch (Exception ex) {
            Popup.error(ex.getMessage(), "Das Bild konnte nicht geladen werden.");
        }
        return new javax.swing.ImageIcon(this.getClass().getResource("/bilder/medium/messagebox_warning.png"));
    }
}
