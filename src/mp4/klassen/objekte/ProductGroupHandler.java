/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mp4.klassen.objekte;

import java.util.ArrayList;
import mp4.datenbank.verbindung.Query;


import mp3.classes.utils.Log;
import mp4.datenbank.verbindung.ConnectionHandler;

/**
 *
 * @author anti43
 */
public class ProductGroupHandler extends mp3.classes.layer.Things implements mp4.datenbank.struktur.Tabellen {

    private ProductGroupCategory[] categories;
    private ProductGroupFamily[] families;
    private ProductGroupGroup[] groups;
    private ProductGroupCategory category;
    private ProductGroupFamily family;
    private ProductGroupGroup group;
    private Query query;
    private static ProductGroupHandler g;
    public final int CATEGORY = 0;
    public final int FAMILY = 1;
    public final int GROUP = 2;

//
//    public ProductGroupHandler() {
//        new ProductGroupHandler(ConnectionHandler.instanceOf());
//    }
    /**
     * 
     * @return 
     */
    public static ProductGroupHandler instanceOf() {



        if (g == null) {
            g = new ProductGroupHandler(ConnectionHandler.instanceOf());
        }

        return g;

    }

    private ProductGroupHandler(Query query) {

        super(query.clone(TABLE_PRODUCTS_GROUPS_CATEGORIES));

        this.query = query;

        fillCategories();
        fillFamilies();
        fillGroups();
    }

    public void deleteAll() {
        
        super.freeQuery("DELETE from " + TABLE_PRODUCTS_GROUPS_CATEGORIES);
        super.freeQuery("DELETE from " + TABLE_PRODUCTS_GROUPS_FAMILIES);
        super.freeQuery("DELETE from " + TABLE_PRODUCTS_GROUPS_GROUPS);
    }

    public int existFam(String fam) {
        
         for (int b = 0; b < getFamilies().length; b++) {
                    
//                     Log.Debug("matching?: "+getFamilies()[b].getName() + " : " + fam,true);
                    if (getFamilies()[b].getName().matches(fam)) {
                       
                        return getFamilies()[b].getID();
                    }
                }
         return 0;
    }

    /**
     * 
     * @param cat
     * @param type
     * @return
     */
    @SuppressWarnings("fallthrough")
    public int exists(String cat, int type) {
        switch (type) {

            case (0):
                for (int c = 0; c < getCategories().length; c++) {
                    if (getCategories()[c].getName().matches(cat)) {
                       
                        return getCategories()[c].getID();
                    }
                }

                break;
            case (1):

                for (int b = 0; b < getFamilies().length; b++) {
                    
                     Log.Debug("matching?: "+getFamilies()[b].getName() + " : " + cat,true);
                    if (getFamilies()[b].getName().matches(cat)) {
                       
                        return getFamilies()[b].getID();
                    }
                }

                break;
            case (2):

                for (int c = 0; c < getGroups().length; c++) {
                    if (getGroups()[c].getName().matches(cat)) {
                        return getGroups()[c].getID();
                    }
                }

            default:
                return new Integer(0);


        }
        
        return new Integer(0);
    }

 

    private void fillCategories() {
        String[][] tmp = this.select("*", null, null, false, true);
//        Log.Debug("fillcats: " + tmp.length);
        categories =
                new ProductGroupCategory[tmp.length];

        for (int i = 0; i <
                tmp.length; i++) {

            category = new ProductGroupCategory(query);

            try {
                category.setId(Integer.valueOf(tmp[i][0]));

            } catch (NumberFormatException numberFormatException) {
                category.setId(0);
            }

            category.setKategorienummer(tmp[i][1]);
            category.setName(tmp[i][2]);
//            Log.Debug(category.getName());

            categories[i] = category;
        }

    }

    private void fillFamilies() {

        ProductGroupFamily pgf = new ProductGroupFamily(query);


        String[][] tmp = pgf.select("*", null, null, false, true);
        families =
                new ProductGroupFamily[tmp.length];

        for (int i = 0; i <
                tmp.length; i++) {

            family = new ProductGroupFamily(query);

            try {
                family.setId(Integer.valueOf(tmp[i][0]));
//                Log.Debug("fillfam" + Integer.valueOf(tmp[i][0]));

            } catch (NumberFormatException numberFormatException) {
                family.setId(0);
            }

            family.setFamiliennummer(tmp[i][1]);
            family.setKategorieid(Integer.valueOf(tmp[i][2]));
            family.setName(tmp[i][3]);
//            Log.Debug(family.getName());

            families[i] = family;
        }

    }

    private void fillGroups() {
        ProductGroupGroup pgf = new ProductGroupGroup(query);


        String[][] tmp = pgf.select("*", null, null, false, true);
        groups =
                new ProductGroupGroup[tmp.length];

        for (int i = 0; i <
                tmp.length; i++) {

            group = new ProductGroupGroup(query);

            try {
                group.setId(Integer.valueOf(tmp[i][0]));

            } catch (NumberFormatException numberFormatException) {
                group.setId(0);
            }

            group.setGruppennummer(tmp[i][1]);
            group.setFamilienid(Integer.valueOf(tmp[i][2]));
            group.setName(tmp[i][3]);
//            Log.Debug(group.getName());

            groups[i] = group;
        }

    }

