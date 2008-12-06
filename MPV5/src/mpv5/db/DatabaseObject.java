/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 *
 * @author Administrator
 */
public abstract class DatabaseObject {

    public Context context;
    public Integer id = 0;
    public boolean isSaved = false;
    public boolean readonly = false;
    public boolean active = true;

    public ArrayList<Method> getVars() {
        ArrayList<Method> list = new ArrayList<Method>();
        for (int i = 0; i < this.getClass().getMethods().length; i++) {
            if (this.getClass().getMethods()[i].getName().startsWith("get") || this.getClass().getMethods()[i].getName().startsWith("is")) {
                list.add(this.getClass().getMethods()[i]);
            }
        }
        return list;
    }

    public boolean save() {
        return false;
    }


    public boolean reset() {
        return false;
    }


    public boolean delete() {
        return false;
    }

}
