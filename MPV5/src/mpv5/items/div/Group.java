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
package mpv5.items.div;

import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;

/**
 *
 * @author anti
 */
public class Group extends DatabaseObject {

    private String description   = "";
    private String defaults = "";
    private int parentgroup = 0;


    public Group(){
        context.setDbIdentity(Context.IDENTITY_GROUPS);
        context.setIdentityClass(this.getClass());
    }

    @Override
    public String __getCName() {
        return cname;
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

    /**
     * @return the defaultvalue
     */
    public String __getDefaults() {
        return defaults;
    }

    /**
     * @param defaultvalue the defaultvalue to set
     */
    public void setDefaults(String defaultvalue) {
        this.defaults = defaultvalue;
    }

    /**
     * @return the parentgroup
     */
    public int __getParentgroup() {
        return parentgroup;
    }

    /**
     * @param parentgroup the parentgroup to set
     */
    public void setParentgroup(int parentgroup) {
        this.parentgroup = parentgroup;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(this);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.description != null ? this.description.hashCode() : 0);
        hash = 83 * hash + (this.defaults != null ? this.defaults.hashCode() : 0);
        hash = 83 * hash + this.parentgroup;
        return hash;
    }

    @Override
    public String toString(){
        return __getCName();
    }

}