    @SuppressWarnings("unchecked")
    public ArrayList getGrps(
            Integer famid) {
        ArrayList groupt = new ArrayList();
        for (int c = 0; c <
                getGroups().length; c++) {
            if (getGroups()[c].getFamilyID().equals(new Integer(famid))) {


                groupt.add(getGroups()[c]);

            }

        }

        return groupt;
    }

    @SuppressWarnings("unchecked")
    public ArrayList getFams(
            Integer catid) {
        ArrayList fats = new ArrayList();
        for (int b = 0; b <
                getFamilies().length; b++) {

            if (getFamilies()[b].getKategorieID().equals(new Integer(catid))) {

                fats.add(getFamilies()[b]);

            }

        }
        return fats;
    }

    /**
     * 
     * @param refresh 
     * @return { ArrayList cats}
     */
    @SuppressWarnings("unchecked")
    public ArrayList getCats(
            boolean refresh) {

        if (refresh) {



            fillCategories();
            fillFamilies();
            fillGroups();

        }

        ArrayList cats = new ArrayList();


        int i = 0, a;


        for (a = 0; a <
                getCategories().length; a++) {
            cats.add(getCategories()[a]);

        }

        

//        Formater.listToIntegerArray(fats);

        return cats;



    }

    public ProductGroupCategory getCategory(
            Integer catId) {
        ProductGroupCategory p = null;
        for (int i = 0; i <
                getCategories().length; i++) {
            if (getCategories()[i].getId().equals(catId.toString())) {

                p = getCategories()[i];

            }

        }
        return p;
    }

    public ProductGroupGroup getGroup(
            Integer groupId) {
        ProductGroupGroup p = null;
        for (int i = 0; i <
                getGroups().length; i++) {
            if (getGroups()[i].getId().equals(groupId.toString())) {

                p = getGroups()[i];

            }

        }
        return p;
    }

    public ProductGroupFamily getFamily(
            Integer familyId) {
        ProductGroupFamily p = null;
        for (int i = 0; i <
                getFamilies().length; i++) {
            if (getFamilies()[i].getId().equals(familyId.toString())) {

                p = getFamilies()[i];

            }

        }
        return p;
    }

    /**
     * 
     * @param group
     * @return whole product path cat -> family -> group
     */
    public String getHierarchyPath(
            Integer group) {

        String str = "";
        for (int j = 0; j <
                getGroups().length; j++) {
            if (getGroups()[j].getId().equals(group.toString())) {
                str = " -> " + getGroups()[j].getName();

                ProductGroupFamily pgf = getFamily(getGroups()[j].getFamilyID());
                str =
                        " -> " + pgf.getName() + str;

                ProductGroupCategory pgc = getCategory(pgf.getKategorieID());
                str =
                        pgc.getName() + str;

            }

        }
        return str;
    }

    /**
     * 
     * @param catname
     * @param catnummer
     * @param famsNgrps {{family-name, group-name, group-name}{family-name, group-name}}
     */
    public void addTree(String catname, Integer catnummer, String[][] famsNgrps) {

        int cat = addCategory(catname, catnummer);

        for (int z = 0; z <
                famsNgrps.length; z++) {

            int fa = addFamily(famsNgrps[z][0], null, cat);
            for (int m = 0; m <
                    famsNgrps[z].length; m++) {

                addGroup(famsNgrps[z][m], null, fa);
            }

        }
    }

    /**
     * 
     * @param name
     * @param nummer (null)
     * @param famid
     * @return groupid
     */
    public int addGroup(String name, Integer nummer, Integer famid) {

        ProductGroupGroup p = new ProductGroupGroup(query);

        p.setName(name);
        if (nummer != null) {
            p.setGruppennummer(nummer.toString());
        } else {

            p.setGruppennummer(p.getNextIndex("gruppenummer").toString());
        }

        p.setFamilienid(famid);

        p.save();
        return p.getID();
    }

    /**
     * 
     * @param name
     * @param nummer (null)
     * @param catid
     * @return famid
     */
    public int addFamily(String name, Integer nummer, Integer catid) {

        ProductGroupFamily p = new ProductGroupFamily(query);

        p.setName(name);
        if (nummer != null) {
            p.setFamiliennummer(nummer.toString());
        } else {

            p.setFamiliennummer(p.getNextIndex("familiennummer").toString());
        }

        p.setKategorieid(catid);

        p.save();
        return p.getID();
    }

    /**
     * 
     * @param name
     * @param nummer (null)
     * @return catid
     */
    public int addCategory(String name, Integer nummer) {

        ProductGroupCategory p = new ProductGroupCategory(query);

        p.setName(name);
        if (nummer != null) {
            p.setKategorienummer(nummer.toString());
        } else {

            p.setKategorienummer(p.getNextIndex("kategorienummer").toString());
        }

        p.save();
        return p.getID();
    }

    public ProductGroupCategory[] getCategories() {
        return categories;
    }

    public ProductGroupFamily[] getFamilies() {
        return families;
    }

    public ProductGroupGroup[] getGroups() {
        return groups;
    }
}
