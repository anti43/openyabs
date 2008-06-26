/*
 * 
 *  *  This file is part of MP by anti43 /GPL.
 *  *  
 *  *      MP is free software: you can redistribute it and/or modify
 *  *      it under the terms of the GNU General Public License as published by
 *  *      the Free Software Foundation, either version 3 of the License, or
 *  *      (at your option) any later version.
 *  *  
 *  *      MP is distributed in the hope that it will be useful,
 *  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 * *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *      GNU General Public License for more details.
 *  *  
 *  *      You should have received a copy of the GNU General Public License
 *  *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package mp3.classes.utils;

/**
 *
 * @author anti43
 */
import java.awt.*;
import java.util.*;

public class Graph extends Canvas {
    // variables needed
    public int top;
    public int bottom;
    public int left;
    public int right;
    int titleHeight;
    int labelWidth;
    FontMetrics fm;
    int padding = 4;
    String title;
    int min;
    int max;
    Vector items;

    public Graph(String title, int min, int max) {
        this.title = title;
        this.min = min;
        this.max = max;
        items = new Vector();
    } // end constructor

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        fm = getFontMetrics(getFont());
        titleHeight = fm.getHeight();
        labelWidth = Math.max(fm.stringWidth(new Integer(min).toString()),
                fm.stringWidth(new Integer(max).toString())) + 2;
        top = padding + titleHeight;
        bottom = getSize().height - padding;
        left = padding + labelWidth;
        right = getSize().width - padding;
    } // end reshape

    @Override
    public void paint(Graphics g2) {
        Graphics2D g = (Graphics2D) g2;
        g.setFont(new Font("Arial", Font.BOLD, 14));

        g.setColor(Color.decode("#123307"));

        float[] dash_array = new float[1];
        dash_array[0] = 1; //sichtbar



        BasicStroke s4 = new BasicStroke(
                2f, //Breite
                BasicStroke.CAP_SQUARE, //End Style
                BasicStroke.JOIN_ROUND, //Join Style
                1f, //Limit für Join
                dash_array, //Strichelung
                0 //offset in Pixeln f. Strichelung
                );


        g.setStroke(s4);

        // draw the title
        fm = getFontMetrics(getFont());
        g.drawString(title, padding + 75, top);
        // draw the max and min values

        g.setFont(new Font("Arial", Font.PLAIN, 10));

        g.setColor(Color.LIGHT_GRAY);

        g.drawString("Min", padding, bottom);
        g.drawString("Max", padding, top + titleHeight);
        // draw the vertical and horizontal lines
        g.setColor(Color.GRAY);
        g.drawLine(left, top, left, bottom);
        g.drawLine(left, bottom, right, bottom);
    } // end paint

    @Override
    public Dimension getPreferredSize() {
        return (new Dimension(300, 200));
    }

    class GraphItem {

        String title;
        int value;
        Color color;

        public GraphItem(String title, int value, Color color) {
            this.title = title;
            this.value = value;
            this.color = color;
        } // end constructor
    } // end GraphItem

    @SuppressWarnings("unchecked")
    public void addItem(String name, int value, Color col) {
       
        items.addElement(new GraphItem(name, value, col));
    } // end addItem

    @SuppressWarnings("unchecked")
    public void addItem(String name, int value) {
         items.addElement(new GraphItem(name, value, Color.black));
    } // end addItem

    public void removeItem(String name) {
        for (int i = 0; i < items.size(); i++) {
            if (((GraphItem) items.elementAt(i)).title.equals(name)) {
                items.removeElementAt(i);
            }
        }
    } // end removeItem
} // end Graph

