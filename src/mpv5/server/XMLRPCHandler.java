package mpv5.server;

import java.util.ArrayList;
import java.util.HashMap;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.logging.Log;

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

    public HashMap<String, Object> getObjects(String context, int startid, int endid) throws Exception {
        HashMap<String, Object> m = new HashMap<String, Object>();
        for (int i = startid; i <= endid; i++) {
            try {
                DatabaseObject d = DatabaseObject.getObject(Context.getMatchingContext(context), i);
                HashMap<String, Object> m2 = new HashMap<String, Object>();
                ArrayList<Object[]> l = d.getValues2();
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
        DatabaseObject d = DatabaseObject.getObject(Context.getMatchingContext(context), id);
        HashMap<String, Object> m = new HashMap<String, Object>();
        ArrayList<Object[]> l = d.getValues2();
        for (Object[] objects : l) {
            m.put(objects[0].toString(), objects[1]);
        }
        return m;
    }

    public HashMap<String, Object> getObject(String context, String cname) throws Exception {
        DatabaseObject d = DatabaseObject.getObject(Context.getMatchingContext(context), cname);
        HashMap<String, Object> m = new HashMap<String, Object>();
        ArrayList<Object[]> l = d.getValues2();
        for (Object[] objects : l) {
            m.put(objects[0].toString(), objects[1]);
        }
        return m;
    }

    public boolean addObject(String context, HashMap<String, Object> data) throws Exception {
        DatabaseObject d = DatabaseObject.getObject(Context.getMatchingContext(context));
        d.parse(data);
        d.setIDS(-1);
        d.saveImport();
        return true;
    }

     public boolean updateObject(String context, int id, HashMap<String, Object> data) throws Exception {
        DatabaseObject d = DatabaseObject.getObject(Context.getMatchingContext(context), id);
        d.parse(data);
        d.setIDS(id);
        d.save(true);
        return true;
    }

    public boolean deleteObject(String context, int id) throws Exception {
        return DatabaseObject.getObject(Context.getMatchingContext(context), id).delete();
    }
}
