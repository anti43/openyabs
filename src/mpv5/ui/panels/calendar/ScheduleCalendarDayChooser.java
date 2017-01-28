/*
 *  JDayChooser.java  - A bean for choosing a day
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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;

import java.text.DateFormatSymbols;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import mpv5.db.objects.Schedule;
import mpv5.logging.Log;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import org.apache.commons.lang.time.DateUtils;

/**
 * JDayChooser is a bean for choosing a day.
 * 
 * @author Kai Toedter
 * @version $LastChangedRevision: 104 $
 * @version $LastChangedDate: 2006-06-04 15:20:45 +0200 (So, 04 Jun 2006) $
 */
public final class ScheduleCalendarDayChooser extends JPanel implements KeyListener, ActionListener {

    private static final long serialVersionUID = 5876398337018781820L;
    protected ScheduleCalendarButton[] days;
    protected JButton[] weeks;
    protected ScheduleCalendarButton selectedDay;
    protected JPanel weekPanel;
    protected JPanel dayPanel;
    protected int day;
    protected Color oldDayBackgroundColor;
    protected Color selectedColor;
    protected Color sundayForeground;
    protected Color weekdayForeground;
    protected Color decorationBackgroundColor;
    protected String[] dayNames;
    protected Calendar calendar;
    protected Calendar today;
    protected Locale locale;
    protected boolean initialized;
    protected boolean weekOfYearVisible;
    protected boolean decorationBackgroundVisible = false;
    protected boolean decorationBordersVisible;
    protected boolean dayBordersVisible;
    protected Date minSelectableDate;
    protected Date maxSelectableDate;
    protected Date defaultMinSelectableDate;
    protected Date defaultMaxSelectableDate;
    protected int maxDayCharacters;
    private static ArrayList<Schedule> events;
    private static ScheduleCalendarDayChooser icke;

    /**
     * The dc instance
     * @return
     */
    public static ScheduleCalendarDayChooser instanceOf() {
        if (icke == null) {
            icke = new ScheduleCalendarDayChooser(true);
        }
        return icke;
    }

