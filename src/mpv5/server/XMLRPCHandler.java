package mpv5.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.logging.Log;
import mpv5.usermanagement.MPSecurityManager;

/**
 *
 * @author andreasw
 */
public class XMLRPCHandler {

    public String[] getAvailableContexts() {
        ArrayList<Context> c = Context.getImportableContexts();
        String[] s = new String[c.size()];
        for (int i = 0; i < c.size(); i++) {
            s[i] = c.get(i).getDbIdentity();
        }
        return s;
    }
    
    public boolean login(String user, String passw) throws Exception {
        Log.Debug(this, "Login " + user);
        return MPSecurityManager.checkAuth(user, passw) != null;
    }

    public HashMap<String, Object> getObjects(String context, int startid, int endid) throws Exception {
        Log.Debug(this, "Getting " + context + " ids: " + startid + " to " + endid);
        HashMap<String, Object> m = new HashMap<String, Object>();
        for (int i = startid; i <= endid; i++) {
            try {
                DatabaseObject d = DatabaseObject.getObject(Context.getMatchingContext(context), i);
                HashMap<String, Object> m2 = new HashMap<String, Object>();
                List<Object[]> l = d.getValues2();
                for (Object[] objects : l) {
                    m2.put(objects[0].toString(), objects[1]);
                }
                m.put(context + " [" + i + "]", m2);
            } catch (NodataFoundException nodataFoundException) {
                Log.Debug(this, nodataFoundException);
            }
        }
        return m;
    }

    public HashMap<String, Object> getObject(String context, int id) throws Exception {
        Log.Debug(this, "Getting " + context + " id: " + id);
        DatabaseObject d = DatabaseObject.getObject(Context.getMatchingContext(context), id);
        HashMap<String, Object> m = new HashMap<String, Object>();
        List<Object[]> l = d.getValues2();
        for (Object[] objects : l) {
            m.put(objects[0].toString(), objects[1]);
        }
        return m;
    }

    public HashMap<String, Object> getObject(String context, String cname) throws Exception {
        Log.Debug(this, "Getting " + context + " cname: " + cname);
        DatabaseObject d = DatabaseObject.getObject(Context.getMatchingContext(context), cname);
        HashMap<String, Object> m = new HashMap<String, Object>();
        List<Object[]> l = d.getValues2();
        for (Object[] objects : l) {
            m.put(objects[0].toString(), objects[1]);
        }
        return m;
    }

    public boolean addObject(String context, HashMap<String, Object> data) throws Exception {
        Log.Debug(this, "Adding " + context + " data: " + data);
        DatabaseObject d = DatabaseObject.getObject(Context.getMatchingContext(context));
        d.parse(data);
        d.setIDS(-1);
        return d.saveImport();
    }

    public boolean updateObject(String context, int id, HashMap<String, Object> data) throws Exception {
        Log.Debug(this, "Updating " + context + " id: " + id + " data: " + data);
        DatabaseObject d = DatabaseObject.getObject(Context.getMatchingContext(context), id);
        d.parse(data);
        d.setIDS(id);
        return d.save();
    }

    public boolean deleteObject(String context, int id) throws Exception {
        Log.Debug(this, "Deleting " + context + " id: " + id);
        return DatabaseObject.getObject(Context.getMatchingContext(context), id).delete();
    }
}
