/*
 * 
This file is part of MP by anti43 /GPL.

MP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with MP.  If not, see <http://www.gnu.org/licenses/>.

 * 
 */
package mp4.utils.zahlen;

import java.text.NumberFormat;
import mp4.klassen.objekte.MyData;

/**
 *
 * @author Administrator
 */
public class FormatMoney {

    public static String formatLokal(Double betrag) {
        NumberFormat n = NumberFormat.getCurrencyInstance(MyData.instanceOf().getLocale());
        return n.format(betrag);
    }
}