    /**
     * JDayChooser constructor.
     *
     * @param weekOfYearVisible
     *            true, if the weeks of a year shall be shown
     */
    private ScheduleCalendarDayChooser(boolean weekOfYearVisible) {
        setName("JDayChooser");
        setBackground(Color.blue);
        this.weekOfYearVisible = weekOfYearVisible;
        locale = Locale.getDefault();
        days = new ScheduleCalendarButton[49];
        selectedDay = null;
        calendar = Calendar.getInstance(locale);
        today = (Calendar) calendar.clone();

        setLayout(new BorderLayout());

        dayPanel = new JPanel();
        dayPanel.setLayout(new GridLayout(7, 7));

        maxDayCharacters = 2;

        sundayForeground = new Color(164, 0, 0);
        weekdayForeground = new Color(0, 90, 164);

        decorationBackgroundColor = new Color(238, 238, 238);

        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                int index = x + (7 * y);

                if (y == 0) {
                    // Create a button that doesn't react on clicks or focus
                    // changes.
                    // Thanks to Thomas Schaefer for the focus hint :)
                    days[index] = new DecoratorButton();
                } else {
                    days[index] = new ScheduleCalendarButton(new Date()) {

                        private static final long serialVersionUID = -7433645992591669725L;

                        @Override
                        public void paint(Graphics g) {
                            if ("Windows".equals(UIManager.getLookAndFeel().getID())) {
                                // this is a hack to get the background painted
                                // when using Windows Look & Feel
                                if (selectedDay == this) {
                                    g.setColor(selectedColor);
                                    g.fillRect(0, 0, getWidth(), getHeight());
                                }
                            }
                            super.paint(g);
                        }
                    };
                    days[index].addActionListener(this);
                    days[index].addKeyListener(this);
                }

                days[index].setMargin(new Insets(1, 1, 1, 1));
                days[index].setFocusPainted(true);
                days[index].setContentAreaFilled(true);
                days[index].setBorderPainted(true);
                dayPanel.add(days[index]);
            }
        }

        weekPanel = new JPanel();
        weekPanel.setLayout(new GridLayout(7, 1));
        weeks = new JButton[7];

        for (int i = 0; i < 7; i++) {
            weeks[i] = new DecoratorButton();
            weeks[i].setMargin(new Insets(0, 0, 0, 0));
            weeks[i].setFocusPainted(false);
            weeks[i].setForeground(new Color(100, 100, 100));

            if (i != 0) {
                weeks[i].setText("0" + (i + 1));
            }

            weekPanel.add(weeks[i]);
        }

        Calendar tmpCalendar = Calendar.getInstance();
        tmpCalendar.set(1, 0, 1, 1, 1);
        defaultMinSelectableDate = tmpCalendar.getTime();
        minSelectableDate = defaultMinSelectableDate;
        tmpCalendar.set(9999, 0, 1, 1, 1);
        defaultMaxSelectableDate = tmpCalendar.getTime();
        maxSelectableDate = defaultMaxSelectableDate;

        init();

        setDay(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        add(dayPanel, BorderLayout.CENTER);
        add(weekPanel, BorderLayout.WEST);
        updateUI();
    }

    /**
     * Initilizes the locale specific names for the days of the week.
     */
    private void init() {

        oldDayBackgroundColor = Color.WHITE;
        selectedColor = new Color(160, 160, 160);

        Date date = calendar.getTime();
        calendar = Calendar.getInstance(locale);
        calendar.setTime(date);

        drawDayNames();
        drawDays(null);
    }

    /**
     * Draws the day names of the day columnes.
     *
     */
    private void drawDayNames() {
        int firstDayOfWeek = calendar.getFirstDayOfWeek();
        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
        dayNames = dateFormatSymbols.getShortWeekdays();

        int fwd = firstDayOfWeek;

        for (int i = 0; i < 7; i++) {
            if (maxDayCharacters > 0 && maxDayCharacters < 5) {
                if (dayNames[fwd].length() >= maxDayCharacters) {
                    dayNames[fwd] = dayNames[fwd].substring(0, maxDayCharacters);
                }
            }

            days[i].setText(dayNames[fwd]);
            days[i].setFont(days[i].getFont().deriveFont(Font.BOLD));

            if (fwd == 1) {
                days[i].setForeground(sundayForeground);
            } else {
                days[i].setForeground(weekdayForeground);
            }

            if (fwd < 7) {
                fwd++;
            } else {
                fwd -= 6;
            }
        }
    }

    /**
     * Hides and shows the week buttons.
     */
    private void drawWeeks() {
        Calendar tmpCalendar = (Calendar) calendar.clone();

        for (int i = 1; i < 7; i++) {
            tmpCalendar.set(Calendar.DAY_OF_MONTH, (i * 7) - 6);

            int week = tmpCalendar.get(Calendar.WEEK_OF_YEAR);
            String buttonText = Integer.toString(week);

            if (week < 10) {
                buttonText = "0" + buttonText;
            }

            weeks[i].setText(buttonText);
            weeks[i].setContentAreaFilled(false);

            if ((i == 5) || (i == 6)) {
                weeks[i].setVisible(days[i * 7].isVisible());
            }
        }

        setDayBordersVisible(true);

    }

    /**
     * Hides and shows the day buttons.
     * @param list
     */
    private void drawDays(ArrayList<Schedule> list) {

        if (list != null) {
            events = list;
        }

        Calendar tmpCalendar = (Calendar) calendar.clone();
        tmpCalendar.set(Calendar.HOUR_OF_DAY, 0);
        tmpCalendar.set(Calendar.MINUTE, 0);
        tmpCalendar.set(Calendar.SECOND, 0);
        tmpCalendar.set(Calendar.MILLISECOND, 0);

        Calendar minCal = Calendar.getInstance();
        minCal.setTime(minSelectableDate);
        minCal.set(Calendar.HOUR_OF_DAY, 0);
        minCal.set(Calendar.MINUTE, 0);
        minCal.set(Calendar.SECOND, 0);
        minCal.set(Calendar.MILLISECOND, 0);

        Calendar maxCal = Calendar.getInstance();
        maxCal.setTime(maxSelectableDate);
        maxCal.set(Calendar.HOUR_OF_DAY, 0);
        maxCal.set(Calendar.MINUTE, 0);
        maxCal.set(Calendar.SECOND, 0);
        maxCal.set(Calendar.MILLISECOND, 0);

        int firstDayOfWeek = tmpCalendar.getFirstDayOfWeek();
        tmpCalendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDay = tmpCalendar.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek;

        if (firstDay < 0) {
            firstDay += 7;
        }

        int i;

        for (i = 0; i < firstDay; i++) {
            days[i + 7].setVisible(false);
            days[i + 7].setText("");
        }

        tmpCalendar.add(Calendar.MONTH, 1);

        Date firstDayInNextMonth = tmpCalendar.getTime();
        tmpCalendar.add(Calendar.MONTH, -1);

        Date time = tmpCalendar.getTime();
        int n = 0;
        Color foregroundColor = getForeground();

        while (time.before(firstDayInNextMonth)) {
            days[i + n + 7].setDate(time);
            days[i + n + 7].setText(Integer.toString(n + 1));
            days[i + n + 7].setVisible(true);
            Log.Debug(this, "Creating Day: " + days[i + n + 7].getText());

            if ((tmpCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) && (tmpCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR))) {
                days[i + n + 7].setForeground(sundayForeground);
            } else {
                days[i + n + 7].setForeground(foregroundColor);
            }

            if ((n + 1) == this.day) {
                days[i + n + 7].setBackground(selectedColor);
                selectedDay = days[i + n + 7];
            } else {
                days[i + n + 7].setBackground(oldDayBackgroundColor);
            }

            if (tmpCalendar.before(minCal) || tmpCalendar.after(maxCal)) {
                days[i + n + 7].setEnabled(false);
            } else {
                days[i + n + 7].setEnabled(true);
            }

            Log.Debug(this, "Checking events");
            int j = 0;
            if (events != null) {
                days[i + n + 7].clearScheduledEvents();
                while (!events.isEmpty() && events.size() > j) {
                    Schedule schedule = events.get(j);
                    Log.Debug(this, "Checking date: "
                            + schedule.getDate()
                            + " against "
                            + days[i + n + 7].getText());
                    if (schedule.getDate().contains(time)) {
                        Log.Debug(this, "Found date: " + schedule.getDate());
                        days[i + n + 7].addScheduledEvent(schedule);
                        events.remove(j);
                    } else { 
                        j++;
                    }
                }
            }
            
            n++;
            tmpCalendar.add(Calendar.DATE, 1);
            time = tmpCalendar.getTime();
        }

        for (int k = n + i + 7; k < 49; k++) {
            days[k].setVisible(false);
            days[k].setText("");
        }

        drawWeeks();
    }

    /**
     * Returns the locale.
     *
     * @return the locale value
     *
     * @see #setLocale
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale.
     *
     * @param locale
     *            the new locale value
     *
     * @see #getLocale
     */
    @Override
    public void setLocale(Locale locale) {
        if (!initialized) {
            super.setLocale(locale);
        } else {
            this.locale = locale;
            super.setLocale(locale);
            init();
        }
    }

    /**
     * Sets the day. This is a bound property.
     *
     * @param d
     *            the day
     *
     * @see #getDay
     */
    private void setDay(int d) {
        if (d < 1) {
            d = 1;
        }
        Calendar tmpCalendar = (Calendar) calendar.clone();
        tmpCalendar.set(Calendar.DAY_OF_MONTH, 1);
        tmpCalendar.add(Calendar.MONTH, 1);
        tmpCalendar.add(Calendar.DATE, -1);

        int maxDaysInMonth = tmpCalendar.get(Calendar.DATE);

        if (d > maxDaysInMonth) {
            d = maxDaysInMonth;
        }

        Date oldDate = null;
        day = d;

        if (selectedDay != null) {
            oldDate = selectedDay.getDate();
            selectedDay.setBackground(oldDayBackgroundColor);
            selectedDay.repaint();
        }

        for (int i = 7; i < 49; i++) {
            if (days[i].getText().equals(Integer.toString(day))) {
                selectedDay = days[i];
                selectedDay.setBackground(selectedColor);
                break;
            }
        }

        firePropertyChange("date", oldDate, selectedDay.getDate());
    }

    /**
     * Returns the selected day.
     *
     * @return the day value
     *
     * @see #setDay
     */
    private int getDay() {
        return day;
    }

    /**
     * Sets the Date to the chooser
     * @param tday 
     */
    protected void setDate(Date tday) {
        calendar.setTime(tday);
        setDay(calendar.get(Calendar.DAY_OF_MONTH));
        drawDays(
                Schedule.getEvents2(
                new vTimeframe(
                DateConverter.getStartOfMonth(calendar.getTime()),
                DateConverter.getEndOfMonth(calendar.getTime()))));
    }

    /**
     * Sets the font property.
     *
     * @param font
     *            the new font
     */
    @Override
    public void setFont(Font font) {
        if (days != null) {
            for (int i = 0; i < 49; i++) {
                days[i].setFont(font);
            }
        }
        if (weeks != null) {
            for (int i = 0; i < 7; i++) {
                weeks[i].setFont(font);
            }
        }
    }

    /**
     * JDayChooser is the KeyListener for all day buttons. (Added by Thomas
     * Schaefer and modified by Austin Moore)
     *
     * @param e
     *            the KeyEvent
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int offset = (e.getKeyCode() == KeyEvent.VK_UP) ? (-7)
                : ((e.getKeyCode() == KeyEvent.VK_DOWN) ? (+7)
                : ((e.getKeyCode() == KeyEvent.VK_LEFT) ? (-1)
                : ((e.getKeyCode() == KeyEvent.VK_RIGHT) ? (+1) : 0)));

        int newDay = getDay() + offset;

        if ((newDay >= 1) && (newDay <= calendar.getMaximum(Calendar.DAY_OF_MONTH))) {
            setDay(newDay);
        }
    }

    /**
     * Does nothing.
     *
     * @param e
     *            the KeyEvent
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Does nothing.
     *
     * @param e
     *            the KeyEvent
     */
    @Override
    public void keyReleased(KeyEvent e) {
    }

    public void setDayBordersVisible(boolean dayBordersVisible) {
        this.dayBordersVisible = dayBordersVisible;
        if (initialized) {

            for (int x = 0; x < 7; x++) {
                days[x].setContentAreaFilled(false);
                days[x].setBorder(new LineBorder(Color.white, 1, false));
                days[x].setBorderPainted(true);
            }

            for (int x = 7; x < 49; x++) {
                days[x].setContentAreaFilled(false);
                days[x].setBorderPainted(dayBordersVisible);
                days[x].setBorder(new LineBorder(Color.darkGray, 1, false));
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Date oldDate = this.selectedDay.getDate();
        this.selectedDay = (ScheduleCalendarButton) e.getSource();
        
        firePropertyChange("date", oldDate, this.selectedDay.getDate());
    }

    /**
     * Updates the Event-Cache for the selected day 
     * @param nday
     * @param sched
     * @param add 
     */
    protected void refreshDayEventsDate(Date nday, Schedule sched, Boolean add) {
        Log.Debug(this, "aktualisieren DaySelektor ... geklickt");
        if (this.selectedDay.getDate() != null
                && DateUtils.isSameDay(nday, this.selectedDay.getDate())) {
            if (add) {
                Log.Debug(this, "aktualisieren DaySelektor ... a");
                this.selectedDay.addScheduledEvent(sched);
            } else {
                Log.Debug(this, "aktualisieren DaySelektor ... b");
                this.selectedDay.removeScheduledEvent(sched);
            }
        }
        Log.Debug(this, "aktualisieren DaySelektor ... fertig");
    }

    /**
     * returns the events of the selected Day
     * @return 
     */
    protected ArrayList<Schedule> getScheduledEvents() {
        if (this.selectedDay.getDate() != null) {
            return this.selectedDay.getScheduledEvents();
        }
        return null;
    }

    /**
     * Creates a JFrame with a JDayChooser inside and can be used for testing.
     *
     * @param s
     *            The command line arguments
     */
    class DecoratorButton extends ScheduleCalendarButton {

        private static final long serialVersionUID = -5306477668406547496L;

        public DecoratorButton() {
            super(new Date());
            setBackground(decorationBackgroundColor);
            setContentAreaFilled(decorationBackgroundVisible);
            setBorderPainted(decorationBordersVisible);
        }

        @Override
        public void addMouseListener(MouseListener l) {
        }

        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public void paint(Graphics g) {
            if ("Windows".equals(UIManager.getLookAndFeel().getID())) {
                // this is a hack to get the background painted
                // when using Windows Look & Feel
                if (decorationBackgroundVisible) {
                    g.setColor(decorationBackgroundColor);
                } else {
                    g.setColor(days[7].getBackground());
                }
                g.fillRect(0, 0, getWidth(), getHeight());
                if (isBorderPainted()) {
                    setContentAreaFilled(true);
                } else {
                    setContentAreaFilled(false);
                }
            }
            super.paint(g);
        }
    };
}
