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
package mp4.cache;

import mp3.classes.visual.main.*;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import mp3.classes.utils.Log;
import mp4.klassen.objekte.*;

/**
 *
 * @author Andreas
 */
public class undoCache {

    private static undoCache dat;
    private static JMenu menu;
    private static mainframe mainframe;
    
    public static final int CREATE = 1;
    public static final int DELETE = 2;
    public static final int EDIT = 3;
    public static final int R_STORNO = 4;


    private int index = 1;
    
    
    
    public static undoCache instanceOf() {
        if (dat == null) {
            dat = new undoCache();
           
            return dat;
        }
        return dat;
    }

    public static void setMenu(JMenu jmenu,mainframe parent) {
        menu = jmenu;
        mainframe = parent;
    }
    
    

    public undoCache() {
    }

    public void addItem(Object item, int action) {
        addToMenu(index, action, item);
        index++;
    }

//    public void removeItem(int itemIndex) {
//
//        Component[] mi = menu.getComponents();
//
//        for (int i = 0; i < mi.length; i++) {
//            menuItem item = (menuItem) menu.getComponent(i);
//            if(item.getItemIndex().intValue() == itemIndex) {
//                menu.remove(i);
//            }    
//        }
//        menu.validate();
//    }

    private void addToMenu(int itemIndex, int action, Object item) {
        String text = getActionText(item, action);
        menu.add(new menuItem(itemIndex, action, text, item, this, menu));
        menu.validate();
    }

    private void doAction(int index, int aktion, Object item, String text) {
        if (item.getClass().isInstance(new Rechnung())) {

            Rechnung it = (Rechnung) item;

            switch (aktion) {

                case 1:
                    it.destroy();
                    break;
                case 4:
                    it.setStorno(false);
                    it.save();
                    break;
                case 3:
                    it.save();
                    break;

            }

        } else if (item.getClass().isInstance(new Angebot())) {

            Angebot it = (Angebot) item;

            switch (aktion) {

                case 1:
                    it.destroy();
                    break;
                case 2:
                    it.unDelete(it.getId());
                    break;
                case 3:
                    it.save();
                    break;

            }


        } else if (item.getClass().isInstance(new Einnahme())) {

            Einnahme it = (Einnahme) item;


            switch (aktion) {

                case 1:
                    it.disable();
                    break;
                case 2:

                   it.enable();
                    break;
                case 3:
                    it.save();
                    break;

            }
            
            mainframe.getEURPanel().getEurepanel().updateTableData();
            new HistoryItem("Undo","Rückgängig: " + text);
            
            
        } else if (item.getClass().isInstance(new Ausgabe())) {

            Ausgabe it = (Ausgabe) item;


            switch (aktion) {

                case 1:
                    it.destroy();
                    break;
                case 2:

                    it.unDelete(it.getId());
                    break;
                case 3:
                    it.save();
                    break;

            }
        } else if (item.getClass().isInstance(new Customer())) {

            Customer it = (Customer) item;


            switch (aktion) {

                case 1:
                    it.destroy();
                    break;
                case 2:

//                    it.unDelete(it.getId());
                    break;
                case 3:
                    it.save();
                    break;

            }
        } else if (item.getClass().isInstance(new Lieferant())) {

            Lieferant it = (Lieferant) item;

            switch (aktion) {

                case 1:
                    it.destroy();
                    break;
                case 2:

//                    it.unDelete(it.getId());
                    break;
                case 3:
                    it.save();
                    break;

            }
        }

//        removeItem(index);
    }

    private String getActionText(Object item, int action) {
        String text = "";

        switch (action) {

            case 1:
                text = " erstellt.";
                break;
            case 2:
                text = " gelöscht.";
                break;
            case 3:
                text = " editiert.";
                break;
        }




        if (item.getClass().isInstance(new Rechnung())) {

            Rechnung it = (Rechnung) item;
            text = "Rechnung Nummer " + it.getRechnungnummer() + text;

        } else if (item.getClass().isInstance(new Angebot())) {

            Angebot it = (Angebot) item;
            text = "Angebot Nummer " + it.getOrdernummer() + text;

        } else if (item.getClass().isInstance(new Einnahme())) {

            Einnahme it = (Einnahme) item;
            text = "Einnahme Nummer " + it.getId() + text;

        } else if (item.getClass().isInstance(new Ausgabe())) {

            Ausgabe it = (Ausgabe) item;
            text = "Ausgabe Nummer " + it.getId() + text;

        } else if (item.getClass().isInstance(new Customer())) {

            Customer it = (Customer) item;
            text = "Kunde Nummer " + it.getKundennummer() + text;

        } else if (item.getClass().isInstance(new Lieferant())) {

            Lieferant it = (Lieferant) item;
            text = "Lieferant Nummer " + it.getLieferantennummer() + text;
        }



        return text;
    }

    class menuItem extends JMenuItem {

        private int index = 0;
        private int aktion = 0;
        private Object item;
        private undoCache chandler;
        private JMenu menu;

        public menuItem(int itemIndex, int aktion, String text, Object item, undoCache cachehandler, JMenu menu) {
            super(text);
            this.index = itemIndex;
            this.aktion = aktion;
            this.item = item;
            this.chandler = cachehandler;
            this.menu = menu;

            this.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    doAction();
                }
            });

        }

        private void doAction() {
            try {
                chandler.doAction(index, aktion, item, this.getText());
                menu.remove(this);
                menu.validate();
            } catch (Exception e) {
                Log.Debug(e);
            }
        }

        public int getAktion() {
            return index;
        }

        public Integer getItemIndex() {
            return index;
        }

        public Object getItem() {
            return item;
        }
    }
}
