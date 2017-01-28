/*
 *  JYearChooser.java  - A bean for choosing a year
 *  Copyright (C) 2004 Kai Toedter
 *  kai@toedter.com
 *  www.toedter.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package mpv5.ui.panels.calendar;

import com.toedter.components.JSpinField;

import java.util.Calendar;

/**
 * JYearChooser is a bean for choosing a year.
 *
 * @version $LastChangedRevision: 85 $
 * @version $LastChangedDate: 2006-04-28 13:50:52 +0200 (Fr, 28 Apr 2006) $
 */
public class ScheduleYearChooser extends JSpinField {
    private static final long serialVersionUID = 2648810220491090064L;
    protected int oldYear;
    protected int startYear;
    protected int endYear;

    /**
     * Default JCalendar constructor.
     */
    public ScheduleYearChooser() {
        setName("JYearChooser");
        Calendar calendar = Calendar.getInstance();
        setMinimum(calendar.getMinimum(Calendar.YEAR));
        setMaximum(calendar.getMaximum(Calendar.YEAR));
        setValue(calendar.get(Calendar.YEAR));
    }

    /**
     * Sets the year. This is a bound property.
     *
     * @param y the new year
     *
     * @see #getYear
     */
    public void setYear(int y) {
        super.setValue(y, true, false);

        spinner.setValue(value);
        firePropertyChange("year", oldYear, value);
        oldYear = value;
    }

    /**
     * Sets the year value.
     *
     * @param value the year value
     */
    @Override
    public final void setValue(int value) {
        setYear(value);
    }

    /**
     * Returns the year.
     *
     * @return the year
     */
    public int getYear() {
        return super.getValue();
    }

    /**
     * Returns the endy ear.
     *
     * @return the end year
     */
    public int getEndYear() {
        return getMaximum();
    }

    /**
     * Sets the end ear.
     *
     * @param endYear the end year
     */
    public void setEndYear(int endYear) {
        setMaximum(endYear);
    }

    /**
     * Returns the start year.
     *
     * @return the start year.
     */
    public int getStartYear() {
        return getMinimum();
    }

    /**
     * Sets the start year.
     *
     * @param startYear the start year
     */
    public void setStartYear(int startYear) {
        setMinimum(startYear);
    }
}
