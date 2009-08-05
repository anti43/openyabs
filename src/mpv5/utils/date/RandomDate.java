/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.date;

import java.util.Date;
import java.util.Random;

/**
 * Creates a random date within the given timeframe
 *  
 */
public class RandomDate extends Date {

    private static final long serialVersionUID = 1L;

    public RandomDate(vTimeframe timeframe) {
        if (timeframe != null) {
            long start = timeframe.getStart().getTime();
            long end = timeframe.getEnd().getTime();
            Random r = new Random();
            long randomTS = (long) (r.nextDouble() * (end - start)) + start;
            this.setTime(randomTS);
        }
    }
}
