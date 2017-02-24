/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.xdocreport;

import com.lowagie.text.Font;
import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import java.awt.Color;

/**
 *
 * @author dev
 */
public class YabsFontProvider implements IFontProvider {

    @Override
    public Font getFont(String familyName, String encoding, float size, int style, Color color) {
        //Font font = new Font(familyName,size,style,color);
        
        return null;
    }

}
