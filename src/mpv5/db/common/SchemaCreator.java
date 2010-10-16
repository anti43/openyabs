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
        if (ConnectionTypeHandler.getDriverType() == ConnectionTypeHandler.DERBY) {
            createTableDerby(cols, newObject.getClass().getSimpleName());
        } else {
            createTableMySQL(cols, newObject.getClass().getSimpleName());
        }
    }

    private static void createTableDerby(List<column> cols, String tableName) {
//CREATE TABLE tax (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), cname VARCHAR(250), taxvalue DOUBLE DEFAULT 0,identifier VARCHAR(250) DEFAULT NULL, groupsids BIGINT  REFERENCES groups(ids) DEFAULT 1,country VARCHAR(50) DEFAULT NULL, dateadded DATE NOT NULL,intaddedby BIGINT DEFAULT 0, invisible SMALLINT DEFAULT 0, reserve1 VARCHAR(500) DEFAULT NULL,reserve2 VARCHAR(500) DEFAULT NULL,PRIMARY KEY  (ids))",
        String query = "CREATE TABLE " + tableName + " (IDS BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)";
        for (int i = 0; i < cols.size(); i++) {
            column elem = cols.get(i);
            query+= ", " + elem.name;
            if(Number.class.isAssignableFrom(elem.type)){
                 query+= ", " + elem.name;

            }
        }
        query += ")";
    }

    private static void createTableMySQL(List<column> cols, String tableName) {
        throw new UnsupportedOperationException("Not yet implemented");
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
