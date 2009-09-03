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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.logging.Log;
import mpv5.utils.images.MPIcon;

/**
 *
 *  
 */
public class Stage extends DatabaseObject {

    private String description = "";
    private double extravalue;

    public Stage() {
        context.setDbIdentity(Context.IDENTITY_STAGES);
        context.setIdentityClass(this.getClass());
    }

    @Override
    public String __getCName() {
        return cname;
    }

    @Override
    public JComponent getView() {
        return null;
    }

    @Override
    public void setCName(String name) {
        cname = name;
    }

    /**
     * @return the description
     */
    public String __getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    MPIcon icon;

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
    }

    /**
     * @return the extravalue
     */
    public double __getExtravalue() {
        return extravalue;
    }

    /**
     * @param extravalue the extravalue to set
     */
    public void setExtravalue(double extravalue) {
        this.extravalue = extravalue;
    }

}
