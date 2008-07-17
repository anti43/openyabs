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

import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import mp4.klassen.objekte.*;

/**
 *
 * @author Andreas
 */
public class backupCache {

    private ArrayList cache = new ArrayList(100);
    private JMenu menu;
    
    public final int CREATE = 1;
    public final int DELETE = 2;
    public final int EDIT = 3;
    
    private int index = 1;
    
    public backupCache(JMenu menu){
    
        this.menu = menu;
    }
    
    public void addItem(Object item, int action){
        cache.set(index, new Object[]{item, action});
        addToMenu(index, action, item);
        index++;
    }
    
    public void removeItem(int itemIndex){   
        cache.remove(itemIndex);   
    }
    
    private void addToMenu(int itemIndex, int action, Object item){
    
        if(item.getClass().isInstance(new Rechnung()))
        ;
        else if(item.getClass().isInstance(new Angebot()))
                ;
        else if(item.getClass().isInstance(new Einnahme()))
                    ;
        else if(item.getClass().isInstance(new Ausgabe()))
                        ;
        else if(item.getClass().isInstance(new Customer()))
                            ;
        else if(item.getClass().isInstance(new Lieferant()))
            ;
            
        menu.add(new menuItem(itemIndex, ""));
        menu.validate();
        
    
    }
    
    class menuItem extends JMenuItem{
       
        private int index = 0;
        
        public menuItem(int index, String text){
            super(text);
            this.index = index;
        }
        
        public int getItemIndex(){        
            return index;
        }
    }

}
