package mp4.utils.tabellen.models;

/**
 * Tablemodel for bills and orders
 * @author anti43
 */
public class PostenTableModel extends MPTableModel {

    private static final long serialVersionUID = -2651695142723361873L;

    public PostenTableModel(Object[][] data, Object[] columnNames) {
        super(new Class[]{java.lang.Integer.class, java.lang.Double.class,
                    java.lang.String.class, java.lang.Double.class, java.lang.Double.class,
                    java.lang.Double.class
                }, new boolean[]{true,true,true,true,true,true},
                data, columnNames);
    }
}
