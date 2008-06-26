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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 *
 * @author anti43
 */
public class BarChart extends Graph {
   int position;
   int increment;
   public BarChart(String title, int min, int max) {
      super(title, min, max);
   } // end constructor
   
    @Override
   public void paint(Graphics g2) {
      super.paint(g2);
       Graphics2D g = (Graphics2D)g2;
      
       g.setFont(new Font("Arial", Font.BOLD, 10));
          
      increment = (right - left)/(items.size());
      position = left;
      Color temp = g.getColor();
      for (int i = 0; i < items.size(); i++) {
         GraphItem item = (GraphItem)items.elementAt(i);
         int adjustedValue = bottom - (((item.value - min)*(bottom - top))
                                       /(max - min));
         g.drawString(item.title, position + (increment -
                      fm.stringWidth(item.title))/2, adjustedValue - 2);
         g.setColor(item.color);
         g.fillRect(position, adjustedValue, increment,
                    bottom - adjustedValue);
         position+=increment;
         g.setColor(temp);
      }
   } // end paint
} // end BarChart
