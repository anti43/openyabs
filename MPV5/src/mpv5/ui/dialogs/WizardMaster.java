/*
 *  This file is part of MP by anti43 /GPL.
 *
 *  MP is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */

package mpv5.ui.dialogs;

import java.awt.Cursor;
import java.util.ArrayList;
import mpv5.data.PropertyStore;


/**
 *
 * @author Administrator
 */
public interface WizardMaster {

    public void dispose();
    public void isEnd(boolean end);
    public PropertyStore getStore();
    public void setCursor(Cursor cursor);
    public void setMessage(String message);
}