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

public class LineGraph extends Graph {

    int increment;
    int position;

    public LineGraph(String title, int min, int max) {
        super(title, min, max);
    }

    @Override
    public void paint(Graphics g2) {
        super.paint(g2);

        Graphics2D g = (Graphics2D) g2;
        g.setFont(new Font("Arial", Font.BOLD, 10));

        float[] dash_array = new float[1];
        dash_array[0] = 1; //sichtbar



        BasicStroke s4 = new BasicStroke(
                4f, //Breite
                BasicStroke.CAP_ROUND, //End Style
                BasicStroke.JOIN_ROUND, //Join Style
                1f, //Limit für Join
                dash_array, //Strichelung
                0 //offset in Pixeln f. Strichelung
                );


        g.setStroke(s4);
        increment = (right - left) / (items.size() - 1);
        position = left;
        Color temp = Color.LIGHT_GRAY;
        GraphItem firstItem = (GraphItem) items.firstElement();
        int firstAdjustedValue = bottom - (((firstItem.value - min) * (bottom - top)) / (max - min));
        g.setColor(firstItem.color);
        g.drawString(firstItem.title, position-20,
                firstAdjustedValue - 2);
        g.fillOval(position - 2, firstAdjustedValue - 2, 4, 4);
        g.setColor(temp);
        for (int i = 0; i < items.size() - 1; i++) {
            GraphItem thisItem = (GraphItem) items.elementAt(i);
            int thisAdjustedValue = bottom - (((thisItem.value - min) *
                    (bottom - top)) / (max - min));
            GraphItem nextItem = (GraphItem) items.elementAt(i + 1);
            int nextAdjustedValue = bottom - (((nextItem.value - min) *
                    (bottom - top)) / (max - min));
            g.drawLine(position, thisAdjustedValue,
                    position += increment, nextAdjustedValue);
            g.setColor(nextItem.color);
            if (nextAdjustedValue < thisAdjustedValue) {
                g.drawString(nextItem.title, position - fm.stringWidth(nextItem.title), nextAdjustedValue + titleHeight + 4);
            } else {
                g.drawString(nextItem.title, position - fm.stringWidth(nextItem.title), nextAdjustedValue - 4);
            }
            g.fillOval(position - 2, nextAdjustedValue - 2, 4, 4);
            g.setColor(temp);
        }
    } // end paint
}
