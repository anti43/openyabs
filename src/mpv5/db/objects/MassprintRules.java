/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.db.objects;

import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Messages;

/**
 *
 *  
 */
public class MassprintRules extends DatabaseObject {
    
    private static final long serialVersionUID = 1L;
    private String content = "";
    private int inttype = 0;
    

    public MassprintRules() {
        setContext(Context.getMassprint());
    }
    
    static String getTypeString(int typ) {
        return Messages.TYPE_MASSPRINT.toString();
    }
   
    @Override
    public JComponent getView() {
        return null;
    }

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
    }

    public String __getContent() {
        return content;
    }

    public void setContent(String content_) {
        this.content = content_;
    }

    public int __getInttype() {
        return inttype;
    }

    public void setInttype(int inttype) {
        this.inttype = inttype;
    }
}
