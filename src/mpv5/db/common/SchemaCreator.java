package mpv5.db.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import mpv5.db.common.DatabaseObject.*;
import mpv5.logging.Log;

/**
 *
 * @author anti
 */
public class SchemaCreator {

    public static boolean schemaExists(DatabaseObject checkObject) {
        try {
            QueryHandler qh = QueryHandler.instanceOf().clone(checkObject.getClass().getSimpleName());
            qh.setLimit(1);
            qh.freeSelect("ids");
        } catch (NodataFoundException nodataFoundException) {
            //thats actually ok, table exist
        } catch (Exception s) {
            //whoops -- java.sql.SQLSyntaxErrorException: Table/View 'HURZ' does not exist
            Log.Debug(s);
            return false;
        }
        return true;
    }

    public static void createSchema(DatabaseObject newObject) {
        List<Method> ms = newObject.setVars();
        List<column> cols = new ArrayList<column>();
        for (Method m : ms) {
            if ((m.isAnnotationPresent(Persistable.class) && m.getAnnotation(Persistable.class).value())) {
                Class type = m.getReturnType();
                String name = m.getName().substring(3);
                Context references = null;
                boolean cascade = false;
                if ((m.isAnnotationPresent(References.class))) {
                    references = Context.getByID(m.getAnnotation(References.class).value());
                    if ((m.isAnnotationPresent(Cascade.class) && m.getAnnotation(Cascade.class).value())) {
                        cascade = true;
                    }
                }
                cols.add(new column(name, type, references, cascade));
            }
        }
    }

    static class column {

        public column(String name, Class type, Context reference, boolean cascades) {
            this.name = name;
            this.type = type;
            this.reference = reference;
            this.cascades = cascades;
        }
        String name;
        Context reference;
        boolean cascades;
        Class type;
    }
}
