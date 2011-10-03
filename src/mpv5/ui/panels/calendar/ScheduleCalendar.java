/*
 *  JCalendar.java  - A bean for choosing a date
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

import java.awt.BorderLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import mpv5.globals.LocalSettings;

/**
 * JCalendar is a bean for entering a date by choosing the year, month and day.
 * 
 * @author Kai Toedter
 * @version $LastChangedRevision: 95 $
 * @version $LastChangedDate: 2006-05-05 18:43:15 +0200 (Fr, 05 Mai 2006) $
 */
public final class ScheduleCalendar extends JPanel implements PropertyChangeListener {

    private static final long serialVersionUID = 8913369762644440133L;

    private Calendar calendar;
    /** the day chooser */
    protected ScheduleCalendarDayChooser dayChooser;
    /** indicates if weeks of year shall be visible */
    protected boolean weekOfYearVisible = true;
    /** the locale */
    protected Locale locale;
    /** the month chooser */
    protected ScheduleMonthChooser monthChooser;
    private JPanel monthYearPanel;
    /** the year chhoser */
    protected ScheduleYearChooser yearChooser;
    protected Date minSelectableDate;
    protected Date maxSelectableDate;
    private static ScheduleCalendar icke;

    /**
     * The jc instance
     * @return
     */
    public static ScheduleCalendar instanceOf() {
        if (icke == null) {
            icke = new ScheduleCalendar(null, null, true, true);
        }
        return icke;
    }
   
    
    /**
     * JCalendar constructor with month spinner parameter.
     *
     * @param date
     *            the date
     * @param locale
     *            the locale
     * @param monthSpinner
     *            false, if no month spinner should be used
     * @param weekOfYearVisible
     *            true, if weeks of year shall be visible
     */
    private ScheduleCalendar(Date date, Locale locale, boolean monthSpinner, boolean weekOfYearVisible) {
      
        // needed for setFont() etc.
        dayChooser = null;
        monthChooser = null;
        yearChooser = null;
        this.weekOfYearVisible = weekOfYearVisible;

        if (locale == null) {
            this.locale = Locale.getDefault();
        } else {
            this.locale = locale;
        }

        calendar = Calendar.getInstance();

        setLayout(new BorderLayout());

        monthYearPanel = new JPanel();
        monthYearPanel.setLayout(new BorderLayout());

        monthChooser = new ScheduleMonthChooser(monthSpinner);
        yearChooser = new ScheduleYearChooser();
        yearChooser.setSize(yearChooser.getWidth(), yearChooser.getHeight() + 20);
        monthChooser.setSize(monthChooser.getWidth(), monthChooser.getHeight() + 20);
        monthChooser.setYearChooser(yearChooser);
        monthYearPanel.add(monthChooser, BorderLayout.WEST);
        monthYearPanel.add(yearChooser, BorderLayout.CENTER);
        monthYearPanel.setBorder(BorderFactory.createEmptyBorder());

        dayChooser = ScheduleCalendarDayChooser.instanceOf();
        dayChooser.addPropertyChangeListener(this);
        monthChooser.setDayChooser(dayChooser);
        monthChooser.addPropertyChangeListener(this);
        yearChooser.setDayChooser(dayChooser);
        yearChooser.addPropertyChangeListener(this);
        add(monthYearPanel, BorderLayout.NORTH);
        add(dayChooser, BorderLayout.CENTER);

        // Set the initialized flag before setting the calendar. This will
        // cause the other components to be updated properly.
        if (date != null) {
            calendar.setTime(date);
        }

        setCalendar(calendar);
        yearChooser.setFont(Font.decode(LocalSettings.getProperty(LocalSettings.DEFAULT_FONT)).deriveFont(Font.BOLD, 14));
        monthChooser.setFont(Font.decode(LocalSettings.getProperty(LocalSettings.DEFAULT_FONT)).deriveFont(Font.PLAIN, 14));
    }

    /**
     * Gets the dayChooser attribute of the JCalendar object
     *
     * @return the dayChooser value
     */
    public ScheduleCalendarDayChooser getDayChooser() {
        return dayChooser;
    }

    /**
     * Gets the monthChooser attribute of the JCalendar object
     *
     * @return the monthChooser value
     */
    public ScheduleMonthChooser getMonthChooser() {
        return monthChooser;
    }

    /**
     * Gets the yearChooser attribute of the JCalendar object
     *
     * @return the yearChooser value
     */
    public ScheduleYearChooser getYearChooser() {
        return yearChooser;
    }

    /**
     * JCalendar is a PropertyChangeListener, for its day, month and year
     * chooser.
     *
     * @param evt
     *            the property change event
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (calendar != null) {
            Calendar c = (Calendar) calendar.clone();

            if (evt.getPropertyName().equals("day")) {
                c.set(Calendar.DAY_OF_MONTH, ((Integer) evt.getNewValue()).intValue());
                setCalendar(c, false);
            } else if (evt.getPropertyName().equals("month")) {
                c.set(Calendar.MONTH, ((Integer) evt.getNewValue()).intValue());
                setCalendar(c, false);
            } else if (evt.getPropertyName().equals("year")) {
                c.set(Calendar.YEAR, ((Integer) evt.getNewValue()).intValue());
                setCalendar(c, false);
            } else if (evt.getPropertyName().equals("date")) {
                c.setTime((Date) evt.getNewValue());
                setCalendar(c, true);
            }
        }
    }

    /**
     * Sets the calendar property. This is a bound property.
     *
     * @param c
     *            the new calendar
     * @throws NullPointerException -
     *             if c is null;
     * @see #getCalendar
     */
    public void setCalendar(Calendar c) {
        setCalendar(c, true);
    }

    /**
     * Sets the calendar attribute of the JCalendar object
     *
     * @param c
     *            the new calendar value
     * @param update
     *            the new calendar value
     * @throws NullPointerException -
     *             if c is null;
     */
    private void setCalendar(Calendar c, boolean update) {
        if (c == null) {
            setDate(null);
        }
        Calendar oldCalendar = calendar;
        calendar = c;

        if (update) {
            // Thanks to Jeff Ulmer for correcting a bug in the sequence :)
            yearChooser.setYear(c.get(Calendar.YEAR));
            monthChooser.setMonth(c.get(Calendar.MONTH));
            dayChooser.setDay(c.get(Calendar.DATE));
        }

        firePropertyChange("calendar", oldCalendar, calendar);
    }

    /**
     * Returns a Date object.
     *
     * @return a date object constructed from the calendar property.
     */
    public Date getDate() {
        return new Date(calendar.getTimeInMillis());
    }

    /**
     * Sets the date. Fires the property change "date".
     *
     * @param date
     *            the new date.
     * @throws NullPointerException -
     *             if tha date is null
     */
    public void setDate(Date date) {
        Date oldDate = calendar.getTime();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        yearChooser.setYear(year);
        monthChooser.setMonth(month);
        dayChooser.setCalendar(calendar);
        dayChooser.setDay(day);
        
        firePropertyChange("date", oldDate, date);
    }
}
