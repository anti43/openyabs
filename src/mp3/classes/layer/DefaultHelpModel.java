

package mp3.classes.layer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mp4.utils.files.FileReaderWriter;

/**
 *
 * @author anti43
 */
public class DefaultHelpModel {
    private String thema;
    private String text;

    public DefaultHelpModel(String thema, String text){
        this.thema =thema;
        this.text=text;
    }
    
   public DefaultHelpModel(String thema, File file){
        this.thema = thema;
        try {
            this.text = new FileReaderWriter(file.getCanonicalPath()).read();
        } catch (IOException ex) {
            Logger.getLogger(DefaultHelpModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getThema() {
        return thema;
    }

    public void setThema(String thema) {
        this.thema = thema;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
