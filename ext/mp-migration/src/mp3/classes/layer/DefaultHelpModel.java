/*
 * 
 * 
 */

package mp3.classes.layer;

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
