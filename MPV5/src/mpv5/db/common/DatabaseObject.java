/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.logging.Log;

/**
 *
 * @author Administrator
 */
public abstract class DatabaseObject {

    public Context context = new Context(this);
    public Integer id = 0;
    public boolean isSaved = false;
    public boolean readonly = false;
    public boolean active = true;

    public abstract String getName();

    public Integer getID() {
        return id;
    }

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

        try {
            if (id <= 0) {
                id = QueryHandler.instanceOf().clone(context).insert(collect());
            } else {
                QueryHandler.instanceOf().clone(context).update(collect(), new String[]{"id", String.valueOf(id), ""});
            }
            return true;
        } catch (Exception e) {
            Log.Debug(this, e);
            e.printStackTrace();
            return false;
        }
    }

    public boolean reset() {
        return false;
    }

    public boolean delete() {
        return false;
    }

    public String getDbID() {
        return context.getDbIdentity();
    }

    private String[] collect() {

        String left = "";
        String right = "";
        Object tempval;
        int intval = 0;
        String stringval = "";

        for (int i = 0; i < this.getClass().getMethods().length; i++) {
            if (this.getClass().getMethods()[i].getName().startsWith("get") && !this.getClass().getMethods()[i].getName().endsWith("Class")) {
                try {
                    left += this.getClass().getMethods()[i].getName().substring(3, this.getClass().getMethods()[i].getName().length()) + ",";
                    tempval = this.getClass().getMethods()[i].invoke(this, (Object[]) null);
                    if (tempval.getClass().isInstance(String.class)) {
                        stringval = (String) tempval;
                    }
                    right += "(;;2#4#1#1#8#0#;;)" + stringval + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (this.getClass().getMethods()[i].getName().startsWith("is")) {
                try {
                    left += this.getClass().getMethods()[i].getName().substring(2, this.getClass().getMethods()[i].getName().length()) + ",";
                    tempval = this.getClass().getMethods()[i].invoke(this, (Object[]) null);
                    if (tempval.getClass().isInstance(boolean.class)) {
                        if (((Boolean) tempval)) {
                            intval = 1;
                        }
                    }
                    right += "(;;2#4#1#1#8#0#;;)" + intval + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(DatabaseObject.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return new String[]{left.substring(0, left.length()-1), right.substring(0, right.length()-7), ""};
    }
}
