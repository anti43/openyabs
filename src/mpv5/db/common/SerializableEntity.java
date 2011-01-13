/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.common;

/**
 *
 * @author Andreas
 */
public class SerializableEntity {

    private Long id;
    private String tablename;

    public SerializableEntity() {
    }

    public SerializableEntity(DatabaseObject.Entity v) {
        this.id = Long.valueOf(v.getValue().toString());
        this.tablename = v.getKey().getDbIdentity();
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the tablename
     */
    public String getTablename() {
        return tablename;
    }

    /**
     * @param tablename the tablename to set
     */
    public void setTablename(String tablename) {
        this.tablename = tablename;
    }
}
